package org.opencb.cellbase.lib.impl.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.Gene;
import org.opencb.cellbase.core.api.GeneQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.db.MongoDBManager;
import org.opencb.cellbase.lib.impl.core.singleton.DataReleaseSingleton;
import org.opencb.cellbase.lib.managers.GeneManager;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.opencb.cellbase.core.ParamConstants.DATA_RELEASE_PARAM;

class DataReleaseSingletonTest extends GenericMongoDBAdaptorTest {

    private String dbname;

    @BeforeEach
    public void init() throws CellBaseException {
        dbname = MongoDBManager.getDatabaseName(SPECIES, ASSEMBLY, cellBaseConfiguration.getVersion());
    }

    @Test
    public void testChangeCollectionMap() throws CellBaseException, QueryException, IllegalAccessException, JsonProcessingException {
        MongoDBManager mongoDBManager = new MongoDBManager(cellBaseConfiguration);
        MongoDataStore mongoDatastore = mongoDBManager.createMongoDBDatastore(SPECIES, ASSEMBLY);

        GeneManager geneManager = cellBaseManagerFactory.getGeneManager(SPECIES, ASSEMBLY);

//        DataReleaseManager dataReleaseManager = new DataReleaseManager(SPECIES, ASSEMBLY, cellBaseConfiguration);
//        DataRelease newDataRelease = dataReleaseManager.createRelease();
//        assertEquals(5, newDataRelease.getRelease());

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("id", "ENSG00000248746");
        paramMap.put("include", "id,name,start,end");
        paramMap.put(DATA_RELEASE_PARAM, "1");

        GeneQuery geneQuery = new GeneQuery(paramMap);
        geneQuery.setCount(Boolean.TRUE);

        CellBaseDataResult<Gene> cellBaseDataResult = geneManager.search(geneQuery);
        // WARNING: these values below may slightly change from one data version to another
        assertEquals(1, cellBaseDataResult.getNumMatches());
        assertThat(cellBaseDataResult.getResults().stream().map(gene -> gene.getName()).collect(Collectors.toList()),
                CoreMatchers.hasItems("ACTN3"));
    }

    @Test
    public void testDataReleaseSingleton() throws CellBaseException {
        MongoDBCollection collection = DataReleaseSingleton.getInstance().getMongoDBCollection(dbname, "gene", 1);
        assertTrue(collection != null);
    }

    @Test
    public void testCheckDatabaseFail() throws CellBaseException {
        CellBaseException exception = assertThrows(CellBaseException.class, () -> DataReleaseSingleton.getInstance()
                .checkDataRelease("toto", 10));
        assertTrue(exception.getMessage().startsWith(DataReleaseSingleton.UNKOWN_DATABASE_MSG_PREFIX));
    }

    @Test
    public void testCheckReleaseFail() throws CellBaseException {
        CellBaseException exception = assertThrows(CellBaseException.class, () -> DataReleaseSingleton.getInstance()
                .checkDataRelease(dbname, 10));
        assertTrue(exception.getMessage().startsWith(DataReleaseSingleton.INVALID_RELEASE_MSG_PREFIX));
    }

    @Test
    public void testCheckReleasePass() throws CellBaseException {
        DataReleaseSingleton.getInstance().checkDataRelease(dbname, 1);
    }

    @Test
    public void testCheckDataFail() throws CellBaseException {
        CellBaseException exception = assertThrows(CellBaseException.class, () -> DataReleaseSingleton.getInstance()
                .checkDataRelease(dbname, 1, "toto"));
        assertTrue(exception.getMessage().startsWith(DataReleaseSingleton.INVALID_DATA_MSG_PREFIX));
    }

    @Test
    public void testCheckDataPass() throws CellBaseException {
        DataReleaseSingleton.getInstance().checkDataRelease(dbname, 1, "gene");
    }
}