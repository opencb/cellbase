package org.opencb.cellbase.core.monitor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by fjlopez on 20/09/17.
 */
public class HealthCheckResponse {

    public enum Status { OK, DEGRADED, DOWN, NOT_CONFIGURED }

    private String serviceName;
    private String requestUrl;
    private String datetime;
    private HealthCheckDependencies dependencies;
    private Status status;
    private List<String> components;
    private List<String> unavailableComponents;


    public HealthCheckResponse() {
    }

    public String getServiceName() {
        return serviceName;
    }

    public HealthCheckResponse setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public HealthCheckResponse setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
        return this;
    }

    public HealthCheckResponse setDatetime() {
        this.datetime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        return this;
    }

    public String getDatetime() {
        return datetime;
    }

    public HealthCheckDependencies getDependencies() {
        return dependencies;
    }

    public HealthCheckResponse setDependencies(HealthCheckDependencies dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public HealthCheckResponse setStatus(Status status) {
        this.status = status;
        return this;
    }

    public List<String> getComponents() {
        return components;
    }

    public HealthCheckResponse setComponents(List<String> components) {
        this.components = components;
        return this;
    }

    public List<String> getUnavailableComponents() {
        return unavailableComponents;
    }

    public HealthCheckResponse setUnavailableComponents(List<String> unavailableComponents) {
        this.unavailableComponents = unavailableComponents;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HealthCheckResponse{");
        sb.append("serviceName='").append(serviceName).append('\'');
        sb.append(", requestUrl='").append(requestUrl).append('\'');
        sb.append(", datetime='").append(datetime).append('\'');
        sb.append(", dependencies=").append(dependencies);
        sb.append(", status=").append(status);
        sb.append(", components=").append(components);
        sb.append(", unavailableComponents=").append(unavailableComponents);
        sb.append('}');
        return sb.toString();
    }
}
