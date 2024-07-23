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

import org.opencb.cellbase.core.common.GitRepositoryState;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.commons.utils.DockerUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class GenomeDownloadManager extends AbstractDownloadManager {

    private Path sequenceFolder;

    public GenomeDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);

        this.sequenceFolder = downloadFolder.resolve(GENOME_DATA);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        downloadGenomeInfo();
        return downloadReferenceGenome();
    }

    public List<DownloadFile> downloadReferenceGenome() throws IOException, InterruptedException, CellBaseException {
        Path genomeVersionFilePath = sequenceFolder.resolve(getDataVersionFilename(GENOME_DATA));

        if (Files.exists(genomeVersionFilePath)) {
            logger.info(DATA_ALREADY_DOWNLOADED, genomeVersionFilePath.getFileName(), getDataName(GENOME_DATA));
            return new ArrayList<>();
        }

        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GENOME_DATA));
        Files.createDirectories(sequenceFolder);

        // Reference genome sequences are downloaded from Ensembl
        // New Homo sapiens assemblies contain too many ALT regions, so we download 'primary_assembly' file instead
        DownloadFile downloadFile = downloadEnsemblDataSource(configuration.getDownload().getEnsembl(), ENSEMBL_PRIMARY_FA_FILE_ID,
                sequenceFolder);

        // Save data source
        saveDataSource(GENOME_DATA, ensemblVersion, getTimeStamp(), Collections.singletonList(downloadFile.getUrl()),
                genomeVersionFilePath);

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(GENOME_DATA));

        return Collections.singletonList(downloadFile);
    }

    public void downloadGenomeInfo() throws IOException, CellBaseException {
        String genomeInfoFilename = "genome_info.json";

        if (Files.exists(sequenceFolder.resolve(genomeInfoFilename))) {
            logger.info(DATA_ALREADY_DOWNLOADED, genomeInfoFilename, getDataName(GENOME_INFO_DATA));
            return;
        }

        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GENOME_INFO_DATA));
        Files.createDirectories(sequenceFolder);

        String dockerImage = "opencb/cellbase-builder:" + GitRepositoryState.get().getBuildVersion();
        try {
            // Build command line to run Perl script via docker image
            // Output binding
            AbstractMap.SimpleEntry<String, String> outputBinding = new AbstractMap.SimpleEntry<>(
                    sequenceFolder.toAbsolutePath().toString(), "/tmp");

            // Params
            String params = "/opt/cellbase/scripts/ensembl-scripts/genome_info.pl"
                    + " --species \"" + speciesConfiguration.getScientificName() + "\""
                    + " --assembly \"" + assemblyConfiguration.getName() + "\""
                    + " --outfile \"" + outputBinding.getValue() + "/" + genomeInfoFilename + "\"";

            // Execute perl script in docker
            DockerUtils.run(dockerImage, null, outputBinding, params, null);
        } catch (Exception e) {
            throw new CellBaseException("Error executing Perl script from Docker " + dockerImage, e);
        }

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(GENOME_INFO_DATA));
    }

}
