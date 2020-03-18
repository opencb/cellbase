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

package org.opencb.cellbase.core.variant.annotation.hgvs;

/**
 * Created by fjlopez on 26/01/17.
 */

/**
 * Builder to get the hgvs identifier properly formatted.
 */
public class BuildingComponents {

    enum Kind { CODING, NON_CODING, INFRAME, FRAMESHIFT }

    // HGVS string parts
    private String prefix;
    private String chromosome;
    private String geneId;
    private String transcriptId;
    private String proteinId;
    private Kind kind;
    private String mutationType;
    private int start;
    private int end;
    private String referenceStart; // reference allele/amino acid at variant start
    private String referenceEnd; // reference allele/amino acid at variant end
    private String alternate; // alternate allele

    // cDNA-specific fields
    private CdnaCoord cdnaStart;
    private CdnaCoord cdnaEnd;

    public BuildingComponents() {

    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    public String getProteinId() {
        return proteinId;
    }

    public void setProteinId(String proteinId) {
        this.proteinId = proteinId;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public String getMutationType() {
        return mutationType;
    }

    public void setMutationType(String mutationType) {
        this.mutationType = mutationType;
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

    public String getReferenceStart() {
        return referenceStart;
    }

    public void setReferenceStart(String referenceStart) {
        this.referenceStart = referenceStart;
    }

    public String getReferenceEnd() {
        return referenceEnd;
    }

    public void setReferenceEnd(String referenceEnd) {
        this.referenceEnd = referenceEnd;
    }

    public String getAlternate() {
        return alternate;
    }

    public void setAlternate(String alternate) {
        this.alternate = alternate;
    }

    public CdnaCoord getCdnaStart() {
        return cdnaStart;
    }

    public void setCdnaStart(CdnaCoord cdnaStart) {
        this.cdnaStart = cdnaStart;
    }

    public CdnaCoord getCdnaEnd() {
        return cdnaEnd;
    }

    public void setCdnaEnd(CdnaCoord cdnaEnd) {
        this.cdnaEnd = cdnaEnd;
    }

//    /**
//     * Generate a HGVS string.
//     * @return String containing an HGVS formatted variant representation
//     */
//    public String format() {
////
//
//        StringBuilder allele = new StringBuilder();
//        allele.append(formatPrefix());  // if use_prefix else ''
//        allele.append(":");
//
//        if (kind.equals(Kind.CDNA_CODING)) {
//            allele.append("c.").append(formatCdna());
//        } else if (kind.equals(Kind.CDNA_NON_CODING)) {
//            allele.append("n.").append(formatCdna());
////    elif self.kind == 'p':
////    allele = 'p.' + self.format_protein()
////    elif self.kind == 'g':
////    allele = 'g.' + self.format_genome()
//        } else {
//            throw new NotImplementedException("HGVS calculation not implemented for variant " + chromosome + ":"
//                    + start + ":" + referenceStart + ":" + alternate + "; kind: " + kind);
//        }
////            if prefix:
//        return allele.toString();
////        else:
////                return allele
//    }
//
//    /**
//     * Generate HGVS trancript/geneId prefix.
//     * Some examples of full hgvs names with transcriptId include:
//     * NM_007294.3:c.2207A>C
//     * NM_007294.3(BRCA1):c.2207A>C
//     */
//    private String formatPrefix() {
////        if use_gene and self.geneId:
//        StringBuilder stringBuilder = new StringBuilder(transcriptId);
//        stringBuilder.append("(").append(geneId).append(")");
//
//        return stringBuilder.toString();
////        return self.transcriptId
//    }
//
//    /**
//     * Generate HGVS cDNA allele.
//     * Some examples include:
//     * Substitution: 101A>C,
//     * Indel: 3428delCinsTA, 1000_1003delATG, 1000_1001insATG
//     */
//    private String formatCdna() {
//        return formatCdnaCoords() + formatDnaAllele();
//    }
//
//    /**
//     * Generate HGVS DNA allele.
//     * @return
//     */
//    private String formatDnaAllele() {
//
////        We are not considering this case since would mean there's no variant
////        if self.mutation_type == '=':
////            #No change.
////            #example:
////        101 A =
////        return self.ref_allele + '='
//
//        if (">".equals(mutationType)) {
//            // SNP.
//            // example:
//            // 101 A > C
//            return referenceStart + '>' + alternate;
//        } else if ("delins".equals(mutationType)) {
//            // Indel.
//            // example:
//            // 112_117d elAGGTCAinsTG, 112_117d elinsTG
//            return "del" + referenceStart + "ins" + alternate;
//        } else if ("del".equals(mutationType)) {
//            // Delete, duplication.
//            // example:
//            // 1000_1003d elATG, 1000_1003d upATG
//            return mutationType + referenceStart;
//        } else if ("dup".equals(mutationType)) {
//            // Insertion normalized as duplication
//            // example:
//            // "ENST00000382869.3:c.1735+32dupA"
//            return mutationType + alternate;
//        } else if ("ins".equals(mutationType)) {
//            // Insert.
//            // example:
//            // 1000_1001 insATG
//            return mutationType + alternate;
//        } else {
//            throw new AssertionError("unknown mutation type: '" + mutationType + "'");
//        }
//    }
//
//    /**
//     * Generate HGVS cDNA coordinates string.
//     */
//    private String formatCdnaCoords() {
//        if (cdnaStart.equals(cdnaEnd)) {
//            return cdnaStart.toString();
//        } else {
//            return cdnaStart.toString() + "_" + cdnaEnd.toString();
//        }
//    }

}
