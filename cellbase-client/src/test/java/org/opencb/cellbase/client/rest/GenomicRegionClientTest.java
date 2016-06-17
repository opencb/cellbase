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
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.Arrays;

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
    public void getGene() throws Exception {
        QueryResponse<Gene> geneQueryResponse = cellBaseClient.getGenomicRegionClient().getGene(Arrays.asList("3:555-77777"), null);
        assertNotNull("The gene that belongs to the region 3:555-77777 must be returned", geneQueryResponse.firstResult());
        assertEquals("The gene returned is wrong", "AY269186.1", geneQueryResponse.firstResult().getName());
    }

    @Test
    public void getTranscript() throws Exception {
        QueryResponse<Transcript> transcriptQueryResponse = cellBaseClient.getGenomicRegionClient().getTranscript(Arrays.asList("3:555-77777"), null);
        assertNotNull("The transcript that belongs to the region 3:555-77777 must be returned",transcriptQueryResponse.firstResult());
        assertEquals("The transcript returned is wrong", "AY269186.1-001", transcriptQueryResponse.firstResult().getName());
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

    @Test
    public void getSequence() throws Exception {
        QueryResponse<GenomeSequenceFeature> response = cellBaseClient.getGenomicRegionClient().getSequence(Arrays.asList("10:69999-77777"), null);
        assertTrue("The genome sequence for the region 3:555-77777 starts with GATTACCAAAGGC", response.firstResult().getSequence().startsWith("GATTACCAAAGGC"));
    }

    @Test
    public void getRegulatory() throws Exception {
        QueryResponse<RegulatoryFeature> regulatoryFeatureQueryResponse = cellBaseClient.getGenomicRegionClient().getRegulatory(Arrays.asList("10:69999-77777"), null);
        assertNotNull("Regulatory elements present in the region 10:69999-77777 must be returned", regulatoryFeatureQueryResponse.firstResult());
        assertEquals("The number of regulatory elements in the region 10:69999-77777 does not match", 39, regulatoryFeatureQueryResponse.getResponse().get(0).getResult().size());
    }

    @Test
    public void getTfbs() throws Exception {
        QueryResponse<RegulatoryFeature> response = cellBaseClient.getGenomicRegionClient().getTfbs(Arrays.asList("1:555-66666"), null);
        assertNotNull("Regulatory elements with feature type=TF_binding_site,TF_binding_site_motif must be returned", response.firstResult());
        assertEquals("The number of Regulatory elements with feature type=TF_binding_site,TF_binding_site_motif in 1:555-66666 does not match", 276,
                response.getResponse().get(0).getResult().size());
    }

    @Test
    public void getConservation() throws Exception {
        // TODO add default constructor to GenomicScoreRegion in biodata-models
        QueryResponse<GenomicScoreRegion> conservation = cellBaseClient.getGenomicRegionClient().getConservation(Arrays.asList("1:555-66666"), null);
        assertNotNull("Conservation values for 1:555-66666 must be returned", conservation.firstResult());

    }
}