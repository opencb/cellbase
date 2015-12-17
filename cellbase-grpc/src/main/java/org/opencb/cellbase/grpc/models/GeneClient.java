package org.opencb.cellbase.grpc.models;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.opencb.cellbase.grpc.GeneModel;
import org.opencb.cellbase.grpc.GeneServiceGrpc;
import org.opencb.cellbase.grpc.GenericServiceModel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by swaathi on 16/12/15.
 */
@Deprecated
public class GeneClient {
    private static final Logger LOGGER = Logger.getLogger(GeneClient.class.getName());

    private final ManagedChannel channel;
    private final GeneServiceGrpc.GeneServiceBlockingStub geneServiceBlockingStub;

    public GeneClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();
        geneServiceBlockingStub = GeneServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void getGene(String species, String assembly) {
        try {
            LOGGER.info("fetching gene info");
            Map<String, String> query = new HashMap<>();
            query.put("biotype", "lincRNA");

            GenericServiceModel.Request request = GenericServiceModel.Request.newBuilder()
                    .setSpecies(species)
                    .setAssembly(assembly)
                    .putAllQuery(query)
                    .build();

            Iterator<GeneModel.Gene> geneIterator = geneServiceBlockingStub.get(request);
            while (geneIterator.hasNext()) {
                GeneModel.Gene next = geneIterator.next();
                System.out.println(next.toString());
            }
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
