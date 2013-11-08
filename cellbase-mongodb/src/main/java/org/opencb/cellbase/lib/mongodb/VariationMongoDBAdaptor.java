package org.opencb.cellbase.lib.mongodb;

import com.mongodb.*;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.api.variation.VariationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.*;

public class VariationMongoDBAdaptor extends MongoDBAdaptor implements VariationDBAdaptor {


    public VariationMongoDBAdaptor(DB db) {
        super(db);
    }

    public VariationMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("variation");
    }

    //    private List<Variation> executeQuery(DBObject query, List<String> excludeFields) {
    //        List<Variation> result = null;
    //
    //        DBCursor cursor = null;
    //        if (excludeFields != null && excludeFields.size() > 0) {
    //            BasicDBObject returnFields = new BasicDBObject();
    //                returnFields.put("_id", 0);
    //            for (String field : excludeFields) {
    //                returnFields.put(field, 0);
    //            }
    //            System.out.println(query);
    //            System.out.println(returnFields);
    //            cursor = mongoDBCollection.find(query, returnFields);
    //
    //        } else {
    //            System.out.println(query);
    //            cursor = mongoDBCollection.find(query);
    //        }
    //
    //
    //        try {
    //            if (cursor != null) {
    //                result = new ArrayList<Variation>(cursor.size());
    ////                Gson jsonObjectMapper = new Gson();
    //                Variation variation = null;
    //                while (cursor.hasNext()) {
    ////                    variation = (Variation) jsonObjectMapper.fromJson(cursor.next().toString(), Variation.class);
    //                    result.add(variation);
    //                }
    //            }
    //        } finally {
    //            cursor.close();
    //        }
    //        return result;
    //    }

    @Override
    public QueryResult getById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("id").is(id);
            queries.add(builder.get());
        }

        return executeQueryList(idList, queries, options);
    }


    @Override
    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options) {
        return getAllByRegion(chromosome, position, position, options);
    }

    @Override
    public QueryResult getAllByPosition(Position position, QueryOptions options) {
        return getAllByRegion(new Region(position.getChromosome(), position.getPosition(), position.getPosition()), options);
    }

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

    @Override
    //  public QueryResponse getAllByRegion(String chromosome, int start, int end, List<String> consequence_types, List<String> exclude) {
    public QueryResult getAllByRegion(Region region, QueryOptions options) {
        return getAllByRegionList(Arrays.asList(region), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();

        String consequenceTypes = options.getString("consequence_type", null);
        BasicDBList consequenceTypeDBList = new BasicDBList();
        if (consequenceTypes != null && !consequenceTypes.equals("")) {
            for (String ct : consequenceTypes.split(",")) {
                consequenceTypeDBList.add(ct);
            }
        }

        List<String> ids = new ArrayList<>(regions.size());
        for (Region region : regions) {
            //			QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end").greaterThan(region.getStart()).and("start").lessThan(region.getEnd());
            QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("start").greaterThanEquals(region.getStart()).lessThanEquals(region.getEnd());
            if (consequenceTypeDBList.size() > 0) {
                builder = builder.and("transcriptVariations.consequenceTypes").in(consequenceTypeDBList);
            }
            queries.add(builder.get());
            ids.add(region.toString());
        }

        return executeQueryList(ids, queries, options);
    }
    @Override
    public QueryResult getAllIntervalFrequencies(Region region, QueryOptions queryOptions) {
        return super.getAllIntervalFrequencies(region, queryOptions);
    }

    @Override
    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions queryOptions) {
        return super.getAllIntervalFrequencies(regions, queryOptions);
    }
}
