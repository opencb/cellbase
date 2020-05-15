/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.opencb.cellbase.lib.builders;

import org.opencb.biodata.models.core.Xref;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneBuilderIndexer {

    private RocksDB rocksdb;
    private static final String DESCRIPTION_SUFFIX = "_description";
    private static final String XREF_SUFFIX = "_xref";
    private static final String PROTEIN_XREF_SUFFIX = "_protein_xref";
    private RocksDbManager rocksDbManager;
    protected Logger logger;
    private Options dbOption = null;
    private String dbLocation = null;

    public GeneBuilderIndexer(Path geneDirectoryPath) {
        init(geneDirectoryPath);
    }

    private void init(Path geneDirectoryPath) {
        rocksDbManager = new RocksDbManager();
        dbLocation = geneDirectoryPath.toString() + "/integration.idx";
        rocksdb = rocksDbManager.getDBConnection(dbLocation);
        dbOption = new Options().setCreateIfMissing(true);

        logger = LoggerFactory.getLogger(this.getClass());
    }

    public void index(Path geneDescriptionFile, Path xrefsFile, Path uniprotIdMappingFile)
            throws IOException, RocksDBException {
        indexDescriptions(geneDescriptionFile);
        indexXrefs(xrefsFile, uniprotIdMappingFile);
    }

    public String getDescription(String id) throws RocksDBException {
        String key = id + DESCRIPTION_SUFFIX;
        byte[] value = rocksdb.get(key.getBytes());
        if (value != null) {
            return new String(value);
        }
        return null;
    }

    public List<Xref> getXrefs(String id) throws RocksDBException, IOException {
        List<Xref> xrefs = new ArrayList<>();
        String key = id + XREF_SUFFIX;
        List<Xref> ensemblXrefs = rocksDbManager.getXrefs(rocksdb, key);
        if (ensemblXrefs != null) {
            xrefs.addAll(ensemblXrefs);
        }
        key = id + PROTEIN_XREF_SUFFIX;
        List<Xref> proteinXrefs = rocksDbManager.getXrefs(rocksdb, key);
        if (proteinXrefs != null) {
            xrefs.addAll(proteinXrefs);
        }
        return xrefs;
    }

    private void indexDescriptions(Path geneDescriptionFile) throws IOException, RocksDBException {
        logger.info("Loading gene description data...");
        String[] fields;
        if (geneDescriptionFile != null && Files.exists(geneDescriptionFile) && Files.size(geneDescriptionFile) > 0) {
            List<String> lines = Files.readAllLines(geneDescriptionFile, StandardCharsets.ISO_8859_1);
            for (String line : lines) {
                fields = line.split("\t", -1);
                rocksDbManager.update(rocksdb, fields[0] + DESCRIPTION_SUFFIX, fields[1]);
            }
        } else {
            logger.warn("Gene description file " + geneDescriptionFile + " not found");
            logger.warn("Gene description data not loaded");
        }
    }

    public void indexXrefs(Path xrefsFile, Path uniprotIdMappingFile) throws IOException, RocksDBException {

        logger.info("Loading xref data...");
        String[] fields;
        if (xrefsFile != null && Files.exists(xrefsFile) && Files.size(xrefsFile) > 0) {
            List<String> lines = Files.readAllLines(xrefsFile, StandardCharsets.ISO_8859_1);
            String currentTranscriptId = "";
            List<Xref> xrefs= new ArrayList<>();
            for (String line : lines) {
                fields = line.split("\t", -1);
                if (fields.length >= 4) {
                    String transcriptId = fields[0];
                    if (!currentTranscriptId.equals(transcriptId)) {
                        rocksDbManager.update(rocksdb, currentTranscriptId + XREF_SUFFIX, xrefs);
                        xrefs = new ArrayList<>();
                        currentTranscriptId = transcriptId;
                    }
                    String xrefValue = fields[1];
                    String dbName = fields[2];
                    String dbDisplayName = fields[3];
                    xrefs.add(new Xref(xrefValue, dbName, dbDisplayName));
                }
            }
            // parse the last transcript
            rocksDbManager.update(rocksdb, currentTranscriptId + XREF_SUFFIX, xrefs);
        } else {
            logger.warn("Xrefs file " + xrefsFile + " not found");
            logger.warn("Xref data not loaded");
        }

        Map<String, List<Xref>> xrefMap = new HashMap<>();
        logger.info("Loading protein mapping into xref data...");
        if (uniprotIdMappingFile != null && Files.exists(uniprotIdMappingFile) && Files.size(uniprotIdMappingFile) > 0) {
            BufferedReader br = FileUtils.newBufferedReader(uniprotIdMappingFile);
            String line;
            while ((line = br.readLine()) != null) {
                fields = line.split("\t", -1);
                if (fields.length >= 19 && fields[19].startsWith("ENST")) {
                    String[] transcripts = fields[19].split("; ");
                    for (String transcript : transcripts) {
                        if (!xrefMap.containsKey(transcript)) {
                            xrefMap.put(transcript, new ArrayList<Xref>());
                        }
                        xrefMap.get(transcript).add(new Xref(fields[0], "uniprotkb_acc", "UniProtKB ACC"));
                        xrefMap.get(transcript).add(new Xref(fields[1], "uniprotkb_id", "UniProtKB ID"));
                    }
                }
            }
            br.close();

            for (Map.Entry<String, List<Xref>> entry : xrefMap.entrySet()) {
                String transcriptId = entry.getKey();
                List<Xref> xrefs = entry.getValue();
                rocksDbManager.update(rocksdb, transcriptId + PROTEIN_XREF_SUFFIX, xrefs);
            }

        } else {
            logger.warn("Uniprot if mapping file " + uniprotIdMappingFile + " not found");
            logger.warn("Protein mapping into xref data not loaded");
        }
    }

    protected void close() throws IOException {
        rocksDbManager.closeIndex(rocksdb, dbOption, dbLocation);
    }
}
