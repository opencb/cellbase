package org.opencb.cellbase.client.rest;

import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by swaathi on 23/05/16.
 */
public class VariationClientTest {
    private CellBaseClient cellBaseClient;

    public VariationClientTest() {
        ClientConfiguration clientConfiguration;
        try {
            clientConfiguration = ClientConfiguration.load(getClass().getResource("/client-configuration-test.yml").openStream());
            cellBaseClient = new CellBaseClient(clientConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void count() throws Exception {
        QueryResponse<Long> count = cellBaseClient.getVariationClient().count(new Query());
        assertEquals("Number of returned variants do not match", 154913735, count.firstResult().longValue());
    }

    @Test
    public void first() throws Exception {
        QueryResponse<Variant> first = cellBaseClient.getVariationClient().first();
        assertNotNull("First Variation in the collection must be returned", first);
    }

    @Test
    public void get() throws Exception {
        QueryResponse<Variant> variation = cellBaseClient.getVariationClient().get(Collections.singletonList("rs666"), null);
        assertNotNull("This variation should exist", variation.firstResult());
    }

    @Test
    public void getAllConsequenceTypes() throws Exception {
        QueryResponse<String> response = cellBaseClient.getVariationClient().getAllConsequenceTypes(new Query());
        assertNotNull("List of all the consequence types present should be returned", response.firstResult());
    }

    @Test
    public void getConsequenceTypeById() throws Exception {
        QueryResponse<String> stringQueryResponse = cellBaseClient.getVariationClient().getConsequenceTypeById("rs6661", null);
        assertEquals("Consequence Type of rs6661 is wrong", "3_prime_UTR_variant", stringQueryResponse.firstResult());
    }
}