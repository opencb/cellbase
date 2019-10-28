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
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
    public void getGene() throws Exception {
        QueryResponse<Gene> geneQueryResponse = cellBaseClient.getGenomicRegionClient().getGene(Arrays.asList("3:555-77777"), null);
        assertNotNull(geneQueryResponse.firstResult());
        assertEquals("AY269186.1", geneQueryResponse.firstResult().getName());
    }

    @Test
    public void getTranscript() throws Exception {
        QueryResponse<Transcript> transcriptQueryResponse = cellBaseClient.getGenomicRegionClient().getTranscript(Arrays.asList("3:555-77777"), null);
        assertNotNull(transcriptQueryResponse.firstResult());
        assertEquals("AY269186.1-001", transcriptQueryResponse.firstResult().getName());
    }

    @Test
    public void getRepeat() throws Exception {
        QueryResponse<Repeat> queryResponse = cellBaseClient
                .getGenomicRegionClient().getRepeat(Arrays.asList("3:555-77777"), null);
        // MAY need fixing
        assertTrue(queryResponse.allResults().size() > 0);
    }

    @Test
    public void getVariation() throws Exception {
        QueryOptions queryOptions = new QueryOptions(QueryOptions.INCLUDE, "chromosome,start,id");
        QueryResponse<Variant> variantQueryResponse = cellBaseClient.getGenomicRegionClient()
                .getVariation(Arrays.asList("3:555-77777", "11:58888-198888"), queryOptions);

        assertNotNull(variantQueryResponse.firstResult());
        assertEquals(variantQueryResponse.getResponse().get(0).getNumTotalResults(),
                variantQueryResponse.getResponse().get(0).getResult().size());
        assertTrue(variantQueryResponse.getResponse().get(0).getResult().size() >= 2699);
        assertEquals(variantQueryResponse.getResponse().get(1).getNumTotalResults(),
                variantQueryResponse.getResponse().get(1).getResult().size());
        assertTrue(variantQueryResponse.getResponse().get(1).getResult().size() >= 12029);
    }

    @Test
    public void getSequence() throws Exception {
        QueryResponse<GenomeSequenceFeature> response = cellBaseClient.getGenomicRegionClient().getSequence(Arrays.asList("10:69999-77777"), null);
        assertTrue(response.firstResult().getSequence().startsWith("GATTACCAAAGGC"));
    }

    @Test
    public void getRegulatory() throws Exception {
        QueryResponse<RegulatoryFeature> regulatoryFeatureQueryResponse = cellBaseClient.getGenomicRegionClient().getRegulatory(Arrays.asList("10:69999-77777"), null);
        assertNotNull(regulatoryFeatureQueryResponse.firstResult());
        assertEquals(39, regulatoryFeatureQueryResponse.getResponse().get(0).getResult().size());
    }

    @Test
    public void getTfbs() throws Exception {
        QueryResponse<RegulatoryFeature> response = cellBaseClient.getGenomicRegionClient().getTfbs(Arrays.asList("1:555-66666"), null);
        assertNotNull(response.firstResult());
        assertEquals(276, response.getResponse().get(0).getResult().size());
    }

    @Test
    public void getConservation() throws Exception {
        QueryResponse<GenomicScoreRegion> conservation = cellBaseClient.getGenomicRegionClient().getConservation(Arrays.asList("1:555-66666"), null);
        assertNotNull(conservation.firstResult());

    }
}