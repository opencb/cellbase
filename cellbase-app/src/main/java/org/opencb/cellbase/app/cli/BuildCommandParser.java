package org.opencb.cellbase.app.cli;

/**
 * Created by imedina on 03/02/15.
 */
public class BuildCommandParser extends CommandParser {

    private CliOptionsParser.BuildCommandOptions buildCommandOptions;

    public BuildCommandParser(CliOptionsParser.BuildCommandOptions buildCommandOptions) {
        super(buildCommandOptions.commonOptions.logLevel, buildCommandOptions.commonOptions.verbose,
                buildCommandOptions.commonOptions.conf);

        this.buildCommandOptions = buildCommandOptions;
    }


    /**
     * Parse specific 'build' command options
     */
    public void parse() {

        if(buildCommandOptions.build != null) {
            System.out.println(buildCommandOptions.build);
        }

    }
}
