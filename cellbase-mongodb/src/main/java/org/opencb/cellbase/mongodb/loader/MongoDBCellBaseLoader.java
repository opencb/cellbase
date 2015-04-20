/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.mongodb.loader;

import com.mongodb.BulkWriteResult;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.lang.StringUtils;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.loader.CellBaseLoader;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.core.loader.LoaderException;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDBCollection;
import org.opencb.datastore.mongodb.MongoDBConfiguration;
import org.opencb.datastore.mongodb.MongoDataStore;
import org.opencb.datastore.mongodb.MongoDataStoreManager;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by parce on 18/02/15.
 */
public class MongoDBCellBaseLoader extends CellBaseLoader {

    private MongoDataStoreManager mongoDataStoreManager;
    private MongoDataStore mongoDataStore;
    private MongoDBCollection mongoDBCollection;

    private Path indexScriptFolder;
    private int[] chunkSizes;

    public MongoDBCellBaseLoader(BlockingQueue<List<String>> queue, String data, String database) {
        this(queue, data, database, null);
    }

    public MongoDBCellBaseLoader(BlockingQueue<List<String>> queue, String data, String database,
                                 CellBaseConfiguration cellBaseConfiguration) {
        super(queue, data, database, cellBaseConfiguration);
        if(cellBaseConfiguration.getDatabase().getOptions().get("mongodb-index-folder") != null) {
            indexScriptFolder = Paths.get(cellBaseConfiguration.getDatabase().getOptions().get("mongodb-index-folder"));
        }
    }


    @Override
    public void init() throws LoaderException {
        /*
         * OpenCB 'datastore' project is used to load data into MongoDB. The following code:
         * 1. creates a Manager to connect to a physical server
         * 2. a 'datastore' object connects to a specific database
         * 3. finally a connection to the collection is stored in 'mongoDBCollection'
         */
        mongoDataStoreManager = new MongoDataStoreManager(cellBaseConfiguration.getDatabase().getHost(),
                Integer.parseInt(cellBaseConfiguration.getDatabase().getPort()));

        MongoDBConfiguration mongoDBConfiguration;
        if(cellBaseConfiguration != null
                && cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase") != null
                && !cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase").isEmpty()) {
            mongoDBConfiguration = MongoDBConfiguration.builder()
                    .add("username", cellBaseConfiguration.getDatabase().getUser())
                    .add("password", cellBaseConfiguration.getDatabase().getPassword())
                    .add("authenticationDatabase", cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase")).build();
            logger.debug("MongoDB 'authenticationDatabase' database parameter set to '{}'",
                    cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase"));
        }else {
            mongoDBConfiguration = MongoDBConfiguration.builder()
                    .add("username", cellBaseConfiguration.getDatabase().getUser())
                    .add("password", cellBaseConfiguration.getDatabase().getPassword()).build();
        }
        logger.debug("MongoDB credentials are user: '{}', password: '{}'",
                cellBaseConfiguration.getDatabase().getUser(), cellBaseConfiguration.getDatabase().getPassword());

        mongoDataStore = mongoDataStoreManager.get(database, mongoDBConfiguration);

        String collectionName = getCollectionName(data);
        mongoDBCollection = mongoDataStore.getCollection(collectionName);
        logger.debug("Connection to MongoDB datastore '{}' created, collection '{}' is used",
                mongoDataStore.getDatabaseName(), collectionName);

        // Some collections need to add an extra _chunkIds field to speed up some queries
        getChunkSizes(collectionName);
        logger.debug("Chunk sizes '{}' used for collection '{}'", Arrays.toString(chunkSizes), collectionName);
    }


    private String getCollectionName(String data) throws LoaderException {
        String collectionName;
        switch (data) {
            case "genome_info":
                collectionName = "genome_info";
                break;
            case "genome_sequence":
                collectionName = "genome_sequence";
                break;
            case "gene":
                collectionName = "gene";
                break;
            case "variation":
                collectionName = "variation";
                break;
            case "regulatory_region":
                collectionName = "regulatory_region";
                break;
            case "protein":
                collectionName = "protein";
                break;
            case "protein_protein_interaction":
                collectionName = "protein_protein_interaction";
                break;
            case "protein_functional_prediction":
                collectionName = "protein_functional_prediction";
                break;
            case "conservation":
                collectionName = "conservation";
                break;
            case "cosmic":
            case "clinvar":
            case "gwas":
            case "clinical":
                collectionName = "clinical";
                break;
            default:
                throw new LoaderException("Unknown data to load: '" + data + "'");
        }

        return collectionName;
    }

    private void getChunkSizes(String collectionName) {
        if (collectionName != null) {
            switch (collectionName) {
                case "genome_sequence":
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.GENOME_SEQUENCE_CHUNK_SIZE};
                    break;
                case "gene":
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.GENE_CHUNK_SIZE};
                    break;
                case "variation":
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE,
                            10 * MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE};
                    break;
                case "regulatory_region":
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.REGULATORY_REGION_CHUNK_SIZE};
                    break;
                case "conservation":
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE};
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Integer call() {
        Integer loadedObjects = 0;
        boolean finished = false;
        while (!finished) {
            try {
                List<String> batch = blockingQueue.take();
                if (batch == LoadRunner.POISON_PILL) {
                    finished = true;
                } else {
                    List<DBObject> dbObjectsBatch = new ArrayList<>(batch.size());
                    for (String jsonLine : batch) {
                        DBObject dbObject = (DBObject) JSON.parse(jsonLine);
                        addChunkId(dbObject);
                        dbObjectsBatch.add(dbObject);
                    }
                    loadedObjects += load(dbObjectsBatch);
                }
            } catch (InterruptedException e) {
                logger.error("Loader thread interrupted: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error Loading batch: " + e.getMessage());
            }
        }
        logger.debug("'load' finished. " + loadedObjects + " records loaded");
        return loadedObjects;
    }

    @Override
    public void createIndex(String data) throws LoaderException {
        Path indexFilePath = getIndexFilePath(data);
        if(indexFilePath != null) {
            try {
                runCreateIndexProcess(indexFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {
            logger.warn("No index found for '{}'", data);        }
    }

    public int load(List<DBObject> batch) {
        // TODO: queryOptions?
        QueryResult<BulkWriteResult> result = mongoDBCollection.insert(batch, new QueryOptions());
        return result.first().getInsertedCount();
    }

    private void addChunkId(DBObject dbObject) {
        if (chunkSizes != null && chunkSizes.length > 0) {
            List<String> chunkIds = new ArrayList<>();
            for (int chunkSize : chunkSizes) {
                int chunkStart = (Integer) dbObject.get("start") / chunkSize;
                int chunkEnd = (Integer) dbObject.get("end") / chunkSize;
                String chunkIdSuffix = chunkSize / 1000 + "k";
                for (int i = chunkStart; i <= chunkEnd; i++) {
                    if(dbObject.containsField("chromosome")) {
                        chunkIds.add(dbObject.get("chromosome") + "_" + i + "_" + chunkIdSuffix);
                    }else {
                        chunkIds.add(dbObject.get("sequenceName") + "_" + i + "_" + chunkIdSuffix);
                    }
                }
            }
            dbObject.put("_chunkIds", chunkIds);
        }
    }

    @Override
    public void close() {
        mongoDataStoreManager.close(database);
    }

    private Path getIndexFilePath(String data) throws LoaderException {
        if(indexScriptFolder == null || data == null) {
            logger.error("No path can be provided for index, check index folder '{}' and data '{}'",
                    indexScriptFolder, data);
            return null;
        }

        String indexFileName = null;
        switch (data) {
            case "genome_info":
                indexFileName = null;
                break;
            case "genome_sequence":
                indexFileName = "genome_sequence-indexes.js";
                break;
            case "gene":
                indexFileName = "gene-indexes.js";
                break;
            case "variation":
                indexFileName = "variation-indexes.js";
                break;
            case "regulatory_region":
                indexFileName = "regulatory_region-indexes.js";
                break;
            case "protein":
                indexFileName = "protein-indexes.js";
                break;
            case "protein_protein_interaction":
                indexFileName = "protein_protein_interaction-indexes.js";
                break;
            case "protein_functional_prediction":
                indexFileName = "protein_functional_prediction-indexes.js";
                break;
            case "conservation":
                indexFileName = "conservation-indexes.js";
                break;
            case "cosmic":
            case "clinvar":
            case "gwas":
            case "clinical":
                indexFileName = "clinical-indexes.js";
                break;
            default:
                break;
        }
        if(indexFileName == null) {
            return null;
        }
        return indexScriptFolder.resolve(indexFileName);
    }


    protected boolean runCreateIndexProcess(Path indexFilePath) throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add("mongo");
        args.add("--host");
        args.add(cellBaseConfiguration.getDatabase().getHost());
        if(cellBaseConfiguration.getDatabase().getUser() != null && !cellBaseConfiguration.getDatabase().getUser().equals("")) {
            args.addAll(Arrays.asList(
                    "-u", cellBaseConfiguration.getDatabase().getUser(),
                    "-p", cellBaseConfiguration.getDatabase().getPassword()
            ));
        }
        if(cellBaseConfiguration != null && cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase") != null) {
            args.add("--authenticationDatabase");
            args.add(cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase"));
            logger.debug("MongoDB 'authenticationDatabase' database parameter set to '{}'",
                    cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase"));
        }
        args.add(database);
        args.add(indexFilePath.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        logger.debug("Executing command: '{}'",  StringUtils.join(processBuilder.command(), " "));

//        processBuilder.redirectErrorStream(true);
//        if (logFilePath != null) {
//            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(logFilePath)));
//        }

        Process process = processBuilder.start();
        process.waitFor();

        // Check process output
        boolean executedWithoutErrors = true;
        int genomeInfoExitValue = process.exitValue();
        if (genomeInfoExitValue != 0) {
            logger.warn("Error executing {}, error code: {}", indexFilePath, genomeInfoExitValue);
            executedWithoutErrors = false;
        }
        return executedWithoutErrors;
    }

}
