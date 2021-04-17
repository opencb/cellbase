package org.opencb.cellbase.lib.indexer;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.db.MongoDBManager;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IndexManagerTest extends GenericMongoDBAdaptorTest {

    private IndexManager indexManager = null;
    private String databaseName = "cellbase_hsapiens_grch37_v4";

    public IndexManagerTest() throws IOException {
        indexManager = new IndexManager(cellBaseConfiguration, databaseName);
    }

    @Test
    public void testIndexes() throws IOException, CellBaseException {
        indexManager.createMongoDBIndexes("repeats", true);

        MongoDBManager mongoDBManager = new MongoDBManager(cellBaseConfiguration);
        MongoDataStore mongoDataStore = mongoDBManager.createMongoDBDatastore("hsapiens", "grch37");
        MongoDBCollection mongoDBCollection = mongoDataStore.getCollection("repeats");
        DataResult<Document> index = mongoDBCollection.getIndex();
        assertNotNull(index);

        DataResult<Document> expected = new DataResult<>();
        expected.setNumResults(15);

        String expectedToString = "[Document{{v=2, key=Document{{_id=1}}, name=_id_, ns=cellbase_hsapiens_grch37_v4.repeats}}, Document{{v=2, key=Document{{_chunkIds=1}}, name=_chunkIds_1, ns=cellbase_hsapiens_grch37_v4.repeats, background=true}}, Document{{v=2, key=Document{{chromosome=1, start=1, end=1}}, name=chromosome_1_start_1_end_1, ns=cellbase_hsapiens_grch37_v4.repeats, background=true}}]";

        assertEquals(expectedToString, index.getResults().toString());
    }


}
