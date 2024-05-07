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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;


public class RegulationDownloadManager extends AbstractDownloadManager {

    private Path regulationFolder;

    public RegulationDownloadManager(String species, String assembly, Path outdir, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, outdir, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(REGULATION_DATA));
        if (!speciesHasInfoToDownload(speciesConfiguration, REGULATION_DATA)) {
            logger.info("{} not supported for the species {}", getDataName(REGULATION_DATA), speciesConfiguration.getScientificName());
            return Collections.emptyList();
        }
        regulationFolder = downloadFolder.resolve(REGULATION_DATA);
        Files.createDirectories(regulationFolder);

        List<DownloadFile> downloadFiles = new ArrayList<>();

        downloadFiles.addAll(downloadRegulatoryaAndMotifFeatures());
        downloadFiles.add(downloadMiRTarBase());
        downloadFiles.add(downloadMirna());

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(REGULATION_DATA));
        return downloadFiles;
    }

    /**
     * Downloads Ensembl regulatory build and motif feature files.
     * @throws IOException Any issue when writing files
     * @throws InterruptedException Any issue downloading files
     */
    private List<DownloadFile> downloadRegulatoryaAndMotifFeatures() throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFile;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // Regulatory build
        downloadFile = downloadAndSaveEnsemblDataSource(configuration.getDownload().getEnsembl(), ENSEMBL_REGULATORY_BUILD_FILE_ID,
                REGULATORY_BUILD_DATA, regulationFolder);
        downloadFiles.add(downloadFile);

        // Motifs features
        List<String> urls = new ArrayList<>();
        downloadFile = downloadEnsemblDataSource(configuration.getDownload().getEnsembl(), ENSEMBL_MOTIF_FEATURES_FILE_ID, null,
                regulationFolder);
        downloadFiles.add(downloadFile);
        urls.add(downloadFile.getUrl());
        // And now the index file
        downloadFile = downloadEnsemblDataSource(configuration.getDownload().getEnsembl(), ENSEMBL_MOTIF_FEATURES_INDEX_FILE_ID, null,
                regulationFolder);
        downloadFiles.add(downloadFile);
        urls.add(downloadFile.getUrl());
        // Save data source (name, category, version,...)
        saveDataSource(MOTIF_FEATURES_DATA, "(" + getDataName(ENSEMBL_DATA) + " " + ensemblVersion + ")", getTimeStamp(), urls,
                regulationFolder.resolve(getDataVersionFilename(MOTIF_FEATURES_DATA)));

        return downloadFiles;
    }

    private DownloadFile downloadMirna() throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(MIRBASE_DATA));

        DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getMirbase(), MIRBASE_FILE_ID, MIRBASE_DATA,
                regulationFolder);

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(MIRBASE_DATA));
        return downloadFile;
    }

    private DownloadFile downloadMiRTarBase() throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(MIRTARBASE_DATA));

        DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getMiRTarBase(), MIRTARBASE_FILE_ID,
                MIRTARBASE_DATA, regulationFolder);

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(MIRTARBASE_DATA));
        return downloadFile;
    }
}
