package org.opencb.cellbase.core.config;

/**
 * Created by imedina on 20/10/16.
 */
public class CacheProperties {

    /**
     * This field contain the host and port, ie. host[:port].
     */
    private String host;
    private boolean active;

    /**
     * Accepted values are: JSON, Kryo.
     */
    private String serialization;

    private int slowThreshold;

    public static final boolean DEFAULT_ACTVE = true;
    public static final String DEFAULT_SERIALIZATION = "JSON";
    public static final String DEFAULT_HOST = "localhost:6379";

    public CacheProperties() {
        this("", DEFAULT_ACTVE, DEFAULT_SERIALIZATION, 50);
    }

    public CacheProperties(String host, boolean active, String serialization, int slowThreshold) {
        this.host = host;
        this.active = active;
        this.serialization = serialization;
        this.slowThreshold = slowThreshold;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CacheProperties{");
        sb.append("host='").append(host).append('\'');
        sb.append(", active=").append(active);
        sb.append(", serialization='").append(serialization).append('\'');
        sb.append(", slowThreshold=").append(slowThreshold);
        sb.append('}');
        return sb.toString();
    }

    public String getHost() {
        return host;
    }

    public CacheProperties setHost(String host) {
        this.host = host;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public CacheProperties setActive(boolean active) {
        this.active = active;
        return this;
    }

    public String getSerialization() {
        return serialization;
    }

    public CacheProperties setSerialization(String serialization) {
        this.serialization = serialization;
        return this;
    }

    public int getSlowThreshold() {
        return slowThreshold;
    }

    public CacheProperties setSlowThreshold(int slowThreshold) {
        this.slowThreshold = slowThreshold;
        return this;
    }
}
