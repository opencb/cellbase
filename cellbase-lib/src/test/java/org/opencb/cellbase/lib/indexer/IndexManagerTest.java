package org.opencb.cellbase.lib.indexer;

import org.bson.Document;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IndexManagerTest extends GenericMongoDBAdaptorTest {

    private IndexManager indexManager = null;
    private String databaseName = "cellbase_hsapiens_grch37_v4";

    public IndexManagerTest() throws IOException {
        indexManager = new IndexManager(cellBaseConfiguration, databaseName);
    }

    @Test
    public void testIndexes() throws IOException, CellbaseException {
        indexManager.createMongoDBIndexes("repeats", true);

        MongoDBAdaptorFactory factory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        MongoDataStore mongoDataStore = factory.getMongoDBDatastore("hsapiens", "grch37");
        MongoDBCollection mongoDBCollection = mongoDataStore.getCollection("repeats");
        DataResult<Document> index = mongoDBCollection.getIndex();
        assertNotNull(index);

        DataResult<Document> expected = new DataResult<>();
        expected.setNumResults(15);

        String expectedToString = "[Document{{v=2, key=Document{{_id=1}}, name=_id_, ns=cellbase_hsapiens_grch37_v4.repeats}}, Document{{v=2, key=Document{{_chunkIds=1}}, name=_chunkIds_1, ns=cellbase_hsapiens_grch37_v4.repeats, background=true}}, Document{{v=2, key=Document{{chromosome=1, start=1, end=1}}, name=chromosome_1_start_1_end_1, ns=cellbase_hsapiens_grch37_v4.repeats, background=true}}]";

        assertEquals(expectedToString, index.getResults().toString());
    }


}
