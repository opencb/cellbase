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
import org.opencb.cellbase.lib.EtlCommons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class ClinicalDownloadManager extends AbstractDownloadManager {

    public ClinicalDownloadManager(String species, String assembly, Path outdir, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, outdir, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        return downloadClinical();
    }

    public List<DownloadFile> downloadClinical() throws IOException, InterruptedException, CellBaseException {
        // Check if the species supports this data
        if (!SpeciesUtils.hasData(configuration, speciesConfiguration.getScientificName(), CLINICAL_VARIANT_DATA)) {
            logger.info(DATA_NOT_SUPPORTED_MSG, getDataName(CLINICAL_VARIANT_DATA), speciesConfiguration.getScientificName());
            return Collections.emptyList();
        }

        Path versionPath;
        Path downloadPath;
        DownloadFile downloadFile;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        logger.info(DOWNLOADING_MSG, getDataName(CLINICAL_VARIANT_DATA));

        // Create clinical directory
        Path clinicalPath = downloadFolder.resolve(EtlCommons.CLINICAL_VARIANT_DATA).toAbsolutePath();
        Files.createDirectories(clinicalPath);

        DownloadProperties.URLProperties props;
        List<String> urls;

        // ClinVar
        downloadPath = clinicalPath.resolve(CLINVAR_DATA);
        versionPath = downloadPath.resolve(getDataVersionFilename(CLINVAR_DATA));
        if (Files.exists(versionPath)) {
            logger.info("{} already downloaded. Skipping download.", getDataName(CLINVAR_DATA));
        } else {
            if (!Files.exists(downloadPath)) {
                logger.info("Creating {} directory: {}...", CLINVAR_DATA, downloadPath);
                Files.createDirectory(downloadPath);
            }

            logger.info(DOWNLOADING_MSG, getDataName(CLINVAR_DATA));
            props = configuration.getDownload().getClinvar();
            urls = new ArrayList<>();
            for (String fileId : Arrays.asList(CLINVAR_FULL_RELEASE_FILE_ID, CLINVAR_SUMMARY_FILE_ID, CLINVAR_ALLELE_FILE_ID,
                    CLINVAR_EFO_TERMS_FILE_ID)) {
                downloadFile = downloadDataSource(props, fileId, downloadPath);
                downloadFiles.add(downloadFile);

                // Save URLs to be written in the version file
                urls.add(downloadFile.getUrl());
            }
            // Save data source
            saveDataSource(CLINVAR_DATA, props.getVersion(), getTimeStamp(), urls, versionPath);
            logger.info(DOWNLOADING_DONE_MSG, getDataName(CLINVAR_DATA));
        }

        // COSMIC
        downloadPath = clinicalPath.resolve(COSMIC_DATA);
        versionPath = downloadPath.resolve(getDataVersionFilename(COSMIC_DATA));
        if (Files.exists(versionPath)) {
            logger.info("{} already downloaded. Skipping download.", getDataName(COSMIC_DATA));
        } else {
            if (!Files.exists(downloadPath)) {
                logger.info("Creating {} directory: {}...", COSMIC_DATA, downloadPath);
                Files.createDirectory(downloadPath);
            }

            logger.warn("{} files must be downloaded manually !", getDataName(COSMIC_DATA));
            props = configuration.getDownload().getCosmic();
            urls = new ArrayList<>();
            for (String fileId : Arrays.asList(COSMIC_GENOME_SCREENS_MUTANT_FILE_ID, COSMIC_CLASSIFICATION_FILE_ID)) {
                // Save URLs to be written in the version file
                urls.add(props.getHost() + props.getFiles().get(fileId));
            }
            saveDataSource(COSMIC_DATA, props.getVersion(), getTimeStamp(), urls, versionPath);
        }

        // HGMD
        downloadPath = clinicalPath.resolve(HGMD_DATA);
        versionPath = downloadPath.resolve(getDataVersionFilename(HGMD_DATA));
        if (Files.exists(versionPath)) {
            logger.info("{} already downloaded. Skipping download.", getDataName(HGMD_DATA));
        } else {
            if (!Files.exists(downloadPath)) {
                logger.info("Creating {} directory: {}...", HGMD_DATA, downloadPath);
                Files.createDirectory(downloadPath);
            }

            logger.warn("{} files must be downloaded manually !", getDataName(HGMD_DATA));
            props = configuration.getDownload().getHgmd();
            String url = props.getHost() + props.getFiles().get(HGMD_FILE_ID);
            saveDataSource(HGMD_DATA, props.getVersion(), getTimeStamp(), Collections.singletonList(url), versionPath);
        }

        // CIViC
        downloadPath = clinicalPath.resolve(CIVIC_DATA);
        versionPath = downloadPath.resolve(getDataVersionFilename(CIVIC_DATA));
        if (Files.exists(versionPath)) {
            logger.info("{} already downloaded. Skipping download.", getDataName(CIVIC_DATA));
        } else {
            if (!Files.exists(downloadPath)) {
                logger.info("Creating {} directory: {}...", CIVIC_DATA, downloadPath);
                Files.createDirectory(downloadPath);
            }

            logger.info(DOWNLOADING_MSG, getDataName(CIVIC_DATA));
            props = configuration.getDownload().getCivic();
            urls = new ArrayList<>();
            for (String fileId : Arrays.asList(CIVIC_VARIANTS_FILE_ID, CIVIC_FEATURES_FILE_ID, CIVIC_PROFILES_FILE_ID,
                    CIVIC_ASSERTIONS_FILE_ID, CIVIC_EVIDENCES_FILE_ID)) {
                downloadFile = downloadDataSource(props, fileId, downloadPath);
                downloadFiles.add(downloadFile);

                // Save URLs to be written in the version file
                urls.add(downloadFile.getUrl());
            }
            // Save data source
            saveDataSource(CIVIC_DATA, props.getVersion(), getTimeStamp(), urls, versionPath);
            logger.info(DOWNLOADING_DONE_MSG, getDataName(CIVIC_DATA));
        }

        // GWAS catalog
        downloadPath = clinicalPath.resolve(GWAS_DATA);
        versionPath = downloadPath.resolve(getDataVersionFilename(GWAS_DATA));
        if (Files.exists(versionPath)) {
            logger.info("{} already downloaded. Skipping download.", getDataName(GWAS_DATA));
        } else {
            if (!Files.exists(downloadPath)) {
                logger.info("Creating {} directory: {}...", GWAS_DATA, downloadPath);
                Files.createDirectory(downloadPath);
            }

            logger.info(DOWNLOADING_MSG, getDataName(GWAS_DATA));
            downloadFile = downloadDataSource(configuration.getDownload().getGwasCatalog(), GWAS_FILE_ID, GWAS_DATA, downloadPath);
            downloadFiles.add(downloadFile);
            saveDataSource(GWAS_DATA, configuration.getDownload().getGwasCatalog().getVersion(), getTimeStamp(),
                    Collections.singletonList(downloadFile.getUrl()), versionPath);
            logger.info(DOWNLOADING_DONE_MSG, getDataName(GWAS_DATA));
        }

        return downloadFiles;
    }
}
