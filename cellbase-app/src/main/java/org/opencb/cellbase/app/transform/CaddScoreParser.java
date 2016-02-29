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

import org.opencb.biodata.models.core.GenomicScoreRegion;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by imedina on 06/11/15.
 */
public class CaddScoreParser extends CellBaseParser {

    private Path caddFilePath;

    private static final int CHUNK_SIZE = 1000;
    private static final int DECIMAL_RESOLUTION = 100;

    public CaddScoreParser(Path caddFilePath, CellBaseSerializer serializer) {
        super(serializer);
        this.caddFilePath = caddFilePath;

        logger = LoggerFactory.getLogger(ConservationParser.class);
    }

    /* Example:
        ## CADD v1.3 (c) University of Washington and Hudson-Alpha Institute for Biotechnology 2013-2015. All rights reserved.
        #Chrom  Pos     Ref     Alt     RawScore        PHRED
        1       10001   T       A       0.337036        6.046
        1       10001   T       C       0.143254        4.073
        1       10001   T       G       0.202491        4.705
        1       10002   A       C       0.192576        4.601
        1       10002   A       G       0.178363        4.450
        1       10002   A       T       0.347401        6.143
    */
    @Override
    public void parse() throws Exception {
        FileUtils.checkPath(caddFilePath);

        BufferedReader bufferedReader = FileUtils.newBufferedReader(caddFilePath);
        List<Long> rawValues = new ArrayList<>(CHUNK_SIZE);
        List<Long> scaledValues = new ArrayList<>(CHUNK_SIZE);

        int start = 1;
//        int end = 1999;
        int end = CHUNK_SIZE - 1;
        String line;
        String[] fields = new String[0];
        short v;
        int lineCount = 0;
        int counter = 1;
        int serializedChunks = 0;
        int previousPosition = 0;
        int newPosition = 0;
        String chromosome = null;

        String[] nucleotides = new String[]{"A", "C", "G", "T"};
        long rawLongValue = 0;
        long scaledLongValue = 0;
        Map<String, Float> rawScoreValuesMap = new HashMap<>();
        Map<String, Float> scaledScoreValuesMap = new HashMap<>();
        while ((line = bufferedReader.readLine()) != null) {
            if (!line.startsWith("#")) {
                fields = line.split("\t");
                newPosition = Integer.parseInt(fields[1]);
//                if (fields[0].equals("1") && fields[1].equals("249240621")) {
//                if (fields[0].equals("1") && fields[1].equals("69100")) {
//                if (fields[0].equals("1") && fields[1].equals("144854598")) {
//                    logger.debug("line {} reached", line);
//                    logger.debug("Associated chunk count {}", serializedChunks);
//                    logger.debug("start {}", start);
//                    logger.debug("end {}", end);
//                    logger.debug("chunk size {}", CHUNK_SIZE);
//                }
                // this only happens the first time, when we start reading the file
                if (chromosome == null) {
                    logger.info("Parsing chr {} ", fields[0]);
                    chromosome = fields[0];

                    start = newPosition;
                    previousPosition = newPosition;
                    end = start + CHUNK_SIZE - 2;
                }

                if (!chromosome.equals(fields[0])) {
                    logger.info("Parsing chr {} ", fields[0]);
                    // both raw and scaled are serialized
                    GenomicScoreRegion<Long> genomicScoreRegion =
                            new GenomicScoreRegion<>(chromosome, start, previousPosition, "cadd_raw", rawValues);
                    serializer.serialize(genomicScoreRegion);

                    genomicScoreRegion = new GenomicScoreRegion<>(chromosome, start, previousPosition, "cadd_scaled", scaledValues);
                    serializer.serialize(genomicScoreRegion);

                    serializedChunks++;
                    chromosome = fields[0];
                    start = newPosition;
//                    end = CHUNK_SIZE - 1;
                    end = start + CHUNK_SIZE - 2;

                    counter = 0;
                    rawValues.clear();
                    scaledValues.clear();
//                    rawLongValue = 0;
//                    lineCount = 0;
//                    rawScoreValuesMap.clear();
//                    scaledScoreValuesMap.clear();
                // The series of cadd scores is not continuous through the whole chromosome
                } else if (end < newPosition || (newPosition - previousPosition) > 1) {
                    // both raw and scaled are serialized
                    GenomicScoreRegion genomicScoreRegion
                            = new GenomicScoreRegion<>(fields[0], start, previousPosition, "cadd_raw", rawValues);
                    serializer.serialize(genomicScoreRegion);

                    genomicScoreRegion
                            = new GenomicScoreRegion<>(fields[0], start, previousPosition, "cadd_scaled", scaledValues);
                    serializer.serialize(genomicScoreRegion);

                    serializedChunks++;
                    start = newPosition;
//                    start = end + 1;
//                    end += CHUNK_SIZE;
                    end = (start / CHUNK_SIZE) * CHUNK_SIZE + CHUNK_SIZE - 1;

                    counter = 0;
                    rawValues.clear();
                    scaledValues.clear();
                }

                rawScoreValuesMap.put(fields[3], Float.valueOf(fields[4]));
                scaledScoreValuesMap.put(fields[3], Float.valueOf(fields[5]));

                if (++lineCount == 3) {
//                    if (fields[0].equals("1") && fields[1].equals("249240621")) {
//                    if (fields[0].equals("1") && fields[1].equals("69100")) {
//                    if (fields[0].equals("1") && fields[1].equals("144854598")) {
//                        logger.info("offset: {}", rawValues.size());
//                    }

                    for (String nucleotide : nucleotides) {
                        // raw CADD score values can be negative, we add 10 to make positive
                        float a = rawScoreValuesMap.getOrDefault(nucleotide, 10f) + 10.0f;
                        v = (short) (a * DECIMAL_RESOLUTION);
                        rawLongValue = (rawLongValue << 16) | v;

                        // scaled CADD scores are always positive
                        a = scaledScoreValuesMap.getOrDefault(nucleotide, 0f);
                        v = (short) (a * DECIMAL_RESOLUTION);
                        scaledLongValue = (scaledLongValue << 16) | v;
                    }

//                    if (rawLongValue < 0 || scaledLongValue < 0) {
//                        logger.error("raw/scaled Long Values cannot be 0");
//                        logger.error("Last read line {}", line);
//                        System.exit(1);
//                    }
                    rawValues.add(rawLongValue);
                    scaledValues.add(scaledLongValue);

                    counter++;
                    rawLongValue = 0;
                    lineCount = 0;
                    rawScoreValuesMap.clear();
                    scaledScoreValuesMap.clear();
                }
                previousPosition = newPosition;
            }
        }

        // Last chunks can be incomplete for both raw and scaled are serialized
//        GenomicScoreRegion<Long> genomicScoreRegion =
//                new GenomicScoreRegion<>(fields[0], start, start + rawValues.size() - 1, "cadd_raw", rawValues);
        GenomicScoreRegion<Long> genomicScoreRegion =
                new GenomicScoreRegion<>(fields[0], start, newPosition, "cadd_raw", rawValues);
        serializer.serialize(genomicScoreRegion);

//        genomicScoreRegion = new GenomicScoreRegion<>(fields[0], start, start + scaledValues.size() - 1, "cadd_scaled", scaledValues);
        genomicScoreRegion = new GenomicScoreRegion<>(fields[0], start, newPosition, "cadd_scaled", scaledValues);
        serializer.serialize(genomicScoreRegion);

        serializer.close();
        bufferedReader.close();
        logger.info("Parsing finished.");
    }
}
