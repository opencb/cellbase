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
import org.opencb.biodata.formats.variant.annotation.io.VepFormatReader;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by fjlopez on 14/04/15.
 */
public class PostLoadCommandExecutor extends CommandExecutor {

    private CliOptionsParser.PostLoadCommandOptions postLoadCommandOptions;

    private Path clinicalAnnotationFilename = null;
    private String assembly = null;
    private static final int CLINICAL_ANNOTATION_BATCH_SIZE = 1000;
//    private static final int CLINICAL_ANNOTATION_BATCH_SIZE=1000;

    // TODO: remove constructor, just for debugging purposes
    public PostLoadCommandExecutor() {
    }

    public PostLoadCommandExecutor(CliOptionsParser.PostLoadCommandOptions postLoadCommandOptions) {
        super(postLoadCommandOptions.commonOptions.logLevel, postLoadCommandOptions.commonOptions.verbose,
                postLoadCommandOptions.commonOptions.conf);

        this.postLoadCommandOptions = postLoadCommandOptions;
    }

    @Override
    public void execute() {
        checkParameters();
        if (clinicalAnnotationFilename != null) {
            loadClinicalAnnotation();
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

            if (postLoadCommandOptions.assembly != null) {
                assembly = postLoadCommandOptions.assembly;
                if (!assembly.equals("GRCh37") && !assembly.equals("GRCh38")) {
                    throw new ParameterException("Please, provide a valid human assembly. Available assemblies: GRCh37, GRCh38");
                }
            } else {
                throw new ParameterException("Providing human assembly is mandatory if loading clinical annotations. "
                        + "Available assemblies: GRCh37, GRCh38");
            }

        } else {
            throw new ParameterException("Please check command line syntax. Provide a valid input file name.");
        }
    }

    // TODO: change to private - just for debugging purposes
    public void loadClinicalAnnotation() {

        /**
         * Initialize VEP reader
         */
        logger.info("Initializing VEP reader...");
        VepFormatReader vepFormatReader = new VepFormatReader(clinicalAnnotationFilename.toString());
        vepFormatReader.open();
        vepFormatReader.pre();

        /**
         * Prepare clinical adaptor
         */
        logger.info("Initializing adaptor, connecting to the database...");
        logger.error("Implementation of this functionality  is outdated and must be re-written if needed");

        // TODO: this shall be done in a different way - old Java API was deprecated and removed

//        System.out.println("cellBaseConfiguration = " + configuration.getDatabase().getUser());
//        System.out.println("cellBaseConfiguration = " + configuration.getDatabase().getHost());
//        System.out.println("cellBaseConfiguration = " + configuration.getDatabase().getPassword());
//        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(configuration);
//        ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalLegacyDBAdaptor("hsapiens", assembly);
//
//        /**
//         * Load annotations
//         */
//        logger.info("Reading/Loading variant annotations...");
//        int nVepAnnotatedVariants = 0;
//        List<VariantAnnotation> variantAnnotationList = vepFormatReader.read(CLINICAL_ANNOTATION_BATCH_SIZE);
//        while (!variantAnnotationList.isEmpty()) {
//            nVepAnnotatedVariants += variantAnnotationList.size();
//            clinicalDBAdaptor.updateAnnotations(variantAnnotationList, new QueryOptions());
//            logger.info(Integer.valueOf(nVepAnnotatedVariants) + " read variants with vep annotations");
//            variantAnnotationList = vepFormatReader.read(CLINICAL_ANNOTATION_BATCH_SIZE);
//        }

//
//        vepFormatReader.post();
//        vepFormatReader.close();
//        logger.info(nVepAnnotatedVariants + " VEP annotated variants were read from " + clinicalAnnotationFilename.toString());
//        logger.info("Finished");
    }


}
