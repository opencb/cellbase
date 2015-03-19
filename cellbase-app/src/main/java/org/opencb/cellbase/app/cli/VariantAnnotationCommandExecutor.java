package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.client.CellBaseClient;
import org.opencb.cellbase.core.variant_annotation.VariantAnnotatorRunner;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

/**
 * Created by fjlopez on 18/03/15.
 */
public class VariantAnnotationCommandExecutor extends CommandExecutor {

    private CliOptionsParser.VariantAnnotationCommandOptions variantAnnotationCommandOptions;

    private Path inputFile;
    private Path outputFile;

    public VariantAnnotationCommandExecutor(CliOptionsParser.VariantAnnotationCommandOptions variantAnnotationCommandOptions) {
        super(variantAnnotationCommandOptions.commonOptions.logLevel, variantAnnotationCommandOptions.commonOptions.verbose,
                variantAnnotationCommandOptions.commonOptions.conf);

        this.variantAnnotationCommandOptions = variantAnnotationCommandOptions;
    }

    @Override
    public void execute() {
        checkParameters();
        VariantAnnotatorRunner variantAnnotatorRunner = null;
        try {
            variantAnnotatorRunner = new VariantAnnotatorRunner(inputFile, outputFile,
                    getCellBaseClient(), variantAnnotationCommandOptions.threads);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            variantAnnotatorRunner.run();
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error executing annotator: " + e);
        }
    }

    private void checkParameters() {
        // input file
        if (variantAnnotationCommandOptions.inputFile != null) {
            inputFile = Paths.get(variantAnnotationCommandOptions.inputFile);
            if (!inputFile.toFile().exists()) {
                throw new ParameterException("Input file " + inputFile + " doesn't exist");
            } else if (inputFile.toFile().isDirectory()) {
                throw new ParameterException("Input file cannot be a directory: " + inputFile);
            }
        } else {
            throw  new ParameterException("Please check command line sintax. Provide a valid input file name.");
        }
        // output file
        if (variantAnnotationCommandOptions.outputFile != null) {
            outputFile = Paths.get(variantAnnotationCommandOptions.outputFile);
            Path outputDir = outputFile.getParent();
            if (!outputDir.toFile().exists()) {
                throw new ParameterException("Output directory " + outputDir + " doesn't exist");
            } else if (outputFile.toFile().isDirectory()) {
                throw new ParameterException("Output file cannot be a directory: " + outputFile);
            }
        } else {
            throw  new ParameterException("Please check command line sintax. Provide a valid output file name.");
        }
    }

    private CellBaseClient getCellBaseClient() throws URISyntaxException {
        CellBaseConfiguration.DatabaseProperties cellbaseDDBBProperties = configuration.getDatabase();
        // TODO: read path from configuration file?
        String path = "/cellbase/webservices/rest/";
        return new CellBaseClient(variantAnnotationCommandOptions.url, variantAnnotationCommandOptions.port, path,
                configuration.getVersion(), variantAnnotationCommandOptions.species);
    }

}
