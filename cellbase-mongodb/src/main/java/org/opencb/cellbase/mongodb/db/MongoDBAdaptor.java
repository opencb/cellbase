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

package org.opencb.cellbase.mongodb.db;

import com.mongodb.*;
import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDBCollection;
import org.opencb.datastore.mongodb.MongoDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

public class MongoDBAdaptor {

    protected String species;
    protected String assembly;

    protected MongoDataStore mongoDataStore;
    protected MongoDBCollection mongoDBCollection2;

    //	Old classes
    @Deprecated
    protected DB db;
    @Deprecated
    protected DBCollection mongoDBCollection;

    protected static Map<String, Number> cachedQuerySizes = new HashMap<String, Number>();

//    protected ObjectMapper jsonObjectMapper;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Deprecated
    public MongoDBAdaptor(DB db) {
        this.db = db;
    }

    @Deprecated
    public MongoDBAdaptor(DB db, String species, String assembly) {
        this.db = db;
        this.species = species;
        this.assembly = assembly;
        //		logger.warn(applicationProperties.toString());
        initSpeciesAssembly(species, assembly);

//        jsonObjectMapper = new ObjectMapper();
    }

    public MongoDBAdaptor(MongoDataStore mongoDataStore) {
        this("", "", mongoDataStore);
        this.db = mongoDataStore.getDb();
    }

    public MongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        this.species = species;
        this.assembly = assembly;
        this.mongoDataStore = mongoDataStore;
        this.db = mongoDataStore.getDb();

        logger = LoggerFactory.getLogger(this.getClass().toString());

        initSpeciesAssembly(species, assembly);
//        jsonObjectMapper = new ObjectMapper();
    }

    @Deprecated
    private void initSpeciesVersion(String species, String version) {
        if (species != null && !species.equals("")) {
            // if 'version' parameter has not been provided the default version is selected
//            if (this.version == null || this.version.trim().equals("")) {
//                this.version = applicationProperties.getProperty(species + ".DEFAULT.VERSION").toUpperCase();
//                //				logger.debug("HibernateDBAdaptorFactory in createSessionFactory(): 'version' parameter is null or empty, it's been set to: '"+version+"'");
//            }
        }
    }

    private void initSpeciesAssembly(String species, String assembly) {
        if (species != null && !species.equals("")) {
            // if 'version' parameter has not been provided the default version is selected
            if (this.assembly == null || this.assembly.trim().equals("")) {
                this.assembly = "default";
                //				logger.debug("HibernateDBAdaptorFactory in createSessionFactory(): 'version' parameter is null or empty, it's been set to: '"+version+"'");
            }
        }
    }


    protected QueryResult executeQuery(Object id, DBObject query, QueryOptions options) {
        return executeQueryList2(Arrays.asList(id), Arrays.asList(query), options, mongoDBCollection2).get(0);
    }

    protected QueryResult executeQuery(Object id, DBObject query, QueryOptions options, MongoDBCollection mongoDBCollection2) {
        return executeQueryList2(Arrays.asList(id), Arrays.asList(query), options, mongoDBCollection2).get(0);
    }

    protected List<QueryResult> executeQueryList2(List<? extends Object> ids, List<DBObject> queries, QueryOptions options) {
        return executeQueryList2(ids, queries, options, mongoDBCollection2);
    }

    protected List<QueryResult> executeQueryList2(List<? extends Object> ids, List<DBObject> queries, QueryOptions options,
                                                  MongoDBCollection mongoDBCollection2) {
        List<QueryResult> queryResults = new ArrayList<>(ids.size());
//        logger.info("executeQueryList2");
//        System.out.println("executeQueryList2");
        long dbTimeStart, dbTimeEnd;

        for (int i = 0; i < queries.size(); i++) {
            DBObject query = queries.get(i);
            QueryResult queryResult = new org.opencb.datastore.core.QueryResult();

            // Execute query and calculate time
            dbTimeStart = System.currentTimeMillis();
            DBCursor cursor = mongoDBCollection2.nativeQuery().find(query, options);
            List<DBObject> dbObjectList = new LinkedList<>();
            while (cursor.hasNext()) {
                dbObjectList.add(cursor.next());
            }
            dbTimeEnd = System.currentTimeMillis();
            // setting queryResult fields
            queryResult.setId(ids.get(i).toString());
            queryResult.setDbTime(Long.valueOf(dbTimeEnd - dbTimeStart).intValue());
            queryResult.setNumResults(dbObjectList.size());
            // Limit is set in queryOptions, count number of total results
            if(options != null && options.getInt("limit", 0) > 0) {
                queryResult.setNumTotalResults(mongoDBCollection2.count(query).first());
            } else {
                queryResult.setNumTotalResults(dbObjectList.size());
            }
            queryResult.setResult(dbObjectList);

            queryResults.add(queryResult);
        }

        return queryResults;
    }

    protected QueryResult executeAggregation2(Object id, List<DBObject> pipeline, QueryOptions options) {
        return executeAggregationist2(Arrays.asList(id), Arrays.asList(pipeline), options, mongoDBCollection2).get(0);
    }

    protected List<QueryResult> executeAggregationList2(List<? extends Object> ids, List<List<DBObject>> queries,
                                                        QueryOptions options) {
        return executeAggregationist2(ids, queries, options, mongoDBCollection2);
    }

    protected List<QueryResult> executeAggregationist2(List<? extends Object> ids, List<List<DBObject>> pipelines,
                                                       QueryOptions options, MongoDBCollection mongoDBCollection2) {
        List<QueryResult> queryResults = new ArrayList<>(ids.size());
//        logger.info("executeQueryList2");
//        System.out.println("executeQueryList2");
        long dbTimeStart, dbTimeEnd;

        for (int i = 0; i < pipelines.size(); i++) {
            List<DBObject> pipeline = pipelines.get(i);
//            QueryResult queryResult = new org.opencb.datastore.core.QueryResult();

            // Execute query and calculate time
            dbTimeStart = System.currentTimeMillis();
            QueryResult queryResult = mongoDBCollection2.aggregate(pipeline, options);
//            List<DBObject> dbObjectList = new LinkedList<>();
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
//                queryResult.setNumTotalResults(mongoDBCollection2.count(pipeline).first());
//            } else {
//                queryResult.setNumTotalResults(dbObjectList.size());
//            }
//            queryResult.setResult(dbObjectList);

            queryResults.add(queryResult);
        }

        return queryResults;
    }

    protected String getChunkIdPrefix(String chromosome, int position, int chunkSize) {
        return chromosome + "_" +  position/chunkSize + "_" + chunkSize/1000 + "k";
    }

    @Deprecated
    protected QueryOptions addIncludeReturnFields(String returnField, QueryOptions options) {
        if (options != null ) { //&& !options.getBoolean(returnField, true)
            if (options.getList("include") != null) {
//                options.put("include", options.get("include") + "," + returnField);
                options.getList("include").add(returnField);
            } else {
                options.put("include", Arrays.asList(returnField));
            }
        }else {
            options = new QueryOptions("include", Arrays.asList(returnField));
        }
        return options;
    }

    @Deprecated
    protected QueryOptions addExcludeReturnFields(String returnField, QueryOptions options) {
        if (options != null ) { //&& !options.getBoolean(returnField, true)) {
            if (options.getList("exclude") != null) {
//                options.put("exclude", options.get("exclude") + "," + returnField);
                List<Object> arr = options.getList("exclude");
                arr.add(returnField);
//                options.getList("exclude").add(returnField);
                options.put("exclude", arr);
            } else {
                options.put("exclude", Arrays.asList(returnField));
            }
        }else {
            options = new QueryOptions("exclude", Arrays.asList(returnField));
        }
        return options;
    }

    @Deprecated
    protected BasicDBObject getReturnFields(QueryOptions options) {
        // Select which fields are excluded and included in MongoDB query
        BasicDBObject returnFields = new BasicDBObject("_id", 0);
        if(options != null) {
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

    @Deprecated
    protected BasicDBList executeFind(DBObject query, DBObject returnFields, QueryOptions options) {
        return executeFind(query, returnFields, options, mongoDBCollection);
    }

    @Deprecated
    protected BasicDBList executeFind(DBObject query, DBObject returnFields, QueryOptions options, DBCollection dbCollection) {
        BasicDBList list = new BasicDBList();

        if (options.getBoolean("count")) {
            Long count = dbCollection.count(query);
            list.add(new BasicDBObject("count", count));
        }else {
            DBCursor cursor = dbCollection.find(query, returnFields);

            int limit = options.getInt("limit", 0);
            if (limit > 0) {
                cursor.limit(limit);
            }
            int skip = options.getInt("skip", 0);
            if (skip > 0) {
                cursor.skip(skip);
            }

            BasicDBObject sort = (BasicDBObject) options.get("sort");
            if (sort != null) {
                cursor.sort(sort);
            }
            try {
                if (cursor != null) {
                    while (cursor.hasNext()) {
                        list.add(cursor.next());
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return list;
    }

    @Deprecated
    protected QueryResult executeDistinct(Object id, String key) {
        return executeDistinct(id, key, mongoDBCollection);
    }

    @Deprecated
    protected QueryResult executeDistinct(Object id, String key, DBCollection dbCollection) {
        QueryResult queryResult = new QueryResult();
        long dbTimeStart = System.currentTimeMillis();
        List<String> diseases = dbCollection.distinct(key);
        long dbTimeEnd = System.currentTimeMillis();
        queryResult.setId(id.toString());
//        queryResult.setDbTime(dbTimeEnd - dbTimeStart);
        queryResult.setResult(diseases);
        queryResult.setNumResults(diseases.size());

        return queryResult;
    }

//    @Deprecated
//    protected QueryResult executeQuery(Object id, DBObject query, QueryOptions options) {
//        return executeQuery(id, query, options, mongoDBCollection);
//    }

    @Deprecated
    protected QueryResult executeQuery(Object id, DBObject query, QueryOptions options, DBCollection dbCollection) {
        return executeQueryList(Arrays.asList(id), Arrays.asList(query), options, dbCollection).get(0);
    }

    @Deprecated
    protected List<QueryResult> executeQueryList(List<? extends Object> ids, List<DBObject> queries, QueryOptions options) {
        return executeQueryList(ids, queries, options, mongoDBCollection);
    }

    @Deprecated
    protected List<QueryResult> executeQueryList(List<? extends Object> ids, List<DBObject> queries, QueryOptions options, DBCollection dbCollection) {
//		QueryResponse queryResponse = new QueryResponse();
        List<QueryResult> queryResults = new ArrayList<>(ids.size());

        // Select which fields are excluded and included in MongoDB query
        BasicDBObject returnFields = getReturnFields(options);
//        System.out.println(returnFields.toString());
        // Time parameters
//		long timeStart = System.currentTimeMillis();
        long dbTimeStart, dbTimeEnd;

        for (int i = 0; i < queries.size(); i++) {
            DBObject query = queries.get(i);
            QueryResult queryResult = new QueryResult();

            // Execute query and calculate time
            dbTimeStart = System.currentTimeMillis();
            BasicDBList list = executeFind(query, returnFields, options, dbCollection);
            dbTimeEnd = System.currentTimeMillis();

            // setting queryResult fields
            queryResult.setId(ids.get(i).toString());
//            queryResult.setDbTime((dbTimeEnd - dbTimeStart));
            // Limit is set in queryOptions, count number of total results
            if(options.getInt("limit", 0) > 0) {
                queryResult.setNumResults((int) dbCollection.count(query));
            } else {
                queryResult.setNumResults(list.size());
            }

            queryResult.setResult(list);

            queryResults.add(queryResult);


//          //TODO move query response to webservicve
//			// Save QueryResult into QueryResponse object
//			if(ids.get(i) != null && !ids.get(i).equals("")) {
//				queryResponse.put(ids.get(i).toString(), queryResult);
//			}else {
//				// some 'getAll' queries do not have a query ID, for those
//				// cases an empty string is accepted and 'result' is written
//				queryResponse.put("result", queryResult);
//			}
        }
//		long timeEnd = System.currentTimeMillis();

//        //TODO
//		// Check if 'metadata' field must be returned
//		if (options != null && options.getBoolean("metadata", true)) {
//			queryResponse.getMetadata().put("queryIds", ids);
//			queryResponse.getMetadata().put("time", timeEnd - timeStart);
//		} else {
//			queryResponse.removeField("metadata");
//		}

        return queryResults;
    }


    protected QueryResult executeAggregation(Object id, DBObject[] operations, QueryOptions options) {
        return executeAggregation(id, operations, options, mongoDBCollection);
    }

    protected QueryResult executeAggregation(Object id, DBObject[] operations, QueryOptions options, DBCollection dbCollection) {
        List<DBObject[]> operationsList = new ArrayList<>();
        operationsList.add(operations);
        return executeAggregationList(Arrays.asList(id), operationsList, options, dbCollection).get(0);
    }

    protected List<QueryResult> executeAggregationList(List<? extends Object> ids, List<DBObject[]> operationsList, QueryOptions options) {
        return executeAggregationList(ids, operationsList, options, mongoDBCollection);
    }

    protected List<QueryResult> executeAggregationList(List<? extends Object> ids, List<DBObject[]> operationsList, QueryOptions options, DBCollection dbCollection) {
        List<QueryResult> queryResults = new ArrayList<>(ids.size());
        AggregationOutput aggregationOutput;

        long dbTimeStart, dbTimeEnd;
        for (int i = 0; i < operationsList.size(); i++) {
            DBObject[] operations = operationsList.get(i);

            // MongoDB aggregate method signature is: public AggregationOutput aggregate( DBObject firstOp, DBObject ... additionalOps)
            // so the operations array must be decomposed, TODO check operations length
            DBObject firstOperation = operations[0];
            DBObject[] additionalOperations = Arrays.copyOfRange(operations, 1, operations.length);

            QueryResult queryResult = new QueryResult();

            // Execute query and calculate time
            dbTimeStart = System.currentTimeMillis();
            aggregationOutput = dbCollection.aggregate(firstOperation, additionalOperations);
            dbTimeEnd = System.currentTimeMillis();

            BasicDBList list = new BasicDBList();
            try {
                if (aggregationOutput != null) {
                    Iterator<DBObject> results = aggregationOutput.results().iterator();
                    while (results.hasNext()) {
                        list.add(results.next());
                    }
                }
            } finally {

            }
            queryResult.setId(ids.get(i).toString());
//            queryResult.setDbTime((dbTimeEnd - dbTimeStart));
            queryResult.setResult(list);

            queryResults.add(queryResult);
        }
        return queryResults;
    }


    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions queryOptions) {
        List<QueryResult> queryResult = new ArrayList<>(regions.size());
        for (Region region : regions) {
            queryResult.add(getAllIntervalFrequencies(region, queryOptions));
        }
        return queryResult;
    }

    public QueryResult getAllIntervalFrequencies(Region region, QueryOptions options) {
        //  MONGO QUERY TO IMPLEMENT
        //    db.variation.aggregate({$match: {$and: [{chromosome: "1"}, {start: {$gt: 251391, $lt: 2701391}}]}}, {$group: {_id: {$subtract: [{$divide: ["$start", 40000]}, {$divide: [{$mod: ["$start", 40000]}, 40000]}]}, totalCount: {$sum: 1}}})
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

        int interval = options.getInt("interval");

        BasicDBObject start = new BasicDBObject("$gt", region.getStart());
        start.append("$lt", region.getEnd());

        BasicDBList andArr = new BasicDBList();
        andArr.add(new BasicDBObject("chromosome", region.getChromosome()));
        andArr.add(new BasicDBObject("start", start));

        BasicDBObject match = new BasicDBObject("$match", new BasicDBObject("$and", andArr));


        BasicDBList divide1 = new BasicDBList();
        divide1.add("$start");
        divide1.add(interval);

        BasicDBList divide2 = new BasicDBList();
        divide2.add(new BasicDBObject("$mod", divide1));
        divide2.add(interval);

        BasicDBList subtractList = new BasicDBList();
        subtractList.add(new BasicDBObject("$divide", divide1));
        subtractList.add(new BasicDBObject("$divide", divide2));


        BasicDBObject substract = new BasicDBObject("$subtract", subtractList);

        DBObject totalCount = new BasicDBObject("$sum", 1);

        BasicDBObject g = new BasicDBObject("_id", substract);
        g.append("features_count", totalCount);
        BasicDBObject group = new BasicDBObject("$group", g);

        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("_id", 1));

        logger.info("getAllIntervalFrequencies - (>·_·)>");
        System.out.println(options.toString());

        System.out.println(match.toString());
        System.out.println(group.toString());
        System.out.println(sort.toString());

        AggregationOutput output = mongoDBCollection.aggregate(match, group, sort);

        System.out.println(output.getCommand());

        Map<Long, DBObject> ids = new HashMap<>();
        for (DBObject intervalObj : output.results()) {
            Long _id = Math.round((Double) intervalObj.get("_id"));//is double

            DBObject intervalVisited = ids.get(_id);
            if (intervalVisited == null) {
                intervalObj.put("_id", _id);
                intervalObj.put("start", getChunkStart(_id.intValue(), interval));
                intervalObj.put("end", getChunkEnd(_id.intValue(), interval));
                intervalObj.put("chromosome", region.getChromosome());
                intervalObj.put("features_count", Math.log((int) intervalObj.get("features_count")));
                ids.put(_id, intervalObj);
            } else {
                Double sum = (Double) intervalVisited.get("features_count") + Math.log((int) intervalObj.get("features_count"));
                intervalVisited.put("features_count", sum.intValue());
            }
        }

        /****/
        BasicDBList resultList = new BasicDBList();
        int firstChunkId = getChunkId(region.getStart(), interval);
        int lastChunkId = getChunkId(region.getEnd(), interval);
        DBObject intervalObj;
        for (int chunkId = firstChunkId; chunkId <= lastChunkId; chunkId++) {
            intervalObj = ids.get((long) chunkId);
            if (intervalObj == null) {
                intervalObj = new BasicDBObject();
                intervalObj.put("_id", chunkId);
                intervalObj.put("start", getChunkStart(chunkId, interval));
                intervalObj.put("end", getChunkEnd(chunkId, interval));
                intervalObj.put("chromosome", region.getChromosome());
                intervalObj.put("features_count", 0);
            }
            resultList.add(intervalObj);
        }
        /****/

        QueryResult queryResult = new QueryResult();
        queryResult.setResult(resultList);
        queryResult.setId(region.toString());
        queryResult.setResultType("frequencies");

        return queryResult;


        /***************************/
        //        QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getSequenceName()).and("end")
        //                .greaterThan(region.getStart()).and("start").lessThan(region.getEnd());
        //
        //        int numIntervals = (region.getEnd() - region.getStart()) / interval + 1;
        //        int[] intervalCount = new int[numIntervals];
        //
        //        List<Variation> variationList = executeQuery(builder.get(), Arrays.asList("id,chromosome,end,strand,type,reference,alternate,alleleString,species,assembly,source,version,transcriptVariations,xrefs,featureId,featureAlias,variantFreq,validationStatus"));
        //
        //        System.out.println("Variation index");
        //        System.out.println("numIntervals: " + numIntervals);
        //        for (Variation variation : variationList) {
        //            System.out.print("gsnp start:" + variation.getStart() + " ");
        //            if (variation.getStart() >= region.getStart() && variation.getStart() <= region.getEnd()) {
        //                int intervalIndex = (variation.getStart() - region.getStart()) / interval; // truncate
        //                System.out.print(intervalIndex + " ");
        //                intervalCount[intervalIndex]++;
        //            }
        //        }
        //        System.out.println("Variation index");
        //
        //        int intervalStart = region.getStart();
        //        int intervalEnd = intervalStart + interval - 1;
        //        BasicDBList intervalList = new BasicDBList();
        //        for (int i = 0; i < numIntervals; i++) {
        //            BasicDBObject intervalObj = new BasicDBObject();
        //            intervalObj.put("start", intervalStart);
        //            intervalObj.put("end", intervalEnd);
        //            intervalObj.put("interval", i);
        //            intervalObj.put("value", intervalCount[i]);
        //            intervalList.add(intervalObj);
        //            intervalStart = intervalEnd + 1;
        //            intervalEnd = intervalStart + interval - 1;
        //        }
        //
        //        System.out.println(region.getSequenceName());
        //        System.out.println(region.getStart());
        //        System.out.println(region.getEnd());
        //        return intervalList.toString();
    }


    protected int getChunkId(int position, int chunksize) {
        return position / chunksize;
    }

    private int getChunkStart(int id, int chunksize) {
        return (id == 0) ? 1 : id * chunksize;
    }

    private int getChunkEnd(int id, int chunksize) {
        return (id * chunksize) + chunksize - 1;
    }


    //	@SuppressWarnings("unchecked")
    //	protected String getDatabaseQueryCache(String key) {
    //		Criteria criteria = this.openSession().createCriteria(Metainfo.class)
    //			.add(Restrictions.eq("property", key));
    //		List<Metainfo> metaInfoList = (List<Metainfo>) executeAndClose(criteria);
    //		if(metaInfoList != null && metaInfoList.size() > 0) {
    //			return metaInfoList.get(0).getValue();
    //		}else {
    //			return null;
    //		}
    //	}
    //
    //	protected void putDatabaseQueryCache(String key, String value) {
    ////		Query query = this.openSession().createQuery("insert into Metainfo (property, value) values ('"+key+"', '"+value+"')");
    ////		query.executeUpdate();
    //
    //		Session session = this.openSession();
    //		session.beginTransaction();
    //		session.save( new Metainfo( key, value ) );
    //		session.getTransaction().commit();
    //		session.close();
    //	}

    /**
     * For histograms
     */
    protected List<IntervalFeatureFrequency> getIntervalFeatureFrequencies(Region region, int interval, List<Object[]> objectList, int numFeatures, double maxSnpsInterval) {

        int numIntervals = (region.getEnd() - region.getStart()) / interval + 1;
        List<IntervalFeatureFrequency> intervalFeatureFrequenciesList = new ArrayList<IntervalFeatureFrequency>(numIntervals);

        //		BigInteger max = new BigInteger("-1");
        //		for(int i=0; i<objectList.size(); i++) {
        //			if(((BigInteger)objectList.get(i)[1]).compareTo(max) > 0) {
        //				max = (BigInteger)objectList.get(i)[1];
        //			}
        //		}
        float maxNormValue = 1;

        if (numFeatures != 0) {
            maxNormValue = (float) maxSnpsInterval / numFeatures;
        }

        int start = region.getStart();
        int end = start + interval;
        for (int i = 0, j = 0; i < numIntervals; i++) {
            if (j < objectList.size() && ((BigInteger) objectList.get(j)[0]).intValue() == i) {
                if (numFeatures != 0) {
                    intervalFeatureFrequenciesList.add(new IntervalFeatureFrequency(start, end, ((BigInteger) objectList.get(j)[0]).intValue()
                            , ((BigInteger) objectList.get(j)[1]).intValue()
                            , (float) Math.log(((BigInteger) objectList.get(j)[1]).doubleValue()) / numFeatures / maxNormValue));
                } else {    // no features for this chromosome
                    intervalFeatureFrequenciesList.add(new IntervalFeatureFrequency(start, end, ((BigInteger) objectList.get(j)[0]).intValue()
                            , ((BigInteger) objectList.get(j)[1]).intValue()
                            , 0));
                }
                j++;
            } else {
                intervalFeatureFrequenciesList.add(new IntervalFeatureFrequency(start, end, i, 0, 0.0f));
            }
            //			System.out.println(intervalFeatureFrequenciesList.get(i).getStart()+":"+intervalFeatureFrequenciesList.get(i).getEnd()+":"+intervalFeatureFrequenciesList.get(i).getInterval()+":"+ intervalFeatureFrequenciesList.get(i).getAbsolute()+":"+intervalFeatureFrequenciesList.get(i).getValue());

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
                intervalFeatureFrequenciesList.add(new IntervalFeatureFrequency(start, end, ((BigInteger) objectList.get(j)[0]).intValue()
                        , ((BigInteger) objectList.get(j)[1]).intValue()
                        , ((BigInteger) objectList.get(j)[1]).floatValue() / max.floatValue()));
                j++;
            } else {
                intervalFeatureFrequenciesList.add(new IntervalFeatureFrequency(start, end, i, 0, 0.0f));
            }
            //			System.out.println(intervalFeatureFrequenciesList.get(i).getStart()+":"+intervalFeatureFrequenciesList.get(i).getEnd()+":"+intervalFeatureFrequenciesList.get(i).getInterval()+":"+ intervalFeatureFrequenciesList.get(i).getAbsolute()+":"+intervalFeatureFrequenciesList.get(i).getValue());

            start += interval;
            end += interval;
        }

        return intervalFeatureFrequenciesList;
    }


    /**
     * @return the species
     */
    public String getSpecies() {
        return species;
    }

    /**
     * @param species the species to set
     */
    public void setSpecies(String species) {
        this.species = species;
    }


    /**
     *
     * @return the assembly
     */
    public String getAssembly() { return this.assembly; }

    /**
     *
     * @param assembly param to set
     */
    public void setAssembly(String assembly) { this.assembly = assembly; }

}
