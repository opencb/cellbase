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
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.lib.EtlCommons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class CaddDownloadManager extends AbstractDownloadManager {

    public CaddDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        if (!speciesHasInfoToDownload(speciesConfiguration, VARIATION_FUNCTIONAL_SCORE_DATA)) {
            return null;
        }
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading {} files ...", CADD_NAME);

            Path variationFunctionalScoreFolder = downloadFolder.resolve(VARIATION_FUNCTIONAL_SCORE_FOLDER_NAME);
            Files.createDirectories(variationFunctionalScoreFolder);

            // Download CADD and save data source
            DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getCadd(), CADD_NAME,
                    VARIATION_FUNCTIONAL_SCORE_DATA, CADD_FILE_ID, CADD_VERSION_FILENAME, variationFunctionalScoreFolder);

            return Collections.singletonList(downloadFile);
        }
        logger.warn("CADD scores are not supported for {}", speciesConfiguration.getScientificName());
        return Collections.emptyList();
    }
}
