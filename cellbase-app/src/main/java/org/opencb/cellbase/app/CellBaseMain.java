package org.opencb.cellbase.app;

import com.beust.jcommander.ParameterException;
import org.opencb.cellbase.app.cli.*;

import java.io.IOException;
import java.net.URISyntaxException;

public class CellBaseMain {

    public static void main(String[] args) {

        CliOptionsParser cliOptionsParser = new CliOptionsParser();
        cliOptionsParser.parse(args);

        String parsedCommand = cliOptionsParser.getCommand();
        if (parsedCommand == null || parsedCommand.isEmpty()) {
            if (cliOptionsParser.getGeneralOptions().help) {
                cliOptionsParser.printUsage();
            }
            if (cliOptionsParser.getGeneralOptions().version) {
                System.out.println("version = 3.1.0");
            }
        } else {
            CommandParser commandParser = null;
            switch (parsedCommand) {
                case "download":
                    if (cliOptionsParser.getDownloadCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandParser = new DownloadCommandParser(cliOptionsParser.getDownloadCommandOptions());
                    }
                    break;
                case "build":
                    if (cliOptionsParser.getBuildCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandParser = new BuildCommandParser(cliOptionsParser.getBuildCommandOptions());
                    }
                    break;
                case "load":
                    if (cliOptionsParser.getLoadCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandParser = new LoadCommandParser(cliOptionsParser.getLoadCommandOptions());
                    }
                    break;
                case "query":
                    if (cliOptionsParser.getQueryCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandParser = new LoadCommandParser(cliOptionsParser.getLoadCommandOptions());
                    }
                    break;
                default:
                    break;
            }

            if (commandParser != null) {
                try {
                    commandParser.readCellBaseConfiguration();
                    commandParser.parse();
                } catch (IOException | URISyntaxException ex) {
                    commandParser.getLogger().error("Error reading cellbase configuration: " + ex.getMessage());
                }
            }
        }
    }

}
