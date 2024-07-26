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

import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.commons.ProgressLogger;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

/**
 * Created by fjlopez on 05/05/17.
 */
public class RepeatsBuilder extends AbstractBuilder {

    private CellBaseConfiguration configuration;

    private List<String> dataList;
    private final Path filesDir;

    public static final String REPEATS_OUTPUT_FILENAME = EtlCommons.REPEATS_DATA + ".json.gz";

    public RepeatsBuilder(List<String> dataList, Path filesDir, CellBaseFileSerializer serializer, CellBaseConfiguration configuration) {
        super(serializer);
        this.dataList = dataList;
        this.filesDir = filesDir;
        this.configuration = configuration;
    }


    @Override
    public void parse() throws Exception {
        // Sanity check
        checkDirectory(filesDir, getDataName(REPEATS_DATA));

        // Check Simple Repeats (TRF) filename
        String trfFilename = null;
        if (dataList.contains(TRF_DATA)) {
            trfFilename = Paths.get(configuration.getDownload().getSimpleRepeats().getFiles().get(SIMPLE_REPEATS_FILE_ID)).getFileName()
                    .toString();
            if (!Files.exists(filesDir.resolve(trfFilename))) {
                throw new CellBaseException(getMessageMissingFile(TRF_DATA, trfFilename, filesDir));
            }
        }

        // Check Genomic Super Duplications (GSD) file
        String gsdFilename = null;
        if (dataList.contains(GSD_DATA)) {
            gsdFilename = Paths.get(configuration.getDownload().getGenomicSuperDups().getFiles().get(GENOMIC_SUPER_DUPS_FILE_ID))
                    .getFileName().toString();
            if (!Files.exists(filesDir.resolve(gsdFilename))) {
                throw new CellBaseException(getMessageMissingFile(GSD_DATA, gsdFilename, filesDir));
            }
        }

        // Check Window Masker (WM) file
        String wmFilename = null;
        if (dataList.contains(WM_DATA)) {
            wmFilename = Paths.get(configuration.getDownload().getWindowMasker().getFiles().get(WINDOW_MASKER_FILE_ID)).getFileName()
                    .toString();
            if (!Files.exists(filesDir.resolve(wmFilename))) {
                throw new CellBaseException(getMessageMissingFile(WM_DATA, wmFilename, filesDir));
            }
        }

        // Parse TRF file
        if (dataList.contains(TRF_DATA)) {
            logger.info(PARSING_LOG_MESSAGE, getDataName(TRF_DATA));
            parseTrfFile(filesDir.resolve(trfFilename));
        }

        // Parse GSD file
        if (dataList.contains(GSD_DATA)) {
            logger.info(PARSING_LOG_MESSAGE, getDataName(GSD_DATA));
            parseGsdFile(filesDir.resolve(gsdFilename));
        }

        // Parse WM file
        if (dataList.contains(WM_DATA)) {
            logger.info(PARSING_LOG_MESSAGE, getDataName(WM_DATA));
            parseWmFile(filesDir.resolve(wmFilename));
        }
    }

    private void parseTrfFile(Path filePath) throws IOException, CellBaseException {
        logger.info(PARSING_LOG_MESSAGE, filePath);
        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(filePath)) {
            String line = bufferedReader.readLine();

            ProgressLogger progressLogger = new ProgressLogger(getMessageParsedLines(TRF_DATA), () -> EtlCommons.countFileLines(filePath),
                    200).setBatchSize(10000);
            while (line != null) {
                serializer.serialize(parseTrfLine(line));
                line = bufferedReader.readLine();
                progressLogger.increment(1);
            }
        }
        logger.info(PARSING_DONE_LOG_MESSAGE);
    }

    private Repeat parseTrfLine(String line) {
        String[] parts = line.split("\t");

        return new Repeat(null, Region.normalizeChromosome(parts[1]), Integer.valueOf(parts[2]) + 1,
                Integer.valueOf(parts[3]), Integer.valueOf(parts[5]), Integer.valueOf(parts[7]),
                Float.valueOf(parts[6]), Float.valueOf(parts[8]) / 100, Float.valueOf(parts[10]), parts[16], TRF_DATA);
    }

    private void parseGsdFile(Path filePath) throws IOException, CellBaseException {
        logger.info(PARSING_LOG_MESSAGE, filePath);
        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(filePath)) {
            String line = bufferedReader.readLine();

            ProgressLogger progressLogger = new ProgressLogger(getMessageParsedLines(GSD_DATA), () -> EtlCommons.countFileLines(filePath),
                    200).setBatchSize(10000);
            while (line != null) {
                serializer.serialize(parseGSDLine(line));
                line = bufferedReader.readLine();
                progressLogger.increment(1);
            }
        }
        logger.info(PARSING_DONE_LOG_MESSAGE);
    }

    private Repeat parseGSDLine(String line) {
        String[] parts = line.split("\t");

        return new Repeat(parts[11], Region.normalizeChromosome(parts[1]), Integer.valueOf(parts[2]) + 1,
                Integer.valueOf(parts[3]), null, null, 2f, Float.valueOf(parts[26]), null,
                null, GSD_DATA);

    }

    private void parseWmFile(Path filePath) throws IOException, CellBaseException {
        logger.info(PARSING_LOG_MESSAGE, filePath);
        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(filePath)) {
            String line = bufferedReader.readLine();

            ProgressLogger progressLogger = new ProgressLogger(getMessageParsedLines(WM_DATA), () -> EtlCommons.countFileLines(filePath),
                    200).setBatchSize(10000);
            while (line != null) {
                serializer.serialize(parseWmLine(line));
                line = bufferedReader.readLine();
                progressLogger.increment(1);
            }
        }
        logger.info(PARSING_DONE_LOG_MESSAGE);
    }

    private Repeat parseWmLine(String line) {
        String[] parts = line.split("\t");

        return new Repeat(parts[4].replace("\t", ""), Region.normalizeChromosome(parts[1]),
                Integer.valueOf(parts[2]) + 1, Integer.valueOf(parts[3]), null, null, null, null, null, null, WM_DATA);
    }

    private String getMessageMissingFile(String data, String filename, Path folder) throws CellBaseException {
        return getDataName(data) + " file " + filename + " does not exist at " + folder;
    }

    private String getMessageParsedLines(String data) throws CellBaseException {
        return "Parsed " + getDataName(data) + " lines:";
    }
}

