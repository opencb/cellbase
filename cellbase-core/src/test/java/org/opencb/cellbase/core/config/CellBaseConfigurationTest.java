package org.opencb.cellbase.core.config;

import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by jacobo on 29/03/19.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CellBaseConfigurationTest {

    CellBaseConfiguration cellBaseConfiguration;

    @BeforeAll
    public void setUp() throws Exception {
        cellBaseConfiguration = CellBaseConfiguration.load(
                org.opencb.cellbase.core.config.CellBaseConfigurationTest.class.getResourceAsStream("/cellBaseProperties_test.json"),
                CellBaseConfiguration.ConfigurationFileFormat.JSON);
    }

    @Test
    public void load() throws Exception {
        cellBaseConfiguration = CellBaseConfiguration.load(
                org.opencb.cellbase.core.config.CellBaseConfigurationTest.class.getResourceAsStream("/cellBaseProperties_test.json"),
                CellBaseConfiguration.ConfigurationFileFormat.JSON);
        System.out.println(new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(cellBaseConfiguration));
    }

    @Test
    public void defaultOutdir() {
        Assertions.assertEquals("/tmp", cellBaseConfiguration.getDefaultOutdir());
    }

    @Test
    public void vertebrates() {
        Assertions.assertEquals(3, cellBaseConfiguration.getSpecies().getVertebrates().size());
    }

    @Test
    public void metazoa() {
        Assertions.assertEquals(1, cellBaseConfiguration.getSpecies().getMetazoa().size());
    }

    @Test
    public void fungi() {
        Assertions.assertEquals(1, cellBaseConfiguration.getSpecies().getFungi().size());
    }

    @Test
    public void plants() {
        Assertions.assertEquals(2, cellBaseConfiguration.getSpecies().getPlants().size());
    }

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

        Assertions.assertEquals(configuration.getDatabases().getMongodb().getHost(), "myHost");
        Assertions.assertEquals(configuration.getDatabases().getMongodb().getUser(), "me");
        Assertions.assertEquals(configuration.getDatabases().getMongodb().getPassword(), "1234");
        assertThat(configuration.getDatabases().getMongodb().getOptions().entrySet(), hasItem(Pair.of("authenticationDatabase", "admin")));
        assertThat(configuration.getDatabases().getMongodb().getOptions().entrySet(), hasItem(Pair.of("replicaSet", "IDK")));
        assertThat(configuration.getDatabases().getMongodb().getOptions().entrySet(), hasItem(Pair.of("readPreference", "any")));
        assertThat(configuration.getDatabases().getMongodb().getOptions().entrySet(), hasItem(Pair.of("connectionsPerHost", "a lot!")));
        assertThat(configuration.getDatabases().getMongodb().getOptions().entrySet(), hasItem(Pair.of("sslEnabled", "true")));
    }
}
