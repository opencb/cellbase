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

package org.opencb.cellbase.app.transform.formats;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lcruz
 * @since 31/10/2014
 */
@Deprecated
public class ConservedRegionFeature {
    private String chromosome;
    private int start;
    private int end;
    private int chunk;
    private List<ConservedRegionSource> sources;

    public ConservedRegionFeature(String chromosome, int start, int end, int chunk) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.chunk = chunk;
        this.sources = new ArrayList<>();
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
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

    public int getChunk() {
        return chunk;
    }

    public void setChunk(int chunk) {
        this.chunk = chunk;
    }

    public List<ConservedRegionSource> getSources() {
        return sources;
    }

    public void setSources(List<ConservedRegionSource> sources) {
        this.sources = sources;
    }

    public void addSource(String type, List<Float> values) {
        this.sources.add(new ConservedRegionSource(type, values));
    }

    public static class ConservedRegionSource {
        private String type;
        private List<Float> values;

        public ConservedRegionSource(String type, List<Float> values) {
            this.type = type;
            this.values = values;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Float> getValues() {
            return values;
        }

        public void setValues(List<Float> values) {
            this.values = values;
        }
    }
}
