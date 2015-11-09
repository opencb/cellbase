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

import org.opencb.cellbase.core.common.GenomicPositionScore;
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

    private static int CHUNK_SIZE = 1000;
    private static final int DECIMAL_RESOLUTION = 1000;

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

        String[] nucleotides = new String[]{"A", "C", "G", "T"};
        long rawLongValue = 0;
        long scaledLongValue = 0;
        Map<String, Float> rawScoreValuesMap = new HashMap<>();
        Map<String, Float> scaledScoreValuesMap = new HashMap<>();
        while ((line = bufferedReader.readLine()) != null) {
            if (!line.startsWith("#")) {
                fields = line.split("\t");

                rawScoreValuesMap.put(fields[3], Float.valueOf(fields[4]));
                scaledScoreValuesMap.put(fields[3], Float.valueOf(fields[5]));

                if (++lineCount == 3) {
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

                    rawValues.add(rawLongValue);
                    scaledValues.add(scaledLongValue);

                    counter++;
                    rawLongValue = 0;
                    lineCount = 0;
                    rawScoreValuesMap.clear();
                    scaledScoreValuesMap.clear();
                }

                if (counter == CHUNK_SIZE) {
                    // both raw and scaled are serialized
                    GenomicPositionScore genomicPositionScore = new GenomicPositionScore(fields[0], start, end, "cadd_raw", rawValues);
                    serializer.serialize(genomicPositionScore);

                    genomicPositionScore = new GenomicPositionScore(fields[0], start, end, "cadd_scaled", scaledValues);
                    serializer.serialize(genomicPositionScore);

                    start = end + 1;
                    end += CHUNK_SIZE;

                    counter = 0;
                    rawValues.clear();
                    scaledValues.clear();
                }
            }
        }

        // Last chunks can be incomplete for both raw and scaled are serialized
        GenomicPositionScore genomicPositionScore = new GenomicPositionScore(fields[0], start, start + rawValues.size() - 1, "cadd_raw", rawValues);
        serializer.serialize(genomicPositionScore);

        genomicPositionScore = new GenomicPositionScore(fields[0], start, start + scaledValues.size() - 1, "cadd_scaled", scaledValues);
        serializer.serialize(genomicPositionScore);

        serializer.close();
        bufferedReader.close();
    }
}
