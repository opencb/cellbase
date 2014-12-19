package org.opencb.cellbase.build.transform;

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
import java.sql.*;
import java.util.*;

public class VariationParser extends CellBaseParser {

    public static final String VARIATION_FILENAME = "variation.txt.gz";
    public static final String VARIATION_FEATURE_FILENAME = "variation_feature.txt";
    public static final String TRANSCRIPT_VARIATION_FILENAME = "transcript_variation.txt";
    public static final String VARIATION_SYNONYM_FILENAME = "variation_synonym.txt";
    private RandomAccessFile rafVariationFeature, rafTranscriptVariation, rafVariationSynonym;
    private Connection sqlConn = null;
    private PreparedStatement prepStmVariationFeature, prepStmTranscriptVariation, prepStmVariationSynonym;

    private int LIMITROWS = 100000;
    private Path variationDirectoryPath;


    public VariationParser(Path variationDirectoryPath, CellBaseSerializer serializer) {
        super(serializer);
        this.variationDirectoryPath = variationDirectoryPath;
    }

    //	public void parseCosmic(String species, String assembly, String source, String version, Path variationDirectoryPath, Path outfileJson) throws IOException, SQLException {
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
        gunzipVariationInputFiles(variationDirectoryPath);

        // Prepares connections to database to resolve queries by ID.
        // This version combines a SQLite database with the file offsets
        // with a RandomAccessFile to access data using offsets.
        connect(variationDirectoryPath);

        Map<String, String> seqRegionMap = VariationUtils.parseSeqRegionToMap(variationDirectoryPath);
        Map<String, String> sourceMap = VariationUtils.parseSourceToMap(variationDirectoryPath);

        long countprocess = 0;
        String line;
        while ((line = bufferedReaderVariation.readLine()) != null) {
            String[] variationFields = line.split("\t");

            int variationId = Integer.parseInt(variationFields[0]);
            List<String> resultVariationFeature = queryByVariationId(variationId, "variation_feature");

            if (resultVariationFeature != null && resultVariationFeature.size() > 0) {
                String[] variationFeatureFields = resultVariationFeature.get(0).split("\t", -1);

                List<TranscriptVariation> transcriptVariation = getTranscriptVariations(variationFeatureFields[0]);
                List<Xref> xrefs = getXrefs(sourceMap, variationId);

                try {
                    // Preparing the variation alleles
                    String[] allelesArray = getAllelesArray(variationFeatureFields);

                    // For code sanity save chromosome in a variable
                    String chromosome = seqRegionMap.get(variationFeatureFields[1]);

                    // TODO: check that variationFeatureFields is always different to null and intergenic-variant is never used
                    //List<String> consequenceTypes = (variationFeatureFields != null) ? Arrays.asList(variationFeatureFields[12].split(",")) : Arrays.asList("intergenic_variant");
                    List<String> consequenceTypes = Arrays.asList(variationFeatureFields[12].split(","));
                    String displayConsequenceType = getDisplayConsequenceType(consequenceTypes);

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
        }

        gzipVariationFiles(variationDirectoryPath);

        try {
            bufferedReaderVariation.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (consequenceTypes.size() == 0) {
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
        List<String> resultVariationSynonym = queryByVariationId(variationId, "variation_synonym");
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

    private List<TranscriptVariation> getTranscriptVariations(String variationFeatureField) throws IOException, SQLException {
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

    public void connect(Path variationDirectoryPath) throws SQLException, ClassNotFoundException, IOException {
        createSqlLiteConnection(variationDirectoryPath);
        prepareSelectStatements();
        prepareRandomAccessFiles(variationDirectoryPath);
    }

    private void createSqlLiteConnection(Path variationDirectoryPath) throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.sqlite.JDBC");
        sqlConn = DriverManager.getConnection("jdbc:sqlite:" + variationDirectoryPath.toAbsolutePath().toString() + "/variation_tables.db");
        if (!Files.exists(variationDirectoryPath.resolve("variation_tables.db")) || Files.size(variationDirectoryPath.resolve("variation_tables.db")) == 0) {
            sqlConn.setAutoCommit(false);
            createTable(5, variationDirectoryPath.resolve(VARIATION_FEATURE_FILENAME), "variation_feature");
            createTable(1, variationDirectoryPath.resolve(TRANSCRIPT_VARIATION_FILENAME), "transcript_variation");
            createTable(1, variationDirectoryPath.resolve(VARIATION_SYNONYM_FILENAME), "variation_synonym");
            sqlConn.setAutoCommit(true);
        }
    }

    private void prepareRandomAccessFiles(Path variationDirectoryPath) throws FileNotFoundException {
        rafVariationFeature = new RandomAccessFile(variationDirectoryPath.resolve(VARIATION_FEATURE_FILENAME).toFile(), "r");
        rafTranscriptVariation = new RandomAccessFile(variationDirectoryPath.resolve(TRANSCRIPT_VARIATION_FILENAME).toFile(), "r");
        rafVariationSynonym = new RandomAccessFile(variationDirectoryPath.resolve(VARIATION_SYNONYM_FILENAME).toFile(), "r");
    }

    private void prepareSelectStatements() throws SQLException {
        prepStmVariationFeature = sqlConn.prepareStatement("select offset from variation_feature where variation_id = ? order by offset ASC ");
        prepStmTranscriptVariation = sqlConn.prepareStatement("select offset from transcript_variation where variation_id = ? order by offset ASC ");
        prepStmVariationSynonym = sqlConn.prepareStatement("select offset from variation_synonym where variation_id = ? order by offset ASC ");
    }

    public void disconnect() {
        super.disconnect();
        closeSqlLiteObjects();
        closeRandomAccessFile(rafVariationFeature);
        closeRandomAccessFile(rafTranscriptVariation);
        closeRandomAccessFile(rafVariationSynonym);
    }

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

    private void closeRandomAccessFile(RandomAccessFile file) {
        try {
            file.close();
        } catch (IOException e) {
            logger.error("Error closing file : " + e.getMessage());
        }
    }

    public List<String> queryByVariationId(int variationId, String tableName) throws IOException, SQLException {
        List<Long> offsets;
        List<String> variations = null;
        switch (tableName) {
            case "variation_feature":
                offsets = getOffsetForVariationRandomAccessFile(prepStmVariationFeature, variationId);
                variations = getVariationsFromRandomAccesssFile(offsets, rafVariationFeature);
                break;
            case "transcript_variation":
                offsets = getOffsetForVariationRandomAccessFile(prepStmTranscriptVariation, variationId);
                variations = getVariationsFromRandomAccesssFile(offsets, rafTranscriptVariation);
                break;
            case "variation_synonym":
                offsets = getOffsetForVariationRandomAccessFile(prepStmVariationSynonym, variationId);
                variations = getVariationsFromRandomAccesssFile(offsets, rafVariationSynonym);
                break;
        }
        return variations;
    }

    private List<Long> getOffsetForVariationRandomAccessFile(PreparedStatement statement, int variationId) throws SQLException {
        statement.setInt(1, variationId);
        ResultSet rs = statement.executeQuery();

        List<Long> offsets = new ArrayList<>();
        while (rs.next()) {
            offsets.add(rs.getLong(1));
        }
        Collections.sort(offsets);

        return offsets;
    }

    private List<String> getVariationsFromRandomAccesssFile(List<Long> offsets, RandomAccessFile raf) throws IOException {
        List<String> results = new ArrayList<>();

        for (Long offset : offsets) {
            if (offset >= 0) {
                raf.seek(offset);
                String line = raf.readLine();
                if (line != null) {
                    results.add(line);
                }
            }
        }

        return results;
    }

    private void createTable(int columnIndex, Path variationFilePath, String tableName) throws SQLException, IOException {
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

        Statement stm = sqlConn.createStatement();
        stm.executeUpdate("CREATE INDEX " + tableName + "_idx on " + tableName + "(variation_id)");
        sqlConn.commit();
    }

    private void gunzipVariationInputFiles(Path variationDirectoryPath) throws IOException, InterruptedException {
        logger.info("Unzipping variation files ...");
        gunzipFile(variationDirectoryPath, VARIATION_FEATURE_FILENAME);
        gunzipFile(variationDirectoryPath, TRANSCRIPT_VARIATION_FILENAME);
        gunzipFile(variationDirectoryPath, VARIATION_SYNONYM_FILENAME);
        logger.info("Done");

    }

    private void gunzipFile(Path directory, String fileName) throws IOException, InterruptedException {
        Path zippedFile = directory.resolve(fileName + ".gz");
        logger.info("Unzipping " + zippedFile + "...");
        if (Files.exists(zippedFile)) {
            Process process = Runtime.getRuntime().exec("gunzip " + zippedFile.toAbsolutePath());
            process.waitFor();
        } else {
            logger.error("File " + zippedFile + " doesn't exist");
        }
    }


    private void gzipVariationFiles(Path variationDirectoryPath) throws IOException, InterruptedException {
        gzipFile(variationDirectoryPath, VARIATION_FEATURE_FILENAME);

        if (Files.exists(variationDirectoryPath.resolve(TRANSCRIPT_VARIATION_FILENAME))) {
            Process process = Runtime.getRuntime().exec("gzip " + variationDirectoryPath.resolve(TRANSCRIPT_VARIATION_FILENAME).toAbsolutePath());
            process.waitFor();
        }

        if (Files.exists(variationDirectoryPath.resolve(VARIATION_SYNONYM_FILENAME))) {
            Process process = Runtime.getRuntime().exec("gzip " + variationDirectoryPath.resolve(VARIATION_SYNONYM_FILENAME).toAbsolutePath());
            process.waitFor();
        }
    }

    private void gzipFile(Path directory, String fileName) throws IOException, InterruptedException {
        Path unzippedFile = directory.resolve(fileName);
        if (Files.exists(unzippedFile)) {
            Process process = Runtime.getRuntime().exec("gzip " + unzippedFile.toAbsolutePath());
            process.waitFor();
        } else {
            logger.error("File " + unzippedFile + " doesn't exist");
        }
    }

}
