import org.junit.Test;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.GeneDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;
import org.opencb.cellbase.lib.mongodb.db.MongoDBAdaptorFactory;

import java.io.IOException;
import java.util.List;

public class IntervalTest {
    protected static DBAdaptorFactory dbAdaptorFactory;

//    static {
//        dbAdaptorFactory = new MongoDBAdaptorFactory();
//    }
//
//    @Test
//    public void testPosition() throws IOException {
//        System.out.println("works");
//
//        QueryOptions queryOptions = new QueryOptions();
//        queryOptions.put("interval","100000");
//
//        List<Region> regions = Region.parseRegions("13:32000000-32999999");
//        GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor("hsapiens", "V3");
//
//        List<QueryResult> res = geneDBAdaptor.getAllIntervalFrequencies(regions, queryOptions);
//        System.out.println(res);
//    }
}

