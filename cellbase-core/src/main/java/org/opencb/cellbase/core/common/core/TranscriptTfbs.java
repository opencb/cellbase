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

public class TranscriptTfbs {

    private String tfName;
    private String pwm;
    private String chromosome;
    private int start;
    private int end;
    private String strand;
    private int relativeStart;
    private int relativeEnd;
    private float score;

    public TranscriptTfbs() {
    }

    public TranscriptTfbs(String tfName, String pwm, String chromosome,
                          Integer start, Integer end, String strand, Integer relativeStart,
                          Integer relativeEnd, Float score) {
        super();
        this.tfName = tfName;
        this.pwm = pwm;
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.strand = strand;
        this.relativeStart = relativeStart;
        this.relativeEnd = relativeEnd;
        this.score = score;
    }

    public String getTfName() {
        return tfName;
    }

    public void setTfName(String tfName) {
        this.tfName = tfName;
    }

    public String getPwm() {
        return pwm;
    }

    public void setPwm(String pwm) {
        this.pwm = pwm;
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

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public int getRelativeStart() {
        return relativeStart;
    }

    public void setRelativeStart(int relativeStart) {
        this.relativeStart = relativeStart;
    }

    public int getRelativeEnd() {
        return relativeEnd;
    }

    public void setRelativeEnd(int relativeEnd) {
        this.relativeEnd = relativeEnd;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

}
