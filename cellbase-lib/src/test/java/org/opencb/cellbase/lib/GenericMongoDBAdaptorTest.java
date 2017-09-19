package org.opencb.cellbase.lib;

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.config.DatabaseCredentials;
import org.opencb.cellbase.core.config.Databases;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;

import java.io.IOException;

/**
 * Created by fjlopez on 18/09/15.
 */
public class GenericMongoDBAdaptorTest {

    private static final String LOCALHOST = "localhost:27017";
    protected static final String GRCH37_DBNAME = "cellbase_hsapiens_grch37_v4";

    protected final LoadRunner loadRunner;
    protected DBAdaptorFactory dbAdaptorFactory;

    public GenericMongoDBAdaptorTest() {

        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();
        DatabaseCredentials credentials = new DatabaseCredentials(LOCALHOST, StringUtils.EMPTY, StringUtils.EMPTY,
                null);
        Databases databases = new Databases(credentials, null);
        cellBaseConfiguration.setDatabases(databases);
//            cellBaseConfiguration = CellBaseConfiguration
//                    .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
        dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        loadRunner = new LoadRunner(loader, database, numThreads, configuration);
    }

    protected void clearDB(String dbName) throws Exception {
        MongoCredentials credentials = getVariantStorageEngine().getMongoCredentials();
        logger.info("Cleaning MongoDB {}", credentials.getMongoDbName());
        try (MongoDataStoreManager mongoManager = new MongoDataStoreManager(credentials.getDataStoreServerAddresses())) {
            mongoManager.get(credentials.getMongoDbName(), credentials.getMongoDBConfiguration());
            mongoManager.drop(credentials.getMongoDbName());
        }
    }


}
