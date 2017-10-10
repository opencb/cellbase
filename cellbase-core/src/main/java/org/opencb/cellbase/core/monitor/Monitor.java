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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fjlopez on 20/09/17.
 */
public class Monitor {

    enum ServiceStatus { OK, DEGRADED, DOWN, MAINTENANCE }

    private static final String CELLBASE = "CellBase";
    private static final String CELLBASE_GEL_ZONE = "cellbase.gel.zone";
    private static final String NONE = "None";
    private static final String WEBSERVICES = "webservices";
    private static final String REST = "rest";
    private static final String VERSION = "v4";  // TODO: CAREFUL hardcoded to v4
    private static final String META = "meta";
    private static final String SERVICE_DETAILS = "service_details";
    private static ObjectMapper jsonObjectMapper;

    WebTarget webTarget;

    private final DBAdaptorFactory dbAdaptorFactory;

    private ServiceStatus serviceStatus = ServiceStatus.OK;

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
        healthStatus.setDependencies(getDependenciesStatus(species, assembly));
        healthStatus.setInfrastructure(new Infrastructure(1, NONE));
        healthStatus.setService(new ServiceStatus(CELLBASE, CELLBASE_GEL_ZONE, serviceStatus));

        return healthStatus;
    }

    private HealthStatus.DependenciesStatus getDependenciesStatus(String species, String assembly) {
        HealthStatus.DependenciesStatus.DatastoreDependenciesStatus datastores
                = new HealthStatus.DependenciesStatus.DatastoreDependenciesStatus();
        datastores.setMongodb(dbAdaptorFactory.getDatabaseStatus(species, assembly));
        HealthStatus.DependenciesStatus dependenciesStatus = new HealthStatus.DependenciesStatus();
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
            serviceStatus = ServiceStatus.DOWN;
        }

        return null;
    }

    private <U> QueryResponse<U> parseResult(String json, Class<U> clazz) throws IOException {
        ObjectReader reader = jsonObjectMapper
                .readerFor(jsonObjectMapper.getTypeFactory().constructParametrizedType(QueryResponse.class, QueryResult.class, clazz));
        return reader.readValue(json);
    }

}
