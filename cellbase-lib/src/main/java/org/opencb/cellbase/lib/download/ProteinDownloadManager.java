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

public class ProteinDownloadManager extends AbstractDownloadManager {

    public ProteinDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    /**
     * This method downloads UniProt, IntAct and Interpro data from EMBL-EBI.
     *
     * @return list of files downloaded
     * @throws IOException if there is an error writing to a file
     * @throws InterruptedException if there is an error downloading files
     * @throws CellBaseException if there is an error in the CelllBase configuration file
     */
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        // Check if the species supports this data
        if (!SpeciesUtils.hasData(configuration, speciesConfiguration.getScientificName(), PROTEIN_DATA)) {
            logger.info(DATA_NOT_SUPPORTED_MSG, getDataName(PROTEIN_DATA), speciesConfiguration.getScientificName());
            return Collections.emptyList();
        }

        Path proteinFolder = downloadFolder.resolve(PROTEIN_DATA);
        Files.createDirectories(proteinFolder);

        Path uniProtFolder = Files.createDirectories(proteinFolder.resolve(UNIPROT_DATA));
        Path interProFolder = Files.createDirectories(proteinFolder.resolve(INTERPRO_DATA));
        Path intactFolder = Files.createDirectories(proteinFolder.resolve(INTACT_DATA));

        DownloadFile downloadFile;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        logger.info(DOWNLOADING_MSG, getDataName(PROTEIN_DATA));

        // Uniprot
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getUniprot(), UNIPROT_FILE_ID, UNIPROT_DATA,
                uniProtFolder);
        downloadFiles.add(downloadFile);

        // InterPro
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getInterpro(), INTERPRO_FILE_ID, INTERPRO_DATA,
                interProFolder);
        downloadFiles.add(downloadFile);

        // Intact
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getIntact(), INTACT_FILE_ID, INTACT_DATA,
                intactFolder);
        downloadFiles.add(downloadFile);

        logger.info(DOWNLOADING_DONE_MSG, getDataName(PROTEIN_DATA));

        return downloadFiles;
    }
}
