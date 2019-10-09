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

package org.opencb.cellbase.app.transform.variation;

import com.google.common.base.Stopwatch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by parce on 09/12/15.
 */
public class VariationTranscriptFile extends AbstractVariationFile {

    public static final String TRANSCRIPT_VARIATION_FILENAME = "transcript_variation.txt";
    public static final String PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME = "transcript_variation.includingVariationId.txt";

    public VariationTranscriptFile(Path variationDirectory) {
        super(variationDirectory, TRANSCRIPT_VARIATION_FILENAME, PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME);
    }


    public void preprocess(VariationFeatureFile variationFeatureFile) throws IOException, InterruptedException {
        if (!existsZippedOrUnzippedFile(preprocessedFileName)) {
            logger.info("Preprocessing {} file ...", preprocessedFileName);
            Stopwatch stopwatch = Stopwatch.createStarted();

            // add variationId to transcript_variation file
            Map<Integer, Integer> variationFeatureToVariationId = createVariationFeatureIdToVariationIdMap(variationFeatureFile);
            Path transcriptVariationTempFile = addVariationIdToTranscriptVariationFile(variationFeatureToVariationId);
            // transcript_variation file columns number can vary, so we need to save the variationId column index because it is not constant
            variationIdColumnIndex =
                    getVariationIdColumnIndexInTranscriptVariationFile(transcriptVariationTempFile.getFileName().toString());
            sortFileByNumericColumn(transcriptVariationTempFile, variationDirectory.resolve(preprocessedFileName), variationIdColumnIndex);

            logger.info("Removing temp file {}", transcriptVariationTempFile);
            transcriptVariationTempFile.toFile().delete();
            logger.info("Removed");

            logger.info("{} preprocessed. New file {} including (and sorted by) variation Id has been created",
                    TRANSCRIPT_VARIATION_FILENAME, VariationTranscriptFile.PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME);
            logger.debug("Elapsed time preprocessing transcript variation file: {}", stopwatch);
        } else {
            // transcript_variation file columns number can vary, so we need to save the variationId column index because it is not constant
            variationIdColumnIndex = getVariationIdColumnIndexInTranscriptVariationFile(preprocessedFileName);
        }
    }

    private Map<Integer, Integer> createVariationFeatureIdToVariationIdMap(VariationFeatureFile variationFeatureFile) throws IOException {
        logger.info("Creating variationFeatureId to variationId map ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<Integer, Integer> variationFeatureToVariationId = new HashMap<>();

        BufferedReader variationFeatFileReader = variationFeatureFile.getUnprocessedFileBufferedReader();

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

        logger.info("Done");
        logger.debug("Elapsed time creating variationFeatureId to variationId map: {}", stopwatch);

        return variationFeatureToVariationId;
    }

    private Path addVariationIdToTranscriptVariationFile(Map<Integer, Integer> variationFeatureToVariationId) throws IOException {
        Path transcriptVariationTempFile = variationDirectory.resolve(unprocessedFileName + ".tmp");
        logger.info("Adding variation Id to transcript variations and saving them into {} ...", transcriptVariationTempFile);
        Stopwatch stopwatch = Stopwatch.createStarted();

        BufferedReader br = getUnprocessedFileBufferedReader();
        BufferedWriter bw = Files.newBufferedWriter(transcriptVariationTempFile, Charset.defaultCharset(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);

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

        logger.info("Added");
        logger.debug("Elapsed time adding variation Id to transcript variation file: {}", stopwatch);

        return transcriptVariationTempFile;
    }

    private int getVariationIdColumnIndexInTranscriptVariationFile(String inputFileName) throws IOException {
        BufferedReader br = getBufferedReader(inputFileName);
        int variationIdColumIndex = br.readLine().split("\t").length - 1;
        br.close();
        return variationIdColumIndex;
    }
}
