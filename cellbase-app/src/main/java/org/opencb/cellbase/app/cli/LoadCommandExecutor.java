/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.app.cli;

import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.core.loader.LoaderException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by imedina on 03/02/15.
 */
public class LoadCommandExecutor extends CommandExecutor {

    private LoadRunner loadRunner;
    private CliOptionsParser.LoadCommandOptions loadCommandOptions;

    private Path input;

    private String database;
    private String field;
    private String loader;
    private int numThreads;

    public LoadCommandExecutor(CliOptionsParser.LoadCommandOptions loadCommandOptions) {
        super(loadCommandOptions.commonOptions.logLevel, loadCommandOptions.commonOptions.verbose,
                loadCommandOptions.commonOptions.conf);

        this.loadCommandOptions = loadCommandOptions;

        if (loadCommandOptions.input != null) {
            input = Paths.get(loadCommandOptions.input);
        }
        if (loadCommandOptions.database != null) {
            database = loadCommandOptions.database;
        }
        if (loadCommandOptions.field != null) {
            field = loadCommandOptions.field;
        }
        if (loadCommandOptions.loader != null) {
            loader = loadCommandOptions.loader;
        }
    }


    /**
     * Parse specific 'data' command options.
     */
    public void execute() {

        checkParameters();

        if (loadCommandOptions.data != null) {

            if (loadCommandOptions.loaderParams.containsKey("mongodb-index-folder")) {
                configuration.getDatabase().getOptions().put("mongodb-index-folder",
                        loadCommandOptions.loaderParams.get("mongodb-index-folder"));
            }
            // If 'authenticationDatabase' is not passed by argument then we read it from configuration.json
            if (loadCommandOptions.loaderParams.containsKey("authenticationDatabase")) {
                configuration.getDatabase().getOptions().put("authenticationDatabase",
                        loadCommandOptions.loaderParams.get("authenticationDatabase"));
            }

//                loadRunner = new LoadRunner(loader, database, loadCommandOptions.loaderParams, numThreads, configuration);
            loadRunner = new LoadRunner(loader, database, numThreads, configuration);

            String[] buildOptions;
            if (loadCommandOptions.data.equals("all")) {
                buildOptions = new String[]{"genome", "gene", "conservation", "regulatory_region",
                        "protein", "ppi", "protein_functional_prediction", "variation", "variation_functional_score", "clinical", };
            } else {
                buildOptions = loadCommandOptions.data.split(",");
            }

            for (int i = 0; i < buildOptions.length; i++) {
                String buildOption = buildOptions[i];
                try {
                    switch (buildOption) {
                        case "genome":
                            loadRunner.load(input.resolve("genome_info.json"), "genome_info");
                            loadRunner.load(input.resolve("genome_sequence.json.gz"), "genome_sequence");
                            loadRunner.index("genome_sequence");
                            break;
                        case "gene":
                            loadRunner.load(input.resolve("gene.json.gz"), "gene");
                            loadRunner.index("gene");
                            break;
                        case "variation":
                            loadVariationData();
                            break;
                        case "variation_functional_score":
                            loadRunner.load(input.resolve("cadd.json.gz"), "cadd");
                            loadRunner.index("variation_functional_score");
                            break;
                        case "conservation":
                            loadConservation();
                            break;
                        case "regulatory_region":
                            loadRunner.load(input.resolve("regulatory_region.json.gz"), "regulatory_region");
                            loadRunner.index("regulatory_region");
                            break;
                        case "protein":
                            loadRunner.load(input.resolve("protein.json.gz"), "protein");
                            loadRunner.index("protein");
                            break;
                        case "ppi":
                            loadRunner.load(input.resolve("protein_protein_interaction.json.gz"), "protein_protein_interaction");
                            loadRunner.index("protein_protein_interaction");
                            break;
                        case "protein_functional_prediction":
                            loadProteinFunctionalPrediction();
                            break;
                        case "clinical":
                            loadClinical();
                            break;
                        default:
                            logger.warn("Not valid 'data'. We should not reach this point");
                            break;
                    }
                } catch (IllegalAccessException | InstantiationException | InvocationTargetException | ExecutionException
                        | NoSuchMethodException | InterruptedException | ClassNotFoundException | LoaderException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkParameters() {
        if (loadCommandOptions.numThreads > 1) {
            numThreads = loadCommandOptions.numThreads;
        } else {
            numThreads = 1;
            logger.warn("Incorrect number of numThreads, it must be a positive value. This has been set to '{}'", numThreads);
        }

        if (field != null) {
            if (loadCommandOptions.data == null) {
                logger.error("--data option cannot be empty. Please provide a valid value for the --data parameter.");
            } else if (!Files.exists(input)) {
                logger.error("Input parameter {} does not exist", input);
            }
        } else if (!Files.exists(input) || !Files.isDirectory(input)) {
            logger.error("Input parameter {} does not exist or is not a directory", input);
        }
        try {
            Class.forName(loader);
        } catch (ClassNotFoundException e) {
            logger.error("Loader Java class '{}' does not exist", loader);
            e.printStackTrace();
            System.exit(-1);
        }
    }


    private void loadVariationData() throws NoSuchMethodException, InterruptedException, ExecutionException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IOException, LoaderException {

        // Common loading process from CellBase variation data models
        if (field == null) {
            DirectoryStream<Path> stream = Files.newDirectoryStream(input, entry -> {
                return entry.getFileName().toString().startsWith("variation_chr");
            });

            for (Path entry : stream) {
                logger.info("Loading file '{}'", entry.toString());
                loadRunner.load(input.resolve(entry.getFileName()), "variation");
            }
            loadRunner.index("variation");
            // Custom update required e.g. population freqs loading
        } else {
            logger.info("Loading file '{}'", input.toString());
            loadRunner.load(input, "variation", field);
        }
    }

    private void loadConservation() throws NoSuchMethodException, InterruptedException, ExecutionException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IOException, LoaderException {

        DirectoryStream<Path> stream = Files.newDirectoryStream(input, entry -> {
            return entry.getFileName().toString().startsWith("conservation_");
        });

        for (Path entry : stream) {
            logger.info("Loading file '{}'", entry.toString());
            loadRunner.load(input.resolve(entry.getFileName()), "conservation");
        }
        loadRunner.index("conservation");
    }

    private void loadProteinFunctionalPrediction() throws NoSuchMethodException, InterruptedException, ExecutionException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IOException, LoaderException {

        DirectoryStream<Path> stream = Files.newDirectoryStream(input, entry -> {
            return entry.getFileName().toString().startsWith("prot_func_pred_");
        });

        for (Path entry : stream) {
            logger.info("Loading file '{}'", entry.toString());
            loadRunner.load(input.resolve(entry.getFileName()), "protein_functional_prediction");
        }
        loadRunner.index("protein_functional_prediction");
    }

    private void loadClinical() throws NoSuchMethodException, IllegalAccessException, InstantiationException,
            LoaderException, InvocationTargetException, ClassNotFoundException {

        Map<String, String> files = new LinkedHashMap<>();
        files.put("clinvar", "clinvar.json.gz");
        files.put("cosmic", "cosmic.json.gz");
        files.put("gwas", "gwas.json.gz");

        files.keySet().forEach(entry -> {
            Path path = input.resolve(files.get(entry));
            if (Files.exists(path)) {
                try {
                    logger.debug("Loading '{}' ...", entry);
                    loadRunner.load(path, entry);
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException
                        | IllegalAccessException | ExecutionException | IOException | InterruptedException e) {
                    logger.error(e.toString());
                }
            }
        });

        loadRunner.index("clinical");
    }

}
