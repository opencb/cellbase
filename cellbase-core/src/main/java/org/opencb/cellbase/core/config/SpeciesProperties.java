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

import java.util.List;

/**
 * Created by imedina on 19/08/16.
 */
public class SpeciesProperties {

    private List<Species> vertebrates;
    private List<Species> metazoa;
    private List<Species> fungi;
    private List<Species> protist;
    private List<Species> plants;
    private List<Species> virus;


    public SpeciesProperties() {
    }

    public SpeciesProperties(List<Species> vertebrates, List<Species> metazoa, List<Species> fungi, List<Species> protist,
                             List<Species> plants) {
        this.vertebrates = vertebrates;
        this.metazoa = metazoa;
        this.fungi = fungi;
        this.protist = protist;
        this.plants = plants;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpeciesProperties{");
        sb.append("vertebrates=").append(vertebrates);
        sb.append(", metazoa=").append(metazoa);
        sb.append(", fungi=").append(fungi);
        sb.append(", protist=").append(protist);
        sb.append(", plants=").append(plants);
        sb.append('}');
        return sb.toString();
    }

    public List<Species> getVertebrates() {
        return vertebrates;
    }

    public void setVertebrates(List<Species> vertebrates) {
        this.vertebrates = vertebrates;
    }

    public List<Species> getMetazoa() {
        return metazoa;
    }

    public void setMetazoa(List<Species> metazoa) {
        this.metazoa = metazoa;
    }

    public List<Species> getFungi() {
        return fungi;
    }

    public void setFungi(List<Species> fungi) {
        this.fungi = fungi;
    }

    public List<Species> getProtist() {
        return protist;
    }

    public void setProtist(List<Species> protist) {
        this.protist = protist;
    }

    public List<Species> getPlants() {
        return plants;
    }

    public void setPlants(List<Species> plants) {
        this.plants = plants;
    }

    public List<Species> getVirus() {
        return virus;
    }

    public void setVirus(List<Species> virus) {
        this.virus = virus;
    }
}
