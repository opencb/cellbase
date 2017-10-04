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

package org.opencb.cellbase.lib.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.QueryBuilder;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.*;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.cache.CacheManager;
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DatabaseCredentials;
import org.opencb.cellbase.core.config.Species;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

public class MongoDBAdaptor {

    private static final String CELLBASE_DB_MONGODB_REPLICASET = "CELLBASE.DB.MONGODB.REPLICASET";

    enum QueryValueType {INTEGER, STRING}

    protected String species;
    protected String assembly;

    protected static MongoDataStoreManager mongoDataStoreManager;
    protected MongoDataStore mongoDataStore;
    protected MongoDBCollection mongoDBCollection;
    protected CellBaseConfiguration cellBaseConfiguration;
    private static CacheManager cacheManager;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected ObjectMapper objectMapper;

    public MongoDBAdaptor(CellBaseConfiguration cellBaseConfiguration) {
        this("", "", cellBaseConfiguration);
    }

    public MongoDBAdaptor(String species, String assembly, CellBaseConfiguration cellBaseConfiguration) {
        this.species = species;
        this.assembly = assembly;
        this.cellBaseConfiguration = cellBaseConfiguration;

        logger = LoggerFactory.getLogger(this.getClass().toString());
        objectMapper = new ObjectMapper();

        initSpeciesAssembly(species, assembly);
        init();
        this.mongoDataStore = createMongoDBDatastore(species, assembly);
        this.cacheManager = new CacheManager(cellBaseConfiguration);
    }

    private void initSpeciesAssembly(String species, String assembly) {
        if (species != null && !species.equals("")) {
            // if 'version' parameter has not been provided the default version is selected
            if (this.assembly == null || this.assembly.trim().equals("")) {
                this.assembly = "default";
            }
        }
    }

    private void init() {
        if (mongoDataStoreManager == null) {
//            String[] hosts = cellBaseConfiguration.getDatabases().get("mongodb").getHost().split(",");
            String[] hosts = cellBaseConfiguration.getDatabases().getMongodb().getHost().split(",");
            List<DataStoreServerAddress> dataStoreServerAddresses = new ArrayList<>(hosts.length);
            for (String host : hosts) {
                String[] hostPort = host.split(":");
                if (hostPort.length == 1) {
                    dataStoreServerAddresses.add(new DataStoreServerAddress(hostPort[0], 27017));
                } else {
                    dataStoreServerAddresses.add(new DataStoreServerAddress(hostPort[0], Integer.parseInt(hostPort[1])));
                }
            }
            mongoDataStoreManager = new MongoDataStoreManager(dataStoreServerAddresses);
            logger.debug("MongoDBAdaptorFactory constructor, this should be only be printed once");
        }

//        logger = LoggerFactory.getLogger(this.getClass());
    }

    private MongoDataStore createMongoDBDatastore(String species, String assembly) {
        /**
         Database name has the following pattern in lower case and with no '.' in the name:
         cellbase_speciesId_assembly_cellbaseVersion
         Example:
         cellbase_hsapiens_grch37_v3
         **/

        DatabaseCredentials mongodbCredentials = cellBaseConfiguration.getDatabases().getMongodb();

        // We need to look for the species object in the configuration
        Species speciesObject = getSpecies(species);
        if (speciesObject != null) {
            species = speciesObject.getId();
            String cellbaseAssembly = getAssembly(speciesObject, assembly);

            if (species != null && !species.isEmpty() && cellbaseAssembly != null && !cellbaseAssembly.isEmpty()) {
                cellbaseAssembly = cellbaseAssembly.toLowerCase();
                // Database name is built following the above pattern
                String database = "cellbase" + "_" + species + "_" + cellbaseAssembly.replaceAll("\\.", "").replaceAll("-", "")
                        .replaceAll("_", "") + "_" + cellBaseConfiguration.getVersion();
                logger.debug("Database for the species is '{}'", database);

                MongoDBConfiguration mongoDBConfiguration;
                MongoDBConfiguration.Builder builder = MongoDBConfiguration.builder();

                // For authenticated databases
                if (!mongodbCredentials.getUser().isEmpty() && !mongodbCredentials.getPassword().isEmpty()) {
                    // MongoDB could authenticate against different databases
                    builder.setUserPassword(mongodbCredentials.getUser(), mongodbCredentials.getPassword());
                    if (mongodbCredentials.getOptions().containsKey(MongoDBConfiguration.AUTHENTICATION_DATABASE)) {
                        builder.setAuthenticationDatabase(mongodbCredentials.getOptions()
                                .get(MongoDBConfiguration.AUTHENTICATION_DATABASE));
                    }
                }

                if (mongodbCredentials.getOptions().get(MongoDBConfiguration.READ_PREFERENCE) != null
                        && !mongodbCredentials.getOptions().get(MongoDBConfiguration.READ_PREFERENCE).isEmpty()) {
                    builder.add(MongoDBConfiguration.READ_PREFERENCE,
                            mongodbCredentials.getOptions().get(MongoDBConfiguration.READ_PREFERENCE));
                }

                String replicaSet = mongodbCredentials.getOptions().get(MongoDBConfiguration.REPLICA_SET);
                if (replicaSet != null && !replicaSet.isEmpty() && !replicaSet.contains(CELLBASE_DB_MONGODB_REPLICASET)) {
                    builder.setReplicaSet(mongodbCredentials.getOptions().get(MongoDBConfiguration.REPLICA_SET));
                }

                String connectionsPerHost = mongodbCredentials.getOptions().get(MongoDBConfiguration.CONNECTIONS_PER_HOST);
                if (connectionsPerHost != null && !connectionsPerHost.isEmpty()) {
                    builder.setConnectionsPerHost(Integer.valueOf(mongodbCredentials.getOptions()
                            .get(MongoDBConfiguration.CONNECTIONS_PER_HOST)));
                }

                mongoDBConfiguration = builder.build();

                logger.debug("*************************************************************************************");
                logger.debug("MongoDataStore configuration parameters: ");
                logger.debug("{} = {}", MongoDBConfiguration.AUTHENTICATION_DATABASE,
                        mongoDBConfiguration.get(MongoDBConfiguration.AUTHENTICATION_DATABASE));
                logger.debug("{} = {}", MongoDBConfiguration.READ_PREFERENCE,
                        mongoDBConfiguration.get(MongoDBConfiguration.READ_PREFERENCE));
                logger.debug("{} = {}", MongoDBConfiguration.REPLICA_SET,
                        mongoDBConfiguration.get(MongoDBConfiguration.REPLICA_SET));
                logger.debug("{} = {}", MongoDBConfiguration.CONNECTIONS_PER_HOST,
                        mongoDBConfiguration.get(MongoDBConfiguration.CONNECTIONS_PER_HOST));
                logger.debug("*************************************************************************************");
//                } else {
//                    mongoDBConfiguration = MongoDBConfiguration.builder().init().build();
//                }

                // A MongoDataStore to this host and database is returned
                MongoDataStore mongoDatastore = mongoDataStoreManager.get(database, mongoDBConfiguration);

                // we return the MongoDataStore object
                return mongoDatastore;
            } else {
                logger.error("Assembly is not valid, assembly '{}'. Valid assemblies: {}", assembly,
                        String.join(",", speciesObject.getAssemblies().stream().map((assemblyObject)
                                -> assemblyObject.getName()).collect(Collectors.toList())));
                throw new InvalidParameterException("Assembly is not valid, assembly '" + assembly
                        + "'. Please provide one of supported assemblies: {"
                        + String.join(",", speciesObject.getAssemblies().stream().map((assemblyObject)
                        -> assemblyObject.getName()).collect(Collectors.toList())) + "}");
            }
        } else {
            logger.error("Species name is not valid: '{}'. Valid species: {}", species,
                    String.join(",", cellBaseConfiguration.getAllSpecies().stream().map((tmpSpeciesObject)
                            -> (tmpSpeciesObject.getCommonName() + "|" + tmpSpeciesObject.getScientificName()))
                            .collect(Collectors.toList())));
            throw new InvalidParameterException("Species name is not valid: '" + species + "'. Please provide one"
                    + " of supported species: {"
                    + String.join(",", cellBaseConfiguration.getAllSpecies().stream().map((tmpSpeciesObject)
                    -> (tmpSpeciesObject.getCommonName() + "|" + tmpSpeciesObject.getScientificName()))
                    .collect(Collectors.toList())) + "}");
        }
    }

    protected Species getSpecies(String speciesName) {
        Species species = null;
        for (Species sp : cellBaseConfiguration.getAllSpecies()) {
            if (speciesName.equalsIgnoreCase(sp.getId()) || speciesName.equalsIgnoreCase(sp.getScientificName())) {
                species = sp;
                break;
            }
        }
        return species;
    }

    protected String getAssembly(Species species, String assemblyName) {
        String assembly = null;
        if (assemblyName == null || assemblyName.isEmpty()) {
            assembly = species.getAssemblies().get(0).getName();
        } else {
            for (Species.Assembly assembly1 : species.getAssemblies()) {
                if (assemblyName.equalsIgnoreCase(assembly1.getName())) {
                    assembly = assembly1.getName();
                }
            }
        }
        return assembly;
    }

    protected QueryOptions addPrivateExcludeOptions(QueryOptions options) {
        return addPrivateExcludeOptions(options, "_id,_chunkIds");
    }

    protected QueryOptions addPrivateExcludeOptions(QueryOptions options, String csvFields) {
        if (options != null) {
            if (options.get("exclude") == null) {
                options.put("exclude", csvFields);
            } else {
                String exclude = options.getString("exclude");
                if (exclude.contains(csvFields)) {
                    return options;
                } else {
                    options.put("exclude", exclude + "," + csvFields);
                }
            }
        } else {
            options = new QueryOptions("exclude", csvFields);
        }
        return options;
    }

    protected void createRegionQuery(Query query, String queryParam, List<Bson> andBsonList) {
        if (query != null && query.getString(queryParam) != null && !query.getString(queryParam).isEmpty()) {
            List<Region> regions = Region.parseRegions(query.getString(queryParam));
            if (regions != null && regions.size() > 0) {
                // if there is only one region we add the AND filter directly to the andBsonList passed
                if (regions.size() == 1) {
                    Bson chromosome = Filters.eq("chromosome", regions.get(0).getChromosome());
                    Bson start = Filters.lte("start", regions.get(0).getEnd());
                    Bson end = Filters.gte("end", regions.get(0).getStart());
                    andBsonList.add(Filters.and(chromosome, start, end));
                } else {
                    // when multiple regions then we create and OR list before add it to andBsonList
                    List<Bson> orRegionBsonList = new ArrayList<>(regions.size());
                    for (Region region : regions) {
                        Bson chromosome = Filters.eq("chromosome", region.getChromosome());
                        Bson start = Filters.lte("start", region.getEnd());
                        Bson end = Filters.gte("end", region.getStart());
                        orRegionBsonList.add(Filters.and(chromosome, start, end));
                    }
                    andBsonList.add(Filters.or(orRegionBsonList));
                }
            } else {
                logger.warn("Region query no created, region object is null or empty.");
            }
        }
    }

    protected void createRegionQuery(Query query, String queryParam, int chunkSize, List<Bson> andBsonList) {
        if (chunkSize <= 0) {
            // if chunkSize is not valid we call to the default method
            createRegionQuery(query, queryParam, andBsonList);
        } else {
            if (query != null && query.getString(queryParam) != null && !query.getString(queryParam).isEmpty()) {
                List<Region> regions = Region.parseRegions(query.getString(queryParam));
                if (regions != null && regions.size() > 0) {
                    if (regions.size() == 1) {
                        Bson chunkQuery = createChunkQuery(regions.get(0), chunkSize);
                        andBsonList.add(chunkQuery);
                    } else {
                        // if multiple regions we add them first to a OR list
                        List<Bson> orRegionBsonList = new ArrayList<>(regions.size());
                        for (Region region : regions) {
                            Bson chunkQuery = createChunkQuery(region, chunkSize);
                            orRegionBsonList.add(chunkQuery);
                        }
                        andBsonList.add(Filters.or(orRegionBsonList));
                    }
                }
            }
        }
    }

    private Bson createChunkQuery(Region region, int chunkSize) {
        int startChunkId = getChunkId(region.getStart(), chunkSize);
        int endChunkId = getChunkId(region.getEnd(), chunkSize);

        List<String> chunkIds = new ArrayList<>(endChunkId - startChunkId + 1);
        for (int chunkId = startChunkId; chunkId <= endChunkId; chunkId++) {
            chunkIds.add(region.getChromosome() + "_" + chunkId + "_" + chunkSize / 1000 + "k");
        }

        Bson chunk = Filters.in("_chunkIds", chunkIds);
        Bson start = Filters.lte("start", region.getEnd());
        Bson end = Filters.gte("end", region.getStart());
        return Filters.and(chunk, start, end);

//        // We only use chunks if region queried belongs to a single chunk
//        if (startChunkId == endChunkId) {
//            logger.info("Querying by chunkId, {}, {}", startChunkId, endChunkId);
//            Bson chunk = Filters.eq("_chunkIds", getChunkIdPrefix(region.getChromosomeInfo(), region.getStart(), chunkSize));
//            Bson start = Filters.lte("start", region.getEnd());
//            Bson end = Filters.gte("end", region.getStart());
//            return Filters.and(chunk, start, end);
//        } else {
//            Bson chromosome = Filters.eq("chromosome", region.getChromosomeInfo());
//            Bson start = Filters.lte("start", region.getEnd());
//            Bson end = Filters.gte("end", region.getStart());
//            return Filters.and(chromosome, start, end);
//        }
    }

    protected void createOrQuery(Query query, String queryParam, String mongoDbField, List<Bson> andBsonList) {
        createOrQuery(query, queryParam, mongoDbField, andBsonList, QueryValueType.STRING);
    }

    protected void createOrQuery(Query query, String queryParam, String mongoDbField, List<Bson> andBsonList,
                                 QueryValueType queryValueType) {
        if (query != null && query.getString(queryParam) != null && !query.getString(queryParam).isEmpty()) {
            switch (queryValueType) {
                case INTEGER:
                    createOrQuery(query.getAsIntegerList(queryParam), mongoDbField, andBsonList);
                    break;
                default:
                    createOrQuery(query.getAsStringList(queryParam), mongoDbField, andBsonList);
            }
        }
    }

    protected <T> void createOrQuery(List<T> queryValues, String mongoDbField, List<Bson> andBsonList) {
        if (queryValues.size() == 1) {
            andBsonList.add(Filters.eq(mongoDbField, queryValues.get(0)));
        } else {
            List<Bson> orBsonList = new ArrayList<>(queryValues.size());
            for (T queryItem : queryValues) {
                orBsonList.add(Filters.eq(mongoDbField, queryItem));
            }
            andBsonList.add(Filters.or(orBsonList));
        }
    }

    protected QueryResult groupBy(Bson query, String groupByField, String featureIdField, QueryOptions options) {
        if (groupByField == null || groupByField.isEmpty()) {
            return new QueryResult();
        }

        if (groupByField.contains(",")) {
            // call to multiple groupBy if commas are present
            return groupBy(query, Arrays.asList(groupByField.split(",")), featureIdField, options);
        } else {
            Bson match = Aggregates.match(query);
            Bson project = Aggregates.project(Projections.include(groupByField, featureIdField));
            Bson group;
            if (options.getBoolean("count", false)) {
                group = Aggregates.group("$" + groupByField, Accumulators.sum("count", 1));
                return mongoDBCollection.aggregate(Arrays.asList(match, project, group), options);
            } else {
                // Limit the documents passed if count is false
                Bson limit = Aggregates.limit(options.getInt("limit", 10));
                group = Aggregates.group("$" + groupByField, Accumulators.addToSet("features", "$" + featureIdField));
                // TODO change the default "_id" returned by mongodb to id
                return mongoDBCollection.aggregate(Arrays.asList(match, limit, project, group), options);
            }
        }
    }

    protected QueryResult groupBy(Bson query, List<String> groupByField, String featureIdField, QueryOptions options) {
        if (groupByField == null || groupByField.isEmpty()) {
            return new QueryResult();
        }

        if (groupByField.size() == 1) {
            // if only one field then we call to simple groupBy
            return groupBy(query, groupByField.get(0), featureIdField, options);
        } else {
            Bson match = Aggregates.match(query);
            // add all group-by fields to the projection together with the aggregation field name
            List<String> groupByFields = new ArrayList<>(groupByField);
            groupByFields.add(featureIdField);
            Bson project = Aggregates.project(Projections.include(groupByFields));
            // _id document creation to have the multiple id
            Document id = new Document();
            for (String s : groupByField) {
                id.append(s, "$" + s);
            }
            Bson group;
            if (options.getBoolean("count", false)) {
                group = Aggregates.group(id, Accumulators.sum("count", 1));
                return mongoDBCollection.aggregate(Arrays.asList(match, project, group), options);
            } else {
                // Limit the documents passed if count is false
                Bson limit = Aggregates.limit(options.getInt("limit", 10));
                group = Aggregates.group(id, Accumulators.addToSet("features", "$" + featureIdField));
                // TODO change the default "_id" returned by mongodb to id
                return mongoDBCollection.aggregate(Arrays.asList(match, limit, project, group), options);
            }
        }
    }


    public QueryResult getIntervalFrequencies(Bson query, Region region, int intervalSize, QueryOptions options) {
        //  MONGO QUERY TO IMPLEMENT
        //    db.variation.aggregate({$match: {$and: [{chromosome: "1"}, {start: {$gt: 251391, $lt: 2701391}}]}}, {$group:
        // {_id: {$subtract: [{$divide: ["$start", 40000]}, {$divide: [{$mod: ["$start", 40000]}, 40000]}]}, totalCount: {$sum: 1}}})
        //        {
        //            $match: {
        //                $and: [{
        //                    chromosome: "1"
        //                }, {
        //                    start: {
        //                        $gt: 251391,
        //                                $lt: 2701391
        //                    }
        //                }
        //                ]
        //            }
        //        }, {
        //            $group: {
        //                _id: {
        //                    $subtract: [{
        //                        $divide: ["$start", 40000]
        //                    }, {
        //                        $divide: [{
        //                            $mod: ["$start", 40000]
        //                        },
        //                        40000
        //                        ]
        //                    }
        //                    ]
        //                },
        //                totalCount: {
        //                    $sum: 1
        //                }
        //            }
        //        }

        int interval = 50000;
        if (intervalSize > 0) {
            interval = intervalSize;
        }

        Bson match = Aggregates.match(query);

        // group
//        Document divide1 = new Document();
//        divide1.append("$start", interval);
//        divide1.add("$start");
//        divide1.add(interval);
        BsonArray divide1 = new BsonArray();
        divide1.add(new BsonString("$start"));
        divide1.add(new BsonInt32(interval));

//        Document divide2 = new Document();
//        divide2.add(new Document("$mod", divide1));
//        divide2.add(interval);
        BsonArray divide2 = new BsonArray();
        divide2.add(new BsonDocument("$mod", divide1));
        divide2.add(new BsonInt32(interval));

//        Document subtractList = new Document();
//        subtractList.append("$divide", divide1);
//        subtractList.append("$divide", divide2);
        BsonArray subtractList = new BsonArray();
        subtractList.add(new BsonDocument("$divide", divide1));
        subtractList.add(new BsonDocument("$divide", divide2));
//        subtractList.add(new Document("$divide", divide1));
//        subtractList.add(new Document("$divide", divide2));

        Document substract = new Document("$subtract", subtractList);
        Document totalCount = new Document("$sum", 1);

        Document g = new Document("_id", substract);
        g.append("features_count", totalCount);
        Document group = new Document("$group", g);

//        Bson sort = Sorts.ascending("$_id");
        Document sort = new Document("$sort", new Document("_id", 1));

        QueryResult<Document> aggregationOutput = mongoDBCollection.aggregate(Arrays.asList(match, group, sort), options);

        Map<Long, Document> ids = new HashMap<>();
        for (Document intervalObj : aggregationOutput.getResult()) {
            Long id = Math.round((Double) intervalObj.get("_id")); //is double

            Document intervalVisited = ids.get(id);
            if (intervalVisited == null) {
                intervalObj.put("_id", id);
                intervalObj.put("chromosome", region.getChromosome());
                intervalObj.put("start", getChunkStart(id.intValue(), interval));
                intervalObj.put("end", getChunkEnd(id.intValue(), interval));
//                intervalObj.put("features_count", Math.log((int) intervalObj.get("features_count")));
                intervalObj.put("features_count", intervalObj.getInteger("features_count"));
                ids.put(id, intervalObj);
            } else {
//                Double sum = (Double) intervalVisited.get("features_count") + Math.log((int) intervalObj.get("features_count"));
                int sum = intervalVisited.getInteger("features_count") + intervalObj.getInteger("features_count");
                intervalVisited.put("features_count", sum);
            }
        }

        List<Document> resultList = new ArrayList<>();
        int firstChunkId = getChunkId(region.getStart(), interval);
        int lastChunkId = getChunkId(region.getEnd(), interval);
        Document intervalObj;
        for (int chunkId = firstChunkId; chunkId <= lastChunkId; chunkId++) {
            intervalObj = ids.get((long) chunkId);
            if (intervalObj == null) {
                intervalObj = new Document();
                intervalObj.put("_id", chunkId);
                intervalObj.put("chromosome", region.getChromosome());
                intervalObj.put("start", getChunkStart(chunkId, interval));
                intervalObj.put("end", getChunkEnd(chunkId, interval));
                intervalObj.put("features_count", 0);
            } else {
                intervalObj.put("features_count", Math.log(intervalObj.getInteger("features_count")));
            }
            resultList.add(intervalObj);
        }

        QueryResult queryResult = new QueryResult();
        queryResult.setResult(resultList);
        queryResult.setId(region.toString());
        queryResult.setResultType("frequencies");

        return queryResult;
    }

    protected QueryResult<Long> count(Bson query, MongoDBCollection mongoDBCollection) {
//        QueryResult result = cacheManager.get();
        QueryResult result = null;
        if (result == null) {
            result = mongoDBCollection.count(query);
//            cacheManager.set(result);
        }
        return result;
    }


    protected QueryResult distinct(String field, Bson query, MongoDBCollection mongoDBCollection) {
//        QueryResult result = cacheManager.get();
        QueryResult result = null;
        if (result == null) {
            result = mongoDBCollection.distinct(field, query);
//            cacheManager.set(result);
        }
        return result;
    }

    protected <T> QueryResult<T> executeBsonQuery(Bson bsonQuery, Bson projection, Query query, QueryOptions options,
                                                  MongoDBCollection mongoDBCollection, Class<T> clazz) {
        QueryResult<T> result = null;

        if (options.getBoolean("cache") && cacheManager.isActive()) {
            String key = cacheManager.createKey(this.species, query, options);
            result = cacheManager.get(key, clazz);
            if (result.getResult().size() != 0) {
                return result;
            } else {
                options.replace("cache", true, false);
                options = addPrivateExcludeOptions(options);
                result = mongoDBCollection.find(bsonQuery, projection, clazz, options);
                cacheManager.set(key, query, result);
            }
        } else {
            options = addPrivateExcludeOptions(options);
            result = mongoDBCollection.find(bsonQuery, projection, clazz, options);
        }
        return result;
    }


    @Deprecated
    protected QueryResult executeQuery(Object id, Document query, QueryOptions options) {
        return executeQueryList2(Arrays.asList(id), Arrays.asList(query), options, mongoDBCollection).get(0);
    }

    @Deprecated
    protected QueryResult executeQuery(Object id, Document query, QueryOptions options, MongoDBCollection mongoDBCollection2) {
        return executeQueryList2(Arrays.asList(id), Arrays.asList(query), options, mongoDBCollection2).get(0);
    }

    @Deprecated
    protected List<QueryResult> executeQueryList2(List<? extends Object> ids, List<Document> queries, QueryOptions options) {
        return executeQueryList2(ids, queries, options, mongoDBCollection);
    }

    @Deprecated
    protected List<QueryResult> executeQueryList2(List<? extends Object> ids, List<Document> queries, QueryOptions options,
                                                  MongoDBCollection mongoDBCollection2) {
        List<QueryResult> queryResults = new ArrayList<>(ids.size());
        long dbTimeStart, dbTimeEnd;

        for (int i = 0; i < queries.size(); i++) {
            Document query = queries.get(i);
            QueryResult queryResult = new QueryResult();
            queryResult.setId(ids.get(i).toString());

            // Execute query and calculate time
            dbTimeStart = System.currentTimeMillis();
            if (options.containsKey("count") && options.getBoolean("count")) {
                queryResult = mongoDBCollection2.count(query);
            } else {
                MongoCursor<Document> cursor = mongoDBCollection2.nativeQuery().find(query, options).iterator();
                List<Document> dbObjectList = new LinkedList<>();
                while (cursor.hasNext()) {
                    dbObjectList.add(cursor.next());
                }
                queryResult.setNumResults(dbObjectList.size());
                queryResult.setResult(dbObjectList);

                // Limit is set in queryOptions, count number of total results
                if (options != null && options.getInt("limit", 0) > 0) {
                    queryResult.setNumTotalResults(mongoDBCollection2.count(query).first());
                } else {
                    queryResult.setNumTotalResults(dbObjectList.size());
                }
            }
            dbTimeEnd = System.currentTimeMillis();
            queryResult.setDbTime(Long.valueOf(dbTimeEnd - dbTimeStart).intValue());

            queryResults.add(queryResult);
        }

        return queryResults;
    }

    protected QueryResult executeAggregation2(Object id, List<Bson> pipeline, QueryOptions options) {
        return executeAggregationist2(Arrays.asList(id), Arrays.asList(pipeline), options, mongoDBCollection).get(0);
    }

    protected List<QueryResult> executeAggregationList2(List<? extends Object> ids, List<List<Bson>> queries,
                                                        QueryOptions options) {
        return executeAggregationist2(ids, queries, options, mongoDBCollection);
    }

    protected List<QueryResult> executeAggregationist2(List<? extends Object> ids, List<List<Bson>> pipelines,
                                                       QueryOptions options, MongoDBCollection mongoDBCollection2) {
        List<QueryResult> queryResults = new ArrayList<>(ids.size());
//        logger.info("executeQueryList2");
        long dbTimeStart, dbTimeEnd;

        for (int i = 0; i < pipelines.size(); i++) {
            List<Bson> pipeline = pipelines.get(i);
//            QueryResult queryResult = new org.opencb.commons.datastore.core.QueryResult();

            // Execute query and calculate time
            dbTimeStart = System.currentTimeMillis();
            QueryResult queryResult = mongoDBCollection2.aggregate(pipeline, options);
//            List<Document> dbObjectList = new LinkedList<>();
//            while (cursor.hasNext()) {
//                dbObjectList.add(cursor.next());
//            }
            dbTimeEnd = System.currentTimeMillis();
//            // setting queryResult fields
            queryResult.setId(ids.get(i).toString());
            queryResult.setDbTime(Long.valueOf(dbTimeEnd - dbTimeStart).intValue());
            queryResult.setNumResults(queryResult.getResult().size());
//            // Limit is set in queryOptions, count number of total results
//            if(options != null && options.getInt("limit", 0) > 0) {
//                queryResult.setNumTotalResults(mongoDBCollection.count(pipeline).first());
//            } else {
//                queryResult.setNumTotalResults(dbObjectList.size());
//            }
//            queryResult.setResult(dbObjectList);

            queryResults.add(queryResult);
        }

        return queryResults;
    }

    protected String getChunkIdPrefix(String chromosome, int position, int chunkSize) {
        return chromosome + "_" + position / chunkSize + "_" + chunkSize / 1000 + "k";
    }

    protected int getChunkId(int position, int chunkSize) {
        return position / chunkSize;
    }

    private int getChunkStart(int id, int chunkSize) {
        return (id == 0) ? 1 : id * chunkSize;
    }

    private int getChunkEnd(int id, int chunkSize) {
        return (id * chunkSize) + chunkSize - 1;
    }


    public QueryResult next(String chromosome, int position, QueryOptions options, MongoDBCollection mongoDBCollection) {
        QueryBuilder builder;
        if (options.getString("strand") == null || options.getString("strand").equals("")
                || (options.getString("strand").equals("1") || options.getString("strand").equals("+"))) {
            builder = QueryBuilder.start("chromosome").is(chromosome).and("start").greaterThanEquals(position);
            options.put("sort", new HashMap<String, String>().put("start", "asc"));
            options.put("limit", 1);
        } else {
            builder = QueryBuilder.start("chromosome").is(chromosome).and("end").lessThanEquals(position);
            options.put("sort", new HashMap<String, String>().put("end", "desc"));
            options.put("limit", 1);
        }
        return executeQuery("result", new Document(builder.get().toMap()), options, mongoDBCollection);
    }

    @Deprecated
    protected QueryOptions addIncludeReturnFields(String returnField, QueryOptions options) {
        if (options != null) { //&& !options.getBoolean(returnField, true)
            if (options.getList("include") != null) {
//                options.put("include", options.get("include") + "," + returnField);
                options.getList("include").add(returnField);
            } else {
                options.put("include", Arrays.asList(returnField));
            }
        } else {
            options = new QueryOptions("include", Arrays.asList(returnField));
        }
        return options;
    }

    @Deprecated
    protected QueryOptions addExcludeReturnFields(String returnField, QueryOptions options) {
        if (options != null) { //&& !options.getBoolean(returnField, true)) {
            if (options.getList("exclude") != null) {
//                options.put("exclude", options.get("exclude") + "," + returnField);
                List<Object> arr = options.getList("exclude");
                arr.add(returnField);
//                options.getList("exclude").add(returnField);
                options.put("exclude", arr);
            } else {
                options.put("exclude", Arrays.asList(returnField));
            }
        } else {
            options = new QueryOptions("exclude", Arrays.asList(returnField));
        }
        return options;
    }

    @Deprecated
    protected Document getReturnFields(QueryOptions options) {
        // Select which fields are excluded and included in MongoDB query
        Document returnFields = new Document("_id", 0);
        if (options != null) {
//            List<Object> includeList = options.getList("include");

            // Read and process 'exclude' field from 'options' object
//        if (options != null && options.get("include") != null && !options.getString("include").equals("")) {
            if (options != null && options.getList("include") != null && options.getList("include").size() > 0) {
//            String[] includedOptionFields = options.getString("include").split(",");
//            if (includedOptionFields != null && includedOptionFields.length > 0) {
//            if (options.getList("include") != null && options.getList("include").size() > 0) {
                for (Object field : options.getList("include")) {
//                    returnFields.put(field, 1);
                    returnFields.put(field.toString(), 1);
                }
            } else {
//                List<Object> excludeList = options.getList("exclude");
//                if (options != null && options.get("exclude") != null && !options.getString("exclude").equals("")) {
                if (options != null && options.getList("exclude") != null && options.getList("exclude").size() > 0) {
//                    String[] excludedOptionFields = options.getString("exclude").split(",");
//                    if (excludedOptionFields != null && excludedOptionFields.length > 0) {
                    for (Object field : options.getList("exclude")) {
                        returnFields.put(field.toString(), 0);
                    }
                }
            }
        }
        return returnFields;
    }

//    @Deprecated
//    protected BasicDBList executeFind(Document query, Document returnFields, QueryOptions options) {
//        return executeFind(query, returnFields, options, mongoDBCollection);
//    }
//
//    @Deprecated
//    protected BasicDBList executeFind(Document query, Document returnFields, QueryOptions options, DBCollection dbCollection) {
//        BasicDBList list = new BasicDBList();
//
//        if (options.getBoolean("count")) {
//            Long count = dbCollection.count(query);
//            list.add(new Document("count", count));
//        }else {
//            DBCursor cursor = dbCollection.find(query, returnFields);
//
//            int limit = options.getInt("limit", 0);
//            if (limit > 0) {
//                cursor.limit(limit);
//            }
//            int skip = options.getInt("skip", 0);
//            if (skip > 0) {
//                cursor.skip(skip);
//            }
//
//            Document sort = (Document) options.get("sort");
//            if (sort != null) {
//                cursor.sort(sort);
//            }
//            try {
//                if (cursor != null) {
//                    while (cursor.hasNext()) {
//                        list.add(cursor.next());
//                    }
//                }
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//        }
//        return list;
//    }

//    @Deprecated
//    protected QueryResult executeDistinct(Object id, String key) {
//        return executeDistinct(id, key, mongoDBCollection);
//    }
//
//    @Deprecated
//    protected QueryResult executeDistinct(Object id, String key, DBCollection dbCollection) {
//        QueryResult queryResult = new QueryResult();
//        long dbTimeStart = System.currentTimeMillis();
//        List<String> diseases = dbCollection.distinct(key);
//        long dbTimeEnd = System.currentTimeMillis();
//        queryResult.setId(id.toString());
////        queryResult.setDbTime(dbTimeEnd - dbTimeStart);
//        queryResult.setResult(diseases);
//        queryResult.setNumResults(diseases.size());
//
//        return queryResult;
//    }

//    @Deprecated
//    protected QueryResult executeQuery(Object id, Document query, QueryOptions options) {
//        return executeQuery(id, query, options, mongoDBCollection);
//    }

//    @Deprecated
//    protected QueryResult executeQuery(Object id, Document query, QueryOptions options, DBCollection dbCollection) {
//        return executeQueryList(Arrays.asList(id), Arrays.asList(query), options, dbCollection).get(0);
//    }
//
//    @Deprecated
//    protected List<QueryResult> executeQueryList(List<? extends Object> ids, List<Document> queries, QueryOptions options) {
//        return executeQueryList(ids, queries, options, mongoDBCollection);
//    }

//    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions queryOptions) {
//        List<QueryResult> queryResult = new ArrayList<>(regions.size());
//        for (Region region : regions) {
//            queryResult.add(getIntervalFrequencies(region, queryOptions));
//        }
//        return queryResult;
//    }


    /*
     * For histograms
     */
    protected List<IntervalFeatureFrequency> getIntervalFeatureFrequencies(Region region, int interval, List<Object[]> objectList,
                                                                           int numFeatures, double maxSnpsInterval) {

        int numIntervals = (region.getEnd() - region.getStart()) / interval + 1;
        List<IntervalFeatureFrequency> intervalFeatureFrequenciesList = new ArrayList<>(numIntervals);

        float maxNormValue = 1;

        if (numFeatures != 0) {
            maxNormValue = (float) maxSnpsInterval / numFeatures;
        }

        int start = region.getStart();
        int end = start + interval;
        for (int i = 0, j = 0; i < numIntervals; i++) {
            if (j < objectList.size() && ((BigInteger) objectList.get(j)[0]).intValue() == i) {
                if (numFeatures != 0) {
                    intervalFeatureFrequenciesList.add(new IntervalFeatureFrequency(start, end,
                            ((BigInteger) objectList.get(j)[0]).intValue(), ((BigInteger) objectList.get(j)[1]).intValue(),
                            (float) Math.log(((BigInteger) objectList.get(j)[1]).doubleValue()) / numFeatures / maxNormValue));
                } else {    // no features for this chromosome
                    intervalFeatureFrequenciesList.add(new IntervalFeatureFrequency(start, end,
                            ((BigInteger) objectList.get(j)[0]).intValue(), ((BigInteger) objectList.get(j)[1]).intValue(), 0));
                }
                j++;
            } else {
                intervalFeatureFrequenciesList.add(new IntervalFeatureFrequency(start, end, i, 0, 0.0f));
            }
            start += interval;
            end += interval;
        }

        return intervalFeatureFrequenciesList;
    }


    protected List<IntervalFeatureFrequency> getIntervalFeatureFrequencies(Region region, int interval, List<Object[]> objectList) {

        int numIntervals = (region.getEnd() - region.getStart()) / interval + 1;
        List<IntervalFeatureFrequency> intervalFeatureFrequenciesList = new ArrayList<IntervalFeatureFrequency>(numIntervals);

        BigInteger max = new BigInteger("-1");
        for (int i = 0; i < objectList.size(); i++) {
            if (((BigInteger) objectList.get(i)[1]).compareTo(max) > 0) {
                max = (BigInteger) objectList.get(i)[1];
            }
        }

        int start = region.getStart();
        int end = start + interval;
        for (int i = 0, j = 0; i < numIntervals; i++) {
            if (j < objectList.size() && ((BigInteger) objectList.get(j)[0]).intValue() == i) {
                intervalFeatureFrequenciesList.add(new IntervalFeatureFrequency(start, end, ((BigInteger) objectList.get(j)[0]).intValue(),
                        ((BigInteger) objectList.get(j)[1]).intValue(),
                        ((BigInteger) objectList.get(j)[1]).floatValue() / max.floatValue()));
                j++;
            } else {
                intervalFeatureFrequenciesList.add(new IntervalFeatureFrequency(start, end, i, 0, 0.0f));
            }
            start += interval;
            end += interval;
        }

        return intervalFeatureFrequenciesList;
    }


    public String getSpecies() {
        return species;
    }


    public void setSpecies(String species) {
        this.species = species;
    }


    public String getAssembly() {
        return this.assembly;
    }


    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

}
