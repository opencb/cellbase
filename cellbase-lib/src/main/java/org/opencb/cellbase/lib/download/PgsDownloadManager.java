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
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PgsDownloadManager extends AbstractDownloadManager {

    public PgsDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        logger.info("Downloading PGS files...");

        DownloadProperties.URLProperties pgsUrlProperties = configuration.getDownload().getPgs();

        Path pgsFolder = downloadFolder.resolve("pgs");
        Files.createDirectories(pgsFolder);

        List<String> urls = new ArrayList<>();
        urls.add(pgsUrlProperties.getHost());

        String urlAllMeta = pgsUrlProperties.getFiles().get(0);
        urls.add(urlAllMeta);

        String filename = new File(urlAllMeta).getName();

        // Downloads PGS files
        List<DownloadFile> list = new ArrayList<>();
        System.out.println(urlAllMeta);
        list.add(downloadFile(urlAllMeta, pgsFolder.resolve(filename).toString()));

        String baseUrl = urlAllMeta.replace(filename, "").replace("metadata", "scores");
        BufferedReader br = FileUtils.newBufferedReader(pgsFolder.resolve(filename));
        // Skip first line
        String line = br.readLine();
        while ((line = br.readLine()) != null) {
            String[] field = line.split(",");
            String pgsId = field[0];

            String url = baseUrl + pgsId + "/Metadata/" + pgsId + "_metadata.tar.gz";
            logger.info("Downloading file {}", url);
            list.add(downloadFile(url, pgsFolder.resolve(new File(url).getName()).toString()));

            url = baseUrl + pgsId + "/ScoringFiles/Harmonized/" + pgsId + "_hmPOS_GRCh38.txt.gz";
            logger.info("Downloading file {}", url);
            list.add(downloadFile(url, pgsFolder.resolve(new File(url).getName()).toString()));
        }
        br.close();

        // Save version file
        saveVersionData(EtlCommons.PGS_DATA, EtlCommons.PGS_DATA, pgsUrlProperties.getVersion(), getTimeStamp(), urls,
                pgsFolder.resolve("pgsVersion.json"));

        logger.info("Done. Downloaded PGS files!");

        return list;
    }
}
