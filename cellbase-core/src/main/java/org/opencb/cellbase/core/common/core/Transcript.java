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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class Transcript implements Serializable {

    private static final long serialVersionUID = 2069002722080532350L;

    private String id;
    private String name;
    private String biotype;
    private String status;
    private String chromosome;
    private int start;
    private int end;
    private String strand;
    private int genomicCodingStart;
    private int genomicCodingEnd;
    private int cdnaCodingStart;
    private int cdnaCodingEnd;
    private int cdsLength;
    private String proteinID;
    private String description;
    private List<Xref> xrefs;
    private List<TranscriptTfbs> tfbs;
    private List<Exon> exons;

    public Transcript() {

    }

    public Transcript(String id, String name, String biotype, String status, String chromosome, Integer start, Integer end,
                      String strand, Integer codingRegionStart, Integer codingRegionEnd, Integer cdnaCodingStart, Integer cdnaCodingEnd,
                      Integer cdsLength, String proteinId, String description, ArrayList<Xref> xrefs, ArrayList<Exon> exons,
                      ArrayList<TranscriptTfbs> tfbs) {
        this.id = id;
        this.name = name;
        this.biotype = biotype;
        this.status = status;
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.strand = strand;
        this.genomicCodingStart = codingRegionStart;
        this.genomicCodingEnd = codingRegionEnd;
        this.cdnaCodingStart = cdnaCodingStart;
        this.cdnaCodingEnd = cdnaCodingEnd;
        this.cdsLength = cdsLength;
        this.proteinID = proteinId;
        this.description = description;
        this.xrefs = xrefs;
        this.exons = exons;
        this.tfbs = tfbs;
    }


    @Override
    public String toString() {
        return "Transcript [id=" + id + ", name=" + name + ", biotype="
                + biotype + ", status=" + status + ", chromosome=" + chromosome
                + ", start=" + start + ", end=" + end + ", strand=" + strand
                + ", genomicCodingStart=" + genomicCodingStart
                + ", genomicCodingEnd=" + genomicCodingEnd
                + ", cdnaCodingStart=" + cdnaCodingStart + ", cdnaCodingEnd="
                + cdnaCodingEnd + ", cdsLength=" + cdsLength + ", proteinID="
                + proteinID + ", description=" + description + ", xrefs="
                + xrefs + ", tfbs=" + tfbs + ", exons=" + exons + "]";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiotype() {
        return biotype;
    }

    public void setBiotype(String biotype) {
        this.biotype = biotype;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getGenomicCodingStart() {
        return genomicCodingStart;
    }

    public void setGenomicCodingStart(int genomicCodingStart) {
        this.genomicCodingStart = genomicCodingStart;
    }

    public int getGenomicCodingEnd() {
        return genomicCodingEnd;
    }

    public void setGenomicCodingEnd(int genomicCodingEnd) {
        this.genomicCodingEnd = genomicCodingEnd;
    }

    public int getCdnaCodingStart() {
        return cdnaCodingStart;
    }

    public void setCdnaCodingStart(int cdnaCodingStart) {
        this.cdnaCodingStart = cdnaCodingStart;
    }

    public int getCdnaCodingEnd() {
        return cdnaCodingEnd;
    }

    public void setCdnaCodingEnd(int cdnaCodingEnd) {
        this.cdnaCodingEnd = cdnaCodingEnd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Exon> getExons() {
        return exons;
    }

    public void setExons(List<Exon> exons) {
        this.exons = exons;
    }

    public String getProteinID() {
        return proteinID;
    }

    public void setProteinID(String proteinID) {
        this.proteinID = proteinID;
    }

    public int getCdsLength() {
        return cdsLength;
    }

    public void setCdsLength(int cdsLength) {
        this.cdsLength = cdsLength;
    }

    public List<Xref> getXrefs() {
        return xrefs;
    }

    public void setXrefs(List<Xref> xrefs) {
        this.xrefs = xrefs;
    }

    public List<TranscriptTfbs> getTfbs() {
        return tfbs;
    }

    public void setTfbs(List<TranscriptTfbs> tfbs) {
        this.tfbs = tfbs;
    }

}
