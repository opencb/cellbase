package org.opencb.cellbase.mongodb.loader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.cellbase.core.loader.LoadRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LoadRunnerTest {

    LoadRunner cosmicLoadRunner;

    @Before
    public void setUp() throws Exception {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug");
        Path jsonFile = Paths.get(LoadRunnerTest.class.getResource("/cosmic.json.gz").toURI());
        // connection params
        Map<String, String> params = new HashMap<>();
//        params.put(CellBaseLoader.CELLBASE_HOST, "localhost");
//        params.put(CellBaseLoader.CELLBASE_PORT, "27017");
//        params.put(CellBaseLoader.CELLBASE_DATABASE_NAME_PROPERTY, "cellbaseTest");

//        cosmicLoadRunner = new LoadRunner(jsonFile, "cosmic", 4, params);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRun() throws Exception {
//        cosmicLoadRunner.load();
    }
}