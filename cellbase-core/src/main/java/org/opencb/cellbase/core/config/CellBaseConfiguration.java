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

package org.opencb.cellbase.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.CaseFormat;
import org.apache.commons.lang.StringUtils;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.*;

public class CellBaseConfiguration {

    private String logLevel;
    private String logDir;
    private String logOuput;
    private String version;
    private String apiVersion;
    private String wiki;
    private String maintenanceFlagFile;
    private String maintainerContact;
    private String defaultOutdir;
    private Databases databases;
    private DownloadProperties download;
    private SpeciesProperties species;
    private ServerProperties server;

    public static final String CELLBASE_PREFIX = "CELLBASE_";
    public static final String CELLBASE_DATABASES_MONGODB_HOST = "CELLBASE_DATABASES_MONGODB_HOST";
    public static final String CELLBASE_DATABASES_MONGODB_USER = "CELLBASE_DATABASES_MONGODB_USER";
    public static final String CELLBASE_DATABASES_MONGODB_PASSWORD = "CELLBASE_DATABASES_MONGODB_PASSWORD";
    public static final String CELLBASE_DATABASES_MONGODB_OPTIONS_PREFIX = "CELLBASE_DATABASES_MONGODB_OPTIONS_";

    public enum ConfigurationFileFormat {
        JSON, YAML
    };

    public static CellBaseConfiguration load(Path configurationPath) throws IOException {
        InputStream inputStream = FileUtils.newInputStream(configurationPath);
        if (configurationPath.toFile().getName().endsWith("json")) {
            return load(inputStream, ConfigurationFileFormat.JSON);
        } else {
            return load(inputStream, ConfigurationFileFormat.YAML);
        }
    }

    public static CellBaseConfiguration load(InputStream configurationInputStream) throws IOException {
        return load(configurationInputStream, ConfigurationFileFormat.YAML);
    }

    public static CellBaseConfiguration load(InputStream configurationInputStream, ConfigurationFileFormat format) throws IOException {
        CellBaseConfiguration configuration;
        try {
            switch (format) {
                case JSON:
                    ObjectMapper objectMapper = new ObjectMapper();
                    configuration = objectMapper.readValue(configurationInputStream, CellBaseConfiguration.class);
                    break;
                case YAML:
                default:
                    ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
                    configuration = yamlObjectMapper.readValue(configurationInputStream, CellBaseConfiguration.class);
                    break;
            }
        } catch (IOException e) {
            throw new IOException("Configuration file could not be parsed: " + e.getMessage(), e);
        }

        overwriteEnvVariables(configuration);
        return configuration;
    }

    public void serialize(OutputStream configurationOututStream) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.writerWithDefaultPrettyPrinter().writeValue(configurationOututStream, this);
    }

    protected static void overwriteEnvVariables(CellBaseConfiguration configuration) {
        Map<String, String> envVariables = System.getenv();
        overwriteEnvVariables(configuration, envVariables);
    }

    protected static void overwriteEnvVariables(CellBaseConfiguration configuration, Map<String, String> envVariables) {
        for (Map.Entry<String, String> entry : envVariables.entrySet()) {
            String variable = entry.getKey();
            String value = entry.getValue();
            if (variable.startsWith(CELLBASE_PREFIX)) {
                if (variable.startsWith(CELLBASE_DATABASES_MONGODB_OPTIONS_PREFIX)) {
                    String optionKey = variable.substring(CELLBASE_DATABASES_MONGODB_OPTIONS_PREFIX.length());
                    String camelCaseKey = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, optionKey);
                    secureGetMongodb(configuration).getOptions().put(optionKey, value);
                    secureGetMongodb(configuration).getOptions().put(camelCaseKey, value);
                } else {
                    switch (variable) {
                        case CELLBASE_DATABASES_MONGODB_HOST:
                            secureGetMongodb(configuration).setHost(value);
                            break;
                        case CELLBASE_DATABASES_MONGODB_USER:
                            secureGetMongodb(configuration).setUser(value);
                            break;
                        case CELLBASE_DATABASES_MONGODB_PASSWORD:
                            secureGetMongodb(configuration).setPassword(value);
                            break;
                        default:
                            LoggerFactory.getLogger(CellBaseConfiguration.class).warn("Unknown env var '" + variable + "'");
                    }
                }
            }
        }
    }

    private static DatabaseCredentials secureGetMongodb(CellBaseConfiguration configuration) {
        if (configuration.getDatabases() == null) {
            configuration.setDatabases(new Databases());
        }
        if (configuration.getDatabases().getMongodb() == null) {
            configuration.getDatabases().setMongodb(new MongoDBDatabaseCredentials());
        }
        if (configuration.getDatabases().getMongodb().getShards() == null) {
            configuration.getDatabases().getMongodb().setShards(Collections.emptyList());
        }
        if (configuration.getDatabases().getMongodb().getOptions() == null) {
            configuration.getDatabases().getMongodb().setOptions(new HashMap<>());
        }
        return configuration.getDatabases().getMongodb();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getWiki() {
        return wiki;
    }

    public void setWiki(String wiki) {
        this.wiki = wiki;
    }

    public String getMaintenanceFlagFile() {
        return maintenanceFlagFile;
    }

    public void setMaintenanceFlagFile(String maintenanceFlagFile) {
        this.maintenanceFlagFile = maintenanceFlagFile;
    }

    public String getMaintainerContact() {
        return maintainerContact;
    }

    public void setMaintainerContact(String maintainerContact) {
        this.maintainerContact = maintainerContact;
    }

    public Databases getDatabases() {
        return databases;
    }

    public CellBaseConfiguration setDatabases(Databases databases) {
        this.databases = databases;
        return this;
    }

    public String getDefaultOutdir() {
        return defaultOutdir;
    }

    public void setDefaultOutdir(String defaultOutdir) {
        this.defaultOutdir = defaultOutdir;
    }

    public DownloadProperties getDownload() {
        return download;
    }

    public void setDownload(DownloadProperties download) {
        this.download = download;
    }

    public SpeciesProperties getSpecies() {
        return species;
    }

    public void setSpecies(SpeciesProperties species) {
        this.species = species;
    }

    /**
     * get the config for this species.
     * @param id shortName for species, e.g. hsapiens
     * @return configuration for this species
     */
    public SpeciesConfiguration getSpeciesConfig(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        List<SpeciesConfiguration> allSpecies = getAllSpecies();
        for (SpeciesConfiguration config : allSpecies) {
            if (config.getId().equals(id)) {
                return config;
            }
        }
        return null;
    }

    public List<SpeciesConfiguration> getAllSpecies() {
        List<SpeciesConfiguration> allSpecies = new ArrayList<>();
        if (species.getVertebrates() != null && !species.getVertebrates().isEmpty()) {
            allSpecies.addAll(species.getVertebrates());
        }
        if (species.getMetazoa() != null && !species.getMetazoa().isEmpty()) {
            allSpecies.addAll(species.getMetazoa());
        }
        if (species.getFungi() != null && !species.getFungi().isEmpty()) {
            allSpecies.addAll(species.getFungi());
        }
        if (species.getProtist() != null && !species.getProtist().isEmpty()) {
            allSpecies.addAll(species.getProtist());
        }
        if (species.getPlants() != null && !species.getPlants().isEmpty()) {
            allSpecies.addAll(species.getPlants());
        }
        if (species.getVirus() != null && !species.getVirus().isEmpty()) {
            allSpecies.addAll(species.getVirus());
        }
        if (species.getBacteria() != null && !species.getBacteria().isEmpty()) {
            allSpecies.addAll(species.getBacteria());
        }

        return allSpecies;
    }

    public ServerProperties getServer() {
        return server;
    }

    public void setServer(ServerProperties server) {
        this.server = server;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public CellBaseConfiguration setLogLevel(String logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public String getLogDir() {
        return logDir;
    }

    public CellBaseConfiguration setLogDir(String logDir) {
        this.logDir = logDir;
        return this;
    }

    public String getLogOuput() {
        return logOuput;
    }

    public CellBaseConfiguration setLogOuput(String logOuput) {
        this.logOuput = logOuput;
        return this;
    }
}
