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
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class OntologyDownloadManager extends DownloadManager {

    public OntologyDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellbaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    public OntologyDownloadManager(CellBaseConfiguration configuration, Path targetDirectory, SpeciesConfiguration speciesConfiguration,
                                   SpeciesConfiguration.Assembly assembly) throws IOException, CellbaseException {
        super(configuration, targetDirectory, speciesConfiguration, assembly);
    }

    public List<DownloadFile> downloadObo() throws IOException, InterruptedException {
        logger.info("Downloading obo files ...");

        List<DownloadFile> downloadFiles = new ArrayList<>();
        Path oboFolder = downloadFolder.resolve("obo");
        Files.createDirectories(oboFolder);

        String url = configuration.getDownload().getHpoObo().getHost();
        downloadFiles.add(downloadFile(url, oboFolder.resolve("hp.obo").toString()));

        url = configuration.getDownload().getGoObo().getHost();
        downloadFiles.add(downloadFile(url, oboFolder.resolve("go-basic.obo").toString()));

        url = configuration.getDownload().getDoidObo().getHost();
        downloadFiles.add(downloadFile(url, oboFolder.resolve("doid.obo").toString()));

        return downloadFiles;
    }
}
