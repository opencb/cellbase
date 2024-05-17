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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.feature.mirbase.MirBaseParser;
import org.opencb.biodata.formats.feature.mirbase.MirBaseParserCallback;
import org.opencb.biodata.formats.gaf.GafParser;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.models.core.FeatureOntologyTermAnnotation;
import org.opencb.biodata.models.core.MiRnaGene;
import org.opencb.biodata.models.core.MirnaTarget;
import org.opencb.biodata.models.core.Xref;
import org.opencb.biodata.models.variant.avro.Constraint;
import org.opencb.biodata.models.variant.avro.Expression;
import org.opencb.biodata.models.variant.avro.ExpressionCall;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.opencb.cellbase.lib.EtlCommons.ENSEMBL_DATA;
import static org.opencb.cellbase.lib.builders.CellBaseBuilder.PARSING_DONE_LOG_MESSAGE;
import static org.opencb.cellbase.lib.builders.CellBaseBuilder.PARSING_LOG_MESSAGE;

public class EnsemblGeneBuilderIndexer extends GeneBuilderIndexer {

    private static final String DESCRIPTION_SUFFIX = "_description";
    private static final String XREF_SUFFIX = "_xref";
    private static final String PROTEIN_XREF_SUFFIX = "_protein_xref";
    private static final String EXPRESSION_SUFFIX = "_expression";
    private static final String CONSTRAINT_SUFFIX = "_constraint";
    private static final String ONTOLOGY_SUFFIX = "_ontology";
    private static final String OBO_SUFFIX = "_obo";
    private static final String MIRBASE_SUFFIX = "_mirbase";
    private static final String CANONICAL_SUFFIX = "_canonical";

    public EnsemblGeneBuilderIndexer(Path geneDirectoryPath) {
        super(geneDirectoryPath);
    }

    public void index(Path geneDescriptionFile, Path xrefsFile, Path hgncFile, Path maneFile, Path lrgFile, Path uniprotIdMappingFile,
                      Path proteinFastaFile, Path cDnaFastaFile, String species, Path geneExpressionFile, Path geneDrugFile, Path hpoFile,
                      Path disgenetFile, Path gnomadFile, Path geneOntologyAnnotationFile, Path miRBaseFile, Path miRTarBaseFile,
                      Path cancerGeneGensusFile, Path cancerHostpotFile, Path canonicalFile, Path tso500File, Path eglhHaemOncFile)
            throws IOException, RocksDBException, FileFormatException, CellBaseException {
        indexDescriptions(geneDescriptionFile);
        indexXrefs(xrefsFile, uniprotIdMappingFile);
        indexHgncIdMapping(hgncFile);
        indexManeMapping(maneFile, ENSEMBL_DATA);
        indexLrgMapping(lrgFile, ENSEMBL_DATA);
        indexProteinSequences(proteinFastaFile);
        indexCdnaSequences(cDnaFastaFile);
        indexExpression(species, geneExpressionFile);
        indexDrugs(geneDrugFile);
        indexDiseases(hpoFile, disgenetFile);
        indexConstraints(gnomadFile);
        indexOntologyAnnotations(geneOntologyAnnotationFile);
        indexMiRBase(species, miRBaseFile);
        indexMiRTarBase(miRTarBaseFile);
        indexCancerGeneCensus(cancerGeneGensusFile);
        indexCancerHotspot(cancerHostpotFile);
        indexCanonical(canonicalFile);
        indexTSO500(tso500File);
        indexEGLHHaemOnc(eglhHaemOncFile);
    }

    private void indexDescriptions(Path geneDescriptionFile) throws IOException, RocksDBException {
        logger.info(PARSING_LOG_MESSAGE, geneDescriptionFile);
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
        logger.info(PARSING_DONE_LOG_MESSAGE);
    }

    public String getDescription(String id) throws RocksDBException {
        String key = id + DESCRIPTION_SUFFIX;
        byte[] value = rocksdb.get(key.getBytes());
        if (value != null) {
            return new String(value);
        }
        return null;
    }

    private void indexXrefs(Path xrefsFile, Path uniprotIdMappingFile) throws IOException, RocksDBException {
        logger.info(PARSING_LOG_MESSAGE, xrefsFile);
        String[] fields;
        if (xrefsFile != null && Files.exists(xrefsFile) && Files.size(xrefsFile) > 0) {
            List<String> lines = Files.readAllLines(xrefsFile, StandardCharsets.ISO_8859_1);
            String currentTranscriptId = "";
            List<Xref> xrefs= new ArrayList<>();
            for (String line : lines) {
                fields = line.split("\t", -1);
                if (fields.length >= 4) {
                    String transcriptId = fields[0];
                    if (currentTranscriptId.equals("")) {
                        // set the first transcript
                        currentTranscriptId = transcriptId;
                    } else if (!currentTranscriptId.equals(transcriptId)) {
                        // update DB with previous transcript when we get to another transcript
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
        logger.info(PARSING_DONE_LOG_MESSAGE);
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

    private void indexExpression(String species, Path geneExpressionFile) throws IOException, RocksDBException {
        Map<String, List<Expression>> geneExpressionMap = new HashMap<>();
        if (geneExpressionFile != null && Files.exists(geneExpressionFile) && Files.size(geneExpressionFile) > 0
                && species != null) {
            logger.info("Loading gene expression data from '{}'", geneExpressionFile);
            BufferedReader br = FileUtils.newBufferedReader(geneExpressionFile);

            // Skip header. Column name line does not start with # so the last line read by this while will be this one
            int lineCounter = 0;
            String line;
            while (((line = br.readLine()) != null)) {  //  && (line.startsWith("#"))
                if (line.startsWith("#")) {
                    lineCounter++;
                } else {
                    break;
                }
            }

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (species.equals(parts[2])) {
                    if (parts[7].equals("UP")) {
                        addValueToMapElement(geneExpressionMap, parts[1], new Expression(parts[1], null, parts[3],
                                parts[4], parts[5], parts[6], ExpressionCall.UP, Float.valueOf(parts[8])));
                    } else if (parts[7].equals("DOWN")) {
                        addValueToMapElement(geneExpressionMap, parts[1], new Expression(parts[1], null, parts[3],
                                parts[4], parts[5], parts[6], ExpressionCall.DOWN, Float.valueOf(parts[8])));
                    } else {
                        logger.warn("Expression tags found different from UP/DOWN at line {}. Entry omitted. ", lineCounter);
                    }
                }
                lineCounter++;
            }

            br.close();

            for (Map.Entry<String, List<Expression>> entry : geneExpressionMap.entrySet()) {
                String key = entry.getKey() + EXPRESSION_SUFFIX;
                rocksDbManager.update(rocksdb, key, entry.getValue());
            }

        } else {
            logger.warn("Parameters are not correct");
        }
    }

    public List<Expression> getExpression(String id) throws RocksDBException, IOException {
        String key = id + EXPRESSION_SUFFIX;
        return rocksDbManager.getExpression(rocksdb, key);
    }

    private void indexConstraints(Path gnomadFile) throws IOException, RocksDBException {
        if (gnomadFile != null && Files.exists(gnomadFile) && Files.size(gnomadFile) > 0) {
            logger.info("Loading OE scores from '{}'", gnomadFile);
            InputStream inputStream = Files.newInputStream(gnomadFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(inputStream)));
            // Skip header.
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                String transcriptIdentifier = parts[1];
                String canonical = parts[2];
                String oeMis = parts[5];
                String oeSyn = parts[14];
                String oeLof = parts[24];
                String exacPLI = parts[70];
                String exacLof = parts[73];
                String geneIdentifier = parts[64];

                List<Constraint> constraints = new ArrayList<>();
                addConstraint(constraints, "oe_mis", oeMis);
                addConstraint(constraints, "oe_syn", oeSyn);
                addConstraint(constraints, "oe_lof", oeLof);
                addConstraint(constraints, "exac_pLI", exacPLI);
                addConstraint(constraints, "exac_oe_lof", exacLof);
                rocksDbManager.update(rocksdb, transcriptIdentifier + CONSTRAINT_SUFFIX, constraints);

                if ("TRUE".equalsIgnoreCase(canonical)) {
                    rocksDbManager.update(rocksdb, geneIdentifier + CONSTRAINT_SUFFIX, constraints);
                }
            }
            br.close();
        } else {
            logger.error("gnomad constraints file not found");
        }
    }

    public List<Constraint> getConstraints(String id) throws RocksDBException, IOException {
        String key = id + CONSTRAINT_SUFFIX;
        return rocksDbManager.getConstraints(rocksdb, key);
    }

    private void addConstraint(List<Constraint> constraints, String name, String value) {
        Constraint constraint = new Constraint();
        constraint.setMethod("pLoF");
        constraint.setSource("gnomAD");
        constraint.setName(name);
        try {
            constraint.setValue(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            // invalid number (e.g. NA), discard.
            return;
        }
        constraints.add(constraint);
    }

    private void indexOntologyAnnotations(Path goaFile) throws IOException, RocksDBException {
        Map<String, List<FeatureOntologyTermAnnotation>> annotations = new HashMap<>();
        if (goaFile != null && Files.exists(goaFile) && Files.size(goaFile) > 0) {
            logger.info("Loading GO annotation from '{}'", goaFile);
//            BufferedReader br = FileUtils.newBufferedReader(goaFile);
            GafParser parser = new GafParser();
            Path oboFile = goaFile.getParent().getParent().resolve("ontology/go-basic.obo");
            annotations = parser.parseGaf(goaFile, oboFile);
        }

        for (Map.Entry<String, List<FeatureOntologyTermAnnotation>> entry : annotations.entrySet()) {
            rocksDbManager.update(rocksdb, entry.getKey() + ONTOLOGY_SUFFIX, entry.getValue());
        }
    }

    public List<FeatureOntologyTermAnnotation> getOntologyAnnotations(String id) throws RocksDBException, IOException {
        String key = id + ONTOLOGY_SUFFIX;
        return rocksDbManager.getOntologyAnnotations(rocksdb, key);
    }

    private void indexMiRBase(String species, Path miRBaseFile) throws IOException {
        logger.info(PARSING_LOG_MESSAGE, miRBaseFile);

        MirBaseCallback callback = new MirBaseCallback(rocksdb, rocksDbManager);
        MirBaseParser.parse(miRBaseFile, species, callback);

        logger.info(PARSING_DONE_LOG_MESSAGE, miRBaseFile);
    }

    public MiRnaGene getMirnaGene(String transcriptId) throws RocksDBException, IOException {
        String xrefKey = transcriptId + XREF_SUFFIX;
        List<Xref> xrefs = rocksDbManager.getXrefs(rocksdb, xrefKey);
        if (xrefs == null || xrefs.isEmpty()) {
            return null;
        }
        for (Xref xref : xrefs) {
            if ("mirbase".equals(xref.getDbName())) {
                String mirnaKey = xref.getId() + MIRBASE_SUFFIX;
                return rocksDbManager.getMirnaGene(rocksdb, mirnaKey);
            }
        }
        return null;
    }

    public List<MirnaTarget> getMirnaTargets(String geneName) throws RocksDBException, IOException {
        String key = geneName + MIRTARBASE_SUFFIX;
        return rocksDbManager.getMirnaTargets(rocksdb, key);
    }

    protected void indexCanonical(Path canonocalFile) throws IOException, RocksDBException {
        // Gene  Transcript  Canonical
        // ENSG00000210049.1  ENST00000387314.1  1
        logger.info("Indexing Ensembl Canonical mapping data ...");

        if (canonocalFile != null && Files.exists(canonocalFile) && Files.size(canonocalFile) > 0) {
            try (BufferedReader bufferedReader = FileUtils.newBufferedReader(canonocalFile)) {
                String line = bufferedReader.readLine();
                while (StringUtils.isNotEmpty(line)) {
                    if (!line.startsWith("#")) {
                        String[] fields = line.split("\t", -1);
                        String transcriptId = fields[1];
                        if (StringUtils.isNotEmpty(transcriptId) && fields.length > 2 && fields[2].equals("1")) {
                            rocksDbManager.update(rocksdb, transcriptId + CANONICAL_SUFFIX, "canonical");
                        }
                    }
                    line = bufferedReader.readLine();
                }
            }
        } else {
            logger.warn("Ensembl Canonical mapping file " + canonocalFile + " not found");
        }
    }

    public String getCanonical(String transcriptId) throws RocksDBException, IOException {
        String key = transcriptId + CANONICAL_SUFFIX;
        byte[] bytes = rocksdb.get(key.getBytes());
        if (bytes == null) {
            return null;
        }
        return new String(bytes);
    }

    // Implementation of the MirBaseParserCallback function
    public class MirBaseCallback implements MirBaseParserCallback {

        private RocksDB rocksDB;
        private RocksDbManager rocksDbManager;
        private Logger logger;

        public MirBaseCallback(RocksDB rocksDB, RocksDbManager rocksDbManager) {
            this.rocksDB = rocksDB;
            this.rocksDbManager = rocksDbManager;
            this.logger = LoggerFactory.getLogger(this.getClass());
        }

        @Override
        public boolean processMiRnaGene(MiRnaGene miRnaGene) {
            try {
                rocksDbManager.update(rocksdb, miRnaGene.getId() + MIRBASE_SUFFIX, miRnaGene);
            } catch (JsonProcessingException | RocksDBException e) {
                logger.warn("Something wrong happened when processing miRNA gene {}: {}", miRnaGene.getId(),
                        StringUtils.join(e.getStackTrace(), "\t"));
                return false;
            }
            return true;
        }
    }
}
