package org.opencb.cellbase.lib.indexer;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.Gene;
import org.opencb.cellbase.core.api.GeneQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.db.MongoDBManager;
import org.opencb.cellbase.lib.impl.core.CellBaseDBAdaptor;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.cellbase.lib.managers.GeneManager;
import org.opencb.cellbase.lib.managers.ReleaseManager;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IndexManagerTest extends GenericMongoDBAdaptorTest {

    private IndexManager indexManager;
    private ReleaseManager releaseManager;
    private String databaseName = "cellbase_hsapiens_grch37_v4";

    public IndexManagerTest() throws Exception {
        int release = 1;

        Path path = Paths.get(getClass().getResource("/index/mongodb-indexes.json").toURI());
        indexManager = new IndexManager(databaseName, path, cellBaseConfiguration);
        releaseManager = new ReleaseManager(databaseName, cellBaseConfiguration);

        clearDB(GRCH37_DBNAME);

        releaseManager.createRelease();
        path = Paths.get(getClass().getResource("/gene/gene-test.json.gz").toURI());
        loadRunner.load(path, "gene", release);
    }

    @Test
    public void testIndexes() throws IOException, CellBaseException, QueryException, IllegalAccessException {
        String collectionName = "gene" + CellBaseDBAdaptor.DATA_RELEASE_SEPARATOR + dataRelease;

        indexManager.createMongoDBIndexes(Collections.singletonList(collectionName), true);

        MongoDBManager mongoDBManager = new MongoDBManager(cellBaseConfiguration);
        MongoDataStore mongoDataStore = mongoDBManager.createMongoDBDatastore("hsapiens", "grch37");
        MongoDBCollection mongoDBCollection = mongoDataStore.getCollection(collectionName);
        DataResult<Document> index = mongoDBCollection.getIndex();
        assertNotNull(index);

        CellBaseManagerFactory factory = new CellBaseManagerFactory(cellBaseConfiguration);
        GeneManager geneManager = factory.getGeneManager("hsapiens", "grch37", dataRelease);
        GeneQuery query = new GeneQuery();
        query.setIds(Collections.singletonList("ENSG00000279457"));
        CellBaseDataResult<Gene> result = geneManager.search(query);
        assertEquals(1, result.getNumResults());
        assertEquals("ENSG00000279457", result.getResults().get(0).getId());
        assertEquals("WASH9P", result.getResults().get(0).getName());
    }
}
