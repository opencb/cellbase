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
        logger.info(DOWNLOADING_LOG_MESSAGE, PROTEIN_NAME);
        if (!speciesHasInfoToDownload(speciesConfiguration, PROTEIN_DATA)) {
            logger.info("{} not supported for the species {}", PROTEIN_NAME, speciesConfiguration.getScientificName());
            return Collections.emptyList();
        }
        Path proteinFolder = downloadFolder.resolve(PROTEIN_SUBDIRECTORY);
        Files.createDirectories(proteinFolder);

        DownloadFile downloadFile;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // Uniprot
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getUniprot(), UNIPROT_FILE_ID, UNIPROT_NAME, PROTEIN_DATA,
                UNIPROT_VERSION_FILENAME, proteinFolder);
        downloadFiles.add(downloadFile);

        // InterPro
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getInterpro(), INTERPRO_FILE_ID, INTERPRO_NAME, PROTEIN_DATA,
                INTERPRO_VERSION_FILENAME, proteinFolder);
        downloadFiles.add(downloadFile);

        // Intact
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getIntact(), INTACT_FILE_ID, INTACT_NAME, PROTEIN_DATA,
                INTACT_VERSION_FILENAME, proteinFolder);
        downloadFiles.add(downloadFile);

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, PROTEIN_NAME);

        return downloadFiles;
    }
}
