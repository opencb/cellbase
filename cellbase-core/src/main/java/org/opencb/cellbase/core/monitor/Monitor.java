package org.opencb.cellbase.core.monitor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Created by fjlopez on 20/09/17.
 */
public class Monitor {

    private static final String CELLBASE = "CellBase";
    private static final String CELLBASE_GEL_ZONE = "cellbase.gel.zone";
    private static final String NONE = "None";
    private static final String WEBSERVICES = "webservices";
    private static final String REST = "rest";
    private static final String VERSION = "v4";  // TODO: CAREFUL hardcoded to v4
    private static final String META = "meta";
    private static final String SERVICE_DETAILS = "service_details";
    private static final String CELLBASE_TOMCAT = "CellBase-tomcat";
    private static final String REPLICA_SET = "replica_set";
    private static ObjectMapper jsonObjectMapper;

    private WebTarget webTarget;

    private final DBAdaptorFactory dbAdaptorFactory;

    static {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Monitor(String host, DBAdaptorFactory dbAdaptorFactory) {
        Client client = ClientBuilder.newClient();
        this.webTarget = client.target(URI.create(host));
        this.dbAdaptorFactory = dbAdaptorFactory;
    }

    public HealthStatus run(String species, String assembly) {
        HealthStatus healthStatus = new HealthStatus();

        healthStatus.setApplication(getApplicationDetails());
        healthStatus.getApplication().setDependencies(getDependenciesStatus(species, assembly));
        healthStatus.setInfrastructure(new HealthStatus.Infrastructure(1, NONE));
        healthStatus.setService(getService(healthStatus.getApplication()));

        return healthStatus;
    }

    private HealthStatus.Service getService(HealthStatus.ApplicationDetails applicationDetails) {
        HealthStatus.ApplicationDetails.DependenciesStatus dependencies = applicationDetails.getDependencies();
        HealthStatus.Service service = new HealthStatus.Service();
        service.setName(CELLBASE)
               .setApplicationTier(CELLBASE_TOMCAT);

        // application details object provides just UP, MAINTENANCE or DOWN i.e. information about the status of the app
        // including if the maintenance file exists in the server, but does not check database status
        if (HealthStatus.ServiceStatus.OK.equals(applicationDetails.getServiceStatus())) {
            service.setStatus(getOverallServiceStatus(dependencies));
        } else {
            service.setStatus(applicationDetails.getServiceStatus());
        }
        return service;
    }

    private HealthStatus.ServiceStatus getOverallServiceStatus(HealthStatus.ApplicationDetails.DependenciesStatus dependencies) {
        Map<String, HealthStatus.ApplicationDetails.DependenciesStatus.DatastoreDependenciesStatus.DatastoreStatus> datastoreStatusMap
                = dependencies.getDatastores().getMongodb();

        if (datastoreStatusMap != null && datastoreStatusMap.size() == 0) {
            int downServers = 0;
            for (String datastoreDependencyName : datastoreStatusMap.keySet()) {
                if (datastoreStatusMap.get(datastoreDependencyName).getResponseTime() == null) {
                    downServers++;
                    // entry with role "replica_set" represents the overall database and its response time is measured
                    // by a direct query over one collection. If this response time is not there, the database is down
                    if (REPLICA_SET.equals(datastoreStatusMap.get(datastoreDependencyName).getRole())) {
                        return HealthStatus.ServiceStatus.DOWN;
                    }
                }
            }

            if (downServers == 0) {
                return HealthStatus.ServiceStatus.OK;
            // If the number of servers not responding is lower than the number of dependencies it's probably a
            // repl set in which one or more machines are down, but not all of them
            } else if (downServers < datastoreStatusMap.size()) {
                return HealthStatus.ServiceStatus.DEGRADED;
            } else {
                return HealthStatus.ServiceStatus.DOWN;
            }
        } else {
            return HealthStatus.ServiceStatus.DOWN;
        }
    }

    private HealthStatus.ApplicationDetails.DependenciesStatus getDependenciesStatus(String species, String assembly) {
        HealthStatus.ApplicationDetails.DependenciesStatus.DatastoreDependenciesStatus datastores
                = new HealthStatus.ApplicationDetails.DependenciesStatus.DatastoreDependenciesStatus();
        datastores.setMongodb(dbAdaptorFactory.getDatabaseStatus(species, assembly));
        HealthStatus.ApplicationDetails.DependenciesStatus dependenciesStatus = new HealthStatus.ApplicationDetails.DependenciesStatus();
        dependenciesStatus.setDatastores(datastores);

        return dependenciesStatus;
    }

    private HealthStatus.ApplicationDetails getApplicationDetails() {
        WebTarget callUrl = webTarget.path(WEBSERVICES)
                                    .path(REST)
                                    .path(VERSION)  // TODO: CAREFUL hardcoded to v4
                                    .path(META)
                                    .path(SERVICE_DETAILS);

        String jsonString = callUrl.request().get(String.class);

        try {
            return parseResult(jsonString, HealthStatus.ApplicationDetails.class).getResponse().get(0).getResult().get(0);
        } catch (IOException e) {
            e.printStackTrace();
            HealthStatus.ApplicationDetails applicationDetails = new HealthStatus.ApplicationDetails();
            return applicationDetails.setServiceStatus(HealthStatus.ServiceStatus.DOWN);
        }
    }

    private <U> QueryResponse<U> parseResult(String json, Class<U> clazz) throws IOException {
        ObjectReader reader = jsonObjectMapper
                .readerFor(jsonObjectMapper.getTypeFactory().constructParametrizedType(QueryResponse.class, QueryResult.class, clazz));
        return reader.readValue(json);
    }

}
