package org.opencb.cellbase.lib.mongodb;

import org.junit.Test;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.ConservedRegionDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;

/**
 * Created by fsalavert on 29/04/14.
 */
public class ConservedRegionMongoDBAdaptorTest {
    protected static DBAdaptorFactory dbAdaptorFactory;

//    static {
//        dbAdaptorFactory = new MongoDBAdaptorFactory();
//    }
//
//    @Test
//    public void testRegion() throws Exception {
//        QueryOptions queryOptions = new QueryOptions();
//        List<Region> regions = Region.parseRegions("1:9000-15000");
//        ConservedRegionDBAdaptor conservedRegionDBAdaptor = dbAdaptorFactory.getConservedRegionDBAdaptor("hsapiens", "V3");
//        List<QueryResult> a = conservedRegionDBAdaptor.getAllByRegionList(regions, queryOptions);
//        System.out.println(a);
//    }
}
