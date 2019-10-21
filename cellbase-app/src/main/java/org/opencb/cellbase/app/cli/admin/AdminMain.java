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

package org.opencb.cellbase.app.cli.admin;

import com.beust.jcommander.ParameterException;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.executors.BuildCommandExecutor;
import org.opencb.cellbase.app.cli.admin.executors.DownloadCommandExecutor;
import org.opencb.cellbase.app.cli.admin.executors.LoadCommandExecutor;
import org.opencb.cellbase.app.cli.admin.executors.PostLoadCommandExecutor;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by imedina on 03/02/15.
 */
public class AdminMain {

    public static void main(String[] args) {

        AdminCliOptionsParser cliOptionsParser = new AdminCliOptionsParser();
        try {
            cliOptionsParser.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            cliOptionsParser.printUsage();
            System.exit(1);
        }

        String parsedCommand = cliOptionsParser.getCommand();
        if (StringUtils.isEmpty(parsedCommand)) {
            if (cliOptionsParser.getGeneralOptions().version) {
                cliOptionsParser.printVersion();
                System.exit(0);
            } else {
                cliOptionsParser.printUsage();
                System.exit(0);
            }
//            if (cliOptionsParser.getGeneralOptions().help) {
//                cliOptionsParser.printUsage();
//                System.exit(0);
//            } else {
//                if (cliOptionsParser.getGeneralOptions().version) {
//                    cliOptionsParser.printVersion();
//                    System.exit(0);
//                } else {
//                    cliOptionsParser.printUsage();
//                    System.exit(1);
//                }
//            }
        } else {
            CommandExecutor commandExecutor = null;
            if (cliOptionsParser.isHelp()) {
                cliOptionsParser.printUsage();
                System.exit(0);
            } else {
                switch (parsedCommand) {
                    case "download":
                        commandExecutor = new DownloadCommandExecutor(cliOptionsParser.getDownloadCommandOptions());
                        break;
                    case "build":
                        commandExecutor = new BuildCommandExecutor(cliOptionsParser.getBuildCommandOptions());
                        break;
                    case "load":
                        commandExecutor = new LoadCommandExecutor(cliOptionsParser.getLoadCommandOptions());
                        break;
                    case "post-load":
                        commandExecutor = new PostLoadCommandExecutor(cliOptionsParser.getPostLoadCommandOptions());
                        break;
                    default:
                        break;
                }
            }

            if (commandExecutor != null) {
                try {
                    commandExecutor.loadCellBaseConfiguration();
                    commandExecutor.execute();
                } catch (IOException | URISyntaxException e) {
                    commandExecutor.getLogger().error("Error reading CellBase configuration: " + e.getMessage());
                    System.exit(1);
                }
            }
        }
    }

}
