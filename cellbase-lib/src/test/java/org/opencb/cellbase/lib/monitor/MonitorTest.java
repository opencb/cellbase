package org.opencb.cellbase.lib.monitor;

import org.junit.Before;
import org.junit.Test;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.monitor.HealthStatus;
import org.opencb.cellbase.core.monitor.Monitor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Created by fjlopez on 12/10/17.
 */
public class MonitorTest extends GenericMongoDBAdaptorTest {

    private static final String REST_API_HOST = "http://localhost:8080/cellbase-4.6.0-SNAPSHOT";
//    private static final String REST_API_HOST = "http://bioinfo.hpc.cam.ac.uk/cellbase";
    private static final String SPECIES = "hsapiens";
    private static final String ASSEMBLY = "GRCh37";
    private static final String UNKNOWN_HTTP_HOST = "http://foo:8080/cellbase-4.6.0-SNAPSHOT";
    private static final String REST_API_DOES_NOT_PROVIDE_SERVICE_DETAILS = "http://bioinfo.hpc.cam.ac.uk/hgva";
    private static final String FAKE = "fake";

    public MonitorTest() throws IOException {
        super();
    }

    @Test
    public void run() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene");

        Monitor monitor = new Monitor(REST_API_HOST, dbAdaptorFactory);
        HealthStatus health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.OK, health.getService().getStatus());

        // Unknown http host
        monitor = new Monitor(UNKNOWN_HTTP_HOST, dbAdaptorFactory);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.DOWN, health.getService().getStatus());

        // Known http host but service_details end point not available
        monitor = new Monitor(REST_API_DOES_NOT_PROVIDE_SERVICE_DETAILS, dbAdaptorFactory);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.DOWN, health.getService().getStatus());

        // Empty gene collection
        clearDB(GRCH37_DBNAME);
        monitor = new Monitor(REST_API_HOST, dbAdaptorFactory);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.DOWN, health.getService().getStatus());

        // Unknown mongo host
        // Create new CellBase configuration with an unknown host
        CellBaseConfiguration cellBaseConfiguration = CellBaseConfiguration
                .load(MonitorTest.class.getClassLoader().getResourceAsStream("configuration.test.json"));
        cellBaseConfiguration.getDatabases().getMongodb().setHost(FAKE);
        DBAdaptorFactory localDBAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        monitor = new Monitor(REST_API_HOST, localDBAdaptorFactory);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.DOWN, health.getService().getStatus());
    }


}