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


import org.opencb.biodata.models.core.MissensePredictedScore;
import org.opencb.biodata.models.core.MissensePredictions;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class RevelScoreBuilder extends CellBaseBuilder {

    private Path revelFilePath = null;

    public RevelScoreBuilder(Path revelFilePath, CellBaseSerializer serializer) {
        super(serializer);
        this.revelFilePath = revelFilePath;
        logger = LoggerFactory.getLogger(ConservationBuilder.class);
    }

    @Override
    public void parse() throws IOException {

        ZipInputStream zis = new ZipInputStream(new FileInputStream(String.valueOf(revelFilePath)));
        ZipEntry zipEntry = zis.getNextEntry();

        ZipFile zipFile = new ZipFile(String.valueOf(revelFilePath));
        InputStream inputStream = zipFile.getInputStream(zipEntry);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        // skip header
        String line = bufferedReader.readLine();
        String[] fields = null;
        int lastPosition = 0;
        List<MissensePredictedScore> scores = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            fields = line.split(",");
            String chromosome = fields[0];
            int position = Integer.parseInt(fields[2]);
            String reference = fields[3];
            String alternate = fields[4];
            String aaReference = fields[5];
            String aaAlternate = fields[6];
            float score = Float.parseFloat(fields[7]);

            if (lastPosition != 0 && position != lastPosition) {
                MissensePredictions predictions = new MissensePredictions(chromosome, position, scores);
                serializer.serialize(predictions);
                scores = new ArrayList<>();
            }

            MissensePredictedScore predictedScore = new MissensePredictedScore(reference, alternate, aaReference, aaAlternate, score);
            scores.add(predictedScore);
            lastPosition = position;
        }

        zis.close();
        zipFile.close();
        inputStream.close();
        bufferedReader.close();
    }
}
