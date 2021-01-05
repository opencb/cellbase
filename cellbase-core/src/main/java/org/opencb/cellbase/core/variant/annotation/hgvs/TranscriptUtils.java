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

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.Xref;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;

import java.util.List;

public class TranscriptUtils {

    private Transcript transcript;

    public TranscriptUtils(Transcript transcript) {
        this.transcript = transcript;
    }

    public boolean isCoding() {
        // 0 in the cdnaCodingEnd means that the transcript doesn't
        // have a coding end <==> is non coding. Just annotating
        // coding transcripts in a first approach
        return transcript.getCdnaCodingEnd() != 0;
    }

    public boolean hasUnconfirmedStart() {
        return transcript.unconfirmedStart() || (transcript.getProteinSequence() != null
                && transcript.getProteinSequence().startsWith(VariantAnnotationUtils.UNKNOWN_AMINOACID));
    }

    public boolean hasUnconfirmedSEnd() {
        return transcript.unconfirmedEnd() || (transcript.getProteinSequence() != null
                && transcript.getProteinSequence().endsWith(VariantAnnotationUtils.UNKNOWN_AMINOACID));
    }

    public int cdnaToCds(int cdnaPosition) {
        int cdsPosition = 0;
        if (cdnaPosition >= transcript.getCdnaCodingStart()) {
            cdsPosition = transcript.getCdnaCodingStart() - cdnaPosition + 1;
        }
        return cdsPosition;
    }

    /**
     * Returns cDNA position when CDS is valid, 0 otherwise.
     * @param cdsPosition CDS position
     * @return CDS position or 0 if CDS is not valid
     */
    public int cdsToCdna(int cdsPosition) {
        int cdnaPosition = 0;
        // TODO We need to make sure CdsLength includes the STOP codon
        if (cdsPosition > 0 && cdsPosition <= transcript.getCdsLength() + 3) {
            cdnaPosition = cdsPosition + transcript.getCdnaCodingStart() - 1;
        }
        return cdnaPosition;
    }

    public int genomicsToCdna(int position) {
        int cds = genomicsToCds(position);
        return cdsToCdna(cds);
    }

    public int genomicsToCds(int position) {
        return 0;
    }

    public int getFirstCodonPosition() {
        int cdnaCodingStart = transcript.getCdnaCodingStart();
        // Unconfirmed start transcript always have a CdnaCodingStart=1, we need to increment the phase to get the first codon position
        if (transcript.unconfirmedStart()) {
            cdnaCodingStart += getFirstCodonPhase();
        }
        return cdnaCodingStart;
    }

    /**
     * Returns the exon phase for the first codon, this is always 0 for normal transcripts.
     * Different values are expected for unconfirmed transcripts.
     * @return Firs exon phase.
     */
    public int getFirstCodonPhase() {
        // This works fine for both normal and unconfirmed transcripts
        for (Exon exon : transcript.getExons()) {
            if (exon.getPhase() != -1) {
                return exon.getPhase();
            }
        }
        return -1;
    }

    public String getCodon(int codonPosition) {
        if (codonPosition > 0) {
            int cdsCodonStart = ((codonPosition - 1) * 3) + 1;
            if (hasUnconfirmedStart()) {
                // firstCodonPhase is 1. Codon 4 starts at position 8:
                //  cDNA        1    2      5      8      11
                //  codon       1    2      3      4      5
                //  sequence    G    CAG    ATG    GCT    TAT
                int firstCodonPhase = getFirstCodonPhase();

                if (codonPosition == 1 && firstCodonPhase != 0) {
                    return StringUtils.repeat('N', 3 - firstCodonPhase)
                            + transcript.getcDnaSequence().substring(0, firstCodonPhase);
                }

                // If first phase is not 0 we need to adjust codons to fetch a real one
                if (firstCodonPhase != 0) {
                    cdsCodonStart -= (3 - firstCodonPhase);
                }
            }
            int cdnaCodonStart = cdsToCdna(cdsCodonStart);
            // Adjust for manipulating strings, set to be zero base
            cdnaCodonStart = cdnaCodonStart - 1;

            int cdnaCodonEnd = Math.min(cdnaCodonStart + 3, transcript.getCdnaCodingEnd());
            return transcript.getcDnaSequence().substring(cdnaCodonStart, cdnaCodonEnd);
        }
        return "";
    }

    /**
     * Returns the codon position 1-based for a CDS position.
     * @param cdsPosition CDS position
     * @return Codon position
     */
    public int getCodonPosition(int cdsPosition) {
        // cdsPosition might need adjusting for transcripts with unclear start
        // Found GRCh38 transcript which does not have the unconfirmed start flag BUT the first aa is an X;
        // ENST00000618610 (ENSP00000484524)
        if (hasUnconfirmedStart()) {
            int firstCodonPhase = getFirstCodonPhase();
            // firstCodingExonPhase is the ENSEMBL's annotated phase for the transcript, which takes following values
            // - 0 if fits perfectly with the reading frame, i.e.
            // Sequence ---------ACTTACGGTC
            // Codons            ---|||---|||
            // - 1 if shifted one position, i.e.
            // Sequence ---------ACTTACGGTC
            // Codons             ---|||---||||
            // - 2 if shifted two positions, i.e.
            // Sequence ---------ACTTACGGTC
            // Codons              ---|||---|||
            if (firstCodonPhase != 0) {
                cdsPosition = cdsPosition + (3 - firstCodonPhase);
            }

//            if (cdsPosition > firstCodonPhase) {
//                cdsPosition += firstCodonPhase;
//            } else {
//                return 0;
//            }
        }
        // We add 1 to get the position not index
        return ((cdsPosition - 1) / 3) + 1;
    }

    /**
     * Returns the position 1-based in the codon.
     * @param cdsPosition CDS position
     * @return Valid values are 1, 2 or 3. When incomplete codon then 0 is returned.
     */
    public int getPositionAtCodon(int cdsPosition) {
        // phase might need adjusting for transcripts with unclear start
        // Found GRCh38 transcript which does not have the unconfirmed start flag BUT the first aa is an X;
        // ENST00000618610 (ENSP00000484524)
        if (hasUnconfirmedStart()) {
            int firstCodonPhase = getFirstCodonPhase();
            // firstCodingExonPhase is the ENSEMBL's annotated phase for the transcript, which takes following values
            // - 0 if fits perfectly with the reading frame, i.e.
            // Sequence ---------ACTTACGGTC
            // Codons            ---|||---|||
            // - 1 if shifted one position, i.e.
            // Sequence ---------ACTTACGGTC
            // Codons             ---|||---||||
            // - 2 if shifted two positions, i.e.
            // Sequence ---------ACTTACGGTC
            // Codons              ---|||---|||
//            if (firstCodonPhase != 0) {
//                cdsPosition = cdsPosition + (3 - firstCodonPhase);
//            }
//            System.out.println("cdsPosition = " + cdsPosition);
//            System.out.println("firstCodonPhase = " + firstCodonPhase);
            if (cdsPosition > firstCodonPhase) {
                cdsPosition -= firstCodonPhase;
//                cdsPosition += (3 - firstCodonPhase);
            } else {
                // If CDS position belongs to the first incomplete codon we return 0
                return 0;
            }
        }
        // We add 1 to get the position not index
        return ((cdsPosition - 1) % 3) + 1;
    }

    public String getXrefId(String sourceDatabase) {
        List<Xref> xrefs = transcript.getXrefs();
         if (xrefs != null && !xrefs.isEmpty()) {
            for (Xref xref : xrefs) {
                if (sourceDatabase.equals(xref.getDbName())) {
                    return xref.getId();
                }
            }
        }
        return null;
    }

    public boolean isExonic(int genomicPosition) {
        return isExonic(genomicPosition, genomicPosition);
    }

    public boolean isExonic(int start, int end) {
        for (Exon exon : transcript.getExons()) {
            if (start == end) {
                if (start >= exon.getStart() && start <= exon.getEnd()) {
                    return true;
                }
            } else {
                if (start >= exon.getStart() && start <= exon.getEnd() && end >= exon.getStart() && end <= exon.getEnd()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isExonicSpliceSite(int position) {
        for (Exon exon : transcript.getExons()) {
            if (position > exon.getEnd() - 2 && position <= exon.getEnd()) {
                return true;
            }
        }
        return false;
    }

    public boolean isExonicSpliceSite(int start, int end) {
        for (Exon exon : transcript.getExons()) {
            if ((start > exon.getEnd() - 2 && start <= exon.getEnd()) || (end > exon.getEnd() - 2 && end <= exon.getEnd())) {
                return true;
            }
        }
        return false;
    }

    public String getFormattedCdnaSequence() {
        return this.getFormattedCdnaSequence(transcript.getcDnaSequence());
    }

    /**
     * Calculates de cDNA sequence applying the genomic variant provided. The type of variant (SNV, INDEL, ...), the transcript
     * unconfirmed start, the strand and the UTRs are taken care of.
     * @param variant A genomic variant with the coordinates dna the alleles
     * @return Alternate cDNA sequence caulculated.
     */
    protected String getAlternateCdnaSequence(Variant variant) {
        if (variant.getEnd() < transcript.getGenomicCodingStart() || variant.getStart() > transcript.getGenomicCodingEnd()) {
            return transcript.getcDnaSequence();
        }

        // Create a StringBuilder to easily mannipulate the string
        StringBuilder alternateCdnaSequence = new StringBuilder(transcript.getcDnaSequence());
        String alternate = transcript.getStrand().equals("+")
                ? variant.getAlternate()
                : VariantAnnotationUtils.reverseComplementary(variant.getAlternate());

        // Genomic to cDNA
        int variantCdsPosition = HgvsCalculator.getCdsStart(transcript, variant.getStart());
        int cdnaVariantIndex = cdsToCdna(variantCdsPosition) - 1;

        switch (variant.getType()) {
            case SNV:
                if (transcript.getStrand().equals("-")) {
                    // FIXME a error on HgvsCalculator.getCdsStart
                    cdnaVariantIndex--;
                }
                alternateCdnaSequence.setCharAt(cdnaVariantIndex, alternate.charAt(0));
                break;
            case INSERTION:
            case DELETION:
            case INDEL:
                if (StringUtils.isBlank(variant.getReference()) || variant.getReference().equals("-")) {
                    // Insertion
                    alternateCdnaSequence.insert(cdnaVariantIndex, alternate);
                } else {
                    // Deletion
                    if (StringUtils.isBlank(variant.getAlternate()) || variant.getAlternate().equals("-")) {

                        String referenceAllele = variant.getReference();
                        if (transcript.getStrand().equals("+")) {
                            if (variant.getStart() < transcript.getGenomicCodingStart()
                                    && variant.getEnd() >= transcript.getGenomicCodingStart()) {
                                referenceAllele = referenceAllele.substring(transcript.getGenomicCodingStart() - variant.getStart());
                                variantCdsPosition = HgvsCalculator.getCdsStart(transcript, transcript.getGenomicCodingStart());
                            } else {
                                variantCdsPosition = HgvsCalculator.getCdsStart(transcript, variant.getStart());
                            }
                        } else {
                            if (variant.getStart() <= transcript.getGenomicCodingEnd()
                                    && variant.getEnd() > transcript.getGenomicCodingEnd()) {
                                referenceAllele = referenceAllele.substring(0, transcript.getGenomicCodingEnd() - variant.getStart() + 1);
                                variantCdsPosition = HgvsCalculator.getCdsStart(transcript, transcript.getGenomicCodingEnd());
                            } else {
                                variantCdsPosition = HgvsCalculator.getCdsStart(transcript, variant.getEnd());
                            }
                            // FIXME Method HgvsCalculator.getCdsStart wringly returns +1 for reverse strand
                            variantCdsPosition--;
                        }
                        cdnaVariantIndex = cdsToCdna(variantCdsPosition) - 1;
                        alternateCdnaSequence.replace(cdnaVariantIndex, cdnaVariantIndex + referenceAllele.length(), "");
                    } else {
                        System.out.println("No valid INDEL variant: " + variant.getId());
                        return null;
                    }
                }
                break;
            default:
                return null;
        }
        return alternateCdnaSequence.toString();
    }

    public String getFormattedCdnaSequence(String cdnaSequence) {
        StringBuilder cdnaPositions = new StringBuilder();
        StringBuilder codonPositions = new StringBuilder();
        StringBuilder formattedCdnaSequence = new StringBuilder();
        StringBuilder aaPositions = new StringBuilder();
        StringBuilder proteinSequence = new StringBuilder();
        StringBuilder proteinCodedSequence = new StringBuilder();

        String separator = "    ";

        int position = 1;
        int aaPosition = 1;
        int transcriptPhase = (transcript.getCdnaCodingStart() - 1) % 3;

        // Unconfirmed start transcripts ALWAYS start at position 1
        if (hasUnconfirmedStart()) {
            position = transcriptPhase != 0 ? 0 : 1;
            transcriptPhase = getFirstCodonPhase();
        }

        if (transcriptPhase > 0) {
            cdnaPositions.append(StringUtils.rightPad(String.valueOf(position == 0 ? 1 : position), transcriptPhase)).append(separator);
            codonPositions.append(StringUtils.rightPad(String.valueOf(position++), transcriptPhase)).append(separator);
            formattedCdnaSequence.append(cdnaSequence.substring(0, transcriptPhase)).append(separator);

            if (transcript.getProteinSequence().startsWith("X")) {
                aaPositions.append(StringUtils.rightPad("1", transcriptPhase)).append(separator);
                proteinSequence.append(StringUtils.rightPad("X", transcriptPhase)).append(separator);
                proteinCodedSequence.append(StringUtils.rightPad("X", transcriptPhase)).append(separator);
                aaPosition++;
            } else {
                aaPositions.append(StringUtils.repeat(' ', transcriptPhase)).append(separator);
                proteinSequence.append(StringUtils.repeat(' ', transcriptPhase)).append(separator);
                proteinCodedSequence.append(StringUtils.repeat(' ', transcriptPhase)).append(separator);
            }
        }

        String codon, aa;
        boolean insideCodingSequence = transcript.getCdnaCodingStart() == 1;
        for (int i = transcriptPhase; i < cdnaSequence.length(); i += 3) {
            cdnaPositions.append(StringUtils.rightPad(String.valueOf(i + 1), 4)).append("   ");
            codonPositions.append(StringUtils.rightPad(String.valueOf(position++), 4)).append("   ");
            codon = cdnaSequence.substring(i, Math.min(i + 3, cdnaSequence.length()));
            formattedCdnaSequence.append(codon).append(separator);

            // We are now inside the protein coding region
            if (!insideCodingSequence && i == transcript.getCdnaCodingStart() - 1) {
                insideCodingSequence = true;
            }

            if (insideCodingSequence) {
                aa = VariantAnnotationUtils.getAminoacid(VariantAnnotationUtils.MT.equals(transcript.getChromosome()), codon);
                if (aa != null && aa.equals("STOP")) {
                    // STOP has 4 letters so me need to remove 1 white space
                    proteinSequence.append(aa).append("   ");
                    insideCodingSequence = false;
                } else {
                    proteinSequence.append(aa).append(separator);
                }
                aaPositions.append(StringUtils.rightPad(String.valueOf(aaPosition++), 4)).append("   ");
                proteinCodedSequence.append(StringUtils.rightPad(VariantAnnotationUtils.TO_ABBREVIATED_AA.get(aa), 4)).append("   ");
            } else {
                aaPositions.append(" - ").append(separator);
                proteinSequence.append(" - ").append(separator);
                proteinCodedSequence.append(" - ").append(separator);
            }
        }

        return cdnaPositions.toString() + "\n"
                + codonPositions.toString() + "\n"
                + formattedCdnaSequence.toString() + "\n"
                + StringUtils.repeat('-', cdnaPositions.length()) + "\n"
                + aaPositions.toString() + "\n"
                + proteinSequence.toString() + "\n"
                + proteinCodedSequence.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TranscriptUtils{");
        sb.append("transcript=").append(transcript);
        sb.append('}');
        return sb.toString();
    }

    public Transcript getTranscript() {
        return transcript;
    }

    public TranscriptUtils setTranscript(Transcript transcript) {
        this.transcript = transcript;
        return this;
    }


}
