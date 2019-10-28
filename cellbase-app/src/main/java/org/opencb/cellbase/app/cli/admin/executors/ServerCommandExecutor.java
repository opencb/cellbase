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

package org.opencb.cellbase.app.cli.admin.executors;

import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.server.RestServer;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.nio.file.Paths;

public class ServerCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.ServerCommandOptions serverCommandOptions;

    public ServerCommandExecutor(AdminCliOptionsParser.ServerCommandOptions serverCommandOptions) {
        super(serverCommandOptions.commonOptions.logLevel, serverCommandOptions.commonOptions.verbose,
                serverCommandOptions.commonOptions.conf);
        this.serverCommandOptions = serverCommandOptions;
    }

    @Override
    public void execute() {
        int port = (serverCommandOptions.port == 0) ? configuration.getServer().getRest().getPort() : serverCommandOptions.port;

        if (serverCommandOptions.start) {
            RestServer server = new RestServer(Paths.get(this.appHome), port);
            try {
                server.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (serverCommandOptions.stop) {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:" + port)
                    .path("cellbase")
                    .path("webservices")
                    .path("rest")
                    .path("admin")
                    .path("stop");
            Response response = target.request().get();
            logger.info(response.toString());
        }
    }
}
