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
import org.opencb.biodata.formats.protein.uniprot.v202003jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.TranscriptTfbs;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.client.rest.models.GroupByFields;
import org.opencb.cellbase.client.rest.models.GroupCount;
import org.opencb.cellbase.core.CellBaseDataResponse;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Created by imedina on 12/05/16.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GeneClientTest {

    private CellBaseClient cellBaseClient;

//    @Rule
//    public TestRule globalTimeout = new Timeout(2000);

    public GeneClientTest() {
    }

    @BeforeAll
    public void setUp() throws Exception {
        try {
            cellBaseClient = new CellBaseClient(ClientConfiguration.load(getClass().getResourceAsStream("/client-configuration-test.yml")));
        } catch (IOException e) {
            throw new RuntimeException(" didn't initialise client correctly ");
        }
    }

//    @Test
//    public void count() throws Exception {
//        CellBaseDataResponse<Long> count = cellBaseClient.getGeneClient().count(new Query());
//        assertEquals( 57905, count.firstResult().longValue(), "Number of returned genes do not match");
//
//        count = cellBaseClient.getGeneClient().count(new Query(GeneDBAdaptor.QueryParams.BIOTYPE.key(), "protein_coding"));
//        assertEquals(20356, count.firstResult().longValue(), "Number of returned protein-coding genes do not match");
//    }

//    @Test
//    public void first() throws Exception {
//        CellBaseDataResponse<Gene> gene = cellBaseClient.getGeneClient().first();
//        assertNotNull(gene, "First gene in the collection must be returned");
//    }

    @Test
    public void getBiotypes() throws Exception {
        CellBaseDataResponse<String> biotypes = cellBaseClient.getGeneClient().getBiotypes(null);
        assertNotNull(biotypes.firstResult());

        biotypes = cellBaseClient.getGeneClient().getBiotypes(new Query(ParamConstants.QueryParams.REGION.key(), "1:65100-65200"));
        assertNull(biotypes.firstResult());

        biotypes = cellBaseClient.getGeneClient().getBiotypes(new Query(ParamConstants.QueryParams.REGION.key(), "1:20000-70000"));
        assertNotNull(biotypes.firstResult());
    }

    @Test
    public void get() throws Exception {
        CellBaseDataResponse<Gene> gene = cellBaseClient.getGeneClient().get(Collections.singletonList("BRCA2"), null);
        assertNotNull(gene.firstResult());

        gene = cellBaseClient.getGeneClient().get(Collections.singletonList("BRCA2"), new QueryOptions(QueryOptions.EXCLUDE, "transcripts"));
        assertNull(gene.firstResult().getTranscripts());

        gene = cellBaseClient.getGeneClient().get(Collections.singletonList("NotExistingGene"), null);
        assertNull(gene.firstResult());
    }

    @Test
    public void list() throws Exception {
        CellBaseDataResponse<Gene> gene = cellBaseClient.getGeneClient().list(new Query("limit", 10));
        assertNotNull(gene);
    }

    @Test
    public void search() throws Exception {
        Map<String, Object> params = new HashMap<>();
        CellBaseDataResponse<Gene> gene = cellBaseClient.getGeneClient().search(new Query(ParamConstants.QueryParams.BIOTYPE.key(), "miRNA"),
                new QueryOptions("limit", 1));
        assertNotNull(gene.firstResult());
    }

    @Test
    public void getProtein() throws Exception {
        CellBaseDataResponse<Entry> protein = cellBaseClient.getGeneClient().getProtein("BRCA2", null);
        assertNotNull(protein.firstResult());
    }

    @Test
    public void getSnp() throws Exception {
        QueryOptions queryOptions = new QueryOptions("exclude", "annotation");
        queryOptions.add(QueryOptions.LIMIT, 3);
        CellBaseDataResponse<Variant> variantCellBaseDataResponse = cellBaseClient.getGeneClient().getVariation(Arrays.asList("BRCA2",
                "hsapiens"), queryOptions);
        assertNotNull(variantCellBaseDataResponse.firstResult());
        assertNotNull(variantCellBaseDataResponse.getResponses().get(1).getResults());
    }

    @Test
    public void getTfbs() throws Exception {
        CellBaseDataResponse<TranscriptTfbs> tfbs = cellBaseClient.getGeneClient().getTfbs("ENSG00000132170", null);
        assertNotNull(tfbs.firstResult());
    }

    @Test
    public void getTranscript() throws Exception {
        CellBaseDataResponse<Transcript> transcript = cellBaseClient.getGeneClient().getTranscript("BRCA2", null);
        assertNotNull(transcript.firstResult());

        QueryOptions queryOptions = new QueryOptions();
        queryOptions.add("biotype", "protein_coding");
        queryOptions.add(QueryOptions.COUNT, true);
        transcript = cellBaseClient.getGeneClient().getTranscript("BRCA2", queryOptions);
        assertNotNull(transcript.firstResult());
        assertEquals(5, transcript.getResponses().get(0).getNumResults(), "Number of transcripts with biotype protein_coding");
    }

//    @Test
//    public void getClinical() throws Exception {
//        CellBaseDataResponse<Document> clinical = cellBaseClient.getGeneClient().getClinical("BRCA2", null);
//        assertNotNull(clinical.firstResult());
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("source", "cosmic");
//        params.put("limit", 2);
//        clinical = cellBaseClient.getGeneClient().getClinical("BRCA2", params);
//        assertNotNull(clinical.firstResult());
//    }

    @Test
    public void group() throws Exception {
        Query query = new Query();
        query.put("field", "chromosome");
        query.put("region", "1:6635137-6835325");
        CellBaseDataResponse<GroupByFields> group = cellBaseClient.getGeneClient().group(query, new QueryOptions());
        assertNotNull(group.firstResult());
    }

    @Test
    public void groupCount() throws Exception {
        Query query = new Query();
        query.put("field", "chromosome");
        query.put("count", true);
        CellBaseDataResponse<GroupCount> result = cellBaseClient.getGeneClient().groupCount(query, new QueryOptions());
        assertNotNull(result.firstResult());
    }
}