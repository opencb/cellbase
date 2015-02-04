package org.opencb.cellbase.app.cli;

/**
 * Created by imedina on 03/02/15.
 */
public class LoadCommandParser extends CommandParser {

    private CliOptionsParser.LoadCommandOptions loadCommandOptions;

    public LoadCommandParser(CliOptionsParser.LoadCommandOptions loadCommandOptions) {
        super(loadCommandOptions.commonOptions.logLevel, loadCommandOptions.commonOptions.verbose,
                loadCommandOptions.commonOptions.conf);

        this.loadCommandOptions = loadCommandOptions;
    }


    /**
     * Parse specific 'download' command options
     */
    public void parse() {

        if (loadCommandOptions.load != null) {
            System.out.println(loadCommandOptions.load);
        }

    }
}
