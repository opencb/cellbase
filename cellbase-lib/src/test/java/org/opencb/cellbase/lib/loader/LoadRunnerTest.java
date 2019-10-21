/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.lib.loader;

import org.junit.After;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.loader.LoadRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Deprecated
@Disabled
public class LoadRunnerTest {

    LoadRunner cosmicLoadRunner;

    @BeforeAll
    public void setUp() throws Exception {
//        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug");
        Path jsonFile = Paths.get(LoadRunnerTest.class.getResource("/cosmic.json.gz").toURI());
        // connection params
        Map<String, String> params = new HashMap<>();
//        params.put(CellBaseLoader.CELLBASE_HOST, "localhost");
//        params.put(CellBaseLoader.CELLBASE_PORT, "27017");
//        params.put(CellBaseLoader.CELLBASE_DATABASE_NAME_PROPERTY, "cellbaseTest");

//        cosmicLoadRunner = new LoadRunner(jsonFile, "cosmic", 4, params);
    }

    @AfterAll
    public void tearDown() throws Exception {

    }

    @Test
    public void testRun() throws Exception {
//        cosmicLoadRunner.load();
    }
}