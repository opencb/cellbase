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
import org.opencb.cellbase.core.result.CellBaseDataResponse;
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
    public void testgetGene() throws Exception {
        CellBaseDataResponse<Gene> geneCellBaseDataResponse = cellBaseClient.getGenomicRegionClient().getGene(Arrays.asList("3:555-77777"), null);
        assertNotNull(geneCellBaseDataResponse.firstResult());
        assertEquals("AC066595.1", geneCellBaseDataResponse.firstResult().getName());
    }

    @Test
    public void testgetTranscript() throws Exception {
        CellBaseDataResponse<Transcript> transcriptCellBaseDataResponse = cellBaseClient.getGenomicRegionClient().getTranscript(Arrays.asList("3:555-77777"), null);
        assertNotNull(transcriptCellBaseDataResponse.firstResult());
        assertEquals("LINC01986-206", transcriptCellBaseDataResponse.firstResult().getName());
    }

    @Test
    public void testgetRepeat() throws Exception {
        CellBaseDataResponse<Repeat> queryResponse = cellBaseClient
                .getGenomicRegionClient().getRepeat(Arrays.asList("3:555-77777"), null);
        // MAY need fixing
        assertTrue(queryResponse.allResults().size() > 0);
    }

    @Test
    public void testgetVariant() throws Exception {
        QueryOptions queryOptions = new QueryOptions(QueryOptions.INCLUDE, "chromosome,start,end,id");
        CellBaseDataResponse<Variant> variantCellBaseDataResponse = cellBaseClient.getGenomicRegionClient()
                .getVariant(Arrays.asList("13:32316519", "13:32316514-32316517"), queryOptions);

        assertNotNull(variantCellBaseDataResponse.firstResult());
        assertEquals(variantCellBaseDataResponse.getResponses().get(0).getNumResults(),
                variantCellBaseDataResponse.getResponses().get(0).getResults().size());
        assertEquals(1, variantCellBaseDataResponse.getResponses().get(0).getResults().size());
        assertEquals(variantCellBaseDataResponse.getResponses().get(1).getNumResults(),
                variantCellBaseDataResponse.getResponses().get(1).getResults().size());
        assertEquals(5, variantCellBaseDataResponse.getResponses().get(1).getResults().size());
    }

    @Test
    public void testgetSequence() throws Exception {
        CellBaseDataResponse<GenomeSequenceFeature> response = cellBaseClient.getGenomicRegionClient().getSequence(Arrays.asList("10:69999-77777"), null);
        assertTrue(response.firstResult().getSequence().startsWith("AACCAAGCTAAAC"));
    }

    @Test
    public void testgetRegulatory() throws Exception {
        CellBaseDataResponse<RegulatoryFeature> regulatoryFeatureCellBaseDataResponse = cellBaseClient.getGenomicRegionClient().getRegulatory(Arrays.asList("10:69999-77777"), null);
        assertNotNull(regulatoryFeatureCellBaseDataResponse.firstResult());
        assertEquals(4, regulatoryFeatureCellBaseDataResponse.getResponses().get(0).getResults().size());
    }

    @Test
    public void testgetTfbs() throws Exception {
        CellBaseDataResponse<RegulatoryFeature> response = cellBaseClient.getGenomicRegionClient().getTfbs(Arrays.asList("1:555-66666"), null);
        assertNotNull(response.firstResult());
        assertEquals(1, response.getResponses().get(0).getResults().size());
    }

    @Test
    public void testgetConservation() throws Exception {
        //QueryOptions queryOptions = new QueryOptions(QueryOptions.LIMIT, 10);
        CellBaseDataResponse<GenomicScoreRegion> conservation = cellBaseClient.getGenomicRegionClient().getConservation(Arrays.asList("1"
                + ":6635137-6635325"), null);
        assertNotNull(conservation.firstResult());

    }
}