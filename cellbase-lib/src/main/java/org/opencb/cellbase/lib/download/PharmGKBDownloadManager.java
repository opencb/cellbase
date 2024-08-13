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
import org.opencb.cellbase.core.utils.SpeciesUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class PharmGKBDownloadManager extends AbstractDownloadManager {

    public PharmGKBDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        // Check if the species supports this data
        if (!SpeciesUtils.hasData(configuration, speciesConfiguration.getScientificName(), PHARMACOGENOMICS_DATA)) {
            logger.info(DATA_NOT_SUPPORTED_MSG, getDataName(PHARMACOGENOMICS_DATA), speciesConfiguration.getScientificName());
            return Collections.emptyList();
        }

        logger.info(CATEGORY_DOWNLOADING_MSG, getDataCategory(PHARMGKB_DATA), getDataName(PHARMGKB_DATA));

        Path pharmgkbDownloadFolder = downloadFolder.resolve(PHARMACOGENOMICS_DATA).resolve(PHARMGKB_DATA);
        Files.createDirectories(pharmgkbDownloadFolder);

        DownloadProperties.URLProperties pharmGKBConfig = configuration.getDownload().getPharmGKB();

        DownloadFile downloadFile;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        List<String> urls = new ArrayList<>();
        for (String fileName : pharmGKBConfig.getFiles().values()) {
            String url = pharmGKBConfig.getHost() + fileName;
            urls.add(url);

            Path downloadedFilePath = pharmgkbDownloadFolder.resolve(getFilenameFromUrl(url));
            logger.info(DOWNLOADING_FROM_TO_MSG, url, downloadedFilePath);
            downloadFile = downloadFile(url, downloadedFilePath);
            logger.info(OK_MSG);
            downloadFiles.add(downloadFile);
        }

        // Save data source
        saveDataSource(PHARMGKB_DATA, pharmGKBConfig.getVersion(), getTimeStamp(), urls,
                pharmgkbDownloadFolder.resolve(getDataVersionFilename(PHARMGKB_DATA)));

        logger.info(CATEGORY_DOWNLOADING_DONE_MSG, getDataCategory(PHARMGKB_DATA), getDataName(PHARMGKB_DATA));

        return downloadFiles;
    }
}
