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

package org.opencb.cellbase.app.cli.validation;

public class TranscriptConsequence {
    private String geneId;
    private String transcriptId;
    private String variantAllele;
    private String codons;
    private String strand;

    private String hgvsc;
    private String hgvsp;

    private String aminoAcids;
    private String impact;

    private int proteinStart;
    private int proteinEnd;
    private int cdnaStart;
    private int cdnaEnd;
    private int cdsStart;
    private int cdsEnd;

    public TranscriptConsequence() {}

    public TranscriptConsequence(String geneId, String transcriptId, String variantAllele, String codons, String strand, String hgvsc,
                                 String hgvsp, String aminoAcids, String impact, int proteinStart, int proteinEnd, int cdnaStart,
                                 int cdnaEnd, int cdsStart, int cdsEnd) {
        this.geneId = geneId;
        this.transcriptId = transcriptId;
        this.variantAllele = variantAllele;
        this.codons = codons;
        this.strand = strand;
        this.hgvsc = hgvsc;
        this.hgvsp = hgvsp;
        this.aminoAcids = aminoAcids;
        this.impact = impact;
        this.proteinStart = proteinStart;
        this.proteinEnd = proteinEnd;
        this.cdnaStart = cdnaStart;
        this.cdnaEnd = cdnaEnd;
        this.cdsStart = cdsStart;
        this.cdsEnd = cdsEnd;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TranscriptConsequence{");
        sb.append("geneId='").append(geneId).append('\'');
        sb.append(", transcriptId='").append(transcriptId).append('\'');
        sb.append(", variantAllele='").append(variantAllele).append('\'');
        sb.append(", codons='").append(codons).append('\'');
        sb.append(", strand='").append(strand).append('\'');
        sb.append(", hgvsc='").append(hgvsc).append('\'');
        sb.append(", hgvsp='").append(hgvsp).append('\'');
        sb.append(", aminoAcids='").append(aminoAcids).append('\'');
        sb.append(", impact='").append(impact).append('\'');
        sb.append(", proteinStart=").append(proteinStart);
        sb.append(", proteinEnd=").append(proteinEnd);
        sb.append(", cdnaStart=").append(cdnaStart);
        sb.append(", cdnaEnd=").append(cdnaEnd);
        sb.append(", cdsStart=").append(cdsStart);
        sb.append(", cdsEnd=").append(cdsEnd);
        sb.append('}');
        return sb.toString();
    }

    public String getGeneId() {
        return geneId;
    }

    public TranscriptConsequence setGeneId(String geneId) {
        this.geneId = geneId;
        return this;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public TranscriptConsequence setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
        return this;
    }

    public String getVariantAllele() {
        return variantAllele;
    }

    public TranscriptConsequence setVariantAllele(String variantAllele) {
        this.variantAllele = variantAllele;
        return this;
    }

    public String getCodons() {
        return codons;
    }

    public TranscriptConsequence setCodons(String codons) {
        this.codons = codons;
        return this;
    }

    public String getStrand() {
        return strand;
    }

    public TranscriptConsequence setStrand(String strand) {
        this.strand = strand;
        return this;
    }

    public String getHgvsc() {
        return hgvsc;
    }

    public TranscriptConsequence setHgvsc(String hgvsc) {
        this.hgvsc = hgvsc;
        return this;
    }

    public String getHgvsp() {
        return hgvsp;
    }

    public TranscriptConsequence setHgvsp(String hgvsp) {
        this.hgvsp = hgvsp;
        return this;
    }

    public String getAminoAcids() {
        return aminoAcids;
    }

    public TranscriptConsequence setAminoAcids(String aminoAcids) {
        this.aminoAcids = aminoAcids;
        return this;
    }

    public String getImpact() {
        return impact;
    }

    public TranscriptConsequence setImpact(String impact) {
        this.impact = impact;
        return this;
    }

    public int getProteinStart() {
        return proteinStart;
    }

    public TranscriptConsequence setProteinStart(int proteinStart) {
        this.proteinStart = proteinStart;
        return this;
    }

    public int getProteinEnd() {
        return proteinEnd;
    }

    public TranscriptConsequence setProteinEnd(int proteinEnd) {
        this.proteinEnd = proteinEnd;
        return this;
    }

    public int getCdnaStart() {
        return cdnaStart;
    }

    public TranscriptConsequence setCdnaStart(int cdnaStart) {
        this.cdnaStart = cdnaStart;
        return this;
    }

    public int getCdnaEnd() {
        return cdnaEnd;
    }

    public TranscriptConsequence setCdnaEnd(int cdnaEnd) {
        this.cdnaEnd = cdnaEnd;
        return this;
    }

    public int getCdsStart() {
        return cdsStart;
    }

    public TranscriptConsequence setCdsStart(int cdsStart) {
        this.cdsStart = cdsStart;
        return this;
    }

    public int getCdsEnd() {
        return cdsEnd;
    }

    public TranscriptConsequence setCdsEnd(int cdsEnd) {
        this.cdsEnd = cdsEnd;
        return this;
    }
}

