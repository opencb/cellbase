package org.opencb.cellbase.mongodb.db.core;

import org.junit.Test;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.db.DBAdaptorFactory;
import org.opencb.cellbase.core.db.api.core.ProteinDBAdaptor;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptorFactory;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 17/09/15.
 */
public class ProteinMongoDBAdaptorTest {

    @Test
    public void testGetVariantInfo() throws Exception {

        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();
        try {
            cellBaseConfiguration = CellBaseConfiguration
                    .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor("hsapiens", "GRCh37");
        QueryResult queryResult = proteinDBAdaptor.getVariantInfo("ENST00000252487", 49, "ARG", new QueryOptions());
//        QueryResult queryResult = proteinDBAdaptor.getVariantInfo("ENST00000252487", 130, "ARG", new QueryOptions());

    }
}