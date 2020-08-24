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
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.CellBaseDataResponse;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;


import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by swaathi on 20/05/16.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TranscriptClientTest {
    private CellBaseClient cellBaseClient;

    public TranscriptClientTest() {
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
    public void count() throws Exception {
        CellBaseDataResponse<Long> count = cellBaseClient.getTranscriptClient().count(new Query());
        assertEquals(196501, count.firstResult().longValue(), "Number of returned transcripts do not match");
    }

//    @Test
//    public void first() throws Exception {
//        CellBaseDataResponse<Transcript> transcript = cellBaseClient.getTranscriptClient().first();
//        assertNotNull(transcript, "First transcript in the collection must be returned");
//    }

    @Test
    public void get() throws Exception {
        CellBaseDataResponse<Transcript> transcript = cellBaseClient.getTranscriptClient().get(Collections.singletonList("ENST00000456328"), null);
        assertNotNull(transcript.firstResult(), "This transcript should exist");

//        Map<String, Object> params = new HashMap<>();
//        params.put("exclude", "xrefs");
//        transcript = cellBaseClient.getTranscriptClient().get(Collections.singletonList("ENST00000456328"), params);
//        assertNull("This transcript should not have xrefs", transcript.firstResult().getXrefs());
    }

    @Test
    public void getGene() throws Exception {
        CellBaseDataResponse<Gene> response = cellBaseClient.getTranscriptClient().getGene("ENST00000456328", new QueryOptions());
        assertNotNull(response.firstResult(), "It should return the respective gene");
    }

//    @Test
    public void getVariation() throws Exception {
        CellBaseDataResponse<Variant> response = cellBaseClient.getTranscriptClient().getVariation("ENST00000456328,ENST00000528762",
                new QueryOptions(QueryOptions.EXCLUDE, "annotation"));
        assertNotNull(response.firstResult(), "It should return the variations for the given transcript(s)");
    }

    @Test
    public void getSequence() throws Exception {
        CellBaseDataResponse<String> seq = cellBaseClient.getTranscriptClient().getSequence("ENST00000528762", null);
        assertEquals("The sequence returned is wrong", "TCATCTGGATTATACATATTTCGCAATGAAAGAGAGGAAGAAAAGGAAGCAGCAAAATATGTG" +
                "GAGGCCCAACAAAAGAGACTAGAAGCCTTATTCACTAAAATTCAGGAGGAATTTGAAGAACATGAAGTTACTTCCTCCACTGAAGTCTTGAACCCCCCAA" +
                "AGTCATCCATGAGGGTTGGAATCAACTTCTGAAAACACAACAAAACCATATTTACCATCACGTGCACTAACAAGACAGCAAGTTCGTGCTTTGCAAGATGG" +
                "TGCAGAGCTTTATGAAGCAGTGAAGAATGCAGCAGACCCAGCTTACCTTGAGGGTTATTTCAGTGAAGAGCAGTTAAGAGCCTTGAATAATCACAGGCAAATG" +
                "TTGAATGATAAGAAACAAGCTCAGATCCAGTTGGAAATTAGGAAGGCCATGGAATCTGCTGAACAAAAGGAACAAGGTTTATCAAGGGATGTCACAACCGTGT" +
                "GGAAGTTGCGTATTGTAAGCTATTC", seq.firstResult());

    }

    @Test
    public void getProtein() throws Exception {
        CellBaseDataResponse<Entry> response = cellBaseClient.getTranscriptClient().getProtein("ENST00000342992", null);
        assertNotNull(response.firstResult(), "Protein for the given transcript must be returned");
    }

//    @Test
//    public void getFunctionPrediction() throws Exception {
//        CellBaseDataResponse<List> response = cellBaseClient.getTranscriptClient().getProteinFunctionPrediction("ENST00000530893", null);
//        assertNotNull("The function prediction for the given trnascript must be returned", response.firstResult());
//    }
}