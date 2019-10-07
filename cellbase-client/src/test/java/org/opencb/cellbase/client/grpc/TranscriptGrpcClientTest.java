/*
 * Copyright 2015-2019 OpenCB
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

import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.GeneModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by swaathi on 17/08/16.
 */
public class TranscriptGrpcClientTest {

    private CellbaseGrpcClient cellbaseGrpcClient;

    public TranscriptGrpcClientTest() {
        cellbaseGrpcClient = new CellbaseGrpcClient("localhost", 9090);
    }

    @Ignore
    @Test
    public void count() throws Exception {
        Long count = cellbaseGrpcClient.getTranscriptClient().count(new HashMap<>());
        assertEquals("Number of transcripts does not match", 196501, count.longValue());
        System.out.println(count.longValue());
    }

    @Ignore
    @Test
    public void first() throws Exception {
        TranscriptModel.Transcript transcript = cellbaseGrpcClient.getTranscriptClient().first(new HashMap<>(), new HashMap<>());
        assertNotNull("First transcript exists and it must be returned", transcript);
//        assertEquals("The biotype returned is wrong", "processed_transcript", transcript.getBiotype());
    }

    @Ignore
    @Test
    public void get() throws Exception {
        Map<String, String> query = new HashMap<>();
        query.put("id", "ENST00000456328");
        Iterator<TranscriptModel.Transcript> transcriptIterator = cellbaseGrpcClient.getTranscriptClient().get(query, new HashMap<>());
        int count = 0;
        while (transcriptIterator.hasNext()) {
            TranscriptModel.Transcript next = transcriptIterator.next();
            assertEquals("The biotype returned is wrong", "processed_transcript", next.getBiotype());
            count++;
        }
        System.out.println(count);
    }

    @Ignore
    @Test
    public void distinct() throws Exception {

    }

    @Ignore
    @Test
    public void getSequence() throws Exception {
        Map<String, String> query = new HashMap<>();
        query.put("id", "ENST00000456328");
        ServiceTypesModel.StringResponse sequence = cellbaseGrpcClient.getTranscriptClient().getSequence(query);
        assertTrue("The sequence returned is wrong", sequence.getValue().startsWith("GTTAACTTGCCGTCAGCCTTTTCTTT"));
    }
}