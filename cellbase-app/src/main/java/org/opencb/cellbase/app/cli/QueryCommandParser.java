package org.opencb.cellbase.app.cli;

/**
 * Created by imedina on 20/02/15.
 */
public class QueryCommandParser extends CommandParser {

    private CliOptionsParser.QueryCommandOptions queryCommandOptions;

    public QueryCommandParser(CliOptionsParser.QueryCommandOptions queryCommandOptions) {
        super(queryCommandOptions.commonOptions.logLevel, queryCommandOptions.commonOptions.verbose,
                queryCommandOptions.commonOptions.conf);

        this.queryCommandOptions = queryCommandOptions;
    }

    @Override
    public void parse() {
        logger.info("In Query command");
    }
}
