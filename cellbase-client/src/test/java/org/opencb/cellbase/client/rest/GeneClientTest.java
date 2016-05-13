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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.opencb.biodata.models.core.Gene;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.client.config.RestConfig;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
/**
 * Created by imedina on 12/05/16.
 */
public class GeneClientTest {

    private CellBaseClient cellBaseClient;

    @Rule
    public TestRule globalTimeout = new Timeout(1000);

    public GeneClientTest() {
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
        QueryResponse<Long> count = cellBaseClient.getGeneClient().count(null);
        assertEquals("Number of returned genes do not match", 57905, count.firstResult().longValue());

        count = cellBaseClient.getGeneClient().count(new Query(GeneDBAdaptor.QueryParams.BIOTYPE.key(), "protein_coding"));
        assertEquals("Number of returned protein-coding genes do not match", 20356, count.firstResult().longValue());
    }

    @Test
    public void distinct() throws Exception {
        QueryResponse<String> distinct = cellBaseClient.getGeneClient().distinct(new Query(GeneDBAdaptor.QueryParams.BIOTYPE.key()));
        System.out.println(distinct.getResponse().get(0).getResult());
    }

    @Test
    public void get() throws Exception {
        QueryResponse<Gene> gene = cellBaseClient.getGeneClient().get("BRCA2", null, null);
        assertNotNull("This gene should exist", gene.firstResult());

        Map<String, Object> params = new HashMap<>();
        params.put("exclude", "transcripts");
        gene = cellBaseClient.getGeneClient().get("BRCA2", params, null);
        assertNull("This gene should not have transcritps", gene.firstResult().getTranscripts());

        gene = cellBaseClient.getGeneClient().get("NotExistingGene", null, null);
        assertNull("This gene should not exist", gene.firstResult());
    }

    @Test
    public void search() throws Exception {
        QueryResponse<Gene> gene = cellBaseClient.getGeneClient().search(new Query(GeneDBAdaptor.QueryParams.BIOTYPE.key(), "miRNA"), new QueryOptions("limit", 1));
        System.out.println(gene.getResponse().get(0).getResult());
    }
}