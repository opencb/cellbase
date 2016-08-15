package org.opencb.cellbase.lib.db.core;

import org.junit.Test;
import org.opencb.cellbase.core.api.ProteinDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;

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