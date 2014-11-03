package org.opencb.cellbase.lib.mongodb.loader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.opencb.commons.utils.OptionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by parce on 11/3/14.
 */
public class CellBaseLoadMain {

    private static final String CLINICAL_DIR = "clinicalDir";
    private static final String CLINICAL_VALUE = "clinical";
    private static Options options;
    private static CommandLine commandLine;
    
    private static final String LOAD_OPTION = "load";
    private static Logger logger;

    public static void main (String[] args) {
        initOptions();
        logger = LoggerFactory.getLogger("org.opencb.cellbase.build.CellBaseMain");
        try {
            MongoDBLoader loader = parse(args);
            loader.load();
        } catch (ParseException e) {
            logger.error("Error parsing command line args: " + e.getMessage());
        }

    }

    private static void initOptions() {
        options = new Options();

        // Mandatory parameters
        options.addOption(OptionFactory.createOption(LOAD_OPTION, "Load values: clinical"));
        
        // optional parameters
        options.addOption(OptionFactory.createOption(CLINICAL_DIR, "Folder containing clinical files (cosmic, clinvar and gwas) in .json.gz format", false));

    }

    private static MongoDBLoader parse(String[] args) throws ParseException {
        MongoDBLoader loader = null;
        PosixParser parser = new PosixParser();
        commandLine = parser.parse(options, args, false);
        // TODO: ¿es necesario el logger?

        String loadValue = commandLine.getOptionValue(LOAD_OPTION);
        switch (loadValue) {
            case CLINICAL_VALUE:
                if (commandLine.hasOption(CLINICAL_DIR)) {
                    Path clinicalFilesDir = Paths.get(commandLine.getOptionValue(CLINICAL_DIR));
                    String host = "localhost";
                    int port = 27017;
                    String user = "";
                    String password = "";
                    ClinicalMongoDBLoader clinicalLoader = new ClinicalMongoDBLoader(host, port, user, password, clinicalFilesDir);
                    loader = clinicalLoader;
                } else {
                    throw new ParseException("'" + CLINICAL_DIR + "' option is mandatory when '" + LOAD_OPTION + "' option value is '" + CLINICAL_VALUE + "'");
                }
                break;
            default:
                throw new ParseException("Invalid value for option '" + LOAD_OPTION + "'");

        }
        return loader;
    }
}
