package org.opencb.cellbase.mongodb.db.core;

import org.junit.Test;
import org.opencb.cellbase.core.api.ProteinDBAdaptor;
import org.opencb.cellbase.mongodb.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

/**
 * Created by fjlopez on 17/09/15.
 */
public class ProteinMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public ProteinMongoDBAdaptorTest() {
        super();
    }

    @Test
    public void testGetVariantAnnotation() throws Exception {
        ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor("hsapiens", "GRCh37");
        //QueryResult queryResult = proteinDBAdaptor.getVariantAnnotation("ENST00000252487", 49, "ARG", new QueryOptions());
//        QueryResult queryResult = proteinDBAdaptor.getVariantAnnotation("ENST00000252487", 49,"-", "ARG", new QueryOptions());
//        QueryResult queryResult = proteinDBAdaptor.getVariantAnnotation("ENST00000252487", 4934123,"-", "ARG", new QueryOptions()); // Should not return any result
        int a=1;

    }
}