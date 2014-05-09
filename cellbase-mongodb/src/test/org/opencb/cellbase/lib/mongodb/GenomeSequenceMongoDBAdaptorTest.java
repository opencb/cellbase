package org.opencb.cellbase.lib.mongodb;

import org.junit.Test;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.GenomeSequenceDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;

/**
 * Created by fsalavert on 30/04/14.
 */
public class GenomeSequenceMongoDBAdaptorTest {
    protected static DBAdaptorFactory dbAdaptorFactory;

    static {
        dbAdaptorFactory = new MongoDBAdaptorFactory();
    }

    @Test
    public void testRegion() throws Exception {
        QueryOptions queryOptions = new QueryOptions();
        List<Region> regions = Region.parseRegions("1:9000-15000");
        GenomeSequenceDBAdaptor genomeSequenceDBAdaptor = dbAdaptorFactory.getGenomeSequenceDBAdaptor("hsapiens", "V3");
        List<QueryResult> a = genomeSequenceDBAdaptor.getAllByRegionList(regions, queryOptions);
        System.out.println(a);
    }
}
