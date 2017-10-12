package org.opencb.cellbase.lib.monitor;

import org.junit.Before;
import org.junit.Test;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
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
    private MongoDBAdaptorFactory dbAdaptorFactory;

    @Before
    public void setUp() throws IOException {
        CellBaseConfiguration cellBaseConfiguration = CellBaseConfiguration
                .load(MonitorTest.class.getClassLoader().getResourceAsStream("configuration.test.json"));
        dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
    }

    @Test
    public void run() throws Exception {
        Monitor monitor = new Monitor(REST_API_HOST, dbAdaptorFactory);
        HealthStatus health = monitor.run(SPECIES, ASSEMBLY);
        assertEquals(HealthStatus.ServiceStatus.OK, health.getService().getStatus());

    }


}