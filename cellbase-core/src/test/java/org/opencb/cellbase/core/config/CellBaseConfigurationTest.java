package org.opencb.cellbase.core.config;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by jacobo on 29/03/19.
 */
public class CellBaseConfigurationTest {

    @Test
    public void testEnvVars() throws Exception {
        CellBaseConfiguration configuration = new CellBaseConfiguration();
        Map<String, String> envVariables = new HashMap<>();

        envVariables.put("CELLBASE_DATABASES_MONGODB_HOST", "myHost");
        envVariables.put("CELLBASE_DATABASES_MONGODB_USER", "me");
        envVariables.put("CELLBASE_DATABASES_MONGODB_PASSWORD", "1234");
        envVariables.put("CELLBASE_DATABASES_MONGODB_OPTIONS_AUTHENTICATION_DATABASE", "admin");
        envVariables.put("CELLBASE_DATABASES_MONGODB_OPTIONS_REPLICA_SET", "IDK");
        envVariables.put("CELLBASE_DATABASES_MONGODB_OPTIONS_READ_PREFERENCE", "any");
        envVariables.put("CELLBASE_DATABASES_MONGODB_OPTIONS_CONNECTIONS_PER_HOST", "a lot!");
        envVariables.put("CELLBASE_DATABASES_MONGODB_OPTIONS_SSL_ENABLED", "true");

        CellBaseConfiguration.overwriteEnvVariables(configuration, envVariables);

        assertEquals(configuration.getDatabases().getMongodb().getHost(), "myHost");
        assertEquals(configuration.getDatabases().getMongodb().getUser(), "me");
        assertEquals(configuration.getDatabases().getMongodb().getPassword(), "1234");
        assertThat(configuration.getDatabases().getMongodb().getOptions().entrySet(), hasItem(Pair.of("authenticationDatabase", "admin")));
        assertThat(configuration.getDatabases().getMongodb().getOptions().entrySet(), hasItem(Pair.of("replicaSet", "IDK")));
        assertThat(configuration.getDatabases().getMongodb().getOptions().entrySet(), hasItem(Pair.of("readPreference", "any")));
        assertThat(configuration.getDatabases().getMongodb().getOptions().entrySet(), hasItem(Pair.of("connectionsPerHost", "a lot!")));
        assertThat(configuration.getDatabases().getMongodb().getOptions().entrySet(), hasItem(Pair.of("sslEnabled", "true")));
    }
}
