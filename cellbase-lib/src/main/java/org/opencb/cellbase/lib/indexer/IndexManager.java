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
import org.opencb.cellbase.core.common.Species;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.SpeciesUtils;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.mongodb.MongoDBIndexUtils;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class IndexManager {

    private CellBaseConfiguration configuration;
    private Logger logger;

    public IndexManager(CellBaseConfiguration configuration) {
        this.configuration = configuration;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Create indexes. Exception thrown if species or assembly is incorrect. NULL assembly value will default to
     * first assembly in the config file.
     *
     * @param data list of collections to index
     * @param speciesName name of species
     * @param assemblyName name of assembly
     * @param dropIndexesFirst if TRUE, deletes the index before creating a new one. FALSE, no index is created if it
     *                         already exists.
     * @throws IOException if configuration file can't be read
     * @throws CellbaseException if indexes file isn't found, or invalid input
     */
    public void createMongoDBIndexes(String data, String speciesName, String assemblyName, boolean dropIndexesFirst)
            throws CellbaseException, IOException {
        Species species = SpeciesUtils.getSpecies(configuration, speciesName, assemblyName);
        if (StringUtils.isEmpty(data) || "all".equalsIgnoreCase(data)) {
            createMongoDBIndexes(new String[0], species.getSpecies(), species.getAssembly(), dropIndexesFirst);
        } else {
            String[] collections = data.split(",");
            createMongoDBIndexes(collections, species.getSpecies(), species.getAssembly(), dropIndexesFirst);
        }
    }

    /**
     * Create indexes for specified collection. Use by the load to create indexes. Will throw an exception if
     * given database does not already exist.
     *
     * @param collectionName create indexes for this collection, can be "all" or a list of collection names
     * @param databaseName name of database
     * @param dropIndexesFirst if TRUE, deletes the index before creating a new one. FALSE, no index is created if it
     *                         already exists.
     * @throws IOException if configuration file can't be read
     * @throws CellbaseException if indexes file isn't found
     */
    public void createMongoDBIndexes(String collectionName, String databaseName, boolean dropIndexesFirst)
            throws IOException, CellbaseException {
        InputStream resourceAsStream = IndexManager.class.getResourceAsStream("/mongodb-indexes.json");
        if (resourceAsStream == null) {
            throw new CellbaseException("Index file mongodb-indexes.json not found");
        }
        MongoDBAdaptorFactory factory = new MongoDBAdaptorFactory(configuration);
        MongoDataStore mongoDataStore = factory.getMongoDBDatastore(databaseName);

        if (StringUtils.isEmpty(collectionName) || "all".equalsIgnoreCase(collectionName)) {
            MongoDBIndexUtils.createAllIndexes(mongoDataStore, resourceAsStream, dropIndexesFirst);
        } else {
            String[] collections = collectionName.split(",");
            for (String collection : collections) {
                MongoDBIndexUtils.createIndexes(mongoDataStore, resourceAsStream, collection, dropIndexesFirst);
            }
        }
    }

    private void createMongoDBIndexes(String[] collections, String species, String assembly, boolean dropIndexesFirst)
            throws IOException, CellbaseException {
        InputStream resourceAsStream = IndexManager.class.getResourceAsStream("/mongodb-indexes.json");
        if (resourceAsStream == null) {
            throw new CellbaseException("Index file mongodb-indexes.json not found");
        }
        MongoDBAdaptorFactory factory = new MongoDBAdaptorFactory(configuration);
        MongoDataStore mongoDataStore = factory.getMongoDBDatastore(species, assembly);
        if (collections == null || collections.length == 0) {
            MongoDBIndexUtils.createAllIndexes(mongoDataStore, resourceAsStream, dropIndexesFirst);
        } else {
            for (String collectionName : collections) {
                try {
                    MongoDBIndexUtils.createIndexes(mongoDataStore, resourceAsStream, collectionName, dropIndexesFirst);
                } catch (NullPointerException e) {
                    throw new CellbaseException("Error creating an index for collection '" + collectionName
                            + "', collection does not exist");
                }
            }
        }
    }
}
