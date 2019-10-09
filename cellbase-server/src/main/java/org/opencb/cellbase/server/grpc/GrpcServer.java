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

package org.opencb.cellbase.server.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by imedina on 16/12/15.
 */
public class GrpcServer {

    private Server server;
    private int port = 9090;

    private static CellBaseConfiguration cellBaseConfiguration;
    protected static DBAdaptorFactory dbAdaptorFactory;

    protected static Logger logger; // = Logger.getLogger(GeneServer.class.getName());

    static {
        logger = LoggerFactory.getLogger("org.opencb.cellbase.server.grpc.GenericGrpcServer");
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
            dbAdaptorFactory = new org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory(cellBaseConfiguration);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(new GeneGrpcService(dbAdaptorFactory))
                .addService(new TranscriptGrpcService(dbAdaptorFactory))
                .addService(new VariantGrpcService(dbAdaptorFactory))
                .addService(new RegulatoryGrpcService(dbAdaptorFactory))
                .addService(new VariantAnnotationGrpcService(dbAdaptorFactory))
                .addService(new GenomicRegionGrpcService(dbAdaptorFactory))
                .build()
                .start();

        logger.info("Server started, listening on {}", port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GrpcServer.this.stop();
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
        final GrpcServer server = new GrpcServer();
        server.start();
        server.blockUntilShutdown();
    }

}
