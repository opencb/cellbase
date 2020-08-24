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

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opencb.biodata.models.core.Gene;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.CellBaseDataResponse;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.QueryOptions;


import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by fjlopez on 07/07/17.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GenericClientTest {

    private CellBaseClient cellBaseClient;

    public GenericClientTest() {
    }

    @BeforeAll
    public void setUp() throws Exception {
        try {
            cellBaseClient = new CellBaseClient(ClientConfiguration.load(getClass().getResourceAsStream("/client-configuration-test.yml")));
        } catch (IOException e) {
            throw new RuntimeException(" didn't initialise client correctly ");
        }
    }

    @Test
    public void get() throws Exception {
        CellBaseDataResponse<Gene> queryResponse1 = cellBaseClient.getGenericClient().get("feature", "gene", "BRCA2", "info",
                new QueryOptions(QueryOptions.INCLUDE, "name"), Gene.class);
        assertEquals(1, queryResponse1.allResults().size());
        assertEquals("BRCA2", queryResponse1.firstResult().getName());

        CellBaseDataResponse<ObjectMap> queryResponse2 = cellBaseClient.getGenericClient().get("meta", "about", new QueryOptions(),
                ObjectMap.class);
        assertEquals(5, queryResponse2.firstResult().size());
        assertTrue(queryResponse2.firstResult().containsKey("Program"));
        assertTrue(queryResponse2.firstResult().containsKey("Description"));
        assertTrue(queryResponse2.firstResult().containsKey("Git commit"));
        assertTrue(queryResponse2.firstResult().containsKey("Version"));
        assertTrue(queryResponse2.firstResult().containsKey("Git branch"));

        queryResponse2 = cellBaseClient.getGenericClient().get("meta", null, "hsapiens",
                "versions", new QueryOptions(), ObjectMap.class);
        assertTrue(queryResponse2.allResults().size() > 0);
        assertThat(queryResponse2
                .allResults()
                .stream()
                .map((value) -> value.get("data"))
                .collect(Collectors.toSet()), CoreMatchers.hasItems("gene", "ontology"));

    }

}
