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

import com.mongodb.bulk.BulkWriteResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.api.CellBaseDBAdaptor;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.loader.CellBaseLoader;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.core.loader.LoaderException;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.cellbase.mongodb.impl.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by parce on 18/02/15.
 */
public class MongoDBCellBaseLoader extends CellBaseLoader {

    private MongoDataStoreManager mongoDataStoreManager;
    private MongoDataStore mongoDataStore;
    private MongoDBCollection mongoDBCollection;

    private DBAdaptorFactory dbAdaptorFactory;
    @Deprecated
    private CellBaseDBAdaptor dbAdaptor;

    private Path indexScriptFolder;
    private int[] chunkSizes;
    private String clinicalVariantSource;

    private static final String CLINVARVARIANTSOURCE = "clinvar";
    private static final String COSMICVARIANTSOURCE = "cosmic";
    private static final String GWASVARIANTSOURCE = "gwas";

    public MongoDBCellBaseLoader(BlockingQueue<List<String>> queue, String data, String database) {
        this(queue, data, database, null, null);
    }

    public MongoDBCellBaseLoader(BlockingQueue<List<String>> queue, String data, String database, String field,
                                 CellBaseConfiguration cellBaseConfiguration) {
        super(queue, data, database, field, cellBaseConfiguration);
        if (cellBaseConfiguration.getDatabase().getOptions().get("mongodb-index-folder") != null) {
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
        String[] hosts = cellBaseConfiguration.getDatabase().getHost().split(",");
        List<DataStoreServerAddress> dataStoreServerAddressList = new ArrayList<>(hosts.length);
        for (String host : hosts) {
            String[] hostAndPort = host.split(":");
            dataStoreServerAddressList.add(new DataStoreServerAddress(hostAndPort[0], (hostAndPort.length == 2)
                    ? Integer.parseInt(hostAndPort[1]) : 27017));
        }
        mongoDataStoreManager = new MongoDataStoreManager(dataStoreServerAddressList);

        MongoDBConfiguration mongoDBConfiguration;
        if (cellBaseConfiguration != null
                && cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase") != null
                && !cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase").isEmpty()) {
            mongoDBConfiguration = MongoDBConfiguration.builder()
                    .add("username", cellBaseConfiguration.getDatabase().getUser())
                    .add("password", cellBaseConfiguration.getDatabase().getPassword())
                    .add("authenticationDatabase", cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase")).build();
            logger.debug("MongoDB 'authenticationDatabase' database parameter set to '{}'",
                    cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase"));
        } else {
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

        dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        dbAdaptor = getDBAdaptor(data);
    }

    @Deprecated
    private CellBaseDBAdaptor getDBAdaptor(String data) throws LoaderException {
        String[] databaseParts = database.split("_");
        String species = databaseParts[1];
        String assembly = databaseParts[2];
        CellBaseDBAdaptor dbAdaptor;
        switch (data) {
            case "genome_info":
                dbAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(species, assembly);
                break;
            case "genome_sequence":
                dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
                break;
            case "gene":
                dbAdaptor = dbAdaptorFactory.getGeneDBAdaptor(species, assembly);
                break;
            case "variation":
                dbAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species, assembly);
                break;
            case "cadd":
//                dbAdaptor = dbAdaptorFactory.getVariantFunctionalScoreDBAdaptor(species, assembly);
                dbAdaptor = null;
                break;
            case "regulatory_region":
                dbAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(species, assembly);
                break;
            case "protein":
                dbAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
                break;
            case "protein_protein_interaction":
                dbAdaptor = dbAdaptorFactory.getProteinProteinInteractionDBAdaptor(species, assembly);
                break;
            // TODO: implement an adaptor for protein_functional_prediction - current queries are issued from the
            // TODO: ProteinDBAdaptors, that's why there isn't one yet
            case "protein_functional_prediction":
                dbAdaptor = null;
//                collectionName = "protein_functional_prediction";
                break;
            case "conservation":
                dbAdaptor = dbAdaptorFactory.getConservationDBAdaptor(species, assembly);
                break;
            case "cosmic":
                clinicalVariantSource = "cosmic";
                dbAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(species, assembly);
                break;
            case "clinvar":
                clinicalVariantSource = "clinvar";
                dbAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(species, assembly);
                break;
            case "gwas":
                clinicalVariantSource = "gwas";
                dbAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(species, assembly);
                break;
            case "clinical":
                dbAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(species, assembly);
                break;
            default:
                throw new LoaderException("Unknown data to load: '" + data + "'");
        }

        return dbAdaptor;
    }

    // TODO: use adaptors within MongoDBCellBaseLoader, avoid using mongoDBCollection and remove this method
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
            case "cadd":
                collectionName = "variation_functional_score";
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
                clinicalVariantSource = "cosmic";
                collectionName = "clinical";
                break;
            case "clinvar":
                clinicalVariantSource = "clinvar";
                collectionName = "clinical";
                break;
            case "gwas":
                clinicalVariantSource = "gwas";
                collectionName = "clinical";
                break;
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
                case "variation":  // TODO: why are we using different chunk sizes??
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE,
                            10 * MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE, };
                    break;
                case "variation_functional_score":
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.VARIATION_FUNCTIONAL_SCORE_CHUNK_SIZE};
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
        if (field != null) {
            return prepareBatchAndUpdate();
        } else {
            return prepareBatchAndLoad();
        }
    }

    private int prepareBatchAndUpdate() {
        int numLoadedObjects = 0;
        boolean finished = false;
        while (!finished) {
            try {
                List<String> batch = blockingQueue.take();
                if (batch == LoadRunner.POISON_PILL) {
                    finished = true;
                } else {
                    List<Document> dbObjectsBatch = new ArrayList<>(batch.size());
                    for (String jsonLine : batch) {
                        Document dbObject = Document.parse(jsonLine);
                        dbObjectsBatch.add(dbObject);
                    }

                    Long numUpdates = (Long) dbAdaptor.update(dbObjectsBatch, field).first();
                    numLoadedObjects += numUpdates;
                }
            } catch (InterruptedException e) {
                logger.error("Loader thread interrupted: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error Loading batch: " + e.getMessage());
            }
        }
        logger.debug("'load' finished. " + numLoadedObjects + " records loaded");
        return numLoadedObjects;
    }

    private int prepareBatchAndLoad() {
        int numLoadedObjects = 0;
        boolean finished = false;
        while (!finished) {
            try {
                List<String> batch = blockingQueue.take();
                if (batch == LoadRunner.POISON_PILL) {
                    finished = true;
                } else {
                    List<Document> documentBatch = new ArrayList<>(batch.size());
                    for (String jsonLine : batch) {
                        Document document = Document.parse(jsonLine);
                        addChunkId(document);
                        addClinicalPrivateFields(document);
                        documentBatch.add(document);
                    }
                    numLoadedObjects += load(documentBatch);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("Loader thread interrupted: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error Loading batch: " + e.getMessage());
            }
        }
        logger.debug("'load' finished. " + numLoadedObjects + " records loaded");
        return numLoadedObjects;
    }

    private void addClinicalPrivateFields(Document document) {
        if (clinicalVariantSource != null) {
            List<String> geneIdList = null;
            List<String> phenotypeList = null;
            switch (clinicalVariantSource) {
                case CLINVARVARIANTSOURCE:
                    geneIdList = getClinvarGeneIds(document);
                    phenotypeList = getClinvarPhenotypes(document);
                    break;
                case COSMICVARIANTSOURCE:
                    geneIdList = document.get("geneName") != null ? Collections.singletonList(document.getString("geneName")) : null;
                    phenotypeList = getCosmicPhenotypes(document);
                    break;
                case GWASVARIANTSOURCE:
                    geneIdList = document.get("reportedGenes") != null
                            ? Collections.singletonList(document.getString("reportedGenes"))
                            : null;
                    phenotypeList = getGwasPhenotypes(document);
                    break;
                default:
                    break;
            }
            if (geneIdList != null) {
                document.put("_geneIds", geneIdList);
            }
            if (phenotypeList != null) {
                document.put("_phenotypes", phenotypeList);
            }
        }
    }

    private List<String> getGwasPhenotypes(Document document) {
        List<String> phenotypeList = new ArrayList<>();
        List studiesDBList = document.get("studies", List.class);
        for (Object studyObject : studiesDBList) {
            Document studyDBObject = (Document) studyObject;
            List traitsDBList = studyDBObject.get("traits", List.class);
            if (traitsDBList != null) {
                for (Object traitObject : traitsDBList) {
                    Document traitDBObject = (Document) traitObject;
                    if (traitDBObject.get("diseaseTrait") != null) {
                        phenotypeList.add(traitDBObject.getString("diseaseTrait"));
                    }
                }
            }
        }
        return phenotypeList;
    }

    private List<String> getCosmicPhenotypes(Document document) {
        List<String> phenotypeList = new ArrayList<>(4);
        addIfNotEmpty((String) document.get("primarySite"), phenotypeList);
        addIfNotEmpty((String) document.get("histologySubtype"), phenotypeList);
        addIfNotEmpty((String) document.get("primaryHistology"), phenotypeList);
        addIfNotEmpty((String) document.get("siteSubtype"), phenotypeList);

        return phenotypeList;

    }

    private void addIfNotEmpty(String element, List<String> stringList) {
        if (element != null && !element.isEmpty()) {
            stringList.add(element);
        }
    }

    private List<String> getClinvarPhenotypes(Document dbObject) {
        List<String> phenotypeList = new ArrayList<>();
        List basicDBList = ((Document) ((Document) ((Document) dbObject.get("clinvarSet")).get("referenceClinVarAssertion"))
                .get("traitSet")).get("trait", List.class);
        for (Object object : basicDBList) {
            Document document = (Document) object;
            List nameDBList = document.get("name", List.class);
            if (nameDBList != null) {
                for (Object nameObject : nameDBList) {
                    Document elementValueDBObject = (Document) ((Document) nameObject).get("elementValue");
                    if (elementValueDBObject != null) {
                        String phenotype = (String) elementValueDBObject.get("value");
                        if (phenotype != null) {
                            phenotypeList.add(phenotype);
                        }
                    }
                }

            }
        }
        if (phenotypeList.size() > 0) {
            return phenotypeList;
        } else {
            return null;
        }
    }

    private List<String> getClinvarGeneIds(Document dbObject) {
        List<String> geneIdList = new ArrayList<>();
        List basicDBList = ((Document) ((Document) ((Document) dbObject.get("clinvarSet")).get("referenceClinVarAssertion"))
                .get("measureSet")).get("measure", List.class);
        for (Object object : basicDBList) {
            Document document = (Document) object;
            List measureRelationshipDBList = document.get("measureRelationship", List.class);
            if (measureRelationshipDBList != null) {
                for (Object measureRelationShipObject : measureRelationshipDBList) {
                    List symbolDBList = ((Document) measureRelationShipObject).get("symbol", List.class);
                    if (symbolDBList != null) {
                        for (Object symbolObject : symbolDBList) {
                            Document elementValueDBObject = (Document) ((Document) symbolObject).get("elementValue");
                            if (elementValueDBObject != null) {
                                String geneId = (String) elementValueDBObject.get("value");
                                if (geneId != null) {
                                    geneIdList.add(geneId);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (geneIdList.size() > 0) {
            return geneIdList;
        } else {
            return null;
        }
    }

    @Override
    public void createIndex(String data) throws LoaderException {
        Path indexFilePath = getIndexFilePath(data);
        if (indexFilePath != null) {
            logger.info("Creating indexes...");
            try {
                runCreateIndexProcess(indexFilePath);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            logger.warn("No index found for '{}'", data);
        }
    }

    public int load(List<Document> batch) {
        // TODO: queryOptions?
        QueryResult<BulkWriteResult> result = mongoDBCollection.insert(batch, new QueryOptions());
        return result.first().getInsertedCount();
    }

    private void addChunkId(Document document) {
        if (chunkSizes != null && chunkSizes.length > 0) {
            List<String> chunkIds = new ArrayList<>();
            for (int chunkSize : chunkSizes) {
                int chunkStart = (Integer) document.get("start") / chunkSize;
                int chunkEnd = (Integer) document.get("end") / chunkSize;
                String chunkIdSuffix = chunkSize / 1000 + "k";
                for (int i = chunkStart; i <= chunkEnd; i++) {
                    if (document.containsKey("chromosome")) {
                        chunkIds.add(document.get("chromosome") + "_" + i + "_" + chunkIdSuffix);
                    } else {
                        chunkIds.add(document.get("sequenceName") + "_" + i + "_" + chunkIdSuffix);
                    }
                }
            }
            logger.debug("Setting chunkIds to {}", chunkIds.toString());
            document.put("_chunkIds", chunkIds);
        }
    }

    @Override
    public void close() {
        mongoDataStoreManager.close(database);
    }

    private Path getIndexFilePath(String data) throws LoaderException {
        if (indexScriptFolder == null || data == null) {
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
            case "variation_functional_score":
                indexFileName = "variation_functional_score-indexes.js";
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
        if (indexFileName == null) {
            return null;
        }
        return indexScriptFolder.resolve(indexFileName);
    }


    protected boolean runCreateIndexProcess(Path indexFilePath) throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add("mongo");
        args.add("--host");
        args.add(cellBaseConfiguration.getDatabase().getHost());
        if (cellBaseConfiguration.getDatabase().getUser() != null && !cellBaseConfiguration.getDatabase().getUser().equals("")) {
            args.addAll(Arrays.asList(
                    "-u", cellBaseConfiguration.getDatabase().getUser(),
                    "-p", cellBaseConfiguration.getDatabase().getPassword()
            ));
        }
        if (cellBaseConfiguration != null && cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase") != null) {
            args.add("--authenticationDatabase");
            args.add(cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase"));
            logger.debug("MongoDB 'authenticationDatabase' database parameter set to '{}'",
                    cellBaseConfiguration.getDatabase().getOptions().get("authenticationDatabase"));
        }
        args.add(database);
        args.add(indexFilePath.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        logger.debug("Executing command: '{}'", StringUtils.join(processBuilder.command(), " "));

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
