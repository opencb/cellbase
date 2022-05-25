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
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.impl.core.CellBaseDBAdaptor;
import org.opencb.cellbase.lib.indexer.IndexManager;
import org.opencb.cellbase.lib.loader.LoadRunner;
import org.opencb.cellbase.lib.loader.LoaderException;
import org.opencb.cellbase.lib.managers.DataReleaseManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by imedina on 03/02/15.
 */
public class LoadCommandExecutor extends CommandExecutor {

    private static final String METADATA = "metadata";
    private LoadRunner loadRunner;
    private AdminCliOptionsParser.LoadCommandOptions loadCommandOptions;

    private Path input;
    private String[] loadOptions;
    private int dataRelease;

    private String database;
    private String field;
    private String[] innerFields;
    private String loader;
    private int numThreads;
    private boolean createIndexes;
    private IndexManager indexManager;
    private DataReleaseManager dataReleaseManager;

    public LoadCommandExecutor(AdminCliOptionsParser.LoadCommandOptions loadCommandOptions) {
        super(loadCommandOptions.commonOptions.logLevel, loadCommandOptions.commonOptions.conf);

        this.loadCommandOptions = loadCommandOptions;

        input = Paths.get(loadCommandOptions.input);
        if (loadCommandOptions.database != null) {
            database = loadCommandOptions.database;
        }
        if (loadCommandOptions.data.equals("all")) {
            loadOptions = new String[]{EtlCommons.GENOME_DATA, EtlCommons.GENE_DATA, EtlCommons.REFSEQ_DATA,
                    EtlCommons.CONSERVATION_DATA, EtlCommons.REGULATION_DATA, EtlCommons.PROTEIN_DATA,
                    EtlCommons.PROTEIN_FUNCTIONAL_PREDICTION_DATA, EtlCommons.VARIATION_DATA,
                    EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA, EtlCommons.CLINICAL_VARIANTS_DATA, EtlCommons.REPEATS_DATA,
                    EtlCommons.OBO_DATA, EtlCommons.MISSENSE_VARIATION_SCORE_DATA, EtlCommons.SPLICE_SCORE_DATA};
        } else {
            loadOptions = loadCommandOptions.data.split(",");
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
        createIndexes = !loadCommandOptions.skipIndex;
    }

    /**
     * Parse specific 'data' command options.
     *
     * @throws CellBaseException CellBase exception
     */
    public void execute() throws CellBaseException {
        // Init release manager
        dataReleaseManager = new DataReleaseManager(database, configuration);

        checkParameters();
        logger.info("Loading in data release " + dataRelease);

        if (loadCommandOptions.data != null) {
            // If 'authenticationDatabase' is not passed by argument then we read it from configuration.json
            if (loadCommandOptions.loaderParams.containsKey("authenticationDatabase")) {
                configuration.getDatabases().getMongodb().getOptions().put("authenticationDatabase",
                        loadCommandOptions.loaderParams.get("authenticationDatabase"));
            }
            loadRunner = new LoadRunner(loader, database, numThreads, configuration);
            if (createIndexes) {
                Path indexFile = Paths.get(this.appHome).resolve("conf").resolve("mongodb-indexes.json");
                indexManager = new IndexManager(database, indexFile, configuration);
            }

            for (int i = 0; i < loadOptions.length; i++) {
                String loadOption = loadOptions[i];
                try {
                    switch (loadOption) {
                        case EtlCommons.GENOME_DATA: {
                            // Load data
                            loadIfExists(input.resolve("genome_info.json"), "genome_info");
                            loadIfExists(input.resolve("genome_sequence.json.gz"), "genome_sequence");

                            // Create index
                            createIndex("genome_info");
                            createIndex("genome_sequence");

                            // Update release (collection and sources)
                            List<Path> sources = new ArrayList<>(Arrays.asList(
                                    input.resolve("genomeVersion.json")
                            ));
                            dataReleaseManager.update(dataRelease, "genome_info", EtlCommons.GENOME_DATA, sources);
                            dataReleaseManager.update(dataRelease, "genome_sequence", null, null);
                            break;
                        }
                        case EtlCommons.GENE_DATA: {
                            // Load data
                            loadIfExists(input.resolve("gene.json.gz"), "gene");

                            // Create index
                            createIndex("gene");

                            // Update release (collection and sources)
                            List<Path> sources = new ArrayList<>(Arrays.asList(
                                    input.resolve("dgidbVersion.json"),
                                    input.resolve("ensemblCoreVersion.json"),
                                    input.resolve("uniprotXrefVersion.json"),
                                    input.resolve("geneExpressionAtlasVersion.json"),
                                    input.resolve("hpoVersion.json"),
                                    input.resolve("disgenetVersion.json"),
                                    input.resolve("gnomadVersion.json")
                            ));
                            dataReleaseManager.update(dataRelease, "gene", EtlCommons.GENE_DATA, sources);
                            break;
                        }
                        case EtlCommons.REFSEQ_DATA: {
                            // Load data
                            loadIfExists(input.resolve("refseq.json.gz"), "refseq");

                            // Create index
                            createIndex("refseq");

                            // Update release (collection and sources)
                            List<Path> sources = new ArrayList<>(Arrays.asList(
                                    input.resolve("refseqVersion.json")

                            ));
                            dataReleaseManager.update(dataRelease, "refseq", EtlCommons.REFSEQ_DATA, sources);
                            break;
                        }
                        case EtlCommons.VARIATION_DATA: {
                            // Load data, create index and update release
                            loadVariationData();
                            break;
                        }
                        case EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA: {
                            // Load data
                            loadIfExists(input.resolve("cadd.json.gz"), "variation_functional_score");

                            // Create index
                            createIndex("variation_functional_score");

                            // Update release (collection and sources)
                            List<Path> sources = new ArrayList<>(Arrays.asList(
                                    input.resolve("caddVersion.json")
                            ));
                            dataReleaseManager.update(dataRelease, "variation_functional_score", EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA,
                                    sources);
                            break;
                        }
                        case EtlCommons.MISSENSE_VARIATION_SCORE_DATA: {
                            // Load data
                            loadIfExists(input.resolve("missense_variation_functional_score.json.gz"),
                                    "missense_variation_functional_score");

                            // Create index
                            createIndex("missense_variation_functional_score");

                            // Update release (collection and sources)
                            List<Path> sources = new ArrayList<>(Arrays.asList(
                                    input.resolve("revelVersion.json")
                            ));
                            dataReleaseManager.update(dataRelease, "missense_variation_functional_score",
                                    EtlCommons.MISSENSE_VARIATION_SCORE_DATA, sources);
                            break;
                        }
                        case EtlCommons.CONSERVATION_DATA: {
                            // Load data, create index and update release
                            loadConservation();
                            break;
                        }
                        case EtlCommons.REGULATION_DATA: {
                            // Load data (regulatory region and regulatory PFM))
                            loadIfExists(input.resolve("regulatory_region.json.gz"), "regulatory_region");
                            loadIfExists(input.resolve("regulatory_pfm.json.gz"), "regulatory_pfm");

                            // Create index
                            createIndex("regulatory_region");
                            createIndex("regulatory_pfm");

                            // Update release (collection and sources)
                            List<Path> sources = new ArrayList<>(Arrays.asList(
                                    input.resolve("ensemblRegulationVersion.json")
                            ));
                            dataReleaseManager.update(dataRelease, "regulatory_region", EtlCommons.REGULATION_DATA, sources);
                            dataReleaseManager.update(dataRelease, "regulatory_pfm", null, null);
                            break;
                        }
                        case EtlCommons.PROTEIN_DATA: {
                            // Load data
                            loadIfExists(input.resolve("protein.json.gz"), "protein");

                            // Create index
                            createIndex("protein");

                            // Update release (collection and sources)
                            List<Path> sources = new ArrayList<>(Arrays.asList(
                                    input.resolve("uniprotVersion.json"),
                                    input.resolve("interproVersion.json")
                            ));
                            dataReleaseManager.update(dataRelease, "protein", EtlCommons.PROTEIN_DATA, sources);
                            break;
                        }
//                        case EtlCommons.PPI_DATA:
//                            loadIfExists(input.resolve("protein_protein_interaction.json.gz"), "protein_protein_interaction");
//                            loadIfExists(input.resolve("intactVersion.json"), METADATA);
//                            createIndex("protein_protein_interaction");
//                            break;
                        case EtlCommons.PROTEIN_FUNCTIONAL_PREDICTION_DATA: {
                            // Load data, create index and update release
                            loadProteinFunctionalPrediction();
                            break;
                        }
                        case EtlCommons.CLINICAL_VARIANTS_DATA: {
                            // Load data, create index and update release
                            loadClinical();
                            break;
                        }
                        case EtlCommons.REPEATS_DATA: {
                            // Load data, create index and update release
                            loadRepeats();
                            break;
                        }
//                        case EtlCommons.STRUCTURAL_VARIANTS_DATA:
//                            loadStructuralVariants();
//                            break;
                        case EtlCommons.OBO_DATA: {
                            // Load data
                            loadIfExists(input.resolve("ontology.json.gz"), "ontology");

                            // Create index
                            createIndex("ontology");

                            // Update release (collection and sources)
                            List<Path> sources = new ArrayList<>(Arrays.asList(
                                    input.resolve(EtlCommons.HPO_VERSION_FILE),
                                    input.resolve(EtlCommons.GO_VERSION_FILE),
                                    input.resolve(EtlCommons.DO_VERSION_FILE)
                            ));
                            dataReleaseManager.update(dataRelease, "ontology", EtlCommons.OBO_DATA, sources);
                            break;
                        }
                        case EtlCommons.SPLICE_SCORE_DATA: {
                            // Load data, create index and update release
                            loadSpliceScores();
                            break;
                        }
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

//    private void loadStructuralVariants() {
//        Path path = input.resolve(EtlCommons.STRUCTURAL_VARIANTS_JSON + ".json.gz");
//        if (Files.exists(path)) {
//            try {
//                logger.debug("Loading '{}' ...", path.toString());
//                loadRunner.load(path, EtlCommons.STRUCTURAL_VARIANTS_DATA);
//                loadIfExists(input.resolve(EtlCommons.DGV_VERSION_FILE), "metadata");
//            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException
//                    | IllegalAccessException | ExecutionException | IOException | InterruptedException e) {
//                logger.error(e.toString());
//            }
//        }
//    }

    private void loadIfExists(Path path, String collection) throws NoSuchMethodException, InterruptedException,
            ExecutionException, InstantiationException, IOException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException {
        File file = new File(path.toString());
        if (file.exists()) {
            if (file.isFile()) {
                loadRunner.load(path, collection, dataRelease);
            } else {
                logger.warn("{} is not a file - skipping", path);
            }
        } else {
            logger.warn("{} does not exist - skipping", path);
        }
    }

    private void checkParameters() throws CellBaseException {
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

        // Check data release
        CellBaseDataResult<DataRelease> dataReleaseResults = dataReleaseManager.getReleases();
        if (CollectionUtils.isEmpty(dataReleaseResults.getResults())) {
            throw new CellBaseException("No data releases are available for database " + database);
        }
        int lastDataRelease = 0;
        int defaultDataRelease = 0;
        for (DataRelease dataRelease : dataReleaseResults.getResults()) {
            if (dataRelease.isActiveByDefault()) {
                defaultDataRelease = dataRelease.getRelease();
            }
            if (dataRelease.getRelease() > lastDataRelease) {
                lastDataRelease = dataRelease.getRelease();
            }
        }
        if (lastDataRelease == defaultDataRelease) {
            throw new CellBaseException("Loading data in the active data release (" + defaultDataRelease + ") is not permitted.");
        }
        dataRelease = lastDataRelease;

//        for (DataRelease dr : dataReleaseResults.getResults()) {
//            if (dr.getRelease() == dataRelease) {
//                for (String loadOption : loadOptions) {
//                    if (dr.getCollections().containsKey(loadOption)) {
//                        String collectionName = CellBaseDBAdaptor.buildCollectionName(loadOption, dataRelease);
//                        if (dr.getCollections().get(loadOption).equals(collectionName)) {
//                           throw new CellBaseException("Impossible load data " + loadOption + " with release " + dataRelease + " since it"
//                                    + " has already been done.");
//                        }
//                    }
//                }
//                break;
//            }
//        }
    }

    private void loadVariationData() throws NoSuchMethodException, InterruptedException, ExecutionException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IOException, LoaderException {
        // First load data
        // Common loading process from CellBase variation data models
        if (field == null) {
            DirectoryStream<Path> stream = Files.newDirectoryStream(input, entry -> {
                return entry.getFileName().toString().startsWith("variation_chr");
            });

            for (Path entry : stream) {
                logger.info("Loading file '{}'", entry.toString());
                loadRunner.load(input.resolve(entry.getFileName()), "variation", dataRelease);
            }

            // Create index
            createIndex("variation");

            // Update release (collection and sources)
            List<Path> sources = new ArrayList<>(Arrays.asList(
                    input.resolve("ensemblVariationVersion.json")
            ));
            dataReleaseManager.update(dataRelease, "variation", EtlCommons.VARIATION_DATA, sources);

            // Custom update required e.g. population freqs loading
        } else {
            logger.info("Loading file '{}'", input.toString());
            loadRunner.load(input, "variation", dataRelease, field, innerFields);
        }
    }

    private void loadConservation() throws NoSuchMethodException, InterruptedException, ExecutionException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IOException {
        // Load data
        DirectoryStream<Path> stream = Files.newDirectoryStream(input, entry -> {
            return entry.getFileName().toString().startsWith("conservation_");
        });
        for (Path entry : stream) {
            logger.info("Loading file '{}'", entry.toString());
            loadRunner.load(input.resolve(entry.getFileName()), "conservation", dataRelease);
        }

        // Create index
        createIndex("conservation");

        // Update release (collection and sources)
        List<Path> sources = new ArrayList<>(Arrays.asList(
                input.resolve("gerpVersion.json"),
                input.resolve("phastConsVersion.json"),
                input.resolve("phyloPVersion.json")
        ));
        dataReleaseManager.update(dataRelease, "conservation", EtlCommons.CONSERVATION_DATA, sources);
    }

    private void loadProteinFunctionalPrediction() throws NoSuchMethodException, InterruptedException, ExecutionException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IOException {
        // Load data
        DirectoryStream<Path> stream = Files.newDirectoryStream(input, entry -> {
            return entry.getFileName().toString().startsWith("prot_func_pred_");
        });
        for (Path entry : stream) {
            logger.info("Loading file '{}'", entry.toString());
            loadRunner.load(input.resolve(entry.getFileName()), "protein_functional_prediction", dataRelease);
        }

        // Create index
        createIndex("protein_functional_prediction");

        // Update release (collection and sources)
        dataReleaseManager.update(dataRelease, "protein_functional_prediction", null, null);
    }

    private void loadClinical() throws FileNotFoundException {
        Path path = input.resolve(EtlCommons.CLINICAL_VARIANTS_ANNOTATED_JSON_FILE);
        if (Files.exists(path)) {
            try {
                // Load data
                logger.info("Loading '{}' ...", path);
                loadRunner.load(path, "clinical_variants", dataRelease);

                // Create index
                createIndex("clinical_variants");

                // Update release (collection and sources)
                List<Path> sources = new ArrayList<>(Arrays.asList(
                        input.resolve("clinvarVersion.json"),
                        input.resolve("cosmicVersion.json"),
                        input.resolve("gwasVersion.json")
                ));
                dataReleaseManager.update(dataRelease, "clinical_variants", EtlCommons.CLINICAL_VARIANTS_DATA, sources);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException
                    | IllegalAccessException | ExecutionException | IOException | InterruptedException e) {
                logger.error(e.toString());
            }
        } else {
            throw new FileNotFoundException("File " + path.toString() + " does not exist");
        }
    }

    private void loadRepeats() {
        Path path = input.resolve(EtlCommons.REPEATS_JSON + ".json.gz");
        if (Files.exists(path)) {
            try {
                // Load data
                logger.debug("Loading '{}' ...", path);
                loadRunner.load(path, "repeats", dataRelease);

                // Create index
                createIndex("repeats");

                // Update release (collection and sources)
                List<Path> sources = new ArrayList<>(Arrays.asList(
                        input.resolve(EtlCommons.TRF_VERSION_FILE),
                        input.resolve(EtlCommons.GSD_VERSION_FILE),
                        input.resolve(EtlCommons.WM_VERSION_FILE)
                ));
                dataReleaseManager.update(dataRelease, "repeats", EtlCommons.REPEATS_DATA, sources);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException
                    | IllegalAccessException | ExecutionException | IOException | InterruptedException e) {
                logger.error(e.toString());
            }
        } else {
            logger.warn("Repeats file {} not found", path.toString());
            logger.warn("No repeats data will be loaded");
        }
    }

    private void loadSpliceScores() throws NoSuchMethodException, InterruptedException, ExecutionException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IOException {
        // Load data
        logger.info("Loading splice scores from '{}'", input);
        // MMSplice scores
        loadSpliceScores(input.resolve(EtlCommons.SPLICE_SCORE_DATA + "/" + EtlCommons.MMSPLICE_SUBDIRECTORY));
        // SpliceAI scores
        loadSpliceScores(input.resolve(EtlCommons.SPLICE_SCORE_DATA + "/" + EtlCommons.SPLICEAI_SUBDIRECTORY));

        // Create index
        createIndex("splice_score");

        // Update release (collection and sources)
        List<Path> sources = new ArrayList<>(Arrays.asList(
                input.resolve(EtlCommons.SPLICE_SCORE_DATA + "/" + EtlCommons.MMSPLICE_VERSION_FILENAME),
                input.resolve(EtlCommons.SPLICE_SCORE_DATA + "/" + EtlCommons.SPLICEAI_VERSION_FILENAME)
        ));
        dataReleaseManager.update(dataRelease, "splice_score", EtlCommons.SPLICE_SCORE_DATA, sources);
    }

    private void loadSpliceScores(Path spliceFolder) throws IOException, ExecutionException, InterruptedException,
            ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Get files from folder
        DirectoryStream<Path> stream = Files.newDirectoryStream(spliceFolder, entry -> {
            return entry.getFileName().toString().startsWith("splice_score_");
        });
        // Load from JSON files
        for (Path entry : stream) {
            logger.info("Loading file '{}'", entry.toString());
            loadRunner.load(spliceFolder.resolve(entry.getFileName()), "splice_score", dataRelease);
        }
    }

    private void createIndex(String collection) {
        if (!createIndexes) {
            return;
        }
        String collectionName = CellBaseDBAdaptor.buildCollectionName(collection, dataRelease);
        logger.info("Loading indexes for '{}' collection ...", collectionName);
        try {
            indexManager.createMongoDBIndexes(Collections.singletonList(collectionName), true);
        } catch (IOException e) {
            logger.error("Error creating index: " +  e.getMessage());
        }
    }
}
