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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class VariationDownloadManager extends AbstractDownloadManager {

    public VariationDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        return downloadVariation();
    }

    public List<DownloadFile> downloadVariation() throws IOException, InterruptedException, CellBaseException {
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // Check if species is supported
        // and we do not need to download human variation data from Ensembl. It is already included in the CellBase.
        if (SpeciesUtils.hasData(configuration, speciesConfiguration.getScientificName(), VARIATION_DATA)
                && !speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            Path variationFolder = downloadFolder.resolve(VARIATION_DATA);
            Files.createDirectories(variationFolder);

            if (isAlreadyDownloaded(variationFolder.resolve(getDataVersionFilename(VARIATION_DATA)), getDataName(VARIATION_DATA))) {
                return new ArrayList<>();
            }

            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(VARIATION_DATA));

            DownloadFile downloadFile;
            String prefixId = getConfigurationFileIdPrefix(speciesConfiguration.getScientificName());

            // Variation and structural variations
            List<String> fileIds = Arrays.asList(prefixId + VARIATION_FILE_ID, prefixId + STRUCTURAL_VARIATIONS_FILE_ID);
            List<String> urls = new ArrayList<>();
            for (String fileId : fileIds) {
                downloadFile = downloadEnsemblDataSource(configuration.getDownload().getEnsembl(), fileId, null, variationFolder);
                downloadFiles.add(downloadFile);
                urls.add(downloadFile.getUrl());
            }

            saveDataSource(VARIATION_DATA, ensemblVersion, getTimeStamp(), urls,
                    variationFolder.resolve(getDataVersionFilename(VARIATION_DATA)));

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(VARIATION_DATA));
        }
        return downloadFiles;
    }
}
