package org.opencb.cellbase.mongodb.loader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.cellbase.core.loader.LoadRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LoadRunnerTest {

    Path jsonFile;

    @Before
    public void setUp() throws Exception {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug");
        jsonFile = Paths.get(LoadRunnerTest.class.getResource("/cosmic.json.gz").toURI());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRun() throws Exception {
        LoadRunner cosmicLoadRunner = new LoadRunner(jsonFile, "cosmic", 4);
        cosmicLoadRunner.run();
    }
}