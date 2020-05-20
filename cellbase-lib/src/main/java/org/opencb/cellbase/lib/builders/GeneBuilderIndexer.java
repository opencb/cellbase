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

import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.formats.gaf.GafParser;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.formats.sequence.fasta.Fasta;
import org.opencb.biodata.formats.sequence.fasta.io.FastaReader;
import org.opencb.biodata.models.core.Constraint;
import org.opencb.biodata.models.core.FeatureOntologyTermAnnotation;
import org.opencb.biodata.models.core.Xref;
import org.opencb.biodata.models.variant.avro.Expression;
import org.opencb.biodata.models.variant.avro.ExpressionCall;
import org.opencb.biodata.models.variant.avro.GeneDrugInteraction;
import org.opencb.biodata.models.variant.avro.GeneTraitAssociation;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.Options;
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
import java.util.*;
import java.util.zip.GZIPInputStream;

public class GeneBuilderIndexer {

    private RocksDB rocksdb;
    private static final String DESCRIPTION_SUFFIX = "_description";
    private static final String XREF_SUFFIX = "_xref";
    private static final String PROTEIN_XREF_SUFFIX = "_protein_xref";
    private static final String PROTEIN_SEQUENCE_SUFFIX = "_protein_fasta";
    private static final String CDNA_SEQUENCE_SUFFIX = "_cdna_fasta";
    private static final String EXPRESSION_SUFFIX = "_expression";
    private static final String DRUGS_SUFFIX = "_drug";
    private static final String DISEASE_SUFFIX = "_disease";
    private static final String CONSTRAINT_SUFFIX = "_constraint";
    private static final String ONTOLOGY_SUFFIX = "_ontology";
    private static final String MIRBASE_SUFFIX = "_mirbase";
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

    public void index(Path geneDescriptionFile, Path xrefsFile, Path uniprotIdMappingFile, Path proteinFastaFile, Path cDnaFastaFile,
                      String species, Path geneExpressionFile, Path geneDrugFile, Path hpoFile, Path disgenetFile, Path gnomadFile,
                      Path geneOntologyAnnotationFile, Path miRBaseFile, Path miRTarBaseFile)
            throws IOException, RocksDBException, FileFormatException {
        indexDescriptions(geneDescriptionFile);
        indexXrefs(xrefsFile, uniprotIdMappingFile);
        indexProteinSequences(proteinFastaFile);
        indexCdnaSequences(cDnaFastaFile);
        indexExpression(species, geneExpressionFile);
        indexDrugs(geneDrugFile);
        indexDiseases(hpoFile, disgenetFile);
        indexConstraints(gnomadFile);
        indexOntologyAnnotations(geneOntologyAnnotationFile);
        indexMiRBase(miRBaseFile);
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

    public String getProteinFasta(String id) throws RocksDBException {
        String key = id + PROTEIN_SEQUENCE_SUFFIX;
        byte[] value = rocksdb.get(key.getBytes());
        if (value != null) {
            return new String(value);
        }
        return null;
    }

    public String getCdnaFasta(String id) throws RocksDBException {
        String key = id + CDNA_SEQUENCE_SUFFIX;
        byte[] value = rocksdb.get(key.getBytes());
        if (value != null) {
            return new String(value);
        }
        return null;
    }

    public List<Expression> getExpression(String id) throws RocksDBException, IOException {
        String key = id + EXPRESSION_SUFFIX;
        return rocksDbManager.getExpression(rocksdb, key);
    }

    public List<GeneDrugInteraction> getDrugs(String id) throws RocksDBException, IOException {
        String key = id + DRUGS_SUFFIX;
        return rocksDbManager.getDrugs(rocksdb, key);
    }

    public List<GeneTraitAssociation> getDiseases(String id) throws RocksDBException, IOException {
        String key = id + DISEASE_SUFFIX;
        return rocksDbManager.getDiseases(rocksdb, key);
    }

    public List<Constraint> getConstraints(String id) throws RocksDBException, IOException {
        String key = id + CONSTRAINT_SUFFIX;
        return rocksDbManager.getConstraints(rocksdb, key);
    }

    public List<FeatureOntologyTermAnnotation> getOntologyAnnotations(String id) throws RocksDBException, IOException {
        String key = id + ONTOLOGY_SUFFIX;
        return rocksDbManager.getOntologyAnnotations(rocksdb, key);
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
    }

    private void indexCdnaSequences(Path cDnaFastaFile) throws IOException, FileFormatException, RocksDBException {
        logger.info("Loading ENSEMBL's cDNA sequences...");
        if (cDnaFastaFile != null && Files.exists(cDnaFastaFile) && !Files.isDirectory(cDnaFastaFile)
                && Files.size(cDnaFastaFile) > 0) {
            FastaReader fastaReader = new FastaReader(cDnaFastaFile);
            List<Fasta> fastaList = fastaReader.readAll();
            fastaReader.close();
            for (Fasta fasta : fastaList) {
                rocksDbManager.update(rocksdb, fasta.getId().split("\\.")[0] + CDNA_SEQUENCE_SUFFIX, fasta.getSeq());
            }
        } else {
            logger.warn("cDNA fasta file " + cDnaFastaFile + " not found");
            logger.warn("ENSEMBL's cDNA sequences not loaded");
        }
    }

    private void indexProteinSequences(Path proteinFastaFile) throws IOException, FileFormatException, RocksDBException {
        logger.info("Loading ENSEMBL's protein sequences...");
        if (proteinFastaFile != null && Files.exists(proteinFastaFile) && !Files.isDirectory(proteinFastaFile)
                && Files.size(proteinFastaFile) > 0) {
            FastaReader fastaReader = new FastaReader(proteinFastaFile);
            List<Fasta> fastaList = fastaReader.readAll();
            fastaReader.close();
            for (Fasta fasta : fastaList) {
                rocksDbManager.update(rocksdb,
                        fasta.getDescription().split("transcript:")[1].split("\\s")[0].split("\\.")[0]
                                + PROTEIN_SEQUENCE_SUFFIX, fasta.getSeq());
            }
        } else {
            logger.warn("Protein fasta file " + proteinFastaFile + " not found");
            logger.warn("ENSEMBL's protein sequences not loaded");
        }
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

    private void indexDrugs(Path geneDrugFile) throws IOException, RocksDBException {
        if (geneDrugFile != null && Files.exists(geneDrugFile) && Files.size(geneDrugFile) > 0) {
            logger.info("Loading gene-drug interaction data from '{}'", geneDrugFile);
            BufferedReader br = FileUtils.newBufferedReader(geneDrugFile);

            // Skip header
            br.readLine();

            int lineCounter = 1;
            String line;
            String currentGene = "";
            List<GeneDrugInteraction> drugs = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                String geneName = parts[0];
                if (currentGene.equals("")) {
                    currentGene = geneName;
                } else if (!currentGene.equals(geneName)) {
                    rocksDbManager.update(rocksdb, currentGene + DRUGS_SUFFIX, drugs);
                    drugs = new ArrayList<>();
                    currentGene = geneName;
                }

                String source = null;
                if (parts.length >= 4) {
                    source = parts[3];
                }

                String interactionType = null;
                if (parts.length >= 5) {
                    interactionType = parts[4];
                }

                String drugName = null;
                if (parts.length >= 8) {
                    // if drug name column is empty, use drug claim name instead
                    drugName = StringUtils.isEmpty(parts[7]) ? parts[6] : parts[7];
                }
                if (StringUtils.isEmpty(drugName)) {
                    // no drug name
                    continue;
                }

                String chemblId = null;
                if (parts.length >= 9) {
                    chemblId = parts[8];
                }

                List<String> publications = new ArrayList<>();
                if (parts.length >= 10 && parts[9] != null) {
                    publications = Arrays.asList(parts[9].split(","));
                }

                GeneDrugInteraction drug = new GeneDrugInteraction(
                        geneName, drugName, source, null, null, interactionType, chemblId, publications);
                drugs.add(drug);
                lineCounter++;
            }
            br.close();
            // update last gene
            rocksDbManager.update(rocksdb, currentGene + DRUGS_SUFFIX, drugs);
        } else {
            logger.warn("Gene drug file " + geneDrugFile + " not found");
            logger.warn("Ignoring " + geneDrugFile);
        }
    }

    private void indexDiseases(Path hpoFilePath, Path disgenetFilePath) throws IOException, RocksDBException {
        Map<String, List<GeneTraitAssociation>> geneDiseaseAssociationMap = new HashMap<>(50000);

        String line;
        if (hpoFilePath != null && hpoFilePath.toFile().exists() && Files.size(hpoFilePath) > 0) {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(hpoFilePath);
            // skip first header line
            bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split("\t");
                String omimId = fields[6];
                String geneSymbol = fields[3];
                String hpoId = fields[0];
                String diseaseName = fields[1];
                GeneTraitAssociation disease =
                        new GeneTraitAssociation(omimId, diseaseName, hpoId, 0f, 0, new ArrayList<>(), new ArrayList<>(), "hpo");
                addValueToMapElement(geneDiseaseAssociationMap, geneSymbol, disease);
            }
            bufferedReader.close();
        }

        if (disgenetFilePath != null && disgenetFilePath.toFile().exists() && Files.size(disgenetFilePath) > 0) {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(disgenetFilePath);
            // skip first header line
            bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split("\t");
                String diseaseId = fields[4];
                String diseaseName = fields[5];
                String score = fields[9];
                String numberOfPubmeds = fields[13].trim();
                String numberOfSNPs = fields[14];
                String source = fields[15];
                GeneTraitAssociation disease = new GeneTraitAssociation(diseaseId, diseaseName, "", Float.parseFloat(score),
                        Integer.parseInt(numberOfPubmeds), Arrays.asList(numberOfSNPs), Arrays.asList(source), "disgenet");
                addValueToMapElement(geneDiseaseAssociationMap, fields[1], disease);
            }
            bufferedReader.close();
        }

        for (Map.Entry<String, List<GeneTraitAssociation>> entry : geneDiseaseAssociationMap.entrySet()) {
            rocksDbManager.update(rocksdb, entry.getKey() + DISEASE_SUFFIX, entry.getValue());
        }
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
            BufferedReader br = FileUtils.newBufferedReader(goaFile);
            GafParser parser = new GafParser();
            annotations = parser.parseGaf(br);
        }

        for (Map.Entry<String, List<FeatureOntologyTermAnnotation>> entry : annotations.entrySet()) {
            rocksDbManager.update(rocksdb, entry.getKey() + ONTOLOGY_SUFFIX, entry.getValue());
        }
    }

    private void indexMiRBase(Path miRBaseFile) throws IOException, RocksDBException {
        if (miRBaseFile != null && Files.exists(miRBaseFile) && Files.size(miRBaseFile) > 0) {

        }

    }

    private static <T> void addValueToMapElement(Map<String, List<T>> map, String key, T value) {
        if (map.containsKey(key)) {
            map.get(key).add(value);
        } else {
            List<T> expressionValueList = new ArrayList<>();
            expressionValueList.add(value);
            map.put(key, expressionValueList);
        }
    }

    protected void close() throws IOException {
        rocksDbManager.closeIndex(rocksdb, dbOption, dbLocation);
    }
}
