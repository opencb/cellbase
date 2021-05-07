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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        if (logLevel != null && !logLevel.isEmpty()) {
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
//        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, logLevel);

        org.apache.log4j.Logger rootLogger = LogManager.getRootLogger();
        ConsoleAppender stderr = (ConsoleAppender) rootLogger.getAppender("stderr");

        // Line above returning null - and causing NPE - in certain environments
        if (stderr != null) {
            stderr.setThreshold(Level.toLevel(logLevel));
        }

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

    /*
     * This method attempts to first data configuration from CLI parameter, if not present then uses
     * the configuration from installation directory, if not exists then loads JAR configuration.json.
     */
    public void loadCellBaseConfiguration() throws URISyntaxException, IOException {
        if (this.configFile != null) {
            logger.debug("Loading configuration from '{}'", this.configFile);
            this.configuration = CellBaseConfiguration.load(new FileInputStream(new File(this.configFile)));
        } else {
            if (Files.exists(Paths.get(this.appHome + "/configuration.json"))) {
                logger.debug("Loading configuration from '{}'", this.appHome + "/configuration.json");
                this.configuration = CellBaseConfiguration.load(new FileInputStream(new File(this.appHome + "/configuration.json")));
            } else {
                logger.debug("Loading configuration from '{}'",
                        CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json").toString());
                this.configuration = CellBaseConfiguration
                        .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
            }
        }
    }


    protected void makeDir(Path folderPath) throws IOException {
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }
    }

//    protected boolean runCommandLineProcess(File workingDirectory, String binPath, List<String> args, String logFilePath)
//            throws IOException, InterruptedException {
//        ProcessBuilder builder = getProcessBuilder(workingDirectory, binPath, args, logFilePath);
//
//        logger.debug("Executing command: " + StringUtils.join(builder.command(), " "));
//        Process process = builder.start();
//        process.waitFor();
//
//        // Check process output
//        boolean executedWithoutErrors = true;
//        int genomeInfoExitValue = process.exitValue();
//        if (genomeInfoExitValue != 0) {
//            logger.warn("Error executing {}, error code: {}. More info in log file: {}", binPath, genomeInfoExitValue, logFilePath);
//            executedWithoutErrors = false;
//        }
//        return executedWithoutErrors;
//    }
//
//    private ProcessBuilder getProcessBuilder(File workingDirectory, String binPath, List<String> args, String logFilePath) {
//        List<String> commandArgs = new ArrayList<>();
//        commandArgs.add(binPath);
//        commandArgs.addAll(args);
//        ProcessBuilder builder = new ProcessBuilder(commandArgs);
//
//        // working directoy and error and output log outputs
//        if (workingDirectory != null) {
//            builder.directory(workingDirectory);
//        }
//        builder.redirectErrorStream(true);
//        if (logFilePath != null) {
//            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(logFilePath)));
//        }
//
//        return builder;
//    }

}
