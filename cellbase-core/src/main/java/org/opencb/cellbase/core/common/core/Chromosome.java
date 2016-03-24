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

package org.opencb.cellbase.core.common.core;

import java.util.List;

public class Chromosome {

    private String name;
    private int start;
    private int end;
    private int size;
    private int isCircular;
    private int numberGenes;

    private List<Cytoband> cytobands;

    public Chromosome() {

    }

    public Chromosome(String name, int start, int end, int size, int isCircular, int numberGenes, List<Cytoband> cytobands) {
        super();
        this.name = name;
        this.start = start;
        this.end = end;
        this.size = size;
        this.isCircular = isCircular;
        this.numberGenes = numberGenes;
        this.cytobands = cytobands;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getIsCircular() {
        return isCircular;
    }

    public void setIsCircular(int isCircular) {
        this.isCircular = isCircular;
    }

    public int getNumberGenes() {
        return numberGenes;
    }

    public void setNumberGenes(int numberGenes) {
        this.numberGenes = numberGenes;
    }

    public List<Cytoband> getCytobands() {
        return cytobands;
    }

    public void setCytobands(List<Cytoband> cytobands) {
        this.cytobands = cytobands;
    }

}
