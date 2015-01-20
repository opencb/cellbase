package org.opencb.cellbase.build.transform;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.IOUtils;
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

    private static final String VARIATION_FILENAME = "variation.txt.gz";
    private static final String VARIATION_FEATURE_FILENAME = "variation_feature.txt";
    private static final String TRANSCRIPT_VARIATION_FILENAME = "transcript_variation.txt";
    private static final String VARIATION_SYNONYM_FILENAME = "variation_synonym.txt";
    private static final String PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME = "transcript_variation.includingVariationId.txt";
    private static final String PREPROCESSED_VARIATION_FEATURE_FILENAME = "variation_feature.sorted.txt";
    private static final String PREPROCESSED_VARIATION_SYNONYM_FILENAME = "variation_synonym.sorted.txt";

    private RandomAccessFile rafVariationFeature, rafTranscriptVariation, rafVariationSynonym;
    private Connection sqlConn = null;
    private PreparedStatement prepStmVariationFeature, prepStmTranscriptVariation, prepStmVariationSynonym;

    private int LIMITROWS = 100000;
    private Path variationDirectoryPath;

    private int noFeatureVariations = 0;
    private int noSynonimVariations = 0;
    private int noTranscriptVariations = 0;

    private int lastSynonymId = -1;
    private int lastTranscriptId = -1;
    private int lastFeatureId = -1;
    private BufferedReader variationSynonymsFileReader;
    private BufferedReader variationTranscriptsFileReader;
    private BufferedReader variationFeaturesFileReader;
    private String[] lastLineTranscriptVariationFields;
    private String lastFeatureLine;
    private String lasSynonymLine;
    private boolean featureEof = false;

    private static final int VARIATION_ID_COLUMN_INDEX_IN_VARIATION_SYNONYM_FILE = 1;
    private static final int VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE = 5;
    private static final int VARIATION_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE = 22;
    private static final int VARIATION_FEATURE_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE = 1;

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

        // Open variation file, this file never gets uncompressed. It's read from gzip file
        BufferedReader bufferedReaderVariation = FileUtils.newBufferedReader(variationDirectoryPath.resolve(VARIATION_FILENAME));

        // To speed up calculation a SQLite database is created with the IDs and file offsets,
        // file must be uncompressed for doing this.
        gunzipVariationInputFiles();

        // add idVariation to transcript_variation file
        preprocessInputFiles();

        // TODO: remove this line and 'connect' method
        // Prepares connections to database to resolve queries by ID.
        // This version combines a SQLite database with the file offsets with a RandomAccessFile to access data using offsets.
        //connect(variationDirectoryPath);
        createVariationFilesBufferedReaders(variationDirectoryPath);

        Map<String, String> seqRegionMap = VariationUtils.parseSeqRegionToMap(variationDirectoryPath);
        Map<String, String> sourceMap = VariationUtils.parseSourceToMap(variationDirectoryPath);

        Stopwatch stopwatch = Stopwatch.createStarted();
        logger.info("Parsing variation file " + variationDirectoryPath.resolve(VARIATION_FILENAME) + " ...");
        long countprocess = 0;
        String line;
        while ((line = bufferedReaderVariation.readLine()) != null) {
            String[] variationFields = line.split("\t");

            int variationId = Integer.parseInt(variationFields[0]);
            // TODO: testing, remove the unused line and methods
            //List<String> resultVariationFeature = queryByVariationId(variationId, "variation_feature");
            List<String> resultVariationFeature = getVariationFeatures(variationId);

            if (resultVariationFeature != null && resultVariationFeature.size() > 0) {
                String[] variationFeatureFields = resultVariationFeature.get(0).split("\t", -1);

                // TODO: testing, remove the unused line and methods
                //List<TranscriptVariation> transcriptVariation = getTranscriptVariationsFromSqlLite(variationFeatureFields[0]);
                List<TranscriptVariation> transcriptVariation = getTranscriptVariations(variationId, variationFeatureFields[0]);
                List<Xref> xrefs = getXrefs(sourceMap, variationId);

                try {
                    // Preparing the variation alleles
                    String[] allelesArray = getAllelesArray(variationFeatureFields);

                    // TODO: check that variationFeatureFields is always different to null and intergenic-variant is never used
                    //List<String> consequenceTypes = (variationFeatureFields != null) ? Arrays.asList(variationFeatureFields[12].split(",")) : Arrays.asList("intergenic_variant");
                    List<String> consequenceTypes = Arrays.asList(variationFeatureFields[12].split(","));
                    String displayConsequenceType = getDisplayConsequenceType(consequenceTypes);

                    // For code sanity save chromosome in a variable
                    String chromosome = seqRegionMap.get(variationFeatureFields[1]);
                    // we have all the necessary to construct the 'variation' object
                    variation = buildVariation(variationFields, variationFeatureFields, chromosome, transcriptVariation, xrefs, allelesArray, consequenceTypes, displayConsequenceType);

                    if (++countprocess % 10000 == 0 && countprocess != 0) {
                        logger.debug("Processed variations: " + countprocess);
                    }

                    serializer.serialize(variation);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error parsing variation: " + e.getMessage());
                    logger.error("Last line processed: " + line);
                }
            }
            // TODO: just for testing, remove
//            if (countprocess % 10000 == 0) {
//                break;
//            }
        }

        logger.info("Variation parsing finished");
        logger.debug("Elapsed time parsing: " + stopwatch);
        logger.debug("Variation transcript with no variation:\t" + noTranscriptVariations);
        logger.debug("Variation features with no variation:\t" + noFeatureVariations);
        logger.debug("Variation Synonims with no variation:\t" + noSynonimVariations);

        gzipVariationFiles(variationDirectoryPath);

        try {
            bufferedReaderVariation.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void preprocessInputFiles() throws IOException, InterruptedException {
        preprocessTranscriptVariationFile();
        sortFeatureVariationFile();
        sortSynonymVariationFile();
    }

    private void sortSynonymVariationFile() throws IOException, InterruptedException {
        Path sortedFeatureVariationFile = variationDirectoryPath.resolve(PREPROCESSED_VARIATION_SYNONYM_FILENAME);
        if (!Files.exists(sortedFeatureVariationFile)) {
            Path unsortedFeatureVariationFile = variationDirectoryPath.resolve(VARIATION_SYNONYM_FILENAME);
            sortFileByNumericColumn(unsortedFeatureVariationFile, sortedFeatureVariationFile, VARIATION_ID_COLUMN_INDEX_IN_VARIATION_SYNONYM_FILE);
        }
    }

    private void sortFeatureVariationFile() throws IOException, InterruptedException {
        Path sortedFeatureVariationFile = variationDirectoryPath.resolve(PREPROCESSED_VARIATION_FEATURE_FILENAME);
        if (!Files.exists(sortedFeatureVariationFile)) {
            Path unsortedFeatureVariationFile = variationDirectoryPath.resolve(VARIATION_FEATURE_FILENAME);
            sortFileByNumericColumn(unsortedFeatureVariationFile, sortedFeatureVariationFile, VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE);
        }
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

    private void sortFileByNumericColumn(Path inputFile, Path outputFile, int columnIndex) throws InterruptedException, IOException {
        this.logger.info("Sorting file " + inputFile + " into " + outputFile + " ...");

        // increment column index by 1, beacause Java indexes are 0-based and 'sort' command uses 1-based indexes
        columnIndex++;
        ProcessBuilder pb = new ProcessBuilder("sort", "-t", "\t", "-k", Integer.toString(columnIndex), "-n", "--stable", inputFile.toAbsolutePath().toString(), "-T", System.getProperty("java.io.tmpdir"), "-o", outputFile.toAbsolutePath().toString());
        //String sortCommand = "sort -t\"\t\" -k " + columnIndex + " -n " + inputFile.toAbsolutePath() +  " -T " + System.getProperty("java.io.tmpdir") + " -o " + outputFile.toAbsolutePath();
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

    private void createVariationFilesBufferedReaders(Path variationDirectoryPath) throws IOException {
        variationFeaturesFileReader = FileUtils.newBufferedReader(variationDirectoryPath.resolve(PREPROCESSED_VARIATION_FEATURE_FILENAME), Charset.defaultCharset());
        variationSynonymsFileReader = FileUtils.newBufferedReader(variationDirectoryPath.resolve(PREPROCESSED_VARIATION_SYNONYM_FILENAME), Charset.defaultCharset());
        variationTranscriptsFileReader = FileUtils.newBufferedReader(variationDirectoryPath.resolve(PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME), Charset.defaultCharset());
    }

    private List<String> getVariationFeatures(int variationId) throws IOException {
        List<String> variationFeatures;
        while (lastFeatureId == -1 || lastFeatureId < variationId) {
            if ((lastFeatureLine = variationFeaturesFileReader.readLine()) == null) {
                featureEof = true;
            } else {
                lastFeatureId = getVariationIdFromFeatureLine(lastFeatureLine);
            }
        }
        if (featureEof || lastFeatureId > variationId) {
            variationFeatures = Collections.EMPTY_LIST;
            noFeatureVariations++;
        } else {
            variationFeatures = new ArrayList<>();
            while (lastFeatureId == variationId) {
                variationFeatures.add(lastFeatureLine);
                if ((lastFeatureLine = variationFeaturesFileReader.readLine()) == null) {
                    featureEof = true;
                } else {
                    lastFeatureId = getVariationIdFromFeatureLine(lastFeatureLine);
                }
            }
        }
        // TODO: en lugar de devolver una lista de lineas completas, aprovechando que ya hacemos el split, devolver solo
        //       los campos que nos interesen
        return variationFeatures;
    }

    private int getVariationIdFromFeatureLine(String featureLine) {
        int variationId = Integer.parseInt(featureLine.split("\t")[VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE]);
        return variationId;
    }

    private Variation buildVariation(String[] variationFields, String[] variationFeatureFields, String chromosome, List<TranscriptVariation> transcriptVariation, List<Xref> xrefs, String[] allelesArray, List<String> consequenceTypes, String displayConsequenceType) {
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
                consequenceTypes, transcriptVariation, null, null, null, xrefs, /*"featureId",*/
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
        // TODO: testing, remove the unused line and methods
        //List<String> resultVariationSynonym = queryByVariationId(variationId, "variation_synonym");
        List<String> resultVariationSynonym = getVariationSynonyms(variationId);
                List<Xref> xrefs = new ArrayList<>();
        if (resultVariationSynonym != null && resultVariationSynonym.size() > 0) {
            String arr[];
            for (String rxref : resultVariationSynonym) {
                String[] variationSynonymFields = rxref.split("\t");
                if (sourceMap.get(variationSynonymFields[3]) != null) {
                    arr = sourceMap.get(variationSynonymFields[3]).split(",");
                    xrefs.add(new Xref(variationSynonymFields[4], arr[0], arr[1]));
                }
            }
        }
        return xrefs;
    }

    private List<String> getVariationSynonyms(int variationId) throws IOException {
        List<String> variationSynonyms;

        while (lastSynonymId == -1 || lastSynonymId < variationId) {
            lasSynonymLine = variationSynonymsFileReader.readLine();
            lastSynonymId = getVariationIdFromSynonymLine(lasSynonymLine);
        }
        if (lastSynonymId > variationId) {
            variationSynonyms = Collections.EMPTY_LIST;
            noSynonimVariations++;
        } else {
            variationSynonyms = new ArrayList<>();
            while (lastSynonymId == variationId) {
                variationSynonyms.add(lasSynonymLine);
                lasSynonymLine = variationSynonymsFileReader.readLine();
                lastSynonymId = getVariationIdFromSynonymLine(lasSynonymLine);
            }
        }
        // TODO: en lugar de devolver una lista de lineas completas, aprovechando que ya hacemos el split, devolver solo
        //       los campos que nos interesen
        return variationSynonyms;
    }

    private int getVariationIdFromSynonymLine(String synonymLine) {
        int variationId = Integer.parseInt(synonymLine.split("\t")[VARIATION_ID_COLUMN_INDEX_IN_VARIATION_SYNONYM_FILE]);
        return variationId;
    }

    @Deprecated
    private List<TranscriptVariation> getTranscriptVariationsFromSqlLite(String variationFeatureField) throws IOException, SQLException {
        // Note the ID used, TranscriptVariation references to VariationFeature no Variation !!!
        List<TranscriptVariation> transcriptVariation = new ArrayList<>();
        List<String> resultTranscriptVariations = queryByVariationId(Integer.parseInt(variationFeatureField), "transcript_variation");
        if (resultTranscriptVariations != null && resultTranscriptVariations.size() > 0) {
            for (String rtv : resultTranscriptVariations) {
                TranscriptVariation tv = buildTranscriptVariation(rtv);
                transcriptVariation.add(tv);
            }
        }
        return transcriptVariation;
    }

    @Deprecated
    private TranscriptVariation buildTranscriptVariation(String rtv) {
        String[] transcriptVariationFields = rtv.split("\t");

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

    private List<TranscriptVariation> getTranscriptVariations(int variationId, String variationFeatureId) throws IOException, SQLException {
        // Note the ID used, TranscriptVariation references to VariationFeature no Variation !!!
        List<TranscriptVariation> transcriptVariation = new ArrayList<>();
        List<String[]> resultTranscriptVariations = getVariationTranscripts(variationId, Integer.parseInt(variationFeatureId));
        if (resultTranscriptVariations != null && resultTranscriptVariations.size() > 0) {
            for (String[] transcriptVariationFields : resultTranscriptVariations) {
                TranscriptVariation tv = buildTranscriptVariation(transcriptVariationFields);
                transcriptVariation.add(tv);
            }
        }
        return transcriptVariation;
    }

    private List<String[]> getVariationTranscripts(int variationId, int variationFeatureId) throws IOException {
        List<String[]> variationTranscripts;

        while (lastTranscriptId == -1 || lastTranscriptId < variationId) {
            lastLineTranscriptVariationFields = variationTranscriptsFileReader.readLine().split("\t");
            lastTranscriptId = Integer.parseInt(lastLineTranscriptVariationFields[VARIATION_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE]);
        }
        if (lastTranscriptId > variationId) {
            variationTranscripts = Collections.EMPTY_LIST;
            noTranscriptVariations++;
        } else {
            variationTranscripts = new ArrayList<>();
            while (lastTranscriptId == variationId) {
                if (Integer.parseInt(lastLineTranscriptVariationFields[VARIATION_FEATURE_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE]) == variationFeatureId) {
                    variationTranscripts.add(lastLineTranscriptVariationFields);
                }
                lastLineTranscriptVariationFields = variationTranscriptsFileReader.readLine().split("\t");
                lastTranscriptId = Integer.parseInt(lastLineTranscriptVariationFields[VARIATION_ID_COLUMN_INDEX_IN_TRANSCRIPT_VARIATION_FILE]);
            }
        }

        return variationTranscripts;
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

    @Deprecated
    public void connect(Path variationDirectoryPath) throws SQLException, ClassNotFoundException, IOException {
        createSqlLiteConnection(variationDirectoryPath);
        createTables(variationDirectoryPath);
        prepareSelectStatements();
        prepareRandomAccessFiles(variationDirectoryPath);
    }

    @Deprecated
    private void createSqlLiteConnection(Path variationDirectoryPath) throws ClassNotFoundException, SQLException, IOException {
        logger.info("Creating SQLITE connection ...");
        Class.forName("org.sqlite.JDBC");
        sqlConn = DriverManager.getConnection("jdbc:sqlite:" + variationDirectoryPath.toAbsolutePath().toString() + "/variation_tables.db");
        logger.info("Done");
    }

    @Deprecated
    private void createTables(Path variationDirectoryPath) throws IOException, SQLException {
        if (!Files.exists(variationDirectoryPath.resolve("variation_tables.db")) || Files.size(variationDirectoryPath.resolve("variation_tables.db")) == 0) {
            logger.info("Creating tables ...");
            sqlConn.setAutoCommit(false);
            createTable(5, variationDirectoryPath.resolve(VARIATION_FEATURE_FILENAME), "variation_feature");
            createTable(1, variationDirectoryPath.resolve(TRANSCRIPT_VARIATION_FILENAME), "transcript_variation");
            createTable(1, variationDirectoryPath.resolve(VARIATION_SYNONYM_FILENAME), "variation_synonym");
            sqlConn.setAutoCommit(true);
            logger.info("Done");
        }
    }

    @Deprecated
    private void prepareSelectStatements() throws SQLException {
        prepStmVariationFeature = sqlConn.prepareStatement("select offset from variation_feature where variation_id = ? order by offset ASC ");
        prepStmTranscriptVariation = sqlConn.prepareStatement("select offset from transcript_variation where variation_id = ? order by offset ASC ");
        prepStmVariationSynonym = sqlConn.prepareStatement("select offset from variation_synonym where variation_id = ? order by offset ASC ");
    }

    @Deprecated
    private void prepareRandomAccessFiles(Path variationDirectoryPath) throws FileNotFoundException {
        rafVariationFeature = new RandomAccessFile(variationDirectoryPath.resolve(VARIATION_FEATURE_FILENAME).toFile(), "r");
        rafTranscriptVariation = new RandomAccessFile(variationDirectoryPath.resolve(TRANSCRIPT_VARIATION_FILENAME).toFile(), "r");
        rafVariationSynonym = new RandomAccessFile(variationDirectoryPath.resolve(VARIATION_SYNONYM_FILENAME).toFile(), "r");
    }

    // TODO: remove if sqllite version is discarded
//    public void disconnect() {
//        super.disconnect();
//        closeSqlLiteObjects();
//        closeRandomAccessFile(rafVariationFeature);
//        closeRandomAccessFile(rafTranscriptVariation);
//        closeRandomAccessFile(rafVariationSynonym);
//    }

    @Deprecated
    private void closeSqlLiteObjects() {
        try {
            if (sqlConn != null && !sqlConn.isClosed()) {
                prepStmVariationFeature.close();
                prepStmTranscriptVariation.close();
                prepStmVariationSynonym.close();

                sqlConn.close();
            }
        } catch (SQLException e) {
            logger.error("Error closing prepared statemen: " + e.getMessage());
        }
    }

    @Deprecated
    private void closeRandomAccessFile(RandomAccessFile file) {
        try {
            file.close();
        } catch (IOException e) {
            logger.error("Error closing file : " + e.getMessage());
        }
    }

    @Deprecated
    public List<String> queryByVariationId(int variationId, String tableName) throws IOException, SQLException {
        List<Long> offsets;
        List<String> variations = null;
        switch (tableName) {
            case "variation_feature":
                offsets = getOffsetForVariationRandomAccessFile(prepStmVariationFeature, variationId);
                variations = getVariationsFromRandomAccesssFile(offsets, rafVariationFeature);
                if (variations.isEmpty()) noFeatureVariations++;
                break;
            case "transcript_variation":
                offsets = getOffsetForVariationRandomAccessFile(prepStmTranscriptVariation, variationId);
                variations = getVariationsFromRandomAccesssFile(offsets, rafTranscriptVariation);
                if (variations.isEmpty()) noTranscriptVariations++;
                break;
            case "variation_synonym":
                offsets = getOffsetForVariationRandomAccessFile(prepStmVariationSynonym, variationId);
                variations = getVariationsFromRandomAccesssFile(offsets, rafVariationSynonym);
                if (variations.isEmpty()) noSynonimVariations++;
                break;
        }
        return variations;
    }

    @Deprecated
    private List<Long> getOffsetForVariationRandomAccessFile(PreparedStatement statement, int variationId) throws SQLException {
        statement.setInt(1, variationId);
        ResultSet rs = statement.executeQuery();

        List<Long> offsets = null;

        while (rs.next()) {
            if (offsets == null) {
                offsets = new ArrayList<>();
            }
            offsets.add(rs.getLong(1));
        }
        if (offsets != null) {
            Collections.sort(offsets);
        } else {
            offsets = Collections.EMPTY_LIST;
        }

        return offsets;
    }

    @Deprecated
    private List<String> getVariationsFromRandomAccesssFile(List<Long> offsets, RandomAccessFile raf) throws IOException {
        List<String> results;

        if (!offsets.isEmpty()) {
            results = new ArrayList<>();

            for (Long offset : offsets) {
                if (offset >= 0) {
                    raf.seek(offset);
                    String line = raf.readLine();
                    if (line != null) {
                        results.add(line);
                    }
                }
            }
        } else {
            results = Collections.EMPTY_LIST;
        }

        return results;
    }

    @Deprecated
    private void createTable(int columnIndex, Path variationFilePath, String tableName) throws SQLException, IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Statement createTables = sqlConn.createStatement();

        // A table containing offset for files
        createTables.executeUpdate("CREATE TABLE if not exists " + tableName + "(" + "variation_id INT , offset BIGINT)");
        PreparedStatement ps = sqlConn.prepareStatement("INSERT INTO " + tableName + "(variation_id, offset) values (?, ?)");

        long offset = 0;
        int count = 0;
        String[] fields;
        String line;
        BufferedReader br = FileUtils.newBufferedReader(variationFilePath, Charset.defaultCharset());
        while ((line = br.readLine()) != null) {
            fields = line.split("\t");

            ps.setInt(1, Integer.parseInt(fields[columnIndex])); // motif_feature_id
            ps.setLong(2, offset); // seq_region_id
            ps.addBatch();
            count++;

            if (count % LIMITROWS == 0 && count != 0) {
                ps.executeBatch();
                sqlConn.commit();
                logger.info("Inserting in " + tableName + ": " + count);
//                if(count > 1000000) break;
            }

            offset += line.length() + 1;
        }
        br.close();

        ps.executeBatch();
        sqlConn.commit();

        createTableIndex(tableName);

        logger.debug("Elapsed time creating table " + tableName + ": " + stopwatch);
    }

    @Deprecated
    private void createTableIndex(String tableName) throws SQLException {
        logger.info("Creating index for " + tableName);
        Statement stm = sqlConn.createStatement();
        stm.executeUpdate("CREATE INDEX " + tableName + "_idx on " + tableName + "(variation_id)");
        sqlConn.commit();
        logger.info("Done");
    }

    private void gunzipVariationInputFiles() throws IOException, InterruptedException {
        logger.info("Unzipping variation files ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
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
        gzipFile(variationDirectoryPath, VARIATION_FEATURE_FILENAME);
        gzipFile(variationDirectoryPath, TRANSCRIPT_VARIATION_FILENAME);
        gzipFile(variationDirectoryPath, VARIATION_SYNONYM_FILENAME);
        gzipFile(variationDirectoryPath, PREPROCESSED_VARIATION_FEATURE_FILENAME);
        gzipFile(variationDirectoryPath, PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME);
        gzipFile(variationDirectoryPath, PREPROCESSED_VARIATION_SYNONYM_FILENAME);
    }

    private void gzipFile(Path directory, String fileName) throws IOException, InterruptedException {
        Path unzippedFile = directory.resolve(fileName);
        if (Files.exists(unzippedFile)) {
            Process process = Runtime.getRuntime().exec("gzip " + unzippedFile.toAbsolutePath());
            process.waitFor();
        }
    }

}
