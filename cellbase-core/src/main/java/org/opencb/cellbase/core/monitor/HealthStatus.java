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

package org.opencb.cellbase.core.monitor;

import java.util.Map;

/**
 * Created by fjlopez on 20/09/17.
 */
public class HealthStatus {

    public enum ServiceStatus { OK, DEGRADED, DOWN, MAINTENANCE }

    private ApplicationDetails application;
    private Infrastructure infrastructure;
    private Service service;

    public HealthStatus() {
    }

    public ApplicationDetails getApplication() {
        return application;
    }

    public HealthStatus setApplication(ApplicationDetails application) {
        this.application = application;
        return this;
    }

    public Infrastructure getInfrastructure() {
        return infrastructure;
    }

    public HealthStatus setInfrastructure(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
        return this;
    }

    public Service getService() {
        return service;
    }

    public HealthStatus setService(Service service) {
        this.service = service;
        return this;
    }

    public static class ApplicationDetails {

        private String maintainer;
        private String server;
        private String started;
        private String uptime;
        private Version version;
        private DependenciesStatus dependencies;

        public ApplicationDetails() {
        }

        public String getMaintainer() {
            return maintainer;
        }

        public ApplicationDetails setMaintainer(String maintainer) {
            this.maintainer = maintainer;
            return this;
        }

        public String getServer() {
            return server;
        }

        public ApplicationDetails setServer(String server) {
            this.server = server;
            return this;
        }

        public String getStarted() {
            return started;
        }

        public ApplicationDetails setStarted(String started) {
            this.started = started;
            return this;
        }

        public String getUptime() {
            return uptime;
        }

        public ApplicationDetails setUptime(String uptime) {
            this.uptime = uptime;
            return this;
        }

        public Version getVersion() {
            return version;
        }

        public ApplicationDetails setVersion(Version version) {
            this.version = version;
            return this;
        }

        public DependenciesStatus getDependencies() {
            return dependencies;
        }

        public ApplicationDetails setDependencies(DependenciesStatus dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public static class Version {
            private String tagName;
            private String commit;

            public Version() {
            }

            public Version(String tagName, String commit) {
                this.tagName = tagName;
                this.commit = commit;
            }

            public String getTagName() {
                return tagName;
            }

            public void setTagName(String tagName) {
                this.tagName = tagName;
            }

            public String getCommit() {
                return commit;
            }

            public void setCommit(String commit) {
                this.commit = commit;
            }
        }
        public static class DependenciesStatus {

            private DatastoreDependenciesStatus datastores;

            public DependenciesStatus() {
            }

            public DatastoreDependenciesStatus getDatastores() {
                return datastores;
            }

            public void setDatastores(DatastoreDependenciesStatus datastores) {
                this.datastores = datastores;
            }

            public static class DatastoreDependenciesStatus {
                private Map<String, DatastoreStatus> mongodb;

                public DatastoreDependenciesStatus() {
                }

                public Map<String, DatastoreStatus> getMongodb() {
                    return mongodb;
                }

                public void setMongodb(Map<String, DatastoreStatus> mongodb) {
                    this.mongodb = mongodb;
                }

                public static class DatastoreStatus {
                    private String responseTime;
                    private String role;
                    private String repset;

                    public DatastoreStatus() {
                    }

                    public String getResponseTime() {
                        return responseTime;
                    }

                    public void setResponseTime(String responseTime) {
                        this.responseTime = responseTime;
                    }

                    public String getRole() {
                        return role;
                    }

                    public void setRole(String role) {
                        this.role = role;
                    }

                    public String getRepset() {
                        return repset;
                    }

                    public void setRepset(String repset) {
                        this.repset = repset;
                    }
                }
            }

        }
    }

    public static class Infrastructure {
        private int endpointVersion;
        private String serviceDiscovery;

        public Infrastructure() {
        }

        public Infrastructure(int endpointVersion, String serviceDiscovery) {
            this.endpointVersion = endpointVersion;
            this.serviceDiscovery = serviceDiscovery;
        }

        public int getEndpointVersion() {
            return endpointVersion;
        }

        public Infrastructure setEndpointVersion(int endpointVersion) {
            this.endpointVersion = endpointVersion;
            return this;
        }

        public String getServiceDiscovery() {
            return serviceDiscovery;
        }

        public Infrastructure setServiceDiscovery(String serviceDiscovery) {
            this.serviceDiscovery = serviceDiscovery;
            return this;
        }
    }

    public static class Service {
        private String name;
        private String applicationTier;
        private ServiceStatus status;

        public String getName() {
            return name;
        }

        public Service setName(String name) {
            this.name = name;
            return this;
        }

        public String getApplicationTier() {
            return applicationTier;
        }

        public Service setApplicationTier(String applicationTier) {
            this.applicationTier = applicationTier;
            return this;
        }

        public ServiceStatus getStatus() {
            return status;
        }

        public Service setStatus(ServiceStatus status) {
            this.status = status;
            return this;
        }
    }
}
