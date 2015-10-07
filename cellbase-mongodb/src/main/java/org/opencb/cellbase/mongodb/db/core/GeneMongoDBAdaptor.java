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

package org.opencb.cellbase.mongodb.db.core;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.db.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.db.api.variation.ClinicalDBAdaptor;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDataStore;

import java.util.*;

public class GeneMongoDBAdaptor extends MongoDBAdaptor implements GeneDBAdaptor {

    private int geneChunkSize = MongoDBCollectionConfiguration.GENE_CHUNK_SIZE;
    private ClinicalDBAdaptor clinicalDBAdaptor;

//    public GeneMongoDBAdaptor(DB db) {
//        super(db);
//    }

//    @Deprecated
//    public GeneMongoDBAdaptor(DB db, String species, String assembly) {
//        super(db, species, assembly);
//        mongoDBCollection = db.getCollection("gene");
//
//        logger.info("GeneMongoDBAdaptor: in 'constructor'");
//    }

    public GeneMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.debug("GeneMongoDBAdaptor: in 'constructor'");
    }


    public ClinicalDBAdaptor getClinicalDBAdaptor() {
        return clinicalDBAdaptor;
    }

    public void setClinicalDBAdaptor(ClinicalDBAdaptor clinicalDBAdaptor) {
        this.clinicalDBAdaptor = clinicalDBAdaptor;
    }

    @Override
    public QueryResult first() {
        return mongoDBCollection.find(new BasicDBObject(), new QueryOptions("limit", 1));
    }

    @Override
    public QueryResult count() {
        return mongoDBCollection.count();
    }

    @Override
    public QueryResult stats() {
        return null;
    }


    @Override
    public QueryResult getAll(QueryOptions options) {
        QueryBuilder builder = new QueryBuilder();

        List<String> biotypes = options.getAsStringList("biotype");
        if (biotypes != null && biotypes.size() > 0) {
            BasicDBList biotypeIds = new BasicDBList();
            biotypeIds.addAll(biotypes);
            builder = builder.and("biotype").in(biotypeIds);
        }

        return executeQuery("result", builder.get(), options);
    }

    public QueryResult next(String id, QueryOptions options) {
        QueryOptions _options = new QueryOptions();
        _options.put("include", Arrays.asList("chromosome", "start", "strand"));
        QueryResult queryResult = getAllById(id, _options);
        if(queryResult != null && queryResult.getResult() != null) {
            DBObject gene = (DBObject)queryResult.getResult().get(0);
            String chromosome = gene.get("chromosome").toString();
//            options.put("strand", gene.get("strand").toString());
            int start = Integer.parseInt(gene.get("start").toString());
            return next(chromosome, start, options);
        }
        return null;
    }

    @Override
    public QueryResult next(String chromosome, int position, QueryOptions options) {
        return next(chromosome, position + 1, options, mongoDBCollection);
    }


    @Override
    public QueryResult getAllById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        //		QueryBuilder builder = QueryBuilder.start("transcripts.xrefs.id").in(idList);

        List<DBObject> queries = new ArrayList<>(idList.size());
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("transcripts.xrefs.id").is(id);
            queries.add(builder.get());
        }

//        options = addExcludeReturnFields("transcripts", options);
//        return executeQueryList(idList, queries, options);
        return executeQueryList2(idList, queries, options);
    }

    @Override
    public QueryResult getStatsById(String id, QueryOptions options) {

        Map<String, Object> stats = new HashMap<>();
        QueryResult queryResult = new QueryResult();
        queryResult.setId(id);

        QueryBuilder geneBuilder = QueryBuilder.start("transcripts.xrefs.id").is(id);
        long dbTimeStart = System.currentTimeMillis();
        QueryResult geneQueryResult = executeQuery(id, geneBuilder.get(), new QueryOptions());
        QueryResult clinicalQueryResult = clinicalDBAdaptor.getByGeneId(id,
                new QueryOptions("include", "annot.consequenceTypes.soTerms,clinvarSet.referenceClinVarAssertion.clinicalSignificance.description"));
        long dbTimeEnd = System.currentTimeMillis();
        queryResult.setDbTime(Long.valueOf(dbTimeEnd - dbTimeStart).intValue());

        if(geneQueryResult.getNumResults()>0) {
            queryResult.setNumResults(1);
            stats = setCoreGeneStats(geneQueryResult, stats);
            stats = setVariantStats(clinicalQueryResult, stats);
            queryResult.setResult(Collections.singletonList(stats));
        }

        return queryResult;
//        gene name
//        ensembl gene id
//        chr
//        start
//        end
//        sequence length
//        num transcripts
//        breakdown num transcripts by biotype
//        num exons
//        num drug interactions
//        Clinical Variants {
//            #
//            Breakdown by clinical significance
//            Breakdown by SO
//        }



//        options = addExcludeReturnFields("transcripts", options);
//        return executeQueryList(idList, queries, options);

    }

    private Map<String, Object> setVariantStats(QueryResult queryResult, Map<String, Object> stats) {
        if(queryResult!=null && queryResult.getNumResults()>0) {
            Map<String, Map> clinicalVariantStats = new HashMap<>();
            Map<String, Map> clinicalSignificanceSummary = new HashMap<>();
            Map<String, Map> soSummary = new HashMap<>();
            for(Object result : queryResult.getResult()) {
                // TODO count and set summaries
            }
            clinicalVariantStats.put("clinicalSignificanceSummary", clinicalSignificanceSummary);
            clinicalVariantStats.put("soSummary", soSummary);
            stats.put("clinicalVariantStats", clinicalVariantStats);
        }
        return stats;
    }

    private Map<String, Object> setCoreGeneStats(QueryResult queryResult, Map<String, Object> stats){

        stats.put("name", ((BasicDBObject)queryResult.getResult()).get("name"));
        stats.put("id", ((BasicDBObject)queryResult.getResult()).get("id"));
        stats.put("chr", ((BasicDBObject)queryResult.getResult()).get("chr"));
        int start = (int)((BasicDBObject)queryResult.getResult()).get("start");
        stats.put("start", start);
        int end = (int)((BasicDBObject)queryResult.getResult()).get("end");
        stats.put("start", end);
        stats.put("length", end-start+1);

        return stats;
    }

    @Override
    public QueryResult getAllByXref(String id, QueryOptions options) {
        return null;
    }

    @Override
    public List<QueryResult> getAllByXrefList(List<String> idList, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getAllBiotypes(QueryOptions options) {
        DBObject query = null;

        if(options != null && options.get("chromosome") != null) {
            query = QueryBuilder.start("chromosome").is(options.get("chromosome")).get();
        }
        return executeDistinct("distinct", "biotype", query);
    }

    @Override
    public QueryResult getAllTargetsByTf(String tfId, QueryOptions queryOptions) {
        return null;
    }

    @Override
    public List<QueryResult> getAllTargetsByTfList(List<String> tfIdList, QueryOptions queryOptions) {
        return null;
    }


    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();

        List<Object> biotypes = options.getList("biotype", null);
        BasicDBList biotypeIds = new BasicDBList();
        if (biotypes != null && biotypes.size() > 0) {
            biotypeIds.addAll(biotypes);
        }

        List<String> ids = new ArrayList<>(regions.size());
        for (Region region : regions) {

            QueryBuilder builder = null;
            // If regions is 1 position then query can be optimize using chunks
            if (region.getStart() == region.getEnd()) {
                builder = QueryBuilder.start("_chunkIds").is(getChunkIdPrefix(region.getChromosome(), region.getStart(), geneChunkSize)).and("end")
                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
            } else {
                builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
            }

            if (biotypeIds.size() > 0) {
                builder = builder.and("biotype").in(biotypeIds);
            }
            queries.add(builder.get());
            ids.add(region.toString());
        }
        return executeQueryList2(ids, queries, options);
//        return executeQueryList(ids, queries, options);
    }


    @Override
    public QueryResult getIntervalFrequencies(Region region, QueryOptions queryOptions) {
        return super.getIntervalFrequencies(region, queryOptions);
    }

    @Override
    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions queryOptions) {
        return super.getAllIntervalFrequencies(regions, queryOptions);
    }


//    @Override
//    public QueryResult getIntervalFrequencies(Region region, int interval) {
//
//        BasicDBObject start = new BasicDBObject("$gt", region.getStart());
//        start.append("$lt", region.getEnd());
//
//        BasicDBList andArr = new BasicDBList();
//        andArr.add(new BasicDBObject("chromosome", region.getSequenceName()));
//        andArr.add(new BasicDBObject("start", start));
//
//        BasicDBObject match = new BasicDBObject("$match", new BasicDBObject("$and", andArr));
//
//
//        BasicDBList divide1 = new BasicDBList();
//        divide1.add("$start");
//        divide1.add(interval);
//
//        BasicDBList divide2 = new BasicDBList();
//        divide2.add(new BasicDBObject("$mod", divide1));
//        divide2.add(interval);
//
//        BasicDBList subtractList = new BasicDBList();
//        subtractList.add(new BasicDBObject("$divide", divide1));
//        subtractList.add(new BasicDBObject("$divide", divide2));
//
//
//        BasicDBObject substract = new BasicDBObject("$subtract", subtractList);
//
//        DBObject totalCount = new BasicDBObject("$sum", 1);
//
//        BasicDBObject g = new BasicDBObject("_id", substract);
//        g.append("features_count", totalCount);
//        BasicDBObject group = new BasicDBObject("$group", g);
//
//        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("_id", 1));
//
//        AggregationOutput output = mongoDBCollection.aggregate(match, group, sort);
//
//        System.out.println(output.getCommand());
//
//        HashMap<Long, DBObject> ids = new HashMap<>();
//        BasicDBList resultList = new BasicDBList();
//
//        for (DBObject intervalObj : output.results()) {
//            Long _id = Math.round((Double) intervalObj.get("_id"));//is double
//
//            DBObject intervalVisited = ids.get(_id);
//            if (intervalVisited == null) {
//                intervalObj.put("_id", _id);
//                intervalObj.put("start", getChunkStart(_id.intValue(), interval));
//                intervalObj.put("end", getChunkEnd(_id.intValue(), interval));
//                intervalObj.put("features_count", Math.log((int) intervalObj.get("features_count")));
//                ids.put(_id, intervalObj);
//                resultList.add(intervalObj);
//            } else {
//                Double sum = (Double) intervalVisited.get("features_count") + Math.log((int) intervalObj.get("features_count"));
//                intervalVisited.put("features_count", sum.intValue());
//            }
//        }
//        return BasicDBList;
//
//        //		QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getSequenceName()).and("end")
//        //				.greaterThan(region.getStart()).and("start").lessThan(region.getEnd());
//        //
//        //		int numIntervals = (region.getEnd() - region.getStart()) / interval + 1;
//        //		int[] intervalCount = new int[numIntervals];
//        //
//        //		List<Gene> genes = executeQuery(builder.get(),
//        //				Arrays.asList("transcripts,id,name,biotype,status,chromosome,end,strand,source,description"));
//        //
//        //		System.out.println("GENES index");
//        //		System.out.println("numIntervals: " + numIntervals);
//        //		for (Gene gene : genes) {
//        //			System.out.print("gs:" + gene.getStart() + " ");
//        //			if (gene.getStart() >= region.getStart() && gene.getStart() <= region.getEnd()) {
//        //				int intervalIndex = (gene.getStart() - region.getStart()) / interval; // truncate
//        //				System.out.print(intervalIndex + " ");
//        //				intervalCount[intervalIndex]++;
//        //			}
//        //		}
//        //		System.out.println("GENES index");
//        //
//        //		int intervalStart = region.getStart();
//        //		int intervalEnd = intervalStart + interval - 1;
//        //		BasicDBList intervalList = new BasicDBList();
//        //		for (int i = 0; i < numIntervals; i++) {
//        //			BasicDBObject intervalObj = new BasicDBObject();
//        //			intervalObj.put("start", intervalStart);
//        //			intervalObj.put("end", intervalEnd);
//        //			intervalObj.put("interval", i);
//        //			intervalObj.put("value", intervalCount[i]);
//        //			intervalList.add(intervalObj);
//        //			intervalStart = intervalEnd + 1;
//        //			intervalEnd = intervalStart + interval - 1;
//        //		}
//        //
//        //		System.out.println(region.getSequenceName());
//        //		System.out.println(region.getStart());
//        //		System.out.println(region.getEnd());
//        //		return intervalList.toString();
//
//    }
//
//    @Override
//    public List<QueryResult> getIntervalFrequencies(List<Region> regions, QueryOptions queryOptions) {
//        List<QueryResult> queryResult = new ArrayList<>(regions.size());
//        for (Region region :regions){
//            queryResult.add(getIntervalFrequencies(region, queryOptions));
//        }
//        return queryResult;
//    }
}
