package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;
import org.opencb.cellbase.app.query.VcfAnnotator;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.client.CellBaseClient;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by imedina on 20/02/15.
 */
public class QueryCommandParser extends CommandParser {

    private CliOptionsParser.QueryCommandOptions queryCommandOptions;

    private Path inputFile;
    private Path outputFile;

    public QueryCommandParser(CliOptionsParser.QueryCommandOptions queryCommandOptions) {
        super(queryCommandOptions.commonOptions.logLevel, queryCommandOptions.commonOptions.verbose,
                queryCommandOptions.commonOptions.conf);

        this.queryCommandOptions = queryCommandOptions;
    }

    @Override
    public void parse() {
        checkParameters();
        try {
            CellBaseClient cellBaseClient = getCellBaseClient();
            if (queryCommandOptions.annotate && inputFile != null && inputFile.toString().toLowerCase().endsWith(".vcf")) {
                VcfAnnotator vcfAnnotator= new VcfAnnotator(inputFile, outputFile, cellBaseClient);
                vcfAnnotator.annotateVcfFile();
            }
        } catch (ParameterException e) {
            logger.error("Error parsing 'query' command line parameters: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void checkParameters() {
        // input file
        if (queryCommandOptions.inputFile != null) {
            inputFile = Paths.get(queryCommandOptions.inputFile);
            if (!inputFile.toFile().exists()) {
                throw new ParameterException("Input file " + inputFile + " doesn't exist");
            } else if (inputFile.toFile().isDirectory()) {
                throw new ParameterException("Input file cannot be a directory: " + inputFile);
            }
        }

        if (queryCommandOptions.outputFile != null) {
            outputFile = Paths.get(queryCommandOptions.outputFile);
            // TODO: check that output file is not a directory
        }
    }

    private CellBaseClient getCellBaseClient() throws URISyntaxException {
        CellBaseConfiguration.DatabaseProperties cellbaseDDBBProperties = configuration.getDatabase();
//        String host = cellbaseDDBBProperties.getHost();
//        int port = Integer.parseInt(cellbaseDDBBProperties.getPort());
        // TODO: read path from configuration file?
        // TODO: hardcoded port???
        String path = "/cellbase/webservices/rest/";
        return new CellBaseClient(queryCommandOptions.url, 80, path, configuration.getVersion(), queryCommandOptions.species);
    }
}
