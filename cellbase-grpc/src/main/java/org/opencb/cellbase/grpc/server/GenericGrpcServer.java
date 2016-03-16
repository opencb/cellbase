/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.grpc.service.*;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by imedina on 16/12/15.
 */
public class GenericGrpcServer {

    private Server server;
    private int port = 9090;

    private static CellBaseConfiguration cellBaseConfiguration;
    protected static DBAdaptorFactory dbAdaptorFactory;

    protected static Logger logger; // = Logger.getLogger(GeneServer.class.getName());

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
                .addService(GeneServiceGrpc.bindService(new GeneGrpcServer()))
                .addService(TranscriptServiceGrpc.bindService(new TranscriptGrpcServer()))
                .addService(VariantServiceGrpc.bindService(new VariantGrpcServer()))
                .addService(RegulatoryRegionServiceGrpc.bindService(new RegulatoryGrpcServer()))
                .build()
                .start();
        logger.info("Server started, listening on {}", port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GenericGrpcServer.this.stop();
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


    protected Query createQuery(GenericServiceModel.Request request) {
        Query query = new Query();
        for (String key : request.getQuery().keySet()) {
            if (request.getQuery().get(key) != null) {
                query.put(key, request.getQuery().get(key));
            }
        }
        return query;
    }

    protected QueryOptions createQueryOptions(GenericServiceModel.Request request) {
        QueryOptions queryOptions = new QueryOptions();
        for (String key : request.getOptions().keySet()) {
            if (request.getOptions().get(key) != null) {
                queryOptions.put(key, request.getOptions().get(key));
            }
        }
        return queryOptions;
    }


    public static void main(String[] args) throws Exception {
        final GenericGrpcServer server = new GenericGrpcServer();
        server.start();
        server.blockUntilShutdown();
    }

}
