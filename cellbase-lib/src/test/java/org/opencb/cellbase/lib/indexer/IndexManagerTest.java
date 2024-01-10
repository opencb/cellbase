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
import org.opencb.cellbase.lib.managers.GeneManager;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IndexManagerTest extends GenericMongoDBAdaptorTest {

    private IndexManager indexManager;

    public IndexManagerTest() throws URISyntaxException {
        super();

        Path path = Paths.get(getClass().getResource("/index/mongodb-indexes.json").toURI());
        indexManager = new IndexManager(cellBaseName, path, cellBaseConfiguration);
    }

    @Test
    public void testIndexes() throws IOException, CellBaseException, QueryException, IllegalAccessException {
        String collectionName = "gene" + CellBaseDBAdaptor.DATA_RELEASE_SEPARATOR + dataRelease.getRelease();

        indexManager.createMongoDBIndexes(Collections.singletonList(collectionName), true);

        MongoDBManager mongoDBManager = new MongoDBManager(cellBaseConfiguration);
        MongoDataStore mongoDataStore = mongoDBManager.createMongoDBDatastore(SPECIES, ASSEMBLY);
        MongoDBCollection mongoDBCollection = mongoDataStore.getCollection(collectionName);
        DataResult<Document> index = mongoDBCollection.getIndex();
        assertNotNull(index);

        GeneManager geneManager = cellBaseManagerFactory.getGeneManager(SPECIES, ASSEMBLY);
        GeneQuery query = new GeneQuery();
        query.setNames(Collections.singletonList("BRCA1"));
        query.setDataRelease(dataRelease.getRelease());
        CellBaseDataResult<Gene> result = geneManager.search(query);
        assertEquals(1, result.getNumResults());
        assertEquals("BRCA1", result.getResults().get(0).getName());
        assertEquals("ENSG00000012048", result.getResults().get(0).getId());
    }
}
