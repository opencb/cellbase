package org.opencb.cellbase.core.config;

import java.util.Map;

/**
 * Created by imedina on 19/08/16.
 */
public class DatabaseProperties {

    private String host;
//    @Deprecated
//    private String port;
    private String user;
    private String password;
    private Map<String, String> options;


    public DatabaseProperties() {
    }

    public DatabaseProperties(String host, String user, String password, Map<String, String> options) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.options = options;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatabaseProperties{");
        sb.append("host='").append(host).append('\'');
        sb.append(", user='").append(user).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", options=").append(options);
        sb.append('}');
        return sb.toString();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

}
