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

import com.beust.jcommander.ParameterException;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.lib.EtlCommons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        return downloadReferenceGenome();
    }

    public List<DownloadFile> downloadReferenceGenome() throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, GENOME_NAME);
        Path sequenceFolder = downloadFolder.resolve(GENOME_SUBDIRECTORY);
        Files.createDirectories(sequenceFolder);

        // Reference genome sequences are downloaded from Ensembl
        // New Homo sapiens assemblies contain too many ALT regions, so we download 'primary_assembly' file instead
        DownloadFile downloadFile = downloadEnsemblDataSource(configuration.getDownload().getEnsembl(), ENSEMBL_PRIMARY_FA_FILE_ID,
                sequenceFolder);

        // Save data source
        saveDataSource(ENSEMBL_NAME, EtlCommons.GENOME_DATA, ensemblVersion, getTimeStamp(),
                Collections.singletonList(downloadFile.getUrl()), sequenceFolder.resolve(GENOME_VERSION_FILENAME));

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, GENOME_NAME);

        return Collections.singletonList(downloadFile);
    }

    /**
     * This method downloads bith PhastCons and PhyloP data from UCSC for Human and Mouse species.
     * @return list of files downloaded
     * @throws IOException if there is an error writing to a file
     * @throws InterruptedException if there is an error downloading files
     * @throws CellBaseException if there is an error executing the command line
     */
    public List<DownloadFile> downloadConservation() throws IOException, InterruptedException, CellBaseException {
        if (!speciesHasInfoToDownload(speciesConfiguration, CONSERVATION_DATA)) {
            return Collections.emptyList();
        }
        List<DownloadFile> downloadFiles = new ArrayList<>();
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info(DOWNLOADING_LOG_MESSAGE, CONSERVATION_NAME);
            Path conservationFolder = downloadFolder.resolve(CONSERVATION_SUBDIRECTORY);

            Files.createDirectories(conservationFolder);
            Files.createDirectories(conservationFolder.resolve(GERP_SUBDIRECTORY));
            Files.createDirectories(conservationFolder.resolve(PHASTCONS_SUBDIRECTORY));
            Files.createDirectories(conservationFolder.resolve(PHYLOP_SUBDIRECTORY));

            String[] chromosomes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                    "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "M", };

            if (assemblyConfiguration.getName().equalsIgnoreCase(GRCH38_NAME)) {
                String filename;
                Path outputPath;
                String assembly = HG38_NAME;
                List<String> phastconsUrls = new ArrayList<>(chromosomes.length);
                List<String> phyloPUrls = new ArrayList<>(chromosomes.length);
                // Downloading PhastCons and PhyloP
                logger.info(DOWNLOADING_LOG_MESSAGE, (PHASTCONS_NAME + "/" + PHYLOP_NAME));
                for (String chromosome : chromosomes) {
                    // PhastCons
                    String phastConsUrl = configuration.getDownload().getPhastCons().getHost() + configuration.getDownload().getPhastCons()
                            .getFiles().get(PHASTCONS_FILE_ID).replaceAll(PUT_ASSEMBLY_HERE_MARK, assembly)
                            .replace(PUT_CHROMOSOME_HERE_MARK, chromosome);
                    filename = Paths.get(phastConsUrl).getFileName().toString();
                    outputPath = conservationFolder.resolve(PHASTCONS_SUBDIRECTORY).resolve(filename);
                    logger.info(DOWNLOADING_FROM_TO_LOG_MESSAGE, phastConsUrl, outputPath);
                    downloadFiles.add(downloadFile(phastConsUrl, outputPath.toString()));
                    phastconsUrls.add(phastConsUrl);

                    // PhyloP
                    String phyloPUrl = configuration.getDownload().getPhylop().getHost() + configuration.getDownload().getPhylop()
                            .getFiles().get(PHYLOP_FILE_ID).replaceAll(PUT_ASSEMBLY_HERE_MARK, assembly)
                            .replace(PUT_CHROMOSOME_HERE_MARK, chromosome);
                    filename = Paths.get(phyloPUrl).getFileName().toString();
                    outputPath = conservationFolder.resolve(PHYLOP_SUBDIRECTORY).resolve(filename);
                    logger.info(DOWNLOADING_FROM_TO_LOG_MESSAGE, phyloPUrl, outputPath);
                    downloadFiles.add(downloadFile(phyloPUrl, outputPath.toString()));
                    phyloPUrls.add(phyloPUrl);
                }

                // Downloading Gerp
                logger.info(DOWNLOADING_LOG_MESSAGE, GERP_NAME);
                String gerpUrl = configuration.getDownload().getGerp().getHost() + configuration.getDownload().getGerp().getFiles()
                        .get(GERP_FILE_ID);
                filename = Paths.get(gerpUrl).getFileName().toString();
                outputPath = conservationFolder.resolve(GERP_SUBDIRECTORY).resolve(filename);
                logger.info(DOWNLOADING_FROM_TO_LOG_MESSAGE, gerpUrl, outputPath);
                downloadFiles.add(downloadFile(gerpUrl, outputPath.toString()));

                // Save data version
                saveDataSource(PHASTCONS_NAME, EtlCommons.CONSERVATION_DATA, configuration.getDownload().getPhastCons().getVersion(),
                        getTimeStamp(), phastconsUrls, conservationFolder.resolve(PHASTCONS_VERSION_FILENAME));
                saveDataSource(PHYLOP_NAME, EtlCommons.CONSERVATION_DATA, configuration.getDownload().getPhylop().getVersion(),
                        getTimeStamp(), phyloPUrls, conservationFolder.resolve(PHYLOP_VERSION_FILENAME));
                saveDataSource(GERP_NAME, EtlCommons.CONSERVATION_DATA, configuration.getDownload().getGerp().getVersion(), getTimeStamp(),
                        Collections.singletonList(gerpUrl), conservationFolder.resolve(GERP_VERSION_FILENAME));
            }
            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, CONSERVATION_NAME);
        }

        return downloadFiles;
    }

    public List<DownloadFile> downloadRepeats() throws IOException, InterruptedException, CellBaseException {
        if (!speciesHasInfoToDownload(speciesConfiguration, REPEATS_DATA)) {
            return Collections.emptyList();
        }
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info(DOWNLOADING_LOG_MESSAGE, REPEATS_NAME);
            Path repeatsFolder = downloadFolder.resolve(EtlCommons.REPEATS_SUBDIRECTORY);
            Files.createDirectories(repeatsFolder);
            List<DownloadFile> downloadFiles = new ArrayList<>();
            String pathParam;
            if (assemblyConfiguration.getName().equalsIgnoreCase(GRCH38_NAME)) {
                pathParam = HG38_NAME;
            } else {
                logger.error("Please provide a valid human assembly: {}, {}", GRCH37_NAME, GRCH38_NAME);
                throw new ParameterException("Assembly '" + assemblyConfiguration.getName() + "' is not valid. Please provide "
                        + "a valid human assembly: " + GRCH37_NAME + ", " + GRCH38_NAME);
            }

            // Download tandem repeat finder
            String url = configuration.getDownload().getSimpleRepeats().getHost() + configuration.getDownload().getSimpleRepeats()
                    .getFiles().get(SIMPLE_REPEATS_FILE_ID).replace(PUT_ASSEMBLY_HERE_MARK, pathParam);
            saveDataSource(TRF_NAME, EtlCommons.REPEATS_DATA, configuration.getDownload().getSimpleRepeats().getVersion(), getTimeStamp(),
                    Collections.singletonList(url), repeatsFolder.resolve(EtlCommons.TRF_VERSION_FILENAME));

            Path outputPath = repeatsFolder.resolve(getFilenameFromUrl(url));
            logger.info(DOWNLOADING_FROM_TO_LOG_MESSAGE, url, outputPath);
            downloadFiles.add(downloadFile(url, outputPath.toString()));

            // Download genomic super duplications
            url = configuration.getDownload().getGenomicSuperDups().getHost() + configuration.getDownload().getGenomicSuperDups()
                    .getFiles().get(GENOMIC_SUPER_DUPS_FILE_ID).replace(PUT_ASSEMBLY_HERE_MARK, pathParam);
            saveDataSource(GSD_NAME, EtlCommons.REPEATS_DATA, configuration.getDownload().getGenomicSuperDups().getVersion(),
                    getTimeStamp(), Collections.singletonList(url), repeatsFolder.resolve(EtlCommons.GSD_VERSION_FILENAME));

            outputPath = repeatsFolder.resolve(getFilenameFromUrl(url));
            logger.info(DOWNLOADING_FROM_TO_LOG_MESSAGE, url, outputPath);
            downloadFiles.add(downloadFile(url, outputPath.toString()));

            // Download WindowMasker
            if (!pathParam.equalsIgnoreCase(HG19_NAME)) {
                url = configuration.getDownload().getWindowMasker().getHost() + configuration.getDownload().getWindowMasker().getFiles()
                        .get(WINDOW_MASKER_FILE_ID).replace(PUT_ASSEMBLY_HERE_MARK, pathParam);
                saveDataSource(WM_NAME, EtlCommons.REPEATS_DATA, configuration.getDownload().getWindowMasker().getVersion(),
                        getTimeStamp(), Collections.singletonList(url), repeatsFolder.resolve(EtlCommons.WM_VERSION_FILENAME));

                outputPath = repeatsFolder.resolve(getFilenameFromUrl(url));
                logger.info(DOWNLOADING_FROM_TO_LOG_MESSAGE, url, outputPath);
                downloadFiles.add(downloadFile(url, outputPath.toString()));
            }
            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, REPEATS_NAME);

            return downloadFiles;
        }
        return Collections.emptyList();
    }
}
