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

package org.opencb.cellbase.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by parce on 12/02/15.
 */
public class CellBaseConfiguration {

    private String version;
    private String apiVersion;
    private String wiki;
    private String maintenanceFlagFile;
    private String defaultOutdir;
    private Databases databases;
    private DownloadProperties download;
    private SpeciesProperties species;


    public static CellBaseConfiguration load(InputStream configurationInputStream) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.readValue(configurationInputStream, CellBaseConfiguration.class);
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

    public List<Species> getAllSpecies() {
        List<Species> allSpecies = new ArrayList<>();
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

}
