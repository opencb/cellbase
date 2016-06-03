package org.opencb.cellbase.client.rest;

import org.junit.Test;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by swaathi on 20/05/16.
 */
public class TranscriptClientTest {
    private CellBaseClient cellBaseClient;

    public TranscriptClientTest() {
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
        QueryResponse<Long> count = cellBaseClient.getTranscriptClient().count(new Query());
        assertEquals("Number of returned transcripts do not match", 196501, count.firstResult().longValue());
    }

    @Test
    public void first() throws Exception {
        QueryResponse<Transcript> transcript = cellBaseClient.getTranscriptClient().first();
        assertNotNull("First transcript in the collection must be returned", transcript);
    }

    @Test
    public void get() throws Exception {
        QueryResponse<Transcript> transcript = cellBaseClient.getTranscriptClient().get(Collections.singletonList("ENST00000456328"), null);
        assertNotNull("This transcript should exist", transcript.firstResult());

//        Map<String, Object> params = new HashMap<>();
//        params.put("exclude", "xrefs");
//        transcript = cellBaseClient.getTranscriptClient().get(Collections.singletonList("ENST00000456328"), params);
//        assertNull("This transcript should not have xrefs", transcript.firstResult().getXrefs());
    }

    @Test
    public void getGene() throws Exception {
        QueryResponse<Gene> response = cellBaseClient.getTranscriptClient().getGene("ENST00000456328", new QueryOptions());
        assertNotNull("It should return the respective gene", response.firstResult());
    }

//    @Test
    public void getVariation() throws Exception {
        QueryResponse<Variant> response = cellBaseClient.getTranscriptClient().getVariation("ENST00000456328,ENST00000528762",
                new QueryOptions(QueryOptions.EXCLUDE, "annotation"));
        assertNotNull("It should return the variations for the given transcript(s)", response.firstResult());
    }

    @Test
    public void getSequence() throws Exception {
        QueryResponse<String> seq = cellBaseClient.getTranscriptClient().getSequence("ENST00000528762", null);
        assertEquals("The sequence returned is wrong", "TCATCTGGATTATACATATTTCGCAATGAAAGAGAGGAAGAAAAGGAAGCAGCAAAATATGTG" +
                "GAGGCCCAACAAAAGAGACTAGAAGCCTTATTCACTAAAATTCAGGAGGAATTTGAAGAACATGAAGTTACTTCCTCCACTGAAGTCTTGAACCCCCCAA" +
                "AGTCATCCATGAGGGTTGGAATCAACTTCTGAAAACACAACAAAACCATATTTACCATCACGTGCACTAACAAGACAGCAAGTTCGTGCTTTGCAAGATGG" +
                "TGCAGAGCTTTATGAAGCAGTGAAGAATGCAGCAGACCCAGCTTACCTTGAGGGTTATTTCAGTGAAGAGCAGTTAAGAGCCTTGAATAATCACAGGCAAATG" +
                "TTGAATGATAAGAAACAAGCTCAGATCCAGTTGGAAATTAGGAAGGCCATGGAATCTGCTGAACAAAAGGAACAAGGTTTATCAAGGGATGTCACAACCGTGT" +
                "GGAAGTTGCGTATTGTAAGCTATTC", seq.firstResult());

    }

    @Test
    public void getProtein() throws Exception {
        QueryResponse<Entry> response = cellBaseClient.getTranscriptClient().getProtein("ENST00000342992", null);
        assertNotNull("Protein for the given transcript must be returned", response.firstResult());
    }

//    @Test
//    public void getFunctionPrediction() throws Exception {
//        QueryResponse<List> response = cellBaseClient.getTranscriptClient().getProteinFunctionPrediction("ENST00000530893", null);
//        assertNotNull("The function prediction for the given trnascript must be returned", response.firstResult());
//    }
}