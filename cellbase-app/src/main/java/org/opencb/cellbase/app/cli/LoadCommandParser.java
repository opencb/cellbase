package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;
import org.opencb.cellbase.core.loader.LoadRunner;

import java.lang.reflect.InvocationTargetException;
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

//        LoadRunner loadRunner = new LoadRunner(inputFile, collection, loadCommandOptions.threads);
        LoadRunner loadRunner = new LoadRunner(inputFile, loadCommandOptions.threads, loadCommandOptions.load, loadCommandOptions.loader, loadCommandOptions.loaderParams);

        try {
            loadRunner.run();
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error executing loader: " + e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
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
