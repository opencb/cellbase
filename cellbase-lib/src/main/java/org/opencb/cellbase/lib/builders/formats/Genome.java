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

package org.opencb.cellbase.lib.builders.formats;

import org.opencb.biodata.models.core.Chromosome;

import java.util.List;

public class Genome {
    private String species;
    private List<Chromosome> chromosomes;
    private List<Chromosome> supercontigs;

    public Genome() {
    }

    public Genome(String species, List<Chromosome> chromosomes, List<Chromosome> supercontigs) {
        this.species = species;
        this.chromosomes = chromosomes;
        this.supercontigs = supercontigs;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Genome{");
        sb.append("species='").append(species).append('\'');
        sb.append(", chromosomes=").append(chromosomes);
        sb.append(", supercontigs=").append(supercontigs);
        sb.append('}');
        return sb.toString();
    }

    public String getSpecies() {
        return species;
    }

    public Genome setSpecies(String species) {
        this.species = species;
        return this;
    }

    public List<Chromosome> getChromosomes() {
        return chromosomes;
    }

    public Genome setChromosomes(List<Chromosome> chromosomes) {
        this.chromosomes = chromosomes;
        return this;
    }

    public List<Chromosome> getSupercontigs() {
        return supercontigs;
    }

    public Genome setSupercontigs(List<Chromosome> supercontigs) {
        this.supercontigs = supercontigs;
        return this;
    }
}
