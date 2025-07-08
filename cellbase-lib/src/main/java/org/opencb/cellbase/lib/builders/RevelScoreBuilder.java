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


import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.ProteinSubstitutionPrediction;
import org.opencb.biodata.models.core.ProteinSubstitutionPredictionScore;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class RevelScoreBuilder extends AbstractBuilder {

    private Path revelDownloadPath;

    public RevelScoreBuilder(Path revelDownloadPath, CellBaseSerializer serializer) {
        super(serializer);
        this.revelDownloadPath = revelDownloadPath;
        logger = LoggerFactory.getLogger(RevelScoreBuilder.class);

    }

    @Override
    public void parse() throws IOException, CellBaseException {
        String dataName = getDataName(REVEL_DATA);
        String dataCategory = getDataCategory(REVEL_DATA);

        logger.info(CATEGORY_BUILDING_LOG_MESSAGE, dataCategory, dataName);

        // Check REVEL files
        List<File> revelFiles = checkFiles(dataSourceReader.readValue(revelDownloadPath.resolve(getDataVersionFilename(REVEL_DATA))
                .toFile()), revelDownloadPath, dataName);
        if (revelFiles.size() != 1) {
            throw new CellBaseException("One " + dataName + " file is expected, but currently there are " + revelFiles.size() + " files");
        }

        logger.info(PARSING_LOG_MESSAGE, revelFiles.get(0));

        ProteinSubstitutionPrediction prediction = null;

        try (ZipFile zipFile = new ZipFile(revelFiles.get(0))) {
            Enumeration<? extends ZipArchiveEntry> entries = zipFile.getEntries();
            if (!entries.hasMoreElements()) {
                throw new CellBaseException("No zip entry found in " + revelFiles.get(0));
            }
            ZipArchiveEntry entry = entries.nextElement();
            try (InputStream inputStream = zipFile.getInputStream(entry);
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                // Skip header line
                String line = bufferedReader.readLine();
                logger.info("Skipping header line: {}", line);

                String[] fields;
                String lastEntry = null;
                String currentEntry;
                List<ProteinSubstitutionPredictionScore> scores = new ArrayList<>();

                // Main loop, read line by line
                while ((line = bufferedReader.readLine()) != null) {
                    fields = line.split(",");
                    // 0   1        2          3   4   5     6     7     8
                    // chr,hg19_pos,grch38_pos,ref,alt,aaref,aaalt,REVEL,Ensembl_transcriptid
                    // 1,35142,35142,G,A,T,M,0.027,ENST00000417324

                    if (StringUtils.isEmpty(fields[0])) {
                        logger.warn("Missing field 'chr', skipping line: {}", line);
                        continue;
                    }
                    String chromosome = fields[0];
                    if (".".equalsIgnoreCase(fields[2]) || StringUtils.isEmpty(fields[2])) {
                        // Skip line if invalid position
                        logger.warn("Missing field 'grch38_pos', skipping line: {}", line);
                        continue;
                    }
                    int position = Integer.parseInt(fields[2]);
                    String reference = fields[3];
                    String alternate = fields[4];
                    String aaReference = fields[5];
                    String aaAlternate = fields[6];
                    if (StringUtils.isEmpty(fields[7])) {
                        logger.warn("Missing field 'REVEL' (i.e., score value), skipping line: {}", line);
                        continue;
                    }
                    double score = Double.parseDouble(fields[7]);
                    String transcriptId = fields[8];

                    currentEntry = chromosome + ":" + position;

                    // New chromosome + position, store previous entry
                    if (lastEntry != null && !currentEntry.equals(lastEntry)) {
                        serializer.serialize(prediction);
                        scores = new ArrayList<>();
                        prediction = null;
                    }

                    if (prediction == null) {
                        prediction = new ProteinSubstitutionPrediction(chromosome, position, reference, transcriptId, null, 0, aaReference,
                                REVEL_DATA, null, scores);
                    }

                    ProteinSubstitutionPredictionScore predictedScore = new ProteinSubstitutionPredictionScore(alternate, aaAlternate,
                            score, null);
                    scores.add(predictedScore);
                    lastEntry = chromosome + ":" + position;
                }
            }
        }

        // Serialize last entry
        if (prediction != null) {
            serializer.serialize(prediction);
        }

        logger.info(PARSING_DONE_LOG_MESSAGE);
        logger.info(CATEGORY_BUILDING_DONE_LOG_MESSAGE);
    }
}
