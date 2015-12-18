package org.opencb.cellbase.app.transform.variation;

import com.google.common.base.Stopwatch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by parce on 10/12/15.
 */
public class VariationFrequenciesFile extends AbstractVariationFile {

    private static final String FREQUENCIES_FILE_NAME = "eva_population_freqs.sorted.txt";
    private static final String PREPROCESSED_FREQUENCIES_FILE_NAME = "eva_population_freqs.sorted.includingVariationId.txt";

    private static final int VARIATION_ID_COLUMN_INDEX = 7;
    private static final int RS_COLUMN_INDEX_IN_FREQUENCIES_FILE = 5;
    private final Pattern populationFrequnciesPattern;
    private static final String POPULATION_ID_GROUP = "popId";
    private static final String REFERENCE_FREQUENCY_GROUP = "ref";
    private static final String ALTERNATE_FREQUENCY_GROUP = "alt";

    public VariationFrequenciesFile(Path variationDirectory) {
        super(variationDirectory, FREQUENCIES_FILE_NAME, PREPROCESSED_FREQUENCIES_FILE_NAME, VARIATION_ID_COLUMN_INDEX);
        populationFrequnciesPattern = Pattern.compile("(?<" + POPULATION_ID_GROUP + ">\\w+):(?<"
                + REFERENCE_FREQUENCY_GROUP + ">\\d+(.\\d+)?),(?<" + ALTERNATE_FREQUENCY_GROUP + ">\\d+(.\\d+)?)");
    }

    public void loadIntoMap() throws IOException {
        logger.info("Loading frequencies file {} into memory ...", unprocessedFileName);
        Stopwatch stopwatch = Stopwatch.createStarted();
        BufferedReader variationFileReader = this.getUnprocessedFileBufferedReader();
        Map<Variant, List<Freq>> freqsMap = new HashMap<>();
        String line;
        long read = 0;
        while ((line = variationFileReader.readLine()) != null) {
            String[] varFreqFields = line.split("\t");
            // TODO: usar pattern para key
            if (varFreqFields.length == 7) {
                List<Freq> freqs = parseFreqString(varFreqFields[6]);
                StringJoiner keyBuilder = new StringJoiner(":");
                //keyBuilder.add(varFreqFields[0]).add(varFreqFields[1]).add(varFreqFields[3]).add(varFreqFields[4]);
                Variant key = new Variant(varFreqFields[0], Integer.parseInt(varFreqFields[1]), varFreqFields[3], varFreqFields[4]);
                freqsMap.put(key, freqs);
            }
            read++;
            if (read % 1000000 == 0) {
                logger.debug("{} read. Elapsed time loading frequencies: {}", read, stopwatch);
            }
        }
        logger.debug("Freqs map size: {}", freqsMap.size());
        logger.debug("Loaded. Elapsed time loading frequencies: {}", stopwatch);
    }

    private List<Freq> parseFreqString(String variationFrequenciesString) {
        List<Freq> freqs = new ArrayList<>();
        for (String populationFrequency : variationFrequenciesString.split(";")) {
            Freq freq = parsePopulationFrequency(populationFrequency);
            if (freq != null) {
                freqs.add(freq);
            }
        }
        return freqs;
    }

    private Freq parsePopulationFrequency(/*float[][] freqs,*/ String frequencyString) {
        Freq freq = null;
        Matcher m = populationFrequnciesPattern.matcher(frequencyString);
        if (m.matches()) {
            short index;
            String population = m.group(POPULATION_ID_GROUP);
            switch (population) {
                case "1000G_PHASE_1_ALL_AF":
                    index = 0;
                    break;
                case "1000G_PHASE_1_AMR_AF":
                    index = 1;
                    break;
                case "1000G_PHASE_1_ASN_AF":
                    index = 2;
                    break;
                case "1000G_PHASE_1_AFR_AF":
                    index = 3;
                    break;
                case "1000G_PHASE_1_EUR_AF":
                    index = 4;
                    break;
                case "1000G_PHASE_3_ALL_AF":
                    index = 5;
                    break;
                case "1000G_PHASE_3_AMR_AF":
                    index = 6;
                    break;
                case "1000G_PHASE_3_AFR_AF":
                    index = 7;
                    break;
                case "1000G_PHASE_3_EUR_AF":
                    index = 8;
                    break;
                case "1000G_PHASE_3_EAS_AF":
                    index = 9;
                    break;
                case "1000G_PHASE_3_SAS_AF":
                    index = 10;
                    break;
                case "ESP_6500_EA_AF":
                    index = 11;
                    break;
                case "ESP_6500_AA_AF":
                    index = 12;
                    break;
                case "ESP_6500_ALL_AF":
                    index = 13;
                    break;
                case "EXAC_AFR_AF":
                    index = 14;
                    break;
                case "EXAC_AMR_AF":
                    index = 15;
                    break;
                case "EXAC_EAS_AF":
                    index = 16;
                    break;
                case "EXAC_FIN_AF":
                    index = 17;
                    break;
                case "EXAC_NFE_AF":
                    index = 18;
                    break;
                case "EXAC_SAS_AF":
                    index = 19;
                    break;
                case "EXAC_OTH_AF":
                    index = 20;
                    break;
                case "EXAC_ALL_AF":
                    index = 21;
                    break;
                default:
                    index = -1;
                    break;
            }
            if (index != -1) {
                //freqs[index][0] = Float.valueOf(m.group(REFERENCE_FREQUENCY_GROUP));
                //freqs[index][1] = Float.valueOf(m.group(ALTERNATE_FREQUENCY_GROUP));
                freq = new Freq(index,
                        Float.valueOf(m.group(REFERENCE_FREQUENCY_GROUP)),
                        Float.valueOf(m.group(ALTERNATE_FREQUENCY_GROUP)));
            }
        }
        return freq;

    }

    public void preprocess(VariationFile variationFile, VariationSynonymFile variationSynonymFile)
            throws IOException, InterruptedException {
        if (!existsZippedOrUnzippedFile(preprocessedFileName)) {
            logger.info("Preprocessing {} file ...", unprocessedFileName);
            Stopwatch stopwatch = Stopwatch.createStarted();

            // add variationId to transcript_variation file
            Map<String, Integer> rsToVariationIdMap = createRsToVariationIdMap(variationFile, variationSynonymFile);
            Path variationFrequenciesTempFile = addVariationIdToVariationFrequenciesFile(rsToVariationIdMap);

            sortFileByNumericColumn(variationFrequenciesTempFile, variationDirectory.resolve(preprocessedFileName), variationIdColumnIndex);

            logger.info("Removing temp file {}", variationFrequenciesTempFile);
            variationFrequenciesTempFile.toFile().delete();
            logger.info("Removed");

            logger.info("{} preprocessed. New file {} including (and sorted by) variation Id has been created", FREQUENCIES_FILE_NAME,
                    PREPROCESSED_FREQUENCIES_FILE_NAME);
            logger.debug("Elapsed time preprocessing transcript variation file: {}", stopwatch);
        }
    }

    private Map<String, Integer> createRsToVariationIdMap(VariationFile variationFile, VariationSynonymFile variationSynonymFile)
            throws IOException {
        logger.info("Creating rs to variationId map ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<String, Integer> variationRsToVariationId = new HashMap<>();

        addFileToMap(variationFile, variationRsToVariationId, VariationFile.VARIATION_ID_COLUMN_INDEX, VariationFile.RS_COLUMN_INDEX);
        addFileToMap(variationSynonymFile, variationRsToVariationId, VariationSynonymFile.VARIATION_ID_COLUMN_INDEX,
                VariationSynonymFile.RS_COLUMN_INDEX);

        logger.info("Done");
        logger.debug("Elapsed time creating rs to variationId map: {}", stopwatch);

        return variationRsToVariationId;
    }

    private void addFileToMap(AbstractVariationFile variationFile, Map<String, Integer> variationRsToVariationId,
                              int variationIdColumnIndex, int rsColumnIndex) throws IOException {
        BufferedReader variationFileReader = variationFile.getUnprocessedFileBufferedReader();

        String line;
        while ((line = variationFileReader.readLine()) != null) {
            // TODO: add limit parameter would do that run faster?
            String[] fields = line.split("\t");
            Integer variationId = Integer.valueOf(fields[variationIdColumnIndex]);
            String variationRs = fields[rsColumnIndex];
            variationRsToVariationId.put(variationRs, variationId);
        }
        // TODO: use synonyms also?

        variationFileReader.close();
    }

    private Path addVariationIdToVariationFrequenciesFile(Map<String, Integer> variationFeatureToVariationId) throws IOException {
        Path variationFrequenciesTempFile = variationDirectory.resolve(unprocessedFileName + ".tmp");
        logger.info("Adding variation Id to variation frequencies file and saving them into {} ...", variationFrequenciesTempFile);
        Stopwatch stopwatch = Stopwatch.createStarted();

        BufferedReader br = getUnprocessedFileBufferedReader();
        BufferedWriter bw = Files.newBufferedWriter(variationFrequenciesTempFile, Charset.defaultCharset(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        String line;
        long linesRead = 0, idsAdded = 0;
        while ((line = br.readLine()) != null) {
            linesRead++;
            // TODO: add limit parameter would do that run faster?
            // TODO: use a precompiled pattern would improve efficiency
            String[] fields = line.split("\t");
            if (fields.length == VARIATION_ID_COLUMN_INDEX) {
                String ids = fields[RS_COLUMN_INDEX_IN_FREQUENCIES_FILE];
                for (String rs : ids.split(";")) {
                    if (rs.startsWith("rs")) {
                        Integer variationId = variationFeatureToVariationId.get(rs);
                        if (variationId != null) {
                            bw.write(line + "\t" + variationId + "\n");
                            idsAdded++;
                            break;
                        }
                    }
                }
            }
        }
        logger.debug("{} frequencies lines read", linesRead);
        logger.debug("{} ids added", idsAdded);

        br.close();
        bw.close();

        logger.info("Added");
        logger.debug("Elapsed time adding variation Id to transcript variation file: {}", stopwatch);

        return variationFrequenciesTempFile;
    }

    class Freq {
        private short index;
        private float ref;
        private float alt;

        Freq(short index, float ref, float alt) {
            this.index = index;
            this.ref = ref;
            this.alt = alt;
        }
    }

    class Variant {
        private String seq;
        private int start;
        private String ref;
        private String alt;

        Variant(String seq, int start, String ref, String alt) {
            this.seq = seq;
            this.start = start;
            this.ref = ref;
            this.alt = alt;
        }
    }
}
