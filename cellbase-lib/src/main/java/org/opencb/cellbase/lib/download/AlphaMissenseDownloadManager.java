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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.ALPHAMISSENSE_VERSION_FILENAME;

public class AlphaMissenseDownloadManager extends AbstractDownloadManager {

    public AlphaMissenseDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        logger.info("Downloading AlphaMissense file...");

        // Downloads AlphaMissense file
        DownloadProperties.URLProperties alphaMissenseUrlProps = configuration.getDownload().getAlphaMissense();

        List<DownloadFile> list = new ArrayList<>();
        for (String file : alphaMissenseUrlProps.getFiles()) {
            String filename = new File(file).getName();
            logger.info("\tDownloading file " + filename);
            list.add(downloadFile(file, downloadFolder.resolve(filename).toAbsolutePath().toString()));
        }

        // Save version
        saveVersionData(EtlCommons.ALPHAMISSENSE_DATA, EtlCommons.ALPHAMISSENSE_DATA, alphaMissenseUrlProps.getVersion(), getTimeStamp(),
                alphaMissenseUrlProps.getFiles(), downloadFolder.resolve(ALPHAMISSENSE_VERSION_FILENAME));

        logger.info("Downloaded AlphaMissense file. Done!");

        return list;
    }
}