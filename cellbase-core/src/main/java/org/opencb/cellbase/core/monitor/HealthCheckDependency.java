
package org.opencb.cellbase.core.monitor;

public class HealthCheckDependency {
    // 'Url of the service'
    private String url;
    private HealthCheckResponse.Status status;
    // 'Type of dependency. For example: `REST API`'
    private String type;
    // 'Description of the dependency. For example: `Exomiser`'
    private String description;
    private Object additionalProperties;

    public HealthCheckDependency(String url, HealthCheckResponse.Status status, String type, String description,
                                 Object additionalProperties) {
        this.url = url;
        this.status = status;
        this.type = type;
        this.description = description;
        this.additionalProperties = additionalProperties;
    }

    public String getUrl() {
        return url;
    }

    public HealthCheckDependency setUrl(String url) {
        this.url = url;
        return this;
    }

    public HealthCheckResponse.Status getStatus() {
        return status;
    }

    public HealthCheckDependency setStatus(HealthCheckResponse.Status status) {
        this.status = status;
        return this;
    }

    public String getType() {
        return type;
    }

    public HealthCheckDependency setType(String type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public HealthCheckDependency setDescription(String description) {
        this.description = description;
        return this;
    }

    public Object getAdditionalProperties() {
        return additionalProperties;
    }

    public HealthCheckDependency setAdditionalProperties(Object additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HealthCheckDependency{");
        sb.append("url='").append(url).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", additionalProperties=").append(additionalProperties);
        sb.append('}');
        return sb.toString();
    }
}
