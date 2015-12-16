package org.opencb.cellbase.grpc.models;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.grpc.GeneModel;
import org.opencb.cellbase.grpc.GeneServiceGrpc;
import org.opencb.cellbase.grpc.GeneServiceModel;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by swaathi on 16/12/15.
 */
public class GeneServer {

    protected static Logger logger; // = Logger.getLogger(GeneServer.class.getName());

    private int port = 9090;
    private Server server;

    protected static CellBaseConfiguration cellBaseConfiguration;
    protected static DBAdaptorFactory dbAdaptorFactory;

    static {
        logger = LoggerFactory.getLogger("org.opencb.cellbase.server.ws.GenericRestWSServer");
        logger.info("Static block, creating MongoDBAdapatorFactory");
        try {
            if (System.getenv("CELLBASE_HOME") != null) {
                logger.info("Loading configuration from '{}'", System.getenv("CELLBASE_HOME") + "/configuration.json");
                cellBaseConfiguration = CellBaseConfiguration
                        .load(new FileInputStream(new File(System.getenv("CELLBASE_HOME") + "/configuration.json")));
            } else {
                logger.info("Loading configuration from '{}'",
                        CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json").toString());
                cellBaseConfiguration = CellBaseConfiguration
                        .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
            }

            // If Configuration has been loaded we can create the DBAdaptorFactory
            dbAdaptorFactory = new org.opencb.cellbase.mongodb.impl.MongoDBAdaptorFactory(cellBaseConfiguration);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(GeneServiceGrpc.bindService(new GeneServiceImpl()))
                .build()
                .start();
        logger.info("Server started, listening on {}", port);
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
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor("hsapiens", "grch37");
            Query query = new Query();
            Iterator<Document> iterator = geneDBAdaptor.nativeIterator(new Query(), new QueryOptions());
            while (iterator.hasNext()) {
                Document document = iterator.next();
                GeneModel.Gene gene = GeneModel.Gene.newBuilder()
                        .setName(document.getString("name"))
                        .setChromosome(document.getString("chromosome"))
                        .setBiotype(document.getString("biotype"))
                        .build();
                responseObserver.onNext(gene);
            }
            responseObserver.onCompleted();
        }
    }
}
