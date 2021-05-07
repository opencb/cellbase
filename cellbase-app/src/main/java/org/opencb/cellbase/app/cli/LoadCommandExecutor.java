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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

/**
 * Created by imedina on 03/02/15.
 */
public class LoadCommandExecutor extends CommandExecutor {

    private static final String METADATA = "metadata";
    private LoadRunner loadRunner;
    private CliOptionsParser.LoadCommandOptions loadCommandOptions;

    private Path input;

    private String database;
    private String field;
    private String[] innerFields;
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
        if (loadCommandOptions.innerFields != null) {
            innerFields = loadCommandOptions.innerFields.split(",");
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
                configuration.getDatabases().getMongodb().getOptions().put("mongodb-index-folder",
                        loadCommandOptions.loaderParams.get("mongodb-index-folder"));
            }
            // If 'authenticationDatabase' is not passed by argument then we read it from configuration.json
            if (loadCommandOptions.loaderParams.containsKey("authenticationDatabase")) {
                configuration.getDatabases().getMongodb().getOptions().put("authenticationDatabase",
                        loadCommandOptions.loaderParams.get("authenticationDatabase"));
            }

//                loadRunner = new LoadRunner(loader, database, loadCommandOptions.loaderParams, numThreads, configuration);
            loadRunner = new LoadRunner(loader, database, numThreads, configuration);

            String[] loadOptions;
            if (loadCommandOptions.data.equals("all")) {
                loadOptions = new String[]{EtlCommons.GENOME_DATA, EtlCommons.GENE_DATA, EtlCommons.CONSERVATION_DATA,
                        EtlCommons.REGULATION_DATA, EtlCommons.PROTEIN_DATA, EtlCommons.PPI_DATA,
                        EtlCommons.PROTEIN_FUNCTIONAL_PREDICTION_DATA, EtlCommons.VARIATION_DATA,
                        EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA, EtlCommons.CLINICAL_VARIANTS_DATA,
                        EtlCommons.REPEATS_DATA, EtlCommons.STRUCTURAL_VARIANTS_DATA, };
            } else {
                loadOptions = loadCommandOptions.data.split(",");
            }

            for (int i = 0; i < loadOptions.length; i++) {
                String loadOption = loadOptions[i];
                try {
                    switch (loadOption) {
                        case EtlCommons.GENOME_DATA:
                            loadIfExists(input.resolve("genome_info.json"), "genome_info");
                            loadIfExists(input.resolve("genome_sequence.json.gz"), "genome_sequence");
                            loadIfExists(input.resolve("genomeVersion.json"), METADATA);
                            loadRunner.index("genome_sequence");
                            break;
                        case EtlCommons.GENE_DATA:
                            loadIfExists(input.resolve("gene.json.gz"), "gene");
                            loadIfExists(input.resolve("dgidbVersion.json"), METADATA);
                            loadIfExists(input.resolve("ensemblCoreVersion.json"), METADATA);
                            loadIfExists(input.resolve("uniprotXrefVersion.json"), METADATA);
                            loadIfExists(input.resolve("geneExpressionAtlasVersion.json"), METADATA);
                            loadIfExists(input.resolve("hpoVersion.json"), METADATA);
                            loadIfExists(input.resolve("disgenetVersion.json"), METADATA);
                            loadRunner.index("gene");
                            break;
                        case EtlCommons.VARIATION_DATA:
                            loadVariationData();
                            break;
                        case EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA:
                            loadIfExists(input.resolve("cadd.json.gz"), "cadd");
                            loadIfExists(input.resolve("caddVersion.json"), METADATA);
                            loadRunner.index("variation_functional_score");
                            break;
                        case EtlCommons.CONSERVATION_DATA:
                            loadConservation();
                            break;
                        case EtlCommons.REGULATION_DATA:
                            loadIfExists(input.resolve("regulatory_region.json.gz"), "regulatory_region");
                            loadIfExists(input.resolve("ensemblRegulationVersion.json"), METADATA);
                            loadIfExists(input.resolve("mirbaseVersion.json"), METADATA);
                            loadIfExists(input.resolve("targetScanVersion.json"), METADATA);
                            loadIfExists(input.resolve("miRTarBaseVersion.json"), METADATA);
                            loadRunner.index("regulatory_region");
                            break;
                        case EtlCommons.PROTEIN_DATA:
                            loadIfExists(input.resolve("protein.json.gz"), "protein");
                            loadIfExists(input.resolve("uniprotVersion.json"), METADATA);
                            loadIfExists(input.resolve("interproVersion.json"), METADATA);
                            loadRunner.index("protein");
                            break;
                        case EtlCommons.PPI_DATA:
                            loadIfExists(input.resolve("protein_protein_interaction.json.gz"), "protein_protein_interaction");
                            loadIfExists(input.resolve("intactVersion.json"), METADATA);
                            loadRunner.index("protein_protein_interaction");
                            break;
                        case EtlCommons.PROTEIN_FUNCTIONAL_PREDICTION_DATA:
                            loadProteinFunctionalPrediction();
                            break;
                        case EtlCommons.CLINICAL_VARIANTS_DATA:
                            loadClinical();
                            break;
                        case EtlCommons.REPEATS_DATA:
                            loadRepeats();
                            break;
                        case EtlCommons.STRUCTURAL_VARIANTS_DATA:
                            loadStructuralVariants();
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

    private void loadStructuralVariants() throws NoSuchMethodException, IllegalAccessException, InstantiationException,
            LoaderException, InvocationTargetException, ClassNotFoundException {
        Path path = input.resolve(EtlCommons.STRUCTURAL_VARIANTS_JSON + ".json.gz");
        if (Files.exists(path)) {
            try {
                logger.debug("Loading '{}' ...", path.toString());
                loadRunner.load(path, EtlCommons.STRUCTURAL_VARIANTS_DATA);
                loadIfExists(input.resolve(EtlCommons.DGV_VERSION_FILE), "metadata");
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException
                    | IllegalAccessException | ExecutionException | IOException | InterruptedException e) {
                logger.error(e.toString());
            }
        }
        // Assuming variation collection is already indexed
//        loadRunner.index("variation");
    }

    private void loadIfExists(Path path, String collection) throws NoSuchMethodException, InterruptedException,
            ExecutionException, InstantiationException, IOException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException {
        File file = new File(path.toString());
        if (file.exists()) {
            if (file.isFile()) {
                loadRunner.load(path, collection);
            } else {
                logger.warn("{} is not a file - skipping", path.toString());
            }
        } else {
            logger.warn("{} does not exist - skipping", path.toString());
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
            loadIfExists(input.resolve("ensemblVariationVersion.json"), METADATA);
            loadRunner.index("variation");
            // Custom update required e.g. population freqs loading
        } else {
            logger.info("Loading file '{}'", input.toString());
            loadRunner.load(input, "variation", field, innerFields);
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
        loadIfExists(input.resolve("gerpVersion.json"), METADATA);
        loadIfExists(input.resolve("phastConsVersion.json"), METADATA);
        loadIfExists(input.resolve("phyloPVersion.json"), METADATA);
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
            LoaderException, InvocationTargetException, ClassNotFoundException, FileNotFoundException {

//        Map<String, String> files = new LinkedHashMap<>();
//        files.put("clinvar", "clinvar.json.gz");
//        files.put("cosmic", "cosmic.json.gz");
//        files.put("gwas", "gwas.json.gz");
//        files.put("gwas", "gwas.json.gz");

//        files.keySet().forEach(entry -> {
//            Path path = input.resolve(files.get(entry));
        Path path = input.resolve(EtlCommons.CLINICAL_VARIANTS_ANNOTATED_JSON_FILE);
        if (Files.exists(path)) {
            try {
                logger.info("Loading '{}' ...", path.toString());
                loadRunner.load(path, EtlCommons.CLINICAL_VARIANTS_DATA);
                loadIfExists(input.resolve("clinvarVersion.json"), "metadata");
                loadIfExists(input.resolve("cosmicVersion.json"), "metadata");
                loadIfExists(input.resolve("gwasVersion.json"), "metadata");
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException
                    | IllegalAccessException | ExecutionException | IOException | InterruptedException e) {
                logger.error(e.toString());
            }
        } else {
            throw new FileNotFoundException("File " + path.toString() + " does not exist");
        }
//        });
        loadRunner.index("clinical_variants");
//        loadRunner.index("clinical");
    }

    private void loadRepeats() throws NoSuchMethodException, IllegalAccessException, InstantiationException,
            LoaderException, InvocationTargetException, ClassNotFoundException {

        Path path = input.resolve(EtlCommons.REPEATS_JSON + ".json.gz");
        if (Files.exists(path)) {
            try {
                logger.debug("Loading '{}' ...", path.toString());
                loadRunner.load(path, EtlCommons.REPEATS_DATA);
                loadIfExists(input.resolve(EtlCommons.TRF_VERSION_FILE), METADATA);
                loadIfExists(input.resolve(EtlCommons.GSD_VERSION_FILE), METADATA);
                loadIfExists(input.resolve(EtlCommons.WM_VERSION_FILE), METADATA);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException
                    | IllegalAccessException | ExecutionException | IOException | InterruptedException e) {
                logger.error(e.toString());
            }
            loadRunner.index(EtlCommons.REPEATS_DATA);
        } else {
            logger.warn("Repeats file {} not found", path.toString());
            logger.warn("No repeats data will be loaded");
        }
//        Map<String, String> files = new LinkedHashMap<>();
//        files.put("simpleRepeat", "simpleRepeat.json.gz");
//        files.put("genomicSuperDup", "genomicSuperDup.json.gz");
//        files.put("windowMasker", "windowMasker.json.gz");
//
//        files.keySet().forEach(entry -> {
//            Path path = input.resolve(files.get(entry));
//            if (Files.exists(path)) {
//                try {
//                    logger.debug("Loading '{}' ...", entry);
//                    loadRunner.load(path, entry);
//                    loadIfExists(input.resolve(EtlCommons.TRF_VERSION_FILE), "metadata");
//                    loadIfExists(input.resolve(EtlCommons.GSD_VERSION_FILE), "metadata");
//                    loadIfExists(input.resolve(EtlCommons.WM_VERSION_FILE), "metadata");
//                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException
//                        | IllegalAccessException | ExecutionException | IOException | InterruptedException e) {
//                    logger.error(e.toString());
//                }
//            }
//        });

    }

}
