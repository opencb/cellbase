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

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.formats.sequence.fasta.Fasta;
import org.opencb.biodata.formats.sequence.fasta.io.FastaReader;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GeneBuilderIndexer {

    protected RocksDB rocksdb;
    protected RocksDbManager rocksDbManager;
    protected Logger logger;
    protected String dbLocation;
    protected Options dbOption;

    protected final String MANE_SUFFIX = "_mane";
    protected final String PROTEIN_SEQUENCE_SUFFIX = "_protein_fasta";
    protected final String CDNA_SEQUENCE_SUFFIX = "_cdna_fasta";
    protected final String DRUGS_SUFFIX = "_drug";
    protected final String DISEASE_SUFFIX = "_disease";
    protected final String MIRTARBASE_SUFFIX = "_mirtarbase";

    public GeneBuilderIndexer(Path genePath) {
        this.init(genePath);
    }

    private void init(Path genePath) {
        rocksDbManager = new RocksDbManager();
        dbLocation = genePath.resolve("integration.idx").toString();
        rocksdb = rocksDbManager.getDBConnection(dbLocation);
        dbOption = new Options().setCreateIfMissing(true);

        logger = LoggerFactory.getLogger(this.getClass());
    }

    protected void indexCdnaSequences(Path cDnaFastaFile) throws IOException, FileFormatException, RocksDBException {
        logger.info("Loading RefSeq's cDNA sequences...");
        FileUtils.checkPath(cDnaFastaFile);
        if (Files.size(cDnaFastaFile) > 0) {
            FastaReader fastaReader = new FastaReader(cDnaFastaFile);
            Fasta fasta;
            while ((fasta = fastaReader.read()) != null) {
                rocksDbManager.update(rocksdb, fasta.getId() + CDNA_SEQUENCE_SUFFIX, fasta.getSeq());
            }
            fastaReader.close();
        } else {
            logger.warn("RefSeq's cDNA sequences not loaded");
        }
    }

    public String getCdnaFasta(String id) throws RocksDBException {
        return getIndexEntry(id, CDNA_SEQUENCE_SUFFIX);
    }

    protected void indexProteinSequences(Path proteinFastaFile) throws IOException, FileFormatException, RocksDBException {
        logger.info("Loading ENSEMBL's protein sequences...");
        FileUtils.checkPath(proteinFastaFile);
        if (Files.size(proteinFastaFile) > 0) {
            FastaReader fastaReader = new FastaReader(proteinFastaFile);
            Fasta fasta;
            while ((fasta = fastaReader.read()) != null) {
                rocksDbManager.update(rocksdb, fasta.getId() + PROTEIN_SEQUENCE_SUFFIX, fasta.getSeq());
            }
            fastaReader.close();
        } else {
            logger.warn("ENSEMBL's protein sequences not loaded");
        }
    }

    protected String getProteinFasta(String id) throws RocksDBException {
        return getIndexEntry(id, PROTEIN_SEQUENCE_SUFFIX);
    }

    protected void indexManeMapping(Path maneMappingFile, String referenceId) throws IOException, RocksDBException {
        // #NCBI_GeneID    Ensembl_Gene    HGNC_ID      symbol   name    RefSeq_nuc      RefSeq_prot     Ensembl_nuc     Ensembl_prot
        // MANE_status     GRCh38_chr     chr_start       chr_end chr_strand
        logger.info("Indexing MANE mapping data ...");

        if (maneMappingFile != null && Files.exists(maneMappingFile) && Files.size(maneMappingFile) > 0) {
            int idColumn = referenceId.equalsIgnoreCase("ensembl") ? 7 : 5;
            String line;
            BufferedReader bufferedReader = FileUtils.newBufferedReader(maneMappingFile);
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split("\t", -1);
                rocksDbManager.update(rocksdb, fields[idColumn] + MANE_SUFFIX + "_refseq", fields[5]);
                rocksDbManager.update(rocksdb, fields[idColumn] + MANE_SUFFIX + "_refseq_protein", fields[6]);
                rocksDbManager.update(rocksdb, fields[idColumn] + MANE_SUFFIX + "_ensembl", fields[7]);
                rocksDbManager.update(rocksdb, fields[idColumn] + MANE_SUFFIX + "_ensembl_protein", fields[8]);
                rocksDbManager.update(rocksdb, fields[idColumn] + MANE_SUFFIX + "_flag", fields[9]);
            }
            bufferedReader.close();
        } else {
            logger.warn("MANE mapping file " + maneMappingFile + " not found");
        }
    }

    public String getMane(String id, String field) throws RocksDBException {
        return getIndexEntry(id, MANE_SUFFIX, field);
    }


    private String getIndexEntry(String id, String suffix) throws RocksDBException {
        return getIndexEntry(id, suffix, "");
    }

    private String getIndexEntry(String id, String suffix, String field) throws RocksDBException {
        String key = id + suffix;
        if (StringUtils.isNotEmpty(field)) {
            key += "_" + field;
        }
        byte[] value = rocksdb.get(key.getBytes());
        if (value != null) {
            return new String(value);
        }
        return null;
    }

    protected void close() throws IOException {
        rocksDbManager.closeIndex(rocksdb, dbOption, dbLocation);
    }

}
