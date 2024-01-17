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

package org.opencb.cellbase.lib.download;

import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.builders.RevelScoreBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RevelScoresDownloadManager extends AbstractDownloadManager {

    public RevelScoresDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        List<DownloadFile> list = new ArrayList<>();

        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading Revel data ...");

            Path scorePath = downloadFolder.resolve(EtlCommons.PROTEIN_SUBSTITUTION_PREDICTION_DATA);
            Files.createDirectories(scorePath);

            String url = configuration.getDownload().getRevel().getHost();

            list.add(downloadFile(url, scorePath.resolve(EtlCommons.REVEL_RAW_FILENAME).toString()));

            saveVersionData(EtlCommons.PROTEIN_SUBSTITUTION_PREDICTION_DATA, RevelScoreBuilder.SOURCE,
                    configuration.getDownload().getRevel().getVersion(), getTimeStamp(), Collections.singletonList(url),
                    scorePath.resolve(EtlCommons.REVEL_VERSION_FILENAME));

            logger.info("Downloaded Revel file. Done!");
        }

        return list;
    }
}
