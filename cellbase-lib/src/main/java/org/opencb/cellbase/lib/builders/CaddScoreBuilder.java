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

import org.opencb.biodata.models.core.GenomicScoreRegion;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opencb.cellbase.lib.EtlCommons.*;

/**
 * Created by imedina on 06/11/15.
 */
public class CaddScoreBuilder extends CellBaseBuilder {

    private Path caddDownloadPath;

    private static final int CHUNK_SIZE = 1000;
    private static final int DECIMAL_RESOLUTION = 100;

    public CaddScoreBuilder(Path caddDownloadPath, CellBaseSerializer serializer) {
        super(serializer);
        this.caddDownloadPath = caddDownloadPath;
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
        String dataName = getDataName(CADD_DATA);
        String dataCategory = getDataCategory(CADD_DATA);

        logger.info(CATEGORY_BUILDING_LOG_MESSAGE, dataCategory, dataName);

        // Sanity check
        checkDirectory(caddDownloadPath, dataName);

        // Check ontology files
        List<File> caddFiles = checkFiles(dataSourceReader.readValue(caddDownloadPath.resolve(getDataVersionFilename(CADD_DATA)).toFile()),
                caddDownloadPath, dataName);
        if (caddFiles.size() != 1) {
            throw new CellBaseException("One " + dataName + " file is expected, but currently there are " + caddFiles.size() + " files");
        }

        List<Long> rawValues = new ArrayList<>(CHUNK_SIZE);
        List<Long> scaledValues = new ArrayList<>(CHUNK_SIZE);

        int start = 1;
        int end = CHUNK_SIZE - 1;
        String line;
        String[] fields = new String[0];
        short v;
        int lineCount = 0;
        int counter = 1;
        int serializedChunks = 0;
        int prevPos = 0;
        int newPos = 0;
        String chromosome = null;

        String[] nucleotides = new String[]{"A", "C", "G", "T"};
        long rawLongValue = 0;
        long scaledLongValue = 0;
        Map<String, Float> rawScoreValuesMap = new HashMap<>();
        Map<String, Float> scaledScoreValuesMap = new HashMap<>();

        logger.info(PARSING_LOG_MESSAGE, caddFiles.get(0));
        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(caddFiles.get(0).toPath())) {
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    fields = line.split("\t");
                    newPos = Integer.parseInt(fields[1]);
                    String message = "chrom. " + fields[0];
                    // This only happens the first time, when we start reading the file
                    if (chromosome == null) {
                        logger.info(PARSING_LOG_MESSAGE, message);
                        chromosome = fields[0];

                        start = newPos;
                        prevPos = newPos;
                        end = start + CHUNK_SIZE - 2;
                    }

                    if (!chromosome.equals(fields[0])) {
                        logger.info(PARSING_LOG_MESSAGE, message);

                        // Both raw and scaled are serialized
                        GenomicScoreRegion<Long> genomicScoreRegion = new GenomicScoreRegion<>(chromosome, start, prevPos, CADD_RAW_DATA,
                                rawValues);
                        serializer.serialize(genomicScoreRegion);

                        genomicScoreRegion = new GenomicScoreRegion<>(chromosome, start, prevPos, CADD_SCALED_DATA, scaledValues);
                        serializer.serialize(genomicScoreRegion);

                        serializedChunks++;
                        chromosome = fields[0];
                        start = newPos;
                        end = start + CHUNK_SIZE - 2;

                        counter = 0;
                        rawValues.clear();
                        scaledValues.clear();
                        // The series of cadd scores is not continuous through the whole chromosome
                    } else if (end < newPos || (newPos - prevPos) > 1) {
                        // Both raw and scaled are serialized
                        GenomicScoreRegion<Long> genomicScoreRegion = new GenomicScoreRegion<>(fields[0], start, prevPos, CADD_RAW_DATA,
                                rawValues);
                        serializer.serialize(genomicScoreRegion);

                        genomicScoreRegion = new GenomicScoreRegion<>(fields[0], start, prevPos, CADD_SCALED_DATA, scaledValues);
                        serializer.serialize(genomicScoreRegion);

                        serializedChunks++;
                        start = newPos;
                        end = (start / CHUNK_SIZE) * CHUNK_SIZE + CHUNK_SIZE - 1;

                        counter = 0;
                        rawValues.clear();
                        scaledValues.clear();
                    }

                    rawScoreValuesMap.put(fields[3], Float.valueOf(fields[4]));
                    scaledScoreValuesMap.put(fields[3], Float.valueOf(fields[5]));

                    if (++lineCount == 3) {
                        for (String nucleotide : nucleotides) {
                            // Raw CADD score values can be negative, we add 10 to make positive
                            float a = rawScoreValuesMap.getOrDefault(nucleotide, 10f) + 10.0f;
                            v = (short) (a * DECIMAL_RESOLUTION);
                            rawLongValue = (rawLongValue << 16) | v;

                            // Scaled CADD scores are always positive
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
                    prevPos = newPos;
                }
            }

            // Last chunks can be incomplete for both raw and scaled are serialized
            GenomicScoreRegion<Long> genomicScoreRegion = new GenomicScoreRegion<>(fields[0], start, newPos, CADD_RAW_DATA, rawValues);
            serializer.serialize(genomicScoreRegion);

            genomicScoreRegion = new GenomicScoreRegion<>(fields[0], start, newPos, CADD_SCALED_DATA, scaledValues);
            serializer.serialize(genomicScoreRegion);

            serializer.close();
        }
        logger.info(PARSING_DONE_LOG_MESSAGE, caddFiles.get(0));

        logger.info(CATEGORY_BUILDING_DONE_LOG_MESSAGE, dataCategory, dataName);
    }
}
