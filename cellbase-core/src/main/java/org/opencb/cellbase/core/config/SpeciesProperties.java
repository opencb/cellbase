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


public class SpeciesProperties {

    private List<SpeciesConfiguration> vertebrates;
    private List<SpeciesConfiguration> metazoa;
    private List<SpeciesConfiguration> fungi;
    private List<SpeciesConfiguration> protist;
    private List<SpeciesConfiguration> plants;
    private List<SpeciesConfiguration> virus;
    private List<SpeciesConfiguration> bacteria;


    public SpeciesProperties() {
    }

    public SpeciesProperties(List<SpeciesConfiguration> vertebrates, List<SpeciesConfiguration> metazoa, List<SpeciesConfiguration> fungi,
                             List<SpeciesConfiguration> protist, List<SpeciesConfiguration> plants, List<SpeciesConfiguration> virus,
                             List<SpeciesConfiguration> bacteria) {
        this.vertebrates = vertebrates;
        this.metazoa = metazoa;
        this.fungi = fungi;
        this.protist = protist;
        this.plants = plants;
        this.virus = virus;
        this.bacteria = bacteria;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpeciesProperties{");
        sb.append("vertebrates=").append(vertebrates);
        sb.append(", metazoa=").append(metazoa);
        sb.append(", fungi=").append(fungi);
        sb.append(", protist=").append(protist);
        sb.append(", plants=").append(plants);
        sb.append(", virus=").append(virus);
        sb.append(", bacteria=").append(bacteria);
        sb.append('}');
        return sb.toString();
    }

    public List<SpeciesConfiguration> getVertebrates() {
        return vertebrates;
    }

    public SpeciesProperties setVertebrates(List<SpeciesConfiguration> vertebrates) {
        this.vertebrates = vertebrates;
        return this;
    }

    public List<SpeciesConfiguration> getMetazoa() {
        return metazoa;
    }

    public SpeciesProperties setMetazoa(List<SpeciesConfiguration> metazoa) {
        this.metazoa = metazoa;
        return this;
    }

    public List<SpeciesConfiguration> getFungi() {
        return fungi;
    }

    public SpeciesProperties setFungi(List<SpeciesConfiguration> fungi) {
        this.fungi = fungi;
        return this;
    }

    public List<SpeciesConfiguration> getProtist() {
        return protist;
    }

    public SpeciesProperties setProtist(List<SpeciesConfiguration> protist) {
        this.protist = protist;
        return this;
    }

    public List<SpeciesConfiguration> getPlants() {
        return plants;
    }

    public SpeciesProperties setPlants(List<SpeciesConfiguration> plants) {
        this.plants = plants;
        return this;
    }

    public List<SpeciesConfiguration> getVirus() {
        return virus;
    }

    public SpeciesProperties setVirus(List<SpeciesConfiguration> virus) {
        this.virus = virus;
        return this;
    }

    public List<SpeciesConfiguration> getBacteria() {
        return bacteria;
    }

    public SpeciesProperties setBacteria(List<SpeciesConfiguration> bacteria) {
        this.bacteria = bacteria;
        return this;
    }
}
