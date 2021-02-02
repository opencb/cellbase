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
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResponse;
import org.opencb.cellbase.core.config.SpeciesProperties;
import org.opencb.commons.datastore.core.ObjectMap;


import java.io.IOException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by fjlopez on 06/07/17.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetaClientTest {

    private CellBaseClient cellBaseClient;

    public MetaClientTest() {
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
    public void about() throws Exception {
        CellBaseDataResponse<ObjectMap> about = cellBaseClient.getMetaClient().about();
        assertEquals(5, about.firstResult().size());
        assertTrue(about.firstResult().containsKey("Program"));
        assertTrue(about.firstResult().containsKey("Description"));
        assertTrue(about.firstResult().containsKey("Git commit"));
        assertTrue(about.firstResult().containsKey("Version"));
        assertTrue(about.firstResult().containsKey("Git branch"));
    }

    @Test
    public void species() throws Exception {
        CellBaseDataResponse<SpeciesProperties> queryResponse = cellBaseClient.getMetaClient().species();
        assertTrue(queryResponse.firstResult().getVertebrates().stream().map((species) -> species.getId())
                .collect(Collectors.toList()).contains("hsapiens"));
    }

    @Test
    public void versions() throws Exception {
        CellBaseDataResponse<ObjectMap> queryResponse = cellBaseClient.getMetaClient().versions();
        assertTrue(queryResponse.allResults().size() > 0);
        assertThat(queryResponse
                .allResults()
                .stream()
                .map((value) -> value.get("data"))
                .collect(Collectors.toSet()), CoreMatchers.hasItems("gene", "ontology"));
    }

}
