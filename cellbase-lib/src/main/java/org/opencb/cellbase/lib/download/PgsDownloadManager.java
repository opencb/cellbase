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
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class PgsDownloadManager extends AbstractDownloadManager {

    public PgsDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        String pgslabel = getDataCategory(PGS_CATALOG_DATA) + "/" + getDataName(PGS_CATALOG_DATA);
        logger.info(DOWNLOADING_LOG_MESSAGE, pgslabel);

        DownloadProperties.URLProperties pgsProps = configuration.getDownload().getPgsCatalog();

        Path pgsPath = downloadFolder.resolve(PGS_DATA);
        Files.createDirectories(pgsPath);

        List<String> urls = new ArrayList<>();

        String urlAllMeta = pgsProps.getFiles().get(PGS_CATALOG_FILE_ID);
        urls.add(urlAllMeta);

        String filename = new File(urlAllMeta).getName();

        // Downloads PGS files
        String url;
        Path outPath;
        List<DownloadFile> list = new ArrayList<>();
        list.add(downloadFile(urlAllMeta, pgsPath.resolve(filename).toString()));

        String baseUrl = urlAllMeta.replace(filename, "").replace("metadata", "scores");
        try (BufferedReader br = FileUtils.newBufferedReader(pgsPath.resolve(filename))) {
            // Skip first line
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] field = line.split(",");
                String pgsId = field[0];

                url = baseUrl + pgsId + "/Metadata/" + pgsId + "_metadata.tar.gz";
                outPath = pgsPath.resolve(new File(url).getName());
                logger.info(DOWNLOADING_FROM_TO_LOG_MESSAGE, url, outPath);
                list.add(downloadFile(url, outPath.toString()));
                urls.add(url);

                url = baseUrl + pgsId + "/ScoringFiles/Harmonized/" + pgsId + "_hmPOS_GRCh38.txt.gz";
                outPath = pgsPath.resolve(new File(url).getName());
                logger.info(DOWNLOADING_FROM_TO_LOG_MESSAGE, url, outPath);
                list.add(downloadFile(url, outPath.toString()));
                urls.add(url);
            }
        }

        // Save version file
        saveDataSource(PGS_CATALOG_DATA, pgsProps.getVersion(), getTimeStamp(), urls,
                pgsPath.resolve(getDataVersionFilename(PGS_CATALOG_DATA)));

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, pgslabel);

        return list;
    }
}
