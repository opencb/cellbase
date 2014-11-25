package org.opencb.cellbase.build.transform;

import org.opencb.biodata.models.variation.TranscriptVariation;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.biodata.models.variation.Xref;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.build.transform.utils.FileUtils;
import org.opencb.cellbase.build.transform.utils.VariationUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

public class VariationParser extends CellBaseParser {

    private RandomAccessFile raf, rafVariationFeature, rafTranscriptVariation, rafVariationSynonym;
    private Connection sqlConn = null;
    private PreparedStatement prepStmVariationFeature, prepStmTranscriptVariation, prepStmVariationSynonym;

    private static final int DEFAULT_CHUNK_SIZE = 1000;

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

        BufferedWriter bwLog = Files.newBufferedWriter(variationDirectoryPath.resolve("variation.log"), Charset.defaultCharset());
        Map<String, List<String>> queryMap = null;
        String[] variationFields = null;
        String[] variationFeatureFields = null;
        String[] transcriptVariationFields = null;
        String[] variationSynonymFields = null;
        String chromosome;

        Variation variation = null;
//        VariationMongoDB variation = null;
        String[] allelesArray = null;
        String displayConsequenceType = null;
        List<String> consequenceTypes = null;
        List<TranscriptVariation> transcriptVariation = null;
        List<Xref> xrefs = null;

        // Open variation file, this file never gets uncompressed.
        // It's read from gzip file
        BufferedReader bufferedReaderVariation = FileUtils.newBufferedReader(variationDirectoryPath.resolve("variation.txt.gz"));

        // To speed up calculation a SQLite database is created with the IDs and file offsets,
        // file must be uncompressed for doing this.
        gunzipFiles(variationDirectoryPath);

        // Prepares connections to database to resolve queries by ID.
        // This version combines a SQLite database with the file offsets
        // with a RandomAccessFile to access data using offsets.
        connect(variationDirectoryPath);
//        createVariationDatabase(variationDirectoryPath);

        Map<String, String> seqRegionMap = VariationUtils.parseSeqRegionToMap(variationDirectoryPath);
        Map<String, String> sourceMap = VariationUtils.parseSourceToMap(variationDirectoryPath);

        String chunkIdSuffix = DEFAULT_CHUNK_SIZE / 1000 + "k";
        int countprocess = 0;
        int variationId = 0;
        String line = null;
        while ((line = bufferedReaderVariation.readLine()) != null) {
            variationFields = line.split("\t");
            variationId = Integer.parseInt(variationFields[0]);

            List<String> resultVariationFeature = queryByVariationId(variationId, "variation_feature", variationDirectoryPath);

            if (resultVariationFeature != null && resultVariationFeature.size() > 0) {
                transcriptVariation = new ArrayList<>();
                variationFeatureFields = resultVariationFeature.get(0).split("\t", -1);

                // Note the ID used, TranscriptVariation references to VariationFeature no Variation !!!
                List<String> resultTranscriptVariations = queryByVariationId(Integer.parseInt(variationFeatureFields[0]), "transcript_variation", variationDirectoryPath);
                if (resultTranscriptVariations != null && resultTranscriptVariations.size() > 0) {
                    for (String rtv : resultTranscriptVariations) {
                        transcriptVariationFields = rtv.split("\t");

                        TranscriptVariation tv = new TranscriptVariation((transcriptVariationFields[2] != null && !transcriptVariationFields[2].equals("\\N")) ? transcriptVariationFields[2] : ""
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
                        transcriptVariation.add(tv);
                    }
                }

                // Reading XRefs
                List<String> resultVariationSynonym = queryByVariationId(variationId, "variation_synonym", variationDirectoryPath);
                xrefs = new ArrayList<>();
                if (resultVariationSynonym != null && resultVariationSynonym.size() > 0) {
                    String arr[];
                    for (String rxref : resultVariationSynonym) {
                        variationSynonymFields = rxref.split("\t");
                        if (sourceMap.get(variationSynonymFields[3]) != null) {
                            arr = sourceMap.get(variationSynonymFields[3]).split(",");
                            xrefs.add(new Xref(variationSynonymFields[4], arr[0], arr[1]));
                        }
                    }
                }

                try {
                    // Preparing the variation alleles
                    if (variationFeatureFields != null && variationFeatureFields[6] != null) {
                        allelesArray = variationFeatureFields[6].split("/");
                        if (allelesArray.length == 1) {    // In some cases no '/' exists, ie. in 'HGMD_MUTATION'
                            allelesArray = new String[]{"", ""};
                        }
                    } else {
                        allelesArray = new String[]{"", ""};
                    }

                    // For code sanity save chromosome in a variable
                    chromosome = seqRegionMap.get(variationFeatureFields[1]);

                    // Preparing displayConsequenceType and consequenceTypes attributes
                    consequenceTypes = (variationFeatureFields != null) ? Arrays.asList(variationFeatureFields[12].split(",")) : Arrays.asList("intergenic_variant");
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

                    // we have all the necessary to construct the 'variation' object
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

                    if (++countprocess % 10000 == 0 && countprocess != 0) {
                        logger.debug("Processed variations: " + countprocess);
                    }

                    serializer.serialize(variation);
                } catch (Exception e) {
                    e.printStackTrace();
                    bwLog.write(line + "\n");
                }
            }
        }

        // ONLY COMMENTED FOR SPEED UP DEVELOPMENT AS NO NEED TO COMPRESS EVERY RUN!!!
        gzipFiles(variationDirectoryPath);

        try {
            bwLog.close();
            bufferedReaderVariation.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect(Path variationDirectoryPath) throws SQLException, ClassNotFoundException, IOException {
        Class.forName("org.sqlite.JDBC");
        sqlConn = DriverManager.getConnection("jdbc:sqlite:" + variationDirectoryPath.toAbsolutePath().toString() + "/variation_tables.db");
        if (!Files.exists(variationDirectoryPath.resolve("variation_tables.db")) || Files.size(variationDirectoryPath.resolve("variation_tables.db")) == 0) {
            sqlConn.setAutoCommit(false);
            createTable(5, variationDirectoryPath.resolve("variation_feature.txt"), "variation_feature");
            createTable(1, variationDirectoryPath.resolve("transcript_variation.txt"), "transcript_variation");
            createTable(1, variationDirectoryPath.resolve("variation_synonym.txt"), "variation_synonym");
            sqlConn.setAutoCommit(true);
        }

        prepStmVariationFeature = sqlConn.prepareStatement("select offset from variation_feature where variation_id = ? order by offset ASC ");
        prepStmTranscriptVariation = sqlConn.prepareStatement("select offset from transcript_variation where variation_id = ? order by offset ASC ");
        prepStmVariationSynonym = sqlConn.prepareStatement("select offset from variation_synonym where variation_id = ? order by offset ASC ");

        rafVariationFeature = new RandomAccessFile(variationDirectoryPath.resolve("variation_feature.txt").toFile(), "r");
        rafTranscriptVariation = new RandomAccessFile(variationDirectoryPath.resolve("transcript_variation.txt").toFile(), "r");
        rafVariationSynonym = new RandomAccessFile(variationDirectoryPath.resolve("variation_synonym.txt").toFile(), "r");
    }

    public void disconnect() {
        super.disconnect();
        try {
            if (sqlConn != null && !sqlConn.isClosed()) {
                prepStmVariationFeature.close();
                prepStmTranscriptVariation.close();
                prepStmVariationSynonym.close();

                sqlConn.close();
            }
        } catch (SQLException e) {
            logger.error("Error closing connections: " + e.getMessage());
        }

        try {
            rafVariationFeature.close();
            rafTranscriptVariation.close();
            rafVariationSynonym.close();
        } catch (IOException e) {
            logger.error("Error closing file: " + e.getMessage());
        }
    }

    public List<String> queryByVariationId(int variationId, String tableName, Path variationFilePath) throws IOException, SQLException {
        // First query SQLite to get offset position
        List<Long> offsets = new ArrayList<>();
        //		PreparedStatement pst = sqlConn.statement(sql)
        //		ResultSet rs = pst.executeQuery("select offset from "+tableName+" where variation_id = " + variationId + "");
        ResultSet rs = null;
        switch (tableName) {
            case "variation_feature":
                prepStmVariationFeature.setInt(1, variationId);
                rs = prepStmVariationFeature.executeQuery();
                raf = rafVariationFeature;
                break;
            case "transcript_variation":
                prepStmTranscriptVariation.setInt(1, variationId);
                rs = prepStmTranscriptVariation.executeQuery();
                raf = rafTranscriptVariation;
                break;
            case "variation_synonym":
                prepStmVariationSynonym.setInt(1, variationId);
                rs = prepStmVariationSynonym.executeQuery();
                raf = rafVariationSynonym;
                break;
        }

        while (rs.next()) {
            offsets.add(rs.getLong(1));
        }
        Collections.sort(offsets);
        // Second go to file
        String line = null;
        List<String> results = new ArrayList<>();
        if (offsets.size() > 0) {
//			RandomAccessFile raf = new RandomAccessFile(variationFilePath.resolve(tableName+".txt").toFile(), "r");
            for (Long offset : offsets) {
                if (offset >= 0) {
                    raf.seek(offset);
                    line = raf.readLine();
                    if (line != null) {
                        results.add(line);
                    }
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
        String[] fields = null;
        String line = null;
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

    private void gunzipFiles(Path variationDirectoryPath) throws IOException, InterruptedException {
        if (Files.exists(variationDirectoryPath.resolve("variation_feature.txt.gz"))) {
            Process process = Runtime.getRuntime().exec("gunzip " + variationDirectoryPath.resolve("variation_feature.txt.gz").toAbsolutePath());
            process.waitFor();
        }

        if (Files.exists(variationDirectoryPath.resolve("transcript_variation.txt.gz"))) {
            Process process = Runtime.getRuntime().exec("gunzip " + variationDirectoryPath.resolve("transcript_variation.txt.gz").toAbsolutePath());
            process.waitFor();
        }

        if (Files.exists(variationDirectoryPath.resolve("variation_synonym.txt.gz"))) {
            Process process = Runtime.getRuntime().exec("gunzip " + variationDirectoryPath.resolve("variation_synonym.txt.gz").toAbsolutePath());
            process.waitFor();
        }
    }

    private void gzipFiles(Path variationDirectoryPath) throws IOException, InterruptedException {
        if (Files.exists(variationDirectoryPath.resolve("variation_feature.txt"))) {
            Process process = Runtime.getRuntime().exec("gzip " + variationDirectoryPath.resolve("variation_feature.txt").toAbsolutePath());
            process.waitFor();
        }

        if (Files.exists(variationDirectoryPath.resolve("transcript_variation.txt"))) {
            Process process = Runtime.getRuntime().exec("gzip " + variationDirectoryPath.resolve("transcript_variation.txt").toAbsolutePath());
            process.waitFor();
        }

        if (Files.exists(variationDirectoryPath.resolve("variation_synonym.txt"))) {
            Process process = Runtime.getRuntime().exec("gzip " + variationDirectoryPath.resolve("variation_synonym.txt").toAbsolutePath());
            process.waitFor();
        }
    }

//    class VariationMongoDB extends Variation {
//
//        private List<String> chunkIds;
//
//        public VariationMongoDB(String id, String chromosome, String type, int start, int end, String strand, String reference, String alternate, String alleleString, String ancestralAllele, String displayConsequenceType, List<String> consequencesTypes, List<TranscriptVariation> transcriptVariations, Phenotype phenotype, List<SampleGenotype> samples, List<PopulationFrequency> populationFrequencies, List<Xref> xrefs, /*String featureId,*/ String minorAllele, String minorAlleleFreq, String validationStatus, String evidence) {
//            super(id, chromosome, type, start, end, strand, reference, alternate, alleleString, ancestralAllele, displayConsequenceType, consequencesTypes, transcriptVariations, phenotype, samples, populationFrequencies, xrefs, /*featureId,*/ minorAllele, minorAlleleFreq, validationStatus, evidence);
//            this.chunkIds = new ArrayList<>(5);
//        }
//
//        public List<String> getChunkIds() {
//            return chunkIds;
//        }
//
//        public void setChunkIds(List<String> chunkIds) {
//            this.chunkIds = chunkIds;
//        }
//    }

    @Deprecated
    public void createVariationDatabase(Path variationDirectoryPath) {
        try {
//            Class.forName("org.sqlite.JDBC");
//            sqlConn = DriverManager.getConnection("jdbc:sqlite::memory:");
//			sqlConn = DriverManager.getConnection("jdbc:sqlite:"+variationFilePath.toAbsolutePath().toString()+"/variation_tables.db");
            if (!Files.exists(variationDirectoryPath.resolve("variation_tables.db"))) {

                sqlConn.setAutoCommit(false);
                createTable(5, variationDirectoryPath.resolve("variation_feature.txt"), "variation_feature");
                createTable(1, variationDirectoryPath.resolve("transcript_variation.txt"), "transcript_variation");
                createTable(1, variationDirectoryPath.resolve("variation_synonym.txt"), "variation_synonym");
                sqlConn.setAutoCommit(true);
//                prepStmVariationFeature = sqlConn.prepareStatement("select offset from variation_feature where variation_id = ? order by offset ASC ");
//                prepStmTranscriptVariation = sqlConn.prepareStatement("select offset from transcript_variation where variation_id = ? order by offset ASC ");
//                prepStmVariationSynonym = sqlConn.prepareStatement("select offset from variation_synonym where variation_id = ? order by offset ASC ");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public Map<String, List<String>> queryAllByVariationId(int variationId, Path variationFilePath) throws IOException, SQLException {

        List<String> tables = Arrays.asList("variation_feature", "transcript_variation", "variation_synonym");
        Map<String, List<String>> resultMap = new HashMap<>();
        List<Long> offsets;
        for (String table : tables) {

            // First query SQLite to get offset position
            offsets = new ArrayList<Long>();
            //		PreparedStatement pst = sqlConn.statement(sql)
            //		ResultSet rs = pst.executeQuery("select offset from "+tableName+" where variation_id = " + variationId + "");
            ResultSet rs = null;
            switch (table) {
                case "variation_feature":
                    prepStmVariationFeature.setInt(1, variationId);
                    rs = prepStmVariationFeature.executeQuery();
                    break;
                case "transcript_variation":
                    prepStmTranscriptVariation.setInt(1, variationId);
                    rs = prepStmTranscriptVariation.executeQuery();
                    break;
                case "variation_synonym":
                    prepStmVariationSynonym.setInt(1, variationId);
                    rs = prepStmVariationSynonym.executeQuery();
                    break;
            }

            while (rs.next()) {
                offsets.add(rs.getLong(1));
            }

            // Second go to file
            String line = null;
            List<String> results = new ArrayList<>();
            if (offsets.size() > 0) {
                RandomAccessFile raf = new RandomAccessFile(variationFilePath.resolve(table + ".txt.gz").toFile(), "r");
                for (Long offset : offsets) {
                    if (offset >= 0) {
                        raf.seek(offset);
                        line = raf.readLine();
                        if (line != null) {
                            results.add(line);
                        }
                    }
                }
                raf.close();
            }
            resultMap.put(table, results);
        }
        return resultMap;
    }

}
