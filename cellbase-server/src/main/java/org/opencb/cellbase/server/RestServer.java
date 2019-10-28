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
import org.eclipse.jetty.webapp.WebAppContext;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.server.rest.AdminRestWebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class RestServer  {

    private static Server server;
    private Path cellbaseHome;
    private boolean exit;
    private int port;

    private static Logger logger;

    public RestServer(Path cellbaseHome) {
        this(cellbaseHome, 0);
    }

    public RestServer(Path cellbaseHome, int port) {
        this.cellbaseHome = cellbaseHome;
        this.port = port;

        init();
    }


    private void init() {
        logger = LoggerFactory.getLogger(this.getClass());

        try {
            CellBaseConfiguration configuration = CellBaseConfiguration.load(cellbaseHome.resolve("conf").resolve("configuration.yml"));
            this.port = (this.port == 0) ? configuration.getServer().getRest().getPort() : this.port;
        } catch (IOException e) {
            throw new RuntimeException("Invalid CellBase home: " + cellbaseHome.toString());
        }
    }

    public void start() throws Exception {
        server = new Server(port);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/cellbase");
        Optional<Path> warPath = Files.list(cellbaseHome)
                .filter(path -> path.toString().endsWith("war"))
                .findFirst();
        // Check is a war file has been found in cellbaseHome
        if (!warPath.isPresent()) {
            throw new Exception("No war file found at: " + cellbaseHome.toString());
        }
        webapp.setWar(warPath.get().toString());
        webapp.setInitParameter("CELLBASE_HOME", cellbaseHome.toFile().toString());
        server.setHandler(webapp);

        server.start();
        logger.info("REST server started, listening on port: " + port + " at " + server.getURI());

        // A hook is added in case the JVM is shutting down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (server.isRunning()) {
                    stopJettyServer();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

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
        logger.info("Stopping REST server ...");
        server.stop();
        logger.info("REST server is shutdown");
    }

}
