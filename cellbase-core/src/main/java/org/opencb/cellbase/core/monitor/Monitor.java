package org.opencb.cellbase.core.monitor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.common.GitRepositoryState;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by fjlopez on 20/09/17.
 */
public class Monitor {

    private static final String CELLBASE = "CellBase";
    private static final String NONE = "None";
    private static final String WEBSERVICES = "webservices";
    private static final String REST = "rest";
    private static final String VERSION = "v4";  // TODO: CAREFUL hardcoded to v4
    private static final String META = "meta";
    private static final String STATUS = "status";
    private static final String CELLBASE_TOMCAT = "CellBase-tomcat";
    private static final String REPLICA_SET = "replica_set";
    private static final String ASSEMBLY = "assembly";
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

    public HealthStatus run(String species, String assembly) {
        // Run "local" monitoring
        if (dbAdaptorFactory != null) {
            return runLocalMonitoring(species, assembly);
        // Run monitoring in remote machine
        } else {
            return runRemoteMonitoring(species, assembly);
        }
    }

    private HealthStatus runRemoteMonitoring(String species, String assembly) {
        WebTarget callUrl = webTarget.path(WEBSERVICES)
                .path(REST)
                .path(VERSION)  // TODO: CAREFUL hardcoded to v4
                .path(META)
                .path(species)
                .path(STATUS)
                .queryParam(ASSEMBLY, assembly);
        try {
            String jsonString = callUrl.request().get(String.class);
            List<HealthStatus> healthStatusList
                    = parseResult(jsonString, HealthStatus.class).getResponse().get(0).getResult();
            // Old CellBase WS servers may return an empty result if they don't recognize the endpoint
            if (!healthStatusList.isEmpty()) {
                return healthStatusList.get(0);
            } else {
                return (new HealthStatus()).setService((new HealthStatus.Service()).setStatus(HealthStatus.ServiceStatus.DOWN));
            }
            // IOException controls response parsing exceptions, at least
            // ProcessingException controls unknown host exceptions (cannot find specified host), at least
            // NotFoundException controls that remote WS API provides meta/service_details method
        } catch (IOException | ProcessingException | NotFoundException e) {
            e.printStackTrace();
            return (new HealthStatus()).setService((new HealthStatus.Service()).setStatus(HealthStatus.ServiceStatus.DOWN));
        }
    }

    private HealthStatus runLocalMonitoring(String species, String assembly) {
        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setApplication(getApplicationDetails(species, assembly));
        healthStatus.setInfrastructure(new HealthStatus.Infrastructure(1, NONE));
        healthStatus.setService(getService(healthStatus.getApplication()));

        return healthStatus;
    }

    private HealthStatus.Service getService(HealthStatus.ApplicationDetails applicationDetails) {
        HealthStatus.ApplicationDetails.DependenciesStatus dependencies = applicationDetails.getDependencies();
        HealthStatus.Service service = new HealthStatus.Service();
        service.setName(CELLBASE)
               .setApplicationTier(CELLBASE_TOMCAT);
        service.setStatus(getOverallServiceStatus(dependencies));

        return service;
    }

    private HealthStatus.ServiceStatus getOverallServiceStatus(HealthStatus.ApplicationDetails.DependenciesStatus dependencies) {
        // If maintenance file exists rest of checks are overridden
        if (Files.exists(Paths.get(dbAdaptorFactory.getCellBaseConfiguration().getMaintenanceFlagFile()))) {
            return HealthStatus.ServiceStatus.MAINTENANCE;
        } else {
            Map<String, HealthStatus.ApplicationDetails.DependenciesStatus.DatastoreDependenciesStatus.DatastoreStatus> datastoreStatusMap
                    = dependencies.getDatastores().getMongodb();
            if (datastoreStatusMap != null && datastoreStatusMap.size() > 0) {
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
    }

    private HealthStatus.ApplicationDetails.DependenciesStatus getDependenciesStatus(String species, String assembly) {
        HealthStatus.ApplicationDetails.DependenciesStatus.DatastoreDependenciesStatus datastores
                = new HealthStatus.ApplicationDetails.DependenciesStatus.DatastoreDependenciesStatus();
        datastores.setMongodb(dbAdaptorFactory.getDatabaseStatus(species, assembly));
        HealthStatus.ApplicationDetails.DependenciesStatus dependenciesStatus = new HealthStatus.ApplicationDetails.DependenciesStatus();
        dependenciesStatus.setDatastores(datastores);

        return dependenciesStatus;
    }

    private HealthStatus.ApplicationDetails getApplicationDetails(String species, String assembly) {
        HealthStatus.ApplicationDetails applicationDetails = new HealthStatus.ApplicationDetails();
        applicationDetails.setMaintainer(dbAdaptorFactory.getCellBaseConfiguration().getMaintainerContact());
        applicationDetails.setServer(getServerName());
        applicationDetails.setStarted(new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date(ManagementFactory.getRuntimeMXBean().getStartTime())));
        applicationDetails.setUptime(TimeUnit.MILLISECONDS.toMinutes(ManagementFactory.getRuntimeMXBean().getUptime())
                + " minutes");
        applicationDetails.setVersion(
                new HealthStatus.ApplicationDetails.Version(GitRepositoryState.get().getBuildVersion(),
                        GitRepositoryState.get().getCommitId().substring(0, 8)));
        applicationDetails.setDependencies(getDependenciesStatus(species, assembly));

        return applicationDetails;
    }

    private String getServerName() {
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (UnknownHostException ex) {
            logger.warn("Hostname can not be resolved");
            return null;
        }
    }

    private <U> QueryResponse<U> parseResult(String json, Class<U> clazz) throws IOException {
        ObjectReader reader = jsonObjectMapper
                .readerFor(jsonObjectMapper.getTypeFactory().constructParametrizedType(QueryResponse.class, QueryResult.class, clazz));
        return reader.readValue(json);
    }

}
