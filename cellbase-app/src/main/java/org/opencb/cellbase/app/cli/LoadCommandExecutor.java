package org.opencb.cellbase.app.cli;

import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.core.loader.LoaderException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by imedina on 03/02/15.
 */
public class LoadCommandExecutor extends CommandExecutor {

    private LoadRunner loadRunner;
    private CliOptionsParser.LoadCommandOptions loadCommandOptions;

    private Path input;

    private String database;
    private String loader;
    private int numThreads;

    public LoadCommandExecutor(CliOptionsParser.LoadCommandOptions loadCommandOptions) {
        super(loadCommandOptions.commonOptions.logLevel, loadCommandOptions.commonOptions.verbose,
                loadCommandOptions.commonOptions.conf);

        this.loadCommandOptions = loadCommandOptions;

        if(loadCommandOptions.input != null) {
            input = Paths.get(loadCommandOptions.input);
        }
        if(loadCommandOptions.database != null) {
            database = loadCommandOptions.database;
        }
        if(loadCommandOptions.loader != null) {
            loader = loadCommandOptions.loader;
        }
    }


    /**
     * Parse specific 'data' command options
     */
    public void execute() {
        try {
            checkParameters();

            if (loadCommandOptions.data != null) {
                loadRunner = new LoadRunner(loader, database, loadCommandOptions.loaderParams, numThreads, configuration);

                String[] buildOptions = loadCommandOptions.data.split(",");
                for (int i = 0; i < buildOptions.length; i++) {
                    String buildOption = buildOptions[i];

                    switch (buildOption) {
                        case "genome":
                            loadRunner.load(input.resolve("genome_info.json.gz"), "genome_info");
                            loadRunner.load(input.resolve("genome_sequence.json.gz"), "genome_sequence");
                            loadRunner.index("genome_sequence");
                            break;
                        case "gene":
                            loadRunner.load(input.resolve("gene.json.gz"), "gene");
                            loadRunner.index("gene");
                            break;
                        case "variation":
                            loadVariationData();
                            break;
                        case "regulatory_region":
                            loadRunner.load(input.resolve("regulatory_region.json.gz"), "regulatory_region");
                            loadRunner.index("regulatory_region");
                            break;
                        case "protein":
                            loadRunner.load(input.resolve("protein.json.gz"), "protein");
                            loadRunner.index("protein");
                            break;
                        case "conservation":
                            break;
                        case "clinical":
                            break;
                    }

                }
            }
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LoaderException e) {
            e.printStackTrace();
        }
    }

    private void checkParameters() throws IOException {
        if (!Files.exists(input) || !Files.isDirectory(input)) {
            throw new IOException("Input parameter '" + input.toString() + "' does not exist or is not a directory");
        }

        if (loadCommandOptions.numThreads > 1) {
            numThreads = loadCommandOptions.numThreads;
        }else {
            numThreads = 1;
            logger.warn("Incorrect number of threads, it must be a positive value. This has been set to '{}'", numThreads);
        }

        try {
            Class.forName(loader);
        } catch (ClassNotFoundException e) {
            logger.error("Loader Java class '{}' does not exist", loader);
            e.printStackTrace();
            System.exit(-1);
        }
    }


    private void loadVariationData() throws NoSuchMethodException, InterruptedException, ExecutionException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IOException, LoaderException {

        DirectoryStream<Path> stream = Files.newDirectoryStream(input, new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return entry.getFileName().toString().startsWith("variation_chr");
            }
        });
        for (Path entry: stream) {
            logger.info("Loading file '{}'", entry.toString());
            loadRunner.load(input.resolve(entry.getFileName()), "variation");
        }
        loadRunner.index("variation");
    }


}
