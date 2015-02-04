package org.opencb.cellbase.lib.mongodb.loader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.opencb.cellbase.lib.mongodb.serializer.CellbaseMongoDBSerializer;
import org.opencb.commons.utils.OptionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by parce on 11/3/14.
 */
@Deprecated
public class CellBaseLoadMain {

    private static final String CLINICAL_DIR = "clinicalDir";
    private static final String CLINICAL_VALUE = "clinical";
    private static final String HOST_OPTION = "host";
    private static final String PORT_OPTION = "port";
    private static final String USER_OPTION = "user";
    private static final String PASSWORD_OPTION = "password";
    private static final String DATABASE_NAME = "database";
    private static Options options;
    private static CommandLine commandLine;
    
    private static final String LOAD_OPTION = "load";
    private static Logger logger;
    private static CellbaseMongoDBSerializer serializer;

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
        options.addOption(OptionFactory.createOption(HOST_OPTION, "MongoDB Host"));
        options.addOption(OptionFactory.createOption(PORT_OPTION, "MongoDB Port"));
        options.addOption(OptionFactory.createOption(DATABASE_NAME, "MongoDB database name"));
        
        // optional parameters
        options.addOption(OptionFactory.createOption(USER_OPTION, "MongoDB User", false));
        options.addOption(OptionFactory.createOption(PASSWORD_OPTION, "MongoDB Password", false));
        options.addOption(OptionFactory.createOption(CLINICAL_DIR, "Folder containing clinical files (cosmic, clinvar and gwas) in .json.gz format", false));

    }

    private static MongoDBLoader parse(String[] args) throws ParseException {
        MongoDBLoader loader = null;
        PosixParser parser = new PosixParser();
        commandLine = parser.parse(options, args, false);

        serializer = createMongoDBSerializer();
        String loadValue = commandLine.getOptionValue(LOAD_OPTION);
        switch (loadValue) {
            case CLINICAL_VALUE:
                if (commandLine.hasOption(CLINICAL_DIR)) {
                    Path clinicalFilesDir = Paths.get(commandLine.getOptionValue(CLINICAL_DIR));
                    loader = new ClinicalMongoDBLoader(serializer, clinicalFilesDir);
                } else {
                    throw new ParseException("'" + CLINICAL_DIR + "' option is mandatory when '" + LOAD_OPTION + "' option value is '" + CLINICAL_VALUE + "'");
                }
                break;
            default:
                throw new ParseException("Invalid value for option '" + LOAD_OPTION + "'");
        }
        return loader;
    }

    private static CellbaseMongoDBSerializer createMongoDBSerializer() throws ParseException {
        String host = commandLine.getOptionValue(HOST_OPTION);
        int port;
        try {
            port = Integer.parseInt(commandLine.getOptionValue(PORT_OPTION));
            if (port < 1) {
                throw new ParseException("'" + PORT_OPTION + "' value should be a positive integer");
            }
        } catch (NumberFormatException e) {
            throw new ParseException("'" + PORT_OPTION + "' value should be a integer");
        }
        String user = commandLine.getOptionValue(USER_OPTION, null);
        String password = commandLine.getOptionValue(PASSWORD_OPTION, null);
        String database = commandLine.getOptionValue(DATABASE_NAME);
        logger.info("Creating MongoDB Serializer ...");
        CellbaseMongoDBSerializer serializer = new CellbaseMongoDBSerializer(host, port, database, user, password);
        logger.info("done");
        return serializer;
    }
}
