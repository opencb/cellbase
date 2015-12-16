package org.opencb.cellbase.grpc.models;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.opencb.cellbase.grpc.GeneModel;
import org.opencb.cellbase.grpc.GeneServiceGrpc;
import org.opencb.cellbase.grpc.GeneServiceModel;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by swaathi on 16/12/15.
 */
public class GeneClient {
    private static final Logger LOGGER = Logger.getLogger(GeneClient.class.getName());

    private final ManagedChannel channel;
    private final GeneServiceGrpc.GeneServiceBlockingStub geneServiceBlockingStub;

    public GeneClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .build();
        geneServiceBlockingStub = GeneServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void getGene(String species, String query) {
        try {
            LOGGER.info("fetching gene info");
            GeneServiceModel.Query request = GeneServiceModel.Query.newBuilder()
//                    .setSpecies(species)
//                    .setName(query)
                    .build();
            Iterator<GeneModel.Gene> response = geneServiceBlockingStub.get(request);
            LOGGER.info(String.valueOf(response));
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "RPC failed", e);
            return;
        }
    }

    public static void main(String[] args) throws Exception {
        GeneClient client = new GeneClient("localhost", 9090);
        try {
            client.getGene(args[0], args[1]);
        } finally {
            client.shutdown();
        }
    }
}
