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

package org.opencb.cellbase.app.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.opencb.cellbase.core.common.GitRepositoryState;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.commons.utils.CommandLineUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Created by imedina on 03/02/15.
 */
public abstract class CliOptionsParser {

    protected final JCommander jCommander;

    protected final GeneralOptions generalOptions;

    public CliOptionsParser() {
        generalOptions = new GeneralOptions();

        jCommander = new JCommander(generalOptions);
    }

    public void parse(String[] args) throws ParameterException {
        jCommander.parse(args);
    }

    public String getCommand() {
        return (jCommander.getParsedCommand() != null) ? jCommander.getParsedCommand(): "";
    }

    public class GeneralOptions {

        @Parameter(names = {"-h", "--help"}, description = "Display this help and exit", help = true)
        public boolean help;

        @Parameter(names = {"--version"}, description = "Display the version and exit")
        public boolean version;

    }

    public class CommonCommandOptions {

        @Parameter(names = {"-h", "--help"}, description = "Display this help and exit", help = true)
        public boolean help;

        @Parameter(names = {"-L", "--log-level"}, description = "Set the logging level, accepted values are: debug, info, warn, error and fatal", required = false, arity = 1)
        public String logLevel = "info";

        @Deprecated
        @Parameter(names = {"-v", "--verbose"}, description = "[Deprecated] Set the level of the logging", required = false, arity = 1)
        public boolean verbose;

        @Parameter(names = {"-C", "--config"}, description = "CellBase configuration.json file. Have a look at cellbase/cellbase-core/src/main/resources/configuration.json for an example", required = false, arity = 1)
        public String conf;

    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public void printUsage() {
        if (getCommand().isEmpty()) {
            System.err.println("");
            System.err.println("Program:     " + ANSI_WHITE + "CellBase (OpenCB)" + ANSI_RESET);
//            System.err.println("Version:     " + getAPIVersion());
            System.err.println("Version:     " + GitRepositoryState.get().getBuildVersion());
            System.out.println("Git version: " + GitRepositoryState.get().getBranch() + " " + GitRepositoryState.get().getCommitId());
            System.err.println("Description: High-Performance NoSQL database and RESTful web services to access the most relevant biological data");
            System.err.println("");
            System.err.println("Usage:       cellbase.sh [-h|--help] [--version] <command> [options]");
            System.err.println("");
            System.err.println("Commands:");
            printMainUsage();
            System.err.println("");
        } else {
            String parsedCommand = getCommand();
            System.err.println("");
            System.err.println("Usage:   cellbase.sh " + parsedCommand + " [options]");
            System.err.println("");
            System.err.println("Options:");
            CommandLineUtils.printCommandUsage(jCommander.getCommands().get(parsedCommand));
            System.err.println("");
        }
    }

    public void printVersion() {
        System.err.println("");
        System.err.println("Program:     " + ANSI_WHITE + "CellBase (OpenCB)" + ANSI_RESET);
        System.err.println("Version:     " + getAPIVersion());
        System.err.println("");
    }

    @Deprecated
    protected String getAPIVersion() {
        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();
        try {
            cellBaseConfiguration = CellBaseConfiguration
                    .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cellBaseConfiguration.getApiVersion();
    }

    protected void printMainUsage() {
        for (String s : jCommander.getCommands().keySet()) {
            System.err.printf("%20s  %s\n", s, jCommander.getCommandDescription(s));
        }
    }

    public GeneralOptions getGeneralOptions() {
        return generalOptions;
    }



    public String getSubCommand() {
        return getSubCommand(jCommander);
    }

    public static String getSubCommand(JCommander jCommander) {
        String parsedCommand = jCommander.getParsedCommand();
        if (jCommander.getCommands().containsKey(parsedCommand)) {
            String subCommand = jCommander.getCommands().get(parsedCommand).getParsedCommand();
            return subCommand != null ? subCommand: "";
        } else {
            return null;
        }
    }

    public abstract boolean isHelp();


    protected void printCommands(JCommander commander) {
        int pad = commander.getCommands().keySet().stream().mapToInt(String::length).max().orElse(0);
        // Set padding between 14 and 40
        pad = Math.max(14, pad);
        pad = Math.min(40, pad);
        for (Map.Entry<String, JCommander> entry : commander.getCommands().entrySet()) {
            System.err.printf("%" + pad + "s  %s\n", entry.getKey(), commander.getCommandDescription(entry.getKey()));
        }
    }

    public JCommander getJCommander() {
        return jCommander;
    }

}
