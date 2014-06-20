package org.opencb.cellbase.lib.mongodb;

import com.google.common.base.Splitter;
import org.junit.Test;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.XRefsDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;

public class XRefsMongoDBAdaptorTest {
    private static DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory();
    private static String species = "hsapiens";
    private static String version = "v3";

    @Test
    public void testGetByStartsWithQueryList() throws Exception {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put("limit", 51);
        List<String> ids = Splitter.on(",").splitToList("BRCA");
        XRefsDBAdaptor xRefsDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.version);
        List<QueryResult> a = xRefsDBAdaptor.getByStartsWithQueryList(ids, queryOptions);
        System.out.println(a);
    }
}