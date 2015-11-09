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

import java.util.List;

/**
 * Created by imedina on 06/11/15.
 */
public class GenomicPositionScore {

    private String chromosome;
    private int start;
    private int end;
    private String source;

    /**
        This value can code for 3 float numbers like in CADD scores, for this Long looks better than Double
     */
    private List<Long> values;

    public GenomicPositionScore(String chromosome, int start, int end, String type, List<Long> values) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.source = type;
        this.values = values;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GenomicPositionScore{");
        sb.append("chromosome='").append(chromosome).append('\'');
        sb.append(", start=").append(start);
        sb.append(", end=").append(end);
        sb.append(", source='").append(source).append('\'');
        sb.append(", values=").append(values);
        sb.append('}');
        return sb.toString();
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<Long> getValues() {
        return values;
    }

    public void setValues(List<Long> values) {
        this.values = values;
    }

}
