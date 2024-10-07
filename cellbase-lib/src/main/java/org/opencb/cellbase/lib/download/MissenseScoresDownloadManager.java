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
import org.opencb.cellbase.core.utils.SpeciesUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class MissenseScoresDownloadManager extends AbstractDownloadManager {

    public MissenseScoresDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        // Check if the species supports this data
        if (!SpeciesUtils.hasData(configuration, speciesConfiguration.getScientificName(), MISSENSE_VARIATION_SCORE_DATA)) {
            logger.info(DATA_NOT_SUPPORTED_MSG, getDataName(MISSENSE_VARIATION_SCORE_DATA), speciesConfiguration.getScientificName());
            return Collections.emptyList();
        }

        logger.info(DOWNLOADING_MSG, getDataName(MISSENSE_VARIATION_SCORE_DATA));

        DownloadFile downloadFile = downloadRevel();

        logger.info(DOWNLOADING_DONE_MSG, getDataName(MISSENSE_VARIATION_SCORE_DATA));

        return Collections.singletonList(downloadFile);
    }

    public DownloadFile downloadRevel() throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFile = null;

        String prefixId = getConfigurationFileIdPrefix(speciesConfiguration.getScientificName());

        // Check if the species is supported
        if (configuration.getDownload().getRevel().getFiles().containsKey(prefixId + REVEL_FILE_ID)) {
            logger.info(DOWNLOADING_MSG, getDataName(REVEL_DATA));

            // Create the REVEL download path
            Path revelDownloadPath = downloadFolder.resolve(MISSENSE_VARIATION_SCORE_DATA).resolve(REVEL_DATA);
            Files.createDirectories(revelDownloadPath);

            // Download REVEL and save data source
            downloadFile = downloadAndSaveDataSource(configuration.getDownload().getRevel(), prefixId + REVEL_FILE_ID, REVEL_DATA,
                    revelDownloadPath);

            logger.info(DOWNLOADING_MSG, getDataName(REVEL_DATA));
        }

        return downloadFile;
    }
}
