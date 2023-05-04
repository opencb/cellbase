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
import org.opencb.cellbase.lib.EtlCommons;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PharmGKBDownloadManager extends AbstractDownloadManager {

    private static final String PHARMGKB_NAME = "PharmGKB";
    public PharmGKBDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        logger.info("Downloading PharmGKB files...");

        Path folder = downloadFolder.resolve(PHARMGKB_NAME);
        Files.createDirectories(folder);

        // Downloads PubMed XML files
        DownloadProperties.URLProperties pharmGKB = configuration.getDownload().getPharmGKB();

        List<String> urls = new ArrayList<>();
        urls.add(pharmGKB.getHost());
        urls.addAll(pharmGKB.getFiles());
        saveVersionData(EtlCommons.PHARMACOGENOMICS_DATA, PHARMGKB_NAME, pharmGKB.getVersion(), getTimeStamp(), urls,
                folder.resolve("pharmgkbVersion.json"));

        List<DownloadFile> list = new ArrayList<>();
        for (String url : pharmGKB.getFiles()) {
            logger.info("\tDownloading file " + url + " to " + folder.resolve(Paths.get(new URL(url).getPath()).getFileName()));
            list.add(downloadFile(url, folder.resolve(Paths.get(new URL(url).getPath()).getFileName()).toString()));
        }
        return list;
    }
}
