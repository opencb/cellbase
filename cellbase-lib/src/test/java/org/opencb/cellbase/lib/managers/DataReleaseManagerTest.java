package org.opencb.cellbase.lib.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;

import java.io.IOException;

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
    public void test1() throws JsonProcessingException, CellBaseException {
        DataReleaseManager dataReleaseManager = new DataReleaseManager(CELLBASE_DBNAME, cellBaseConfiguration);
        DataRelease firstRelease = dataReleaseManager.createRelease();
        if (firstRelease != null) {
            dataReleaseManager.active(firstRelease.getRelease());
        }
        DataRelease secondRelease = dataReleaseManager.createRelease();
        if (secondRelease != null) {
            dataReleaseManager.active(secondRelease.getRelease());
        }
    }

}