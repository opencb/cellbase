package org.opencb.cellbase.mongodb;

import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.common.core.Gene;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.mongodb.impl.MongoDBAdaptorFactory;

import java.io.IOException;

/**
 * Created by fjlopez on 18/09/15.
 */
public class GenericMongoDBAdaptorTest {

    protected DBAdaptorFactory dbAdaptorFactory;

    public GenericMongoDBAdaptorTest() {
        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();
        try {
            cellBaseConfiguration = CellBaseConfiguration
                    .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
    }

}
