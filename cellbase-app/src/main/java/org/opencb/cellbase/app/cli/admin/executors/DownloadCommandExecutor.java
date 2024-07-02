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

package org.opencb.cellbase.app.cli.admin.executors;

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.download.AbstractDownloadManager;
import org.opencb.cellbase.lib.download.DownloadFile;
import org.opencb.cellbase.lib.download.Downloader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;


public class DownloadCommandExecutor extends CommandExecutor {

    private final AdminCliOptionsParser.DownloadCommandOptions downloadCommandOptions;
    private final Path outputDirectory;

    public DownloadCommandExecutor(AdminCliOptionsParser.DownloadCommandOptions downloadCommandOptions) {
        super(downloadCommandOptions.commonOptions.logLevel, downloadCommandOptions.commonOptions.conf);

        this.downloadCommandOptions = downloadCommandOptions;
        this.outputDirectory = Paths.get(downloadCommandOptions.outputDirectory);
    }

    /**
     * Process CellBase command 'download'.
     *
     * @throws CellBaseException Exception
     */
    public void execute() throws CellBaseException {
        try {
            // Get the species and the assembly
            String species = downloadCommandOptions.speciesAndAssemblyOptions.species;
            String assembly = downloadCommandOptions.speciesAndAssemblyOptions.assembly;

            // Get the valid list of data sources
            SpeciesConfiguration speciesConfig = SpeciesUtils.getSpeciesConfiguration(configuration, species);
            List<String> dataList = getDataList(species, speciesConfig);
            logger.info("Downloading the following data sources: {}", StringUtils.join(dataList, ","));

            List<DownloadFile> downloadFiles = new ArrayList<>();
            Downloader downloader = new Downloader(species, assembly, outputDirectory, configuration);
            for (String data : dataList) {
                switch (data) {
                    case GENOME_DATA:
                        downloadFiles.addAll(downloader.downloadGenome());
                        break;
                    case CONSERVATION_DATA:
                        downloadFiles.addAll(downloader.downloadConservation());
                        break;
                    case REPEATS_DATA:
                        downloadFiles.addAll(downloader.downloadRepeats());
                        break;
                    case GENE_DATA:
                        downloadFiles.addAll(downloader.downloadGene());
                        break;
                    case PROTEIN_DATA:
                        downloadFiles.addAll(downloader.downloadProtein());
                        break;
                    case REGULATION_DATA:
                        downloadFiles.addAll(downloader.downloadRegulation());
                        break;
                    case VARIATION_FUNCTIONAL_SCORE_DATA:
                        downloadFiles.addAll(downloader.downloadCaddScores());
                        break;
                    case MISSENSE_VARIATION_SCORE_DATA:
                        downloadFiles.addAll(downloader.downloadPredictionScores());
                        break;
                    case CLINICAL_VARIANT_DATA:
                        downloadFiles.addAll(downloader.downloadClinicalVariants());
                        break;
                    case SPLICE_SCORE_DATA:
                        downloadFiles.addAll(downloader.downloadSpliceScores());
                        break;
                    case ONTOLOGY_DATA:
                        downloadFiles.addAll(downloader.downloadOntologies());
                        break;
                    case PUBMED_DATA:
                        downloadFiles.addAll(downloader.downloadPubMed());
                        break;
                    case PHARMACOGENOMICS_DATA:
                        downloadFiles.addAll(downloader.downloadPharmKGB());
                        break;
                    default:
                        throw new IllegalArgumentException("Data parameter '" + data + "' is not allowed for '" + species + "'. "
                                + "Valid values are: " + StringUtils.join(speciesConfig.getData(), ",")
                                + ". You can use data parameter 'all' to download everything");
                }
            }
            AbstractDownloadManager.writeDownloadLogFile(outputDirectory, downloadFiles);
        } catch (InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new CellBaseException("Error executing command line 'download': " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CellBaseException("Error executing command line 'download': " + e.getMessage(), e);
        }
    }

    private List<String> getDataList(String species, SpeciesConfiguration speciesConfig) throws CellBaseException {
        // No need to check if 'data' exists since it is declared as required in JCommander
        List<String> dataList;
        if ("all".equalsIgnoreCase(downloadCommandOptions.data)) {
            // Download all data sources for the species in the configuration.yml file
            dataList = speciesConfig.getData();
        } else {
            // Check if the data sources requested are valid for the species
            dataList = Arrays.asList(downloadCommandOptions.data.split(","));
            for (String data : dataList) {
                if (!speciesConfig.getData().contains(data)) {
                    throw new CellBaseException("Data parameter '" + data + "' does not exist or it is not allowed for '" + species + "'. "
                            + "Valid values are: " + StringUtils.join(speciesConfig.getData(), ",") + ". "
                            + "You can use data parameter 'all' to download everything");
                }
            }
        }
        return dataList;
    }
}
