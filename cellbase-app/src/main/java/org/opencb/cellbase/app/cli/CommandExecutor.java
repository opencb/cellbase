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

package org.opencb.cellbase.app.cli;


import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

/**
 * Created by imedina on 03/02/15.
 */
public abstract class CommandExecutor {

    protected String logLevel;
//    protected boolean verbose;
    protected String conf;

    @Deprecated
    protected String configFile;

    protected String appHome;

    protected CellBaseConfiguration configuration;
    protected ClientConfiguration clientConfiguration;

    protected Logger logger;

    public CommandExecutor() {

    }

    public CommandExecutor(String logLevel, String conf) {
        this.logLevel = logLevel;
        this.conf = conf;

        /**
         * System property 'app.home' is set up by cellbase.sh. If by any reason this is null
         * then CELLBASE_HOME environment variable is used instead.
         */
        this.appHome = System.getProperty("app.home", System.getenv("CELLBASE_HOME"));

        if (StringUtils.isEmpty(conf)) {
            this.conf = this.appHome + "/conf";
        }

        if (logLevel != null && !logLevel.isEmpty()) {
            // We must call to this method
            setLogLevel(logLevel);
        }
    }

    public CommandExecutor(String logLevel, boolean verbose, String conf) {
        this.logLevel = logLevel;
//        this.verbose = verbose;
        this.conf = conf;

        /**
         * System property 'app.home' is set up by cellbase.sh. If by any reason this is null
         * then CELLBASE_HOME environment variable is used instead.
         */
        this.appHome = System.getProperty("app.home", System.getenv("CELLBASE_HOME"));

        if (StringUtils.isEmpty(conf)) {
            this.conf = this.appHome + "/conf";
        }

        if (logLevel != null && !logLevel.isEmpty()) {
            // We must call to this method
            setLogLevel(logLevel);
        }
    }

    public abstract void execute() throws CellbaseException;

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        // This small hack allow to configure the appropriate Logger level from the command line, this is done
        // by setting the DEFAULT_LOG_LEVEL_KEY before the logger object is created.
//        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, logLevel);

//        org.apache.log4j.Logger rootLogger = LogManager.getRootLogger();
//        ConsoleAppender stderr = (ConsoleAppender) rootLogger.getAppender("stderr");
//
//        // Line above returning null - and causing NPE - in certain environments
//        if (stderr != null) {
//            stderr.setThreshold(Level.toLevel(logLevel));
//        }

        Level level = Level.toLevel(logLevel, Level.INFO);
        Configurator.setRootLevel(level);
        logger = LoggerFactory.getLogger(this.getClass().toString());
        this.logLevel = logLevel;
    }

//    public boolean isVerbose() {
//        return verbose;
//    }
//
//    public void setVerbose(boolean verbose) {
//        this.verbose = verbose;
//    }

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
     * the configuration from installation directory, if not exists then loads JAR configuration.json or yml.
     */
    public void loadCellBaseConfiguration() throws URISyntaxException, IOException {
        Path confPath = Paths.get(this.conf);
        FileUtils.checkDirectory(confPath);

        if (Files.exists(confPath.resolve("configuration.json"))) {
            logger.debug("Loading configuration from '{}'", confPath.resolve("configuration.json").toAbsolutePath());
            this.configuration = CellBaseConfiguration.load(new FileInputStream(confPath.resolve("configuration.json").toFile()),
                    CellBaseConfiguration.ConfigurationFileFormat.JSON);
        } else if (Files.exists(Paths.get(this.appHome + "/conf/configuration.yml"))) {
            logger.debug("Loading configuration from '{}'", this.appHome + "/conf/configuration.yml");
            this.configuration = CellBaseConfiguration.load(new FileInputStream(new File(this.appHome + "/conf/configuration.yml")));
        } else {
            InputStream inputStream = CellBaseConfiguration.class.getClassLoader().getResourceAsStream("conf/configuration.json");
            String configurationFilePath = "conf/configuration.json";
            CellBaseConfiguration.ConfigurationFileFormat fileType = CellBaseConfiguration.ConfigurationFileFormat.JSON;
            if (inputStream == null) {
                inputStream = CellBaseConfiguration.class.getClassLoader().getResourceAsStream("conf/configuration.yml");
                configurationFilePath = "conf/configuration.yml";
                fileType = CellBaseConfiguration.ConfigurationFileFormat.YAML;
            }
            logger.debug("Loading configuration from '{}'", configurationFilePath);
            this.configuration = CellBaseConfiguration.load(inputStream);
        }
    }

    /**
     * This method attempts to first data configuration from CLI parameter, if not present then uses
     * the configuration from installation directory, if not exists then loads JAR client-configuration.yml.
     *
     * @throws IOException If any IO problem occurs
     */
    public void loadClientConfiguration() throws IOException {
        Path confPath = Paths.get(this.conf);
        FileUtils.checkDirectory(confPath);
        if (Files.exists(confPath.resolve("client-configuration.yml"))) {
            Path configurationPath = confPath.resolve("client-configuration.yml");
            logger.debug("Loading configuration from '{}'", configurationPath.toAbsolutePath());
            this.clientConfiguration = ClientConfiguration.load(new FileInputStream(configurationPath.toFile()),
                    CellBaseConfiguration.ConfigurationFileFormat.YAML);
        } else {
            if (Files.exists(confPath.resolve("client-configuration.json"))) {
                Path clientConfigurationPath = confPath.resolve("client-configuration.json");
                logger.debug("Loading configuration from '{}'", clientConfigurationPath.toAbsolutePath());
                this.clientConfiguration = ClientConfiguration.load(new FileInputStream(clientConfigurationPath.toFile()),
                        CellBaseConfiguration.ConfigurationFileFormat.JSON);
            } else {
                throw new RuntimeException("Invalid configuration file, expecting client-configuration.json or client-configuration.yml");
            }
        }
    }

    protected void makeDir(Path folderPath) throws IOException {
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }
    }

}
