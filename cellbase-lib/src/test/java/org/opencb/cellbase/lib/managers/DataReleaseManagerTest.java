package org.opencb.cellbase.lib.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataReleaseManagerTest extends GenericMongoDBAdaptorTest {

    public DataReleaseManagerTest() throws IOException {
        super();
    }

    @BeforeEach
    public void setUp() throws Exception {
        clearDB(CELLBASE_DBNAME);
//        Path path = Paths.get(getClass().getResource("/gene/gene-test.json.gz").toURI());
//        loadRunner.load(path, "gene");
    }

    @Test
    @Disabled
    public void test1() throws JsonProcessingException, CellBaseException {
        DataReleaseManager dataReleaseManager = new DataReleaseManager(CELLBASE_DBNAME, cellBaseConfiguration);

        DataRelease firstRelease = dataReleaseManager.createRelease();
        assertEquals(firstRelease.getRelease(), 1);

        DataRelease secondRelease = dataReleaseManager.createRelease();
        assertEquals(secondRelease.getRelease(), 2);
    }

}