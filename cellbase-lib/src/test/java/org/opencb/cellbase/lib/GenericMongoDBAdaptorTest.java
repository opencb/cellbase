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

import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by fjlopez on 18/09/15.
 */
public class GenericMongoDBAdaptorTest {

    private static final String LOCALHOST = "localhost:27017";
    protected static final String GRCH37_DBNAME = "cellbase_hsapiens_grch37_v4";
    private static final String MONGODB_CELLBASE_LOADER = "org.opencb.cellbase.lib.loader.MongoDBCellBaseLoader";
    protected CellBaseConfiguration cellBaseConfiguration;
    protected CellBaseManagerFactory cellBaseManagerFactory;

    protected final LoadRunner loadRunner;
    protected MongoDBAdaptorFactory dbAdaptorFactory;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public GenericMongoDBAdaptorTest() throws IOException {
        cellBaseConfiguration = CellBaseConfiguration.load(
                GenericMongoDBAdaptorTest.class.getClassLoader().getResourceAsStream("configuration.test.yaml"),
                CellBaseConfiguration.ConfigurationFileFormat.YAML);
        dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        loadRunner = new LoadRunner(MONGODB_CELLBASE_LOADER, GRCH37_DBNAME, 2, cellBaseConfiguration);
        cellBaseManagerFactory = new CellBaseManagerFactory(cellBaseConfiguration);
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
