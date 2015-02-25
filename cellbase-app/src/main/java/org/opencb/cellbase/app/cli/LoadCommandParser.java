package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.lib.mongodb.loader.MongoDBLoadRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

/**
 * Created by imedina on 03/02/15.
 */
public class LoadCommandParser extends CommandParser {

    private CliOptionsParser.LoadCommandOptions loadCommandOptions;

    private Path inputFile;

    private String collection;

    public LoadCommandParser(CliOptionsParser.LoadCommandOptions loadCommandOptions) {
        super(loadCommandOptions.commonOptions.logLevel, loadCommandOptions.commonOptions.verbose,
                loadCommandOptions.commonOptions.conf);

        this.loadCommandOptions = loadCommandOptions;
    }


    /**
     * Parse specific 'load' command options
     */
    public void parse() {
        checkParameters();
        LoadRunner loadRunner = new MongoDBLoadRunner(inputFile, collection, loadCommandOptions.threads);
        try {
            loadRunner.run();
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error executing loader: " + e);
        }
    }

    private void checkParameters() {
        inputFile = Paths.get(loadCommandOptions.inputFile);
        if (!inputFile.toFile().exists()) {
            throw new ParameterException("Input file " + inputFile + " doesn't exist");
        }

        if (loadCommandOptions.threads < 1) {
            throw new ParameterException("Threads number " + loadCommandOptions.threads + " not valid");
        }

        collection = loadCommandOptions.load;
        // TODO: list of available collections
    }
}
