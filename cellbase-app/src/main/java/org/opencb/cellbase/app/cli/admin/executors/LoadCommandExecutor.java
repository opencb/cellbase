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
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.release.DataRelease;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.lib.loader.LoadRunner;
import org.opencb.cellbase.lib.loader.LoaderException;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.indexer.IndexManager;
import org.opencb.cellbase.lib.managers.ReleaseManager;

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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
        if (loadCommandOptions.dataRelease >= 0) {
            if (loadCommandOptions.dataRelease == 0) {
                logger.warn("Since the parameter data-release is zero, the active release by default will be used");
            }
            dataRelease = loadCommandOptions.dataRelease;
        } else {
            throw new IllegalArgumentException("The input paremeter 'data-release' must be greater or equal to 0. To use the active"
                    + " release by default, set this parameter to 0");
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

        checkParameters();

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
                        case EtlCommons.GENOME_DATA:
                            loadIfExists(input.resolve("genome_info.json"), "genome_info");
                            loadIfExists(input.resolve("genome_sequence.json.gz"), "genome_sequence");
                            loadIfExists(input.resolve("genomeVersion.json"), METADATA);
                            createIndex("genome_sequence");
                            break;
                        case EtlCommons.GENE_DATA:
                            List<Path> sources = new ArrayList<>(Arrays.asList(
                                    input.resolve("dgidbVersion.json"),
                                    input.resolve("ensemblCoreVersion.json"),
                                    input.resolve("uniprotXrefVersion.json"),
                                    input.resolve("geneExpressionAtlasVersion.json"),
                                    input.resolve("hpoVersion.json"),
                                    input.resolve("disgenetVersion.json"),
                                    input.resolve("gnomadVersion.json")
                            ));
                            loadIfExists(input.resolve("gene.json.gz"), "gene", sources);
                            createIndex("gene");
                            break;
                        case EtlCommons.REFSEQ_DATA:
                            loadIfExists(input.resolve("refseq.json.gz"), "refseq");
                            createIndex("refseq");
                            break;
                        case EtlCommons.VARIATION_DATA:
                            loadVariationData();
                            createIndex("variation");
                            break;
                        case EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA:
                            loadIfExists(input.resolve("cadd.json.gz"), "cadd");
                            loadIfExists(input.resolve("caddVersion.json"), METADATA);
                            createIndex("variation_functional_score");
                            break;
                        case EtlCommons.MISSENSE_VARIATION_SCORE_DATA:
                            loadIfExists(input.resolve("missense_variation_functional_score.json.gz"),
                                    "missense_variation_functional_score");
                            loadIfExists(input.resolve("revelVersion.json"), METADATA);
                            createIndex("missense_variation_functional_score");
                            break;
                        case EtlCommons.CONSERVATION_DATA:
                            loadConservation();
                            createIndex("conservation");
                            break;
                        case EtlCommons.REGULATION_DATA:
                            loadIfExists(input.resolve("regulatory_region.json.gz"), "regulatory_region");
                            loadIfExists(input.resolve("ensemblRegulationVersion.json"), METADATA);
                            createIndex("regulatory_region");
                            loadIfExists(input.resolve("regulatory_pfm.json.gz"), "regulatory_pfm");
                            createIndex("regulatory_pfm");
                            break;
                        case EtlCommons.PROTEIN_DATA:
                            loadIfExists(input.resolve("protein.json.gz"), "protein");
                            loadIfExists(input.resolve("uniprotVersion.json"), METADATA);
                            loadIfExists(input.resolve("interproVersion.json"), METADATA);
                            createIndex("protein");
                            break;
//                        case EtlCommons.PPI_DATA:
//                            loadIfExists(input.resolve("protein_protein_interaction.json.gz"), "protein_protein_interaction");
//                            loadIfExists(input.resolve("intactVersion.json"), METADATA);
//                            createIndex("protein_protein_interaction");
//                            break;
                        case EtlCommons.PROTEIN_FUNCTIONAL_PREDICTION_DATA:
                            loadProteinFunctionalPrediction();
                            createIndex("protein_functional_prediction");
                            break;
                        case EtlCommons.CLINICAL_VARIANTS_DATA:
                            loadClinical();
                            createIndex("clinical_variants");
                            break;
                        case EtlCommons.REPEATS_DATA:
                            loadRepeats();
                            createIndex("repeats");
                            break;
//                        case EtlCommons.STRUCTURAL_VARIANTS_DATA:
//                            loadStructuralVariants();
//                            break;
                        case EtlCommons.OBO_DATA:
                            loadIfExists(input.resolve("ontology.json.gz"), "ontology");
                            loadIfExists(input.resolve(EtlCommons.HPO_VERSION_FILE), METADATA);
                            loadIfExists(input.resolve(EtlCommons.GO_VERSION_FILE), METADATA);
                            loadIfExists(input.resolve(EtlCommons.DO_VERSION_FILE), METADATA);
                            createIndex("ontology");
                            break;
                        case EtlCommons.SPLICE_SCORE_DATA:
                            loadSpliceScores();
                            createIndex("splice_score");
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

    @Deprecated
    private void loadIfExists(Path path, String collection) throws NoSuchMethodException, InterruptedException,
            ExecutionException, InstantiationException, IOException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException {
        File file = new File(path.toString());
        if (file.exists()) {
            if (file.isFile()) {
                loadRunner.load(path, collection, dataRelease, null);
            } else {
                logger.warn("{} is not a file - skipping", path.toString());
            }
        } else {
            logger.warn("{} does not exist - skipping", path.toString());
        }
    }

    private void loadIfExists(Path path, String collection, List<Path> sources) throws NoSuchMethodException, InterruptedException,
            ExecutionException, InstantiationException, IOException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException {
        File file = new File(path.toString());
        if (file.exists()) {
            if (file.isFile()) {
                loadRunner.load(path, collection, dataRelease, sources);
            } else {
                logger.warn("{} is not a file - skipping", path.toString());
            }
        } else {
            logger.warn("{} does not exist - skipping", path.toString());
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
        ReleaseManager releaseManager = new ReleaseManager(database, configuration);
        CellBaseDataResult<DataRelease> result = releaseManager.getReleases();
        if (CollectionUtils.isEmpty(result.getResults())) {
            throw new CellBaseException("No data releases are available for database " + database);
        }
        List<Integer> releases = result.getResults().stream().map(dr -> dr.getRelease()).collect(Collectors.toList());
        if (!releases.contains(dataRelease)) {
            throw new IllegalArgumentException("Invalid data release " + dataRelease + " for database " + database + ". Available releases"
                    + " are: " + StringUtils.join(releases, ","));
        }
        for (DataRelease dr : result.getResults()) {
            if (dr.getRelease() == dataRelease) {
                for (String loadOption : loadOptions) {
                    if (dr.getCollections().containsKey(loadOption)) {
                        String collectionName = CellBaseCoreDBAdaptor.getDataReleaseCollectionName(loadOption, dataRelease);
                        if (dr.getCollections().get(loadOption).equals(collectionName)) {
                            throw new CellBaseException("Impossible load data " + loadOption + " with release " + dataRelease + " since it"
                                    + " has already been done.");
                        }
                    }
                }
                break;
            }
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
                loadRunner.load(input.resolve(entry.getFileName()), "variation", dataRelease, null);
            }
            loadIfExists(input.resolve("ensemblVariationVersion.json"), METADATA);
            createIndex("variation");
            // Custom update required e.g. population freqs loading
        } else {
            logger.info("Loading file '{}'", input.toString());
            loadRunner.load(input, "variation", dataRelease, null, field, innerFields);
        }
    }

    private void loadConservation() throws NoSuchMethodException, InterruptedException, ExecutionException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IOException {

        DirectoryStream<Path> stream = Files.newDirectoryStream(input, entry -> {
            return entry.getFileName().toString().startsWith("conservation_");
        });

        for (Path entry : stream) {
            logger.info("Loading file '{}'", entry.toString());
            loadRunner.load(input.resolve(entry.getFileName()), "conservation", dataRelease, null);
        }
        loadIfExists(input.resolve("gerpVersion.json"), METADATA);
        loadIfExists(input.resolve("phastConsVersion.json"), METADATA);
        loadIfExists(input.resolve("phyloPVersion.json"), METADATA);
    }

    private void loadProteinFunctionalPrediction() throws NoSuchMethodException, InterruptedException, ExecutionException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IOException {

        DirectoryStream<Path> stream = Files.newDirectoryStream(input, entry -> {
            return entry.getFileName().toString().startsWith("prot_func_pred_");
        });

        for (Path entry : stream) {
            logger.info("Loading file '{}'", entry.toString());
            loadRunner.load(input.resolve(entry.getFileName()), "protein_functional_prediction", dataRelease, null);
        }
    }

    private void loadClinical() throws FileNotFoundException {
        Path path = input.resolve(EtlCommons.CLINICAL_VARIANTS_ANNOTATED_JSON_FILE);
        if (Files.exists(path)) {
            try {
                logger.info("Loading '{}' ...", path.toString());
                loadRunner.load(path, EtlCommons.CLINICAL_VARIANTS_DATA, dataRelease, null);
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
    }

    private void loadRepeats() {
        Path path = input.resolve(EtlCommons.REPEATS_JSON + ".json.gz");
        if (Files.exists(path)) {
            try {
                logger.debug("Loading '{}' ...", path.toString());
                loadRunner.load(path, EtlCommons.REPEATS_DATA, dataRelease, null);
                loadIfExists(input.resolve(EtlCommons.TRF_VERSION_FILE), METADATA);
                loadIfExists(input.resolve(EtlCommons.GSD_VERSION_FILE), METADATA);
                loadIfExists(input.resolve(EtlCommons.WM_VERSION_FILE), METADATA);
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
        logger.info("Loading splice scores from '{}'", input);

        // MMSplice scores
        loadSpliceScores(input.resolve(EtlCommons.SPLICE_SCORE_DATA + "/" + EtlCommons.MMSPLICE_SUBDIRECTORY),
                EtlCommons.MMSPLICE_VERSION_FILENAME);

        // SpliceAI scores
        loadSpliceScores(input.resolve(EtlCommons.SPLICE_SCORE_DATA + "/" + EtlCommons.SPLICEAI_SUBDIRECTORY),
                EtlCommons.SPLICEAI_VERSION_FILENAME);
    }

    private void loadSpliceScores(Path spliceFolder, String versionFilename) throws IOException, ExecutionException, InterruptedException,
            ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Get files from folder
        DirectoryStream<Path> stream = Files.newDirectoryStream(spliceFolder, entry -> {
            return entry.getFileName().toString().startsWith("splice_score_");
        });
        // Load from JSON files
        for (Path entry : stream) {
            logger.info("Loading file '{}'", entry.toString());
            loadRunner.load(spliceFolder.resolve(entry.getFileName()), EtlCommons.SPLICE_SCORE_DATA, dataRelease, null);
        }
        loadIfExists(input.resolve(EtlCommons.SPLICE_SCORE_DATA + "/" + versionFilename), METADATA);
    }


    private void createIndex(String collectionName) {
        if (!createIndexes) {
            return;
        }
        logger.info("Loading indexes for '{}' collection ...", collectionName);
        try {
            indexManager.createMongoDBIndexes(collectionName, true);
        } catch (IOException e) {
            logger.error("Error creating indexes:" + e);
        }
    }
}
