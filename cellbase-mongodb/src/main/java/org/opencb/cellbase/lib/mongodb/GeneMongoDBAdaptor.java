package org.opencb.cellbase.lib.mongodb;

import com.mongodb.*;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.common.core.Gene;
import org.opencb.cellbase.core.lib.api.GeneDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.*;

//import org.bioinfo.infrared.core.cellbase.Gene;

public class GeneMongoDBAdaptor extends MongoDBAdaptor implements GeneDBAdaptor {


    public GeneMongoDBAdaptor(DB db) {
        super(db);
    }

    public GeneMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("core");

        logger.info("GeneMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult getAll(QueryOptions options) {
        QueryBuilder builder = new QueryBuilder();

        List<Object> biotypes = options.getList("biotypes", null);
        if (biotypes != null && biotypes.size() > 0) {
            BasicDBList biotypeIds = new BasicDBList();
            biotypeIds.addAll(biotypes);
            builder = builder.and("biotype").in(biotypeIds);
        }

        //		options = addExcludeReturnFields("transcripts", options);
        return executeQuery("result", builder.get(), options);
    }

    @Override
    public QueryResult next(String chromosome, int position, QueryOptions options) {
        if (options.getString("strand") == null || options.getString("strand").equals("") || (options.getString("strand").equals("1") || options.getString("strand").equals("+"))) {
            // db.core.find({chromosome: "1", start: {$gt: 1000000}}).sort({start: 1}).limit(1)
            QueryBuilder builder = QueryBuilder.start("chromosome").is(chromosome).and("start").greaterThanEquals(position);
            // options.put("sortAsc", "start");
            options.put("sort", new HashMap<String, String>().put("start", "asc"));
            options.put("limit", 1);
            // mongoDBCollection.find().sort(new BasicDBObject("", "")).limit(1);
            return executeQuery("result", builder.get(), options);
        } else {
            QueryBuilder builder = QueryBuilder.start("chromosome").is(chromosome).and("end").lessThanEquals(position);
            // options.put("sortDesc", "end");
            options.put("sort", new HashMap<String, String>().put("end", "desc"));
            options.put("limit", 1);
            //              mongoDBCollection.find().sort(new BasicDBObject("", "")).limit(1);
            return executeQuery("result", builder.get(), options);
        }
    }

    @Override
    public QueryResult next(String id, QueryOptions options) {
        // TODO Auto-generated method stub
        QueryBuilder builder = QueryBuilder.start("transcripts.xrefs.id").is(id);
        DBObject returnFields = getReturnFields(options);
        BasicDBList list = executeFind(builder.get(), returnFields, options);
        if (list != null && list.size() > 0) {
            DBObject gene = (DBObject) list.get(0);
            System.out.println(Integer.parseInt(gene.get("start").toString()));
            return next((String) gene.get("chromosome"), Integer.parseInt(gene.get("start").toString()), options);
        }
        return null;
    }

    // INFO:
    // next(chromosome, position) method has been moved to MongoDBAdaptor class

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
        return executeQueryList(idList, queries, options);
    }

    @Override
    public QueryResult getAllBiotypes(QueryOptions options) {
        String[] biotypes = applicationProperties.getProperty("CELLBASE."+version.toUpperCase()+".BIOTYPES").split(",");
        QueryResult queryResult = new QueryResult();
        queryResult.setId("result");
        DBObject result = new BasicDBObject("biotypes", biotypes);
        queryResult.setResult(Arrays.asList(result));
        queryResult.setDBTime(0);
        return queryResult;
    }

    @Override
    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options) {
        return getAllByRegion(new Region(chromosome, position, position), options);
    }

    @Override
    public QueryResult getAllByPosition(Position position, QueryOptions options) {
        return getAllByRegion(new Region(position.getChromosome(), position.getPosition(), position.getPosition()), options);
    }

    @Override
    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options) {
        List<Region> regions = new ArrayList<>();
        for (Position position : positionList) {
            regions.add(new Region(position.getChromosome(), position.getPosition(), position.getPosition()));
        }
        return getAllByRegionList(regions, options);
    }


    @Override
    public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options) {
        return getAllByRegion(new Region(chromosome, start, end), options);
    }

    //	db.core_chunks.find( {chunkIds: "1_36248", "start": {$lte: 144998347}, "end": {$gte: 144998347}}, {id:1, start:1, end:1}).explain()
    @Override
    public QueryResult getAllByRegion(Region region, QueryOptions options) {
        return getAllByRegionList(Arrays.asList(region), options).get(0);
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
                builder = QueryBuilder.start("chunkIds").is(region.getChromosome() + "_" + (region.getStart() / Integer.parseInt(applicationProperties.getProperty("CORE_CHUNK_SIZE", "5000")))).and("end")
                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
            } else {
                builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
            }

            if (biotypeIds.size() > 0) {
//                System.out.println("regions = [" + regions + "], options = [" + options + "]");
                builder = builder.and("biotype").in(biotypeIds);
            }
            queries.add(builder.get());
            ids.add(region.toString());
        }

//        options = addExcludeReturnFields("transcripts", options);
        return executeQueryList(ids, queries, options);
    }


    @Override
    public List<Gene> getAllByCytoband(String chromosome, String cytoband) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Gene> getAllBySnpId(String snpId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Gene>> getAllBySnpIdList(List<String> snpIdList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Gene> getAllByTf(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Gene>> getAllByTfList(List<String> idList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Gene> getAllByTfName(String tfName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Gene>> getAllByTfNameList(List<String> tfNameList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Gene> getAllTargetsByTf(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Gene>> getAllTargetsByTfList(List<String> idList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Gene> getAllByMiRnaMature(String mirbaseId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Gene>> getAllByMiRnaMatureList(List<String> mirbaseIds) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Gene> getAllTargetsByMiRnaMature(String mirbaseId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Gene>> getAllTargetsByMiRnaMatureList(List<String> mirbaseIds) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueryResult getAllIntervalFrequencies(Region region, QueryOptions queryOptions) {
        return super.getAllIntervalFrequencies(region, queryOptions);
    }

    @Override
    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions queryOptions) {
        return super.getAllIntervalFrequencies(regions, queryOptions);
    }

//    @Override
//    public QueryResult getAllIntervalFrequencies(Region region, int interval) {
//
//        BasicDBObject start = new BasicDBObject("$gt", region.getStart());
//        start.append("$lt", region.getEnd());
//
//        BasicDBList andArr = new BasicDBList();
//        andArr.add(new BasicDBObject("chromosome", region.getChromosome()));
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
//        //		QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
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
//        //		System.out.println(region.getChromosome());
//        //		System.out.println(region.getStart());
//        //		System.out.println(region.getEnd());
//        //		return intervalList.toString();
//
//    }
//
//    @Override
//    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions queryOptions) {
//        List<QueryResult> queryResult = new ArrayList<>(regions.size());
//        for (Region region :regions){
//            queryResult.add(getAllIntervalFrequencies(region, queryOptions));
//        }
//        return queryResult;
//    }
}
