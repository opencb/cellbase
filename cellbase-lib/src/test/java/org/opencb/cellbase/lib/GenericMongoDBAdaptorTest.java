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
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.db.MongoDBManager;
import org.opencb.cellbase.lib.impl.core.CellBaseDBAdaptor;
import org.opencb.cellbase.lib.loader.LoadRunner;
import org.opencb.cellbase.lib.loader.LoaderException;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.cellbase.lib.managers.DataReleaseManager;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
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

    private static final String LOCALHOST = "localhost:27017";
    protected static final String SPECIES = "hsapiens";
    protected static final String ASSEMBLY = "grch37";
    protected static final String API_VERSION = "v5";
    protected static final String CELLBASE_DBNAME = "cellbase_" + SPECIES + "_" + ASSEMBLY + "_" + API_VERSION;
    private static final String MONGODB_CELLBASE_LOADER = "org.opencb.cellbase.lib.loader.MongoDBCellBaseLoader";
    protected CellBaseConfiguration cellBaseConfiguration;
    protected CellBaseManagerFactory cellBaseManagerFactory;

    protected String HGMD_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzb3VyY2VzIjp7ImhnbWQiOjkyMjMzNzIwMzY4NTQ3NzU4MDd9LCJ2ZXJzaW9uIjoiMS4wIiwic3ViIjoiWkVUVEEiLCJpYXQiOjE2NzU4NzI1MDd9.f3JgVRt7_VrifNWTaRMW3aQfrKbtDbIxlzoenJRYJo0";
    protected String HGMD_COSMIC_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzb3VyY2VzIjp7ImNvc21pYyI6OTIyMzM3MjAzNjg1NDc3NTgwNywiaGdtZCI6OTIyMzM3MjAzNjg1NDc3NTgwN30sInZlcnNpb24iOiIxLjAiLCJzdWIiOiJaRVRUQSIsImlhdCI6MTY3NTg3MjUyN30.NCCFc4SAhjUsN5UU0wXGY6nCZx8jLglvaO1cNZYI0u4";

    protected LoadRunner loadRunner = null;
//    protected MongoDBAdaptorFactory dbAdaptorFactory;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public GenericMongoDBAdaptorTest() {
        try {
            cellBaseConfiguration = CellBaseConfiguration.load(
                    GenericMongoDBAdaptorTest.class.getClassLoader().getResourceAsStream("configuration.test.yaml"),
                    CellBaseConfiguration.ConfigurationFileFormat.YAML);
            cellBaseManagerFactory = new CellBaseManagerFactory(cellBaseConfiguration);
            dataReleaseManager = cellBaseManagerFactory.getDataReleaseManager("hsapiens", "GRCh37");
            loadRunner = new LoadRunner(MONGODB_CELLBASE_LOADER, CELLBASE_DBNAME, 2, dataReleaseManager, cellBaseConfiguration);
//        dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void clearDB(String dbName) throws Exception {
        logger.info("Cleaning MongoDB {}", dbName);
        try (MongoDataStoreManager mongoManager = new MongoDataStoreManager(Collections.singletonList(new DataStoreServerAddress("localhost", 27017)))) {
            MongoDBConfiguration.Builder builder = MongoDBConfiguration.builder();
            MongoDBConfiguration  mongoDBConfiguration = builder.build();
            mongoManager.get(dbName, mongoDBConfiguration);
            mongoManager.drop(dbName);
        }
    }

    protected void initDB() throws IOException, ExecutionException, ClassNotFoundException,
            InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            URISyntaxException, CellBaseException, LoaderException {
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
                .getResource("/genome/genome_info.json").toURI());
        loadRunner.load(path, "genome_info", dataRelease);
        dataReleaseManager.update(dataRelease,"genome_info", "genome_info", Collections.emptyList());

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
