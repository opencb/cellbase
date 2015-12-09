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

import com.google.common.base.Stopwatch;
import htsjdk.tribble.readers.TabixReader;
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
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils.getSequenceOntologyTerms;

public class VariationParser extends CellBaseParser {

    private static final String VARIATION_FILENAME = "variation.txt";
    protected static final String PREPROCESSED_VARIATION_FILENAME = "variation.sorted.txt";
    private static final String VARIATION_FEATURE_FILENAME = "variation_feature.txt";
    private static final String TRANSCRIPT_VARIATION_FILENAME = "transcript_variation.txt";
    private static final String VARIATION_SYNONYM_FILENAME = "variation_synonym.txt";
    private static final String VARIATION_FREQUENCIES_FILENAME = "eva_population_freqs.sorted.txt.gz";

    private static final String THOUSAND_GENOMES_PHASE_1_STUDY = "1000GENOMES_phase_1";
    private static final String THOUSAND_GENOMES_PHASE_3_STUDY = "1000GENOMES_phase_3";
    private static final String ESP_6500_STUDY = "ESP_6500";
    private static final String EXAC_STUDY = "ExAC";
    private static final String THOUSAND_GENOMES_ALL_POPULATION = "ALL";
    private static final String THOUSAND_GENOMES_AMERICAN_POPULATION = "AMR";
    private static final String THOUSAND_GENOMES_ASIAN_POPULATION = "ASN";
    private static final String THOUSAND_GENOMES_AFRICAN_POPULATION = "AFR";
    private static final String THOUSAND_GENOMES_EUROPEAN_POPULATION = "EUR";
    private static final String THOUSAND_GENOMES_EASTASIAN_POPULATION = "EAS";
    private static final String THOUSAND_GENOMES_SOUTHASIAN_POPULATION = "SAS";
    private static final String ESP_EUROPEAN_AMERICAN_POPULATION = "European_American";
    private static final String ESP_AFRICAN_AMERICAN_POPULATION = "African_American";
    private static final String ESP_ALL_POPULATION = "All";
    private static final String EXAC_AFRICAN_POPULATION = "AFR";
    private static final String EXAC_LATINO_POPULATION = "AMR";
    private static final String EXAC_EAST_ASIAN_POPULATION = "EAS";
    private static final String EXAC_FINNISH_POPULATION = "FIN";
    private static final String EXAC_NON_FINNISH_EUROPEAN_POPULATION = "NFE";
    private static final String EXAC_SOUTH_ASIAN_POPULATION = "SAS";
    private static final String EXAC_OTHER_POPULATION = "OTH";
    private static final String EXAC_ALL_POPULATION = "ALL";

    private Path variationDirectoryPath;

    private static final int VARIATION_FEATURE_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE = 1;

    private Pattern cnvPattern;
    private static final String SEQUENCE_GROUP = "seq";
    private static final String COUNT_GROUP = "count";

    private Pattern populationFrequnciesPattern;
    private static final String POPULATION_ID_GROUP = "popId";
    private static final String REFERENCE_FREQUENCY_GROUP = "ref";
    private static final String ALTERNATE_FREQUENCY_GROUP = "alt";
    private TabixReader frequenciesTabixReader;
    private final Set<String> thousandGenomesPhase1MissedPopulations;
    private final Set<String> thousandGenomesPhase3MissedPopulations;

    private CellBaseFileSerializer fileSerializer;
    private Map<String, String> outputFileNames;

    private VariationFile variationFile;
    private VariationTranscriptFile variationTranscriptFile;
    private VariationFeatureFile variationFeatureFile;
    private VariationSynonymFile variationSynonymFile;

    public VariationParser(Path variationDirectoryPath, CellBaseFileSerializer serializer) {
        super(serializer);
        fileSerializer = serializer;
        this.variationDirectoryPath = variationDirectoryPath;
        cnvPattern = Pattern.compile("((?<" + SEQUENCE_GROUP + ">\\(\\w+\\))" + "(?<" + COUNT_GROUP + ">\\d*))+");
        populationFrequnciesPattern = Pattern.compile("(?<" + POPULATION_ID_GROUP + ">\\w+):(?<" + REFERENCE_FREQUENCY_GROUP
                + ">\\d+(.\\d)*),(?<" + ALTERNATE_FREQUENCY_GROUP + ">\\d+(.\\d)*)");
        thousandGenomesPhase1MissedPopulations = new HashSet<>();
        thousandGenomesPhase3MissedPopulations = new HashSet<>();
        outputFileNames = new HashMap<>();
        // create files
        variationFile = new VariationFile(variationDirectoryPath);
        variationFeatureFile = new VariationFeatureFile(variationDirectoryPath);
        variationTranscriptFile = new VariationTranscriptFile(variationDirectoryPath);
        variationSynonymFile = new VariationSynonymFile(variationDirectoryPath);
    }

    @Override
    public void parse() throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        if (!Files.exists(variationDirectoryPath) || !Files.isDirectory(variationDirectoryPath)
                || !Files.isReadable(variationDirectoryPath)) {
            throw new IOException("Variation directory whether does not exist, is not a directory or cannot be read");
        }
        if (!existsZippedOrUnzippedFile(VARIATION_FILENAME) || isEmpty(variationDirectoryPath.resolve(VARIATION_FILENAME).toString())) {
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
                            Map<String, Object> additionalAttributes = getAdditionalAttributes(variationFields, variationFeatureFields);

                            List<ConsequenceType> conseqTypes = getConsequenceTypes(transcriptVariation);
                            String strand = variationFeatureFields[4];

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
                                    List<PopulationFrequency> populationFrequencies = getPopulationFrequencies(chromosome, start, reference,
                                            alternate);
                                    // build and serialize variant
                                    Variant variation = buildVariant(chromosome, start, end, reference, alternate, type, ids, hgvs,
                                            additionalAttributes, conseqTypes, id, xrefs, populationFrequencies, strand);
                                    fileSerializer.serialize(variation, getOutputFileName(chromosome));
                                }
                            }
                        }
                    }

                    if (++countprocess % 100000 == 0 && countprocess != 0) {
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
            // TODO: just for testing, remove
            if (countprocess % 1000000 == 0) {
                break;
            }
        }

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
        if (Files.exists(variationDirectoryPath.resolve(VARIATION_FREQUENCIES_FILENAME))) {
            frequenciesTabixReader = new TabixReader(variationDirectoryPath.resolve(VARIATION_FREQUENCIES_FILENAME).toString());
        }
    }

    public Variant buildVariant(String chromosome, int start, int end, String reference, String alternate, VariantType type,
                                List<String> ids, List<String> hgvs, Map<String, Object> additionalAttributes,
                                List<ConsequenceType> conseqTypes, String id, List<Xref> xrefs,
                                List<PopulationFrequency> populationFrequencies, String strand)
    {
        Variant variant = new Variant(chromosome, start, end, reference, alternate);
        variant.setIds(ids);
        variant.setType(type);
        VariantAnnotation variantAnnotation = new VariantAnnotation(chromosome, start, end, reference, alternate, id,
                xrefs, hgvs, conseqTypes, populationFrequencies, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, Collections.EMPTY_LIST, null, Collections.EMPTY_LIST, additionalAttributes);
        variant.setAnnotation(variantAnnotation);
        variant.setStrand(strand);

        return variant;
    }

    private VariantType getVariantType(String reference, String alternate) {
        if (reference.length() != alternate.length()) {
            return VariantType.INDEL;
        } else {
            if (reference.equals('-') || alternate.equals('-')) {
                return VariantType.INDEL;
            } else if (reference.contains("(") || alternate.contains("(")) {
                return checkSnv(reference, alternate);
            } else {
                return VariantType.SNV;
            }
        }
    }

    private VariantType checkSnv(String reference, String alternate) {
        Matcher referenceMatcher = cnvPattern.matcher(reference);
        Matcher alternateMatcher = cnvPattern.matcher(alternate);
        logger.debug("Checking CNV variant {}/{}", reference, alternate);
        if (referenceMatcher.matches() && alternateMatcher.matches()) {
            if (referenceMatcher.group(SEQUENCE_GROUP).equals(alternateMatcher.group(SEQUENCE_GROUP))
                    && !referenceMatcher.group(COUNT_GROUP).equals(alternateMatcher.group(COUNT_GROUP)))
            {
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
        List<String> hgvs = new ArrayList<>();
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
        return hgvs;
    }

    private List<ConsequenceType> getConsequenceTypes(List<TranscriptVariation> transcriptVariations) {
        List<ConsequenceType>  consequenceTypes = new ArrayList<>();
        for (TranscriptVariation transcriptVariation : transcriptVariations) {
            List<Score> substitionScores = getSubstitutionScores(transcriptVariation);

            // get peptide reference and alternate
            String peptideReference = null,
                    peptideAlternate = null;
            if (!transcriptVariation.getPeptideAlleleString().equals("\\N")) {
                String[] peptideAlleles = transcriptVariation.getPeptideAlleleString().split("/");
                peptideReference = peptideAlleles[0];
                if (peptideAlleles.length == 1) {
                    peptideAlternate = peptideAlleles[0];
                } else {
                    peptideAlternate = peptideAlleles[1];
                }
            }

            ProteinVariantAnnotation proteinVariantAnnotation = new ProteinVariantAnnotation(null, null, 0,
                    peptideReference, peptideAlternate, null, null, substitionScores, null , null);

            List<SequenceOntologyTerm> soTerms = null;
            try {
                soTerms = getSequenceOntologyTerms(transcriptVariation.getConsequenceTypes());
            } catch (SOTermNotAvailableException e) {
                logger.warn(e.getMessage());
            }

            consequenceTypes.add(new ConsequenceType(null, null, transcriptVariation.getTranscriptId(), null, null,
                    Collections.EMPTY_LIST, transcriptVariation.getCdnaStart(), transcriptVariation.getCdsStart(),
                    transcriptVariation.getCodonAlleleString(), proteinVariantAnnotation, soTerms));

        }
        return consequenceTypes;
    }

    private List<Score> getSubstitutionScores(TranscriptVariation transcriptVariation) {
        List<Score> substitionScores = new ArrayList<>();
        substitionScores.add(new Score((double) transcriptVariation.getPolyphenScore(), "Polyphen", ""));
        substitionScores.add(new Score((double) transcriptVariation.getSiftScore(), "Sift", ""));
        return substitionScores;
    }

    private String[] getAllelesArray(String[] variationFeatureFields) {
        String[] allelesArray = null;
        if (variationFeatureFields != null && variationFeatureFields[6] != null) {
            allelesArray = variationFeatureFields[6].split("/");
            if (allelesArray.length == 1) {
                allelesArray = null;
            }
        }
        return allelesArray;
    }

    private Map<String, Object> getAdditionalAttributes(String[] variationFields, String[] variationFeatureFields) {
        Map<String, Object> additionalAttributes = new HashMap<>();
        String ancestralAllele = (variationFields[4] != null && !variationFields[4].equals("\\N")) ? variationFields[4] : "";
        additionalAttributes.put("Ensembl Ancestral Allele", ancestralAllele);
        additionalAttributes.put("Ensembl Validation Status", (variationFeatureFields[11] != null
                && !variationFeatureFields[11].equals("\\N")) ? variationFeatureFields[11] : "");
        additionalAttributes.put("Ensembl Evidence", (variationFeatureFields[20] != null
                && !variationFeatureFields[20].equals("\\N")) ? variationFeatureFields[20] : "");
        additionalAttributes.put("Minor Allele", (variationFeatureFields[16] != null
                && !variationFeatureFields[16].equals("\\N")) ? variationFeatureFields[16] : "");
        additionalAttributes.put("Minor Allele Freq", (variationFeatureFields[17] != null
                && !variationFeatureFields[17].equals("\\N")) ? variationFeatureFields[17] : "");
        return additionalAttributes;
    }

    private List<Xref> getXrefs(Map<String, String> sourceMap, int variationId) throws IOException, SQLException {
        List<String[]> variationSynonyms = variationSynonymFile.getVariationRelatedLines(variationId);
        List<Xref> xrefs = new ArrayList<>();
        if (variationSynonyms != null && variationSynonyms.size() > 0) {
            String[] arr;
            for (String[] variationSynonymFields : variationSynonyms) {
                // TODO: use constans to identify the fields
                if (sourceMap.get(variationSynonymFields[3]) != null) {
                    arr = sourceMap.get(variationSynonymFields[3]).split(",");
                    xrefs.add(new Xref(variationSynonymFields[4], arr[0]));
                }
            }
        }
        return xrefs;
    }

    private List<PopulationFrequency> getPopulationFrequencies(String chromosome, int start, String referenceAllele,
                                                               String alternativeAllele) throws IOException {
        List<PopulationFrequency> populationFrequencies;
        String variationFrequenciesString = getVariationFrequenciesString(chromosome, start, referenceAllele, alternativeAllele);
        if (variationFrequenciesString != null) {
            populationFrequencies = parseVariationFrequenciesString(variationFrequenciesString, referenceAllele, alternativeAllele);
        } else {
            populationFrequencies = Collections.EMPTY_LIST;
        }
        return populationFrequencies;
    }

    private String getVariationFrequenciesString(String chromosome, int start, String reference, String alternate) throws IOException {
        try {
            if (frequenciesTabixReader != null) {
                TabixReader.Iterator frequenciesFileIterator = frequenciesTabixReader.query(chromosome, start - 1, start);
                if (frequenciesFileIterator != null) {
                    String variationFrequenciesLine = frequenciesFileIterator.next();
                    while (variationFrequenciesLine != null) {
                        String[] variationFrequenciesFields = variationFrequenciesLine.split("\t");
                        if (Integer.valueOf(variationFrequenciesFields[1]) == start && variationFrequenciesFields[3].equals(reference)
                                && variationFrequenciesFields[4].equals(alternate)) {
                            return variationFrequenciesFields[6];
                        }
                        variationFrequenciesLine = frequenciesFileIterator.next();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error getting variation {}:{} {}/{} frequencies: {}", chromosome, start, reference, alternate, e.getMessage());
        }
        return null;
    }

    private List<PopulationFrequency> parseVariationFrequenciesString(String variationFrequenciesString, String referenceAllele,
                                                                      String alternativeAllele) {
        List<PopulationFrequency> frequencies = new ArrayList<>();
        for (String populationFrequency : variationFrequenciesString.split(";")) {
            frequencies.add(parsePopulationFrequency(populationFrequency, referenceAllele, alternativeAllele));
        }

        thousandGenomesPhase1MissedPopulations.add(THOUSAND_GENOMES_AFRICAN_POPULATION);
        thousandGenomesPhase1MissedPopulations.add(THOUSAND_GENOMES_AMERICAN_POPULATION);
        thousandGenomesPhase1MissedPopulations.add(THOUSAND_GENOMES_EUROPEAN_POPULATION);
        thousandGenomesPhase1MissedPopulations.add(THOUSAND_GENOMES_ASIAN_POPULATION);
        frequencies = addMissedPopulations(frequencies, thousandGenomesPhase1MissedPopulations,
                THOUSAND_GENOMES_PHASE_1_STUDY, THOUSAND_GENOMES_ALL_POPULATION);

        thousandGenomesPhase3MissedPopulations.add(THOUSAND_GENOMES_AFRICAN_POPULATION);
        thousandGenomesPhase3MissedPopulations.add(THOUSAND_GENOMES_AMERICAN_POPULATION);
        thousandGenomesPhase3MissedPopulations.add(THOUSAND_GENOMES_EUROPEAN_POPULATION);
        thousandGenomesPhase3MissedPopulations.add(THOUSAND_GENOMES_EASTASIAN_POPULATION);
        thousandGenomesPhase3MissedPopulations.add(THOUSAND_GENOMES_SOUTHASIAN_POPULATION);
        frequencies = addMissedPopulations(frequencies, thousandGenomesPhase3MissedPopulations,
                THOUSAND_GENOMES_PHASE_3_STUDY, THOUSAND_GENOMES_ALL_POPULATION);

        return frequencies;
    }

    private PopulationFrequency parsePopulationFrequency(String frequencyString, String referenceAllele, String alternativeAllele) {
        PopulationFrequency populationFrequency = null;
        Matcher m = populationFrequnciesPattern.matcher(frequencyString);

        if (m.matches()) {
            String populationName;
            String study = "";
            String population = m.group(POPULATION_ID_GROUP);
            switch (population) {
                case "1000G_PHASE_1_AF":
                    study = THOUSAND_GENOMES_PHASE_1_STUDY;
                    populationName = THOUSAND_GENOMES_ALL_POPULATION;
                    break;
                case "1000G_PHASE_1_AMR_AF":
                    study = THOUSAND_GENOMES_PHASE_1_STUDY;
                    populationName = THOUSAND_GENOMES_AMERICAN_POPULATION;
                    break;
                case "1000G_PHASE_1_ASN_AF":
                    study = THOUSAND_GENOMES_PHASE_1_STUDY;
                    populationName = THOUSAND_GENOMES_ASIAN_POPULATION;
                    break;
                case "1000G_PHASE_1_AFR_AF":
                    study = THOUSAND_GENOMES_PHASE_1_STUDY;
                    populationName = THOUSAND_GENOMES_AFRICAN_POPULATION;
                    break;
                case "1000G_PHASE_1_EUR_AF":
                    study = THOUSAND_GENOMES_PHASE_1_STUDY;
                    populationName = THOUSAND_GENOMES_EUROPEAN_POPULATION;
                    break;
                case "1000G_PHASE_3_AF":
                    study = THOUSAND_GENOMES_PHASE_3_STUDY;
                    populationName = THOUSAND_GENOMES_ALL_POPULATION;
                    break;
                case "1000G_PHASE_3_AMR_AF":
                    study = THOUSAND_GENOMES_PHASE_3_STUDY;
                    populationName = THOUSAND_GENOMES_AMERICAN_POPULATION;
                    break;
                case "1000G_PHASE_3_AFR_AF":
                    study = THOUSAND_GENOMES_PHASE_3_STUDY;
                    populationName = THOUSAND_GENOMES_AFRICAN_POPULATION;
                    break;
                case "1000G_PHASE_3_EUR_AF":
                    study = THOUSAND_GENOMES_PHASE_3_STUDY;
                    populationName = THOUSAND_GENOMES_EUROPEAN_POPULATION;
                    break;
                case "1000G_PHASE_3_EAS_AF":
                    study = THOUSAND_GENOMES_PHASE_3_STUDY;
                    populationName = THOUSAND_GENOMES_EASTASIAN_POPULATION;
                    break;
                case "1000G_PHASE_3_SAS_AF":
                    study = THOUSAND_GENOMES_PHASE_3_STUDY;
                    populationName = THOUSAND_GENOMES_SOUTHASIAN_POPULATION;
                    break;
                case "ESP_6500_EA_AF":
                    study = ESP_6500_STUDY;
                    populationName = ESP_EUROPEAN_AMERICAN_POPULATION;
                    break;
                case "ESP_6500_AA_AF":
                    study = ESP_6500_STUDY;
                    populationName = ESP_AFRICAN_AMERICAN_POPULATION;
                    break;
                case "ESP_6500_ALL_AF":
                    study = ESP_6500_STUDY;
                    populationName = ESP_ALL_POPULATION;
                    break;
                case "EXAC_AFR_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_AFRICAN_POPULATION;
                    break;
                case "EXAC_AMR_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_LATINO_POPULATION;
                    break;
                case "EXAC_EAS_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_EAST_ASIAN_POPULATION;
                    break;
                case "EXAC_FIN_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_FINNISH_POPULATION;
                    break;
                case "EXAC_NFE_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_NON_FINNISH_EUROPEAN_POPULATION;
                    break;
                case "EXAC_SAS_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_SOUTH_ASIAN_POPULATION;
                    break;
                case "EXAC_OTH_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_OTHER_POPULATION;
                    break;
                case "EXAC_ALL_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_ALL_POPULATION;
                    break;
                default:
                    populationName = population;
            }
            Float referenceFrequency = Float.parseFloat(m.group(REFERENCE_FREQUENCY_GROUP));
            Float alternativeFrequency = Float.parseFloat(m.group(ALTERNATE_FREQUENCY_GROUP));

            populationFrequency = new PopulationFrequency(study, populationName, populationName, referenceAllele, alternativeAllele,
                    referenceFrequency, alternativeFrequency, null, null, null);
        }

        return populationFrequency;
    }

    private List<PopulationFrequency> addMissedPopulations(List<PopulationFrequency> frequencies,
                                                           Set<String> missedPopulations, String study,
                                                           String allPopulation) {
        int thousandGenomesPopulationsNumber = missedPopulations.size();

        String refAllele = null;
        String altAllele = null;
        for (PopulationFrequency frequency : frequencies) {
            if (frequency != null && frequency.getStudy() != null && frequency.getStudy().equals(study)) {
                if (frequency.getPopulation().equals(allPopulation)) {
                    refAllele = frequency.getRefAllele();
                    altAllele = frequency.getAltAllele();
                }
                missedPopulations.remove(frequency.getPopulation());
            }
        }

        // if the variation has some superpopulation frequency, but not all, add the missed superpopulations with 1 as ref allele proportion
        if (!missedPopulations.isEmpty() && missedPopulations.size() != thousandGenomesPopulationsNumber) {
            for (String population : missedPopulations) {
                frequencies.add(new PopulationFrequency(study, population, population, refAllele, altAllele, 1f, 0f, null, null, null));
            }
        }

        return frequencies;
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
                (transVarFields[2] != null && !transVarFields[2].equals("\\N")) ? transVarFields[2] : ""
                , (transVarFields[3] != null && !transVarFields[3].equals("\\N")) ? transVarFields[3] : ""
                , (transVarFields[4] != null && !transVarFields[4].equals("\\N")) ? transVarFields[4] : ""
                , Arrays.asList(transVarFields[5].split(","))
                , (transVarFields[6] != null && !transVarFields[6].equals("\\N")) ? Integer.parseInt(transVarFields[6]) : 0
                , (transVarFields[7] != null && !transVarFields[7].equals("\\N")) ? Integer.parseInt(transVarFields[7]) : 0
                , (transVarFields[8] != null && !transVarFields[8].equals("\\N")) ? Integer.parseInt(transVarFields[8]) : 0
                , (transVarFields[9] != null && !transVarFields[9].equals("\\N")) ? Integer.parseInt(transVarFields[9]) : 0
                , (transVarFields[10] != null && !transVarFields[10].equals("\\N")) ? Integer.parseInt(transVarFields[10]) : 0
                , (transVarFields[11] != null && !transVarFields[11].equals("\\N")) ? Integer.parseInt(transVarFields[11]) : 0
                , (transVarFields[12] != null && !transVarFields[12].equals("\\N")) ? Integer.parseInt(transVarFields[12]) : 0
                , (transVarFields[13] != null && !transVarFields[13].equals("\\N")) ? transVarFields[13] : ""
                , (transVarFields[14] != null && !transVarFields[14].equals("\\N")) ? transVarFields[14] : ""
                , (transVarFields[15] != null && !transVarFields[15].equals("\\N")) ? transVarFields[15] : ""
                , (transVarFields[16] != null && !transVarFields[16].equals("\\N")) ? transVarFields[16] : ""
                , (transVarFields[17] != null && !transVarFields[17].equals("\\N")) ? transVarFields[17] : ""
                , (transVarFields[18] != null && !transVarFields[18].equals("\\N")) ? transVarFields[18] : ""
                , (transVarFields[19] != null && !transVarFields[19].equals("\\N")) ? Float.parseFloat(transVarFields[19]) : 0f
                , (transVarFields[20] != null && !transVarFields[20].equals("\\N")) ? transVarFields[20] : ""
                , (transVarFields[21] != null && !transVarFields[21].equals("\\N")) ? Float.parseFloat(transVarFields[21]) : 0f);
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

    private boolean existsZippedOrUnzippedFile(String baseFilename) {
        return Files.exists(variationDirectoryPath.resolve(baseFilename))
                || Files.exists(variationDirectoryPath.resolve(baseFilename + ".gz"));
    }

    private boolean isEmpty(String fileName) throws IOException {
        if (Files.exists(Paths.get(fileName))) {
            return Files.size(Paths.get(fileName)) == 0;
        } else {
            return Files.size(Paths.get(fileName + ".gz")) == 0;
        }
    }

    private void gzipVariationFiles(Path variationDirectoryPath) throws IOException, InterruptedException {
        logger.info("Compressing variation files ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        gzipFile(variationDirectoryPath, VARIATION_FILENAME);
        gzipFile(variationDirectoryPath, PREPROCESSED_VARIATION_FILENAME);
        gzipFile(variationDirectoryPath, VARIATION_FEATURE_FILENAME);
        gzipFile(variationDirectoryPath, TRANSCRIPT_VARIATION_FILENAME);
        gzipFile(variationDirectoryPath, VARIATION_SYNONYM_FILENAME);
        gzipFile(variationDirectoryPath, VariationFeatureFile.PREPROCESSED_VARIATION_FEATURE_FILENAME);
        gzipFile(variationDirectoryPath, VariationTranscriptFile.PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME);
        gzipFile(variationDirectoryPath, VariationSynonymFile.PREPROCESSED_VARIATION_SYNONYM_FILENAME);
        logger.info("Files compressed");
        logger.debug("Elapsed time compressing files: {}", stopwatch);
    }

    private void gzipFile(Path directory, String fileName) throws IOException, InterruptedException {
        Path unzippedFile = directory.resolve(fileName);
        if (Files.exists(unzippedFile)) {
            logger.info("Compressing {}", unzippedFile.toAbsolutePath());
            Process process = Runtime.getRuntime().exec("gzip " + unzippedFile.toAbsolutePath());
            process.waitFor();
        }
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
