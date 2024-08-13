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

    public SpeciesConfiguration setId(String id) {
        this.id = id;
        return this;
    }

    public String getScientificName() {
        return scientificName;
    }

    public SpeciesConfiguration setScientificName(String scientificName) {
        this.scientificName = scientificName;
        return this;
    }

    public String getCommonName() {
        return commonName;
    }

    public SpeciesConfiguration setCommonName(String commonName) {
        this.commonName = commonName;
        return this;
    }

    public List<Assembly> getAssemblies() {
        return assemblies;
    }

    public SpeciesConfiguration setAssemblies(List<Assembly> assemblies) {
        this.assemblies = assemblies;
        return this;
    }

    public List<String> getData() {
        return data;
    }

    public SpeciesConfiguration setData(List<String> data) {
        this.data = data;
        return this;
    }

    public static class Assembly {
        private String name;
        private String ensemblVersion;
        private String ensemblCollection;  // Only for bacteria

        public Assembly() {
        }

        public Assembly(String ensemblCollection, String ensemblVersion, String name) {
            this.ensemblCollection = ensemblCollection;
            this.ensemblVersion = ensemblVersion;
            this.name = name;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Assembly{");
            sb.append("ensemblCollection='").append(ensemblCollection).append('\'');
            sb.append(", name='").append(name).append('\'');
            sb.append(", ensemblVersion='").append(ensemblVersion).append('\'');
            sb.append('}');
            return sb.toString();
        }

        public String getEnsemblCollection() {
            return ensemblCollection;
        }

        public Assembly setEnsemblCollection(String ensemblCollection) {
            this.ensemblCollection = ensemblCollection;
            return this;
        }

        public String getEnsemblVersion() {
            return ensemblVersion;
        }

        public Assembly setEnsemblVersion(String ensemblVersion) {
            this.ensemblVersion = ensemblVersion;
            return this;
        }

        public String getName() {
            return name;
        }

        public Assembly setName(String name) {
            this.name = name;
            return this;
        }
    }

}
