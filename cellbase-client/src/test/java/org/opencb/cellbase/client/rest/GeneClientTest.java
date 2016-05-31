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
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.TranscriptTfbs;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.client.rest.models.GroupByFields;
import org.opencb.cellbase.client.rest.models.GroupCount;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
/**
 * Created by imedina on 12/05/16.
 */
public class GeneClientTest {

    private CellBaseClient cellBaseClient;

//    @Rule
//    public TestRule globalTimeout = new Timeout(2000);

    public GeneClientTest() {
        try {
            cellBaseClient = new CellBaseClient(ClientConfiguration.load(getClass().getResourceAsStream("/client-configuration-test.yml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void count() throws Exception {
        QueryResponse<Long> count = cellBaseClient.getGeneClient().count(new Query());
        assertEquals("Number of returned genes do not match", 57905, count.firstResult().longValue());

        count = cellBaseClient.getGeneClient().count(new Query(GeneDBAdaptor.QueryParams.BIOTYPE.key(), "protein_coding"));
        assertEquals("Number of returned protein-coding genes do not match", 20356, count.firstResult().longValue());
    }

    @Test
    public void first() throws Exception {
        QueryResponse<Gene> gene = cellBaseClient.getGeneClient().first();
        assertNotNull("First gene in the collection must be returned", gene);
    }

    @Test
    public void getBiotypes() throws Exception {
        QueryResponse<String> biotypes = cellBaseClient.getGeneClient().getBiotypes(null);
        assertNotNull("List of biotypes should be returned", biotypes.firstResult());

        biotypes = cellBaseClient.getGeneClient().getBiotypes(new Query(GeneDBAdaptor.QueryParams.REGION.key(), "1:65342-66500"));
        assertNull("List of biotypes in the given region is empty", biotypes.firstResult());

        biotypes = cellBaseClient.getGeneClient().getBiotypes(new Query(GeneDBAdaptor.QueryParams.REGION.key(), "1:20000-70000"));
        assertNotNull("List of biotypes in the given region", biotypes.firstResult());
    }

    @Test
    public void get() throws Exception {
        QueryResponse<Gene> gene = cellBaseClient.getGeneClient().get(Collections.singletonList("BRCA2"), null);
        assertNotNull("This gene should exist", gene.firstResult());

        gene = cellBaseClient.getGeneClient().get(Collections.singletonList("BRCA2"), new QueryOptions(QueryOptions.EXCLUDE, "transcripts"));
        assertNull("This gene should not have transcritps", gene.firstResult().getTranscripts());

        gene = cellBaseClient.getGeneClient().get(Collections.singletonList("NotExistingGene"), null);
        assertNull("This gene should not exist", gene.firstResult());
    }

    @Test
    public void list() throws Exception {
        QueryResponse<Gene> gene = cellBaseClient.getGeneClient().list(new Query("limit", 10));
        assertNotNull("List of gene Ids", gene);
    }

    @Test
    public void search() throws Exception {
        Map<String, Object> params = new HashMap<>();
        QueryResponse<Gene> gene = cellBaseClient.getGeneClient().search(new Query(GeneDBAdaptor.QueryParams.BIOTYPE.key(), "miRNA"),
                new QueryOptions("limit", 1));
        assertNotNull("The genes with the given biotype must be returned", gene.firstResult());
    }

    @Test
    public void getProtein() throws Exception {
        QueryResponse<Entry> protein = cellBaseClient.getGeneClient().getProtein("BRCA2", null);
        assertNotNull("Protein of the given gene must be returned", protein.firstResult());
    }
//
    @Test
    public void getSnp() throws Exception {
        QueryResponse<Variant> variantQueryResponse = cellBaseClient.getGeneClient().getVariation(Arrays.asList("BRCA1", "TFF1"), new QueryOptions());
        assertNotNull("SNPs of the given gene must be returned", variantQueryResponse.firstResult());
    }
//
    @Test
    public void getTfbs() throws Exception {
        QueryResponse<TranscriptTfbs> tfbs = cellBaseClient.getGeneClient().getTfbs("BRCA2", null);
        assertNotNull("Tfbs of the given gene must be returned", tfbs.firstResult());
    }

    @Test
    public void getTranscript() throws Exception {
        QueryResponse<Transcript> transcript = cellBaseClient.getGeneClient().getTranscript("BRCA2", null);
        assertNotNull("Transcripts of the given gene must be returned", transcript.firstResult());

        transcript = cellBaseClient.getGeneClient().getTranscript("BRCA2", new QueryOptions("transcripts.biotype", "protein_coding"));
        assertNotNull(transcript.firstResult());
        assertEquals("Number of transcripts with biotype protein_coding", 3, transcript.getResponse().get(0).getNumTotalResults());
    }

//    @Test
//    public void getClinical() throws Exception {
//        QueryResponse<Document> clinical = cellBaseClient.getGeneClient().getClinical("BRCA2", null);
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
        query.put("fields", "chromosome");
        query.put("region", "1:6635137-6835325");
        QueryResponse<GroupByFields> group = cellBaseClient.getGeneClient().group(query, new QueryOptions());
        assertNotNull("chromosomes present in the given region should be returned", group.firstResult());
    }

    @Test
    public void groupCount() throws Exception {
        Query query = new Query();
        query.put("fields", "chromosome");
        query.put("count", true);
        QueryResponse<GroupCount> result = cellBaseClient.getGeneClient().groupCount(query, new QueryOptions());
        assertNotNull("chromosomes are grouped and counted", result.firstResult());
    }
}