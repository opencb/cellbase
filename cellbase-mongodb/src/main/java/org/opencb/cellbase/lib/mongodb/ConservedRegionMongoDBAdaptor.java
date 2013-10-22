package org.opencb.cellbase.lib.mongodb;

import com.mongodb.DB;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.api.ConservedRegionDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;

@Deprecated
public class ConservedRegionMongoDBAdaptor extends MongoDBAdaptor implements ConservedRegionDBAdaptor {


    int CHUNKSIZE;

    public ConservedRegionMongoDBAdaptor(DB db) {
        super(db);
    }

    public ConservedRegionMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        CHUNKSIZE = Integer.parseInt(applicationProperties.getProperty("CELLBASE." + version.toUpperCase() + ".REGULATION.CHUNK_SIZE", "2000"));
        mongoDBCollection = db.getCollection("conservation");
    }

    @Override
    public QueryResult getAllByRegion(Region region, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


//    private List<ConservedRegion> executeQuery(DBObject query) {
//        List<ConservedRegion> result = null;
//        DBCursor cursor = mongoDBCollection.find(query);
//        try {
//            if (cursor != null) {
//                result = new ArrayList<ConservedRegion>(cursor.size());
////                Gson jsonObjectMapper = new Gson();
//                ConservedRegion feature = null;
//                while (cursor.hasNext()) {
////                    feature = (ConservedRegion) jsonObjectMapper.fromJson(cursor.next().toString(), ConservedRegion.class);
//                    result.add(feature);
//                }
//            }
//        } finally {
//            cursor.close();
//        }
//        return result;
//    }


//    @Override
//    public List<ConservedRegion> getByRegion(String chromosome, int start, int end) {
//        // positions below 1 are not allowed
//        if (start < 1) {
//            start = 1;
//        }
//        if (end < 1) {
//            end = 1;
//        }
//        QueryBuilder builder = QueryBuilder.start("chromosome").is(chromosome).and("end")
//                .greaterThan(start).and("start").lessThan(end);
//
//        System.out.println(builder.get().toString());
//        List<ConservedRegion> conservedRegionList = executeQuery(builder.get());
//
//        return conservedRegionList;
//    }

//    @Override
//    public List<List<ConservedRegion>> getByRegionList(List<Region> regions) {
//        List<List<ConservedRegion>> result = new ArrayList<List<ConservedRegion>>(regions.size());
//        for (Region region : regions) {
//            result.add(getByRegion(region.getChromosome(), region.getStart(), region.getEnd()));
//        }
//        return result;
//    }


}
