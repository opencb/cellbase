package org.opencb.cellbase.core.monitor;

import java.util.List;

public class HealthCheckDependencies {
    private List<HealthCheckDependency> datastores;
    private List<HealthCheckDependency> apis;

    public HealthCheckDependencies(List<HealthCheckDependency> datastores, List<HealthCheckDependency> apis) {
        this.datastores = datastores;
        this.apis = apis;
    }

    public List<HealthCheckDependency> getDatastores() {
        return datastores;
    }

    public HealthCheckDependencies setDatastores(List<HealthCheckDependency> datastores) {
        this.datastores = datastores;
        return this;
    }

    public List<HealthCheckDependency> getApis() {
        return apis;
    }

    public HealthCheckDependencies setApis(List<HealthCheckDependency> apis) {
        this.apis = apis;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HealthCheckDependencies{");
        sb.append("datastores=").append(datastores);
        sb.append(", apis=").append(apis);
        sb.append('}');
        return sb.toString();
    }
}
