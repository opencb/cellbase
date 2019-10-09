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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by swaathi on 27/05/16.
 */
public class CellbaseGrpcClient {

    private Map<String, ParentGrpcClient> clients;

    private ManagedChannel channel;

    public CellbaseGrpcClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();

        clients = new HashMap<>();
    }

    public GeneGrpcClient getGeneClient() {
        clients.putIfAbsent("GENE", new GeneGrpcClient(channel));
        return (GeneGrpcClient) clients.get("GENE");
    }

    public TranscriptGrpcClient getTranscriptClient() {
        clients.putIfAbsent("TRANSCRIPT", new TranscriptGrpcClient(channel));
        return (TranscriptGrpcClient) clients.get("TRANSCRIPT");
    }

    public VariationGrpcClient getVariationClient() {
        clients.putIfAbsent("VARIATION", new VariationGrpcClient(channel));
        return (VariationGrpcClient) clients.get("VARIATION");
    }

    public RegulatoryGrpcClient getRegulatoryRegionClient() {
        clients.putIfAbsent("REGULATORY_REGION", new RegulatoryGrpcClient(channel));
        return (RegulatoryGrpcClient) clients.get("REGULATORY_REGION");
    }

    public VariantAnnotationGrpcClient getVariantAnnotationGrpcClient() {
        clients.putIfAbsent("VARIANT_ANNOTATION", new VariantAnnotationGrpcClient(channel));
        return (VariantAnnotationGrpcClient) clients.get("VARIANT_ANNOTATION");
    }

    public GenomicRegionGrpcClient getGenomicRegionClient() {
        clients.putIfAbsent("GENOMIC_REGION", new GenomicRegionGrpcClient(channel));
        return (GenomicRegionGrpcClient) clients.get("GENOMIC_REGION");
    }

}
