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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class PharmGKBDownloadManager extends AbstractDownloadManager {

    public PharmGKBDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        logger.info("Downloading PharmGKB files...");

        Path folder = downloadFolder.resolve(PHARMACOGENOMICS_DATA).resolve(PHARMGKB_DATA);
        Files.createDirectories(folder);

        // Downloads PubMed XML files
        DownloadProperties.URLProperties pharmGKB = configuration.getDownload().getPharmGKB();

        List<String> urls = new ArrayList<>();
        urls.add(pharmGKB.getHost());
        urls.addAll(pharmGKB.getFiles());
        saveVersionData(PHARMACOGENOMICS_DATA, PHARMGKB_NAME, pharmGKB.getVersion(), getTimeStamp(), urls,
                folder.resolve(PHARMGKB_VERSION_FILENAME));

        List<DownloadFile> list = new ArrayList<>();
        for (String url : pharmGKB.getFiles()) {
            logger.info("\tDownloading file " + url + " to " + folder.resolve(Paths.get(new URL(url).getPath()).getFileName()));
            list.add(downloadFile(url, folder.resolve(Paths.get(new URL(url).getPath()).getFileName()).toString()));
        }
        return list;
    }
}
