package org.opencb.cellbase.core.monitor;

public class DatastoreStatus {

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
