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
import org.opencb.commons.exec.Command;
import org.opencb.commons.utils.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class PharmGKBDownloadManager extends AbstractDownloadManager {

    public PharmGKBDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        DownloadProperties.URLProperties pharmGKB = configuration.getDownload().getPharmGKB();
        Path pharmgkbDownloadFolder = downloadFolder.resolve(PHARMACOGENOMICS_SUBDIRECTORY).resolve(PHARMGKB_SUBDIRECTORY);
        Files.createDirectories(pharmgkbDownloadFolder);
        logger.info("Downloading {} files at {} ...", PHARMGKB_DATA, pharmgkbDownloadFolder);

        List<String> urls = new ArrayList<>();
        List<DownloadFile> downloadFiles = new ArrayList<>();
        String host = pharmGKB.getHost();
        for (Map.Entry<String, String> entry : pharmGKB.getFiles().entrySet()) {
            String url = host + entry.getValue();
            urls.add(url);

            Path downloadedFileName = Paths.get(new URL(url).getPath()).getFileName();
            Path downloadedFilePath = pharmgkbDownloadFolder.resolve(downloadedFileName);
            logger.info("Downloading file {} to {}", url, downloadedFilePath);
            DownloadFile downloadFile = downloadFile(url, downloadedFilePath.toString());
            downloadFiles.add(downloadFile);

            // Unzip downloaded file
            unzip(downloadedFilePath.getParent(), downloadedFileName.toString(), Collections.emptyList(),
                    pharmgkbDownloadFolder.resolve(downloadedFileName.toString().split("\\.")[0]));
        }

        // Save versions
        saveDataSource(PHARMGKB_NAME, PHARMACOGENOMICS_DATA, pharmGKB.getVersion(), getTimeStamp(), urls,
                pharmgkbDownloadFolder.resolve(PHARMGKB_VERSION_FILENAME));

        return downloadFiles;
    }

    private void unzip(Path inPath, String zipFilename, List<String> outFilenames, Path outPath) throws IOException {
        // Check zip file exists
        FileUtils.checkFile(inPath.resolve(zipFilename));

        // Unzip files if output dir does NOT exist
        if (!outPath.toFile().exists()) {
            logger.info("Unzipping {} into {}", zipFilename, outPath);
            Command cmd = new Command("unzip -d " + outPath + " " + inPath.resolve(zipFilename));
            cmd.run();
            // Check if expected files exist
            for (String outFilename : outFilenames) {
                FileUtils.checkFile(outPath.resolve(outFilename));
            }
        }
    }
}
