package org.opencb.cellbase.mongodb.db.core;

import org.junit.Test;
import org.opencb.cellbase.core.db.api.core.ProteinDBAdaptor;
import org.opencb.cellbase.mongodb.GenericMongoDBAdaptorTest;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

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
        QueryResult queryResult = proteinDBAdaptor.getVariantAnnotation("ENST00000252487", 49,"-", "ARG", new QueryOptions());
        int a=1;

    }
}