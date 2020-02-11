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

package org.opencb.cellbase.lib.monitor;

import com.google.common.io.Files;

;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.monitor.HealthStatus;
import org.opencb.cellbase.core.monitor.Monitor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by fjlopez on 12/10/17.
 */
public class MonitorTest extends GenericMongoDBAdaptorTest {

    private static final String REST_API_HOST = "http://localhost:8080/cellbase";
//    private static final String REST_API_HOST = "https://bioinfo.hpc.cam.ac.uk/cellbase";
    private static final String SPECIES = "hsapiens";
    private static final String ASSEMBLY = "GRCh37";
    private static final String UNKNOWN_HTTP_HOST = "http://foo:8080/cellbase";
    private static final String REST_API_DOES_NOT_IMPLEMENT_STATUS = "https://bioinfo.hpc.cam.ac.uk/hgva";
    private static final String FAKE = "fake";

    public MonitorTest() throws IOException {
        super();
    }

    @Disabled
    @Test
    public void run() throws Exception {

        // "Local" monitoring all OK
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene");
        CellBaseConfiguration cellBaseConfiguration = CellBaseConfiguration.load(
                MonitorTest.class.getClassLoader().getResourceAsStream("configuration.test.json"),
                CellBaseConfiguration.ConfigurationFileFormat.JSON);
        MongoDBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        Monitor monitor = new Monitor(dbAdaptorFactory);
        HealthStatus health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.OK, health.getService().getStatus());

        // Empty gene collection
        clearDB(GRCH37_DBNAME);
        monitor = new Monitor(dbAdaptorFactory);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.DOWN, health.getService().getStatus());

        // "Local" monitoring - maintenance
        // Touch maintenance file
        File maintenanceFile = new File(dbAdaptorFactory.getCellBaseConfiguration().getMaintenanceFlagFile());
        Files.touch(maintenanceFile);
        monitor = new Monitor(dbAdaptorFactory);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.MAINTENANCE, health.getService().getStatus());
        // Remove maintenance file
        maintenanceFile.delete();

        // "Local" monitoring - unknown mongo host
        cellBaseConfiguration.getDatabases().getMongodb().setHost(FAKE);
        dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        monitor = new Monitor(dbAdaptorFactory);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.DOWN, health.getService().getStatus());

        // Remote monitoring all OK
        monitor = new Monitor(REST_API_HOST);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.OK, health.getService().getStatus());

        // Remote monitoring - unknown http host
        monitor = new Monitor(UNKNOWN_HTTP_HOST);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.DOWN, health.getService().getStatus());

        // Remote monitoring - known http host but status end point not available
        monitor = new Monitor(REST_API_DOES_NOT_IMPLEMENT_STATUS);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.DOWN, health.getService().getStatus());
    }


}