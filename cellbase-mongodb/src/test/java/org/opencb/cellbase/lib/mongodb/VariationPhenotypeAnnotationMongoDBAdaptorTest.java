package org.opencb.cellbase.lib.mongodb;

import org.junit.Test;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.variation.VariationPhenotypeAnnotationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

/**
 * Created by fsalavert on 30/04/14.
 */
public class VariationPhenotypeAnnotationMongoDBAdaptorTest {
    protected static DBAdaptorFactory dbAdaptorFactory;

//    static {
//        dbAdaptorFactory = new MongoDBAdaptorFactory();
//    }
//
//    @Test
//    public void testRegion() throws Exception {
//        QueryOptions queryOptions = new QueryOptions();
//        VariationPhenotypeAnnotationDBAdaptor va = dbAdaptorFactory.getVariationPhenotypeAnnotationDBAdaptor("hsapiens", "V3");
//        QueryResult result = va.getAllByPhenotype("Hip", queryOptions);
//        System.out.println();
//    }
}
