package org.opencb.cellbase.lib.mongodb.regulatory;

import com.mongodb.*;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;
import org.opencb.cellbase.lib.mongodb.MongoDBAdaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: fsalavert and mbleda :)
 * Date: 7/18/13
 * Time: 5:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegulatoryRegionMongoDBAdaptor extends MongoDBAdaptor implements RegulatoryRegionDBAdaptor {

    private static int CHUNKSIZE = 2000;

    public RegulatoryRegionMongoDBAdaptor(DB db) {
        super(db);
    }

    public RegulatoryRegionMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("regulatory_region");
    }

    @Override
    public QueryResult getAllById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("name").is(id);
//          System.out.println("Query: " + builder.get());
            queries.add(builder.get());
        }
        options = addExcludeReturnFields("chunkIds", options);
        return executeQueryList(idList, queries, options);
    }

    @Override
    public QueryResult getAllByPosition(Position position, QueryOptions options) {
        return getAllByPositionList(Arrays.asList(position), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options) {
        //  db.regulatory_region.find({"chunkIds": {$in:["1_200", "1_300"]}, "start": 601156})

        String featureType = options.getString("featureType", null);
        String featureClass = options.getString("featureClass", null);

        List<DBObject> queries = new ArrayList<>();
        for (Position position : positionList) {
            String chunkId = position.getChromosome() + "_" + getChunkId(position.getPosition(), CHUNKSIZE);
            BasicDBList chunksId = new BasicDBList();
            chunksId.add(chunkId);
            QueryBuilder builder = QueryBuilder.start("chunkIds").in(chunksId).and("start").is(position.getPosition());
            if (featureType != null) {
                builder.and("featureType").is(featureType);
            }
            if (featureClass != null) {
                builder.and("featureClass").is(featureClass);
            }

//        System.out.println("Query: " + builder.get());
            queries.add(builder.get());
        }

        System.out.println("Query: " + queries);

        options = addExcludeReturnFields("chunkIds", options);
        return executeQueryList(positionList, queries, options);
    }


    @Override
    public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options) {
        return getAllByRegion(new Region(chromosome, start, end), options);
    }

    @Override
    public QueryResult getAllByRegion(Region region, QueryOptions options) {
        return getAllByRegionList(Arrays.asList(region), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regionList, QueryOptions options) {
        //  db.regulatory_region.find({"chunkIds": {$in:["1_200", "1_300"]}, "start": 601156})
        QueryBuilder builder = new QueryBuilder();

        List<Object> featureType = options.getList("featureType", null);
        List<Object> featureClass = options.getList("featureClass", null);

        options = addExcludeReturnFields("chunkIds", options);

        List<DBObject> queries = new ArrayList<>();
        for (Region region : regionList) {
            int firstChunkId = getChunkId(region.getStart(), CHUNKSIZE);
            int lastChunkId = getChunkId(region.getEnd(), CHUNKSIZE);
            BasicDBList chunksId = new BasicDBList();
            for (int j = firstChunkId; j <= lastChunkId; j++) {
                String chunkId = region.getChromosome() + "_" + j;
                chunksId.add(chunkId);
            }

            builder = builder.start("chunkIds").in(chunksId)
                             .and("start").lessThanEquals(region.getEnd())
                             .and("end").greaterThanEquals(region.getStart());

            if (featureType != null && featureType.size() > 0) {
                BasicDBList featureTypeDBList = new BasicDBList();
                featureTypeDBList.addAll(featureType);
                builder = builder.and("featureType").in(featureTypeDBList);
            }

            if (featureClass != null && featureClass.size() > 0) {
                BasicDBList featureClassDBList = new BasicDBList();
                featureClassDBList.addAll(featureClass);
                builder = builder.and("featureClass").in(featureClassDBList);
            }

            queries.add(builder.get());
        }
        System.out.println(">>"+regionList);
        System.out.println(">>"+builder.get().toString());
        return executeQueryList(regionList, queries, options);
    }

    @Override
    public QueryResult next(String chromosome, int position, QueryOptions options) {

        String featureType = options.getString("featureType", null);
        String featureClass = options.getString("featureClass", null);

        BasicDBList chunksId = new BasicDBList();
        String chunkId = chromosome + "_" + getChunkId(position, CHUNKSIZE);
        chunksId.add(chunkId);

        // TODO: Add query to find next item considering next chunk
        // db.regulatory_region.find({ "chromosome" : "19" , "start" : { "$gt" : 62005} , "featureType" : "TF_binding_site_motif"}).sort({start:1}).limit(1)

        QueryBuilder builder;
        if (options.getString("strand") == null || (options.getString("strand").equals("1") || options.getString("strand").equals("+"))) {
            // db.core.find({chromosome: "1", start: {$gt: 1000000}}).sort({start: 1}).limit(1)
            builder = QueryBuilder.start("chunkIds").in(chunksId).and("chromosome").is(chromosome).and("start").greaterThan(position);
            options.put("sort", new BasicDBObject("start", 1));
            options.put("limit", 1);
        } else {
            builder = QueryBuilder.start("chunkIds").in(chunksId).and("chromosome").is(chromosome).and("end").lessThan(position);
            options.put("sort", new BasicDBObject("end", -1));
            options.put("limit", 1);
        }

        if (featureType != null) {
            builder.and("featureType").is(featureType);
        }
        if (featureClass != null) {
            builder.and("featureClass").is(featureClass);
        }
        System.out.println(builder.get());
        return executeQuery("result", builder.get(), options);
    }


    @Override
    public QueryResult getAll(QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    private static int getChunkId(int position, int chunksize) {
        if (chunksize <= 0) {
            return position / CHUNKSIZE;
        } else {
            return position / chunksize;
        }
    }

    private static int getChunkStart(int id, int chunksize) {
        if (chunksize <= 0) {
            return (id == 0) ? 1 : id * CHUNKSIZE;
        } else {
            return (id == 0) ? 1 : id * chunksize;
        }
    }

    private static int getChunkEnd(int id, int chunksize) {
        if (chunksize <= 0) {
            return (id * CHUNKSIZE) + CHUNKSIZE - 1;
        } else {
            return (id * chunksize) + chunksize - 1;
        }
    }
}
