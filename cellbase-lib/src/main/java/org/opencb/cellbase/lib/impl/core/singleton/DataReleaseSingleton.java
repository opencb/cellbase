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

package org.opencb.cellbase.lib.impl.core.singleton;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.lib.db.MongoDBManager;
import org.opencb.cellbase.lib.impl.core.ReleaseMongoDBAdaptor;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class DataReleaseSingleton {

    // Map where the key is dbname and the value is the DatabaseInfo
    private Map<String, DatabaseInfo> dbInfoMap = new HashMap<>();

    private CellBaseManagerFactory managerFactory;

    private static DataReleaseSingleton instance;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataReleaseSingleton.class);

    public static final String UNKOWN_DATABASE_MSG_PREFIX = "Unknown database ";
    public static final String INVALID_RELEASE_MSG_PREFIX = "Invalid release ";
    public static final String INVALID_DATA_MSG_PREFIX = "Invalid data ";

    // Private constructor to prevent instantiation
    private DataReleaseSingleton(CellBaseManagerFactory managerFactory) throws CellBaseException {
        this.managerFactory = managerFactory;

        // Support multi species and assemblies
        CellBaseConfiguration configuration = managerFactory.getConfiguration();
        for (SpeciesConfiguration vertebrate : configuration.getSpecies().getVertebrates()) {
            for (SpeciesConfiguration.Assembly assembly : vertebrate.getAssemblies()) {
                String databaseName = MongoDBManager.getDatabaseName(vertebrate.getId(), assembly.getName(), configuration.getVersion());
                // This is necessary, before creating the database name the assembly is "cleaned", and we need to get the data release
                // manager from the species and the assembly
                DatabaseInfo dbInfo = new DatabaseInfo(databaseName, vertebrate.getId(), assembly.getName(), new ReentrantReadWriteLock(),
                        new HashMap<>());
                dbInfoMap.put(databaseName, dbInfo);

                MongoClient mongoClient = managerFactory.getDataReleaseManager(vertebrate.getId(), assembly.getName()).getMongoDatastore()
                        .getMongoClient();
                MongoDatabase database = mongoClient.getDatabase(databaseName);
                MongoCollection<Document> collection = database.getCollection(ReleaseMongoDBAdaptor.DATA_RELEASE_COLLECTION_NAME);
                LOGGER.info("Setting listener for database {} and collection {}", database.getName(), collection.getNamespace()
                        .getCollectionName());
                // Set up the change stream for the collection
                new Thread(() -> {
                    while (true) {
                        try {
                            collection.watch().fullDocument(FullDocument.UPDATE_LOOKUP).forEach(changeStreamDocument -> {
                                try {
                                    handleDocumentChange(changeStreamDocument);
                                } catch (CellBaseException e) {
                                    LOGGER.warn("Exception from handle document change function (database = {}, collection = {}): {}",
                                            collection.getNamespace().getDatabaseName(), collection.getNamespace().getCollectionName(),
                                            Arrays.toString(e.getStackTrace()));
                                }
                            });
                        } catch (Exception e) {
                            LOGGER.error("Failed to watch collection (database = {}, collection = {}): {}",
                                    collection.getNamespace().getDatabaseName(), collection.getNamespace().getCollectionName(),
                                    Arrays.toString(e.getStackTrace()));
                            try {
                                // Sleep 5 sec before retrying to avoid tight loop
                                Thread.sleep(5000);
                            } catch (InterruptedException ie) {
                                // Restore interrupt status
                                Thread.currentThread().interrupt();
                                break; // Exit loop if interrupted
                            }
                        }
                    }
                }).start();
            }
        }
    }

    // Initialization method to set up the instance with parameters
    public static synchronized void initialize(CellBaseManagerFactory managerFactory) throws CellBaseException {
        if (instance == null) {
            instance = new DataReleaseSingleton(managerFactory);
        }
    }

    // Method to get the single instance of the class
    public static DataReleaseSingleton getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Singleton not initialized. Call the function 'initialize' first.");
        }
        return instance;
    }

    // Method to load data from MongoDB and cache it
    private void loadData(String dbname) throws CellBaseException {
        DatabaseInfo dbInfo = getDatabaseInfo(dbname);

        String species = dbInfo.getSpecies();
        String assembly = dbInfo.getAssembly();
        ReleaseMongoDBAdaptor releaseMongoDBAdaptor = managerFactory.getDataReleaseManager(species, assembly).getReleaseDBAdaptor();
        List<DataRelease> dataReleases = releaseMongoDBAdaptor.getAll().getResults();
        if (CollectionUtils.isNotEmpty(dataReleases)) {
            for (DataRelease dataRelease : dataReleases) {
                Map<String, MongoDBCollection> collectionMap = new HashMap<>();
                for (Map.Entry<String, String> entry : dataRelease.getCollections().entrySet()) {
                    collectionMap.put(entry.getKey(), releaseMongoDBAdaptor.getMongoDataStore().getCollection(entry.getValue()));
                }
                dbInfo.getCacheData().put(dataRelease.getRelease(), collectionMap);
            }
        }
    }

    public void checkDataRelease(String species, String assembly, String version, int release) throws CellBaseException {
        checkDataRelease(MongoDBManager.getDatabaseName(species, assembly, version), release);
    }

    public void checkDataRelease(String dbname, int release) throws CellBaseException {
        checkDataRelease(dbname, release, null);
    }

    public void checkDataRelease(String dbname, int release, String data) throws CellBaseException {
        DatabaseInfo dbInfo = getDatabaseInfo(dbname);

        // Lock and load data if necessary
        dbInfo.getRwLock().writeLock().lock();
        try {
            if (!dbInfo.getCacheData().containsKey(release)
                    || (StringUtils.isNotEmpty(data) && !dbInfo.getCacheData().get(release).containsKey(data))) {
                // Load the data releases from the MongoDB collection for that database name
                loadData(dbname);

                // Check after loading
                if (!dbInfo.getCacheData().containsKey(release)) {
                    // If the release is invalid, throw an exception
                    String msg = INVALID_RELEASE_MSG_PREFIX + release + ". The available data releases are: "
                            + dbInfo.getCacheData().keySet();
                    throw new CellBaseException(msg);
                }
                if (StringUtils.isNotEmpty(data) && !dbInfo.getCacheData().get(release).containsKey(data)) {
                    // If the data is invalid, throw an exception
                    String msg = INVALID_DATA_MSG_PREFIX + " '" + data + "', it's not present in release " + release
                            + ". The available data are: " + dbInfo.getCacheData().get(release).keySet();
                    throw new CellBaseException(msg);
                }
            }
        } finally {
            dbInfo.getRwLock().writeLock().unlock();
        }
    }

    // Method to get collection name based on the data and the release
    public MongoDBCollection getMongoDBCollection(String dbname, String data, int release) throws CellBaseException {
        checkDataRelease(dbname, release, data);

        DatabaseInfo dbInfo = getDatabaseInfo(dbname);
        return dbInfo.getCacheData().get(release).get(data);
    }

    private void handleDocumentChange(ChangeStreamDocument<Document> changeStreamDocument) throws CellBaseException {
        // Get database name
        String dbname = changeStreamDocument.getNamespace().getDatabaseName();
        String collectionName = changeStreamDocument.getNamespace().getCollectionName();
        LOGGER.info("Collection {} of database {} has been updated", collectionName, dbname);

        DatabaseInfo dbInfo = getDatabaseInfo(dbname);

        // Handle the change event
        dbInfo.getRwLock().writeLock().lock();
        try {
            loadData(dbname);
        } finally {
            dbInfo.getRwLock().writeLock().unlock();
        }
    }

    private DatabaseInfo getDatabaseInfo(String dbname) throws CellBaseException {
        if (!dbInfoMap.containsKey(dbname)) {
            // Unknown database
            String msg = UNKOWN_DATABASE_MSG_PREFIX + dbname;
            throw new CellBaseException(msg);
        }

        return dbInfoMap.get(dbname);
    }
}
