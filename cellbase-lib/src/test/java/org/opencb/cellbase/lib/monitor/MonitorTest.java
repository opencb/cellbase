package org.opencb.cellbase.lib.monitor;

import org.junit.Before;
import org.junit.Test;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.monitor.HealthStatus;
import org.opencb.cellbase.core.monitor.Monitor;
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by fjlopez on 12/10/17.
 */
public class MonitorTest {

//    private static final String REST_API_HOST = "http://bioinfo.hpc.cam.ac.uk/cellbase";
    private static final String REST_API_HOST = "http://localhost:8080/cellbase-4.6.0-SNAPSHOT";
    private static final String SPECIES = "hsapiens";
    private static final String ASSEMBLY = "GRCh37";
    private static final String UNKNOWN_HTTP_HOST = "http://foo:8080/cellbase-4.6.0-SNAPSHOT";
    private static final String REST_API_DOES_NOT_PROVIDE_SERVICE_DETAILS = "http://bioinfo.hpc.cam.ac.uk/hgva";
    private static final String FAKE = "fake";

    @Before
    public void setUp() throws IOException {
    }

    @Test
    public void run() throws Exception {
        CellBaseConfiguration cellBaseConfiguration = CellBaseConfiguration
                .load(MonitorTest.class.getClassLoader().getResourceAsStream("configuration.test.json"));
        MongoDBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);

        Monitor monitor = new Monitor(REST_API_HOST, dbAdaptorFactory);
        HealthStatus health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.OK, health.getService().getStatus());

        monitor = new Monitor(UNKNOWN_HTTP_HOST, dbAdaptorFactory);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.DOWN, health.getService().getStatus());

        monitor = new Monitor(REST_API_DOES_NOT_PROVIDE_SERVICE_DETAILS, dbAdaptorFactory);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.DOWN, health.getService().getStatus());

        cellBaseConfiguration.getDatabases().getMongodb().setHost(FAKE);
        dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        monitor = new Monitor(REST_API_HOST, dbAdaptorFactory);
        health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.DOWN, health.getService().getStatus());


    }


}