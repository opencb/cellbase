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

import com.beust.jcommander.ParameterException;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.core.config.Species;
import org.opencb.cellbase.lib.download.DownloadManager;
import org.opencb.cellbase.lib.download.EnsemblInfo;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by imedina on 03/02/15.
 */
public class DownloadCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.DownloadCommandOptions downloadCommandOptions;

    private Path output = null;
    private Path common = null;

    private String ensemblVersion;
    private String ensemblRelease;

    private Species species;


    public DownloadCommandExecutor(AdminCliOptionsParser.DownloadCommandOptions downloadCommandOptions) {
        super(downloadCommandOptions.commonOptions.logLevel, downloadCommandOptions.commonOptions.verbose,
                downloadCommandOptions.commonOptions.conf);

        this.downloadCommandOptions = downloadCommandOptions;

        if (downloadCommandOptions.output != null) {
            output = Paths.get(downloadCommandOptions.output);
        }
        if (downloadCommandOptions.common != null) {
            common = Paths.get(downloadCommandOptions.common);
        } else {
            common = output.resolve("common");
        }
    }

    /**
     * Execute specific 'download' command options.
     */
    public void execute() {
        try {
            if (downloadCommandOptions.species != null && !downloadCommandOptions.species.isEmpty()) {
                // We need to get the Species object from the CLI name
                // This can be the scientific or common name, or the ID
                //            Species speciesToDownload = null;
                for (Species sp : configuration.getAllSpecies()) {
                    if (downloadCommandOptions.species.equalsIgnoreCase(sp.getScientificName())
                            || downloadCommandOptions.species.equalsIgnoreCase(sp.getCommonName())
                            || downloadCommandOptions.species.equalsIgnoreCase(sp.getId())) {
                        species = sp;
                        break;
                    }
                }

                // If everything is right we launch the download
                if (species != null) {
                    processSpecies(species);
                } else {
                    logger.error("Species '{}' not valid", downloadCommandOptions.species);
                }
            } else {
                logger.error("--species parameter '{}' not valid", downloadCommandOptions.species);
            }
        } catch (ParameterException e) {
            logger.error("Error in 'download' command line: " + e.getMessage());
        } catch (IOException | InterruptedException e) {
            logger.error("Error downloading '" + downloadCommandOptions.species + "' files: " + e.getMessage());
        }

    }

    private void processSpecies(Species sp) throws IOException, InterruptedException {
        logger.info("Processing species " + sp.getScientificName());

        // We need to find which is the correct Ensembl host URL.
        // This can different depending on if is a vertebrate species.
        String ensemblHostUrl;
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            ensemblHostUrl = configuration.getDownload().getEnsembl().getUrl().getHost();
        } else {
            ensemblHostUrl = configuration.getDownload().getEnsemblGenomes().getUrl().getHost();
        }

        // Getting the assembly.
        // By default the first assembly in the configuration.json
        Species.Assembly assembly = null;
        if (downloadCommandOptions.assembly == null || downloadCommandOptions.assembly.isEmpty()) {
            assembly = sp.getAssemblies().get(0);
        } else {
            for (Species.Assembly assembly1 : sp.getAssemblies()) {
                if (downloadCommandOptions.assembly.equalsIgnoreCase(assembly1.getName())) {
                    assembly = assembly1;
                    break;
                }
            }
        }

        // Checking that the species and assembly are correct
        if (ensemblHostUrl == null || assembly == null) {
            logger.error("Something is not correct, check the species '{}' or the assembly '{}'",
                    downloadCommandOptions.species, downloadCommandOptions.assembly);
            return;
        }

        // Output folder creation
        String spShortName = sp.getScientificName().toLowerCase()
                .replaceAll("\\.", "")
                .replaceAll("\\)", "")
                .replaceAll("\\(", "")
                .replaceAll("[-/]", " ")
                .replaceAll("\\s+", "_");
        String spAssembly = assembly.getName().toLowerCase();
        Path spFolder = output.resolve(spShortName + "_" + spAssembly);
        makeDir(spFolder);
        makeDir(common);

        ensemblVersion = assembly.getEnsemblVersion();
        ensemblRelease = "release-" + ensemblVersion.split("_")[0];

        if (downloadCommandOptions.data != null && !downloadCommandOptions.data.isEmpty()) {
            List<String> dataList;
            if (downloadCommandOptions.data.equals("all")) {
                dataList = sp.getData();
            } else {
                dataList = Arrays.asList(downloadCommandOptions.data.split(","));
            }

            EnsemblInfo ensemblInfo = new EnsemblInfo(ensemblHostUrl, ensemblVersion, ensemblRelease);
            DownloadManager downloadManager = new DownloadManager(configuration, logger, ensemblInfo, common,
                downloadCommandOptions.assembly);

            for (String data : dataList) {
                switch (data) {
                    case EtlCommons.GENOME_DATA:
                        downloadManager.downloadReferenceGenome(sp, spShortName, assembly.getName(), spFolder);
                        break;
                    case EtlCommons.GENE_DATA:
                        downloadManager.downloadEnsemblGene(sp, spShortName, assembly.getName(), spFolder);
                        if (!dataList.contains(EtlCommons.GENOME_DATA)) {
                            // user didn't specify genome data to download, but we need it for gene data sources
                            downloadManager.downloadReferenceGenome(sp, spShortName, assembly.getName(), spFolder);
                        }
                        break;
                    case EtlCommons.VARIATION_DATA:
                        downloadManager.downloadVariation(sp, spShortName, spFolder);
                        break;
                    case EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA:
                        downloadManager.downloadCaddScores(sp, assembly.getName(), spFolder);
                        break;
                    case EtlCommons.REGULATION_DATA:
                        downloadManager.downloadRegulation(sp, spShortName, assembly.getName(), spFolder);
                        break;
                    case EtlCommons.PROTEIN_DATA:
                        downloadManager.downloadProtein(sp);
                        break;
                    case EtlCommons.CONSERVATION_DATA:
                        downloadManager.downloadConservation(sp, assembly.getName(), spFolder);
                        break;
                    case EtlCommons.CLINICAL_VARIANTS_DATA:
                        downloadManager.downloadClinical(sp, assembly.getName(), spFolder);
                        break;
                    case EtlCommons.STRUCTURAL_VARIANTS_DATA:
                        downloadManager.downloadStructuralVariants(sp, assembly.getName(), spFolder);
                        break;
                    case EtlCommons.REPEATS_DATA:
                        downloadManager.downloadRepeats(sp, assembly.getName(), spFolder);
                        break;
                    default:
                        System.out.println("Value \"" + data + "\" is not allowed for the data parameter. Allowed values"
                                + " are: {genome, gene, gene_disease_association, variation, variation_functional_score,"
                                + " regulation, protein, conservation, clinical_variants}");
                        break;
                }
            }
        }
    }

}
