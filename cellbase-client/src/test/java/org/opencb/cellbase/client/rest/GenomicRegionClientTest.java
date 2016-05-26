/*
 * Copyright 2015 OpenCB
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

import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by imedina on 26/05/16.
 */
public class GenomicRegionClientTest {

    private CellBaseClient cellBaseClient;

    public GenomicRegionClientTest() {
        try {
            cellBaseClient = new CellBaseClient(ClientConfiguration.load(getClass().getResourceAsStream("/client-configuration-test.yml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getVariation() throws Exception {
        QueryOptions queryOptions = new QueryOptions(QueryOptions.INCLUDE, "ids");
        QueryResponse<Variant> variantQueryResponse = cellBaseClient.getGenomicRegionClient()
                .getVariation(Arrays.asList("3:555-77777", "11:58888-198888"), queryOptions);

        assertNotNull("SNPs of the given gene must be returned", variantQueryResponse.firstResult());
        assertEquals("Number of variations do not match for 3:555-77777", 1257, variantQueryResponse.getResponse().get(0).getResult().size());
        assertEquals("Number of variations do not match for 11:58888-198888", 2764, variantQueryResponse.getResponse().get(1).getResult().size());
    }

}