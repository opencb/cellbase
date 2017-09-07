package org.opencb.cellbase.client.rest;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.opencb.biodata.models.core.Gene;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 07/07/17.
 */
public class GenericClientTest {

    private CellBaseClient cellBaseClient;

    public GenericClientTest() {
        try {
            cellBaseClient = new CellBaseClient(ClientConfiguration.load(getClass().getResourceAsStream("/client-configuration-test.yml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void get() throws Exception {
        QueryResponse<Gene> queryResponse1 = cellBaseClient.getGenericClient().get("feature", "gene", "BRCA2", "info",
                new QueryOptions(QueryOptions.INCLUDE, "name"), Gene.class);
        assertEquals(1, queryResponse1.allResults().size());
        assertEquals("BRCA2", queryResponse1.firstResult().getName());

        QueryResponse<ObjectMap> queryResponse2 = cellBaseClient.getGenericClient().get("meta", "about", new QueryOptions(),
                ObjectMap.class);
        assertEquals(5, queryResponse2.firstResult().size());
        assertTrue(queryResponse2.firstResult().containsKey("Program: "));
        assertTrue(queryResponse2.firstResult().containsKey("Description: "));
        assertTrue(queryResponse2.firstResult().containsKey("Git commit: "));
        assertTrue(queryResponse2.firstResult().containsKey("Version: "));
        assertTrue(queryResponse2.firstResult().containsKey("Git branch: "));

        queryResponse2 = cellBaseClient.getGenericClient().get("meta", null, "hsapiens",
                "versions", new QueryOptions(), ObjectMap.class);
        assertTrue(queryResponse2.allResults().size() > 0);
        assertThat(queryResponse2
                .allResults()
                .stream()
                .map((value) -> value.get("data"))
                .collect(Collectors.toSet()), CoreMatchers.hasItems("variation", "conservation"));

    }

}
