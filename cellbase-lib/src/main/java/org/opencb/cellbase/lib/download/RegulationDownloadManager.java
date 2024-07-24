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
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;


public class RegulationDownloadManager extends AbstractDownloadManager {

    private Path regulationFolder;
    private Path regulatoryBuildFolder;
    private Path motifFeaturesFolder;
    private Path mirTarBaseFolder;
    private Path mirBaseFolder;

    public RegulationDownloadManager(String species, String assembly, Path outdir, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, outdir, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // Check if species is supported
        if (SpeciesUtils.hasData(configuration, speciesConfiguration.getScientificName(), REGULATION_DATA)) {
            regulationFolder = downloadFolder.resolve(REGULATION_DATA);
            Files.createDirectories(regulationFolder);
            regulatoryBuildFolder = Files.createDirectories(regulationFolder.resolve(REGULATORY_BUILD_DATA));
            motifFeaturesFolder = Files.createDirectories(regulationFolder.resolve(MOTIF_FEATURES_DATA));
            mirTarBaseFolder = Files.createDirectories(regulationFolder.resolve(MIRTARBASE_DATA));
            mirBaseFolder = Files.createDirectories(regulationFolder.resolve(MIRBASE_DATA));

            String prefixId = getConfigurationFileIdPrefix(speciesConfiguration.getScientificName());

            // Already downloaded ?
            boolean downloadRegulatoryBuild = !isAlreadyDownloaded(regulatoryBuildFolder.resolve(getDataVersionFilename(
                    REGULATORY_BUILD_DATA)), getDataName(REGULATORY_BUILD_DATA));
            boolean downloadMotifFeatures = !isAlreadyDownloaded(motifFeaturesFolder.resolve(getDataVersionFilename(MOTIF_FEATURES_DATA)),
                    getDataName(MOTIF_FEATURES_DATA));
            boolean downloadMirTarBase = !isAlreadyDownloaded(mirTarBaseFolder.resolve(getDataVersionFilename(MIRTARBASE_DATA)),
                    getDataName(MIRTARBASE_DATA)) && configuration.getDownload().getMiRTarBase().getFiles().containsKey(prefixId
                    + MIRTARBASE_FILE_ID);
            boolean downloadMirBase = !isAlreadyDownloaded(mirBaseFolder.resolve(getDataVersionFilename(MIRBASE_DATA)),
                    getDataName(MIRBASE_DATA));

            if (!downloadRegulatoryBuild && !downloadMotifFeatures && !downloadMirTarBase && !downloadMirBase) {
                return new ArrayList<>();
            }

            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(REGULATION_DATA));

            if (downloadRegulatoryBuild) {
                downloadFiles.addAll(downloadRegulatoryaBuild());
            }

            if (downloadMotifFeatures) {
                downloadFiles.addAll(downloadMotifFeatures());
            }

            if (downloadMirTarBase) {
                downloadFiles.add(downloadMiRTarBase());
            }
            if (downloadMirBase) {
                downloadFiles.add(downloadMirna());
            }

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(REGULATION_DATA));
        }

        return downloadFiles;
    }

    /**
     * Downloads Ensembl regulatory build.
     * @throws IOException Any issue when writing files
     * @throws InterruptedException Any issue downloading files
     */
    private List<DownloadFile> downloadRegulatoryaBuild() throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(REGULATORY_BUILD_DATA));

        DownloadFile downloadFile;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // Regulatory build
        downloadFile = downloadAndSaveEnsemblDataSource(configuration.getDownload().getEnsembl(), ENSEMBL_REGULATORY_BUILD_FILE_ID,
                REGULATORY_BUILD_DATA, regulatoryBuildFolder);
        downloadFiles.add(downloadFile);

        return downloadFiles;
    }

    /**
     * Downloads Ensembl motif feature files.
     * @throws IOException Any issue when writing files
     * @throws InterruptedException Any issue downloading files
     */
    private List<DownloadFile> downloadMotifFeatures() throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(MOTIF_FEATURES_DATA));

        DownloadFile downloadFile;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // Motifs features
        List<String> urls = new ArrayList<>();
        downloadFile = downloadEnsemblDataSource(configuration.getDownload().getEnsembl(), ENSEMBL_MOTIF_FEATURES_FILE_ID, null,
                motifFeaturesFolder);
        downloadFiles.add(downloadFile);
        urls.add(downloadFile.getUrl());

        // And now the index file
        downloadFile = downloadEnsemblDataSource(configuration.getDownload().getEnsembl(), ENSEMBL_MOTIF_FEATURES_INDEX_FILE_ID, null,
                motifFeaturesFolder);
        downloadFiles.add(downloadFile);
        urls.add(downloadFile.getUrl());

        // Save data source (name, category, version,...)
        saveDataSource(MOTIF_FEATURES_DATA, "(" + getDataName(ENSEMBL_DATA) + " " + ensemblVersion + ")", getTimeStamp(), urls,
                motifFeaturesFolder.resolve(getDataVersionFilename(MOTIF_FEATURES_DATA)));

        return downloadFiles;
    }

    private DownloadFile downloadMirna() throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(MIRBASE_DATA));

        return downloadAndSaveDataSource(configuration.getDownload().getMirbase(), MIRBASE_FILE_ID, MIRBASE_DATA, mirBaseFolder);
    }

    private DownloadFile downloadMiRTarBase() throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFile = null;
        String prefixId = getConfigurationFileIdPrefix(speciesConfiguration.getScientificName());
        if (configuration.getDownload().getMiRTarBase().getFiles().containsKey(prefixId + MIRTARBASE_FILE_ID)) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(MIRTARBASE_DATA));

            downloadFile = downloadAndSaveDataSource(configuration.getDownload().getMiRTarBase(), prefixId + MIRTARBASE_FILE_ID,
                    MIRTARBASE_DATA, mirTarBaseFolder);
        }
        return downloadFile;
    }
}
