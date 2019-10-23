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

package org.opencb.cellbase.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.server.rest.AdminRestWebService;
import org.opencb.cellbase.server.rest.CORSFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class RestServer  {

    protected static Logger logger = LoggerFactory.getLogger("org.opencb.cellbase.server.RestServer");
    private static Server server;
    private CellBaseConfiguration configuration;
    private boolean exit;
    private int port;

    public RestServer() {
    }

    public RestServer(CellBaseConfiguration configuration) {
        this.configuration = configuration;
        init();
    }

    private void init() {
        logger = LoggerFactory.getLogger(this.getClass());
        if (configuration != null) {
            try {
                this.port = Integer.valueOf(configuration.getServer().getRest().getPort());
                logger.info("Using port: " + port);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid port number: " + configuration.getServer().getRest().getPort());
            }
        }
    }

    public void start() throws Exception {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages(true, "org.opencb.cellbase.server.rest");

        // Registering MultiPart class for POST forms
        resourceConfig.register(MultiPartFeature.class);

        ServletContainer sc = new ServletContainer(resourceConfig);
        ServletHolder sh = new ServletHolder("cellbase", sc);

        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(server, null, ServletContextHandler.SESSIONS);
        context.addServlet(sh, "/cellbase/webservices/rest/*");
        //context.setInitParameter("config-dir", configDir.toFile().toString());

        // To add CORS Java filtert class to Jetty
        context.addFilter(CORSFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR));

        server.start();
        logger.info("REST server started, listening on port: " + port + " at " + server.getURI());

        // A hook is added in case the JVM is shutting down
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    if (server.isRunning()) {
                        stopJettyServer();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // A separated thread is launched to shut down the server
        new Thread(() -> {
            try {
                while (true) {
                    if (exit) {
                        stopJettyServer();
                        break;
                    }
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // AdminWSServer server needs a reference to this class to cll to .stop()
        AdminRestWebService.setServer(this);
    }

    public void stop() throws Exception {
        // By setting exit to true the monitor thread will close the Jetty server
        exit = true;
    }

    private void stopJettyServer() throws Exception {
        // By setting exit to true the monitor thread will close the Jetty server
        logger.info("Shutting down Jetty server");
        server.stop();
        logger.info("REST server shut down");
    }

}
