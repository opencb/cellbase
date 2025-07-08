/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.builders;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.ProteinSubstitutionPrediction;
import org.opencb.biodata.models.core.ProteinSubstitutionPredictionScore;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.builders.utils.RocksDBUtils;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class AlphaMissenseBuilder extends AbstractBuilder {

    private Path alphamissenseDownloadPath;
    private CellBaseFileSerializer fileSerializer;

    private RocksDB rdb;

    private String AA_CHANGE_PATTERN = "^([A-Z])(\\d+)([A-Z])$";
    private Pattern aaChangePattern = Pattern.compile(AA_CHANGE_PATTERN);

    private static ObjectMapper mapper;
    private static ObjectReader predictionReader;
    private static ObjectWriter jsonObjectWriter;

    static {
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        predictionReader = mapper.readerFor(ProteinSubstitutionPrediction.class);
        jsonObjectWriter = mapper.writer();
    }

    public AlphaMissenseBuilder(Path alphamissenseDownloadPath, CellBaseFileSerializer serializer) {
        super(serializer);

        this.fileSerializer = serializer;
        this.alphamissenseDownloadPath = alphamissenseDownloadPath;

        logger = LoggerFactory.getLogger(AlphaMissenseBuilder.class);
    }

    @Override
    public void parse() throws Exception {
        String dataName = getDataName(ALPHAMISSENSE_DATA);
        String dataCategory = getDataCategory(ALPHAMISSENSE_DATA);

        logger.info(CATEGORY_BUILDING_LOG_MESSAGE, dataCategory, dataName);

        // Check REVEL files
        List<File> alphamissenseFiles = checkFiles(dataSourceReader.readValue(alphamissenseDownloadPath.resolve(
                getDataVersionFilename(ALPHAMISSENSE_DATA)).toFile()), alphamissenseDownloadPath, dataName);
        if (alphamissenseFiles.size() != 1) {
            throw new CellBaseException("One " + dataName + " file is expected, but currently there are " + alphamissenseFiles.size()
                    + " files");
        }

        logger.info(PARSING_LOG_MESSAGE, alphamissenseFiles.get(0));

        Object[] dbConnection = RocksDBUtils.getDBConnection(serializer.getOutdir().resolve("alphamissense-rdb.idx").toString(), true);
        rdb = (RocksDB) dbConnection[0];
        Options dbOption = (Options) dbConnection[1];
        String dbLocation = (String) dbConnection[2];

        // AlphaMissense file reader
        try (BufferedReader br = FileUtils.newBufferedReader(alphamissenseFiles.get(0).toPath())) {
            String line;
            int counter = 0;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    // 0        1   2   3   4       5           6               7               8                   9
                    // CHROM    POS REF ALT genome  uniprot_id  transcript_id   protein_variant am_pathogenicity    am_class
                    String[] split = line.split("\t", -1);

                    String chrom = null;
                    int position;
                    String reference;
                    String alternate = null;
                    String transcriptId;
                    String uniprotId;
                    int aaPosition;
                    String aaReference;
                    String aaAlternate;

                    if (StringUtils.isNotEmpty(split[0])) {
                        chrom = split[0].replace("chr", "");
                    }
                    if (StringUtils.isNotEmpty(split[1])) {
                        position = Integer.parseInt(split[1]);
                    } else {
                        logger.warn("Missing field 'position', skipping line: {}", line);
                        continue;
                    }
                    if (StringUtils.isNotEmpty(split[2])) {
                        reference = split[2];
                    } else {
                        logger.warn("Missing field 'reference', skipping line: {}", line);
                        continue;
                    }
                    if (StringUtils.isNotEmpty(split[3])) {
                        alternate = split[3];
                    }
                    if (StringUtils.isNotEmpty(split[6])) {
                        transcriptId = split[6].split("\\.")[0];
                    } else {
                        logger.warn("Missing field 'transcript_id', skipping line: {}", line);
                        continue;
                    }
                    if (StringUtils.isNotEmpty(split[5])) {
                        uniprotId = split[5];
                    } else {
                        logger.warn("Missing field 'uniprot_id', skipping line: {}", line);
                        continue;
                    }
                    if (StringUtils.isNotEmpty(split[7])) {
                        Matcher matcher = aaChangePattern.matcher(split[7]);
                        if (matcher.matches()) {
                            aaReference = matcher.group(1);
                            aaPosition = Integer.parseInt(matcher.group(2));
                            aaAlternate = matcher.group(3);
                        } else {
                            logger.warn("Error parsing field 'protein_variant' = {}, skipping line: {}", split[7], line);
                            continue;
                        }
                    } else {
                        logger.warn("Missing field 'protein_variant', skipping line: {}", line);
                        continue;
                    }

                    // Create protein substitution score
                    ProteinSubstitutionPredictionScore score = new ProteinSubstitutionPredictionScore();
                    score.setAlternate(alternate);
                    score.setAaAlternate(aaAlternate);
                    if (StringUtils.isNotEmpty(split[8])) {
                        score.setScore(Double.parseDouble(split[8]));
                    }
                    if (StringUtils.isNotEmpty(split[9])) {
                        score.setEffect(split[9]);
                    }

                    // Creating and/or updating protein substitution prediction
                    ProteinSubstitutionPrediction prediction;
                    String key = transcriptId + "_" + uniprotId + "_" + position + "_" + reference + "_" + aaPosition + "_" + aaReference;
                    byte[] dbContent = rdb.get(key.getBytes());
                    if (dbContent == null) {
                        prediction = new ProteinSubstitutionPrediction(chrom, position, reference, transcriptId, uniprotId, aaPosition,
                                aaReference, ALPHAMISSENSE_DATA, null, Collections.singletonList(score));
                    } else {
                        prediction = predictionReader.readValue(dbContent);
                        prediction.getScores().add(score);
                    }
                    rdb.put(key.getBytes(), jsonObjectWriter.writeValueAsBytes(prediction));

                    // Log messages
                    counter++;
                    if (counter % 10000 == 0) {
                        logger.info("{} AlphaMissense predictions parsed", counter);
                    }
                }
            }
        }

        // Serialize/write the saved variant polygenic scores in the RocksDB
        serializeRDB(rdb);
        RocksDBUtils.closeIndex(rdb, dbOption, dbLocation);
        serializer.close();

        logger.info(PARSING_DONE_LOG_MESSAGE);
        logger.info(CATEGORY_BUILDING_DONE_LOG_MESSAGE);
    }

    private void serializeRDB(RocksDB rdb) throws IOException {
        // DO NOT change the name of the rocksIterator variable - for some unexplainable reason Java VM crashes if it's
        // named "iterator"
        RocksIterator rocksIterator = rdb.newIterator();

        logger.info("Reading from RocksDB index and serializing to {}.json.gz", serializer.getOutdir().resolve(serializer.getFileName()));
        int counter = 0;
        for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
//            logger.info("variant = {}", new String(rocksIterator.key()));
            ProteinSubstitutionPrediction prediction = predictionReader.readValue(rocksIterator.value());
            serializer.serialize(prediction);
            counter++;
            if (counter % 10000 == 0) {
                logger.info("{} written", counter);
            }
        }
        serializer.close();
        logger.info(EtlCommons.DONE_MSG);
    }
}
