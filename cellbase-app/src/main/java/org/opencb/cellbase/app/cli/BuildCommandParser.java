package org.opencb.cellbase.app.cli;

/**
 * Created by imedina on 03/02/15.
 */
public class BuildCommandParser extends CommandParser {

    private String input = null;
    private String output = null;

    private CliOptionsParser.BuildCommandOptions buildCommandOptions;

    public BuildCommandParser(CliOptionsParser.BuildCommandOptions buildCommandOptions) {
        super(buildCommandOptions.commonOptions.logLevel, buildCommandOptions.commonOptions.verbose,
                buildCommandOptions.commonOptions.conf);

        this.buildCommandOptions = buildCommandOptions;
        if(buildCommandOptions.input != null) {
            input = buildCommandOptions.input;
        }
        if(buildCommandOptions.output != null) {
            output = buildCommandOptions.output;
        }
    }


    /**
     * Parse specific 'build' command options
     */
    public void parse() {

        // Check params

        // If everything is fine
        if(buildCommandOptions.build != null) {
//            logger.debug(Arrays.toString(buildCommandOptions.build));
            for(String build: buildCommandOptions.build) {

                switch(build) {
                    case "sequence":
                        break;
                }

            }
        }

    }
}
