package org.opencb.cellbase.lib.indexer;

import org.bson.Document;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IndexManagerTest extends GenericMongoDBAdaptorTest {

    IndexManager indexManager = null;

    public IndexManagerTest() throws IOException {
        indexManager = new IndexManager(cellBaseConfiguration);
    }

    @Test
    public void testIndexes() throws IOException, CellbaseException {
        indexManager.createMongoDBIndexes("repeats", "hsapiens", "grch37", true);

        MongoDBAdaptorFactory factory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        MongoDataStore mongoDataStore = factory.getMongoDBDatastore("hsapiens", "grch37");
        MongoDBCollection mongoDBCollection = mongoDataStore.getCollection("repeats");
        DataResult<Document> index = mongoDBCollection.getIndex();
        assertNotNull(index);

        DataResult<Document> expected = new DataResult<>();
        expected.setNumResults(15);

        String expectedToString = "[Document{{v=2, key=Document{{_id=1}}, name=_id_, ns=cellbase_hsapiens_grch37_v4.repeats}}, Document{{v=2, key=Document{{_chunkIds=1, start=1, end=1}}, name=_chunkIds_1_start_1_end_1, ns=cellbase_hsapiens_grch37_v4.repeats, background=true}}]";

        assertTrue(expectedToString.equalsIgnoreCase(index.getResults().toString()));
    }
}
