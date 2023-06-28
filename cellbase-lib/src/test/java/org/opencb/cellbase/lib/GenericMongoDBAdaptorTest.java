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

package org.opencb.cellbase.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections4.CollectionUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.db.MongoDBManager;
import org.opencb.cellbase.lib.impl.core.CellBaseDBAdaptor;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.opencb.cellbase.lib.loader.LoadRunner;
import org.opencb.cellbase.lib.loader.LoaderException;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.cellbase.lib.managers.DataReleaseManager;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.opencb.commons.exec.Command;
import org.opencb.commons.utils.URLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by fjlopez on 18/09/15.
 */
public class GenericMongoDBAdaptorTest {

    private DataReleaseManager dataReleaseManager;
    protected int dataRelease;
    protected String token;

    private static final String DATASET_BASENAME = "cb5.5-dr4";
    private static final String DATASET_EXTENSION = ".tar.gz";
    private static final String DATASET_URL = "http://reports.test.zettagenomics.com/cellbase/test-data/";
    private static final String DATASET_TMP_DIR = "/tmp/cb";

    private static final String LOCALHOST = "localhost:27017";
    protected static final String SPECIES = "hsapiens";
    protected static final String ASSEMBLY = "grch38";
    protected static final String API_VERSION = "v5";
    protected static final String CELLBASE_DBNAME = "cellbase_" + SPECIES + "_" + ASSEMBLY + "_" + API_VERSION;
    private static final String MONGODB_CELLBASE_LOADER = "org.opencb.cellbase.lib.loader.MongoDBCellBaseLoader";
    protected CellBaseConfiguration cellBaseConfiguration;
    protected CellBaseManagerFactory cellBaseManagerFactory;

    protected String UNIVERSAL_ACDES_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzb3VyY2VzIjp7ImNvc21pYyI6OTIyMzM3MjAzNjg1NDc3NTgwNywiaGdtZCI6OTIyMzM3MjAzNjg1NDc3NTgwNywic3BsaWNlYWkiOjkyMjMzNzIwMzY4NTQ3NzU4MDd9LCJ2ZXJzaW9uIjoiMS4wIiwic3ViIjoiWkVUVEFHRU5PTUlDUyIsImlhdCI6MTY4Nzk0NDYxN30.9puZMYMlmbH1qdH4tUW6vvjfdYdLcAq-6Ts6CRlnLAs";
    protected String HGMD_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzb3VyY2VzIjp7ImhnbWQiOjkyMjMzNzIwMzY4NTQ3NzU4MDd9LCJ2ZXJzaW9uIjoiMS4wIiwic3ViIjoiWkVUVEEiLCJpYXQiOjE2NzU4NzI1MDd9.f3JgVRt7_VrifNWTaRMW3aQfrKbtDbIxlzoenJRYJo0";
    protected String COSMIC_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzb3VyY2VzIjp7ImNvc21pYyI6OTIyMzM3MjAzNjg1NDc3NTgwN30sInZlcnNpb24iOiIxLjAiLCJzdWIiOiJaRVRUQUdFTk9NSUNTIiwiaWF0IjoxNjg3OTQ3MDYwfQ.wjEfSmCSxGd4TFuYEzoCUXrDNE7rNPoqfb7BYwRtTlw";
    protected String SPLICEAI_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzb3VyY2VzIjp7InNwbGljZWFpIjo5MjIzMzcyMDM2ODU0Nzc1ODA3fSwidmVyc2lvbiI6IjEuMCIsInN1YiI6IlpFVFRBR0VOT01JQ1MiLCJpYXQiOjE2ODc5NDcwODR9.8CVEQe313N9dP6lKkqRv__mR854VcCvM2RlFMpPtRrk";
    protected String HGMD_COSMIC_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzb3VyY2VzIjp7ImNvc21pYyI6OTIyMzM3MjAzNjg1NDc3NTgwNywiaGdtZCI6OTIyMzM3MjAzNjg1NDc3NTgwN30sInZlcnNpb24iOiIxLjAiLCJzdWIiOiJaRVRUQSIsImlhdCI6MTY3NTg3MjUyN30.NCCFc4SAhjUsN5UU0wXGY6nCZx8jLglvaO1cNZYI0u4";
    protected String HGMD_SPLICEAI_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzb3VyY2VzIjp7ImhnbWQiOjkyMjMzNzIwMzY4NTQ3NzU4MDcsInNwbGljZWFpIjo5MjIzMzcyMDM2ODU0Nzc1ODA3fSwidmVyc2lvbiI6IjEuMCIsInN1YiI6IlpFVFRBR0VOT01JQ1MiLCJpYXQiOjE2ODc5NDcxMDh9.Qa_VRbu6dbrrUlqk7ToVQkIA258R4L_kNtLZaeITRFA";
    protected String COSMIC_SPLICEAI_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzb3VyY2VzIjp7ImNvc21pYyI6OTIyMzM3MjAzNjg1NDc3NTgwNywic3BsaWNlYWkiOjkyMjMzNzIwMzY4NTQ3NzU4MDd9LCJ2ZXJzaW9uIjoiMS4wIiwic3ViIjoiWkVUVEFHRU5PTUlDUyIsImlhdCI6MTY4Nzk0NzEyOX0.7WrMgLVgUP1LKYE6v3tDCVvy4XxQfpMPgVU011t8aPM";

    protected LoadRunner loadRunner = null;
//    protected MongoDBAdaptorFactory dbAdaptorFactory;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public GenericMongoDBAdaptorTest() {
        try {
            cellBaseConfiguration = CellBaseConfiguration.load(
                    GenericMongoDBAdaptorTest.class.getClassLoader().getResourceAsStream("configuration.test.yaml"),
                    CellBaseConfiguration.ConfigurationFileFormat.YAML);
//            cellBaseConfiguration.getDatabases().getMongodb().setHost("localhost:27037");
//            cellBaseConfiguration.getDatabases().getMongodb().setUser("cellbase");
//            cellBaseConfiguration.getDatabases().getMongodb().setPassword("cellbase");
//            cellBaseConfiguration.getDatabases().getMongodb().getOptions().put("authenticationDatabase", "admin");
//            cellBaseConfiguration.getDatabases().getMongodb().getOptions().put("authenticationMechanism", "SCRAM-SHA-256");
            cellBaseManagerFactory = new CellBaseManagerFactory(cellBaseConfiguration);
            loadRunner = new LoadRunner(MONGODB_CELLBASE_LOADER, CELLBASE_DBNAME, 2,
                    cellBaseManagerFactory.getDataReleaseManager(SPECIES, ASSEMBLY), cellBaseConfiguration);
            initDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    protected void clearDB(String dbName) throws Exception {
        logger.info("Deprecated, nothing to do!");
//        logger.info("Cleaning MongoDB {}", dbName);
//        try (MongoDataStoreManager mongoManager = new MongoDataStoreManager(Collections.singletonList(new DataStoreServerAddress("localhost", 27017)))) {
//            MongoDBConfiguration.Builder builder = MongoDBConfiguration.builder();
//            MongoDBConfiguration  mongoDBConfiguration = builder.build();
//            mongoManager.get(dbName, mongoDBConfiguration);
//            mongoManager.drop(dbName);
//        }
    }

    protected void initDB() throws IOException, ExecutionException, ClassNotFoundException,
            InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            URISyntaxException, CellBaseException, LoaderException {
        dataReleaseManager = cellBaseManagerFactory.getDataReleaseManager(SPECIES, ASSEMBLY);
        CellBaseDataResult<DataRelease> results = dataReleaseManager.getReleases();
        List<DataRelease> dataReleaseList = results.getResults();
        if (CollectionUtils.isEmpty(dataReleaseList)) {
            // Download data and populate mongo DB
            downloadAndPopulate();
        } else if (dataReleaseList.size() != 1) {
            throw new CellBaseException("Something wrong with the CellBase dataset, it must contain only ONE data release");
        } else {
            dataRelease = dataReleaseList.get(0).getRelease();
        }
    }

    private void downloadAndPopulate() throws IOException, ExecutionException, ClassNotFoundException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, CellBaseException, LoaderException {
        // Download and uncompress dataset
        URL url = new URL(DATASET_URL + DATASET_BASENAME + DATASET_EXTENSION);
        Path tmpPath = Paths.get(DATASET_TMP_DIR);
        tmpPath.toFile().mkdirs();

        URLUtils.download(url, tmpPath);
        Path tmpFile = tmpPath.resolve(DATASET_BASENAME + DATASET_EXTENSION);
        String commandline = "tar -xvzf " + tmpFile.toAbsolutePath() + " -C " + tmpPath;
        logger.info("Running: " + commandline);
        Command command = new Command(commandline);
        command.run();

        logger.info("Downloading and decompressing " + tmpFile.toAbsolutePath());

        Path baseDir = tmpPath.resolve(DATASET_BASENAME);
        if (!baseDir.toFile().exists() || !baseDir.toFile().isDirectory()) {
            throw new CellBaseException("Something wrong downloading and uncompressing the datasets, please check " + tmpPath);
        }

        // Populate mongoDB from the downloaded dataset
        dataRelease = dataReleaseManager.createRelease().getRelease();

        // Genome:  genome_sequence.json.gz, genome_info.json.gz
        loadData("genome_info", "genome_info", baseDir.resolve("genome_info.json.gz"));
        loadData("genome_sequence", "genome_sequence", baseDir.resolve("genome_sequence.json.gz"));

        // Gene: gene.json.gz, refseq.json.gz
        loadData("gene", "gene", baseDir.resolve("gene.json.gz"));
        loadData("refseq", "refseq", baseDir.resolve("refseq.json.gz"));

        // Conservation
        for (File file : baseDir.toFile().listFiles()) {
            if (file.getName().startsWith("conservation_")) {
                loadData("conservation", "conservation", file.toPath(), true);
            }
        }
        dataReleaseManager.update(dataRelease, "conservation", "conservation", Collections.emptyList());

        // Regulatory regions: regulatory_region.json.gz
        loadData("regulatory_region", "regulatory_region", baseDir.resolve("regulatory_region.json.gz"));

        // Protein: protein.json.gz
        loadData("protein", "protein", baseDir.resolve("protein.json.gz"));

        // Protein functional prediction
        for (File file : baseDir.toFile().listFiles()) {
            if (file.getName().startsWith("prot_func_pred_")) {
                loadData("protein_functional_prediction", "protein_functional_prediction", file.toPath(), true);
            }
        }
        dataReleaseManager.update(dataRelease, "protein_functional_prediction", "protein_functional_prediction", Collections.emptyList());

        // Variation: variation_chr_all.json.gz
        loadData("variation", "variation", baseDir.resolve("variation_chr_all.json.gz"));

        // Variant functional score: cadd.json.gz
        loadData("variation_functional_score", "variation_functional_score", baseDir.resolve("cadd.json.gz"));

        // Repeats: repeats.json.gz
        loadData("repeats", "repeats", baseDir.resolve("repeats.json.gz"));

        // Ontology: ontology.json.gz
        loadData("ontology", "ontology", baseDir.resolve("ontology.json.gz"));

        // Missense variation functional scores: missense_variation_functional_score.json.gz
        loadData("missense_variation_functional_score", "missense_variation_functional_score",
                baseDir.resolve("missense_variation_functional_score.json.gz"));

        // splice_score
        loadData("splice_score", "splice_score", baseDir.resolve("splice_score/spliceai/splice_score_all.json.gz"), true);
        loadData("splice_score", "splice_score", baseDir.resolve("splice_score/mmsplice/splice_score_all.json.gz"), true);
        dataReleaseManager.update(dataRelease, "splice_score", "splice_score", Collections.emptyList());

        // clinical_variants.full.json.gz
        loadData("clinical_variants", "clinical_variants", baseDir.resolve("clinical_variants.full.json.gz"));

        // Clean temporary dir
    }

    private void loadData(String collection, String data, Path filePath) throws IOException, ExecutionException, ClassNotFoundException,
            InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, LoaderException, CellBaseException {
        loadData(collection, data, filePath, false);
    }

    private void loadData(String collection, String data, Path filePath, boolean skipUpdate) throws IOException, ExecutionException,
            ClassNotFoundException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, LoaderException, CellBaseException {
        if (filePath.toFile().exists()) {
            logger.info("Loading (" + collection + ", " + data + ") from file " + filePath);
            loadRunner.load(filePath, collection, dataRelease);
            if (!skipUpdate) {
                dataReleaseManager.update(dataRelease, collection, data, Collections.emptyList());
            }
        } else {
            logger.error("(" + collection + ", " + data + ") not loading: file " + filePath + "does not exist");
        }
    }

    @Deprecated
    protected void initDB_OLD() throws IOException, ExecutionException, ClassNotFoundException,
            InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            URISyntaxException, CellBaseException, LoaderException {
        dataReleaseManager = cellBaseManagerFactory.getDataReleaseManager("hsapiens", "GRCh37");
        dataRelease = dataReleaseManager.createRelease().getRelease();

        Path path = Paths.get(getClass()
                .getResource("/variant-annotation/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene", dataRelease);

        path = Paths.get(getClass()
                .getResource("/hgvs/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene", dataRelease);
        dataReleaseManager.update(dataRelease,"gene", "gene", Collections.emptyList());

        path = Paths.get(getClass()
                .getResource("/variant-annotation/genome_sequence.test.json.gz").toURI());
        loadRunner.load(path, "genome_sequence", dataRelease);

        path = Paths.get(getClass()
                .getResource("/hgvs/genome_sequence.test.json.gz").toURI());
        loadRunner.load(path, "genome_sequence", dataRelease);
        dataReleaseManager.update(dataRelease,"genome_sequence", "genome_sequence", Collections.emptyList());

        path = Paths.get(getClass()
                .getResource("/variant-annotation/regulatory_region.test.json.gz").toURI());
        loadRunner.load(path, "regulatory_region", dataRelease);
        dataReleaseManager.update(dataRelease,"regulatory_region", "regulatory_region", Collections.emptyList());

        path = Paths.get(getClass()
                .getResource("/variant-annotation/protein.test.json.gz").toURI());
        loadRunner.load(path, "protein", dataRelease);
        dataReleaseManager.update(dataRelease,"protein", "protein", Collections.emptyList());

        path = Paths.get(getClass()
                .getResource("/variant-annotation/prot_func_pred_chr_13.test.json.gz").toURI());
        loadRunner.load(path, "protein_functional_prediction", dataRelease);

        path = Paths.get(getClass()
                .getResource("/variant-annotation/prot_func_pred_chr_18.test.json.gz").toURI());
        loadRunner.load(path, "protein_functional_prediction", dataRelease);

        path = Paths.get(getClass()
                .getResource("/variant-annotation/prot_func_pred_chr_19.test.json.gz").toURI());
        loadRunner.load(path, "protein_functional_prediction", dataRelease);

        path = Paths.get(getClass()
                .getResource("/variant-annotation/prot_func_pred_chr_MT.test.json.gz").toURI());
        loadRunner.load(path, "protein_functional_prediction", dataRelease);
        dataReleaseManager.update(dataRelease,"protein_functional_prediction", "protein_functional_prediction", Collections.emptyList());

        path = Paths.get(getClass()
                .getResource("/variant-annotation/variation_chr1.full.test.json.gz").toURI());
        loadRunner.load(path, "variation", dataRelease);

        path = Paths.get(getClass()
                .getResource("/variant-annotation/variation_chr2.full.test.json.gz").toURI());
        loadRunner.load(path, "variation", dataRelease);

        path = Paths.get(getClass()
                .getResource("/variant-annotation/variation_chr19.full.test.json.gz").toURI());
        loadRunner.load(path, "variation", dataRelease);

        path = Paths.get(getClass()
                .getResource("/variant-annotation/variation_chrMT.full.test.json.gz").toURI());
        loadRunner.load(path, "variation", dataRelease);

        path = Paths.get(getClass()
                .getResource("/variant-annotation/structuralVariants.json.gz").toURI());
        loadRunner.load(path, "variation", dataRelease);
        dataReleaseManager.update(dataRelease,"variation", "variation", Collections.emptyList());


        path = Paths.get(getClass()
                .getResource("/variant-annotation/repeats.json.gz").toURI());
        loadRunner.load(path, "repeats", dataRelease);
        dataReleaseManager.update(dataRelease,"repeats", "repeats", Collections.emptyList());

        path = Paths.get(getClass()
                .getResource("/variant-annotation/clinical_variants.test.json.gz").toURI());
        loadRunner.load(path, "clinical_variants", dataRelease);
        path = Paths.get(getClass()
                .getResource("/variant-annotation/clinical_variants.cosmic.test.json.gz").toURI());
        loadRunner.load(path, "clinical_variants", dataRelease);
        dataReleaseManager.update(dataRelease,"clinical_variants", "clinical_variants", Collections.emptyList());

        path = Paths.get(getClass()
                .getResource("/revel/missense_variation_functional_score.json.gz").toURI());
        loadRunner.load(path, "missense_variation_functional_score", dataRelease);
        dataReleaseManager.update(dataRelease, "missense_variation_functional_score", "missense_variation_functional_score", Collections.emptyList());

        // Create empty collection
        createEmptyCollection("refseq", dataRelease);
        dataReleaseManager.update(dataRelease, "refseq", "refseq", Collections.emptyList());

        // Create empty collection
        createEmptyCollection("conservation", dataRelease);
        dataReleaseManager.update(dataRelease, "conservation", "conservation", Collections.emptyList());

        // Create empty collection
        createEmptyCollection("variation_functional_score", dataRelease);
        dataReleaseManager.update(dataRelease, "variation_functional_score", "variation_functional_score", Collections.emptyList());

        // Create empty collection
        createEmptyCollection("splice_score", dataRelease);
        dataReleaseManager.update(dataRelease, "splice_score", "splice_score", Collections.emptyList());
    }

    protected void createDataRelease() throws CellBaseException, JsonProcessingException {
        cellBaseManagerFactory.getDataReleaseManager(SPECIES, ASSEMBLY).createRelease();
    }

    protected void updateDataRelease(int dataRelease, String data, List<Path> sources) throws CellBaseException, JsonProcessingException {
        cellBaseManagerFactory.getDataReleaseManager(SPECIES, ASSEMBLY).update(dataRelease, data, data, sources);
    }

    protected void createEmptyCollection(String data, int dataRelease) {
        MongoDBManager mongoDBManager = new MongoDBManager(cellBaseConfiguration);
        MongoDataStore mongoDataStore = mongoDBManager.createMongoDBDatastore(CELLBASE_DBNAME);
        mongoDataStore.createCollection(CellBaseDBAdaptor.buildCollectionName(data, dataRelease));
    }

    protected CellBaseDataResult<Variant> getByVariant(List<CellBaseDataResult<Variant>> variantCellBaseDataResultList, Variant variant) {
        for (CellBaseDataResult<Variant> variantCellBaseDataResult : variantCellBaseDataResultList) {
            if (variantCellBaseDataResult != null) {
                for (Variant variant1 : variantCellBaseDataResult.getResults()) {
                    if (sameVariant(variant, variant1)) {
                        return variantCellBaseDataResult;
                    }
                }
            }
        }

        return null;
    }

    private boolean sameVariant(Variant variant, Variant variant1) {
        return variant.getChromosome().equals(variant1.getChromosome())
                && variant.getStart().equals(variant1.getStart())
                && variant.getEnd().equals(variant1.getEnd())
                && variant.getReference().equals(variant1.getReference())
                && variant.getAlternate().equals(variant1.getAlternate());
    }
}
