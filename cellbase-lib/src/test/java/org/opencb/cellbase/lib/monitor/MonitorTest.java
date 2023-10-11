package org.opencb.cellbase.lib.monitor;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.SpeciesUtilsTest;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.commons.monitor.HealthCheckResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by fjlopez on 12/10/17.
 */
public class MonitorTest extends GenericMongoDBAdaptorTest {

    private static final String REST_API_HOST = "http://localhost:8080/cellbase";
    //    private static final String REST_API_HOST = "http://bioinfo.hpc.cam.ac.uk/cellbase";
    private static final String SPECIES = "hsapiens";
    private static final String ASSEMBLY = "GRCh37";
    private static final String UNKNOWN_HTTP_HOST = "http://foo:8080/cellbase";
    private static final String REST_API_DOES_NOT_IMPLEMENT_STATUS = "http://bioinfo.hpc.cam.ac.uk/hgva";
    private static final String FAKE = "fake";

    public MonitorTest() throws IOException {
        super();
    }

    @Test
    @Disabled
    public void run() throws Exception {

        // "Local" monitoring all OK
        Path path = Paths.get(getClass()
                .getResource("/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene");
        CellBaseConfiguration cellBaseConfiguration = CellBaseConfiguration.load(
                SpeciesUtilsTest.class.getClassLoader().getResourceAsStream("configuration.test.yaml"),
                CellBaseConfiguration.ConfigurationFileFormat.YAML);
//        MongoDBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);

        CellBaseManagerFactory cellBaseManagerFactory = new CellBaseManagerFactory(cellBaseConfiguration);
        Monitor monitor = new Monitor(cellBaseManagerFactory.getMetaManager());
        HealthCheckResponse health = monitor.run("localhost", cellBaseConfiguration, SPECIES, ASSEMBLY, null);
        assertEquals(HealthCheckResponse.Status.OK, health.getStatus());

        // Empty gene collection
        monitor = new Monitor(cellBaseManagerFactory.getMetaManager());
        health = monitor.run("localhost", cellBaseConfiguration, SPECIES, ASSEMBLY, null);
        assertEquals(HealthCheckResponse.Status.DOWN, health.getStatus());

//        // "Local" monitoring - maintenance
//        // Touch maintenance file
//        File maintenanceFile = new File(dbAdaptorFactory.getCellBaseConfiguration().getMaintenanceFlagFile());
//        Files.touch(maintenanceFile);
//        monitor = new Monitor(dbAdaptorFactory);
//        health = monitor.run(SPECIES, ASSEMBLY);
//        assertEquals(HealthCheckResponse.Status.MAINTENANCE, health.getService().getStatus());
//        // Remove maintenance file
//        maintenanceFile.delete();

        // "Local" monitoring - unknown mongo host
        cellBaseConfiguration.getDatabases().getMongodb().setHost(FAKE);
//        dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        monitor = new Monitor(cellBaseManagerFactory.getMetaManager());
        health = monitor.run("localhost", cellBaseConfiguration, SPECIES, ASSEMBLY, null);
        assertEquals(HealthCheckResponse.Status.DOWN, health.getStatus());

//        // Remote monitoring all OK
//        monitor = new Monitor(cellBaseManagerFactory.getMetaManager());
//        health = monitor.run("localhost", cellBaseConfiguration, SPECIES, ASSEMBLY, null);
//        assertEquals(HealthCheckResponse.Status.OK, health.getStatus());
//
//        // Remote monitoring - unknown http host
//        monitor = new Monitor(cellBaseManagerFactory.getMetaManager());
//        health = monitor.run("localhost", cellBaseConfiguration, SPECIES, ASSEMBLY, null);
//        assertEquals(HealthCheckResponse.Status.DOWN, health.getStatus());
//
//        // Remote monitoring - known http host but status end point not available
//        monitor = new Monitor(cellBaseManagerFactory.getMetaManager());
//        health = monitor.run("localhost", cellBaseConfiguration, SPECIES, ASSEMBLY, null);
//        assertEquals(HealthCheckResponse.Status.DOWN, health.getStatus());
    }
}