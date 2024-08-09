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

        DownloadFile downloadFile;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        logger.info(DOWNLOADING_MSG, getDataName(CLINICAL_VARIANT_DATA));

        // Create clinical directory
        Path clinicalPath = downloadFolder.resolve(EtlCommons.CLINICAL_VARIANT_DATA).toAbsolutePath();
        Files.createDirectories(clinicalPath);


        // ClinVar
        logger.info(DOWNLOADING_MSG, getDataName(CLINVAR_DATA));
        DownloadProperties.URLProperties props = configuration.getDownload().getClinvar();
        List<String> urls = new ArrayList<>();
        for (String fileId : Arrays.asList(CLINVAR_FULL_RELEASE_FILE_ID, CLINVAR_SUMMARY_FILE_ID, CLINVAR_ALLELE_FILE_ID,
                CLINVAR_EFO_TERMS_FILE_ID)) {
            downloadFile = downloadDataSource(props, fileId, clinicalPath);
            downloadFiles.add(downloadFile);

            // Save URLs to be written in the version file
            urls.add(downloadFile.getUrl());
        }
        // Save data source
        saveDataSource(CLINVAR_DATA, props.getVersion(), getTimeStamp(), urls,
                clinicalPath.resolve(getDataVersionFilename(CLINVAR_DATA)));
        logger.info(DOWNLOADING_DONE_MSG, getDataName(CLINVAR_DATA));

        // COSMIC
        logger.warn("{} files must be downloaded manually !", getDataName(COSMIC_DATA));
        props = configuration.getDownload().getCosmic();
        String url = props.getHost() + props.getFiles().get(COSMIC_FILE_ID);
        saveDataSource(COSMIC_DATA, props.getVersion(), getTimeStamp(), Collections.singletonList(url),
                clinicalPath.resolve(getDataVersionFilename(COSMIC_DATA)));

        // HGMD
        logger.warn("{} files must be downloaded manually !", getDataName(HGMD_DATA));
        props = configuration.getDownload().getHgmd();
        url = props.getHost() + props.getFiles().get(HGMD_FILE_ID);
        saveDataSource(HGMD_DATA, props.getVersion(), getTimeStamp(), Collections.singletonList(url),
                clinicalPath.resolve(getDataVersionFilename(HGMD_DATA)));

        // GWAS catalog
        logger.info(DOWNLOADING_MSG, getDataName(GWAS_DATA));
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getGwasCatalog(), GWAS_FILE_ID, GWAS_DATA, clinicalPath);
        downloadFiles.add(downloadFile);
        logger.info(DOWNLOADING_DONE_MSG, getDataName(GWAS_DATA));

        return downloadFiles;
    }
}
