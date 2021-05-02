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

package org.opencb.cellbase.core.common;

public class Species {

    private String species;
    private String common;
    private String scientific;
    private String assembly;
    private String taxonomy;

    public Species(String species, String assembly, String taxonomy) {
        super();
        this.species = species;
        this.assembly = assembly;
        this.taxonomy = taxonomy;
    }

    public Species(String shortName, String commonName, String scientificName, String assembly) {
        super();
        this.species = shortName;
        this.common = commonName;
        this.scientific = scientificName;
        this.assembly = assembly;
    }


    @Override
    public String toString() {
        return species + "\t" + common + "\t" + scientific + "\t" + assembly;
    }


    public String getSpecies() {
        return species;
    }


    public void setSpecies(String species) {
        this.species = species;
    }


    public String getCommon() {
        return common;
    }


    public void setCommon(String common) {
        this.common = common;
    }


    public String getScientific() {
        return scientific;
    }


    public void setScientific(String scientific) {
        this.scientific = scientific;
    }


    public String getAssembly() {
        return assembly;
    }


    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

}
