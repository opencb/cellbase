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
import org.opencb.cellbase.core.utils.SpeciesUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class ConservationDownloadManager extends AbstractDownloadManager {

    public ConservationDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        return downloadConservation();
    }

    /**
     * This method downloads both PhastCons and PhyloP data from UCSC for Human and Mouse species.
     * @return list of files downloaded
     * @throws IOException if there is an error writing to a file
     * @throws InterruptedException if there is an error downloading files
     * @throws CellBaseException if there is an error executing the command line
     */
    public List<DownloadFile> downloadConservation() throws IOException, InterruptedException, CellBaseException {
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // Check if the species is supported
        if (SpeciesUtils.hasData(configuration, speciesConfiguration.getScientificName(), CONSERVATION_DATA)) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(CONSERVATION_DATA));

            // Create folders
            Path conservationFolder = downloadFolder.resolve(CONSERVATION_DATA);
            Files.createDirectories(conservationFolder);
            Files.createDirectories(conservationFolder.resolve(GERP_DATA));
            Files.createDirectories(conservationFolder.resolve(PHASTCONS_DATA));
            Files.createDirectories(conservationFolder.resolve(PHYLOP_DATA));

            // Download data
            String filename;
            Path outputPath;

            // Prepare variables
            String phastconsHost = configuration.getDownload().getPhastCons().getHost();
            String phylopHost = configuration.getDownload().getPhylop().getHost();
            List<String> phastconsUrls = new ArrayList<>(50);
            List<String> phyloPUrls = new ArrayList<>(50);
            String gerpUrl = null;

            // Human
            if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
                // 1. PhastCons and PhyloP
                String[] chromosomes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                        "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "M"};
                for (String chromosome : chromosomes) {
                    logger.info(DOWNLOADING_LOG_MESSAGE, "phastConst " + chromosome);
                    String phastConsUrl = phastconsHost + configuration.getDownload().getPhastCons().getFiles().get(PHASTCONS_FILE_ID)
                            + "chr" + chromosome + ".phastCons470way.wigFix.gz";
                    filename = Paths.get(phastConsUrl).getFileName().toString();
                    outputPath = conservationFolder.resolve(PHASTCONS_DATA).resolve(filename);
                    downloadFiles.add(downloadFile(phastConsUrl, outputPath.toString()));
                    phastconsUrls.add(phastConsUrl);

                    logger.info(DOWNLOADING_LOG_MESSAGE, "phyloP " + chromosome);
                    String phyloPUrl = phylopHost + configuration.getDownload().getPhylop().getFiles().get(PHYLOP_FILE_ID)
                            + "chr" + chromosome + ".phyloP470way.wigFix.gz";
                    filename = Paths.get(phyloPUrl).getFileName().toString();
                    outputPath = conservationFolder.resolve(PHYLOP_DATA).resolve(filename);
                    downloadFiles.add(downloadFile(phyloPUrl, outputPath.toString()));
                    phyloPUrls.add(phyloPUrl);
                }

                // 2. Gerp
                logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GERP_DATA));
                gerpUrl = configuration.getDownload().getGerp().getHost()
                        + configuration.getDownload().getGerp().getFiles().get(GERP_FILE_ID);
                filename = Paths.get(gerpUrl).getFileName().toString();
                outputPath = conservationFolder.resolve(GERP_DATA).resolve(filename);
                downloadFiles.add(downloadFile(gerpUrl, outputPath.toString()));
            }

            // Mouse
            if (speciesConfiguration.getScientificName().equals(MUS_MUSCULUS_NAME)) {
                String prefixId = getConfigurationFileIdPrefix(speciesConfiguration.getScientificName());

                // 1. PhastCons and PhyloP
                String[] chromosomes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                        "15", "16", "17", "18", "19", "X", "Y", "M"};
                for (String chromosome : chromosomes) {
                    logger.info(DOWNLOADING_LOG_MESSAGE, "phastConst " + chromosome);
                    String phastConsUrl = phastconsHost
                            + configuration.getDownload().getPhastCons().getFiles().get(prefixId + PHASTCONS_FILE_ID)
                            + "chr" + chromosome + ".phastCons35way.wigFix.gz";
                    filename = Paths.get(phastConsUrl).getFileName().toString();
                    outputPath = conservationFolder.resolve(PHASTCONS_DATA).resolve(filename);
                    downloadFiles.add(downloadFile(phastConsUrl, outputPath.toString()));
                    phastconsUrls.add(phastConsUrl);

                    logger.info(DOWNLOADING_LOG_MESSAGE, "phyloP " + chromosome);
                    String phyloPUrl = phylopHost + configuration.getDownload().getPhylop().getFiles().get(prefixId + PHYLOP_FILE_ID)
                            + "chr" + chromosome + ".phyloP35way.wigFix.gz";
                    filename = Paths.get(phyloPUrl).getFileName().toString();
                    outputPath = conservationFolder.resolve(PHYLOP_DATA).resolve(filename);
                    downloadFiles.add(downloadFile(phyloPUrl, outputPath.toString()));
                    phyloPUrls.add(phyloPUrl);
                }

                // 2. Gerp
                logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GERP_DATA));
                gerpUrl = configuration.getDownload().getGerp().getHost()
                        + configuration.getDownload().getGerp().getFiles().get(prefixId + GERP_FILE_ID);
                filename = Paths.get(gerpUrl).getFileName().toString();
                outputPath = conservationFolder.resolve(GERP_DATA).resolve(filename);
                downloadFiles.add(downloadFile(gerpUrl, outputPath.toString()));
            }

            // Save data version
            saveDataSource(PHASTCONS_DATA, configuration.getDownload().getPhastCons().getVersion(), getTimeStamp(), phastconsUrls,
                    conservationFolder.resolve(getDataVersionFilename(PHASTCONS_DATA)));
            saveDataSource(PHYLOP_DATA, configuration.getDownload().getPhylop().getVersion(), getTimeStamp(), phyloPUrls,
                    conservationFolder.resolve(getDataVersionFilename(PHYLOP_DATA)));
            saveDataSource(GERP_DATA, configuration.getDownload().getGerp().getVersion(), getTimeStamp(),
                    Collections.singletonList(gerpUrl), conservationFolder.resolve(getDataVersionFilename(GERP_DATA)));
            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(CONSERVATION_DATA));
        }
        return downloadFiles;
    }

}