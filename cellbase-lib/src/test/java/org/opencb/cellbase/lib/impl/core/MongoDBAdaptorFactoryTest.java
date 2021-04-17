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

package org.opencb.cellbase.lib.impl.core;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.db.MongoDBManager;

import java.io.IOException;
import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;

public class MongoDBAdaptorFactoryTest extends GenericMongoDBAdaptorTest {

    private static MongoDBManager mongoDBManager;
    private static CellBaseConfiguration cellBaseConfiguration;


    public MongoDBAdaptorFactoryTest() throws IOException {
        super();
    }

    @BeforeAll
    public static void setUp() throws Exception {
        cellBaseConfiguration = CellBaseConfiguration.load(
                MongoDBAdaptorFactoryTest.class.getClassLoader().getResourceAsStream("configuration.test.yaml"),
                CellBaseConfiguration.ConfigurationFileFormat.YAML);

        mongoDBManager = new MongoDBManager(cellBaseConfiguration);
    }

    @Test
    public void testGetDatabaseName() throws Exception {
        // provide assembly
        String databaseName = mongoDBManager.getDatabaseName("speciesName", "assemblyName");
        assertEquals("cellbase_speciesname_assemblyname_v4", databaseName);

        // don't provide assembly
        InvalidParameterException thrown =
                assertThrows(InvalidParameterException.class,
                        () -> mongoDBManager.getDatabaseName("speciesName", null),
                        "Expected getDatabaseName() to throw an exception, but it didn't");

        assertTrue(thrown.getMessage().contains("Assembly is required"));

        // handle special characters
        databaseName = mongoDBManager.getDatabaseName("speciesName", "my_funny.assembly--name");
        assertEquals("cellbase_speciesname_myfunnyassemblyname_v4", databaseName);
    }
}