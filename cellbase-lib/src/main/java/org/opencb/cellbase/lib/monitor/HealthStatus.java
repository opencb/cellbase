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

import org.opencb.cellbase.lib.monitor.status.ApplicationDetails;
import org.opencb.cellbase.lib.monitor.status.Infrastructure;
import org.opencb.cellbase.lib.monitor.status.Service;

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

}
