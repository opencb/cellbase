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

package org.opencb.cellbase.lib.managers;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.QueryBuilder;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.*;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.core.DBAdaptorFactory;
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

public class AbstractManager {

    protected CellBaseConfiguration configuration;
    protected CellBaseManagerFactory managers;
    protected DBAdaptorFactory dbAdaptorFactory;
    protected static ObjectWriter jsonObjectWriter;

    protected String species;
    protected String assembly;

    protected Logger logger;

    enum QueryValueType {INTEGER, STRING}
    public static final int DEFAULT_LIMIT = 10;
    protected int histogramIntervalSize = 200000;

    public AbstractManager(CellBaseConfiguration configuration) {
        this.configuration = configuration;

        this.init();
    }

    public AbstractManager(String species, CellBaseConfiguration configuration) {
        this(species, null, configuration);
    }

    public AbstractManager(String species, String assembly, CellBaseConfiguration configuration) {
        this.species = species;
        this.assembly = assembly;
        this.configuration = configuration;

        this.init();
    }

    private void init() {
        managers = new CellBaseManagerFactory(this.configuration);
        dbAdaptorFactory = new MongoDBAdaptorFactory(this.configuration);

        logger = LoggerFactory.getLogger(this.getClass());
    }

    protected List<Query> createQueries(Query query, String csvField, String queryKey, String... args) {
        String[] ids = csvField.split(",");
        List<Query> queries = new ArrayList<>(ids.length);
        for (String id : ids) {
            Query q = new Query(query);
            q.put(queryKey, id);
            if (args != null && args.length > 0 && args.length % 2 == 0) {
                for (int i = 0; i < args.length; i += 2) {
                    q.put(args[i], args[i + 1]);
                }
            }
            queries.add(q);
        }
        return queries;
    }

    private String getHistogramParameter(QueryOptions queryOptions) {
        return (queryOptions.get("histogram") != null) ? queryOptions.getString("histogram") : "false";
    }

    protected int getHistogramIntervalSize(QueryOptions queryOptions) {
        if (queryOptions.containsKey("interval")) {
            int value = histogramIntervalSize;
            try {
                value = queryOptions.getInt("interval");
                return value;
            } catch (Exception exp) {
                exp.printStackTrace();
                /** malformed string y no se puede castear a int **/
                return value;
            }
        } else {
            return histogramIntervalSize;
        }
    }

    protected boolean hasHistogramQueryParam(QueryOptions queryOptions) {
        return Boolean.parseBoolean(getHistogramParameter(queryOptions));
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

    public CellBaseDataResult next(String chromosome, int position, QueryOptions options, MongoDBCollection mongoDBCollection) {
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


}
