package org.opencb.cellbase.mongodb.db.core;

import org.junit.Test;
import org.opencb.cellbase.core.db.api.core.GeneDBAdaptor;
import org.opencb.cellbase.mongodb.GenericMongoDBAdaptorTest;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 08/10/15.
 */
public class GeneMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public GeneMongoDBAdaptorTest() { super(); }

    @Test
    public void testGetStatsById() throws Exception {
        GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor("hsapiens", "GRCh37");
        QueryResult queryResult = geneDBAdaptor.getStatsById("BRCA2", new QueryOptions());
    }
}