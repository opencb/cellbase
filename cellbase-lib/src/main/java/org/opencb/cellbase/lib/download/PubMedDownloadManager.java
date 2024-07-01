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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class PubMedDownloadManager extends AbstractDownloadManager {

    public PubMedDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(PUBMED_DATA));

        Path pubmedDownloadFolder = downloadFolder.resolve(PUBMED_DATA);
        Files.createDirectories(pubmedDownloadFolder);

        // Downloads PubMed XML files
        String host = configuration.getDownload().getPubmed().getHost();
        List<String> filenames = getPubMedFilenames(configuration.getDownload().getPubmed());
        List<DownloadFile> downloadFiles = new ArrayList<>();
        for (String filename : filenames) {
            String url = host + filename;
            logger.info(DOWNLOADING_FROM_TO_LOG_MESSAGE, url, pubmedDownloadFolder.resolve(filename));
            downloadFiles.add(downloadFile(url, pubmedDownloadFolder.resolve(filename).toString()));
            logger.info(OK_LOG_MESSAGE);
        }

        // Save data source
        saveDataSource(PUBMED_DATA, configuration.getDownload().getPubmed().getVersion(), getTimeStamp(), Collections.singletonList(host),
                pubmedDownloadFolder.resolve(getDataVersionFilename(PUBMED_DATA)));

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(PUBMED_DATA));

        return downloadFiles;
    }

    public static List<String> getPubMedFilenames(DownloadProperties.URLProperties pubMedProps) {
        String regexp = pubMedProps.getFiles().get(PUBMED_REGEX_FILE_ID);
        String[] name = regexp.split("[\\[\\]]");
        String[] split = name[1].split("\\.\\.");
        int start = Integer.parseInt(split[0]);
        int end = Integer.parseInt(split[1]);
        int padding = Integer.parseInt(split[2]);

        List<String> filenames = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            String padString = "%0" + padding + "d";
            String filename = name[0] + String.format(padString, i) + name[2];
            filenames.add(filename);
        }
        return  filenames;
    }
}
