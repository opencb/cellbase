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

package org.opencb.cellbase.lib.impl;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;


import java.io.IOException;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MongoDBAdaptorFactoryTest extends GenericMongoDBAdaptorTest {

    private static CellBaseConfiguration cellBaseConfiguration;
    private static MongoDBAdaptorFactory mongoDBAdaptorFactory;


    public MongoDBAdaptorFactoryTest() throws IOException {
        super();
    }

    @BeforeAll
    static void setUp() throws Exception {
        cellBaseConfiguration = CellBaseConfiguration.load(
                MongoDBAdaptorFactoryTest.class.getClassLoader().getResourceAsStream("configuration.test.json"),
                CellBaseConfiguration.ConfigurationFileFormat.YAML);

        mongoDBAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
    }


    @Test
    public void testGetDatabaseName() throws Exception {
        String databaseName = mongoDBAdaptorFactory.getDatabaseName("speciesName", "assemblyName");

        assertEquals("speciesName_assemblyname", databaseName);
        assertTrue(false);
    }


}