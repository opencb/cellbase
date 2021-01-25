/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fjlopez on 03/06/16.
 */
public class EtlCommons {

    public static final String GENOME_DATA = "genome";
    public static final String GENE_DATA = "gene";
    public static final String REFSEQ_DATA = "refseq";
    public static final String GENE_DISEASE_ASSOCIATION_DATA = "gene_disease_association";
    public static final String VARIATION_DATA = "variation";
    public static final String VARIATION_FUNCTIONAL_SCORE_DATA = "variation_functional_score";
    public static final String MISSENSE_VARIATION_SCORE_DATA = "missense_variation_functional_score";
    public static final String REGULATION_DATA = "regulation";
    public static final String PROTEIN_DATA = "protein";
    public static final String CONSERVATION_DATA = "conservation";
    public static final String CLINICAL_VARIANTS_DATA = "clinical_variants";

    public static final String CLINICAL_VARIANTS_FOLDER = "clinicalVariant";
    public static final String CLINVAR_XML_FILE = "ClinVarFullRelease_2021-01.xml.gz";
    public static final String CLINVAR_EFO_FILE = "ClinVar_Traits_EFO_Names.csv";
    public static final String CLINVAR_SUMMARY_FILE = "variant_summary.txt.gz";
    public static final String CLINVAR_VARIATION_ALLELE_FILE = "variation_allele.txt.gz";
    public static final String IARCTP53_FILE = "IARC-TP53.zip";
    public static final String GWAS_FILE = "gwas_catalog.tsv";
    public static final String COSMIC_FILE = "CosmicMutantExport.tsv.gz";
    public static final String DBSNP_FILE = "All.vcf.gz";

    public static final String STRUCTURAL_VARIANTS_DATA = "svs";
    public static final String REPEATS_DATA = "repeats";
    public static final String OBO_DATA = "ontology";
    public static final String HPO_FILE = "hp.obo";
    public static final String GO_FILE = "go-basic.obo";
    public static final String DOID_FILE = "doid.obo";
    public static final String PFM_DATA = "regulatory_pfm";

    // Build specific data options
    public static final String GENOME_INFO_DATA = "genome_info";
    public static final String DISGENET_DATA = "disgenet";
    public static final String HPO_DATA = "hpo";
    public static final String CADD_DATA = "cadd";
    public static final String PPI_DATA = "ppi";
    public static final String DRUG_DATA = "drug";
    public static final String CLINVAR_DATA = "clinvar";
    public static final String DOCM_DATA = "docm";
    public static final String COSMIC_DATA = "cosmic";
    public static final String GWAS_DATA = "gwas";
    public static final String IARCTP53_GERMLINE_FILE = "germlineMutationDataIARC TP53 Database, R20.txt";
    public static final String IARCTP53_GERMLINE_REFERENCES_FILE = "germlineMutationReferenceIARC TP53 Database, R20.txt";
    public static final String IARCTP53_SOMATIC_FILE = "somaticMutationDataIARC TP53 Database, R20.txt";
    public static final String IARCTP53_SOMATIC_REFERENCES_FILE = "somaticMutationReferenceIARC TP53 Database, R20.txt";

    // Load specific data options
    public static final String PROTEIN_FUNCTIONAL_PREDICTION_DATA = "protein_functional_prediction";

    // Path and file names
    public static final String GERP_SUBDIRECTORY = "gerp";
    // binary bigwig file
    public static final String GERP_FILE = "gerp_conservation_scores.homo_sapiens.GRCh38.bw";
    // bigwig file manually transformed to bedGraph file
    public static final String GERP_PROCESSED_FILE = "gerp.bedGraph.gz";
    public static final String CLINICAL_VARIANTS_JSON_FILE = "clinical_variants.json.gz";
    public static final String CLINICAL_VARIANTS_ANNOTATED_JSON_FILE = "clinical_variants.full.json.gz";
    public static final String DOCM_FILE = "docm.json.gz";
    public static final String DOCM_NAME = "DOCM";
    public static final String STRUCTURAL_VARIANTS_FOLDER = "structuralVariants";
    public static final String DGV_FILE = "dgv.txt";
    public static final String DGV_VERSION_FILE = "dgvVersion.json";
    public static final String STRUCTURAL_VARIANTS_JSON = "structuralVariants";
    public static final String TRF_FILE = "simpleRepeat.txt.gz";
    public static final String TRF_VERSION_FILE = "simpleRepeat.json";
    public static final String GSD_FILE = "genomicSuperDups.txt.gz";
    public static final String GSD_VERSION_FILE = "genomicSuperDups.json";
    public static final String WM_FILE = "windowMasker.txt.gz";
    public static final String WM_VERSION_FILE = "windowMasker.json";
    public static final String REPEATS_FOLDER = "genome";
    public static final String REPEATS_JSON = "repeats";
    public static final String OBO_JSON = "ontology";
    public static final String HPO_VERSION_FILE = "hpoVersion.json";
    public static final String GO_VERSION_FILE = "goVersion.json";
    public static final String DO_VERSION_FILE = "doVersion.json";

    public static final String REGULATORY_FEATURES_FILE = "Regulatory_Build.regulatory_features.gff.gz";
    public static final String MOTIF_FEATURES_FILE = "motif_features.gff.gz";

    public static boolean runCommandLineProcess(File workingDirectory, String binPath, List<String> args, String logFilePath)
            throws IOException, InterruptedException {
        // This small hack allow to configure the appropriate Logger level from the command line, this is done
        // by setting the DEFAULT_LOG_LEVEL_KEY before the logger object is created.
//        org.apache.log4j.Logger rootLogger = LogManager.getRootLogger();
//        ConsoleAppender stderr = (ConsoleAppender) rootLogger.getAppender("stdout");
//        stderr.setThreshold(Level.toLevel("debug"));

//        Configurator.setRootLevel(Level.INFO);

        Logger logger = LoggerFactory.getLogger("EtlCommons");

        ProcessBuilder builder = getProcessBuilder(workingDirectory, binPath, args, logFilePath);

        logger.debug("Executing command: " + StringUtils.join(builder.command(), " "));
        Process process = builder.start();
        process.waitFor();

        // Check process output
        boolean executedWithoutErrors = true;
        int genomeInfoExitValue = process.exitValue();
        if (genomeInfoExitValue != 0) {
            logger.warn("Error executing {}, error code: {}. More info in log file: {}", binPath, genomeInfoExitValue, logFilePath);
            executedWithoutErrors = false;
        }
        return executedWithoutErrors;
    }

    private static ProcessBuilder getProcessBuilder(File workingDirectory, String binPath, List<String> args, String logFilePath) {
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(binPath);
        commandArgs.addAll(args);
        ProcessBuilder builder = new ProcessBuilder(commandArgs);

        // working directoy and error and output log outputs
        if (workingDirectory != null) {
            builder.directory(workingDirectory);
        }
        builder.redirectErrorStream(true);
        if (logFilePath != null) {
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(logFilePath)));
        }

        return builder;
    }

    public static boolean isMissing(String string) {
        return !((string != null) && !string.isEmpty()
                && !string.replace(" ", "")
                .replace("not specified", "")
                .replace("NS", "")
                .replace("NA", "")
                .replace("na", "")
                .replace("NULL", "")
                .replace("null", "")
                .replace("\t", "")
                .replace(".", "")
                .replace("-", "").isEmpty());
    }

    public static Long countFileLines(Path filePath) throws IOException {
        try (BufferedReader bufferedReader1 = FileUtils.newBufferedReader(filePath)) {
            long nLines = 0;
            String line1 = bufferedReader1.readLine();
            while (line1 != null) {
                nLines++;
                line1 = bufferedReader1.readLine();
            }
            return nLines;
        }

    }

}
