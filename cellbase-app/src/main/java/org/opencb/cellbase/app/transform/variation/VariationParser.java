/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.app.transform.variation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Stopwatch;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.annotation.exceptions.SOTermNotAvailableException;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.biodata.models.variation.TranscriptVariation;

import org.opencb.cellbase.app.transform.CellBaseParser;
import org.opencb.cellbase.app.transform.utils.VariationUtils;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils.getSequenceOntologyTerms;

public class VariationParser extends CellBaseParser {

    protected static final String PREPROCESSED_VARIATION_FILENAME = "variation.sorted.txt";

    private Path variationDirectoryPath;

    private static final int VARIATION_FEATURE_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE = 1;

    private Pattern cnvPattern;
    private static final String SEQUENCE_GROUP = "seq";
    private static final String COUNT_GROUP = "count";

    private CellBaseFileSerializer fileSerializer;
    private Map<String, String> outputFileNames;

    private VariationFile variationFile;
    private VariationTranscriptFile variationTranscriptFile;
    private VariationFeatureFile variationFeatureFile;
    private VariationSynonymFile variationSynonymFile;
    private ObjectWriter jsonObjectWriter;

    public VariationParser(Path variationDirectoryPath, CellBaseFileSerializer serializer) {
        super(serializer);
        fileSerializer = serializer;
        this.variationDirectoryPath = variationDirectoryPath;
        //cnvPattern = Pattern.compile("((?<" + SEQUENCE_GROUP + ">\\(\\w+\\))" + "(?<" + COUNT_GROUP + ">\\d*))+");
        // Avoid patterns alleles like "(AG)15(TG)16" to be procesed as CNVs - just one occurrence like "(AG)15"
        // will be accepted
        cnvPattern = Pattern.compile("((?<" + SEQUENCE_GROUP + ">\\(\\w+\\))" + "(?<" + COUNT_GROUP + ">\\d*))");
        outputFileNames = new HashMap<>();
        // create files
        variationFile = new VariationFile(variationDirectoryPath);
        variationFeatureFile = new VariationFeatureFile(variationDirectoryPath);
        variationTranscriptFile = new VariationTranscriptFile(variationDirectoryPath);
        variationSynonymFile = new VariationSynonymFile(variationDirectoryPath);
        initializeJsonWriter();
    }

    @Override
    public void parse() throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        if (!Files.exists(variationDirectoryPath) || !Files.isDirectory(variationDirectoryPath)
                || !Files.isReadable(variationDirectoryPath)) {
            throw new IOException("Variation directory whether does not exist, is not a directory or cannot be read");
        }
        if (!variationFile.existsZippedOrUnzippedFile() || variationFile.isEmpty()) {
            throw new IOException("variation.txt.gz whether does not exist, is not a directory or cannot be read");
        }

        // add idVariation to transcript_variation file
        preprocessInputFiles();

        // Open variation file, this file never gets uncompressed. It's read from gzip file
        BufferedReader bufferedReaderVariation = variationFile.getBufferedReader();

        // create buffered readers for all other input files
        createVariationFilesReaders();

        Map<String, String> seqRegionMap = VariationUtils.parseSeqRegionToMap(variationDirectoryPath);
        Map<String, String> sourceMap = VariationUtils.parseSourceToMap(variationDirectoryPath);

        Stopwatch globalStartwatch = Stopwatch.createStarted();
        Stopwatch batchWatch = Stopwatch.createStarted();
        logger.info("Parsing variation file {} ...", variationDirectoryPath.resolve(PREPROCESSED_VARIATION_FILENAME));
        long countprocess = 0, incorrectEndVariants = 0, incorrectAllelesVariants = 0;

        String line;
        while ((line = bufferedReaderVariation.readLine()) != null) {
            String[] variationFields = line.split("\t");

            int variationId = Integer.parseInt(variationFields[0]);

            List<String[]> resultVariationFeature = variationFeatureFile.getVariationRelatedLines(variationId);
            if (resultVariationFeature != null && resultVariationFeature.size() > 0) {
                String[] variationFeatureFields = resultVariationFeature.get(0);

                List<TranscriptVariation> transcriptVariation = getTranscriptVariations(variationId, variationFeatureFields[0]);
                List<Xref> xrefs = getXrefs(sourceMap, variationId);

                try {
                    // Preparing the variation alleles
                    String[] allelesArray = getAllelesArray(variationFeatureFields);
                    if (allelesArray == null) {
                        logger.debug("Incorrect allele string: {}", variationFeatureFields[6]);
                        incorrectAllelesVariants++;
                    } else {
                        String chromosome = seqRegionMap.get(variationFeatureFields[1]);

                        if (!chromosome.contains("PATCH") && !chromosome.contains("HSCHR") && !chromosome.contains("contig")) {
                            int start = Integer.valueOf(variationFeatureFields[2]);
                            int end = Integer.valueOf(variationFeatureFields[3]);
                            String id = (variationFields[2] != null && !variationFields[2].equals("\\N")) ? variationFields[2] : "";
                            String reference = (allelesArray[0] != null && !allelesArray[0].equals("\\N")) ? allelesArray[0] : "";
                            List<String> alternates = getAlternates(allelesArray);

                            List<String> ids = new LinkedList<>();
                            ids.add(id);

                            List<String> hgvs = getHgvs(transcriptVariation);
                            Map<String, AdditionalAttribute> additionalAttributes
                                    = getAdditionalAttributes(variationFields, variationFeatureFields);

                            List<ConsequenceType> conseqTypes = getConsequenceTypes(transcriptVariation);
                            String displayConsequenceTypes = getDisplayConsequenceType(variationFeatureFields);
                            String strand = variationFeatureFields[4];
                            String ancestralAllele = (variationFields[4] != null && !variationFields[4].equals("\\N"))
                                    ? variationFields[4] : "";
                            String minorAllele = (variationFeatureFields[16] != null && !variationFeatureFields[16].equals("\\N"))
                                    ? variationFeatureFields[16] : "";
                            Float minorAlleleFreq = (variationFeatureFields[17] != null && !variationFeatureFields[17].equals("\\N"))
                                    ? Float.parseFloat(variationFeatureFields[17]) : null;

                            // create a variation object for each alternative
                            for (String alternate : alternates) {
                                VariantType type = getVariantType(reference, alternate);
                                if (type == null) {
                                    logger.warn("Unrecognized variant type (won't be parsed): {}:{}-{} {}/{}", chromosome, start, end,
                                            reference, alternate);
                                } else if (incorrectStartAndEnd(start, end, reference)) {
                                    logger.debug("Incorrect variant start-end pair:  {}:{}-{} {}/{}", chromosome, start, end, reference,
                                            alternate);
                                    incorrectEndVariants++;
                                } else {
                                    // build and serialize variant
                                    Variant variation = buildVariant(chromosome, start, end, reference, alternate, type, ids, hgvs,
                                            additionalAttributes, displayConsequenceTypes, conseqTypes, id, xrefs, strand, ancestralAllele,
                                            minorAllele, minorAlleleFreq);
                                    fileSerializer.serialize(variation, getOutputFileName(chromosome));
                                }
                                countprocess++;
                            }
                        }
                    }

                    if (countprocess % 100000 == 0 && countprocess != 0) {
                        logger.info("Processed variations: {}", countprocess);
                        logger.debug("Elapsed time processing batch: {}", batchWatch);
                        batchWatch.reset();
                        batchWatch.start();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error parsing variation: {}", e.getMessage());
                    logger.error("Last line processed: {}", line);
                    break;
                }
            }
//            // TODO: just for testing, remove
//            if (countprocess % 1000000 == 0) {
//                break;
//            }
        }

        serializer.close();
        logger.info("Variation parsing finished");
        logger.info("Variants processed: {}", countprocess);
        logger.info("Variants not parsed due to incorrect start-end: {}", incorrectEndVariants);
        logger.info("Variants not parsed due to incorrect alleles: {}", incorrectAllelesVariants);
        logger.debug("Elapsed time parsing: {}", globalStartwatch);

        gzipVariationFiles(variationDirectoryPath);

        try {
            bufferedReaderVariation.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void preprocessInputFiles() throws IOException, InterruptedException {
        gunzipVariationInputFiles();
        variationFile.sort();
        variationFeatureFile.sort();
        variationSynonymFile.sort();
        variationTranscriptFile.preprocess(variationFeatureFile);
    }

    private void createVariationFilesReaders() throws IOException {
        variationFeatureFile.createBufferedReader();
        variationSynonymFile.createBufferedReader();
        variationTranscriptFile.createBufferedReader();
    }

    private Variant buildVariant(String chromosome, int start, int end, String reference, String alternate, VariantType type,
                                 List<String> ids, List<String> hgvs, Map<String, AdditionalAttribute> additionalAttributes,
                                 String displayConsequenceType, List<ConsequenceType> conseqTypes, String id, List<Xref> xrefs,
                                 String strand, String ancestralAllele, String minorAllele, Float minorAlleleFreq) {

        StructuralVariation sv = null;
        switch (type) {
            case INDEL:
            case INSERTION:
                if (end < start) {
                    end = start;
                }
                break;
            case CNV:
                reference = String.valueOf(reference.toCharArray()[1]);
                String copyNumberStr = alternate.split("\\)")[1];
                alternate = "<CN" + copyNumberStr + ">";
                Integer copyNumber = Integer.valueOf(copyNumberStr);
                sv = new StructuralVariation(start, start, end, end, copyNumber, null, null,
                        Variant.getCNVSubtype(copyNumber));
                break;
            default:
                break;
        }

//        end = fixEndForInsertions(start, end, type);
        Variant variant = new Variant(chromosome, start, end, reference, alternate);
        variant.setIds(ids);
        variant.setType(type);
        variant.setSv(sv);
        VariantAnnotation ensemblVariantAnnotation = new VariantAnnotation(null, null, null,
                null, null, null, id, xrefs, hgvs,
                displayConsequenceType, conseqTypes, null, null, null,
                null, null, null, null,
                null, null, null, null, null,
                null, null);
        try {
            String ensemblAnnotationJson = getEnsemblAnnotationJson(ensemblVariantAnnotation);
            additionalAttributes.get("ensemblAnnotation").getAttribute().put("annotation", ensemblAnnotationJson);
        } catch (JsonProcessingException e) {
            logger.warn("Variant {} annotation cannot be serialized to Json: {}", id, e.getMessage());
        }
        VariantAnnotation variantAnnotation = new VariantAnnotation(null, null, null,
                null, null, ancestralAllele, id, xrefs, hgvs,
                displayConsequenceType, conseqTypes, null, minorAllele, minorAlleleFreq,
                null, null, null, null,
                null, null, null, null, null, null, additionalAttributes);
        variant.setAnnotation(variantAnnotation);
        variant.setStrand(strand);

        return variant;
    }

//    private int fixEndForInsertions(int start, int end, VariantType type) {
//        if (type == VariantType.INDEL || type == VariantType.INSERTION) {
//            if (end < start) {
//                end = start;
//            }
//        }
//        return end;
//    }

    private String getDisplayConsequenceType(String[] variationFeatureFields) {
        List<String> consequenceTypes = Arrays.asList(variationFeatureFields[12].split(","));
        String displayConsequenceType = null;
        if (consequenceTypes.size() == 1) {
            displayConsequenceType = consequenceTypes.get(0);
        } else {
            for (String cons : consequenceTypes) {
                if (!cons.equals("intergenic_variant")) {
                    displayConsequenceType = cons;
                    break;
                }
            }
        }
        return displayConsequenceType;
    }

    private String getEnsemblAnnotationJson(VariantAnnotation ensemblVariantAnnotation) throws JsonProcessingException {
        return jsonObjectWriter.writeValueAsString(ensemblVariantAnnotation);
    }

    private void initializeJsonWriter() {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = jsonObjectMapper.writer();
    }

    private VariantType getVariantType(String reference, String alternate) {
        if (reference.contains("(") || alternate.contains("(")) {
            return checkSnv(reference, alternate);
        } else if (reference.length() != alternate.length()) {
            return VariantType.INDEL;
        } else if (reference.equals("-") || alternate.equals("-")) {
            return VariantType.INDEL;
        } else {
            return VariantType.SNV;
        }
//
//        if (reference.length() != alternate.length()) {
//            return VariantType.INDEL;
//        } else {
//            if (reference.equals("-") || alternate.equals("-")) {
//                return VariantType.INDEL;
//            } else if (reference.contains("(") || alternate.contains("(")) {
//                return checkSnv(reference, alternate);
//            } else {
//                return VariantType.SNV;
//            }
//        }
    }

    private VariantType checkSnv(String reference, String alternate) {
        Matcher referenceMatcher = cnvPattern.matcher(reference);
        Matcher alternateMatcher = cnvPattern.matcher(alternate);
        if (referenceMatcher.matches() && alternateMatcher.matches()) {
            if (referenceMatcher.group(SEQUENCE_GROUP).equals(alternateMatcher.group(SEQUENCE_GROUP))
                    && !referenceMatcher.group(COUNT_GROUP).equals(alternateMatcher.group(COUNT_GROUP))) {
                return VariantType.CNV;
            }
        } else if (referenceMatcher.matches()) {
            if (referenceMatcher.group(SEQUENCE_GROUP).equals(alternate)) {
                return VariantType.CNV;
            }
        } else if (alternateMatcher.matches()) {
            if (alternateMatcher.group(SEQUENCE_GROUP).equals(reference)) {
                return VariantType.CNV;
            }
        }
        return null;
    }

    private boolean incorrectStartAndEnd(int start, int end, String reference) {
        return end < start && !reference.equals("") && !reference.equals("-");
    }

    private List<String> getAlternates(String[] allelesArray) {
        List<String> alternates = new ArrayList<>(allelesArray.length - 1);
        for (int i = 1; i < allelesArray.length; i++) {
            alternates.add((allelesArray[i] != null && !allelesArray[i].equals("\\N")) ? allelesArray[i] : "");
        }
        return alternates;
    }

    private List<String> getHgvs(List<TranscriptVariation> transcriptVariations) {
        Set<String> hgvs = new HashSet<>();
        for (TranscriptVariation transcriptVariation : transcriptVariations) {
            if (transcriptVariation.getHgvsGenomic() != null) {
                hgvs.add(transcriptVariation.getHgvsGenomic());
            }
            if (transcriptVariation.getHgvsTranscript() != null) {
                hgvs.add(transcriptVariation.getHgvsTranscript());
            }
            if (transcriptVariation.getHgvsProtein() != null) {
                hgvs.add(transcriptVariation.getHgvsProtein());
            }
        }
        return new ArrayList<>(hgvs);
    }

    private List<ConsequenceType> getConsequenceTypes(List<TranscriptVariation> transcriptVariations) {
        List<ConsequenceType> consequenceTypes = null;
        for (TranscriptVariation transcriptVariation : transcriptVariations) {
            ProteinVariantAnnotation proteinVariantAnnotation = getProteinVariantAnnotation(transcriptVariation);

            List<SequenceOntologyTerm> soTerms = null;
            try {
                soTerms = getSequenceOntologyTerms(transcriptVariation.getConsequenceTypes());
            } catch (SOTermNotAvailableException e) {
                logger.warn(e.getMessage());
            }

            if (consequenceTypes == null) {
                consequenceTypes = new ArrayList<>();
            }
            consequenceTypes.add(new ConsequenceType(null, null, transcriptVariation.getTranscriptId(), null, null,
                    null, null, transcriptVariation.getCdnaStart() != 0 ? transcriptVariation.getCdnaStart() : null,
                    transcriptVariation.getCdsStart() != 0 ? transcriptVariation.getCdsStart() : null,
                    transcriptVariation.getCodonAlleleString(), proteinVariantAnnotation, soTerms));

        }
        return consequenceTypes;
    }

    private ProteinVariantAnnotation getProteinVariantAnnotation(TranscriptVariation transcriptVariation) {
        List<Score> substitionScores = getSubstitutionScores(transcriptVariation);

        // get peptide reference and alternate
        String peptideReference = null,
                peptideAlternate = null;
        if (transcriptVariation.getPeptideAlleleString() != null) {
            String[] peptideAlleles = transcriptVariation.getPeptideAlleleString().split("/");
            peptideReference = peptideAlleles[0];
            if (peptideAlleles.length == 1) {
                peptideAlternate = peptideAlleles[0];
            } else {
                peptideAlternate = peptideAlleles[1];
            }
        }

        ProteinVariantAnnotation proteinVariantAnnotation = null;
        if (peptideAlternate != null || peptideReference != null || substitionScores != null) {
            proteinVariantAnnotation = new ProteinVariantAnnotation(null, null, 0,
                    peptideReference, peptideAlternate, null, null, substitionScores, null, null);
        }
        return proteinVariantAnnotation;
    }

    private List<Score> getSubstitutionScores(TranscriptVariation transcriptVariation) {
        List<Score> substitionScores = null;
        if (transcriptVariation.getPolyphenScore() != null) {
            substitionScores = new ArrayList<>();
            substitionScores.add(new Score((double) transcriptVariation.getPolyphenScore(), "Polyphen", null));
        }
        if (transcriptVariation.getSiftScore() != null) {
            if (substitionScores == null) {
                substitionScores = new ArrayList<>();
            }
            substitionScores.add(new Score((double) transcriptVariation.getSiftScore(), "Sift", null));
        }
        return substitionScores;
    }

    private String[] getAllelesArray(String[] variationFeatureFields) {
        String[] allelesArray = null;
        if (variationFeatureFields != null && variationFeatureFields[6] != null) {
            allelesArray = variationFeatureFields[6].split("/");
            if (allelesArray.length == 1 || allelesArray.length == 0) {
                allelesArray = null;
            }
        }
        return allelesArray;
    }

    private Map<String, AdditionalAttribute> getAdditionalAttributes(String[] variationFields, String[] variationFeatureFields) {
        Map<String, AdditionalAttribute> additionalAttributes = new HashMap<>();
        AdditionalAttribute additionalAttribute = new AdditionalAttribute();
        additionalAttribute.setAttribute(new HashMap<String, String>());

        if ((variationFeatureFields[11] != null && !variationFeatureFields[11].equals("\\N"))) {
            additionalAttribute.getAttribute().put("ensemblValidationStatus", variationFeatureFields[11]);
        }

        additionalAttributes.put("ensemblAnnotation", additionalAttribute);

        return additionalAttributes;
    }

    private List<Xref> getXrefs(Map<String, String> sourceMap, int variationId) throws IOException, SQLException {
        List<String[]> variationSynonyms = variationSynonymFile.getVariationRelatedLines(variationId);
        List<Xref> xrefs = null;
        if (variationSynonyms != null && variationSynonyms.size() > 0) {
            String[] arr;
            for (String[] variationSynonymFields : variationSynonyms) {
                // TODO: use constans to identify the fields
                if (sourceMap.get(variationSynonymFields[3]) != null) {
                    arr = sourceMap.get(variationSynonymFields[3]).split(",");
                    if (xrefs == null) {
                        xrefs = new ArrayList<>();
                    }
                    xrefs.add(new Xref(variationSynonymFields[4], arr[0]));
                }
            }
        }
        return xrefs;
    }

    private List<TranscriptVariation> getTranscriptVariations(int variationId, String variationFeatureId) throws IOException, SQLException {
        // Note the ID used, TranscriptVariation references to VariationFeature no Variation !!!
        List<TranscriptVariation> transcriptVariation = new ArrayList<>();
        List<String[]> resultTranscriptVariations = variationTranscriptFile.getVariationRelatedLines(variationId);
        //getVariationTranscripts(variationId, Integer.parseInt(variationFeatureId));
        if (resultTranscriptVariations != null && resultTranscriptVariations.size() > 0) {
            for (String[] transcriptVariationFields : resultTranscriptVariations) {
                if (transcriptVariationFields[VARIATION_FEATURE_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE].equals(variationFeatureId)) {
                    TranscriptVariation tv = buildTranscriptVariation(transcriptVariationFields);
                    transcriptVariation.add(tv);
                }
            }
        }
        return transcriptVariation;
    }

    private TranscriptVariation buildTranscriptVariation(String[] transVarFields) {
        return new TranscriptVariation(
                (transVarFields[2] != null && !transVarFields[2].equals("\\N")) ? transVarFields[2] : null,
                (transVarFields[3] != null && !transVarFields[3].equals("\\N")) ? transVarFields[3] : null,
                (transVarFields[4] != null && !transVarFields[4].equals("\\N")) ? transVarFields[4] : null,
                Arrays.asList(transVarFields[5].split(",")),
                (transVarFields[6] != null && !transVarFields[6].equals("\\N")) ? Integer.parseInt(transVarFields[6]) : 0,
                (transVarFields[7] != null && !transVarFields[7].equals("\\N")) ? Integer.parseInt(transVarFields[7]) : 0,
                (transVarFields[8] != null && !transVarFields[8].equals("\\N")) ? Integer.parseInt(transVarFields[8]) : 0,
                (transVarFields[9] != null && !transVarFields[9].equals("\\N")) ? Integer.parseInt(transVarFields[9]) : 0,
                (transVarFields[10] != null && !transVarFields[10].equals("\\N")) ? Integer.parseInt(transVarFields[10]) : 0,
                (transVarFields[11] != null && !transVarFields[11].equals("\\N")) ? Integer.parseInt(transVarFields[11]) : 0,
                (transVarFields[12] != null && !transVarFields[12].equals("\\N")) ? Integer.parseInt(transVarFields[12]) : 0,
                (transVarFields[13] != null && !transVarFields[13].equals("\\N")) ? transVarFields[13] : null,
                (transVarFields[14] != null && !transVarFields[14].equals("\\N")) ? transVarFields[14] : null,
                (transVarFields[15] != null && !transVarFields[15].equals("\\N")) ? transVarFields[15] : null,
                (transVarFields[16] != null && !transVarFields[16].equals("\\N")) ? transVarFields[16] : null,
                (transVarFields[17] != null && !transVarFields[17].equals("\\N")) ? transVarFields[17] : null,
                (transVarFields[18] != null && !transVarFields[18].equals("\\N")) ? transVarFields[18] : null,
                (transVarFields[19] != null && !transVarFields[19].equals("\\N")) ? Float.parseFloat(transVarFields[19]) : null,
                (transVarFields[20] != null && !transVarFields[20].equals("\\N")) ? transVarFields[20] : null,
                (transVarFields[21] != null && !transVarFields[21].equals("\\N")) ? Float.parseFloat(transVarFields[21]) : null);
    }

    private void gunzipVariationInputFiles() throws IOException, InterruptedException {
        logger.info("Unzipping variation files ...");
        Stopwatch stopwatch = Stopwatch.createStarted();

        variationFile.gunzip();
        variationTranscriptFile.gunzip();
        variationFeatureFile.gunzip();
        variationSynonymFile.gunzip();

        logger.info("Done");
        logger.debug("Elapsed time unzipping files: {}", stopwatch);
    }

    private void gzipVariationFiles(Path variationDirectoryPath) throws IOException, InterruptedException {
        logger.info("Compressing variation files ...");
        Stopwatch stopwatch = Stopwatch.createStarted();

        variationFile.gzip();
        variationFeatureFile.gzip();
        variationTranscriptFile.gzip();
        variationSynonymFile.gzip();

        logger.info("Files compressed");
        logger.debug("Elapsed time compressing files: {}", stopwatch);
    }

    private String getOutputFileName(String chromosome) {
        String outputFileName = outputFileNames.get(chromosome);
        if (outputFileName == null) {
            outputFileName = "variation_chr" + chromosome;
            outputFileNames.put(chromosome, outputFileName);
        }
        return outputFileName;
    }
}
