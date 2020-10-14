package org.opencb.cellbase.core.monitor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.Databases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

/**
 * Created by fjlopez on 20/09/17.
 */
public class Monitor {

    private static final String CELLBASE = "CellBase";
    private static final String REPLICA_SET = "replica_set";
    private static final String COMPONENT = "Database";

    private static final String CELLBASE_TOKEN = "cellbase-health-token";
    private static ObjectMapper jsonObjectMapper;
    private static Logger logger;

    private WebTarget webTarget = null;
    private DBAdaptorFactory dbAdaptorFactory = null;

    static {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        logger = LoggerFactory.getLogger(Monitor.class);
    }

    public Monitor(DBAdaptorFactory dbAdaptorFactory) {
        this.dbAdaptorFactory = dbAdaptorFactory;
    }

    public Monitor(String restHost) {
        Client client = ClientBuilder.newClient();
        this.webTarget = client.target(URI.create(restHost));
    }

    public HealthCheckResponse run(String requestUri, CellBaseConfiguration configuration, String species,
                                   String assembly, String token) {
        HealthCheckResponse healthCheckResponse = new HealthCheckResponse();
        healthCheckResponse.setServiceName(CELLBASE);
        healthCheckResponse.setDatetime();
        healthCheckResponse.setComponents(Collections.singletonList(COMPONENT));
        HealthCheckResponse.Status mongoStatus = checkMongoStatus(species, assembly);
        healthCheckResponse.setStatus(mongoStatus);
        healthCheckResponse.setRequestUrl(requestUri);

        // only return info if token set
        if (CELLBASE_TOKEN.equals(token)) {
            Databases database = configuration.getDatabases();
            HealthCheckDependency mongoDependency = new HealthCheckDependency(
                    database.getMongodb().getHost(), mongoStatus, COMPONENT, "MongoDB", null);
            HealthCheckDependencies healthCheckDependencies = new HealthCheckDependencies(Collections.singletonList(mongoDependency), null);
            healthCheckResponse.setDependencies(healthCheckDependencies);
        }

        return healthCheckResponse;
    }

    private HealthCheckResponse.Status checkMongoStatus(String species, String assembly) {
        Map<String, DatastoreStatus> datastoreStatusMap = dbAdaptorFactory.getDatabaseStatus(species, assembly);

        if (datastoreStatusMap != null && datastoreStatusMap.size() > 0) {
            int downServers = 0;
            for (String datastoreDependencyName : datastoreStatusMap.keySet()) {
                if (datastoreStatusMap.get(datastoreDependencyName).getResponseTime() == null) {
                    downServers++;
                    // entry with role "replica_set" represents the overall database and its response time is measured
                    // by a direct query over one collection. If this response time is not there, the database is down
                    if (REPLICA_SET.equals(datastoreStatusMap.get(datastoreDependencyName).getRole())) {
                        return HealthCheckResponse.Status.DOWN;
                    }
                }
            }
            if (downServers == 0) {
                return HealthCheckResponse.Status.OK;
                // If the number of servers not responding is lower than the number of dependencies it's probably a
                // repl set in which one or more machines are down, but not all of them
            } else if (downServers < datastoreStatusMap.size()) {
                return HealthCheckResponse.Status.DEGRADED;
            } else {
                return HealthCheckResponse.Status.DOWN;
            }
        } else {
            return HealthCheckResponse.Status.DOWN;
        }
    }

}
