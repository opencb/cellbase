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

package org.opencb.cellbase.lib.impl.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.apache.commons.collections.CollectionUtils;
import org.bson.*;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.queries.AbstractQuery;
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDBQueryUtils;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

public class MongoDBAdaptor {

    enum QueryValueType {INTEGER, STRING}

    protected String species;
    protected String assembly;

    protected MongoDataStore mongoDataStore;
    protected MongoDBCollection mongoDBCollection;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected ObjectMapper objectMapper;

    public MongoDBAdaptor(MongoDataStore mongoDataStore) {
        this("", "", mongoDataStore);
    }

    public MongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        this.species = species;
        this.assembly = assembly;
        this.mongoDataStore = mongoDataStore;

        logger = LoggerFactory.getLogger(this.getClass().toString());
        objectMapper = new ObjectMapper();

        initSpeciesAssembly(species, assembly);
    }

    private void initSpeciesAssembly(String species, String assembly) {
        if (species != null && !species.equals("")) {
            // if 'version' parameter has not been provided the default version is selected
            if (this.assembly == null || this.assembly.trim().equals("")) {
                this.assembly = "default";
            }
        }
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

    protected AbstractQuery addPrivateExcludeOptions(AbstractQuery query) {
        query.addExcludes("_id,_chunkIds");
        return query;
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


    // add regions and IDs to the query, joined with OR
    protected void createIdRegionQuery(List<Region> regions, List<String> ids, List<Bson> andBsonList) {
        if (CollectionUtils.isEmpty(regions) && CollectionUtils.isEmpty(ids)) {
            return;
        }
        if (CollectionUtils.isEmpty(ids) && regions.size() == 1) {
            Bson chromosome = Filters.eq("chromosome", regions.get(0).getChromosome());
            Bson start = Filters.lte("start", regions.get(0).getEnd());
            Bson end = Filters.gte("end", regions.get(0).getStart());
            andBsonList.add(Filters.and(chromosome, start, end));
        } else if (CollectionUtils.isEmpty(regions) && ids.size() == 1) {
            Bson idFilter = Filters.eq("id", ids.get(0));
            andBsonList.add(idFilter);
        } else {
            List<Bson> orBsonList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(regions)) {
                for (Region region : regions) {
                    Bson chromosome = Filters.eq("chromosome", region.getChromosome());
                    Bson start = Filters.lte("start", region.getEnd());
                    Bson end = Filters.gte("end", region.getStart());
                    orBsonList.add(Filters.and(chromosome, start, end));
                }
            }
            if (CollectionUtils.isNotEmpty(ids)) {
                for (String id : ids) {
                    Bson idFilter = Filters.eq("id", id);
                    orBsonList.add(idFilter);
                }
            }
            andBsonList.add(Filters.or(orBsonList));
        }
    }

    protected Bson getProjection(org.opencb.cellbase.core.api.queries.QueryOptions options) {
        List<Bson> projections = new ArrayList<>();
        Bson include = null;
        List<String> includeStringList = options.getIncludes();
        if (includeStringList != null && includeStringList.size() > 0) {
            include = Projections.include(includeStringList);
        }
        Bson exclude = null;
        List<String> excludeStringList = options.getIncludes();
        if (excludeStringList != null && excludeStringList.size() > 0) {
            exclude = Projections.exclude(excludeStringList);
        }
        Bson projectionResult = null;
        if (projections.size() > 0) {
            projectionResult = Projections.fields(projections);
        }
        // If both include and exclude exist we only add include
        if (include != null) {
            projections.add(include);
            // MongoDB allows to exclude _id when include is present
            projections.add(Projections.excludeId());
        } else {
            if (exclude != null) {
                projections.add(exclude);
            }
        }
        return projectionResult;
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
            logger.debug(region.getChromosome() + "_" + chunkId + "_" + chunkSize / 1000 + "k");
        }

        Bson chunk = Filters.in("_chunkIds", chunkIds);
        Bson start = Filters.lte("start", region.getEnd());
        Bson end = Filters.gte("end", region.getStart());
        return Filters.and(chunk, start, end);
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

    protected CellBaseDataResult groupBy(Bson query, String groupByField, String featureIdField, QueryOptions options) {
        Boolean count = options.getBoolean("count", false);
        List<Bson> groupBy = MongoDBQueryUtils.createGroupBy(query, groupByField, featureIdField, count);
        return new CellBaseDataResult<>(mongoDBCollection.aggregate(groupBy, options));
    }

    protected CellBaseDataResult groupBy(Bson query, List<String> groupByField, String featureIdField, QueryOptions options) {
        Boolean count = options.getBoolean("count", false);
        List<Bson> groupBy = MongoDBQueryUtils.createGroupBy(query, groupByField, featureIdField, count);
        return new CellBaseDataResult<>(mongoDBCollection.aggregate(groupBy, options));
    }

    protected CellBaseDataResult groupBy(Bson bsonQuery, AbstractQuery abstractQuery, String featureIdField) {
        List<Bson> groupBy = MongoDBQueryUtils.createGroupBy(bsonQuery, abstractQuery.getFacet(), featureIdField, abstractQuery.getCount());
        return new CellBaseDataResult<>(mongoDBCollection.aggregate(groupBy, abstractQuery.toQueryOptions()));
    }

    public CellBaseDataResult getIntervalFrequencies(Bson query, Region region, int intervalSize, QueryOptions options) {
        int interval = 50000;
        if (intervalSize > 0) {
            interval = intervalSize;
        }

        Bson match = Aggregates.match(query);
        BsonArray divide1 = new BsonArray();
        divide1.add(new BsonString("$start"));
        divide1.add(new BsonInt32(interval));

        BsonArray divide2 = new BsonArray();
        divide2.add(new BsonDocument("$mod", divide1));
        divide2.add(new BsonInt32(interval));

        BsonArray subtractList = new BsonArray();
        subtractList.add(new BsonDocument("$divide", divide1));
        subtractList.add(new BsonDocument("$divide", divide2));

        Document substract = new Document("$subtract", subtractList);
        Document totalCount = new Document("$sum", 1);

        Document g = new Document("_id", substract);
        g.append("features_count", totalCount);
        Document group = new Document("$group", g);

        Document sort = new Document("$sort", new Document("_id", 1));

        CellBaseDataResult<Document> aggregationOutput = new CellBaseDataResult<>(mongoDBCollection.aggregate(
                Arrays.asList(match, group, sort), options));

        Map<Long, Document> ids = new HashMap<>();
        for (Document intervalObj : aggregationOutput.getResults()) {
            Long id = Math.round((Double) intervalObj.get("_id")); //is double

            Document intervalVisited = ids.get(id);
            if (intervalVisited == null) {
                intervalObj.put("_id", id);
                intervalObj.put("chromosome", region.getChromosome());
                intervalObj.put("start", getChunkStart(id.intValue(), interval));
                intervalObj.put("end", getChunkEnd(id.intValue(), interval));
                intervalObj.put("features_count", intervalObj.getInteger("features_count"));
                ids.put(id, intervalObj);
            } else {
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

        CellBaseDataResult cellBaseDataResult = new CellBaseDataResult();
        cellBaseDataResult.setResults(resultList);
        cellBaseDataResult.setId(region.toString());
        cellBaseDataResult.setResultType("frequencies");
        return cellBaseDataResult;
    }

    protected CellBaseDataResult executeDistinct(Object id, String fields, Document query) {
        CellBaseDataResult cellBaseDataResult = new CellBaseDataResult<>(mongoDBCollection.distinct(fields, query));
        cellBaseDataResult.setId(id.toString());
        return cellBaseDataResult;
    }

    protected CellBaseDataResult executeQuery(Object id, Document query, QueryOptions options) {
        return executeQueryList2(Arrays.asList(id), Arrays.asList(query), options, mongoDBCollection).get(0);
    }

    protected CellBaseDataResult executeQuery(Object id, Document query, QueryOptions options, MongoDBCollection mongoDBCollection2) {
        return executeQueryList2(Arrays.asList(id), Arrays.asList(query), options, mongoDBCollection2).get(0);
    }

    protected List<CellBaseDataResult> executeQueryList2(List<? extends Object> ids, List<Document> queries, QueryOptions options) {
        return executeQueryList2(ids, queries, options, mongoDBCollection);
    }

    protected List<CellBaseDataResult> executeQueryList2(List<? extends Object> ids, List<Document> queries, QueryOptions options,
                                                  MongoDBCollection mongoDBCollection2) {
        List<CellBaseDataResult> cellBaseDataResults = new ArrayList<>(ids.size());
        long dbTimeStart, dbTimeEnd;

        for (int i = 0; i < queries.size(); i++) {
            Document query = queries.get(i);
            CellBaseDataResult cellBaseDataResult = new CellBaseDataResult();
            cellBaseDataResult.setId(ids.get(i).toString());
            logger.debug("query: {}", query.toJson());
            logger.debug("QueryOptions: {}", options.toJson());
            // Execute query and calculate time
            dbTimeStart = System.currentTimeMillis();
            if (options.containsKey("count") && options.getBoolean("count")) {
                cellBaseDataResult = new CellBaseDataResult(mongoDBCollection2.count(query));
            } else {
                MongoDBIterator<Document> iterator = mongoDBCollection2.nativeQuery().find(query, options);
                List<Document> dbObjectList = new LinkedList<>();
                while (iterator.hasNext()) {
                    dbObjectList.add(iterator.next());
                }
                cellBaseDataResult.setNumResults(dbObjectList.size());
                cellBaseDataResult.setResults(dbObjectList);

                // Limit is set in queryOptions, count number of total results
                if (options != null && options.getInt("limit", 0) > 0) {
                    cellBaseDataResult.setNumMatches(mongoDBCollection2.count(query).first());
                } else {
                    cellBaseDataResult.setNumMatches(dbObjectList.size());
                }
            }
            dbTimeEnd = System.currentTimeMillis();
            cellBaseDataResult.setTime(Long.valueOf(dbTimeEnd - dbTimeStart).intValue());

            cellBaseDataResults.add(cellBaseDataResult);
        }

        return cellBaseDataResults;
    }

    protected CellBaseDataResult executeAggregation2(Object id, List<Bson> pipeline, QueryOptions options) {
        return executeAggregationist2(Arrays.asList(id), Arrays.asList(pipeline), options, mongoDBCollection).get(0);
    }

    protected List<CellBaseDataResult> executeAggregationList2(List<? extends Object> ids, List<List<Bson>> queries,
                                                        QueryOptions options) {
        return executeAggregationist2(ids, queries, options, mongoDBCollection);
    }

    protected List<CellBaseDataResult> executeAggregationist2(List<? extends Object> ids, List<List<Bson>> pipelines,
                                                       QueryOptions options, MongoDBCollection mongoDBCollection2) {
        List<CellBaseDataResult> cellBaseDataResults = new ArrayList<>(ids.size());
//        logger.info("executeQueryList2");
        long dbTimeStart, dbTimeEnd;

        for (int i = 0; i < pipelines.size(); i++) {
            List<Bson> pipeline = pipelines.get(i);

            // Execute query and calculate time
            dbTimeStart = System.currentTimeMillis();
            CellBaseDataResult cellBaseDataResult = new CellBaseDataResult(mongoDBCollection2.aggregate(pipeline, options));
            dbTimeEnd = System.currentTimeMillis();
//            // setting CellBaseDataResult fields
            cellBaseDataResult.setId(ids.get(i).toString());
            cellBaseDataResult.setTime(Long.valueOf(dbTimeEnd - dbTimeStart).intValue());
            cellBaseDataResult.setNumResults(cellBaseDataResult.getResults().size());
            cellBaseDataResults.add(cellBaseDataResult);
        }

        return cellBaseDataResults;
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

//
//    public CellBaseDataResult next(String chromosome, int position, QueryOptions options, MongoDBCollection mongoDBCollection) {
//        QueryBuilder builder;
//        if (options.getString("strand") == null || options.getString("strand").equals("")
//                || (options.getString("strand").equals("1") || options.getString("strand").equals("+"))) {
//            builder = QueryBuilder.start("chromosome").is(chromosome).and("start").greaterThanEquals(position);
//            options.put("sort", new HashMap<String, String>().put("start", "asc"));
//            options.put("limit", 1);
//        } else {
//            builder = QueryBuilder.start("chromosome").is(chromosome).and("end").lessThanEquals(position);
//            options.put("sort", new HashMap<String, String>().put("end", "desc"));
//            options.put("limit", 1);
//        }
//        return executeQuery("result", new Document(builder.get().toMap()), options, mongoDBCollection);
//    }

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
                List<Object> arr = options.getList("exclude");
                arr.add(returnField);
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
            // Read and process 'exclude' field from 'options' object
            if (options != null && options.getList("include") != null && options.getList("include").size() > 0) {
                for (Object field : options.getList("include")) {
                    returnFields.put(field.toString(), 1);
                }
            } else {
                if (options != null && options.getList("exclude") != null && options.getList("exclude").size() > 0) {
                    for (Object field : options.getList("exclude")) {
                        returnFields.put(field.toString(), 0);
                    }
                }
            }
        }
        return returnFields;
    }

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
                intervalFeatureFrequenciesList.add(new IntervalFeatureFrequency(start, end,
                ((BigInteger) objectList.get(j)[0]).intValue(),
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
