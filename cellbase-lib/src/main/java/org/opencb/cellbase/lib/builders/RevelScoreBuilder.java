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


import org.opencb.biodata.models.core.MissenseVariantFunctionalScore;
import org.opencb.biodata.models.core.TranscriptMissenseVariantFunctionalScore;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class RevelScoreBuilder extends AbstractBuilder {

    private Path revelDownloadPath = null;

    public RevelScoreBuilder(Path revelDownloadPath, CellBaseSerializer serializer) {
        super(serializer);
        this.revelDownloadPath = revelDownloadPath;
    }

    @Override
    public void parse() throws IOException, CellBaseException {
        String dataName = getDataName(REVEL_DATA);
        String dataCategory = getDataCategory(REVEL_DATA);

        logger.info(CATEGORY_BUILDING_LOG_MESSAGE, dataCategory, dataName);

        // Sanity check
        checkDirectory(revelDownloadPath, dataName);

        // Check ontology files
        List<File> revelFiles = checkFiles(dataSourceReader.readValue(revelDownloadPath.resolve(getDataVersionFilename(REVEL_DATA))
                        .toFile()), revelDownloadPath, dataName);
        if (revelFiles.size() != 1) {
            throw new CellBaseException("One " + dataName + " file is expected, but currently there are " + revelFiles.size() + " files");
        }

        logger.info(PARSING_LOG_MESSAGE, revelFiles.get(0));

        ZipInputStream zis = new ZipInputStream(new FileInputStream(String.valueOf(revelFiles.get(0))));
        ZipEntry zipEntry = zis.getNextEntry();

        ZipFile zipFile = new ZipFile(revelFiles.get(0).toString());
        InputStream inputStream = zipFile.getInputStream(zipEntry);
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            // Skip header
            bufferedReader.readLine();
            String[] fields;
            String lastEntry = null;
            String currentEntry;
            List<TranscriptMissenseVariantFunctionalScore> scores = new ArrayList<>();
            MissenseVariantFunctionalScore predictions = null;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                fields = line.split(",");
                String chromosome = fields[0];
                if (".".equalsIgnoreCase(fields[2])) {
                    // 1,12855835,.,C,A,A,D,0.175
                    // skip if invalid position
                    continue;
                }
                int position = Integer.parseInt(fields[2]);
                String reference = fields[3];
                String alternate = fields[4];
                String aaReference = fields[5];
                String aaAlternate = fields[6];
                double score = Double.parseDouble(fields[7]);

                currentEntry = chromosome + position;

                // new chromosome + position, store previous entry
                if (lastEntry != null && !currentEntry.equals(lastEntry)) {
                    serializer.serialize(predictions);
                    scores = new ArrayList<>();
                    predictions = null;
                }

                if (predictions == null) {
                    predictions = new MissenseVariantFunctionalScore(chromosome, position, reference, REVEL_DATA, scores);
                }

                TranscriptMissenseVariantFunctionalScore predictedScore = new TranscriptMissenseVariantFunctionalScore("", alternate,
                        aaReference, aaAlternate, score);
                scores.add(predictedScore);
                lastEntry = chromosome + position;
            }

            // Serialise last entry
            serializer.serialize(predictions);
        }

        logger.info(PARSING_DONE_LOG_MESSAGE, revelFiles.get(0));

        // Close
        zis.close();
        zipFile.close();
        inputStream.close();

        logger.info(CATEGORY_BUILDING_DONE_LOG_MESSAGE, dataCategory, dataName);
    }
}
