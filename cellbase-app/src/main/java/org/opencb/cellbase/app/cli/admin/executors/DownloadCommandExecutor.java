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
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.SpeciesUtils;
import org.opencb.cellbase.lib.download.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by imedina on 03/02/15.
 */
public class DownloadCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.DownloadCommandOptions downloadCommandOptions;
    private Path outputDirectory;

    public DownloadCommandExecutor(AdminCliOptionsParser.DownloadCommandOptions downloadCommandOptions) {
        super(downloadCommandOptions.commonOptions.logLevel, downloadCommandOptions.commonOptions.conf);

        this.downloadCommandOptions = downloadCommandOptions;
        this.outputDirectory = Paths.get(downloadCommandOptions.outputDirectory);
    }

    /**
     * Execute specific 'download' command options.
     */
    public void execute() {
        try {
            String species = downloadCommandOptions.speciesAndAssemblyOptions.species;
            String assembly = downloadCommandOptions.speciesAndAssemblyOptions.assembly;
            List<DownloadFile> downloadFiles = new ArrayList<>();
            List<String> dataList = getDataList(species);
            Downloader downloader = new Downloader(species, assembly, outputDirectory, configuration);
            for (String data : dataList) {
                switch (data) {
                    case EtlCommons.GENOME_DATA:
                        downloadFiles.addAll(downloader.downloadGenome());
                        break;
                    case EtlCommons.GENE_DATA:
                        downloadFiles.addAll(downloader.downloadGene());
                        break;
//                    case EtlCommons.VARIATION_DATA:
//                        downloadManager.downloadVariation();
//                        break;
                    case EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA:
                        downloadFiles.addAll(downloader.downloadCaddScores());
                        break;
                    case EtlCommons.MISSENSE_VARIATION_SCORE_DATA:
                        downloadFiles.addAll(downloader.downloadPredictionScores());
                        break;
                    case EtlCommons.REGULATION_DATA:
                        downloadFiles.addAll(downloader.downloadRegulation());
                        break;
                    case EtlCommons.PROTEIN_DATA:
                        downloadFiles.addAll(downloader.downloadProtein());
                        break;
                    case EtlCommons.CONSERVATION_DATA:
                        downloadFiles.addAll(downloader.downloadConservation());
                        break;
                    case EtlCommons.CLINICAL_VARIANTS_DATA:
                        downloadFiles.addAll(downloader.downloadClinicalVariants());
                        break;
//                    case EtlCommons.STRUCTURAL_VARIANTS_DATA:
//                        downloadFiles.add(downloadManager.downloadStructuralVariants());
//                        break;
                    case EtlCommons.REPEATS_DATA:
                        downloadFiles.addAll(downloader.downloadRepeats());
                        break;
                    case EtlCommons.OBO_DATA:
                        downloadFiles.addAll(downloader.downloadOntologies());
                        break;
                    default:
                        System.out.println("Value \"" + data + "\" is not allowed for the data parameter. Allowed values"
                                + " are: {genome, gene, gene_disease_association, variation, variation_functional_score,"
                                + " regulation, protein, conservation, clinical_variants, ontology}");
                        break;
                }
            }
            AbstractDownloadManager.writeDownloadLogFile(outputDirectory, downloadFiles);
        } catch (ParameterException | IOException | CellbaseException | InterruptedException | NoSuchMethodException
                | FileFormatException e) {
            logger.error("Error in 'download' command line: " + e.getMessage());
        }
    }

    private List<String> getDataList(String species) throws CellbaseException {
        if (StringUtils.isEmpty(downloadCommandOptions.data) || downloadCommandOptions.data.equals("all")) {
            return SpeciesUtils.getSpeciesConfiguration(configuration, species).getData();
        } else {
            return Arrays.asList(downloadCommandOptions.data.split(","));
        }
    }

    @Deprecated
    private List<String> getDataList(SpeciesConfiguration sp) {
        List<String> dataList;
        if (downloadCommandOptions.data.equals("all")) {
            dataList = sp.getData();
        } else {
            dataList = Arrays.asList(downloadCommandOptions.data.split(","));
        }
        return dataList;
    }
}
