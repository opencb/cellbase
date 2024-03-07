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
import org.opencb.cellbase.lib.EtlCommons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.PUBMED_VERSION_FILE;

public class PubMedDownloadManager extends AbstractDownloadManager {

    private static final String PUBMED_NAME = "PubMed";
    public PubMedDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        logger.info("Downloading PubMed XML files...");

        Path pubmedFolder = downloadFolder.resolve(EtlCommons.PUBMED_DATA);
        Files.createDirectories(pubmedFolder);

        // Downloads PubMed XML files
        String url = configuration.getDownload().getPubmed().getHost();
        String regexp = configuration.getDownload().getPubmed().getFiles().get(0);
        String[] name = regexp.split("[\\[\\]]");
        String[] split = name[1].split("\\.\\.");
        int start = Integer.parseInt(split[0]);
        int end = Integer.parseInt(split[1]);
        int padding = Integer.parseInt(split[2]);

        saveVersionData(EtlCommons.PUBMED_DATA, PUBMED_NAME, configuration.getDownload().getPubmed().getVersion(), getTimeStamp(),
                Collections.singletonList(url), pubmedFolder.resolve(PUBMED_VERSION_FILE));

        List<DownloadFile> list = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            String filename = name[0] + String.format("%0" + padding + "d", i) + name[2];
            logger.info("\tDownloading file {}", filename);
            list.add(downloadFile(url + "/" + filename, pubmedFolder.resolve(filename).toString()));
        }
        return list;
    }
}
