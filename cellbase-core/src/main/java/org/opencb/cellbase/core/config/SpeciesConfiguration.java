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

import java.util.List;

/**
 * Created by imedina on 19/08/16.
 */
public class SpeciesConfiguration {

    private String id;
    private String scientificName;
    private String commonName;
    private List<Assembly> assemblies;
    private List<String> data;


    public SpeciesConfiguration() {
    }

    public SpeciesConfiguration(String id, String scientificName, String commonName, List<Assembly> assemblies, List<String> data) {
        this.id = id;
        this.scientificName = scientificName;
        this.commonName = commonName;
        this.assemblies = assemblies;
        this.data = data;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Species{");
        sb.append("id='").append(id).append('\'');
        sb.append(", scientificName='").append(scientificName).append('\'');
        sb.append(", commonName='").append(commonName).append('\'');
        sb.append(", assemblies=").append(assemblies);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public List<Assembly> getAssemblies() {
        return assemblies;
    }

    public void setAssemblies(List<Assembly> assemblies) {
        this.assemblies = assemblies;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public static class Assembly {
        private String name;
        private String ensemblVersion;
        private String ensemblCollection;  // Only for bacteria

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEnsemblVersion() {
            return ensemblVersion;
        }

        public void setEnsemblVersion(String ensemblVersion) {
            this.ensemblVersion = ensemblVersion;
        }

        public String getEnsemblCollection() {
            return ensemblCollection;
        }

        public void setEnsemblCollection(String ensemblCollection) {
            this.ensemblCollection = ensemblCollection;
        }
    }
}
