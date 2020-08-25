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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.CellBaseDataResponse;
import org.opencb.commons.datastore.core.QueryOptions;


import java.io.IOException;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Created by imedina on 26/05/16.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GenomicRegionClientTest {

    private CellBaseClient cellBaseClient;

    public GenomicRegionClientTest() {
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
    public void getGene() throws Exception {
        CellBaseDataResponse<Gene> geneCellBaseDataResponse = cellBaseClient.getGenomicRegionClient().getGene(Arrays.asList("3:555-77777"), null);
        assertNotNull(geneCellBaseDataResponse.firstResult());
        assertEquals("LINC01986", geneCellBaseDataResponse.firstResult().getName());
    }

    @Test
    public void getTranscript() throws Exception {
        CellBaseDataResponse<Transcript> transcriptCellBaseDataResponse = cellBaseClient.getGenomicRegionClient().getTranscript(Arrays.asList("3:555-77777"), null);
        assertNotNull(transcriptCellBaseDataResponse.firstResult());
        assertEquals("LINC01986-206", transcriptCellBaseDataResponse.firstResult().getName());
    }

    @Test
    public void getRepeat() throws Exception {
        CellBaseDataResponse<Repeat> queryResponse = cellBaseClient
                .getGenomicRegionClient().getRepeat(Arrays.asList("3:555-77777"), null);
        // MAY need fixing
        assertTrue(queryResponse.allResults().size() > 0);
    }

    @Test
    public void getVariant() throws Exception {
        QueryOptions queryOptions = new QueryOptions(QueryOptions.INCLUDE, "chromosome,start,end,id");
        CellBaseDataResponse<Variant> variantCellBaseDataResponse = cellBaseClient.getGenomicRegionClient()
                .getVariant(Arrays.asList("22:35490160-35490161", "22:10510033-10510034"), queryOptions);

        assertNotNull(variantCellBaseDataResponse.firstResult());
        assertEquals(variantCellBaseDataResponse.getResponses().get(0).getNumResults(),
                variantCellBaseDataResponse.getResponses().get(0).getResults().size());
        assertTrue(variantCellBaseDataResponse.getResponses().get(0).getResults().size() == 3);
        assertEquals(variantCellBaseDataResponse.getResponses().get(1).getNumResults(),
                variantCellBaseDataResponse.getResponses().get(1).getResults().size());
        assertTrue(variantCellBaseDataResponse.getResponses().get(1).getResults().size() == 1);
    }

    @Test
    public void getSequence() throws Exception {
        CellBaseDataResponse<GenomeSequenceFeature> response = cellBaseClient.getGenomicRegionClient().getSequence(Arrays.asList("10:69999-77777"), null);
        assertTrue(response.firstResult().getSequence().startsWith("AACCAAGCTAAAC"));
    }

    @Test
    public void getRegulatory() throws Exception {
        CellBaseDataResponse<RegulatoryFeature> regulatoryFeatureCellBaseDataResponse = cellBaseClient.getGenomicRegionClient().getRegulatory(Arrays.asList("10:69999-77777"), null);
        assertNotNull(regulatoryFeatureCellBaseDataResponse.firstResult());
        assertEquals(4, regulatoryFeatureCellBaseDataResponse.getResponses().get(0).getResults().size());
    }

    @Test
    public void getTfbs() throws Exception {
        CellBaseDataResponse<RegulatoryFeature> response = cellBaseClient.getGenomicRegionClient().getTfbs(Arrays.asList("1:555-66666"), null);
        assertNotNull(response.firstResult());
        assertEquals(1, response.getResponses().get(0).getResults().size());
    }

    @Test
    public void getConservation() throws Exception {
        QueryOptions queryOptions = new QueryOptions(QueryOptions.LIMIT, 10);
        CellBaseDataResponse<GenomicScoreRegion> conservation = cellBaseClient.getGenomicRegionClient().getConservation(Arrays.asList("1:6635137-6635325"), queryOptions);
        assertNotNull(conservation.firstResult());

    }
}