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

package org.opencb.cellbase.client.grpc;

;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.GeneModel;
import org.opencb.biodata.models.core.protobuf.RegulatoryRegionModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by swaathi on 27/05/16.
 */
public class GeneGrpcClientTest {

    private CellbaseGrpcClient cellBaseGrpcClient;

    public GeneGrpcClientTest() {
        cellBaseGrpcClient = new CellbaseGrpcClient("localhost", 9090);
    }

    @Disabled
    @Test
    public void count() throws Exception {
        Long count = cellBaseGrpcClient.getGeneClient().count(new HashMap<>());
        assertEquals(57905, count.longValue());
    }

    @Disabled
    @Test
    public void first() throws Exception {
        GeneModel.Gene gene = cellBaseGrpcClient.getGeneClient().first(new HashMap<>(), new HashMap<>());
        assertNotNull(gene);
//        assertEquals("The biotype returned is wrong", "pseudogene", gene.getBiotype());
    }

    @Disabled
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
            assertNotNull(gene);
            assertTrue(gene.getBiotype().equals("protein_coding"));
            count++ ;
        }
        System.out.println(count);
    }

    @Disabled
    @Test
    public void distinct() throws Exception {
        ServiceTypesModel.StringArrayResponse values = cellBaseGrpcClient.getGeneClient().distinct(new HashMap<>(), "biotype");
        assertNotNull(values);

        values = cellBaseGrpcClient.getGeneClient().distinct(new HashMap<>(), "chromosome");
        assertNotNull(values);
    }

    @Disabled
    @Test
    public void getRegulatoryRegions() throws Exception {
        Iterator<RegulatoryRegionModel.RegulatoryRegion> regulatoryRegions = cellBaseGrpcClient.getGeneClient().getRegulatoryRegions("ENSG00000139618", new HashMap<>());
        while (regulatoryRegions.hasNext()) {
            assertNotNull(regulatoryRegions.next());
        }
    }

    @Disabled
    @Test
    public void getTranscripts() throws Exception {
        Iterator<TranscriptModel.Transcript> transcriptIterator = cellBaseGrpcClient.getGeneClient().getTranscripts("ENSG00000139618", new HashMap<>());
        while (transcriptIterator.hasNext()) {
            TranscriptModel.Transcript transcript = transcriptIterator.next();
            assertNotNull(transcript);
        }
    }

    @Disabled
    @Test
    public void getTranscriptTfbs() throws Exception {
        Iterator<TranscriptModel.TranscriptTfbs> transcriptTfbsIterator = cellBaseGrpcClient.getGeneClient().getTranscriptTfbs("ENSG00000139618", new HashMap<>());
        while (transcriptTfbsIterator.hasNext()) {
            assertNotNull(transcriptTfbsIterator.next());
        }
    }
}