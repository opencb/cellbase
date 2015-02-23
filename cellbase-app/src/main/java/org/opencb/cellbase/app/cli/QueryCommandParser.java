package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by imedina on 20/02/15.
 */
public class QueryCommandParser extends CommandParser {

    private CliOptionsParser.QueryCommandOptions queryCommandOptions;

    private Path inputFile;

    public QueryCommandParser(CliOptionsParser.QueryCommandOptions queryCommandOptions) {
        super(queryCommandOptions.commonOptions.logLevel, queryCommandOptions.commonOptions.verbose,
                queryCommandOptions.commonOptions.conf);

        this.queryCommandOptions = queryCommandOptions;
    }

    @Override
    public void parse() {
        checkParameters();
        if (inputFile != null && inputFile.endsWith(".vcf")) {
            annotateVcfFile();
        }
    }

    private void annotateVcfFile() {
        // TODO: implement
    }


    private void checkParameters() {
        // input file
        if (queryCommandOptions.inputFile != null) {
            inputFile = Paths.get(queryCommandOptions.inputFile);
            if (inputFile.toFile().exists()) {
                throw new ParameterException("Input file " + inputFile + " doesn't exist");
            } else if (inputFile.toFile().isDirectory()) {
                throw new ParameterException("Input file cannot be a directory: " + inputFile);
            }
        }
    }
}
