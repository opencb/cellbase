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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.lib.db.MongoDBManager;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.mongodb.MongoDBIndexUtils;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.opencb.cellbase.lib.impl.core.CellBaseDBAdaptor.DATA_RELEASE_SEPARATOR;


public class IndexManager {

    private CellBaseConfiguration configuration;
    private Logger logger;
    private String databaseName;
    private Path indexFile;
    private MongoDBIndexUtils mongoDBIndexUtils;
    private MongoDBManager mongoDBManager;

    private Map<String, List<Map<String, ObjectMap>>> indexes;

    private static final Map<String, List<String>> DATA_COLLECTIONS = new HashMap<>();

    static {
        DATA_COLLECTIONS.put("genome", Arrays.asList("genome_info", "genome_sequence"));
        DATA_COLLECTIONS.put("conservation", Collections.singletonList("conservation"));
        DATA_COLLECTIONS.put("repeats", Collections.singletonList("repeats"));
        DATA_COLLECTIONS.put("gene", Arrays.asList("gene", "refseq"));
        DATA_COLLECTIONS.put("protein", Collections.singletonList("protein"));
        DATA_COLLECTIONS.put("regulation", Arrays.asList("regulatory_region", "regulatory_pfm"));
        DATA_COLLECTIONS.put("variation", Collections.singletonList("variation"));
        DATA_COLLECTIONS.put("variation_functional_score", Collections.singletonList("missense_variation_functional_score"));
        DATA_COLLECTIONS.put("protein_functional_prediction", Collections.singletonList("protein_functional_prediction"));
        DATA_COLLECTIONS.put("revel", Collections.singletonList("revel"));
        DATA_COLLECTIONS.put("alphamissense", Collections.singletonList("alphamissense"));
        DATA_COLLECTIONS.put("clinical_variants", Collections.singletonList("clinical_variants"));
        DATA_COLLECTIONS.put("splice_score", Collections.singletonList("splice_score"));
        DATA_COLLECTIONS.put("ontology", Collections.singletonList("ontology"));
        DATA_COLLECTIONS.put("pubmed", Collections.singletonList("pubmed"));
        DATA_COLLECTIONS.put("pharmacogenomics", Collections.singletonList("pharmacogenomics"));
        DATA_COLLECTIONS.put("polygenic_score", Arrays.asList("variant_polygenic_score", "common_polygenic_score"));
    }

    public IndexManager(String databaseName, Path indexFile, CellBaseConfiguration configuration) {
        this.databaseName = databaseName;
        this.indexFile = indexFile;
        this.configuration = configuration;

        init();
    }

    private void init() {
        logger = LoggerFactory.getLogger(this.getClass());
        mongoDBManager =  new MongoDBManager(configuration);

//        Path indexFile = Paths.get("./cellbase-lib/src/main/resources/mongodb-indexes.json");

        MongoDataStore mongoDBDatastore = mongoDBManager.createMongoDBDatastore(databaseName);
        mongoDBIndexUtils = new MongoDBIndexUtils(mongoDBDatastore, indexFile);

        indexes = null;
    }

    /**
     * Create indexes for the specified data. Use by the load to create indexes. Will throw an exception if
     * given database does not already exist.
     *
     * @param data create collection indexes for this data, can be "all" or a list of data names
     * @param dataRelease data release
     * @param dropIndexesFirst if TRUE, deletes the index before creating a new one. FALSE, no index is created if it
     *                         already exists.
     * @throws IOException if configuration file can't be read
     */
    @Deprecated
    public void createMongoDBIndexes(String data, String dataRelease, boolean dropIndexesFirst) throws IOException {
        //        InputStream indexResourceStream = getClass().getResourceAsStream("mongodb-indexes.json");
        if (StringUtils.isEmpty(data) || "all".equalsIgnoreCase(data)) {
            mongoDBIndexUtils.createAllIndexes(dropIndexesFirst);
//            mongoDBIndexUtils.createAllIndexes(mongoDataStore, indexResourceStream, dropIndexesFirst);
            logger.info("Loaded all indexes");
        } else {
            List<String> dataList = Arrays.asList(data.split(","));
            for (String dataName : dataList) {
                List<String> collections = new ArrayList<>();
                for (String collection : DATA_COLLECTIONS.get(dataName)) {
                    collections.add(collection + DATA_RELEASE_SEPARATOR + dataRelease);
                }
                createMongoDBIndexes(collections, dropIndexesFirst);
            }
        }
    }

    public void createMongoDBIndexes(List<String> collections, boolean dropIndexesFirst) throws IOException {
        checkIndexes();

        for (String collection : collections) {
            String key = collection.split(DATA_RELEASE_SEPARATOR)[0];
            if (indexes.containsKey(key)) {
                logger.info("Creating index for collection {} ", collection);
                mongoDBIndexUtils.createIndexes(collection, indexes.get(key), dropIndexesFirst);
                logger.info("Loaded index for {} ", collection);
            } else {
                logger.error("Could not create index for collection {}: no defined index was found", collection);
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
        checkIndexes();

        if (StringUtils.isEmpty(collectionName) || "all".equalsIgnoreCase(collectionName)) {
//            mongoDBIndexUtils.validateAllIndexes();
            logger.info("Validated all indexes");
        } else {
            String[] collections = collectionName.split(",");
            for (String collection : collections) {
//                mongoDBIndexUtils.validateIndexes(collection);
                logger.info("Validated index for {} ", collection);
            }
        }
    }

    private void checkIndexes() throws IOException {
        if (indexes == null) {
            indexes = getIndexesFromFile();
        }
    }

    private Map<String, List<Map<String, ObjectMap>>> getIndexesFromFile() throws IOException {
        ObjectMapper objectMapper = generateDefaultObjectMapper();
        Map<String, List<Map<String, ObjectMap>>> indexes = new HashMap<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(indexFile)) {
            bufferedReader.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .forEach(line -> {
                        try {
                            HashMap hashMap = objectMapper.readValue(line, HashMap.class);
                            String collection = (String) hashMap.get("collection");
                            if (!indexes.containsKey(collection)) {
                                indexes.put(collection, new ArrayList<>());
                            }
                            Map<String, ObjectMap> myIndexes = new HashMap<>();
                            myIndexes.put("fields", new ObjectMap((Map) hashMap.get("fields")));
                            myIndexes.put("options", new ObjectMap((Map) hashMap.getOrDefault("options", Collections.emptyMap())));
                            indexes.get(collection).add(myIndexes);
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    });
        }
        return indexes;
    }

    private ObjectMapper generateDefaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return objectMapper;
    }
}
