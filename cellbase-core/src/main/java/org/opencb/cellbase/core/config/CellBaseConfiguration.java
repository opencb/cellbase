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
    private String defaultOutdir;
    private Databases databases;
    private CacheProperties cache;
    private DownloadProperties download;
    private SpeciesProperties species;


    public static CellBaseConfiguration load(InputStream configurationInputStream) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.readValue(configurationInputStream, CellBaseConfiguration.class);
    }

    public List<Species> getAllSpecies() {
        List<Species> allSpecies = new ArrayList<>();
        allSpecies.addAll(species.getVertebrates());
        allSpecies.addAll(species.getMetazoa());
        allSpecies.addAll(species.getFungi());
        allSpecies.addAll(species.getProtist());
        allSpecies.addAll(species.getPlants());

        return allSpecies;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CellBaseConfiguration{");
        sb.append("version='").append(version).append('\'');
        sb.append(", apiVersion='").append(apiVersion).append('\'');
        sb.append(", wiki='").append(wiki).append('\'');
        sb.append(", defaultOutdir='").append(defaultOutdir).append('\'');
        sb.append(", databases=").append(databases);
        sb.append(", cache=").append(cache);
        sb.append(", download=").append(download);
        sb.append(", species=").append(species);
        sb.append('}');
        return sb.toString();
    }

    public String getVersion() {
        return version;
    }

    public CellBaseConfiguration setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public CellBaseConfiguration setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getWiki() {
        return wiki;
    }

    public CellBaseConfiguration setWiki(String wiki) {
        this.wiki = wiki;
        return this;
    }

    public String getDefaultOutdir() {
        return defaultOutdir;
    }

    public CellBaseConfiguration setDefaultOutdir(String defaultOutdir) {
        this.defaultOutdir = defaultOutdir;
        return this;
    }

    public Databases getDatabases() {
        return databases;
    }

    public CellBaseConfiguration setDatabases(Databases databases) {
        this.databases = databases;
        return this;
    }

    public CacheProperties getCache() {
        return cache;
    }

    public CellBaseConfiguration setCache(CacheProperties cache) {
        this.cache = cache;
        return this;
    }

    public DownloadProperties getDownload() {
        return download;
    }

    public CellBaseConfiguration setDownload(DownloadProperties download) {
        this.download = download;
        return this;
    }

    public SpeciesProperties getSpecies() {
        return species;
    }

    public CellBaseConfiguration setSpecies(SpeciesProperties species) {
        this.species = species;
        return this;
    }
}
