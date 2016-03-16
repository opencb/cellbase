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

import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

/**
 * User: fsalavert.
 * Date: 4/10/13
 * Time: 10:14 AM
 */
public class RegulatoryRegionParser extends CellBaseParser {

    private static final int CHUNK_SIZE = 2000;
    private Path regulatoryRegionPath;

    public RegulatoryRegionParser(Path regulatoryRegionFilesDir, CellBaseSerializer serializer) {
        super(serializer);

        this.regulatoryRegionPath = regulatoryRegionFilesDir;

    }

    public void createSQLiteRegulatoryFiles(Path regulatoryRegionPath)
            throws SQLException, IOException, ClassNotFoundException, NoSuchMethodException {
        List<String> gffColumnNames = Arrays.asList("seqname", "source", "feature", "start", "end", "score", "strand", "frame", "group");
        List<String> gffColumnTypes = Arrays.asList("TEXT", "TEXT", "TEXT", "INT", "INT", "TEXT", "TEXT", "TEXT", "TEXT");

        //        Path regulatoryRegionPath = regulationDir.toPath();

        Path filePath;

        filePath = regulatoryRegionPath.resolve("AnnotatedFeatures.gff.gz");
        createSQLiteRegulatoryFiles(filePath, "annotated_features", gffColumnNames, gffColumnTypes);


        filePath = regulatoryRegionPath.resolve("MotifFeatures.gff.gz");
        createSQLiteRegulatoryFiles(filePath, "motif_features", gffColumnNames, gffColumnTypes);


        filePath = regulatoryRegionPath.resolve("RegulatoryFeatures_MultiCell.gff.gz");
        createSQLiteRegulatoryFiles(filePath, "regulatory_features_multicell", gffColumnNames, gffColumnTypes);

//  GFFColumnNames = Arrays.asList("seqname", "source", "feature", "start", "end", "score", "strand", "frame");
//  GFFColumnTypes = Arrays.asList("TEXT", "TEXT", "TEXT", "INT", "INT", "TEXT", "TEXT", "TEXT");
        filePath = regulatoryRegionPath.resolve("mirna_uniq.gff.gz");
        if (Files.exists(filePath)) {
            createSQLiteRegulatoryFiles(filePath, "mirna_uniq", gffColumnNames, gffColumnTypes);
        }

    }

    @Override
    public void parse() throws SQLException, IOException, ClassNotFoundException, NoSuchMethodException {
        if (regulatoryRegionPath == null || !Files.exists(regulatoryRegionPath) || !Files.isDirectory(regulatoryRegionPath)) {
            throw new IOException("Regulation directory whether does not exist, is not a directory or cannot be read");
        }

        // Create the SQLite databases
        createSQLiteRegulatoryFiles(regulatoryRegionPath);

        String chunkIdSuffix = CHUNK_SIZE / 1000 + "k";

        Path annotatedFilePath = regulatoryRegionPath.resolve("AnnotatedFeatures.gff.gz.db");
        Path motifFilePath = regulatoryRegionPath.resolve("MotifFeatures.gff.gz.db");
        Path regulatoryFilePath = regulatoryRegionPath.resolve("RegulatoryFeatures_MultiCell.gff.gz.db");
        Path mirnaFilePath = regulatoryRegionPath.resolve("mirna_uniq.gff.gz.db");

        List<Path> filePaths = Arrays.asList(annotatedFilePath, motifFilePath, regulatoryFilePath);
        List<String> tableNames = Arrays.asList("annotated_features", "motif_features", "regulatory_features_multicell");

        if (Files.exists(mirnaFilePath)) {
            filePaths.add(mirnaFilePath);
            tableNames.add("mirna_uniq");
        }

        // Fetching and joining all chromosomes found in the different databases
        Set<String> setChr = new HashSet<>();
        setChr.addAll(getChromosomesList(annotatedFilePath, "annotated_features"));
        setChr.addAll(getChromosomesList(motifFilePath, "motif_features"));
        setChr.addAll(getChromosomesList(regulatoryFilePath, "regulatory_features_multicell"));
        if (Files.exists(mirnaFilePath)) {
            setChr.addAll(getChromosomesList(mirnaFilePath, "mirna_uniq"));
        }

        List<String> chromosomes = new ArrayList<>(setChr);
        List<RegulatoryFeature> regulatoryFeatures;
        HashSet<Integer> chunksHash;
        for (String chromosome : chromosomes) {
            for (int i = 0; i < tableNames.size(); i++) {
                chunksHash = new HashSet<>();
                regulatoryFeatures = RegulatoryRegionParser.queryChromosomesRegulatoryDB(filePaths.get(i), tableNames.get(i), chromosome);
                for (RegulatoryFeature regulatoryFeature : regulatoryFeatures) {
                    int firstChunkId = getChunkId(regulatoryFeature.getStart(), CHUNK_SIZE);
                    int lastChunkId = getChunkId(regulatoryFeature.getEnd(), CHUNK_SIZE);

                    List<String> chunkIds = new ArrayList<>();
                    String chunkId;
                    for (int j = firstChunkId; j <= lastChunkId; j++) {
                        chunkId = chromosome + "_" + j + "_" + chunkIdSuffix;
                        chunkIds.add(chunkId);
                        //count chunks
                        if (!chunksHash.contains(j)) {
                            chunksHash.add(j);
                        }
                    }
//                    regulatoryFeature.setChunkIds(chunkIds);

                    // remove 'chr' prefix
//                    if (genericFeature.getChromosome() != null) {
//                        genericFeature.setSequenceName(genericFeature.getSequenceName().replace("chr", ""));
//                    }
                    serializer.serialize(regulatoryFeature);
                }
            }
        }
    }


    public void createSQLiteRegulatoryFiles(Path filePath, String tableName, List<String> columnNames, List<String> columnTypes)
            throws ClassNotFoundException, IOException, SQLException {
        int limitRows = 100000;
        int batchCount = 0;

        if (!Files.exists(filePath)) {
            return;
        }

        Path dbPath = Paths.get(filePath.toString() + ".db");
        if (Files.exists(dbPath) && Files.size(dbPath) > 0) {
            return;
        }

        BufferedReader br = FileUtils.newBufferedReader(filePath);

        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString());
        conn.setAutoCommit(false); //Set false to perform commits manually and increase performance on insertion

        //Create table query
        Statement createTables = conn.createStatement();

        StringBuilder sbQuery = new StringBuilder();
        sbQuery.append("CREATE TABLE if not exists " + tableName + "(");
        for (int i = 0; i < columnNames.size(); i++) {    //columnNames and columnTypes must have the same size
            sbQuery.append("'" + columnNames.get(i) + "' " + columnTypes.get(i) + ",");
        }
        sbQuery.deleteCharAt(sbQuery.length() - 1);
        sbQuery.append(")");

        System.out.println(sbQuery.toString());
        createTables.executeUpdate(sbQuery.toString());

        //Prepare insert query
        sbQuery = new StringBuilder();
        sbQuery.append("INSERT INTO " + tableName + "(");
        for (int i = 0; i < columnNames.size(); i++) {
            sbQuery.append("'" + columnNames.get(i) + "',");
        }
        sbQuery.deleteCharAt(sbQuery.length() - 1);
        sbQuery.append(") values (");
        sbQuery.append(repeat("?,", columnNames.size()));
        sbQuery.deleteCharAt(sbQuery.length() - 1);
        sbQuery.append(")");
        System.out.println(sbQuery.toString());

        PreparedStatement ps = conn.prepareStatement(sbQuery.toString());

        //Read file
        String line = null;
        while ((line = br.readLine()) != null) {

            insertByType(ps, getFields(line, tableName), columnTypes);
            ps.addBatch();
            batchCount++;

            //commit batch
            if (batchCount % limitRows == 0 && batchCount != 0) {
                ps.executeBatch();
                conn.commit();
            }

        }
        br.close();

        //Execute last Batch
        ps.executeBatch();
        conn.commit();

        //Create index
        System.out.println("creating indices...");
        createTables.executeUpdate("CREATE INDEX " + tableName + "_seqname_idx on " + tableName + "(" + columnNames.get(0) + ")");
        System.out.println("indices created.");

        conn.commit();
        conn.close();
    }

    public static List<String> getChromosomesList(Path dbPath, String tableName) throws IOException {
        FileUtils.checkFile(dbPath);

        List<String> chromosomes = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString());

            Statement query = conn.createStatement();
            ResultSet rs = query.executeQuery("select distinct(seqname) from " + tableName + " where seqname like 'chr%'");

            while (rs.next()) {
                chromosomes.add(rs.getString(1).replace("chr", ""));
            }
            conn.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return chromosomes;
    }

    public static List<RegulatoryFeature> queryChromosomesRegulatoryDB(Path dbPath, String tableName, String chromosome) {
        Connection conn;
        List<RegulatoryFeature> regulatoryFeatures = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString());

            Statement query = conn.createStatement();
            ResultSet rs = query.executeQuery("select * from " + tableName + " where seqname='chr" + chromosome + "'");
            while (rs.next()) {
                regulatoryFeatures.add(getRegulatoryFeature(rs, tableName));
            }
            conn.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return regulatoryFeatures;
    }

    public static List<RegulatoryFeature> queryRegulatoryDB(Path dbPath, String tableName, String chrFile, int start, int end) {
        Connection conn = null;
        List<RegulatoryFeature> regulatoryFeatures = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString());

            Statement query = conn.createStatement();
            ResultSet rs = query.executeQuery("select * from " + tableName + " where start<=" + end + " AND end>=" + start);

            while (rs.next()) {
                regulatoryFeatures.add(getRegulatoryFeature(rs, tableName));
            }
            conn.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return regulatoryFeatures;
    }

    private static RegulatoryFeature getRegulatoryFeature(ResultSet rs, String tableName) throws SQLException {
        RegulatoryFeature regulatoryFeature = null;
        switch (tableName.toLowerCase()) {
            case "annotated_features":
                regulatoryFeature = getAnnotatedFeature(rs);
                break;
            case "regulatory_features_multicell":
                regulatoryFeature = getRegulatoryFeature(rs);
                break;
            case "motif_features":
                regulatoryFeature = getMotifFeature(rs);
                break;
            case "mirna_uniq":
                regulatoryFeature = getMirnaFeature(rs);
                break;
            default:
                break;
        }
        return regulatoryFeature;
    }

    private static RegulatoryFeature getAnnotatedFeature(ResultSet rs) throws SQLException {
        //   GFF     https://genome.ucsc.edu/FAQ/FAQformat.html#format3
        RegulatoryFeature regulatoryFeature = new RegulatoryFeature();
        Map<String, String> groupFields = getGroupFields(rs.getString(9));

        regulatoryFeature.setChromosome(rs.getString(1).replace("chr", ""));
        regulatoryFeature.setSource(rs.getString(2));
        regulatoryFeature.setFeatureType(rs.getString(3));
        regulatoryFeature.setStart(rs.getInt(4));
        regulatoryFeature.setEnd(rs.getInt(5));
        regulatoryFeature.setScore(rs.getString(6));
        regulatoryFeature.setStrand(rs.getString(7));
        regulatoryFeature.setFrame(rs.getString(8));

        regulatoryFeature.setName(groupFields.get("name"));
        regulatoryFeature.setAlias(groupFields.get("alias"));
        regulatoryFeature.setFeatureClass(groupFields.get("class"));
        regulatoryFeature.getCellTypes().add(groupFields.get("cell_type"));

        return regulatoryFeature;
    }

    private static RegulatoryFeature getRegulatoryFeature(ResultSet rs) throws SQLException {
        //   GFF     https://genome.ucsc.edu/FAQ/FAQformat.html#format3
        RegulatoryFeature regulatoryFeature = new RegulatoryFeature();
        Map<String, String> groupFields = getGroupFields(rs.getString(9));

        regulatoryFeature.setChromosome(rs.getString(1).replace("chr", ""));
        regulatoryFeature.setSource(rs.getString(2));
        regulatoryFeature.setFeatureType(rs.getString(3));
        regulatoryFeature.setStart(rs.getInt(4));
        regulatoryFeature.setEnd(rs.getInt(5));
        regulatoryFeature.setScore(rs.getString(6));
        regulatoryFeature.setStrand(rs.getString(7));
        regulatoryFeature.setFrame(rs.getString(8));
        regulatoryFeature.setFrame(rs.getString(9));

        return regulatoryFeature;
    }

    private static RegulatoryFeature getMotifFeature(ResultSet rs) throws SQLException {
        //   GFF     https://genome.ucsc.edu/FAQ/FAQformat.html#format3
        RegulatoryFeature regulatoryFeature = new RegulatoryFeature();
        Map<String, String> groupFields = getGroupFields(rs.getString(9));

        regulatoryFeature.setChromosome(rs.getString(1).replace("chr", ""));
        regulatoryFeature.setSource(rs.getString(2));
        regulatoryFeature.setFeatureType(rs.getString(3) + "_motif");
        regulatoryFeature.setStart(rs.getInt(4));
        regulatoryFeature.setEnd(rs.getInt(5));
        regulatoryFeature.setScore(rs.getString(6));
        regulatoryFeature.setStrand(rs.getString(7));
        regulatoryFeature.setFrame(rs.getString(8));

        String[] split = groupFields.get("name").split(":");
        regulatoryFeature.setName(split[0]);
        regulatoryFeature.setMatrix(split[1]);

        return regulatoryFeature;
    }

    private static RegulatoryFeature getMirnaFeature(ResultSet rs) throws SQLException {
        //   GFF     https://genome.ucsc.edu/FAQ/FAQformat.html#format3
        RegulatoryFeature regulatoryFeature = new RegulatoryFeature();
        Map<String, String> groupFields = getGroupFields(rs.getString(9));

        regulatoryFeature.setChromosome(rs.getString(1).replace("chr", ""));
        regulatoryFeature.setSource(rs.getString(2));
        regulatoryFeature.setFeatureType(rs.getString(3));
        regulatoryFeature.setStart(rs.getInt(4));
        regulatoryFeature.setEnd(rs.getInt(5));
        regulatoryFeature.setScore(rs.getString(6));
        regulatoryFeature.setStrand(rs.getString(7));
        regulatoryFeature.setFrame(rs.getString(8));

        regulatoryFeature.setFeatureClass("microRNA");
        regulatoryFeature.setName(groupFields.get("name"));

        return regulatoryFeature;
    }

    private static Map<String, String> getGroupFields(String group) {
        //process group column
        Map<String, String> groupFields = new HashMap<>();
        String[] attributeFields = group.split(";");
        String[] attributeKeyValue;
        for (String attributeField : attributeFields) {
            attributeKeyValue = attributeField.trim().split("=");
            groupFields.put(attributeKeyValue[0].toLowerCase(), attributeKeyValue[1]);
        }
        return groupFields;
    }


    public static List<String> getFields(String line, String tableName) {
        List<String> fields = new ArrayList<>();
        switch (tableName.toLowerCase()) {
            case "annotated_features":
                fields = getAnnotatedFeaturesFields(line);
                break;
            case "regulatory_features_multicell":
                fields = getRegulatoryFeaturesFields(line);
                break;
            case "motif_features":
                fields = getMotifFeaturesFields(line);
                break;
            case "mirna_uniq":
                fields = getMirnaFeaturesFields(line);
                break;
            default:
                break;
        }
        return fields;
    }

    public static List<String> getAnnotatedFeaturesFields(String line) {
        String[] fields = line.split("\t");
        return Arrays.asList(fields);
    }

    public static List<String> getRegulatoryFeaturesFields(String line) {
        String[] fields = line.split("\t");
        return Arrays.asList(fields);
    }

    public static List<String> getMotifFeaturesFields(String line) {
        String[] fields = line.split("\t");
        return Arrays.asList(fields);
    }

    public static List<String> getMirnaFeaturesFields(String line) {
        String[] fields = line.split("\t");
        return Arrays.asList(fields);
    }

    public static void insertByType(PreparedStatement ps, List<String> fields, List<String> types) throws SQLException {
        //Datatypes In SQLite Version 3 -> http://www.sqlite.org/datatype3.html
        String raw;
        String type;
        if (types.size() == fields.size()) {
            for (int i = 0; i < fields.size(); i++) { //columnNames and columnTypes must have same size
                int sqliteIndex = i + 1;
                raw = fields.get(i);
                type = types.get(i);

                switch (type) {
                    case "INTEGER":
                    case "INT":
                        ps.setInt(sqliteIndex, Integer.parseInt(raw));
                        break;
                    case "REAL":
                        ps.setFloat(sqliteIndex, Float.parseFloat(raw));
                        break;
                    case "TEXT":
                        ps.setString(sqliteIndex, raw);
                        break;
                    default:
                        ps.setString(sqliteIndex, raw);
                        break;
                }
            }
        }

    }

    public String repeat(String s, int n) {
        if (s == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    private int getChunkId(int position, int chunksize) {
        if (chunksize <= 0) {
            return position / CHUNK_SIZE;
        } else {
            return position / chunksize;
        }
    }

    private int getChunkStart(int id, int chunksize) {
        if (chunksize <= 0) {
            return (id == 0) ? 1 : id * CHUNK_SIZE;
        } else {
            return (id == 0) ? 1 : id * chunksize;
        }
    }

    private int getChunkEnd(int id, int chunksize) {
        if (chunksize <= 0) {
            return (id * CHUNK_SIZE) + CHUNK_SIZE - 1;
        } else {
            return (id * chunksize) + chunksize - 1;
        }
    }
}
