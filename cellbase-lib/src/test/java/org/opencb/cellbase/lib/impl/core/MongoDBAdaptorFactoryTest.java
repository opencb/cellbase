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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.db.MongoDBManager;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;
import static org.opencb.cellbase.lib.db.MongoDBManager.DBNAME_SEPARATOR;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MongoDBAdaptorFactoryTest extends GenericMongoDBAdaptorTest {

    private MongoDBManager mongoDBManager;

    public MongoDBAdaptorFactoryTest() {
        super();
        this.mongoDBManager = new MongoDBManager(cellBaseConfiguration);
    }

    @Test
    public void testGetDatabaseName() {
        String auxVersion = cellBaseConfiguration.getVersion().replace(".", DBNAME_SEPARATOR).replace("-", DBNAME_SEPARATOR);
        String[] split = auxVersion.split(DBNAME_SEPARATOR);
        String version = split[0];
        if (split.length > 1) {
            version += (DBNAME_SEPARATOR + split[1]);
        }

        // provide assembly
        String databaseName = mongoDBManager.getDatabaseName("speciesName", "assemblyName", cellBaseConfiguration.getVersion());
        assertEquals("cellbase_speciesname_assemblyname_" + version, databaseName);

        // don't provide assembly
        InvalidParameterException thrown =
                assertThrows(InvalidParameterException.class,
                        () -> mongoDBManager.getDatabaseName("speciesName", null, cellBaseConfiguration.getVersion()),
                        "Expected getDatabaseName() to throw an exception, but it didn't");
        assertTrue(thrown.getMessage().contains("Species and assembly are required"));

        // handle special characters
        databaseName = mongoDBManager.getDatabaseName("speciesName", "my_funny.assembly--name", cellBaseConfiguration.getVersion());
        assertEquals("cellbase_speciesname_myfunnyassemblyname_" + version, databaseName);
    }
}