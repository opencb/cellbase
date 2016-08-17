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

}
