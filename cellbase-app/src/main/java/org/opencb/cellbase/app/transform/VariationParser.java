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

package org.opencb.cellbase.app.transform;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.IOUtils;
import org.broad.tribble.readers.TabixReader;
import org.opencb.biodata.models.variation.PopulationFrequency;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variation.TranscriptVariation;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.biodata.models.variation.Xref;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.app.transform.utils.FileUtils;
import org.opencb.cellbase.app.transform.utils.VariationUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariationParser extends CellBaseParser {

    private static final String VARIATION_FILENAME = "variation.txt";
    private static final String PREPROCESSED_VARIATION_FILENAME = "variation.sorted.txt";
    private static final String VARIATION_FEATURE_FILENAME = "variation_feature.txt";
    private static final String TRANSCRIPT_VARIATION_FILENAME = "transcript_variation.txt";
    private static final String VARIATION_SYNONYM_FILENAME = "variation_synonym.txt";
    private static final String VARIATION_FREQUENCIES_FILENAME = "eva_population_freqs.sorted.txt.gz";
    private static final String PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME = "transcript_variation.includingVariationId.txt";
    private static final String PREPROCESSED_VARIATION_FEATURE_FILENAME = "variation_feature.sorted.txt";
    private static final String PREPROCESSED_VARIATION_SYNONYM_FILENAME = "variation_synonym.sorted.txt";

    private static final int VARIATION_FEATURE_FILE_ID = 0;
    private static final int TRANSCRIPT_VARIATION_FILE_ID = 1;
    private static final int VARIATION_SYNONYM_FILE_ID = 2;

    private static final String THOUSAND_GENOMES_STUDY = "1000GENOMES";
    private static final String ESP_6500_STUDY = "ESP_6500";
    private static final String THOUSAND_GENOMES_ALL_POPULATION = "phase_1_ALL";
    private static final String THOUSAND_GENOMES_AMERICAN_POPULATION = "phase_1_AMR";
    private static final String THOUSAND_GENOMES_ASIAN_POPULATION = "phase_1_ASN";
    private static final String THOUSAND_GENOMES_AFRICAN_POPULATION = "phase_1_AFR";
    private static final String THOUSAND_GENOMES_EUROPEAN_POPULATION = "phase_1_EUR";
    private static final String ESP_EUROPEAN_AMERICAN_POPULATION = "European_American";
    private static final String ESP_AFRICAN_AMERICAN_POPULATION = "African_American";

    private Path variationDirectoryPath;

    private BufferedReader variationSynonymsFileReader;
    private BufferedReader variationTranscriptsFileReader;
    private BufferedReader variationFeaturesFileReader;

    private static final int VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FILE = 0;
    private static final int VARIATION_ID_COLUMN_INDEX_IN_VARIATION_SYNONYM_FILE = 1;
    private static final int VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE = 5;
    private static final int VARIATION_FEATURE_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE = 1;
    private int variationIdColumnIndexInTranscriptVariationFile;

    private int[] lastVariationIdInVariationRelatedFiles;
    private boolean[] endOfFileOfVariationRelatedFiles;
    private String[][] lastLineInVariationRelatedFile;
    private int[] variationIdColumnIndexInVariationRelatedFile;
    private BufferedReader[] variationRelatedFileReader;

    private Pattern populationFrequnciesPattern;
    private static final String POPULATION_ID_GROUP = "popId";
    private static final String REFERENCE_FREQUENCY_GROUP = "ref";
    private static final String ALTERNATE_FREQUENCY_GROUP = "alt";
    private TabixReader frequenciesTabixReader;
    private final Set<String> thousandGenomesMissedPopulations;

    private CellBaseFileSerializer fileSerializer;
    private Map<String, String> outputFileNames;

    public VariationParser(Path variationDirectoryPath, CellBaseFileSerializer serializer) {
        super(serializer);
        fileSerializer = serializer;
        this.variationDirectoryPath = variationDirectoryPath;
        populationFrequnciesPattern = Pattern.compile("(?<" + POPULATION_ID_GROUP + ">\\w+):(?<" + REFERENCE_FREQUENCY_GROUP + ">\\d+.\\d+),(?<" + ALTERNATE_FREQUENCY_GROUP + ">\\d+.\\d+)");
        thousandGenomesMissedPopulations = new HashSet<>();
        outputFileNames = new HashMap<>();
    }

    @Override
    public void parse() throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        if (!Files.exists(variationDirectoryPath) || !Files.isDirectory(variationDirectoryPath)
                || !Files.isReadable(variationDirectoryPath)) {
            throw new IOException("Variation directory whether does not exist, is not a directory or cannot be read");
        }
        if(!existsZippedOrUnzippedFile(VARIATION_FILENAME) || isEmpty(variationDirectoryPath.resolve(VARIATION_FILENAME).toString())) {
            throw new IOException("variation.txt.gz whether does not exist, is not a directory or cannot be read");
        }

        Variation variation;

        // To speed up calculation a SQLite database is created with the IDs and file offsets,
        // file must be uncompressed for doing this.
        gunzipVariationInputFiles();

        // add idVariation to transcript_variation file
        preprocessInputFiles();

        // Open variation file, this file never gets uncompressed. It's read from gzip file
        BufferedReader bufferedReaderVariation = getBufferedReader(PREPROCESSED_VARIATION_FILENAME);

        // create buffered readers for all other input files
        createVariationFilesBufferedReaders();

        Map<String, String> seqRegionMap = VariationUtils.parseSeqRegionToMap(variationDirectoryPath);
        Map<String, String> sourceMap = VariationUtils.parseSourceToMap(variationDirectoryPath);

        initializeVariationRelatedArrays();
        Stopwatch globalStartwatch = Stopwatch.createStarted();
        Stopwatch batchWatch = Stopwatch.createStarted();
        logger.info("Parsing variation file " + variationDirectoryPath.resolve(PREPROCESSED_VARIATION_FILENAME) + " ...");
        long countprocess = 0;
        String line;
        while ((line = bufferedReaderVariation.readLine()) != null) {
            String[] variationFields = line.split("\t");

            int variationId = Integer.parseInt(variationFields[0]);

            List<String[]> resultVariationFeature = getVariationRelatedFields(VARIATION_FEATURE_FILE_ID, variationId);
            if (resultVariationFeature != null && resultVariationFeature.size() > 0) {
                String[] variationFeatureFields = resultVariationFeature.get(0);

                List<TranscriptVariation> transcriptVariation = getTranscriptVariations(variationId, variationFeatureFields[0]);
                List<Xref> xrefs = getXrefs(sourceMap, variationId);

                try {
                    // Preparing the variation alleles
                    String[] allelesArray = getAllelesArray(variationFeatureFields);

                    // For code sanity save chromosome, start, end and id
                    String chromosome = seqRegionMap.get(variationFeatureFields[1]);
                    int start = (variationFeatureFields != null) ? Integer.valueOf(variationFeatureFields[2]) : 0;
                    int end = (variationFeatureFields != null) ? Integer.valueOf(variationFeatureFields[3]) : 0;
                    String id = (variationFields[2] != null && !variationFields[2].equals("\\N")) ? variationFields[2] : "";
                    String reference = (allelesArray[0] != null && !allelesArray[0].equals("\\N")) ? allelesArray[0] : "";
                    String alternate = (allelesArray[1] != null && !allelesArray[1].equals("\\N")) ? allelesArray[1] : "";

                    // Preparing frequencies
                    //List<PopulationFrequency> populationFrequencies = getPopulationFrequencies(variationId, allelesArray);
                    List<PopulationFrequency> populationFrequencies = getPopulationFrequencies(chromosome, start, end, id, reference, alternate);

                    // TODO: check that variationFeatureFields is always different to null and intergenic-variant is never used
                    //List<String> consequenceTypes = (variationFeatureFields != null) ? Arrays.asList(variationFeatureFields[12].split(",")) : Arrays.asList("intergenic_variant");
                    List<String> consequenceTypes = Arrays.asList(variationFeatureFields[12].split(","));
                    String displayConsequenceType = getDisplayConsequenceType(consequenceTypes);


                    // we have all the necessary to construct the 'variation' object
                    variation = buildVariation(variationFields, variationFeatureFields, chromosome, start, end, id, reference, alternate, transcriptVariation, xrefs, populationFrequencies, allelesArray, consequenceTypes, displayConsequenceType);

                    if (++countprocess % 100000 == 0 && countprocess != 0) {
                        logger.info("Processed variations: " + countprocess);
                        logger.debug("Elapsed time processing batch: " + batchWatch);
                        batchWatch.reset();
                        batchWatch.start();
                    }

                    fileSerializer.serialize(variation, getOutputFileName(chromosome));
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error parsing variation: " + e.getMessage());
                    logger.error("Last line processed: " + line);
                    break;
                }
            }
            // TODO: just for testing, remove
            //if (countprocess % 100000 == 0) {
            //    break;
            //}
        }

        logger.info("Variation parsing finished");
        logger.info("Variants processed: " + countprocess);
        logger.debug("Elapsed time parsing: " + globalStartwatch);

        gzipVariationFiles(variationDirectoryPath);

        try {
            bufferedReaderVariation.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void preprocessInputFiles() throws IOException, InterruptedException {
        preprocessVariationFile();
        sortInputFile(VARIATION_FEATURE_FILENAME, PREPROCESSED_VARIATION_FEATURE_FILENAME, VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE);
        sortInputFile(VARIATION_SYNONYM_FILENAME, PREPROCESSED_VARIATION_SYNONYM_FILENAME, VARIATION_ID_COLUMN_INDEX_IN_VARIATION_SYNONYM_FILE);
        preprocessTranscriptVariationFile();
    }

    private void preprocessVariationFile() throws IOException, InterruptedException {
       if (!existsZippedOrUnzippedFile(PREPROCESSED_VARIATION_FILENAME)) {
            sortInputFile(VARIATION_FILENAME, PREPROCESSED_VARIATION_FILENAME, VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FILE);
        }
    }

    private void sortInputFile(String unsortedFileName, String sortedFileName, int columnToSortByIndex) throws IOException, InterruptedException {
        if (!existsZippedOrUnzippedFile(sortedFileName)) {
            Path sortedFile = variationDirectoryPath.resolve(sortedFileName);
            Path unsortedFile = variationDirectoryPath.resolve(unsortedFileName);
            sortFileByNumericColumn(unsortedFile, sortedFile, columnToSortByIndex);
        }
    }

    private void sortFileByNumericColumn(Path inputFile, Path outputFile, int columnIndex) throws InterruptedException, IOException {
        this.logger.info("Sorting file " + inputFile + " into " + outputFile + " ...");

        // increment column index by 1, beacause Java indexes are 0-based and 'sort' command uses 1-based indexes
        columnIndex++;
        ProcessBuilder pb = new ProcessBuilder("sort", "-t", "\t", "-k", Integer.toString(columnIndex), "-n", "--stable", inputFile.toAbsolutePath().toString(), "-T", variationDirectoryPath.toString(), "-o", outputFile.toAbsolutePath().toString()); // System.getProperty("java.io.tmpdir")
        this.logger.debug("Executing '" + StringUtils.join(pb.command(), " ") + "' ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Process process = pb.start();
        process.waitFor();

        int returnedValue = process.exitValue();
        if (returnedValue != 0) {
            String errorMessage = IOUtils.toString(process.getErrorStream());
            logger.error("Error sorting " + inputFile);
            logger.error(errorMessage);
            throw new RuntimeException("Error sorting " + inputFile);
        }
        this.logger.info("Sorted");
        this.logger.debug("Elapsed time sorting file: " + stopwatch);
    }

    private void preprocessTranscriptVariationFile() throws IOException, InterruptedException {
        Path preprocessedTranscriptVariationFile = variationDirectoryPath.resolve(PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME);
        if (!existsZippedOrUnzippedFile(PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME)) {
            this.logger.info("Preprocessing " + TRANSCRIPT_VARIATION_FILENAME + " file ...");
            Stopwatch stopwatch = Stopwatch.createStarted();

            // add variationId to transcript_variation file
            Map<Integer, Integer> variationFeatureToVariationId = createVariationFeatureIdToVariationIdMap();
            Path transcriptVariationTempFile = addVariationIdToTranscriptVariationFile(variationFeatureToVariationId);
            // transcript_variation file columns number can vary, so we need to save the variationId column index because it is not constant
            variationIdColumnIndexInTranscriptVariationFile = getVariationIdColumnIndexInTranscriptVariationFile(transcriptVariationTempFile);
            sortFileByNumericColumn(transcriptVariationTempFile, preprocessedTranscriptVariationFile, variationIdColumnIndexInTranscriptVariationFile);

            this.logger.info("Removing temp file " + transcriptVariationTempFile);
            transcriptVariationTempFile.toFile().delete();
            this.logger.info("Removed");

            this.logger.info(TRANSCRIPT_VARIATION_FILENAME + " preprocessed. New file " +
                    PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME + " including (and sorted by) variation Id has been created");
            this.logger.debug("Elapsed time preprocessing transcript variation file: " + stopwatch);
        } else {
            // transcript_variation file columns number can vary, so we need to save the variationId column index because it is not constant
            variationIdColumnIndexInTranscriptVariationFile = getVariationIdColumnIndexInTranscriptVariationFile(preprocessedTranscriptVariationFile);
        }

    }

    private int getVariationIdColumnIndexInTranscriptVariationFile(Path preprocessedTranscriptVariationFile) throws IOException {
        BufferedReader br = getBufferedReader(preprocessedTranscriptVariationFile);
        int variationIdColumIndex = br.readLine().split("\t").length - 1;
        br.close();
        return variationIdColumIndex;
    }

    private Path addVariationIdToTranscriptVariationFile(Map<Integer, Integer> variationFeatureToVariationId) throws IOException {
        Path transcriptVariationTempFile = variationDirectoryPath.resolve(TRANSCRIPT_VARIATION_FILENAME + ".tmp");
        this.logger.info("Adding variation Id to transcript variations and saving them into " + transcriptVariationTempFile + " ...");
        Stopwatch stopwatch = Stopwatch.createStarted();

        Path unpreprocessedTranscriptVariationFile = variationDirectoryPath.resolve(TRANSCRIPT_VARIATION_FILENAME);
        BufferedReader br = getBufferedReader(unpreprocessedTranscriptVariationFile);
        BufferedWriter bw = Files.newBufferedWriter(transcriptVariationTempFile, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        String line;
        while ((line = br.readLine()) != null) {
            // TODO: add limit parameter would do that run faster?
            // TODO: use a precompiled pattern would improve efficiency
            Integer variationFeatureId = Integer.valueOf(line.split("\t")[1]);
            Integer variationId = variationFeatureToVariationId.get(variationFeatureId);
            bw.write(line + "\t" + variationId + "\n");
        }



        br.close();
        bw.close();

        this.logger.info("Added");
        this.logger.debug("Elapsed time adding variation Id to transcript variation file: " + stopwatch);

        return transcriptVariationTempFile;
    }

    private Map<Integer, Integer> createVariationFeatureIdToVariationIdMap() throws IOException {
        this.logger.info("Creating variationFeatureId to variationId map ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<Integer, Integer> variationFeatureToVariationId = new HashMap<>();

        BufferedReader variationFeatFileReader = getBufferedReader(variationDirectoryPath.resolve(VARIATION_FEATURE_FILENAME));

        String line;
        while ((line = variationFeatFileReader.readLine()) != null) {
            // TODO: add limit parameter would do that run faster?
            // TODO: use a precompiled pattern would improve efficiency
            String[] fields = line.split("\t");
            Integer variationFeatureId = Integer.valueOf(fields[0]);
            Integer variationId = Integer.valueOf(fields[5]);
            variationFeatureToVariationId.put(variationFeatureId, variationId);
        }

        variationFeatFileReader.close();

        this.logger.info("Done");
        this.logger.debug("Elapsed time creating variationFeatureId to variationId map: " + stopwatch);

        return variationFeatureToVariationId;
    }

    private void createVariationFilesBufferedReaders() throws IOException {
        variationFeaturesFileReader = getBufferedReader(PREPROCESSED_VARIATION_FEATURE_FILENAME);
        variationSynonymsFileReader = getBufferedReader(PREPROCESSED_VARIATION_SYNONYM_FILENAME);
        variationTranscriptsFileReader = getBufferedReader(PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME);
        if(Files.exists(variationDirectoryPath.resolve(VARIATION_FREQUENCIES_FILENAME))) {
            frequenciesTabixReader = new TabixReader(variationDirectoryPath.resolve(VARIATION_FREQUENCIES_FILENAME).toString());
        }
    }

    private Variation buildVariation(String[] variationFields, String[] variationFeatureFields, String chromosome,
                                     int start, int end, String id, String reference, String alternate,
                                     List<TranscriptVariation> transcriptVariation, List<Xref> xrefs,
                                     List<PopulationFrequency> populationFrequencies, String[] allelesArray,
                                     List<String> consequenceTypes, String displayConsequenceType)
    {
        Variation variation;
        variation = new Variation(id, chromosome, "SNV", start, end, variationFeatureFields[4],
                reference, alternate, variationFeatureFields[6],
                (variationFields[4] != null && !variationFields[4].equals("\\N")) ? variationFields[4] : "",
                displayConsequenceType,
//							species, assembly, source, version,
                consequenceTypes, transcriptVariation, null, null, populationFrequencies, xrefs, /*"featureId",*/
                (variationFeatureFields[16] != null && !variationFeatureFields[16].equals("\\N")) ? variationFeatureFields[16] : "",
                (variationFeatureFields[17] != null && !variationFeatureFields[17].equals("\\N")) ? variationFeatureFields[17] : "",
                (variationFeatureFields[11] != null && !variationFeatureFields[11].equals("\\N")) ? variationFeatureFields[11] : "",
                (variationFeatureFields[20] != null && !variationFeatureFields[20].equals("\\N")) ? variationFeatureFields[20] : "");
        return variation;
    }

    private String getDisplayConsequenceType(List<String> consequenceTypes) {
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

    private String[] getAllelesArray(String[] variationFeatureFields) {
        String[] allelesArray;
        if (variationFeatureFields != null && variationFeatureFields[6] != null) {
            allelesArray = variationFeatureFields[6].split("/");
            if (allelesArray.length == 1) {    // In some cases no '/' exists, ie. in 'HGMD_MUTATION'
                allelesArray = new String[]{"", ""};
            }
        } else {
            allelesArray = new String[]{"", ""};
        }
        return allelesArray;
    }

    private List<Xref> getXrefs(Map<String, String> sourceMap, int variationId) throws IOException, SQLException {
        List<String[]> variationSynonyms = getVariationRelatedFields(VARIATION_SYNONYM_FILE_ID, variationId);
        List<Xref> xrefs = new ArrayList<>();
        if (variationSynonyms != null && variationSynonyms.size() > 0) {
            String arr[];
            for (String[] variationSynonymFields : variationSynonyms) {
                // TODO: use constans to identify the fields
                if (sourceMap.get(variationSynonymFields[3]) != null) {
                    arr = sourceMap.get(variationSynonymFields[3]).split(",");
                    xrefs.add(new Xref(variationSynonymFields[4], arr[0], arr[1]));
                }
            }
        }
        return xrefs;
    }

    private void initializeVariationRelatedArrays() {
        lastLineInVariationRelatedFile = new String[5][];

        lastVariationIdInVariationRelatedFiles = new int[5];
        for (int i=0; i < lastVariationIdInVariationRelatedFiles.length; i++) {
            lastVariationIdInVariationRelatedFiles[i] = -1;
        }
        endOfFileOfVariationRelatedFiles = new boolean[5];

        variationRelatedFileReader = new BufferedReader[5];
        variationRelatedFileReader[VARIATION_FEATURE_FILE_ID] = variationFeaturesFileReader;
        variationRelatedFileReader[TRANSCRIPT_VARIATION_FILE_ID] = variationTranscriptsFileReader;
        variationRelatedFileReader[VARIATION_SYNONYM_FILE_ID] = variationSynonymsFileReader;

        variationIdColumnIndexInVariationRelatedFile = new int[5];
        variationIdColumnIndexInVariationRelatedFile[VARIATION_FEATURE_FILE_ID] = VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE;
        variationIdColumnIndexInVariationRelatedFile[TRANSCRIPT_VARIATION_FILE_ID] = variationIdColumnIndexInTranscriptVariationFile;
        variationIdColumnIndexInVariationRelatedFile[VARIATION_SYNONYM_FILE_ID] = VARIATION_ID_COLUMN_INDEX_IN_VARIATION_SYNONYM_FILE;

    }

    private List<String[]> getVariationRelatedFields(int fileId, int variationId) throws IOException {
        List<String[]> variationRelatedLines;

        readFileLinesUntilReachVariation(fileId, variationId);
        if (endOfFile(fileId) || variationIdExceededInFile(fileId, variationId)) {
            variationRelatedLines = Collections.EMPTY_LIST;
        } else {
            variationRelatedLines = new ArrayList<>();
            while (!endOfFile(fileId) && !variationIdExceededInFile(fileId, variationId)) {
                variationRelatedLines.add(lastLineInVariationRelatedFile[fileId]);
                readLineInVariationRelatedFile(fileId);
            }
        }
        return variationRelatedLines;
    }

    private boolean variationIdExceededInFile(int fileId, int variationId) {
        return lastVariationIdInVariationRelatedFiles[fileId] > variationId;
    }


    private void readFileLinesUntilReachVariation(int fileId, int variationId) throws IOException {
        while (!endOfFileOfVariationRelatedFiles[fileId] && !variationReachedInFile(fileId, variationId)) {
            readLineInVariationRelatedFile(fileId);
        }
    }

    private void readLineInVariationRelatedFile(int fileId) throws IOException {
        String line = variationRelatedFileReader[fileId].readLine();
        if (line == null) {
            endOfFileOfVariationRelatedFiles[fileId] = true;
        } else {
            lastLineInVariationRelatedFile[fileId] = line.split("\t", -1);
            lastVariationIdInVariationRelatedFiles[fileId] = getVariationIdFromLastLineInVariationRelatedFile(fileId);
        }
    }

    private int getVariationIdFromLastLineInVariationRelatedFile(int fileId) {
        int variationId = Integer.parseInt(lastLineInVariationRelatedFile[fileId][variationIdColumnIndexInVariationRelatedFile[fileId]]);
        return variationId;
    }

    private boolean variationReachedInFile(int fileId, int variationId) {
        return lastVariationIdInVariationRelatedFiles[fileId] != -1 && lastVariationIdInVariationRelatedFiles[fileId] >= variationId;
    }

    private boolean endOfFile(int fileId) {
        return endOfFileOfVariationRelatedFiles[fileId];
    }

    private List<PopulationFrequency> getPopulationFrequencies(String chromosome, int start, int end, String id, String referenceAllele, String alternativeAllele) throws IOException {
        List<PopulationFrequency> populationFrequencies;
        String variationFrequenciesString = getVariationFrequenciesString(chromosome, start, end, id);
        if (variationFrequenciesString != null) {
            populationFrequencies = parseVariationFrequenciesString(variationFrequenciesString, referenceAllele, alternativeAllele);
        } else{
            populationFrequencies = Collections.EMPTY_LIST;
        }
        return populationFrequencies;
    }

    private String getVariationFrequenciesString(String chromosome, int start, int end, String id) throws IOException {
        try {
            if(frequenciesTabixReader != null) {
                TabixReader.Iterator frequenciesFileIterator = frequenciesTabixReader.query(chromosome + ":" + start + "-" + end);
                if (frequenciesFileIterator != null) {
                    String variationFrequenciesLine = frequenciesFileIterator.next();
                    while (variationFrequenciesLine != null) {
                        String[] variationFrequenciesFields = variationFrequenciesLine.split("\t");
                        if (variationFrequenciesFields[3].equals(id)) {
                            return variationFrequenciesFields[4];
                        }
                        variationFrequenciesLine = frequenciesFileIterator.next();
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private List<PopulationFrequency> parseVariationFrequenciesString(String variationFrequenciesString, String referenceAllele, String alternativeAllele) {
        List<PopulationFrequency> frequencies = new ArrayList<>();
        for (String populationFrequency : variationFrequenciesString.split(";")) {
            frequencies.add(parsePopulationFrequency(populationFrequency, referenceAllele, alternativeAllele));
        }
        frequencies = add1000GenomesMissedPopulations(frequencies);
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
                case "1000G_AF":
                    study = THOUSAND_GENOMES_STUDY;
                    populationName = THOUSAND_GENOMES_ALL_POPULATION;
                    break;
                case "1000G_AMR_AF":
                    study = THOUSAND_GENOMES_STUDY;
                    populationName = THOUSAND_GENOMES_AMERICAN_POPULATION;
                    break;
                case "1000G_ASN_AF":
                    study = THOUSAND_GENOMES_STUDY;
                    populationName = THOUSAND_GENOMES_ASIAN_POPULATION;
                    break;
                case "1000G_AFR_AF":
                    study = THOUSAND_GENOMES_STUDY;
                    populationName = THOUSAND_GENOMES_AFRICAN_POPULATION;
                    break;
                case "1000G_EUR_AF":
                    study = THOUSAND_GENOMES_STUDY;
                    populationName = THOUSAND_GENOMES_EUROPEAN_POPULATION;
                    break;
                case "ESP_EA_AF":
                    study = ESP_6500_STUDY;
                    populationName = ESP_EUROPEAN_AMERICAN_POPULATION;
                    break;
                case "ESP_AA_AF":
                    study = ESP_6500_STUDY;
                    populationName = ESP_AFRICAN_AMERICAN_POPULATION;
                    break;
                default:
                    populationName = population;
            }
            Float referenceFrequency = Float.parseFloat(m.group(REFERENCE_FREQUENCY_GROUP));
            Float alternativeFrequency = Float.parseFloat(m.group(ALTERNATE_FREQUENCY_GROUP));

            populationFrequency = new PopulationFrequency(study, populationName, populationName, referenceAllele, alternativeAllele, referenceFrequency, alternativeFrequency);
        }

        return populationFrequency;
    }

    private List<PopulationFrequency> add1000GenomesMissedPopulations(List<PopulationFrequency> frequencies) {
        thousandGenomesMissedPopulations.add(THOUSAND_GENOMES_AFRICAN_POPULATION);
        thousandGenomesMissedPopulations.add(THOUSAND_GENOMES_AMERICAN_POPULATION);
        thousandGenomesMissedPopulations.add(THOUSAND_GENOMES_EUROPEAN_POPULATION);
        thousandGenomesMissedPopulations.add(THOUSAND_GENOMES_ASIAN_POPULATION);
        int thousandGenomesPopulationsNumber = thousandGenomesMissedPopulations.size();

        String refAllele = null;
        String altAllele = null;
        for (PopulationFrequency frequency : frequencies) {
            if (frequency.getStudy()!= null && frequency.getStudy().equals(THOUSAND_GENOMES_STUDY)) {
                if (frequency.getPop().equals(THOUSAND_GENOMES_ALL_POPULATION)) {
                    refAllele = frequency.getRefAllele();
                    altAllele = frequency.getAltAllele();
                }
                thousandGenomesMissedPopulations.remove(frequency.getPop());
            }
        }

        // if the variation has some 1000 genomes superpopulation frequency, but not all, add the missed superpopulations with 1 as ref allele proportion
        if (!thousandGenomesMissedPopulations.isEmpty() && thousandGenomesMissedPopulations.size() != thousandGenomesPopulationsNumber) {
            for (String population : thousandGenomesMissedPopulations) {
                frequencies.add(new PopulationFrequency(THOUSAND_GENOMES_STUDY, population, population, refAllele, altAllele, 1, 0));
            }
        }

        return frequencies;
    }

    private List<TranscriptVariation> getTranscriptVariations(int variationId, String variationFeatureId) throws IOException, SQLException {
        // Note the ID used, TranscriptVariation references to VariationFeature no Variation !!!
        List<TranscriptVariation> transcriptVariation = new ArrayList<>();
        List<String[]> resultTranscriptVariations = getVariationRelatedFields(TRANSCRIPT_VARIATION_FILE_ID, variationId);
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

    private TranscriptVariation buildTranscriptVariation(String[] transcriptVariationFields) {
        return new TranscriptVariation((transcriptVariationFields[2] != null && !transcriptVariationFields[2].equals("\\N")) ? transcriptVariationFields[2] : ""
                , (transcriptVariationFields[3] != null && !transcriptVariationFields[3].equals("\\N")) ? transcriptVariationFields[3] : ""
                , (transcriptVariationFields[4] != null && !transcriptVariationFields[4].equals("\\N")) ? transcriptVariationFields[4] : ""
                , Arrays.asList(transcriptVariationFields[5].split(","))
                , (transcriptVariationFields[6] != null && !transcriptVariationFields[6].equals("\\N")) ? Integer.parseInt(transcriptVariationFields[6]) : 0
                , (transcriptVariationFields[7] != null && !transcriptVariationFields[7].equals("\\N")) ? Integer.parseInt(transcriptVariationFields[7]) : 0
                , (transcriptVariationFields[8] != null && !transcriptVariationFields[8].equals("\\N")) ? Integer.parseInt(transcriptVariationFields[8]) : 0
                , (transcriptVariationFields[9] != null && !transcriptVariationFields[9].equals("\\N")) ? Integer.parseInt(transcriptVariationFields[9]) : 0
                , (transcriptVariationFields[10] != null && !transcriptVariationFields[10].equals("\\N")) ? Integer.parseInt(transcriptVariationFields[10]) : 0
                , (transcriptVariationFields[11] != null && !transcriptVariationFields[11].equals("\\N")) ? Integer.parseInt(transcriptVariationFields[11]) : 0
                , (transcriptVariationFields[12] != null && !transcriptVariationFields[12].equals("\\N")) ? Integer.parseInt(transcriptVariationFields[12]) : 0
                , (transcriptVariationFields[13] != null && !transcriptVariationFields[13].equals("\\N")) ? transcriptVariationFields[13] : ""
                , (transcriptVariationFields[14] != null && !transcriptVariationFields[14].equals("\\N")) ? transcriptVariationFields[14] : ""
                , (transcriptVariationFields[15] != null && !transcriptVariationFields[15].equals("\\N")) ? transcriptVariationFields[15] : ""
                , (transcriptVariationFields[16] != null && !transcriptVariationFields[16].equals("\\N")) ? transcriptVariationFields[16] : ""
                , (transcriptVariationFields[17] != null && !transcriptVariationFields[17].equals("\\N")) ? transcriptVariationFields[17] : ""
                , (transcriptVariationFields[18] != null && !transcriptVariationFields[18].equals("\\N")) ? transcriptVariationFields[18] : ""
                , (transcriptVariationFields[19] != null && !transcriptVariationFields[19].equals("\\N")) ? Float.parseFloat(transcriptVariationFields[19]) : 0f
                , (transcriptVariationFields[20] != null && !transcriptVariationFields[20].equals("\\N")) ? transcriptVariationFields[20] : ""
                , (transcriptVariationFields[21] != null && !transcriptVariationFields[21].equals("\\N")) ? Float.parseFloat(transcriptVariationFields[21]) : 0f);
    }

    private void gunzipVariationInputFiles() throws IOException, InterruptedException {
        logger.info("Unzipping variation files ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (!existsZippedOrUnzippedFile(PREPROCESSED_VARIATION_FILENAME)) {
            // unzip variation file name for preprocess it later
            gunzipFileIfNeeded(variationDirectoryPath, VARIATION_FILENAME);
        }
        if (!existsZippedOrUnzippedFile(PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME)) {
            gunzipFileIfNeeded(variationDirectoryPath, TRANSCRIPT_VARIATION_FILENAME);
        }
        if (!existsZippedOrUnzippedFile(PREPROCESSED_VARIATION_FEATURE_FILENAME)) {
            gunzipFileIfNeeded(variationDirectoryPath, VARIATION_FEATURE_FILENAME);
        }
        if (!existsZippedOrUnzippedFile(PREPROCESSED_VARIATION_SYNONYM_FILENAME)) {
            gunzipFileIfNeeded(variationDirectoryPath, VARIATION_SYNONYM_FILENAME);
        }

        logger.info("Done");
        logger.debug("Elapsed time unzipping files: " + stopwatch);
    }

    private boolean existsZippedOrUnzippedFile(String baseFilename) {
        return Files.exists(variationDirectoryPath.resolve(baseFilename)) ||
                Files.exists(variationDirectoryPath.resolve(baseFilename + ".gz"));
    }

    private boolean isEmpty(String fileName) throws IOException {
        if (Files.exists(Paths.get(fileName))) {
            return Files.size(Paths.get(fileName)) == 0;
        } else {
            return Files.size(Paths.get(fileName + ".gz")) == 0;
        }
    }

    private void gunzipFileIfNeeded(Path directory, String fileName) throws IOException, InterruptedException {
        Path zippedFile = directory.resolve(fileName + ".gz");
        if (Files.exists(zippedFile)) {
            logger.info("Unzipping " + zippedFile + "...");
            Process process = Runtime.getRuntime().exec("gunzip " + zippedFile.toAbsolutePath());
            process.waitFor();
        } else {
            Path unzippedFile = directory.resolve(fileName);
            if (Files.exists(unzippedFile)){
                logger.info("File " + unzippedFile + " was previously unzipped: skipping 'gunzip' for this file ...");
            } else {
                throw new FileNotFoundException("File " + zippedFile + " doesn't exist");
            }
        }
    }

    private void gzipVariationFiles(Path variationDirectoryPath) throws IOException, InterruptedException {
        this.logger.info("Compressing variation files ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        gzipFile(variationDirectoryPath, VARIATION_FILENAME);
        gzipFile(variationDirectoryPath, PREPROCESSED_VARIATION_FILENAME);
        gzipFile(variationDirectoryPath, VARIATION_FEATURE_FILENAME);
        gzipFile(variationDirectoryPath, TRANSCRIPT_VARIATION_FILENAME);
        gzipFile(variationDirectoryPath, VARIATION_SYNONYM_FILENAME);
        gzipFile(variationDirectoryPath, PREPROCESSED_VARIATION_FEATURE_FILENAME);
        gzipFile(variationDirectoryPath, PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME);
        gzipFile(variationDirectoryPath, PREPROCESSED_VARIATION_SYNONYM_FILENAME);
        this.logger.info("Files compressed");
        this.logger.debug("Elapsed time compressing files: " + stopwatch);
    }

    private void gzipFile(Path directory, String fileName) throws IOException, InterruptedException {
        Path unzippedFile = directory.resolve(fileName);
        if (Files.exists(unzippedFile)) {
            this.logger.info("Compressing " + unzippedFile.toAbsolutePath());
            Process process = Runtime.getRuntime().exec("gzip " + unzippedFile.toAbsolutePath());
            process.waitFor();
        }
    }

    private BufferedReader getBufferedReader(Path file) throws IOException {
        if (Files.exists(file)) {
            return FileUtils.newBufferedReader(file);
        } else {
            return FileUtils.newBufferedReader(Paths.get(file.toString() + ".gz"));
        }
    }

    private BufferedReader getBufferedReader(String fileName) throws IOException {
        Path inputFile;
        if (Files.exists(variationDirectoryPath.resolve(fileName))) {
            inputFile = variationDirectoryPath.resolve(fileName);
        } else {
            inputFile = variationDirectoryPath.resolve(fileName + ".gz");
        }
        return FileUtils.newBufferedReader(inputFile);
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
