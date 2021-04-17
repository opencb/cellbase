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

package org.opencb.cellbase.core.common;

public class Species {

    private String id;
    private String commonName;
    private String scientificName;
    private String assembly;
    private String taxonomy;

    public Species(String id, String assembly) {
        this.id = id;
        this.assembly = assembly;
    }

    public Species(String id, String commonName, String scientificName, String assembly) {
        this.id = id;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.assembly = assembly;
    }

    public Species(String id, String commonName, String scientificName, String assembly, String taxonomy) {
        this.id = id;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.assembly = assembly;
        this.taxonomy = taxonomy;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Species{");
        sb.append("id='").append(id).append('\'');
        sb.append(", commonName='").append(commonName).append('\'');
        sb.append(", scientificName='").append(scientificName).append('\'');
        sb.append(", assembly='").append(assembly).append('\'');
        sb.append(", taxonomy='").append(taxonomy).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public Species setId(String id) {
        this.id = id;
        return this;
    }

    public String getCommonName() {
        return commonName;
    }

    public Species setCommonName(String commonName) {
        this.commonName = commonName;
        return this;
    }

    public String getScientificName() {
        return scientificName;
    }

    public Species setScientificName(String scientificName) {
        this.scientificName = scientificName;
        return this;
    }

    public String getAssembly() {
        return assembly;
    }

    public Species setAssembly(String assembly) {
        this.assembly = assembly;
        return this;
    }

    public String getTaxonomy() {
        return taxonomy;
    }

    public Species setTaxonomy(String taxonomy) {
        this.taxonomy = taxonomy;
        return this;
    }
}
