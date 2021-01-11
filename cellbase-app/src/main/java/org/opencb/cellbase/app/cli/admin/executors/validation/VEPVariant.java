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


package org.opencb.cellbase.app.cli.admin.executors.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VEPVariant {

    private int start;
    private int end;
    @JsonProperty("assembly_name")
    private String assemblyName;
    @JsonProperty("seq_region_name")
    private String seqRegionName;
    private String strand;
    private String input;
    @JsonProperty("allele_string")
    private String alleleString;
    private String id;
    @JsonProperty("most_severe_consequence")
    private String mostSevereConsequences;
    @JsonProperty("transcript_consequences")
    private Set<TranscriptConsequence> transcriptConsequences;

    public VEPVariant() {}

    public VEPVariant(int start, int end, String assemblyName, String seqRegionName, String strand, String input, String alleleString,
                      String id, String mostSevereConsequences, Set<TranscriptConsequence> transcriptConsequences) {
        this.start = start;
        this.end = end;
        this.assemblyName = assemblyName;
        this.seqRegionName = seqRegionName;
        this.strand = strand;
        this.input = input;
        this.alleleString = alleleString;
        this.id = id;
        this.mostSevereConsequences = mostSevereConsequences;
        this.transcriptConsequences = transcriptConsequences;
    }

    public String getVariantId() {
        return seqRegionName + ":" + start + ":" + alleleString.replace("/", ":");
    }

    public List<String> getProteinHgvs() {
        List<String> hgvs = new ArrayList();
        for (TranscriptConsequence transcriptConsequence : transcriptConsequences) {
            hgvs.add(transcriptConsequence.getHgvsp());
        }
        return hgvs;
    }

    public List<String> getTranscriptHgvs() {
        List<String> hgvs = new ArrayList();
        for (TranscriptConsequence transcriptConsequence : transcriptConsequences) {
            hgvs.add(transcriptConsequence.getHgvsc());
        }
        return hgvs;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VEPVariant{");
        sb.append("start=").append(start);
        sb.append(", end=").append(end);
        sb.append(", assemblName='").append(assemblyName).append('\'');
        sb.append(", seqRegionName='").append(seqRegionName).append('\'');
        sb.append(", strand='").append(strand).append('\'');
        sb.append(", input='").append(input).append('\'');
        sb.append(", alleleString='").append(alleleString).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", mostSevereConsequences='").append(mostSevereConsequences).append('\'');
        sb.append(", transcriptConsequences=").append(transcriptConsequences);
        sb.append('}');
        return sb.toString();
    }

    public int getStart() {
        return start;
    }

    public VEPVariant setStart(int start) {
        this.start = start;
        return this;
    }

    public int getEnd() {
        return end;
    }

    public VEPVariant setEnd(int end) {
        this.end = end;
        return this;
    }

    public String getAssemblyName() {
        return assemblyName;
    }

    public VEPVariant setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
        return this;
    }

    public String getSeqRegionName() {
        return seqRegionName;
    }

    public VEPVariant setSeqRegionName(String seqRegionName) {
        this.seqRegionName = seqRegionName;
        return this;
    }

    public String getStrand() {
        return strand;
    }

    public VEPVariant setStrand(String strand) {
        this.strand = strand;
        return this;
    }

    public String getInput() {
        return input;
    }

    public VEPVariant setInput(String input) {
        this.input = input;
        return this;
    }

    public String getAlleleString() {
        return alleleString;
    }

    public VEPVariant setAlleleString(String alleleString) {
        this.alleleString = alleleString;
        return this;
    }

    public String getId() {
        return id;
    }

    public VEPVariant setId(String id) {
        this.id = id;
        return this;
    }

    public String getMostSevereConsequences() {
        return mostSevereConsequences;
    }

    public VEPVariant setMostSevereConsequences(String mostSevereConsequences) {
        this.mostSevereConsequences = mostSevereConsequences;
        return this;
    }

    public Set<TranscriptConsequence> getTranscriptConsequences() {
        return transcriptConsequences;
    }

    public VEPVariant setTranscriptConsequences(Set<TranscriptConsequence> transcriptConsequences) {
        this.transcriptConsequences = transcriptConsequences;
        return this;
    }
}

