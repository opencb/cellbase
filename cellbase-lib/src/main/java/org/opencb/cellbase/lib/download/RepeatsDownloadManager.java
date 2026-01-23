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
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class RepeatsDownloadManager extends AbstractDownloadManager {

    public RepeatsDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        return downloadRepeats();
    }

    public List<DownloadFile> downloadRepeats() throws IOException, InterruptedException, CellBaseException {
        // Check if the species supports this data
        if (!SpeciesUtils.hasData(configuration, speciesConfiguration.getScientificName(), REPEATS_DATA)) {
            logger.info(DATA_NOT_SUPPORTED_MSG, getDataName(REPEATS_DATA), speciesConfiguration.getScientificName());
            return Collections.emptyList();
        }

        Path repeatsFolder = downloadFolder.resolve(REPEATS_DATA);
        Files.createDirectories(repeatsFolder);
        Path trfFolder = Files.createDirectories(repeatsFolder.resolve(TRF_DATA));
        Path wmFolder = Files.createDirectories(repeatsFolder.resolve(WM_DATA));
        Path gsdFolder = Files.createDirectories(repeatsFolder.resolve(GSD_DATA));

        String prefixId = getConfigurationFileIdPrefix(speciesConfiguration.getScientificName());

        String url;
        Path outputPath;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        logger.info(DOWNLOADING_MSG, getDataName(REPEATS_DATA));


        // Download tandem repeat finder
        logger.info(DOWNLOADING_MSG, getDataName(TRF_DATA));
        url = configuration.getDownload().getSimpleRepeats().getHost()
                + configuration.getDownload().getSimpleRepeats().getFiles().get(prefixId + SIMPLE_REPEATS_FILE_ID);
        outputPath = repeatsFolder.resolve(getFilenameFromUrl(url));
        logger.info(DOWNLOADING_FROM_TO_MSG, url, outputPath);
        downloadFiles.add(downloadFile(url, outputPath));
        logger.info(OK_MSG);

        saveDataSource(TRF_DATA, configuration.getDownload().getSimpleRepeats().getVersion(), getTimeStamp(),
                Collections.singletonList(url), trfFolder.resolve(getDataVersionFilename(TRF_DATA)));

        // Download WindowMasker
        logger.info(DOWNLOADING_MSG, getDataName(WM_DATA));
        url = configuration.getDownload().getWindowMasker().getHost()
                + configuration.getDownload().getWindowMasker().getFiles().get(prefixId + WINDOW_MASKER_FILE_ID);
        outputPath = repeatsFolder.resolve(getFilenameFromUrl(url));
        logger.info(DOWNLOADING_FROM_TO_MSG, url, outputPath);
        downloadFiles.add(downloadFile(url, outputPath));
        logger.info(OK_MSG);

        saveDataSource(WM_DATA, configuration.getDownload().getWindowMasker().getVersion(), getTimeStamp(),
                Collections.singletonList(url), wmFolder.resolve(getDataVersionFilename(WM_DATA)));

        // Download genomic super duplications
        logger.info(DOWNLOADING_MSG, getDataName(GSD_DATA));
        url = configuration.getDownload().getGenomicSuperDups().getHost()
                + configuration.getDownload().getGenomicSuperDups().getFiles().get(prefixId + GENOMIC_SUPER_DUPS_FILE_ID);
        outputPath = repeatsFolder.resolve(getFilenameFromUrl(url));
        logger.info(DOWNLOADING_FROM_TO_MSG, url, outputPath);
        downloadFiles.add(downloadFile(url, outputPath));
        logger.info(OK_MSG);

        saveDataSource(GSD_DATA, configuration.getDownload().getGenomicSuperDups().getVersion(), getTimeStamp(),
                Collections.singletonList(url), gsdFolder.resolve(getDataVersionFilename(GSD_DATA)));

        logger.info(DOWNLOADING_DONE_MSG, getDataName(REPEATS_DATA));

        return downloadFiles;
    }
}
