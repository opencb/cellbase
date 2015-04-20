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

package org.opencb.cellbase.app;

import org.opencb.cellbase.app.cli.*;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by imedina on 03/02/15.
 */
public class CellBaseMain {

    public static final String VERSION = "3.1.0-RC";

    public static void main(String[] args) {
        CliOptionsParser cliOptionsParser = new CliOptionsParser();
        cliOptionsParser.parse(args);

        String parsedCommand = cliOptionsParser.getCommand();
        if(parsedCommand == null || parsedCommand.isEmpty()) {
            if(cliOptionsParser.getGeneralOptions().help) {
                cliOptionsParser.printUsage();
            }
            if(cliOptionsParser.getGeneralOptions().version) {
                System.out.println("Version " + VERSION);
            }
        }else {
            CommandExecutor commandExecutor = null;
            switch (parsedCommand) {
                case "download":
                    if (cliOptionsParser.getDownloadCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandExecutor = new DownloadCommandExecutor(cliOptionsParser.getDownloadCommandOptions());
                    }
                    break;
                case "build":
                    if (cliOptionsParser.getBuildCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandExecutor = new BuildCommandExecutor(cliOptionsParser.getBuildCommandOptions());
                    }
                    break;
                case "load":
                    if (cliOptionsParser.getLoadCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandExecutor = new LoadCommandExecutor(cliOptionsParser.getLoadCommandOptions());
                    }
                    break;
                case "query":
                    if (cliOptionsParser.getQueryCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandExecutor = new QueryCommandExecutor(cliOptionsParser.getQueryCommandOptions());
                    }
                    break;
                case "variant-annotation":
                    if (cliOptionsParser.getVariantAnnotationCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandExecutor = new VariantAnnotationCommandExecutor(cliOptionsParser.getVariantAnnotationCommandOptions());
                    }
                    break;
                case "post-load":
                    if (cliOptionsParser.getVariantAnnotationCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandExecutor = new PostLoadCommandExecutor(cliOptionsParser.getPostLoadCommandOptions());
                    }
                    break;
                default:
                    break;
            }

            if (commandExecutor != null) {
                try {
                    commandExecutor.loadCellBaseConfiguration();
                    commandExecutor.execute();
                } catch (IOException | URISyntaxException e) {
                    commandExecutor.getLogger().error("Error reading CellBase configuration: " + e.getMessage());
                }
            }
        }
    }

}
