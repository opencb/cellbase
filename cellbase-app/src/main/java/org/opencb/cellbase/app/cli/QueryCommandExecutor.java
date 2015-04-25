/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Created by imedina on 20/02/15.
 * @author Javier Lopez fjlopez@ebi.ac.uk;
 */
public class QueryCommandExecutor extends CommandExecutor {

    private CliOptionsParser.QueryCommandOptions queryCommandOptions;

    private Path inputFile;
    private Path outputFile;

    public QueryCommandExecutor(CliOptionsParser.QueryCommandOptions queryCommandOptions) {
        super(queryCommandOptions.commonOptions.logLevel, queryCommandOptions.commonOptions.verbose,
                queryCommandOptions.commonOptions.conf);

        this.queryCommandOptions = queryCommandOptions;
    }

    @Override
    public void execute() {
        checkParameters();
//        if(queryCommandOptions.annotate) {
//            VariantAnnotatorRunner variantAnnotatorRunner = null;
//            try {
//                variantAnnotatorRunner = new VariantAnnotatorRunner(inputFile, outputFile,
//                        getCellBaseClient(), queryCommandOptions.threads);
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//            try {
//                variantAnnotatorRunner.run();
//            } catch (ExecutionException | InterruptedException e) {
//                logger.error("Error executing annotator: " + e);
//            }
//        }

//        checkParameters();
//        try {
//            CellBaseClient cellBaseClient = getCellBaseClient();
//            if (queryCommandOptions.annotate && input != null && input.toString().toLowerCase().endsWith(".vcf")) {
//                VcfAnnotator vcfAnnotator= new VcfAnnotator(input, output, cellBaseClient);
//                vcfAnnotator.annotateVcfFile();
//            }
//        } catch (ParameterException e) {
//            logger.error("Error parsing 'query' command line parameters: " + e.getMessage(), e);
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        }
    }

    private void checkParameters() {
        if(queryCommandOptions.annotate) {
            // input file
            if (queryCommandOptions.inputFile != null) {
                inputFile = Paths.get(queryCommandOptions.inputFile);
                if (!inputFile.toFile().exists()) {
                    throw new ParameterException("Input file " + inputFile + " doesn't exist");
                } else if (inputFile.toFile().isDirectory()) {
                    throw new ParameterException("Input file cannot be a directory: " + inputFile);
                }
            } else {
                throw  new ParameterException("Please check command line sintax. Provide a valid input file name.");
            }
            // output file
            if (queryCommandOptions.outputFile != null) {
                outputFile = Paths.get(queryCommandOptions.outputFile);
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
    }

    private CellBaseClient getCellBaseClient() throws URISyntaxException {
        CellBaseConfiguration.DatabaseProperties cellbaseDDBBProperties = configuration.getDatabase();
//        String host = cellbaseDDBBProperties.getHost();
//        int port = Integer.parseInt(cellbaseDDBBProperties.getPort());
        // TODO: read path from configuration file?
        // TODO: hardcoded port???
        String path = "/cellbase/webservices/rest/";
        return new CellBaseClient(queryCommandOptions.url, 8080, path, configuration.getVersion(), queryCommandOptions.species);
    }
}
