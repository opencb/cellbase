package org.opencb.cellbase.build.transform;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.IOUtils;
import org.opencb.biodata.models.variation.PopulationFrequency;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variation.TranscriptVariation;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.biodata.models.variation.Xref;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.build.transform.utils.FileUtils;
import org.opencb.cellbase.build.transform.utils.VariationUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.*;

public class VariationParser extends CellBaseParser {

    private static final Set<String> POPULATIONS_TO_INCLUDE = new HashSet<>();
    private static final Map<String, String> SUPER_POPULATION = new HashMap<>();

    static {
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_FIN");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_IBS");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_YRI");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_CHB");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_JPT");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_LWK");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_TSI");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_PUR");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_CHS");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_GBR");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_CLM");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_MXL");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_ASW");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_CEU");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_EUR");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_ASN");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_ALL");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_AFR");
        POPULATIONS_TO_INCLUDE.add("1000GENOMES:phase_1_AMR");
        POPULATIONS_TO_INCLUDE.add("ESP6500:African_American");
        POPULATIONS_TO_INCLUDE.add("ESP6500:European_American");

        SUPER_POPULATION.put("1000GENOMES:phase_1_FIN", "1000GENOMES:phase_1_EUR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_IBS", "1000GENOMES:phase_1_EUR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_YRI", "1000GENOMES:phase_1_AFR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_CHB", "1000GENOMES:phase_1_ASN");
        SUPER_POPULATION.put("1000GENOMES:phase_1_JPT", "1000GENOMES:phase_1_ASN");
        SUPER_POPULATION.put("1000GENOMES:phase_1_LWK", "1000GENOMES:phase_1_AFR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_TSI", "1000GENOMES:phase_1_EUR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_PUR", "1000GENOMES:phase_1_AMR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_CHS", "1000GENOMES:phase_1_ASN");
        SUPER_POPULATION.put("1000GENOMES:phase_1_GBR", "1000GENOMES:phase_1_EUR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_CLM", "1000GENOMES:phase_1_AMR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_MXL", "1000GENOMES:phase_1_AMR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_ASW", "1000GENOMES:phase_1_AFR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_CEU", "1000GENOMES:phase_1_EUR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_EUR", "1000GENOMES:phase_1_EUR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_ASN", "1000GENOMES:phase_1_ASN");
        SUPER_POPULATION.put("1000GENOMES:phase_1_ALL", "1000GENOMES:phase_1_ALL");
        SUPER_POPULATION.put("1000GENOMES:phase_1_AFR", "1000GENOMES:phase_1_AFR");
        SUPER_POPULATION.put("1000GENOMES:phase_1_AMR", "1000GENOMES:phase_1_AMR");
        SUPER_POPULATION.put("ESP6500:African_American", "ESP6500:African_American");
        SUPER_POPULATION.put("ESP6500:European_American", "ESP6500:European_American");

    }

    private static final String VARIATION_FILENAME = "variation.txt";
    private static final String PREPROCESSED_VARIATION_FILENAME = "variation.sorted.txt";
    private static final String VARIATION_FEATURE_FILENAME = "variation_feature.txt";
    private static final String TRANSCRIPT_VARIATION_FILENAME = "transcript_variation.txt";
    private static final String VARIATION_SYNONYM_FILENAME = "variation_synonym.txt";
    private static final String ALLELE_FILENAME = "allele.txt";
    private static final String ALLELE_CODE_FILENAME = "allele_code.txt.gz";
    private static final String GENOTYPE_CODE_FILENAME = "genotype_code.txt.gz";
    private static final String POPULATION_GENOTYPE_FILENAME = "population_genotype.txt";
    private static final String POPULATION_FILENAME = "population.txt.gz";
    private static final String PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME = "transcript_variation.includingVariationId.txt";
    private static final String PREPROCESSED_VARIATION_FEATURE_FILENAME = "variation_feature.sorted.txt";
    private static final String PREPROCESSED_VARIATION_SYNONYM_FILENAME = "variation_synonym.sorted.txt";
    private static final String PREPROCESSED_ALLELE_FILENAME = "allele.sorted.txt";
    private static final String PREPROCESSED_POPULATION_GENOTYPE_FILENAME = "population_genotype.sorted.txt";
    private static final int VARIATION_FEATURE_FILE_ID = 0;
    private static final int TRANSCRIPT_VARIATION_FILE_ID = 1;
    private static final int VARIATION_SYNONYM_FILE_ID = 2;
    private static final int ALLELE_FILE_ID = 3;
    private static final int POPULATION_GENOTYPE_FILE_ID = 4;

    private RandomAccessFile rafVariationFeature, rafTranscriptVariation, rafVariationSynonym;
    private Connection sqlConn = null;
    private PreparedStatement prepStmVariationFeature, prepStmTranscriptVariation, prepStmVariationSynonym;
    private Map<Integer, String> alleleCodeToAllele;
    private Map<Integer, List<Integer>> genotypeCodeToAlleleCode;
    private Map<Integer, String> requiredPopulations;

    private int LIMITROWS = 100000;
    private Path variationDirectoryPath;

    private int noAlleleFrequencies = 0;
    private int noGenotypeFrequencies = 0;
    private int biallelic = 0;
    private int nonDiploidGenotypes = 0;
    private int lastVariationSynonimId = -1;
    private int lastTranscriptId = -1;
    private int lastFeatureId = -1;
    private int lastAlleleVariationId = -1;
    private int lastGenotypeVariationId = -1;
    private BufferedReader variationSynonymsFileReader;
    private BufferedReader variationTranscriptsFileReader;
    private BufferedReader variationFeaturesFileReader;
    private BufferedReader alleleFileReader;
    private BufferedReader genotypeFileReader;
    private Boolean alleleEof = false;
    private Boolean genotypeEof = false;

    private String[] lastLineTranscriptVariationFields;
    private String lastFeatureLine;
    private String lastSynonymLine;
    private boolean featureEof = false;
    private String lastAlleleLine;
    private String lastGenotypeLine;

    private static final int VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FILE = 1;
    private static final int VARIATION_ID_COLUMN_INDEX_IN_VARIATION_SYNONYM_FILE = 1;
    private static final int VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE = 5;
    private static final int VARIATION_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE = 22;
    private static final int VARIATION_ID_COLUMN_INDEX_IN_ALLELE_FILE = 1;
    private static final int VARIATION_ID_COLUMN_INDEX_IN_POPULATION_GENOTYPE_FILE = 1;
    private static final int VARIATION_FEATURE_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE = 1;
    private int[] lastVariationIdInVariationRelatedFiles;
    private boolean[] endOfFileOfVariationRelatedFiles;
    private String[][] lastLineInVariationRelatedFile;
    private int[] variationIdColumnIndexInVariationRelatedFile;
    private BufferedReader[] variationRelatedFileReader;

    public VariationParser(Path variationDirectoryPath, CellBaseSerializer serializer) {
        super(serializer);
        this.variationDirectoryPath = variationDirectoryPath;
    }

    @Override
    public void parse() throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        if (!Files.exists(variationDirectoryPath) || !Files.isDirectory(variationDirectoryPath) || !Files.isReadable(variationDirectoryPath)) {
            throw new IOException("Variation directory whether does not exist, is not a directory or cannot be read");
        }

        Variation variation;

        // To speed up calculation a SQLite database is created with the IDs and file offsets,
        // file must be uncompressed for doing this.
        gunzipVariationInputFiles();

        // add idVariation to transcript_variation file
        preprocessInputFiles();

        // Open variation file, this file never gets uncompressed. It's read from gzip file
        BufferedReader bufferedReaderVariation = getVariationFileBufferedReader();

        // create buffered readers for all other input files
        createVariationFilesBufferedReaders(variationDirectoryPath);

        Map<String, String> seqRegionMap = VariationUtils.parseSeqRegionToMap(variationDirectoryPath);
        Map<String, String> sourceMap = VariationUtils.parseSourceToMap(variationDirectoryPath);

        initializeArrays();
        Stopwatch stopwatch = Stopwatch.createStarted();
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

                    // Preparing frequencies
                    List<PopulationFrequency> populationFrequencies = getPopulationFrequencies(variationId, allelesArray);

                    // TODO: check that variationFeatureFields is always different to null and intergenic-variant is never used
                    //List<String> consequenceTypes = (variationFeatureFields != null) ? Arrays.asList(variationFeatureFields[12].split(",")) : Arrays.asList("intergenic_variant");
                    List<String> consequenceTypes = Arrays.asList(variationFeatureFields[12].split(","));
                    String displayConsequenceType = getDisplayConsequenceType(consequenceTypes);

                    // For code sanity save chromosome in a variable
                    String chromosome = seqRegionMap.get(variationFeatureFields[1]);
                    // we have all the necessary to construct the 'variation' object
                    variation = buildVariation(variationFields, variationFeatureFields, chromosome, transcriptVariation, xrefs, populationFrequencies, allelesArray, consequenceTypes, displayConsequenceType);

                    if (++countprocess % 100000 == 0 && countprocess != 0) {
                        logger.info("Processed variations: " + countprocess);
                    }

                    serializer.serialize(variation);
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
        logger.debug("Elapsed time parsing: " + stopwatch);
        logger.info("Biallelic variations:\t" + biallelic);
        logger.info("Non-diploid genotypes:\t" + nonDiploidGenotypes);

        // TODO: commented during development, uncomment
        gzipVariationFiles(variationDirectoryPath);

        try {
            bufferedReaderVariation.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BufferedReader getVariationFileBufferedReader() throws IOException {
        Path inputFile;
        if (Files.exists(variationDirectoryPath.resolve(PREPROCESSED_VARIATION_FILENAME))) {
            inputFile = variationDirectoryPath.resolve(PREPROCESSED_VARIATION_FILENAME);
        } else {
            inputFile = variationDirectoryPath.resolve(PREPROCESSED_VARIATION_FILENAME + ".gz");
        }
        return FileUtils.newBufferedReader(inputFile);
    }

    private void preprocessInputFiles() throws IOException, InterruptedException {
        preprocessVariationFile();
        sortInputFile(VARIATION_FEATURE_FILENAME, PREPROCESSED_VARIATION_FEATURE_FILENAME, VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE);
        sortInputFile(VARIATION_SYNONYM_FILENAME, PREPROCESSED_VARIATION_SYNONYM_FILENAME, VARIATION_ID_COLUMN_INDEX_IN_VARIATION_SYNONYM_FILE);
        preprocessTranscriptVariationFile();
        preprocessAlleleCodeFile();
        preprocessGenotypeCodeFile();
        preprocessPopulationFile();
        sortInputFile(ALLELE_FILENAME, PREPROCESSED_ALLELE_FILENAME, VARIATION_ID_COLUMN_INDEX_IN_ALLELE_FILE);
        sortInputFile(POPULATION_GENOTYPE_FILENAME, PREPROCESSED_POPULATION_GENOTYPE_FILENAME, VARIATION_ID_COLUMN_INDEX_IN_POPULATION_GENOTYPE_FILE);
    }

    private void preprocessVariationFile() throws IOException, InterruptedException {
       if (!existsZippedOrUnzippedFile(PREPROCESSED_VARIATION_FILENAME)) {
            sortInputFile(VARIATION_FILENAME, PREPROCESSED_VARIATION_FILENAME, VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FILE);
        }
    }

    private void sortInputFile(String unsortedFileName, String sortedFileName, int columnToSortByIndex) throws IOException, InterruptedException {
        Path sortedFile = variationDirectoryPath.resolve(sortedFileName);
        if (!Files.exists(sortedFile)) {
            Path unsortedFile = variationDirectoryPath.resolve(unsortedFileName);
            sortFileByNumericColumn(unsortedFile, sortedFile, columnToSortByIndex);
        }
    }

    private void sortFileByNumericColumn(Path inputFile, Path outputFile, int columnIndex) throws InterruptedException, IOException {
        this.logger.info("Sorting file " + inputFile + " into " + outputFile + " ...");

        // increment column index by 1, beacause Java indexes are 0-based and 'sort' command uses 1-based indexes
        columnIndex++;
        ProcessBuilder pb = new ProcessBuilder("sort", "-t", "\t", "-k", Integer.toString(columnIndex), "-n", "--stable", inputFile.toAbsolutePath().toString(), "-T", System.getProperty("java.io.tmpdir"), "-o", outputFile.toAbsolutePath().toString());
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
        if (!Files.exists(preprocessedTranscriptVariationFile)) {
            this.logger.info("Preprocessing " + TRANSCRIPT_VARIATION_FILENAME + " file ...");
            Stopwatch stopwatch = Stopwatch.createStarted();

            // add variationId to transcript_variation file
            Map<Integer, Integer> variationFeatureToVariationId = createVariationFeatureIdToVariationIdMap();
            Path transcriptVariationTempFile = addVariationIdToTranscriptVariationFile(variationFeatureToVariationId);
            sortFileByNumericColumn(transcriptVariationTempFile, preprocessedTranscriptVariationFile, VARIATION_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE);

            this.logger.info("Removing temp file " + transcriptVariationTempFile);
            transcriptVariationTempFile.toFile().delete();
            this.logger.info("Removed");

            this.logger.info(TRANSCRIPT_VARIATION_FILENAME + " preprocessed. New file " +
                    PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME + " including (and sorted by) variation Id has been created");
            this.logger.debug("Elapsed time preprocessing transcript variation file: " + stopwatch);
        }
    }

    private void preprocessAlleleCodeFile() throws IOException, InterruptedException {
        this.logger.info("Preprocessing " + ALLELE_CODE_FILENAME + " file ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        alleleCodeToAllele = createAlleleCodeToAlleleMap();
        this.logger.info(ALLELE_CODE_FILENAME + " preprocessed.");
        this.logger.debug("Elapsed time preprocessing allele_code file: " + stopwatch);
    }

    private void preprocessGenotypeCodeFile() throws IOException, InterruptedException {
        this.logger.info("Preprocessing " + GENOTYPE_CODE_FILENAME + " file ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        genotypeCodeToAlleleCode = createGenotypeCodeToAlleleCodeMap();
        this.logger.info(GENOTYPE_CODE_FILENAME + " preprocessed.");
        this.logger.debug("Elapsed time preprocessing genotype_code file: " + stopwatch);
    }

    private void preprocessPopulationFile() throws IOException, InterruptedException {
        this.logger.info("Preprocessing " + POPULATION_FILENAME + " file ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        requiredPopulations = createPopulationMap();
        this.logger.info(POPULATION_FILENAME + " preprocessed.");
        this.logger.debug("Elapsed time preprocessing population file: " + stopwatch);
    }

    private Path addVariationIdToTranscriptVariationFile(Map<Integer, Integer> variationFeatureToVariationId) throws IOException {
        Path transcriptVariationTempFile = variationDirectoryPath.resolve(TRANSCRIPT_VARIATION_FILENAME + ".tmp");
        this.logger.info("Adding variation Id to transcript variations and saving them into " + transcriptVariationTempFile + " ...");
        Stopwatch stopwatch = Stopwatch.createStarted();

        Path unpreprocessedTranscriptVariationFile = variationDirectoryPath.resolve(TRANSCRIPT_VARIATION_FILENAME);
        BufferedReader br = FileUtils.newBufferedReader(unpreprocessedTranscriptVariationFile);
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

        BufferedReader variationFeatFileReader = FileUtils.newBufferedReader(variationDirectoryPath.resolve(VARIATION_FEATURE_FILENAME), Charset.defaultCharset());

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

    private Map<Integer, String> createAlleleCodeToAlleleMap() throws IOException {
        this.logger.info("Creating allele_code_id to allele map ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<Integer, String> alleleCodeToAllele = new HashMap<>();

        BufferedReader alleleCodeFileReader = FileUtils.newBufferedReader(variationDirectoryPath.resolve(ALLELE_CODE_FILENAME), Charset.defaultCharset());

        String line;
        while ((line = alleleCodeFileReader.readLine()) != null) {
            String[] fields = line.split("\t");
            alleleCodeToAllele.put(Integer.valueOf(fields[0]), fields[1]);
        }

        alleleCodeFileReader.close();

        this.logger.info("Done");
        this.logger.debug("Elapsed time creating allele_code_id to allele map: " + stopwatch);

        return alleleCodeToAllele;
    }

    private Map<Integer, List<Integer>> createGenotypeCodeToAlleleCodeMap() throws IOException {
        this.logger.info("Creating genotype_code_id to allele_code map ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<Integer, List<Integer>> genotypeCodeToAlleleCode = new HashMap<>();

        BufferedReader genotypeCodeFileReader = FileUtils.newBufferedReader(variationDirectoryPath.resolve(GENOTYPE_CODE_FILENAME), Charset.defaultCharset());

        String line;
        while ((line = genotypeCodeFileReader.readLine()) != null) {
            String[] fields = line.split("\t");
            Integer genotypeCode = Integer.valueOf(fields[0]);
            List<Integer> alleleCode;
            if((alleleCode = genotypeCodeToAlleleCode.get(genotypeCode)) == null) {
                alleleCode = new ArrayList<Integer>(2);
                alleleCode.add(Integer.valueOf(fields[1]));
                genotypeCodeToAlleleCode.put(genotypeCode, alleleCode);
            } else {
                alleleCode.add(Integer.valueOf(fields[1]));
            }
        }
        genotypeCodeFileReader.close();

        this.logger.info("Done");
        this.logger.debug("Elapsed time creating genotype_code to allele_code map: " + stopwatch);

        return genotypeCodeToAlleleCode;
    }

    private Map<Integer, String> createPopulationMap() throws IOException {
        this.logger.info("Creating population map ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<Integer, String> populationMap = new HashMap<>();

        BufferedReader populationFileReader = FileUtils.newBufferedReader(variationDirectoryPath.resolve(POPULATION_FILENAME), Charset.defaultCharset());

        String line;
        while (((line = populationFileReader.readLine()) != null) && (POPULATIONS_TO_INCLUDE.size()>populationMap.size())){
            String[] fields = line.split("\t");
            Integer populationId = Integer.valueOf(fields[0]);
            if(POPULATIONS_TO_INCLUDE.contains(fields[1])) {
                populationMap.put(populationId, fields[1]);
            }
        }

        populationFileReader.close();

        this.logger.info("Done");
        this.logger.debug("Elapsed time creating population map: " + stopwatch);

        return populationMap;
    }

    private void createVariationFilesBufferedReaders(Path variationDirectoryPath) throws IOException {
        variationFeaturesFileReader = FileUtils.newBufferedReader(variationDirectoryPath.resolve(PREPROCESSED_VARIATION_FEATURE_FILENAME), Charset.defaultCharset());
        variationSynonymsFileReader = FileUtils.newBufferedReader(variationDirectoryPath.resolve(PREPROCESSED_VARIATION_SYNONYM_FILENAME), Charset.defaultCharset());
        variationTranscriptsFileReader = FileUtils.newBufferedReader(variationDirectoryPath.resolve(PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME), Charset.defaultCharset());
        alleleFileReader = FileUtils.newBufferedReader(variationDirectoryPath.resolve(PREPROCESSED_ALLELE_FILENAME), Charset.defaultCharset());
        genotypeFileReader = FileUtils.newBufferedReader(variationDirectoryPath.resolve(PREPROCESSED_POPULATION_GENOTYPE_FILENAME), Charset.defaultCharset());
    }

    private int getVariationIdFromFeatureLine(String featureLine) {
        int variationId = Integer.parseInt(featureLine.split("\t")[VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE]);
        return variationId;
    }

    private Variation buildVariation(String[] variationFields, String[] variationFeatureFields, String chromosome,
                                     List<TranscriptVariation> transcriptVariation, List<Xref> xrefs,
                                     List<PopulationFrequency> populationFrequencies, String[] allelesArray,
                                     List<String> consequenceTypes, String displayConsequenceType) {
        Variation variation;
        variation = new Variation((variationFields[2] != null && !variationFields[2].equals("\\N")) ? variationFields[2] : "",
                chromosome, "SNV",
                (variationFeatureFields != null) ? Integer.parseInt(variationFeatureFields[2]) : 0,
                (variationFeatureFields != null) ? Integer.parseInt(variationFeatureFields[3]) : 0,
                variationFeatureFields[4], // strand
                (allelesArray[0] != null && !allelesArray[0].equals("\\N")) ? allelesArray[0] : "",
                (allelesArray[1] != null && !allelesArray[1].equals("\\N")) ? allelesArray[1] : "",
                variationFeatureFields[6],
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

    // TODO: change name
    private void initializeArrays() {
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
        variationRelatedFileReader[ALLELE_FILE_ID] = alleleFileReader;
        variationRelatedFileReader[POPULATION_GENOTYPE_FILE_ID] = genotypeFileReader;
        variationIdColumnIndexInVariationRelatedFile = new int[5];
        variationIdColumnIndexInVariationRelatedFile[VARIATION_FEATURE_FILE_ID] = VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE;
        variationIdColumnIndexInVariationRelatedFile[TRANSCRIPT_VARIATION_FILE_ID] = VARIATION_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE;
        variationIdColumnIndexInVariationRelatedFile[VARIATION_SYNONYM_FILE_ID] = VARIATION_ID_COLUMN_INDEX_IN_VARIATION_SYNONYM_FILE;
        variationIdColumnIndexInVariationRelatedFile[ALLELE_FILE_ID] = VARIATION_ID_COLUMN_INDEX_IN_ALLELE_FILE;
        variationIdColumnIndexInVariationRelatedFile[POPULATION_GENOTYPE_FILE_ID] = VARIATION_ID_COLUMN_INDEX_IN_POPULATION_GENOTYPE_FILE;

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

    private int getVariationIdFromAlleleLine(String alleleLine) {
        int variationId = Integer.parseInt(alleleLine.split("\t")[VARIATION_ID_COLUMN_INDEX_IN_ALLELE_FILE]);
        return variationId;
    }

    private int getVariationIdFromGenotypeLine(String alleleLine) {
        int variationId = Integer.parseInt(alleleLine.split("\t")[VARIATION_ID_COLUMN_INDEX_IN_POPULATION_GENOTYPE_FILE]);
        return variationId;
    }

    private List<PopulationFrequency> getPopulationFrequencies(int variationId, String[] allelesArray) throws IOException {
//        Map<Integer, PopulationFrequency> populationFrequencies = new HashMap<>();
//        getAlleleFrequencies(variationId, allelesArray, populationFrequencies);
//       getGenotypeFrequencies(variationId, allelesArray, populationFrequencies);
        // TODO: uncomment next lines and remove lines before this
        Map<Integer, PopulationFrequency> populationFrequencies = getAlleleFrequenciesAlt(variationId, allelesArray);
        populationFrequencies = getGenotypeFrequenciesAlt(variationId, allelesArray, populationFrequencies);

        return new ArrayList<>(populationFrequencies.values());
    }

    private Map<Integer, PopulationFrequency> getAlleleFrequenciesAlt(int variationId, String[] allelesArray) throws IOException {
        Map<Integer, PopulationFrequency> populationFrequencies = new HashMap<>();

        List<String[]> alleleLines = getVariationRelatedFields(ALLELE_FILE_ID, variationId);
        for (String[] alleleFields : alleleLines) {
            if (!alleleFields[4].equals("\\N") && !alleleFields[3].equals("\\N")) {  // Check population_id & allele_code_id != NULL, respectively
                Integer populationCode = Integer.valueOf(alleleFields[4]);
                if (requiredPopulations.containsKey(populationCode)) {  // Population in this record is within the list of required populations
                    addAllelePopulationFrequency(allelesArray, populationFrequencies, alleleFields, populationCode);
                }
            }
        }
        return populationFrequencies;
    }

    private void addAllelePopulationFrequency(String[] allelesArray, Map<Integer, PopulationFrequency> populationFrequencies, String[] alleleFields, Integer populationCode) {
        Integer alleleCode = Integer.valueOf(alleleFields[3]);

        if (isBiallelic(allelesArray, alleleCode)) {
            biallelic++;
        } else {
            float alleleFrequency = Float.valueOf(alleleFields[5]);
            PopulationFrequency populationFrequency = populationFrequencies.get(populationCode);
            if (populationFrequency == null) {  // Check if a PopulationFrequency object is already present for this population
                populationFrequency = new PopulationFrequency(requiredPopulations.get(populationCode),
                        SUPER_POPULATION.get(requiredPopulations.get(populationCode)), allelesArray[0], allelesArray[1]);
                populationFrequencies.put(populationCode, populationFrequency);
            }
            if (isReferenceAllele(allelesArray, alleleCode)) {
                populationFrequency.setRefAlleleFreq(alleleFrequency);
            } else {
                populationFrequency.setAltAlleleFreq(alleleFrequency);
            }
        }
    }

    private boolean isReferenceAllele(String[] allelesArray, Integer alleleCode) {
        return alleleCodeToAllele.get(alleleCode).equals(allelesArray[0]);
    }

    private boolean isBiallelic(String[] allelesArray, Integer alleleCode) {
        return !alleleCodeToAllele.get(alleleCode).equals(allelesArray[0]) && !alleleCodeToAllele.get(alleleCode).equals(allelesArray[1]);
    }

    private void getAlleleFrequencies(Integer variationId, String[] allelesArray, Map<Integer,PopulationFrequency> populationFrequencies) throws IOException {

        float refAlleleFreq = 0;
        float altAlleleFreq = 0;
        PopulationFrequency populationFrequency;

        while (!alleleEof && (lastAlleleVariationId == -1 || lastAlleleVariationId < variationId)) {
            if((lastAlleleLine = alleleFileReader.readLine())==null) {
                alleleEof = true;
            } else {
                lastAlleleVariationId = getVariationIdFromAlleleLine(lastAlleleLine);
            }
        }
        if ((lastAlleleVariationId>variationId) || alleleEof) {
            noAlleleFrequencies++;
        } else {
            while (!alleleEof && (lastAlleleVariationId == variationId)) {
                String[] fields = lastAlleleLine.split("\t");
                if(!fields[4].equals("\\N") && !fields[3].equals("\\N")) {  // Check population_id & allele_code_id != NULL, respectively
                    Integer populationCode = Integer.valueOf(fields[4]);
                    if(requiredPopulations.containsKey(populationCode)) {  // Population in this record is within the list of required populations
                        Integer alleleCode = Integer.valueOf(fields[3]);
                        if (alleleCodeToAllele.get(alleleCode).equals(allelesArray[0])) {  // This is reference allele
                            refAlleleFreq = Float.valueOf(fields[5]);
                            if ((populationFrequency = populationFrequencies.get(populationCode)) == null) {  // Check if a PopulationFrequency object is already present for this population
                                populationFrequency = new PopulationFrequency(requiredPopulations.get(populationCode),
                                        SUPER_POPULATION.get(requiredPopulations.get(populationCode)), allelesArray[0], allelesArray[1]);
                                populationFrequencies.put(populationCode, populationFrequency);
                            }
                            populationFrequency.setRefAlleleFreq(refAlleleFreq);
                        } else if (alleleCodeToAllele.get(alleleCode).equals(allelesArray[1])) {  // This is the alternative allele
                            altAlleleFreq = Float.valueOf(fields[5]);
                            if ((populationFrequency = populationFrequencies.get(populationCode)) == null) {  // Check if a PopulationFrequency object is already present for this population
                                populationFrequency = new PopulationFrequency(requiredPopulations.get(populationCode),
                                        SUPER_POPULATION.get(requiredPopulations.get(populationCode)), allelesArray[0], allelesArray[1]);
                                populationFrequencies.put(populationCode, populationFrequency);
                            }
                            populationFrequency.setAltAlleleFreq(altAlleleFreq);
                        } else {
//                            this.logger.info("WARNING: allele found which is neither the reference nor the alternative");
//                            this.logger.info(" Ref: " + allelesArray[0] + ", Alt: " + allelesArray[1] + ", allele code: " + alleleCode +
//                                    ", allele string: " + alleleCodeToAllele.get(alleleCode) + ", variationId: "+variationId);
                            biallelic++;
                        }
                    }
                }
                if((lastAlleleLine = alleleFileReader.readLine())==null) {
                    alleleEof = true;
                } else {
                    lastAlleleVariationId = getVariationIdFromAlleleLine(lastAlleleLine);
                }
            }
        }
    }

    private Map<Integer, PopulationFrequency> getGenotypeFrequenciesAlt(Integer variationId, String[] allelesArray, Map<Integer, PopulationFrequency> populationFrequencies) throws IOException {
        List<String[]> genotypeLines = getVariationRelatedFields(POPULATION_GENOTYPE_FILE_ID, variationId);
        List<String> genotypeAlleles = new ArrayList<>(2);

        for (String[] genotypeFields : genotypeLines) {
            if(!genotypeFields[5].equals("\\N") && !genotypeFields[3].equals("\\N")) {  // Check population_id & genotype_code_id != NULL, respectively
                Integer populationCode = Integer.valueOf(genotypeFields[5]);
                if(requiredPopulations.containsKey(populationCode)) {  // Population in this record is within the list of required populations
                    addGenotypePopulationFrequency(allelesArray, populationFrequencies, genotypeAlleles, genotypeFields, populationCode);
                }
            }
        }
        return populationFrequencies;
    }

    private void addGenotypePopulationFrequency(String[] allelesArray, Map<Integer, PopulationFrequency> populationFrequencies, List<String> genotypeAlleles, String[] genotypeFields, Integer populationCode) {
        Integer genotypeCode = Integer.valueOf(genotypeFields[3]);
        List<Integer> genotypeCodes = genotypeCodeToAlleleCode.get(genotypeCode);
        if(genotypeCodes.size()==2) {
            genotypeAlleles.clear();
            for (Integer alleleCode : genotypeCodes) {
                genotypeAlleles.add(alleleCodeToAllele.get(alleleCode));
            }
            float genotypeFrequency = Float.valueOf(genotypeFields[4]);
            PopulationFrequency populationFrequency = getPopulationFrequency(genotypeFields, allelesArray, populationCode, populationFrequencies);
            if (heterozygousGenotype(genotypeAlleles, allelesArray)) {
                populationFrequency.setHetGenotypeFreq(genotypeFrequency);
            } else if (homozygousForReferenceGenotype(genotypeAlleles, allelesArray)) {
                populationFrequency.setRefHomGenotypeFreq(genotypeFrequency);
            } else if (homozygousForAlternativeGenotype(genotypeAlleles, allelesArray)) {
                populationFrequency.setAltHomGenotypeFreq(genotypeFrequency);
            }
        } else {
            nonDiploidGenotypes++;
        }
    }

    private boolean heterozygousGenotype(List<String> genotypeAlleles, String[] allelesArray) {
        return genotypeAlleles.contains(allelesArray[0]) && genotypeAlleles.contains(allelesArray[1]);
    }

    private boolean homozygousForReferenceGenotype(List<String> genotypeAlleles, String[] allelesArray) {
        return genotypeAlleles.get(0).equals(genotypeAlleles.get(1)) && (genotypeAlleles.get(0).equals(allelesArray[0]));
    }

    private boolean homozygousForAlternativeGenotype(List<String> genotypeAlleles, String[] allelesArray) {
        return genotypeAlleles.get(0).equals(genotypeAlleles.get(1)) && (genotypeAlleles.get(0).equals(allelesArray[1]));
    }

    private PopulationFrequency getPopulationFrequency(String[] genotypeFields, String[] allelesArray, Integer populationCode, Map<Integer,PopulationFrequency> populationFrequencies) {
        PopulationFrequency populationFrequency = populationFrequencies.get(populationCode);
        if (populationFrequency == null) {  // Check if a PopulationFrequency object is already present for this population
            populationFrequency = new PopulationFrequency(requiredPopulations.get(populationCode),
                    SUPER_POPULATION.get(requiredPopulations.get(populationCode)), allelesArray[0], allelesArray[1]);
            populationFrequencies.put(populationCode, populationFrequency);
        }
        return populationFrequency;
    }

    private void getGenotypeFrequencies(Integer variationId, String[] allelesArray, Map<Integer,PopulationFrequency> populationFrequencies) throws IOException {

        float hetGenotypeFreq = 0;
        float homRefGenotypeFreq = 0;
        float homAltGenotypeFreq = 0;
        PopulationFrequency populationFrequency;

        if(variationId==6455874) {
            int a;
            a=1;
        }
        while (!genotypeEof && (lastGenotypeVariationId == -1 || lastGenotypeVariationId < variationId)) {
            if((lastGenotypeLine = genotypeFileReader.readLine())==null) {
                genotypeEof = true;
            } else {
                lastGenotypeVariationId = getVariationIdFromGenotypeLine(lastGenotypeLine);
            }
        }
        if ((lastGenotypeVariationId>variationId) || genotypeEof){
            noGenotypeFrequencies++;
        } else {
            List<String> genotypeAlleles = new ArrayList<>(2);
            while (!genotypeEof && (lastGenotypeVariationId == variationId)) {
                String[] fields = lastGenotypeLine.split("\t");
                if(!fields[5].equals("\\N") && !fields[3].equals("\\N")) {  // Check population_id & genotype_code_id != NULL, respectively
                    Integer populationCode = Integer.valueOf(fields[5]);
                    if(requiredPopulations.containsKey(populationCode)) {  // Population in this record is within the list of required populations
                        Integer genotypeCode = Integer.valueOf(fields[3]);
                        List<Integer> genotypeCodes = genotypeCodeToAlleleCode.get(genotypeCode);
                        if(genotypeCodes.size()==2) {
                            genotypeAlleles.clear();
                            for (Integer alleleCode : genotypeCodes) {
                                genotypeAlleles.add(alleleCodeToAllele.get(alleleCode));
                            }
                            if (genotypeAlleles.contains(allelesArray[0]) && genotypeAlleles.contains(allelesArray[1])) {  // Heterozygous genotype
                                hetGenotypeFreq = Float.valueOf(fields[4]);
                                if ((populationFrequency = populationFrequencies.get(populationCode)) == null) {  // Check if a PopulationFrequency object is already present for this population
                                    populationFrequency = new PopulationFrequency(requiredPopulations.get(populationCode),
                                            SUPER_POPULATION.get(requiredPopulations.get(populationCode)), allelesArray[0], allelesArray[1]);
                                    populationFrequencies.put(populationCode, populationFrequency);
                                }
                                populationFrequency.setHetGenotypeFreq(hetGenotypeFreq);
                            } else if (genotypeAlleles.get(0).equals(genotypeAlleles.get(1)) && (genotypeAlleles.get(0).equals(allelesArray[0]))) {  // Homozygous for the reference
                                homRefGenotypeFreq = Float.valueOf(fields[4]);
                                if ((populationFrequency = populationFrequencies.get(populationCode)) == null) {  // Check if a PopulationFrequency object is already present for this population
                                    populationFrequency = new PopulationFrequency(requiredPopulations.get(populationCode),
                                            SUPER_POPULATION.get(requiredPopulations.get(populationCode)), allelesArray[0], allelesArray[1]);
                                    populationFrequencies.put(populationCode, populationFrequency);
                                }
                                populationFrequency.setRefHomGenotypeFreq(homRefGenotypeFreq);
                            } else if (genotypeAlleles.get(0).equals(genotypeAlleles.get(1)) && (genotypeAlleles.get(0).equals(allelesArray[1]))) {  // Homozygous for the alternative
                                homAltGenotypeFreq = Float.valueOf(fields[4]);
                                if ((populationFrequency = populationFrequencies.get(populationCode)) == null) {  // Check if a PopulationFrequency object is already present for this population
                                    populationFrequency = new PopulationFrequency(requiredPopulations.get(populationCode),
                                            SUPER_POPULATION.get(requiredPopulations.get(populationCode)), allelesArray[0], allelesArray[1]);
                                    populationFrequencies.put(populationCode, populationFrequency);
                                }
                                populationFrequency.setAltHomGenotypeFreq(homAltGenotypeFreq);
                            }
//                            } else {
//                                this.logger.info("WARNING: genotype found with alleles that do not match neither the reference nor the alternative");
//                                this.logger.info(" Ref: " + allelesArray[0] + ", Alt: " + allelesArray[1] + ", genotype code: " + genotypeCode +
//                                        ", genotype alleles: " + genotypeAlleles.toString() + ", variationId: " + variationId);
//                            }
                        } else {
//                            this.logger.info("WARNING: genotype found with (just one)/(more than two) allele(s)");
//                            this.logger.info(" Ref: " + allelesArray[0] + ", Alt: " + allelesArray[1] + ", genotype code: " + genotypeCode +
//                                                ", variationId: " + variationId);
                            nonDiploidGenotypes++;
                        }
                    }
                }
                if((lastGenotypeLine = genotypeFileReader.readLine())==null) {
                    genotypeEof = true;
                } else {
                    lastGenotypeVariationId = getVariationIdFromGenotypeLine(lastGenotypeLine);
                }
            }
        }
    }

    private boolean homozygousReferenceGenotype(String anObject, boolean b) {
        return b;
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
        if (existsZippedOrUnzippedFile(PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME)) {
            gunzipFileIfNeeded(variationDirectoryPath, PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME);
        } else {
            gunzipFileIfNeeded(variationDirectoryPath, TRANSCRIPT_VARIATION_FILENAME);
        }
        if (existsZippedOrUnzippedFile(PREPROCESSED_VARIATION_FEATURE_FILENAME)) {
            gunzipFileIfNeeded(variationDirectoryPath, PREPROCESSED_VARIATION_FEATURE_FILENAME);
        } else {
            gunzipFileIfNeeded(variationDirectoryPath, VARIATION_FEATURE_FILENAME);
        }
        if (existsZippedOrUnzippedFile(PREPROCESSED_VARIATION_SYNONYM_FILENAME)) {
            gunzipFileIfNeeded(variationDirectoryPath, PREPROCESSED_VARIATION_SYNONYM_FILENAME);
        } else {
            gunzipFileIfNeeded(variationDirectoryPath, VARIATION_SYNONYM_FILENAME);
        }
        if (existsZippedOrUnzippedFile(PREPROCESSED_ALLELE_FILENAME)) {
            gunzipFileIfNeeded(variationDirectoryPath, PREPROCESSED_ALLELE_FILENAME);
        } else {
            gunzipFileIfNeeded(variationDirectoryPath, ALLELE_FILENAME);
        }
        if (existsZippedOrUnzippedFile(PREPROCESSED_POPULATION_GENOTYPE_FILENAME)) {
            gunzipFileIfNeeded(variationDirectoryPath, POPULATION_GENOTYPE_FILENAME);
        } else {
            gunzipFileIfNeeded(variationDirectoryPath, POPULATION_GENOTYPE_FILENAME);
        }

        logger.info("Done");
        logger.debug("Elapsed time unzipping files: " + stopwatch);
    }

    private boolean existsZippedOrUnzippedFile(String baseFilename) {
        return Files.exists(variationDirectoryPath.resolve(baseFilename)) ||
                Files.exists(variationDirectoryPath.resolve(baseFilename + ".gz"));
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

}
