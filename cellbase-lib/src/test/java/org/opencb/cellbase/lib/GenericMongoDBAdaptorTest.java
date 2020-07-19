package org.opencb.cellbase.lib;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.config.DatabaseCredentials;
import org.opencb.cellbase.core.config.Databases;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fjlopez on 18/09/15.
 */
public class GenericMongoDBAdaptorTest {

    private static final String LOCALHOST = "localhost:27017";
    protected static final String GRCH37_DBNAME = "cellbase_hsapiens_grch37_v4";
    private static final String MONGODB_CELLBASE_LOADER = "org.opencb.cellbase.lib.loader.MongoDBCellBaseLoader";

    protected final LoadRunner loadRunner;
    protected DBAdaptorFactory dbAdaptorFactory;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public GenericMongoDBAdaptorTest() throws IOException {

        CellBaseConfiguration cellBaseConfiguration = CellBaseConfiguration
                .load(GenericMongoDBAdaptorTest.class.getClassLoader().getResourceAsStream("configuration.test.json"));
        dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        loadRunner = new LoadRunner(MONGODB_CELLBASE_LOADER, GRCH37_DBNAME, 2, cellBaseConfiguration);
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


    protected QueryResult<Variant> getByVariant(List<QueryResult<Variant>> variantQueryResultList, Variant variant) {
        for (QueryResult<Variant> variantQueryResult : variantQueryResultList) {
            if (variantQueryResult != null) {
                for (Variant variant1 : variantQueryResult.getResult()) {
                    if (sameVariant(variant, variant1)) {
                        return variantQueryResult;
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
