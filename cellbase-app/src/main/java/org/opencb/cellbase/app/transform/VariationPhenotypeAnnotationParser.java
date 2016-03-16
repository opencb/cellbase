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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Splitter;
import org.opencb.biodata.models.variation.VariationPhenotypeAnnotation;
import org.opencb.cellbase.app.transform.utils.VariationUtils;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

/**
 * Created by imedina on 12/01/14.
 */
public class VariationPhenotypeAnnotationParser extends CellBaseParser {


    private static final int CHUNK_SIZE = 1000;
    private int LIMIT_ROWS = 100000;

    private RandomAccessFile raf;
    private Connection sqlConn = null;
    private PreparedStatement prepStmVariationFeature;

    private ObjectMapper jsonObjectMapper;
    private ObjectWriter jsonObjectWriter;

    private Path ensemblVariationDir;

    public VariationPhenotypeAnnotationParser(Path ensemblVariationDir, CellBaseSerializer serializer) {
        super(serializer);
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonObjectWriter = jsonObjectMapper.writer();

        this.ensemblVariationDir = ensemblVariationDir;
    }


    // Ensembl: phenotype_feature_id 0| phenotype_id 1| source_id 2| study_id 3| type 4| object_id 5      | is_significant 6|
    // seq_region_id 7| seq_region_start 8| seq_region_end 9| seq_region_strand 10
    @Override
    public void parse() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        Map<String, String> seqRegionMap = VariationUtils.parseSeqRegionToMap(ensemblVariationDir);
        Map<String, String> phenotypeMap = VariationUtils.parsePhenotypeToMap(ensemblVariationDir);
        Map<String, String> sourceMap = VariationUtils.parseSourceToMap(ensemblVariationDir);
        Map<String, String> studyMap = VariationUtils.parseStudyToMap(ensemblVariationDir);
        Map<String, String> attribTypeMap = VariationUtils.parseAttribTypeToMap(ensemblVariationDir);

        Map<String, Set<String>> phenotypeToGeneList = new HashMap<>(20000);

        prepare(ensemblVariationDir);
        createPhenFeatAttribTable(ensemblVariationDir);
        raf = new RandomAccessFile(ensemblVariationDir.resolve("phenotype_feature_attrib.txt").toFile(), "r");

        VariationPhenotypeAnnotation mutation;
        String seqRegion = null;
        String phenotype = null;
        String source = null;
        String study = null;
        String externalId = null;

        String clinSignificance = null;
        List<String> associatedGenes = new ArrayList<>();
        String riskAllele = null;
        float pValue = 0.0f;
        float oddsRatio = 0.0f;

        String chunkIdSuffix = CHUNK_SIZE / 1000 + "k";

        BufferedReader br = FileUtils.newBufferedReader(ensemblVariationDir.resolve("phenotype_feature.txt.gz"), Charset.defaultCharset());
        String[] fields = null;
        String[] rafFields = null;
        String line = null;
        int count = 0;
        while ((line = br.readLine()) != null) {
            fields = line.split("\t");

            if (fields[4].equals("Variation")) {
                seqRegion = seqRegionMap.get(fields[7]);
                phenotype = phenotypeMap.get(fields[1]);
                source = sourceMap.get(fields[2]).split(",")[0];
                study = studyMap.get(fields[3]);
// anyadir type: variation, SV, ...

                clinSignificance = "";
                externalId = "";
                associatedGenes = new ArrayList<>();
                riskAllele = "";
                pValue = -1f;
                oddsRatio = -1f;
                List<String> resultPhenAnnot = queryByVariationId(Integer.parseInt(fields[0]), ensemblVariationDir);
                if (++count % 10000 == 0) {
                    System.out.println(resultPhenAnnot);
//                    if(count > 200000) break;
                }
                for (String result : resultPhenAnnot) {
                    rafFields = result.split("\t", -1);
                    switch (rafFields[1]) {
                        case "10":
                            clinSignificance = rafFields[2];
                            break;
                        case "13":
                            associatedGenes = Splitter.on(",").trimResults().splitToList(rafFields[2]);
                            break;
                        case "14":  // riskAllele
                            riskAllele = rafFields[2];
                            break;
                        case "15":  // pValue
                            pValue = (rafFields[2] != null && !rafFields[2].equalsIgnoreCase("null"))
                                    ? Float.parseFloat(rafFields[2])
                                    : -1f;
                            break;
                        case "22":  // pValue
                            externalId = rafFields[2];
                            break;
                        case "23":  // pValue
                            oddsRatio = Float.parseFloat(rafFields[2]);
                            break;
                        default:
                            break;
                    }

                }

                if (!phenotypeToGeneList.containsKey(phenotype)) {
                    phenotypeToGeneList.put(phenotype, new HashSet<String>());
                }
                phenotypeToGeneList.get(phenotype).addAll(associatedGenes);

                mutation = new VariationPhenotypeAnnotation(fields[5], seqRegion, Integer.parseInt(fields[8]),
                        Integer.parseInt(fields[9]), fields[10], phenotype, source, study, clinSignificance, associatedGenes,
                        riskAllele, pValue, oddsRatio, "", externalId);
                int chunkStart = (mutation.getStart()) / CHUNK_SIZE;
                int chunkEnd = (mutation.getEnd()) / CHUNK_SIZE;

                if (mutation.getChunkIds() == null) {
                    mutation.setChunkIds(new ArrayList<String>());
                }
                for (int i = chunkStart; i <= chunkEnd; i++) {
                    mutation.getChunkIds().add(mutation.getChromosome() + "_" + i + "_" + chunkIdSuffix);
                }
                serializer.serialize(mutation);
            }
        }
        raf.close();
        br.close();

        BufferedWriter bw = Files.newBufferedWriter(Paths.get("/tmp/phenotype.json"), Charset.defaultCharset());
//        Iterator<String> iter = phenotypeToGeneList.keySet().iterator();
//        for (Object o : iter) {
//            jsonObjectWriter.writeValueAsString(phenotypeToGeneList);
//        }
        for (Map.Entry<String, Set<String>> elem : phenotypeToGeneList.entrySet()) {
            bw.write(jsonObjectWriter.writeValueAsString(elem)
                    .replace("\"key\"", "\"phenotype\"")
                    .replace("\"value\"", "\"associatedGenes\""));
            bw.newLine();
        }
        bw.close();

        clean(ensemblVariationDir);
    }

    public List<String> queryByVariationId(int variationId, Path variationFilePath) throws IOException, SQLException {
        // First query SQLite to get offset position
        List<Long> offsets = new ArrayList<>();
        // PreparedStatement pst = sqlConn.statement(sql)
        // ResultSet rs = pst.executeQuery("select offset from "+tableName+" where variation_id = " + variationId + "");
        ResultSet rs = null;

        prepStmVariationFeature.setInt(1, variationId);
        rs = prepStmVariationFeature.executeQuery();

        while (rs.next()) {
            offsets.add(rs.getLong(1));
        }
        Collections.sort(offsets);
        // Second go to file
        String line = null;
        List<String> results = new ArrayList<>();
        if (offsets.size() > 0) {
//            RandomAccessFile raf = new RandomAccessFile(variationFilePath.resolve("phenotype_feature_attrib.txt").toFile(), "r");
            for (Long offset : offsets) {
                if (offset >= 0) {
                    raf.seek(offset);
                    line = raf.readLine();
                    if (line != null) {
                        results.add(line);
                    }
                }
            }
//            raf.close();
        }
//        System.out.println(results.toString());
        return results;
    }


    private void createPhenFeatAttribTable(Path variationDirectoryPath) throws SQLException, IOException, ClassNotFoundException {
        String tableName = "phen_feat_attrib";
        Class.forName("org.sqlite.JDBC");
        sqlConn = DriverManager.getConnection("jdbc:sqlite:" + variationDirectoryPath.resolve("variation_phenotype.db").toString());
        if (!Files.exists(variationDirectoryPath.resolve("variation_phenotype.db"))
                || Files.size(variationDirectoryPath.resolve("variation_phenotype.db")) == 0) {
            sqlConn.setAutoCommit(false);

            Statement createTables = sqlConn.createStatement();

            // A table containing offset for files
            createTables.executeUpdate("CREATE TABLE if not exists " + tableName + "(" + "variation_id INT , offset BIGINT)");
            PreparedStatement ps = sqlConn.prepareStatement("INSERT INTO " + tableName + "(variation_id, offset) values (?, ?)");

            long offset = 0;
            int count = 0;
            String[] fields;
            String line;
            BufferedReader br = FileUtils.newBufferedReader(variationDirectoryPath.resolve("phenotype_feature_attrib.txt"));
            while ((line = br.readLine()) != null) {
                fields = line.split("\t");

                ps.setInt(1, Integer.parseInt(fields[0])); // motif_feature_id
                ps.setLong(2, offset); // seq_region_id
                ps.addBatch();
                count++;

                if (count % LIMIT_ROWS == 0 && count != 0) {
                    ps.executeBatch();
                    sqlConn.commit();
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
        prepStmVariationFeature = sqlConn
                .prepareStatement("select offset from " + tableName + " where variation_id = ? order by offset ASC ");
    }

    private void prepare(Path variationDirectoryPath) throws IOException, InterruptedException {
        if (Files.exists(variationDirectoryPath.resolve("phenotype_feature_attrib.txt.gz"))) {
            Process process = Runtime.getRuntime().exec("gunzip "
                    + variationDirectoryPath.resolve("phenotype_feature_attrib.txt.gz").toAbsolutePath());
            process.waitFor();
        }
    }

    private void clean(Path variationDirectoryPath) throws IOException, InterruptedException {
        if (Files.exists(variationDirectoryPath.resolve("phenotype_feature_attrib.txt"))) {
            Process process = Runtime.getRuntime().exec("gzip "
                    + variationDirectoryPath.resolve("phenotype_feature_attrib.txt").toAbsolutePath());
            process.waitFor();
        }
    }

}
