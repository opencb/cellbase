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
 * Created by fjlopez on 14/04/15.
 */
public class PostLoadCommandExecutor extends CommandExecutor{

    private CliOptionsParser.PostLoadCommandOptions postLoadCommandOptions;

    private Path clinicalAnnotationFilename = null;

    public PostLoadCommandExecutor(CliOptionsParser.PostLoadCommandOptions postLoadCommandOptions) {
        super(postLoadCommandOptions.commonOptions.logLevel, postLoadCommandOptions.commonOptions.verbose,
                postLoadCommandOptions.commonOptions.conf);

        this.postLoadCommandOptions = postLoadCommandOptions;
    }

    @Override
    public void execute() {
        checkParameters();
        if(clinicalAnnotationFilename!=null) {

        } else {
            throw new ParameterException("Only post-load of clinical annotations is available right now.");
        }
    }

    private void checkParameters() {
        // input file
        if (postLoadCommandOptions.clinicalAnnotationFilename != null) {
            clinicalAnnotationFilename = Paths.get(postLoadCommandOptions.clinicalAnnotationFilename);
            if (!clinicalAnnotationFilename.toFile().exists()) {
                throw new ParameterException("Input file " + clinicalAnnotationFilename + " doesn't exist");
            } else if (clinicalAnnotationFilename.toFile().isDirectory()) {
                throw new ParameterException("Input file cannot be a directory: " + clinicalAnnotationFilename);
            }
        } else {
            throw  new ParameterException("Please check command line syntax. Provide a valid input file name.");
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
