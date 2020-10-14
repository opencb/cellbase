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

import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.Databases;
import org.opencb.cellbase.lib.managers.MetaManager;
import org.opencb.commons.monitor.DatastoreStatus;
import org.opencb.commons.monitor.HealthCheckDependencies;
import org.opencb.commons.monitor.HealthCheckDependency;
import org.opencb.commons.monitor.HealthCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

public class Monitor {

    private static final String CELLBASE = "CellBase";
    private static final String REPLICA_SET = "replica_set";
    private static final String COMPONENT = "Database";
    private static final String CELLBASE_TOKEN = "cellbase-health-token";
    private static Logger logger;
    private MetaManager metaManager = null;

    static {
        logger = LoggerFactory.getLogger(Monitor.class);
    }

    public Monitor() {

    }

    public Monitor(MetaManager metaManager) {
        this.metaManager = metaManager;
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
        Map<String, DatastoreStatus> datastoreStatusMap = metaManager.getDatabaseStatus(species, assembly);

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
