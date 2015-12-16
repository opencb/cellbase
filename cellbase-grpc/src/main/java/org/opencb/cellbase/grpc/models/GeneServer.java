package org.opencb.cellbase.grpc.models;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.grpc.GeneModel;
import org.opencb.cellbase.grpc.GeneServiceGrpc;
import org.opencb.cellbase.grpc.GeneServiceModel;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Created by swaathi on 16/12/15.
 */
public class GeneServer {
    private static final Logger LOGGER = Logger.getLogger(GeneServer.class.getName());

    private int port = 9090;
    private Server server;

    protected static org.opencb.cellbase.core.api.DBAdaptorFactory dbAdaptorFactory2;

    private void start() throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(GeneServiceGrpc.bindService(new GeneServiceImpl()))
                .build()
                .start();
        LOGGER.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GeneServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        final GeneServer server = new GeneServer();
        server.start();
        server.blockUntilShutdown();
    }

    private class GeneServiceImpl implements GeneServiceGrpc.GeneService {
        @Override
        public void get(GeneServiceModel.Query request, StreamObserver<GeneModel.Gene> responseObserver) {
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor("hsapiens", "grch37");
            Query query = new Query();
            Iterator<Document> iterator = geneDBAdaptor.nativeIterator(new Query(), new QueryOptions());
            while (iterator.hasNext()) {
                Document document = iterator.next();
                GeneModel.Gene reply = GeneModel.Gene.newBuilder()
                        .setChromosome(document.getString("chromosome"))
                        .setBiotype(document.getString("biotype"))
                        .build();
                responseObserver.onNext(reply);
            }
            responseObserver.onCompleted();
        }
    }
}
