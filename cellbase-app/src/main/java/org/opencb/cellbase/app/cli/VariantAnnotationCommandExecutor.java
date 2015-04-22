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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

/**
 * Created by fjlopez on 18/03/15.
 */
public class VariantAnnotationCommandExecutor extends CommandExecutor {

    private CliOptionsParser.VariantAnnotationCommandOptions variantAnnotationCommandOptions;

    private Path input;
    private Path output;
    private int numThreads;
    private int batchSize;

    public VariantAnnotationCommandExecutor(CliOptionsParser.VariantAnnotationCommandOptions variantAnnotationCommandOptions) {
        super(variantAnnotationCommandOptions.commonOptions.logLevel, variantAnnotationCommandOptions.commonOptions.verbose,
                variantAnnotationCommandOptions.commonOptions.conf);

        this.variantAnnotationCommandOptions = variantAnnotationCommandOptions;

        if(variantAnnotationCommandOptions.input != null) {
            input = Paths.get(variantAnnotationCommandOptions.input);
        }
        if(variantAnnotationCommandOptions.output != null) {
            output = Paths.get(variantAnnotationCommandOptions.output);
        }
    }

    @Override
    public void execute() {
        checkParameters();

        try {
            String path = "/cellbase/webservices/rest/";
            CellBaseClient cellBaseClient = new CellBaseClient(variantAnnotationCommandOptions.url,
                    variantAnnotationCommandOptions.port, path,
                    configuration.getVersion(), variantAnnotationCommandOptions.species);

            VariantAnnotatorRunner variantAnnotatorRunner =
                    new VariantAnnotatorRunner(input, output, cellBaseClient, numThreads, batchSize);
            variantAnnotatorRunner.run();
        } catch (URISyntaxException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            logger.error("Error executing annotator", e);
        }
    }

    private void checkParameters() {
        // input file
        if (variantAnnotationCommandOptions.input != null) {
            input = Paths.get(variantAnnotationCommandOptions.input);
            if (!Files.exists(input)) {
                throw new ParameterException("Input file " + input + " doesn't exist");
            } else if (Files.isDirectory(input)) {
                throw new ParameterException("Input file cannot be a directory: " + input);
            }
        } else {
            throw new ParameterException("Please check command line syntax. Provide a valid input file name.");
        }
        // output file
        if (variantAnnotationCommandOptions.output != null) {
            output = Paths.get(variantAnnotationCommandOptions.output);
            Path outputDir = output.getParent();
            if (!outputDir.toFile().exists()) {
                throw new ParameterException("Output directory " + outputDir + " doesn't exist");
            } else if (output.toFile().isDirectory()) {
                throw new ParameterException("Output file cannot be a directory: " + output);
            }
        } else {
            throw new ParameterException("Please check command line sintax. Provide a valid output file name.");
        }

        if (variantAnnotationCommandOptions.numThreads > 1) {
            numThreads = variantAnnotationCommandOptions.numThreads;
        } else {
            numThreads = 1;
            logger.warn("Incorrect number of numThreads, it must be a positive value. This has been set to '{}'", numThreads);
        }

        if (variantAnnotationCommandOptions.batchSize >= 1 && variantAnnotationCommandOptions.batchSize <= 2000) {
            batchSize = variantAnnotationCommandOptions.batchSize;
        } else {
            batchSize = 1;
            logger.warn("Incorrect size of batch size, it must be a positive value between 1-1000. This has been set to '{}'", batchSize);
        }
    }

}
