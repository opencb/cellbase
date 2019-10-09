package org.opencb.cellbase.client.grpc;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.GeneModel;
import org.opencb.biodata.models.core.protobuf.RegulatoryRegionModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by swaathi on 27/05/16.
 */
public class GeneGrpcClientTest {

    private CellbaseGrpcClient cellBaseGrpcClient;

    public GeneGrpcClientTest() {
        cellBaseGrpcClient = new CellbaseGrpcClient("localhost", 9090);
    }

    @Ignore
    @Test
    public void count() throws Exception {
        Long count = cellBaseGrpcClient.getGeneClient().count(new HashMap<>());
        assertEquals("Number of genes does not match", 57905, count.longValue());
    }

    @Ignore
    @Test
    public void first() throws Exception {
        GeneModel.Gene gene = cellBaseGrpcClient.getGeneClient().first(new HashMap<>(), new HashMap<>());
        assertNotNull("First gene exists and it must be returned", gene);
//        assertEquals("The biotype returned is wrong", "pseudogene", gene.getBiotype());
    }

    @Ignore
    @Test
    public void get() throws Exception {
        Map<String, String> query = new HashMap<>();
        query.put("biotype", "protein_coding");
        Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("limit", "5");
        Iterator<GeneModel.Gene> geneIterator = cellBaseGrpcClient.getGeneClient().get(query, queryOptions);
        int count = 0;
        while (geneIterator.hasNext()) {
            GeneModel.Gene gene = geneIterator.next();
            assertNotNull("Genes returned must not be null", gene);
            assertTrue("Returned genes are of the given biotype - protein_coding", gene.getBiotype().equals("protein_coding"));
            count++ ;
        }
        System.out.println(count);
    }

    @Ignore
    @Test
    public void distinct() throws Exception {
        ServiceTypesModel.StringArrayResponse values = cellBaseGrpcClient.getGeneClient().distinct(new HashMap<>(), "biotype");
        assertNotNull("All existing biotypes should be returned", values);

        values = cellBaseGrpcClient.getGeneClient().distinct(new HashMap<>(), "chromosome");
        assertNotNull("All Chromosomes must be returned", values);
    }

    @Ignore
    @Test
    public void getRegulatoryRegions() throws Exception {
        Iterator<RegulatoryRegionModel.RegulatoryRegion> regulatoryRegions = cellBaseGrpcClient.getGeneClient().getRegulatoryRegions("ENSG00000139618", new HashMap<>());
        while (regulatoryRegions.hasNext()) {
            assertNotNull("Regulatory Regions of the given gene must be returned", regulatoryRegions.next());
        }
    }

    @Ignore
    @Test
    public void getTranscripts() throws Exception {
        Iterator<TranscriptModel.Transcript> transcriptIterator = cellBaseGrpcClient.getGeneClient().getTranscripts("ENSG00000139618", new HashMap<>());
        while (transcriptIterator.hasNext()) {
            TranscriptModel.Transcript transcript = transcriptIterator.next();
            assertNotNull("Transcripts of BRCA2 must not be null", transcript);
        }
    }

    @Ignore
    @Test
    public void getTranscriptTfbs() throws Exception {
        Iterator<TranscriptModel.TranscriptTfbs> transcriptTfbsIterator = cellBaseGrpcClient.getGeneClient().getTranscriptTfbs("ENSG00000139618", new HashMap<>());
        while (transcriptTfbsIterator.hasNext()) {
            assertNotNull("TranscriptTfbs of the given gene must be returned", transcriptTfbsIterator.next());
        }
    }
}