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
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class OntologyDownloadManager extends AbstractDownloadManager {

    private static final String DATA_VERSION_FIELD = "data-version:";

    public OntologyDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        Path oboFolder = downloadFolder.resolve(ONTOLOGY_SUBDIRECTORY);
        Files.createDirectories(oboFolder);
        logger.info(DOWNLOADING_LOG_MESSAGE, ONTOLOGY_NAME);

        DownloadFile downloadFile;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // HPO
        downloadFile = downloadDataSource(configuration.getDownload().getHpoObo(), HPO_OBO_FILE_ID, oboFolder);
        String version = getVersionFromOboFile(oboFolder.resolve(downloadFile.getOutputFile()));
        saveDataSource(HPO_OBO_NAME, ONTOLOGY_NAME, version, getTimeStamp(), Collections.singletonList(downloadFile.getUrl()),
                oboFolder.resolve(HPO_OBO_VERSION_FILENAME));
        downloadFiles.add(downloadFile);

        // GO
        downloadFile = downloadDataSource(configuration.getDownload().getGoObo(), GO_OBO_FILE_ID, oboFolder);
        version = getVersionFromOboFile(oboFolder.resolve(downloadFile.getOutputFile()));
        saveDataSource(GO_OBO_NAME, ONTOLOGY_NAME, version, getTimeStamp(), Collections.singletonList(downloadFile.getUrl()),
                oboFolder.resolve(GO_OBO_VERSION_FILENAME));
        downloadFiles.add(downloadFile);

        // DOID
        downloadFile = downloadDataSource(configuration.getDownload().getDoidObo(), DOID_OBO_FILE_ID, oboFolder);
        version = getVersionFromOboFile(oboFolder.resolve(downloadFile.getOutputFile()));
        saveDataSource(DOID_OBO_NAME, ONTOLOGY_NAME, version, getTimeStamp(), Collections.singletonList(downloadFile.getUrl()),
                oboFolder.resolve(DOID_OBO_VERSION_FILENAME));
        downloadFiles.add(downloadFile);

        // Mondo
        downloadFile = downloadDataSource(configuration.getDownload().getMondoObo(), MONDO_OBO_FILE_ID, oboFolder);
        version = getVersionFromOboFile(oboFolder.resolve(downloadFile.getOutputFile()));
        saveDataSource(MONDO_OBO_NAME, ONTOLOGY_NAME, version, getTimeStamp(), Collections.singletonList(downloadFile.getUrl()),
                oboFolder.resolve(MONDO_OBO_VERSION_FILENAME));
        downloadFiles.add(downloadFile);

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, ONTOLOGY_NAME);

        return downloadFiles;
    }

    private String getVersionFromOboFile(Path oboPath) throws CellBaseException, IOException {
        String version = null;
        if (!oboPath.toFile().exists()) {
            throw new CellBaseException("OBO file " + oboPath + " does not exit");
        }
        try (BufferedReader reader = FileUtils.newBufferedReader(oboPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(DATA_VERSION_FIELD)) {
                    version = line.split(DATA_VERSION_FIELD)[1].trim();
                    break;
                }
            }
        }
        return version;
    }
}
