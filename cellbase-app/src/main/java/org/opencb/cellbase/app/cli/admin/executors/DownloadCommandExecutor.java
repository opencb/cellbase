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
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.lib.download.AbstractDownloadManager;
import org.opencb.cellbase.lib.download.DownloadFile;
import org.opencb.cellbase.lib.download.Downloader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

/**
 * Created by imedina on 03/02/15.
 */
public class DownloadCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.DownloadCommandOptions downloadCommandOptions;
    private Path outputDirectory;

    private static final List<String> VALID_SOURCES_TO_DOWNLOAD = Arrays.asList(GENOME_DATA, GENE_DATA, VARIATION_FUNCTIONAL_SCORE_DATA,
            MISSENSE_VARIATION_SCORE_DATA, REGULATION_DATA, PROTEIN_DATA, CONSERVATION_DATA, CLINICAL_VARIANT_DATA, REPEATS_DATA,
            ONTOLOGY_DATA, PUBMED_DATA, PHARMACOGENOMICS_DATA);

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
            String species = downloadCommandOptions.speciesAndAssemblyOptions.species;
            String assembly = downloadCommandOptions.speciesAndAssemblyOptions.assembly;
            List<DownloadFile> downloadFiles = new ArrayList<>();
            List<String> dataList = checkDataSources();
            Downloader downloader = new Downloader(species, assembly, outputDirectory, configuration);
            for (String data : dataList) {
                switch (data) {
                    case GENOME_DATA:
                        downloadFiles.addAll(downloader.downloadGenome());
                        break;
                    case GENE_DATA:
                        downloadFiles.addAll(downloader.downloadGene());
                        break;
                    case VARIATION_FUNCTIONAL_SCORE_DATA:
                        downloadFiles.addAll(downloader.downloadCaddScores());
                        break;
                    case MISSENSE_VARIATION_SCORE_DATA:
                        downloadFiles.addAll(downloader.downloadPredictionScores());
                        break;
                    case REGULATION_DATA:
                        downloadFiles.addAll(downloader.downloadRegulation());
                        break;
                    case PROTEIN_DATA:
                        downloadFiles.addAll(downloader.downloadProtein());
                        break;
                    case CONSERVATION_DATA:
                        downloadFiles.addAll(downloader.downloadConservation());
                        break;
                    case CLINICAL_VARIANT_DATA:
                        downloadFiles.addAll(downloader.downloadClinicalVariants());
                        break;
                    case REPEATS_DATA:
                        downloadFiles.addAll(downloader.downloadRepeats());
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
                        throw new IllegalArgumentException("Value '" + data + "' is not allowed for the data parameter. Valid values are: "
                                + StringUtils.join(VALID_SOURCES_TO_DOWNLOAD, ",") + "; or use 'all' to download everything");
                }
            }
            AbstractDownloadManager.writeDownloadLogFile(outputDirectory, downloadFiles);
        } catch (InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new CellBaseException("Error executing command line 'download': " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CellBaseException("Error executing command line 'download': " + e.getMessage(), e);
        }
    }

    private List<String> checkDataSources() {
        if (StringUtils.isEmpty(downloadCommandOptions.data)) {
            throw new IllegalArgumentException("Missing data parameter. Valid values are: "
                    + StringUtils.join(VALID_SOURCES_TO_DOWNLOAD, ",") + "; or use 'all' to download everything");
        }
        List<String> dataList = Arrays.asList(downloadCommandOptions.data.split(","));
        for (String data : dataList) {
            switch (data) {
                case GENOME_DATA:
                case GENE_DATA:
                case VARIATION_FUNCTIONAL_SCORE_DATA:
                case MISSENSE_VARIATION_SCORE_DATA:
                case REGULATION_DATA:
                case PROTEIN_DATA:
                case CONSERVATION_DATA:
                case CLINICAL_VARIANT_DATA:
                case REPEATS_DATA:
                case ONTOLOGY_DATA:
                case PUBMED_DATA:
                case PHARMACOGENOMICS_DATA:
                    break;
                default:
                    throw new IllegalArgumentException("Value '" + data + "' is not allowed for the data parameter. Valid values are: "
                            + StringUtils.join(VALID_SOURCES_TO_DOWNLOAD, ",") + "; or use 'all' to download everything");
            }
        }
        return dataList;
    }
}
