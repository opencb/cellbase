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

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fsalavert
 * Date: 4/11/13
 * Time: 12:12 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class GenericFeatureChunk {
    private String chromosome;
    private int chunkId;
    private int start;
    private int end;
    private List<GenericFeature> features = new ArrayList<>();

    public GenericFeatureChunk(String chromosome, int chunkId, int start, int end, List<GenericFeature> features) {
        this.chromosome = chromosome;
        this.chunkId = chunkId;
        this.start = start;
        this.end = end;
        this.features = features;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public int getChunkId() {
        return chunkId;
    }

    public void setChunkId(int chunkId) {
        this.chunkId = chunkId;
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

    public List<GenericFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<GenericFeature> features) {
        this.features = features;
    }
}
