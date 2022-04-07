package org.opencb.cellbase.lib.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.release.DataRelease;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

class ReleaseManagerTest extends GenericMongoDBAdaptorTest {

    public ReleaseManagerTest() throws IOException {
        super();
    }

    @BeforeEach
    public void setUp() throws Exception {
        clearDB(GRCH37_DBNAME);
//        Path path = Paths.get(getClass().getResource("/gene/gene-test.json.gz").toURI());
//        loadRunner.load(path, "gene");
    }

    @Test
    public void test1() throws JsonProcessingException, CellBaseException {
        ReleaseManager releaseManager = new ReleaseManager("cellbase_hsapiens_grch37_v4", cellBaseConfiguration);
        DataRelease firstRelease = releaseManager.createRelease();
        if (firstRelease != null) {
            releaseManager.activeByDefault(firstRelease.getRelease());
        }
        DataRelease secondRelease = releaseManager.createRelease();
        if (secondRelease != null) {
            releaseManager.activeByDefault(secondRelease.getRelease());
        }
    }

}