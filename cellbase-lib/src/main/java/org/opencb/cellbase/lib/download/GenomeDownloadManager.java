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
import org.opencb.commons.exec.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class GenomeDownloadManager extends AbstractDownloadManager {

    public GenomeDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        downloadGenomeInfo();
        return downloadReferenceGenome();
    }

    public List<DownloadFile> downloadReferenceGenome() throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GENOME_DATA));
        Path sequenceFolder = downloadFolder.resolve(GENOME_DATA);
        Files.createDirectories(sequenceFolder);

        // Reference genome sequences are downloaded from Ensembl
        // New Homo sapiens assemblies contain too many ALT regions, so we download 'primary_assembly' file instead
        DownloadFile downloadFile = downloadEnsemblDataSource(configuration.getDownload().getEnsembl(), ENSEMBL_PRIMARY_FA_FILE_ID,
                sequenceFolder);

        // Save data source
        saveDataSource(GENOME_DATA, ensemblVersion, getTimeStamp(), Collections.singletonList(downloadFile.getUrl()),
                sequenceFolder.resolve(getDataVersionFilename(GENOME_DATA)));

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(GENOME_DATA));

        return Collections.singletonList(downloadFile);
    }

    public void downloadGenomeInfo() throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GENOME_INFO_DATA));
        Path sequenceFolder = downloadFolder.resolve(GENOME_DATA);
        Files.createDirectories(sequenceFolder);

        String s = "docker run --mount type=bind,source=\"" + sequenceFolder.toAbsolutePath() + "\",target=\"/tmp\" "
                + "opencb/cellbase-builder:6.2.0-SNAPSHOT /opt/cellbase/scripts/ensembl-scripts/genome_info.pl "
                + "--species \"Homo sapiens\" --outfile \"/tmp/genome_info.json\"";
        logger.info(s);
        logger.info(sequenceFolder.toAbsolutePath().toString());
        Command command = new Command(s);
        command.run();

        // FIXME Joaquin please use DockerUtils.
//        DockerUtils.run("opencb/cellbase-builder:6.2.0-SNAPSHOT", sequenceFolder.toAbsolutePath(), "/tmp" )
        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(GENOME_INFO_DATA));
    }

}
