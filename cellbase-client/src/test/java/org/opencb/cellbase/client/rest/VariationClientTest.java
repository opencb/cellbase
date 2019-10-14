/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.client.rest;

import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.Query;

import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.Collections;


import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertTrue("Number of returned variants do not match", count.firstResult().longValue() > 329000000 );
    }

    @Test
    public void first() throws Exception {
        QueryResponse<Variant> first = cellBaseClient.getVariationClient().first();
        assertNotNull(first,"First Variation in the collection must be returned");
    }

    @Test
    public void get() throws Exception {
        QueryResponse<Variant> variation = cellBaseClient.getVariationClient().get(Collections.singletonList("rs666"), null);
        assertNotNull(variation.firstResult(), "This variation should exist");
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