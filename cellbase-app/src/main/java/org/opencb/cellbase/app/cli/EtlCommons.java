package org.opencb.cellbase.app.cli;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fjlopez on 03/06/16.
 */
public class EtlCommons {

    public static final String GENOME_DATA = "genome";
    public static final String GENE_DATA = "gene";
    public static final String GENE_DISEASE_ASSOCIATION_DATA = "gene_disease_association";
    public static final String VARIATION_DATA = "variation";
    public static final String VARIATION_FUNCTIONAL_SCORE_DATA = "variation_functional_score";
    public static final String REGULATION_DATA = "regulation";
    public static final String PROTEIN_DATA = "protein";
    public static final String CONSERVATION_DATA = "conservation";
    public static final String CLINICAL_DATA = "clinical";
    public static final String REPEATS_DATA = "repeats";

    // Build specific data options
    public static final String GENOME_INFO_DATA = "genome_info";
    public static final String DISGENET_DATA = "disgenet";
    public static final String HPO_DATA = "hpo";
    public static final String CADD_DATA = "cadd";
    public static final String PPI_DATA = "ppi";
    public static final String DRUG_DATA = "drug";
    public static final String CLINVAR_DATA = "clinvar";
    public static final String COSMIC_DATA = "cosmic";
    public static final String GWAS_DATA = "gwas";

    // Load specific data options
    public static final String PROTEIN_FUNCTIONAL_PREDICTION_DATA = "protein_functional_prediction";

    // Path and file names
    public static final String GERP_SUBDIRECTORY = "gerp";
    public static final String GERP_FILE = "hg19.GERP_scores.tar.gz";
    public static final String TRF_FILE = "simpleRepeat.txt.gz";
    public static final String TRF_VERSION_FILE = "simpleRepeat.json";
    public static final String GSD_FILE = "genomicSuperDups.txt.gz";
    public static final String GSD_VERSION_FILE = "genomicSuperDups.json";
    public static final String WM_FILE = "windowMasker.txt.gz";
    public static final String WM_VERSION_FILE = "windowMasker.json";
    public static final String REPEATS_FOLDER = "repeats";
    public static final String REPEATS_JSON = "repeats";


    public static boolean runCommandLineProcess(File workingDirectory, String binPath, List<String> args, String logFilePath)
            throws IOException, InterruptedException {
        // This small hack allow to configure the appropriate Logger level from the command line, this is done
        // by setting the DEFAULT_LOG_LEVEL_KEY before the logger object is created.
        org.apache.log4j.Logger rootLogger = LogManager.getRootLogger();
        ConsoleAppender stderr = (ConsoleAppender) rootLogger.getAppender("stderr");
        stderr.setThreshold(Level.toLevel("debug"));
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


}
