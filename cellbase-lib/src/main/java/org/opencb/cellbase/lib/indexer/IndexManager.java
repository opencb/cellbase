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

package org.opencb.cellbase.lib.indexer;

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.mongodb.MongoDBIndexUtils;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;


public class IndexManager {

    private CellBaseConfiguration configuration;
    private Logger logger;
    private String databaseName;
    private MongoDBIndexUtils mongoDBIndexUtils;

    public IndexManager(CellBaseConfiguration configuration, String databaseName) {
        this.configuration = configuration;
        this.databaseName = databaseName;

        init();
    }

    private void init() {
        logger = LoggerFactory.getLogger(this.getClass());

        Path indexFile = Paths.get("./cellbase-lib/src/main/resources/mongodb-indexes.json");
        if (indexFile == null) {
            try {
                throw new CellbaseException("Index file mongodb-indexes.json not found");
            } catch (CellbaseException e) {
                e.printStackTrace();
            }
        }
        MongoDBAdaptorFactory factory = new MongoDBAdaptorFactory(configuration);
        MongoDataStore mongoDataStore = factory.getMongoDBDatastore(databaseName);
        mongoDBIndexUtils = new MongoDBIndexUtils(mongoDataStore, indexFile);
    }

    /**
     * Create indexes for specified collection. Use by the load to create indexes. Will throw an exception if
     * given database does not already exist.
     *
     * @param collectionName create indexes for this collection, can be "all" or a list of collection names
     * @param dropIndexesFirst if TRUE, deletes the index before creating a new one. FALSE, no index is created if it
     *                         already exists.
     * @throws IOException if configuration file can't be read
     */
    public void createMongoDBIndexes(String collectionName, boolean dropIndexesFirst) throws IOException {
        if (StringUtils.isEmpty(collectionName) || "all".equalsIgnoreCase(collectionName)) {
            mongoDBIndexUtils.createAllIndexes(dropIndexesFirst);
            logger.info("Loaded all indexes");
        } else {
            String[] collections = collectionName.split(",");
            for (String collection : collections) {
                mongoDBIndexUtils.createIndexes(collection, dropIndexesFirst);
                logger.info("Loaded index for {} ", collection);
            }
        }
    }

    /**
     * Validate indexes for specified collection. Will throw an exception if given database does not already exist.
     *
     * @param collectionName create indexes for this collection, can be "all" or a list of collection names
     * @throws IOException if configuration file can't be read
     */
    public void validateMongoDBIndexes(String collectionName) throws IOException {
        if (StringUtils.isEmpty(collectionName) || "all".equalsIgnoreCase(collectionName)) {
            mongoDBIndexUtils.validateAllIndexes();
            logger.info("Validated all indexes");
        } else {
            String[] collections = collectionName.split(",");
            for (String collection : collections) {
                mongoDBIndexUtils.validateIndexes(collection);
                logger.info("Validated index for {} ", collection);
            }
        }
    }

}
