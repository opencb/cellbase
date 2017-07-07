package org.opencb.cellbase.client.rest;

import org.junit.Test;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by fjlopez on 06/07/17.
 */
public class MetaClientTest {

    private CellBaseClient cellBaseClient;

    public MetaClientTest() {
        try {
            cellBaseClient = new CellBaseClient(ClientConfiguration.load(getClass().getResourceAsStream("/client-configuration-test.yml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAbout() throws Exception {
        QueryResponse<Map> about = cellBaseClient.getMetaClient().about();
        assertEquals(5, about.firstResult().size());
        assertTrue(about.firstResult().containsKey("Program: "));
        assertTrue(about.firstResult().containsKey("Description: "));
        assertTrue(about.firstResult().containsKey("Git commit: "));
        assertTrue(about.firstResult().containsKey("Version: "));
        assertTrue(about.firstResult().containsKey("Git branch: "));
    }

}
