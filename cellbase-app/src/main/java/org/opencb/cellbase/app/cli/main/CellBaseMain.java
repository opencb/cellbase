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

package org.opencb.cellbase.app.cli.main;

import com.beust.jcommander.ParameterException;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.main.executors.QueryCommandExecutor;
import org.opencb.cellbase.app.cli.main.executors.VariantAnnotationCommandExecutor;
import org.opencb.cellbase.core.exception.CellbaseException;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by imedina on 03/02/15.
 */
public class CellBaseMain {

    public static void main(String[] args) {

        CellBaseCliOptionsParser cliOptionsParser = new CellBaseCliOptionsParser();
        try {
            cliOptionsParser.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            cliOptionsParser.printUsage();
            System.exit(1);
        }

        String parsedCommand = cliOptionsParser.getCommand();
        if (parsedCommand == null || parsedCommand.isEmpty()) {
            if (cliOptionsParser.getGeneralOptions().help) {
                cliOptionsParser.printUsage();
                System.exit(0);
            } else {
                if (cliOptionsParser.getGeneralOptions().version) {
                    cliOptionsParser.printVersion();
                    System.exit(0);
                } else {
                    cliOptionsParser.printUsage();
                    System.exit(1);
                }
            }
        } else {
            CommandExecutor commandExecutor = null;
            if (cliOptionsParser.isHelp()) {
                cliOptionsParser.printUsage();
                System.exit(0);
            } else {
                switch (parsedCommand) {
                    case "query":
                        commandExecutor = new QueryCommandExecutor(cliOptionsParser.getQueryCommandOptions());
                        break;
                    case "variant-annotation":
                        commandExecutor = new VariantAnnotationCommandExecutor(cliOptionsParser.getVariantAnnotationCommandOptions());
                        break;
                    default:
                        break;
                }
            }

            if (commandExecutor != null) {
                try {
                    commandExecutor.loadCellBaseConfiguration();
                    commandExecutor.execute();
                } catch (IOException | URISyntaxException | CellbaseException e) {
                    commandExecutor.getLogger().error("Error reading CellBase configuration: " + e.getMessage());
                    System.exit(1);
                }
            }
        }
    }

}
