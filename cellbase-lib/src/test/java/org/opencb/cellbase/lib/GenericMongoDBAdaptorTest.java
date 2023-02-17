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
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Created by fjlopez on 18/09/15.
 */
public class GenericMongoDBAdaptorTest {

    protected int dataRelease = 1;

    private static final String LOCALHOST = "localhost:27017";
    protected static final String SPECIES = "hsapiens";
    protected static final String ASSEMBLY = "grch37";
    protected static final String API_VERSION = "v4";
    protected static final String CELLBASE_DBNAME = "cellbase_" + SPECIES + "_" + ASSEMBLY + "_" + API_VERSION;
    private static final String MONGODB_CELLBASE_LOADER = "org.opencb.cellbase.lib.loader.MongoDBCellBaseLoader";
    protected CellBaseConfiguration cellBaseConfiguration;
    protected CellBaseManagerFactory cellBaseManagerFactory;

    protected LoadRunner loadRunner = null;
//    protected MongoDBAdaptorFactory dbAdaptorFactory;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public GenericMongoDBAdaptorTest() {
        try {
            cellBaseConfiguration = CellBaseConfiguration.load(
                    GenericMongoDBAdaptorTest.class.getClassLoader().getResourceAsStream("configuration.test.yaml"),
                    CellBaseConfiguration.ConfigurationFileFormat.YAML);
            loadRunner = new LoadRunner(MONGODB_CELLBASE_LOADER, CELLBASE_DBNAME, 2, cellBaseConfiguration);
            cellBaseManagerFactory = new CellBaseManagerFactory(cellBaseConfiguration);
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
