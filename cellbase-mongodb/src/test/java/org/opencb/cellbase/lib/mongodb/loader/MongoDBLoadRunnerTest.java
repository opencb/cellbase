package org.opencb.cellbase.lib.mongodb.loader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class MongoDBLoadRunnerTest {

    Path jsonFile;

    @Before
    public void setUp() throws Exception {
        jsonFile = Paths.get(MongoDBLoadRunnerTest.class.getResource("/cosmic.json.gz").toURI());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRun() throws Exception {
        MongoDBLoadRunner cosmicLoadRunner = new MongoDBLoadRunner(jsonFile, "cosmic", 2);
        cosmicLoadRunner.run();
    }
}