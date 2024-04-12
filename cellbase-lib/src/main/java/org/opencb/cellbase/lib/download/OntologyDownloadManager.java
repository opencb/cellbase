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
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class OntologyDownloadManager extends AbstractDownloadManager {

    public OntologyDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        Path oboFolder = downloadFolder.resolve(ONTOLOGY_SUBDIRECTORY);
        Files.createDirectories(oboFolder);
        logger.info("Downloading {} files {} ...", ONTOLOGY_DATA, oboFolder);

        DownloadFile downloadFile;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // HPO
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getHpoObo(), HPO_OBO_NAME, ONTOLOGY_DATA,
                HPO_OBO_FILE_ID, HPO_OBO_VERSION_FILENAME, oboFolder);
        downloadFiles.add(downloadFile);

        // GO
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getGoObo(), GO_OBO_NAME, ONTOLOGY_DATA,
                GO_OBO_FILE_ID, GO_OBO_VERSION_FILENAME, oboFolder);
        downloadFiles.add(downloadFile);

        // DOID
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getDoidObo(), DOID_OBO_NAME, ONTOLOGY_DATA,
                DOID_OBO_FILE_ID, DOID_OBO_VERSION_FILENAME, oboFolder);
        downloadFiles.add(downloadFile);

        // Mondo
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getMondoObo(), MONDO_OBO_NAME, ONTOLOGY_DATA,
                MONDO_OBO_FILE_ID, MONDO_OBO_VERSION_FILENAME, oboFolder);
        downloadFiles.add(downloadFile);

        return downloadFiles;
    }
}
