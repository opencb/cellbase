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

import org.bson.Document;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.TranscriptTfbs;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
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
        ClientConfiguration clientConfiguration;
        try {
            clientConfiguration = ClientConfiguration.load(getClass().getResource("/client-configuration-test.yml").openStream());
            cellBaseClient = new CellBaseClient(clientConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void count() throws Exception {
        QueryResponse<Long> count = cellBaseClient.getGeneClient().count(null);
        assertEquals("Number of returned genes do not match", 57905, count.firstResult().longValue());

        count = cellBaseClient.getGeneClient().count(new Query(GeneDBAdaptor.QueryParams.BIOTYPE.key(), "protein_coding"));
        assertEquals("Number of returned protein-coding genes do not match", 20356, count.firstResult().longValue());
    }

    @Ignore
    @Test
    public void first() throws Exception {
        QueryResponse<Gene> gene = cellBaseClient.getGeneClient().first();
        assertNotNull("First gene in the collection must be returned", gene);
    }

    @Ignore
    @Test
    public void getBiotypes() throws Exception {
        QueryResponse<String> biotypes = cellBaseClient.getGeneClient().getBiotypes(null);
        assertNotNull("List of biotypes should be returned", biotypes.firstResult());

        biotypes = cellBaseClient.getGeneClient().getBiotypes(new Query(GeneDBAdaptor.QueryParams.REGION.key(), "1:65342-66500"));
        assertNull("List of biotypes in the given region is empty", biotypes.firstResult());

        biotypes = cellBaseClient.getGeneClient().getBiotypes(new Query(GeneDBAdaptor.QueryParams.REGION.key(), "1:20000-70000"));
        assertNotNull("List of biotypes in the given region", biotypes.firstResult());
    }

    @Ignore
    @Test
    public void get() throws Exception {
        QueryResponse<Gene> gene = cellBaseClient.getGeneClient().get("BRCA2", null);
        assertNotNull("This gene should exist", gene.firstResult());

        Map<String, Object> params = new HashMap<>();
        params.put("exclude", "transcripts");
        gene = cellBaseClient.getGeneClient().get("BRCA2", params);
        assertNull("This gene should not have transcritps", gene.firstResult().getTranscripts());

        gene = cellBaseClient.getGeneClient().get("NotExistingGene", null);
        assertNull("This gene should not exist", gene.firstResult());
    }

    @Ignore
    @Test
    public void list() throws Exception {
        QueryResponse<Gene> gene = cellBaseClient.getGeneClient().list(new Query("limit", 10));
        assertNotNull("List of gene Ids", gene);
    }

    @Ignore
    @Test
    public void search() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(GeneDBAdaptor.QueryParams.BIOTYPE.key(), "miRNA");
        params.put("limit", 1);
        QueryResponse<Gene> gene = cellBaseClient.getGeneClient().search(new Query(params));
//        System.out.println(gene.firstResult());
    }

    @Ignore
    @Test
    public void getProtein() throws Exception {
        QueryResponse<Entry> protein = cellBaseClient.getGeneClient().getProtein("BRCA2", null);
        assertNotNull(protein.firstResult());
    }

    @Ignore
    @Test
    public void getSnp() throws Exception {
        QueryResponse<Variant> variantQueryResponse = cellBaseClient.getGeneClient().getSnp("BRCA2", null);
        assertNotNull(variantQueryResponse.firstResult());
    }

    @Ignore
    @Test
    public void getTfbs() throws Exception {
        QueryResponse<TranscriptTfbs> tfbs = cellBaseClient.getGeneClient().getTfbs("BRCA2", null);
        assertNotNull("Tfbs of the given gene must be returned", tfbs.firstResult());
    }

    @Test
    public void getTranscript() throws Exception {
        QueryResponse<Transcript> transcript = cellBaseClient.getGeneClient().getTranscript("BRCA2", null);
        assertNotNull("Transcripts of the given gene must be returned", transcript.firstResult());

        Map<String, Object> params = new HashMap<>();
        params.put("transcripts.biotype", "protein_coding");
        transcript = cellBaseClient.getGeneClient().getTranscript("BRCA2", params);
        assertNotNull(transcript.firstResult());
        assertEquals("Number of transcripts with biotype protein_coding", 3, transcript.getResponse().get(0).getNumTotalResults());
    }

    @Ignore
    @Test
    public void getClinical() throws Exception {

        QueryResponse<Document> clinical = cellBaseClient.getGeneClient().getClinical("BRCA2", null);
        assertNotNull(clinical.firstResult());


        Map<String, Object> params = new HashMap<>();
        params.put("source", "cosmic");
        params.put("limit", 2);
        clinical = cellBaseClient.getGeneClient().getClinical("BRCA2", params);
        assertNotNull(clinical.firstResult());
    }
}