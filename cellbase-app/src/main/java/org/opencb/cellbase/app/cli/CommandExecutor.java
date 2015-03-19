package org.opencb.cellbase.app.cli;

import org.opencb.cellbase.core.CellBaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by imedina on 03/02/15.
 */
public abstract class CommandExecutor {

    protected String logLevel;
    protected boolean verbose;
    protected String configFile;

    protected String appHome;
    protected CellBaseConfiguration configuration;

    protected Logger logger;

    public CommandExecutor() {

    }

    public CommandExecutor(String logLevel, boolean verbose, String configFile) {
        this.logLevel = logLevel;
        this.verbose = verbose;
        this.configFile = configFile;

        /**
         * System property 'app.home' is set up by cellbase.sh. If by any reason this is null
         * then CELLBASE_HOME environment variable is used instead.
         */
        this.appHome = System.getProperty("app.home", System.getenv("CELLBASE_HOME"));

        if(logLevel != null && !logLevel.isEmpty()) {
            // We must call to this method
            setLogLevel(logLevel);
        }
    }

    public abstract void execute();

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        // This small hack allow to configure the appropriate Logger level from the command line, this is done
        // by setting the DEFAULT_LOG_LEVEL_KEY before the logger object is created.
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, logLevel);
        logger = LoggerFactory.getLogger(this.getClass().toString());
        this.logLevel = logLevel;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * This method attempts to first load configuration from CLI parameter, if not present then uses
     * the configuration from installation directory, if not exists then loads JAR configuration.json
     * @throws URISyntaxException
     * @throws IOException
     */
    public void loadCellBaseConfiguration() throws URISyntaxException, IOException {
        if(this.configFile != null) {
            logger.debug("Loading configuration from '{}'", this.configFile);
            this.configuration = CellBaseConfiguration.load(new FileInputStream(new File(this.configFile)));
        }else {
            if(Files.exists(Paths.get(this.appHome+"/configuration.json"))) {
                logger.debug("Loading configuration from '{}'", this.appHome+"/configuration.json");
                this.configuration = CellBaseConfiguration.load(new FileInputStream(new File(this.appHome+"/configuration.json")));
            }else {
                logger.debug("Loading configuration from '{}'",
                        CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json").toString());
                this.configuration = CellBaseConfiguration.load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
            }
        }
    }
}
