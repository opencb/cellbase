package org.opencb.cellbase.lib;

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.config.DatabaseCredentials;
import org.opencb.cellbase.core.config.Databases;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    public GenericMongoDBAdaptorTest() {

        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();
        DatabaseCredentials credentials = new DatabaseCredentials(LOCALHOST, StringUtils.EMPTY, StringUtils.EMPTY,
                null);
        Databases databases = new Databases(credentials, null);
        cellBaseConfiguration.setDatabases(databases);
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


}
