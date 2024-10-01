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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.download.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.opencb.cellbase.lib.EtlCommons.*;


public class DownloadCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.DownloadCommandOptions downloadCommandOptions;
    private Path outputDirectory;

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
            SpeciesConfiguration speciesConfiguration = SpeciesUtils.getSpeciesConfiguration(configuration, species);
            if (speciesConfiguration == null) {
                throw new CellBaseException("Invalid species: '" + downloadCommandOptions.speciesAndAssemblyOptions.species + "'");
            }
            List<String> dataList = getDataList(species, speciesConfiguration);
            logger.info("Downloading the following data sources: {}", CollectionUtils.isEmpty(dataList)
                    ? Collections.emptyList()
                    : StringUtils.join(dataList, ","));

            List<DownloadFile> downloadFiles = new ArrayList<>();
            AbstractDownloadManager downloader = null;
            for (String data : dataList) {
                switch (data) {
                    case GENOME_DATA:
                        downloader = new GenomeDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case CONSERVATION_DATA:
                        downloader = new ConservationDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case REPEATS_DATA:
                        downloader = new RepeatsDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case GENE_DATA:
                        downloader = new GeneDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case PROTEIN_DATA:
                        downloader = new ProteinDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case REGULATION_DATA:
                        downloader = new RegulationDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case VARIATION_DATA:
                        downloader = new VariationDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case VARIATION_FUNCTIONAL_SCORE_DATA:
                        downloader = new CaddDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case MISSENSE_VARIATION_SCORE_DATA:
                        downloader = new MissenseScoresDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case CLINICAL_VARIANT_DATA:
                        downloader = new ClinicalDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case SPLICE_SCORE_DATA:
                        downloader = new SpliceScoreDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case ONTOLOGY_DATA:
                        downloader = new OntologyDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case PUBMED_DATA:
                        downloader = new PubMedDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case PHARMACOGENOMICS_DATA:
                        downloader = new PharmGKBDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    case PGS_DATA:
                        downloader = new PgsDownloadManager(species, assembly, outputDirectory, configuration);
                        break;
                    default:
                        throw new IllegalArgumentException("Data parameter '" + data + "' is not allowed for '" + species + "'. "
                                + "Valid values are: " + StringUtils.join(speciesConfiguration.getData(), ",")
                                + ". You can use data parameter 'all' to download everything");
                }

                // Call to download method and add the files to the list
                downloadFiles.addAll(downloader.download());
            }
            if (downloader != null) {
                Map<String, Object> params = new HashMap<>();
                params.put("species", species);
                params.put("assembly", assembly);
                params.put("data", dataList);
                params.put("outDir", outputDirectory);
                downloader.writeDownloadLogFile(params, downloadFiles);
            } else {
                logger.warn("Impossible to write log summary: downloader is null");
            }
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
            Set<String> invalidData = new HashSet<>();
            for (String data : dataList) {
                if (!speciesConfig.getData().contains(data)) {
                    invalidData.add(data);
                }
            }
            if (!CollectionUtils.isEmpty(invalidData)) {
                throw new CellBaseException("Data '" + StringUtils.join(invalidData, ",") + "' not supported by species '" + species + "'."
                        + "Valid values are: " + StringUtils.join(speciesConfig.getData(), ",") + ". Our use data parameter 'all' to"
                        + " download everything");
            }
        }
        return dataList;
    }
}
