package org.opencb.cellbase.lib.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.DBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.math.BigInteger;
import java.util.*;

public class MongoDBAdaptor extends DBAdaptor {

    protected String species;
    protected String version;

    //	private MongoOptions mongoOptions;
    //	protected MongoClient mongoClient;
    protected DB db;
    protected DBCollection mongoDBCollection;
    protected static Map<String, Number> cachedQuerySizes = new HashMap<String, Number>();

    protected ObjectMapper jsonObjectMapper;

    protected static ResourceBundle resourceBundle;
    protected static Properties applicationProperties;


    static {
        // reading application.properties file
        resourceBundle = ResourceBundle.getBundle("mongodb");
//            applicationProperties = new Config(resourceBundle);
        applicationProperties = new Properties();
        if (resourceBundle != null) {
            Set<String> keys = resourceBundle.keySet();
            Iterator<String> iterator = keys.iterator();
            String nextKey;
            while (iterator.hasNext()) {
                nextKey = iterator.next();
                applicationProperties.put(nextKey, resourceBundle.getString(nextKey));
            }
        }
    }

    //	public MongoDBAdaptor(String species, String version) {
    //		logger.info("Species: "+species+" Version: "+version);
    //		this.mongoOptions = new MongoOptions();
    //		this.mongoOptions.setAutoConnectRetry(true);
    //		this.mongoOptions.setConnectionsPerHost(40);
    //		try {
    //			this.mongoClient = new MongoClient("mem15", mongoOptions);
    //		} catch (UnknownHostException e) {
    //			e.printStackTrace();
    //		}
    //	}

    public MongoDBAdaptor(DB db) {
        this.db = db;
    }

    public MongoDBAdaptor(DB db, String species, String version) {
        this.db = db;
        this.species = species;
        this.version = version;
        //		logger.warn(applicationProperties.toString());
        initSpeciesVersion(species, version);

        jsonObjectMapper = new ObjectMapper();
    }

    private void initSpeciesVersion(String species, String version) {
        if (species != null && !species.equals("")) {
            // if 'version' parameter has not been provided the default version is selected
            if (this.version == null || this.version.trim().equals("")) {
                this.version = applicationProperties.getProperty(species + ".DEFAULT.VERSION").toUpperCase();
                //				logger.debug("HibernateDBAdaptorFactory in createSessionFactory(): 'version' parameter is null or empty, it's been set to: '"+version+"'");
            }
        }
    }

    //	protected Session openSession() {
    //		if(session == null) {
    //			logger.debug("HibernateDBAdaptor: Session is null");
    //			session = sessionFactory.openSession();
    //		}else {
    //			if(!session.isOpen()) {
    //				logger.debug("HibernateDBAdaptor: Session is closed");
    //				session = sessionFactory.openSession();
    //			}else {
    //				logger.debug("HibernateDBAdaptor: Session is already open");
    //			}
    //		}
    //
    //		return session;
    //	}

    protected String getChunkPrefix(String chromosome, int position, int chunkSize) {
        return  chromosome + "_" +  position/chunkSize + "_" + chunkSize/1000 + "k";
    }


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

    protected QueryOptions addExcludeReturnFields(String returnField, QueryOptions options) {
        if (options != null && !options.getBoolean(returnField, true)) {
            if (options.getList("exclude") != null) {
//                options.put("exclude", options.get("exclude") + "," + returnField);
                options.getList("exclude").add(returnField);
            } else {
                options.put("exclude", Arrays.asList(returnField));
            }
        }else {
            options = new QueryOptions("exclude", Arrays.asList(returnField));
        }
        return options;
    }

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

    protected BasicDBList executeFind(DBObject query, DBObject returnFields, QueryOptions options) {
        return executeFind(query, returnFields, options, mongoDBCollection);
    }

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

    protected QueryResult executeDistinct(Object id, String key) {
        return executeDistinct(id, key, mongoDBCollection);
    }

    protected QueryResult executeDistinct(Object id, String key, DBCollection dbCollection) {
        QueryResult queryResult = new QueryResult();
        long dbTimeStart = System.currentTimeMillis();
        List<String> diseases = dbCollection.distinct(key);
        long dbTimeEnd = System.currentTimeMillis();
        queryResult.setId(id.toString());
        queryResult.setDBTime(dbTimeEnd - dbTimeStart);
        queryResult.setResult(diseases);
        queryResult.setNumResults(diseases.size());

        return queryResult;
    }

    protected QueryResult executeQuery(Object id, DBObject query, QueryOptions options) {
        return executeQuery(id, query, options, mongoDBCollection);
    }

    protected List<QueryResult> executeQueryList(List<? extends Object> ids, List<DBObject> queries, QueryOptions options) {
        return executeQueryList(ids, queries, options, mongoDBCollection);
    }

    protected QueryResult executeQuery(Object id, DBObject query, QueryOptions options, DBCollection dbCollection) {
        return executeQueryList(Arrays.asList(id), Arrays.asList(query), options, dbCollection).get(0);
    }

    protected List<QueryResult> executeQueryList(List<? extends Object> ids, List<DBObject> queries, QueryOptions options, DBCollection dbCollection) {
//		QueryResponse queryResponse = new QueryResponse();
        List<QueryResult> queryResults = new ArrayList<>(ids.size());

        // Select which fields are excluded and included in MongoDB query
        BasicDBObject returnFields = getReturnFields(options);
        System.out.println(returnFields.toString());
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
            queryResult.setDBTime((dbTimeEnd - dbTimeStart));
            queryResult.setNumResults(list.size());
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

    protected List<QueryResult> executeAggregationList(List<? extends Object> ids, List<DBObject[]> operationsList, QueryOptions options) {
        return executeAggregationList(ids, operationsList, options, mongoDBCollection);
    }

    protected QueryResult executeAggregation(Object id, DBObject[] operations, QueryOptions options, DBCollection dbCollection) {
        List<DBObject[]> operationsList = new ArrayList<>();
        operationsList.add(operations);
        return executeAggregationList(Arrays.asList(id), operationsList, options, dbCollection).get(0);
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
            queryResult.setDBTime((dbTimeEnd - dbTimeStart));
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
        //        QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
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
        //        System.out.println(region.getChromosome());
        //        System.out.println(region.getStart());
        //        System.out.println(region.getEnd());
        //        return intervalList.toString();
    }


    private int getChunkId(int position, int chunksize) {
        return position / chunksize;
    }

    private int getChunkStart(int id, int chunksize) {
        return (id == 0) ? 1 : id * chunksize;
    }

    private int getChunkEnd(int id, int chunksize) {
        return (id * chunksize) + chunksize - 1;
    }


    //	protected List<?> execute(Criteria criteria){
    //		List<?> result = criteria.list();
    //		return result;
    //	}
    //
    //	protected List<?> executeAndClose(Criteria criteria){
    //		List<?> result = criteria.list();
    //		//		closeSession();
    //		return result;
    //	}
    //
    //
    //	protected List<?> execute(Query query){
    //		List<?> result = query.list();
    //		return result;
    //	}
    //
    //	protected List<?> executeAndClose(Query query){
    //		List<?> result = query.list();
    //		//		closeSession();
    //		return result;
    //	}

    //	protected void closeSession() {
    //		if(session != null && session.isOpen()) {
    //			session.close();
    //		}
    //	}
    //
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

    //	/**
    //	 * @return the sessionFactory
    //	 */
    //	public SessionFactory getSessionFactory() {
    //		return sessionFactory;
    //	}
    //
    //	/**
    //	 * @param sessionFactory the sessionFactory to set
    //	 */
    //	public void setSessionFactory(SessionFactory sessionFactory) {
    //		this.sessionFactory = sessionFactory;
    //	}

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
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

}
