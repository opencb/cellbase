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

package org.opencb.cellbase.lib.variant.annotation;

import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.ExonOverlap;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.lib.managers.GenomeManager;
import org.opencb.cellbase.lib.variant.VariantAnnotationUtils;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import org.opencb.cellbase.core.db.api.core.GenomeDBAdaptor;

/**
 * Created by fjlopez on 23/07/15.
 */
public class ConsequenceTypeInsertionCalculator extends ConsequenceTypeCalculator {

    private static final String SYMBOLIC_START = "<";
    private static final float INVALID_OVERLAP_PERCENTAGE = -1;
    private int variantStart;
    private int variantEnd;
//    private GenomeDBAdaptor genomeDBAdaptor;

    public ConsequenceTypeInsertionCalculator(GenomeManager genomeManager) {
        this.genomeManager = genomeManager;
    }

    public List<ConsequenceType> run(Variant inputVariant, List<Gene> geneList, boolean[] overlapsRegulatoryRegion,
                                     QueryOptions queryOptions) {

        List<ConsequenceType> consequenceTypeList = new ArrayList<>();
        variant = inputVariant;
        variantEnd = variant.getStart();
        variantStart = variant.getStart() - 1;
        boolean isIntergenic = true;
        for (Gene currentGene : geneList) {
            gene = currentGene;
            String source = getSource(gene.getId());
            if (gene.getTranscripts() == null) {
                continue;
            }
            for (Transcript currentTranscript : gene.getTranscripts()) {
                isIntergenic = isIntergenic && (variantEnd < currentTranscript.getStart() || variantStart > currentTranscript.getEnd());
                transcript = currentTranscript;
                consequenceType = new ConsequenceType();
                consequenceType.setGeneName(gene.getName());
                consequenceType.setGeneId(gene.getId());
                consequenceType.setTranscriptId(transcript.getId());
                if (ParamConstants.QueryParams.ENSEMBL.key().equalsIgnoreCase(source)) {
                    consequenceType.setEnsemblGeneId(gene.getId());
                    consequenceType.setEnsemblTranscriptId(transcript.getId());
                }
                consequenceType.setStrand(transcript.getStrand());
                consequenceType.setBiotype(transcript.getBiotype());
                consequenceType.setSource(source);
                // deprecated
                consequenceType.setTranscriptAnnotationFlags(transcript.getFlags() != null
                        ? new ArrayList<>(transcript.getFlags())
                        : null);
                consequenceType.setTranscriptFlags(transcript.getFlags() != null
                        ? new ArrayList<>(transcript.getFlags())
                        : null);
                SoNames.clear();

                if (transcript.getStrand().equals("+")) {
                    // Check variant overlaps transcript start/end coordinates
                    if (variantEnd > transcript.getStart() && variantStart < transcript.getEnd()) {
                        solvePositiveTranscript(consequenceTypeList);
                    } else {
                        solveTranscriptFlankingRegions(VariantAnnotationUtils.UPSTREAM_GENE_VARIANT,
                                VariantAnnotationUtils.DOWNSTREAM_GENE_VARIANT);
                        if (SoNames.size() > 0) { // Variant does not overlap gene region, just may have upstream/downstream annotations
//                            consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                            consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                            consequenceTypeList.add(consequenceType);
                        }
                    }
                } else {
                    // Check variant overlaps transcript start/end coordinates
                    if (variantEnd > transcript.getStart() && variantStart < transcript.getEnd()) {
                        solveNegativeTranscript(consequenceTypeList);
                    } else {
                        solveTranscriptFlankingRegions(VariantAnnotationUtils.DOWNSTREAM_GENE_VARIANT,
                                VariantAnnotationUtils.UPSTREAM_GENE_VARIANT);
                        if (SoNames.size() > 0) { // Variant does not overlap gene region, just has upstream/downstream annotations
//                            consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                            consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                            consequenceTypeList.add(consequenceType);
                        }
                    }
                }
            }
        }

        solveIntergenic(consequenceTypeList, isIntergenic);
        solveRegulatoryRegions(overlapsRegulatoryRegion, consequenceTypeList);

        return consequenceTypeList;
    }

    protected void solveNonCodingNegativeTranscript() {
        Exon exon = transcript.getExons().get(0);
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;
        int exonNumber = NO_EXON_OVERLAP;

        if (firstCdsPhase == -1 && transcript.getGenomicCodingEnd() >= exon.getStart()) {
            firstCdsPhase = exon.getPhase();
        }
        if (variantStart < exon.getEnd()) {
            if (variantEnd >= exon.getStart()) {  // Variant end within the exon
                cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                consequenceType.setCdnaPosition(cdnaVariantStart);
                exonNumber = exon.getExonNumber();
//                consequenceType.setExonNumber(exon.getExonNumber());
                if (variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                } else {  // Only variant start within the exon  ---ES||||||||||||----
                    cdnaVariantEnd = cdnaVariantStart + 1;  // To account for those insertions in the last nucleotide of the exon
                }
            }
        } else if (variantStart == exon.getEnd()) {
            cdnaVariantEnd = cdnaExonEnd - (variantEnd - exon.getStart());
            cdnaVariantStart = cdnaVariantEnd - 1;  // To account for those insertions in the first nucleotide of the exon
            exonNumber = exon.getExonNumber();
//            consequenceType.setExonNumber(exon.getExonNumber());
        }

        int exonCounter = 1;
        while (exonCounter < transcript.getExons().size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction
            int prevSpliceSite = exon.getStart() - 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            // Set firsCdsPhase only when the first coding exon is reached
            if (firstCdsPhase == -1 && exon.getGenomicCodingEnd() >= exon.getStart()) {
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(exon.getEnd() + 1, prevSpliceSite, VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_DONOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if (variantStart < exon.getEnd()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variantEnd >= exon.getStart()) {  // Variant end within the exon
                    cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                    consequenceType.setCdnaPosition(cdnaVariantStart);
                    exonNumber = exon.getExonNumber();
//                    consequenceType.setExonNumber(exon.getExonNumber());
                    if (variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||SE|||||||||----
                        cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
//                        consequenceType.setExonNumber(exon.getExonNumber());
                    }
                }
            } else if (variantStart == exon.getEnd()) {  // Only variant start within the exon  ----||||||||||||||SE---
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                exonNumber = exon.getExonNumber();
//                consequenceType.setExonNumber(exon.getExonNumber());
            } else {  // Variant does not include this exon, variant is located before this exon
                variantAhead = false;
            }
            exonCounter++;
        }
        if (exonNumber != NO_EXON_OVERLAP) {
            consequenceType.setExonOverlap(
                    Collections.singletonList(new ExonOverlap((new StringBuilder()).append(exonNumber).append("/")
                            .append(transcript.getExons().size()).toString(),
                    INVALID_OVERLAP_PERCENTAGE)));
        }
        solveMiRNA(cdnaVariantStart, cdnaVariantEnd, junctionSolution[1]);
    }

    protected void solveCodingNegativeTranscript() {
        Exon exon = transcript.getExons().get(0);
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;
        int exonNumber = NO_EXON_OVERLAP;

        if (firstCdsPhase == -1 && transcript.getGenomicCodingEnd() >= exon.getStart()) {
            firstCdsPhase = exon.getPhase();
        }
        if (variantStart < exon.getEnd()) {
            if (variantEnd >= exon.getStart()) {  // Variant end within the exon
                cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                consequenceType.setCdnaPosition(cdnaVariantStart);
                exonNumber = exon.getExonNumber();
//                consequenceType.setExonNumber(exon.getExonNumber());
                if (variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                } else {  // Only variant start within the exon  ---ES||||||||||||----
                    cdnaVariantEnd = cdnaVariantStart + 1;  // To account for those insertions in the last nucleotide of the exon
                }
            }
        } else if (variantStart == exon.getEnd()) {
            cdnaVariantEnd = cdnaExonEnd - (variantEnd - exon.getStart());
            cdnaVariantStart = cdnaVariantEnd - 1;  // To account for those insertions in the first nucleotide of the exon
            exonNumber = exon.getExonNumber();
//            consequenceType.setExonNumber(exon.getExonNumber());
        }

        int exonCounter = 1;
        // This is not a do-while since we cannot call solveJunction  until
        while (exonCounter < transcript.getExons().size() && variantAhead) {
            int prevSpliceSite = exon.getStart() - 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            transcriptSequence = exon.getSequence() + transcriptSequence;
            // Set firsCdsPhase only when the first coding exon is reached
            if (firstCdsPhase == -1 && exon.getGenomicCodingEnd() >= exon.getStart()) {
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(exon.getEnd() + 1, prevSpliceSite, VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_DONOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if (variantStart < exon.getEnd()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variantEnd >= exon.getStart()) {  // Variant end within the exon
                    cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                    consequenceType.setCdnaPosition(cdnaVariantStart);
                    exonNumber = exon.getExonNumber();
//                    consequenceType.setExonNumber(exon.getExonNumber());
                    if (variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||SE|||||||||----
                        cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
//                        consequenceType.setExonNumber(exon.getExonNumber());
                    }
                }
            } else if (variantStart == exon.getEnd()) {  // Only variant start within the exon  ----||||||||||||||SE---
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                exonNumber = exon.getExonNumber();
//                consequenceType.setExonNumber(exon.getExonNumber());
            } else {  // Variant does not include this exon, variant is located before this exon
                variantAhead = false;
            }
            exonCounter++;
        }
        if (exonNumber != NO_EXON_OVERLAP) {
            consequenceType.setExonOverlap(Collections.singletonList(new ExonOverlap(
                    (new StringBuilder()).append(exonNumber).append("/").append(transcript.getExons().size()).toString(),
                    INVALID_OVERLAP_PERCENTAGE)));
        }
        // Is not intron variant (both ends fall within the same intron)
        if (!junctionSolution[1]) {
            if (cdnaVariantStart == -1 && cdnaVariantEnd != -1) {  // To account for those insertions in the 3' end of an intron
                cdnaVariantStart = cdnaVariantEnd - 1;
            } else if (cdnaVariantEnd == -1 && cdnaVariantStart != -1) {  // To account for those insertions in the 5' end of an intron
                cdnaVariantEnd = cdnaVariantStart + 1;
            }
            solveExonVariantInNegativeTranscript(splicing, transcriptSequence, cdnaVariantStart, cdnaVariantEnd,
                    firstCdsPhase);
        }
    }

    private void solveExonVariantInNegativeTranscript(boolean splicing, String transcriptSequence,
                                                      int cdnaVariantStart, int cdnaVariantEnd, int firstCdsPhase) {
        if (variantEnd > transcript.getGenomicCodingEnd()) {
            if (transcript.getEnd() > transcript.getGenomicCodingEnd()
                    || transcript.unconfirmedStart()) { // Check transcript has 3 UTR
                SoNames.add(VariantAnnotationUtils.FIVE_PRIME_UTR_VARIANT);
            }
        } else if (variantEnd >= transcript.getGenomicCodingStart()) {
            // Need to define a local cdnaCodingStart because may modified in two lines below
            int cdnaCodingStart = transcript.getCdnaCodingStart();
            cdnaCodingStart = setCdsAndProteinPosition(cdnaVariantStart, firstCdsPhase, cdnaCodingStart);
            if (variantStart >= transcript.getGenomicCodingStart()) {  // Variant start also within coding region
                solveCodingExonVariantInNegativeTranscript(transcriptSequence, cdnaCodingStart, cdnaVariantStart,
                        cdnaVariantEnd);
            } else if (transcript.getStart() < transcript.getGenomicCodingStart()
                    || transcript.unconfirmedEnd()) { // Check transcript has 3 UTR)
                SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
            }
        } else {
            if (transcript.getStart() < transcript.getGenomicCodingStart()
                    || transcript.unconfirmedEnd()) { // Check transcript has 3 UTR)
                SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
            }
        }
    }

    private void solveCodingExonVariantInNegativeTranscript(String transcriptSequence, int cdnaCodingStart,
                                                            int cdnaVariantStart, int cdnaVariantEnd) {
        if (cdnaVariantStart != -1) {  // Insertion
            // cdnaVariantStart=null if variant is intronic. cdnaCodingStart<1 if cds_start_NF and phase!=0
            if (cdnaVariantStart < (cdnaCodingStart + 2) && !transcript.unconfirmedStart()) {
                solveStartCodonNegativeVariant(transcriptSequence, transcript.getCdnaCodingStart(), cdnaVariantEnd);
            }
//            int finalNtPhase = (transcriptSequence.length() - cdnaCodingStart) % 3;
            int finalNtPhase = (transcript.getCdnaCodingEnd() - cdnaCodingStart) % 3;
//            if ((cdnaVariantStart >= (transcriptSequence.length() - finalNtPhase)) &&
            //  Variant in the last codon of a transcript without stop codon. finalNtPhase==2 if the cds length is multiple of 3.
            if ((cdnaVariantEnd >= (transcript.getCdnaCodingEnd() - finalNtPhase)) && finalNtPhase != 2) {
//                    (transcript.getStart() == transcript.getGenomicCodingStart()) && finalNtPhase != 2) {
                SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);
            }
//            if (variant.getAlternate().length() % 3 == 0) {
            // <INS> variants may have uncomplete alternate sequence and therefore unknown length
            if (!variant.getLength().equals(Variant.UNKNOWN_LENGTH)) {
                if (variant.getLength() % 3 == 0) {
                    SoNames.add(VariantAnnotationUtils.INFRAME_INSERTION);
                } else {
                    SoNames.add(VariantAnnotationUtils.FRAMESHIFT_VARIANT);
                }
            } else {
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
            }
            // Be careful, cdnaVariantEnd is being used in this case!!!
            solveStopCodonNegativeInsertion(transcriptSequence, cdnaCodingStart, cdnaVariantEnd);
        } else {
            SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
        }

    }

    private void solveStartCodonNegativeVariant(String transcriptSequence, int cdnaCodingStart, int cdnaVariantEnd) {
        // Not necessary to include % 3 since if we get here we already know that the difference is < 3
        Integer variantPhaseShift = cdnaVariantEnd - cdnaCodingStart;
        int modifiedCodonStart = cdnaVariantEnd - variantPhaseShift;
        // Complementary of start codon ATG - Met (already reversed)
        char[] modifiedCodonArray = getReverseComplementaryCodon(transcriptSequence, modifiedCodonStart);
        String referenceCodon = String.valueOf(modifiedCodonArray);
        // Both MT and non-MT code use same codification for Metionine
        if (VariantAnnotationUtils.isStartCodon(MT.equals(variant.getChromosome()), String.valueOf(modifiedCodonArray))) {
            int i = transcriptSequence.length() - cdnaVariantEnd;  // Position (0 based index) in transcriptSequence of
            String reverseAlternate = (new StringBuilder(variant.getAlternate())).reverse().toString();
            // the first nt after the deletion
            int codonPosition;
            int alternatePosition = 0;
            // If we get here, cdnaVariantStart and cdnaVariantEnd != -1; this is an assumption that was made just before
            // calling this method
            for (codonPosition = variantPhaseShift; codonPosition < 3; codonPosition++) {
                char substitutingNt;
                // Means we've reached the end of the alternate. This can only happen if it's an insertion of one nt and
                // it occurs between the first and second start codon base. THEREFORE, it can enter in this if one time
                // at most
                if (alternatePosition >= reverseAlternate.length()) {
                    // cdnaVariantStart is base 1 and the string is base 0, therefore this is actually getting base at
                    // position cdnaVariantEnd
                    substitutingNt = VariantAnnotationUtils.COMPLEMENTARY_NT.get(transcriptSequence.charAt(i));
                } else {
                    // Paste alternae nts after deletion in the corresponding codon position
                    substitutingNt = VariantAnnotationUtils.COMPLEMENTARY_NT.get(reverseAlternate.charAt(alternatePosition));
                    alternatePosition++;
                }
                modifiedCodonArray[codonPosition] = substitutingNt;
            }

            if (VariantAnnotationUtils.isSynonymousCodon(MT.equals(variant.getChromosome()), referenceCodon,
                    String.valueOf(modifiedCodonArray))) {
                SoNames.add(VariantAnnotationUtils.START_RETAINED_VARIANT);
            } else {
                SoNames.add(VariantAnnotationUtils.START_LOST);
            }
        }
    }

    private void solveStopCodonNegativeInsertion(String transcriptSequence, Integer cdnaCodingStart,
                                                 Integer cdnaVariantEnd) {
        Integer variantPhaseShift = (cdnaVariantEnd - cdnaCodingStart) % 3;
        int modifiedCodonStart = cdnaVariantEnd - variantPhaseShift;
        if (modifiedCodonStart > 0 && (modifiedCodonStart + 2) <= transcriptSequence.length()) {
            String alternate = getRightAlternate();
            String reverseTranscriptSequence = new StringBuilder(
                    transcriptSequence.substring(((transcriptSequence.length() - cdnaVariantEnd) > 2)
                                    ? (transcriptSequence.length() - cdnaVariantEnd - 2)
                                    : 0,  // Be careful reaching the end of the transcript sequence
                            // Rigth limit of the substring sums +1 because substring does not include that position
                            transcriptSequence.length() - cdnaVariantEnd + 1)).reverse().toString();
            char[] referenceCodonArray = getReverseComplementaryCodon(transcriptSequence, modifiedCodonStart);
            String referenceCodon = String.valueOf(referenceCodonArray);
            char[] modifiedCodonArray = referenceCodonArray.clone();
            char[] altArray = (new StringBuilder(alternate).reverse().toString()).toCharArray();
            int i = 0;
            int reverseTranscriptSequencePosition = 0;
            int modifiedCodonPosition;
            int modifiedCodonPositionStart = variantPhaseShift;

            // Char arrays to contain the upper/lower-case formatted strings for the codon change, e.g. aGT/ATG
            char[] formattedReferenceCodonArray = referenceCodon.toLowerCase().toCharArray();
            char[] formattedModifiedCodonArray = referenceCodon.toLowerCase().toCharArray();
            boolean useMitochondrialCode = variant.getChromosome().equals("MT");
            boolean firstCodon = true;

            do {
                for (modifiedCodonPosition = modifiedCodonPositionStart;
                    // Paste alternative nt in the corresponding codon position
                     (modifiedCodonPosition < 3 && i < variant.getAlternate().length()); modifiedCodonPosition++) {
                    modifiedCodonArray[modifiedCodonPosition] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(altArray[i]);

                    // Edit modified nt to make it upper-case in the formatted strings
                    formattedReferenceCodonArray[modifiedCodonPosition]
                            = Character.toUpperCase(formattedReferenceCodonArray[modifiedCodonPosition]);
                    formattedModifiedCodonArray[modifiedCodonPosition]
                            = Character.toUpperCase(modifiedCodonArray[modifiedCodonPosition]);

                    i++;
                }

                reverseTranscriptSequencePosition = updateNegativeInsertionCodonArrays(reverseTranscriptSequence,
                        formattedReferenceCodonArray, reverseTranscriptSequencePosition, modifiedCodonPosition,
                        formattedModifiedCodonArray, modifiedCodonArray);

                // Set codon str, protein ref and protein alt ONLY for the first codon mofified by the insertion
                firstCodon = setInsertionAlleleAminoacidChange(referenceCodon, modifiedCodonArray,
                        formattedReferenceCodonArray, formattedModifiedCodonArray, useMitochondrialCode, firstCodon);

                decideStopCodonModificationAnnotation(SoNames, String.valueOf(referenceCodonArray),
                        String.valueOf(modifiedCodonArray), variant.getChromosome().equals("MT"));
                modifiedCodonPositionStart = 0;  // Reset the position where the next modified codon must be started to be filled
            }
            while (i < variant.getAlternate().length());  // All posible new codons generated by the inserted sequence must be checked
        }

    }

    private String getRightAlternate() {
        // alternate == <INS> <=> alternate seq is not complete and must be found within the variant.sv.rightSvInsSeq
        if (variant.getAlternate().startsWith(SYMBOLIC_START)) {
            if (variant.getSv() != null && variant.getSv().getRightSvInsSeq() != null) {
                return variant.getSv().getRightSvInsSeq();
            } else {
                throw new IllegalArgumentException("Insertion found with <INS> tag in the alternate and no alternate"
                        + " sequence available within variant.sv.rightSvInsSeq; Malformed variant object. Please, check.");
            }
        } else {
            return variant.getAlternate();
        }
    }

    private void solveTranscriptFlankingRegions(String leftRegionTag, String rightRegionTag) {
        // Variant within -5kb region
        if (variantEnd > (transcript.getStart() - 5001) && variantStart < transcript.getStart()) {
            // Variant within -2kb region
            if (variantEnd > (transcript.getStart() - 2001)) {
                SoNames.add("2KB_" + leftRegionTag.replace(DOWN_UP_STREAM_GENE_TAG, ""));
            } else {
                SoNames.add(leftRegionTag);
            }
        }
        // Variant within +5kb region
        if (variantEnd > transcript.getEnd() && variantStart < (transcript.getEnd() + 5001)) {
            // Variant within +2kb region
            if (variantStart < (transcript.getEnd() + 2001)) {
                SoNames.add("2KB_" + rightRegionTag.replace(DOWN_UP_STREAM_GENE_TAG, ""));
            } else {
                SoNames.add(rightRegionTag);
            }
        }
    }

    protected void solveNonCodingPositiveTranscript() {
        Exon exon = transcript.getExons().get(0);
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;
        int exonNumber = NO_EXON_OVERLAP;

        if (transcript.getGenomicCodingStart() <= exon.getEnd()) {
            firstCdsPhase = exon.getPhase();
        }

        if (variantEnd > exon.getStart()) {
            if (variantStart <= exon.getEnd()) { // Variant start within the exon (this is a insertion, variantEnd=variantStart+1)
                cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                consequenceType.setCdnaPosition(cdnaVariantStart);
                exonNumber = exon.getExonNumber();
//                consequenceType.setExonNumber(exon.getExonNumber());
                if (variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||SE||||||||----
                    cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                } else {  // Only variant start within the exon  ---||||||||||||SE----
                    cdnaVariantEnd = cdnaVariantStart + 1;  // To account for those insertions in the last nucleotide of the exon
                }

            }
        } else if (variantEnd == exon.getStart()) {  // Only variant end within the exon  ----E|||||||||||||----
            // We do not contemplate that variant end can be located before this exon since this is the first exon
            cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
            cdnaVariantStart = cdnaVariantEnd - 1;  // To account for those insertions in the first nucleotide of the exon
            exonNumber = exon.getExonNumber();
//            consequenceType.setExonNumber(exon.getExonNumber());
        }

        int exonCounter = 1;
        // This is not a do-while since we cannot call solveJunction until
        while (exonCounter < transcript.getExons().size() && variantAhead) {
            int prevSpliceSite = exon.getEnd() + 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            // Set firsCdsPhase only when the first coding exon is reached
            if (firstCdsPhase == -1 && transcript.getGenomicCodingStart() <= exon.getEnd()) {
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(prevSpliceSite, exon.getStart() - 1, VariantAnnotationUtils.SPLICE_DONOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT, junctionSolution);

            splicing = (splicing || junctionSolution[0]);

            if (variantEnd > exon.getStart()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variantStart <= exon.getEnd()) {  // Variant start within the exon
                    cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                    consequenceType.setCdnaPosition(cdnaVariantStart);
                    exonNumber = exon.getExonNumber();
//                    consequenceType.setExonNumber(exon.getExonNumber());
                    if (variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||SE||||||||----
                        cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                    } else {  // Only variant start within the exon  ---||||||||||||SE----
                        cdnaVariantEnd = cdnaVariantStart + 1;  // To account for those insertions in the 5' end of an intron
                    }
                }
            } else if (variantEnd == exon.getStart()) { // Only variant end within the exon  ---SE|||||||||||||----
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                cdnaVariantStart = cdnaVariantEnd - 1;  // To account for those insertions in the 3' end of an intron
                exonNumber = exon.getExonNumber();
//                consequenceType.setExonNumber(exon.getExonNumber());
            } else {  // Variant does not include this exon, variant is located before this exon
                variantAhead = false;
            }
            exonCounter++;
        }
        if (exonNumber != NO_EXON_OVERLAP) {
            consequenceType.setExonOverlap(Collections.singletonList(new ExonOverlap(
                    (new StringBuilder()).append(exonNumber).append("/").append(transcript.getExons().size()).toString(),
                    INVALID_OVERLAP_PERCENTAGE)));
        }
        solveMiRNA(cdnaVariantStart, cdnaVariantEnd, junctionSolution[1]);
    }

    protected void solveCodingPositiveTranscript() {

        Exon exon = transcript.getExons().get(0);
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;
        int exonNumber = NO_EXON_OVERLAP;

        if (transcript.getGenomicCodingStart() <= exon.getEnd()) {
            firstCdsPhase = exon.getPhase();
        }

        if (variantEnd > exon.getStart()) {
            if (variantStart <= exon.getEnd()) { // Variant start within the exon (this is a insertion, variantEnd=variantStart+1)
                cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                consequenceType.setCdnaPosition(cdnaVariantStart);
                exonNumber = exon.getExonNumber();
//                consequenceType.setExonNumber(exon.getExonNumber());
                if (variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||SE||||||||----
                    cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                } else {  // Only variant start within the exon  ---||||||||||||SE----
                    cdnaVariantEnd = cdnaVariantStart + 1;  // To account for those insertions in the last nucleotide of the exon
                }
            }
        } else if (variantEnd == exon.getStart()) {  // Only variant end within the exon  ----E|||||||||||||----
            // We do not contemplate that variant end can be located before this exon since this is the first exon
            cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
            cdnaVariantStart = cdnaVariantEnd - 1;  // To account for those insertions in the first nucleotide of the exon
            exonNumber = exon.getExonNumber();
//            consequenceType.setExonNumber(exon.getExonNumber());
        }

        int exonCounter = 1;
        // This is not a do-while since we cannot call solveJunction until
        while (exonCounter < transcript.getExons().size() && variantAhead) {
            int prevSpliceSite = exon.getEnd() + 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            transcriptSequence = transcriptSequence + exon.getSequence();
            // Set firsCdsPhase only when the first coding exon is reached
            if (firstCdsPhase == -1 && transcript.getGenomicCodingStart() <= exon.getEnd()) {
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(prevSpliceSite, exon.getStart() - 1, VariantAnnotationUtils.SPLICE_DONOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT, junctionSolution);

            splicing = (splicing || junctionSolution[0]);

            if (variantEnd > exon.getStart()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variantStart <= exon.getEnd()) {  // Variant start within the exon
                    cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                    consequenceType.setCdnaPosition(cdnaVariantStart);
                    exonNumber = exon.getExonNumber();
//                    consequenceType.setExonNumber(exon.getExonNumber());
                    if (variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||SE||||||||----
                        cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                    } else {  // Only variant start within the exon  ---||||||||||||SE----
                        cdnaVariantEnd = cdnaVariantStart + 1;  // To account for those insertions in the 5' end of an intron
                    }
                }
            } else if (variantEnd == exon.getStart()) { // Only variant end within the exon  ---SE|||||||||||||----
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                cdnaVariantStart = cdnaVariantEnd - 1;  // To account for those insertions in the 3' end of an intron
                exonNumber = exon.getExonNumber();
//                consequenceType.setExonNumber(exon.getExonNumber());
            } else {  // Variant does not include this exon, variant is located before this exon
                variantAhead = false;
            }
            exonCounter++;
        }
        if (exonNumber != NO_EXON_OVERLAP) {
            consequenceType.setExonOverlap(Collections.singletonList(new ExonOverlap(
                    (new StringBuilder()).append(exonNumber).append("/").append(transcript.getExons().size()).toString(),
                    INVALID_OVERLAP_PERCENTAGE)));
        }
        // Is not intron variant (both ends fall within the same intron)
        if (!junctionSolution[1]) {
            if (cdnaVariantStart == -1 && cdnaVariantEnd != -1) {  // To account for those insertions in the 3' end of an intron
                cdnaVariantStart = cdnaVariantEnd - 1;
            } else if (cdnaVariantEnd == -1 && cdnaVariantStart != -1) {  // To account for those insertions in the 5' end of an intron
                cdnaVariantEnd = cdnaVariantStart + 1;
            }
            solveExonVariantInPositiveTranscript(splicing, transcriptSequence, cdnaVariantStart, cdnaVariantEnd,
                    firstCdsPhase);
        }
    }

    private void solveExonVariantInPositiveTranscript(boolean splicing, String transcriptSequence,
                                                      int cdnaVariantStart, int cdnaVariantEnd, int firstCdsPhase) {
        if (variantStart < transcript.getGenomicCodingStart()) {
            if (transcript.getStart() < transcript.getGenomicCodingStart()
                    || transcript.unconfirmedStart()) { // Check transcript has 3 UTR
                SoNames.add(VariantAnnotationUtils.FIVE_PRIME_UTR_VARIANT);
            }
        } else if (variantStart <= transcript.getGenomicCodingEnd()) {  // Variant start within coding region
            // Need to define a local cdnaCodingStart because may modified in two lines below
            int cdnaCodingStart = transcript.getCdnaCodingStart();
            cdnaCodingStart = setCdsAndProteinPosition(cdnaVariantStart, firstCdsPhase, cdnaCodingStart);
            if (variantEnd <= transcript.getGenomicCodingEnd()) {  // Variant end also within coding region
                solveCodingExonVariantInPositiveTranscript(transcriptSequence, cdnaCodingStart, cdnaVariantStart,
                        cdnaVariantEnd);
            } else if (transcript.getEnd() > transcript.getGenomicCodingEnd()
                    || transcript.unconfirmedEnd()) { // Check transcript has 3 UTR)
                SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
            }
        } else if (transcript.getEnd() > transcript.getGenomicCodingEnd() || transcript.unconfirmedEnd()) { // Check transcript has 3 UTR)
            SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
        }
    }

    private void solveCodingExonVariantInPositiveTranscript(String transcriptSequence, int cdnaCodingStart,
                                                            int cdnaVariantStart, int cdnaVariantEnd) {
        // Insertion. Be careful: insertion coordinates are special, alternative nts are pasted between cdnaVariantStart and cdnaVariantEnd
        if (cdnaVariantStart != -1) {
            // cdnaVariantStart=null if variant is intronic. cdnaCodingStart<1 if cds_start_NF and phase!=0
            if (cdnaVariantStart < (cdnaCodingStart + 2) && !transcript.unconfirmedStart()) {
                solveStartCodonPositiveVariant(transcriptSequence, transcript.getCdnaCodingStart(), cdnaVariantEnd);
            }
//            int finalNtPhase = (transcriptSequence.length() - cdnaCodingStart) % 3;
            int finalNtPhase = (transcript.getCdnaCodingEnd() - cdnaCodingStart) % 3;
//            if ((cdnaVariantStart >= (transcriptSequence.length() - finalNtPhase)) &&
            //  Variant in the last codon of a transcript without stop codon. finalNtPhase==2 if the cds length is multiple of 3.
            if ((cdnaVariantStart >= (transcript.getCdnaCodingEnd() - finalNtPhase)) && finalNtPhase != 2) {
//                    (transcript.getEnd() == transcript.getGenomicCodingEnd()) && finalNtPhase != 2) {
                SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);
            }
            // <INS> variants may have uncomplete alternate sequence and therefore unknown length
            if (!variant.getLength().equals(Variant.UNKNOWN_LENGTH)) {
                if (variant.getAlternate().length() % 3 == 0) {
                    SoNames.add(VariantAnnotationUtils.INFRAME_INSERTION);
                } else {
                    SoNames.add(VariantAnnotationUtils.FRAMESHIFT_VARIANT);
                }
            } else {
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
            }
            solveStopCodonPositiveInsertion(transcriptSequence, cdnaCodingStart, cdnaVariantStart);
        } else {
            SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
        }
    }

    private void solveStartCodonPositiveVariant(String transcriptSequence, int cdnaCodingStart, int cdnaVariantEnd) {
        // Not necessary to include % 3 since if we get here we already know that the difference is < 3
        Integer variantPhaseShift = cdnaVariantEnd - cdnaCodingStart;
        int modifiedCodonStart = cdnaVariantEnd - variantPhaseShift;
        // Complementary of start codon ATG - Met (already reversed)
        String referenceCodon = transcriptSequence.substring(modifiedCodonStart - 1, modifiedCodonStart - 1 + 3);
        char[] modifiedCodonArray = referenceCodon.toCharArray();
        // Both MT and non-MT code use same codification for Metionine
        if (VariantAnnotationUtils.isStartCodon(MT.equals(variant.getChromosome()), referenceCodon)) {
            int i = cdnaVariantEnd - 1;  // Position (0 based index) in transcriptSequence of
            String alternate = variant.getAlternate();
            // the first nt after the deletion
            int codonPosition;
            int alternatePosition = 0;
            // If we get here, cdnaVariantStart and cdnaVariantEnd != -1; this is an assumption that was made just before
            // calling this method
            for (codonPosition = variantPhaseShift; codonPosition < 3; codonPosition++) {
                char substitutingNt;
                // Means we've reached the end of the alternate. This can only happen if it's an insertion of one nt and
                // it occurs between the first and second start codon base. THEREFORE, it can enter in this if one time
                // at most
                if (alternatePosition >= alternate.length()) {
                    // cdnaVariantStart is base 1 and the string is base 0, therefore this is actually getting base at
                    // position cdnaVariantEnd
                    substitutingNt = transcriptSequence.charAt(i);
                } else {
                    // Paste alternae nts after deletion in the corresponding codon position
                    substitutingNt = alternate.charAt(alternatePosition);
                    alternatePosition++;
                }
                modifiedCodonArray[codonPosition] = substitutingNt;
            }
            if (VariantAnnotationUtils.isSynonymousCodon(MT.equals(variant.getChromosome()), referenceCodon,
                    String.valueOf(modifiedCodonArray))) {
                SoNames.add(VariantAnnotationUtils.START_RETAINED_VARIANT);
            } else {
                SoNames.add(VariantAnnotationUtils.START_LOST);
            }
        }
    }

    private void solveStopCodonPositiveInsertion(String transcriptSequence, Integer cdnaCodingStart, Integer cdnaVariantStart) {
        // Sum 1 to cdnaVariantStart because of the peculiarities of insertion coordinates:
        // cdnaVariantStart coincides with the vcf position, the actual substituted nt is the one on the right
        Integer variantPhaseShift = (cdnaVariantStart + 1 - cdnaCodingStart) % 3;
        int modifiedCodonStart = cdnaVariantStart + 1 - variantPhaseShift;
        if (modifiedCodonStart > 0 && (modifiedCodonStart + 2) <= transcriptSequence.length()) {
            String alternate = getLeftAlternate();
            // -1 and +2 because of base 0 String indexing
            String referenceCodon = transcriptSequence.substring(modifiedCodonStart - 1, modifiedCodonStart + 2);
            char[] modifiedCodonArray = referenceCodon.toCharArray();
            int i = 0;
            // indexing over transcriptSequence is 0 based, transcriptSequencePosition points to cdnaVariantEnd actually
            int transcriptSequencePosition = cdnaVariantStart;
            int modifiedCodonPosition;
            int modifiedCodonPositionStart = variantPhaseShift;

            // Char arrays to contain the upper/lower-case formatted strings for the codon change, e.g. aGT/ATG
            char[] formattedReferenceCodonArray = referenceCodon.toLowerCase().toCharArray();
            char[] formattedModifiedCodonArray = referenceCodon.toLowerCase().toCharArray();
            boolean useMitochondrialCode = variant.getChromosome().equals("MT");
            boolean firstCodon = true;

            do {
                for (modifiedCodonPosition = modifiedCodonPositionStart;
                    // Paste alternative nt in the corresponding codon position
                     (modifiedCodonPosition < 3 && i < variant.getAlternate().length()); modifiedCodonPosition++) {
                    modifiedCodonArray[modifiedCodonPosition] = alternate.toCharArray()[i];

                    // Edit modified nt to make it upper-case in the formatted strings
                    formattedReferenceCodonArray[modifiedCodonPosition]
                            = Character.toUpperCase(formattedReferenceCodonArray[modifiedCodonPosition]);
                    formattedModifiedCodonArray[modifiedCodonPosition]
                            = Character.toUpperCase(alternate.toCharArray()[i]);

                    i++;
                }
                transcriptSequencePosition = updatePositiveInsertionCodonArrays(transcriptSequence, modifiedCodonArray,
                        transcriptSequencePosition, modifiedCodonPosition, formattedReferenceCodonArray,
                        formattedModifiedCodonArray);

                firstCodon = setInsertionAlleleAminoacidChange(referenceCodon, modifiedCodonArray,
                        formattedReferenceCodonArray, formattedModifiedCodonArray, useMitochondrialCode, firstCodon);

                decideStopCodonModificationAnnotation(SoNames, referenceCodon, String.valueOf(modifiedCodonArray),
                        useMitochondrialCode);
                modifiedCodonPositionStart = 0;  // Reset the position where the next modified codon must be started to be filled
            } while (i < variant.getAlternate().length());  // All posible new codons generated by the inserted sequence must be checked
        }
    }

    private String getLeftAlternate() {
        // alternate == <INS> <=> alternate seq is not complete and must be found within the variant.sv.rightSvInsSeq
        if (variant.getAlternate().startsWith(SYMBOLIC_START)) {
            if (variant.getSv() != null && variant.getSv().getLeftSvInsSeq() != null) {
                return variant.getSv().getLeftSvInsSeq();
            } else {
                throw new IllegalArgumentException("Insertion found with <INS> tag in the alternate and no alternate"
                        + " sequence available within variant.sv.leftSvInsSeq; Malformed variant object. Please, check.");
            }
        } else {
            return variant.getAlternate();
        }
    }

    private void solveJunction(Integer spliceSite1, Integer spliceSite2, String leftSpliceSiteTag,
                               String rightSpliceSiteTag, boolean[] junctionSolution) {
        junctionSolution[0] = false;  // Is splicing variant in non-coding region
        junctionSolution[1] = false;  // Variant is intronic and both ends fall within the intron

        // Variant overlaps the rest of intronic region (splice region within the intron and/or rest of intron)
        if (regionsOverlap(spliceSite1 + 2, spliceSite2 - 2, variantStart, variantEnd)) {
            SoNames.add(VariantAnnotationUtils.INTRON_VARIANT);
        }
        if (variantStart >= spliceSite1 && variantEnd <= spliceSite2) {
            junctionSolution[1] = true;  // variant start & end fall within the intron
        }

        if (regionsOverlap(spliceSite1, spliceSite1 + 1, variantStart, variantEnd)) {  // Variant donor/acceptor
            if (variantEnd == spliceSite1) {  // Insertion between last nt of the exon (3' end), first nt of the intron (5' end)
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);  // Inserted nts considered part of the coding sequence
            } else if (variantEnd == (spliceSite1 + 2)) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);  // Inserted nts considered out of the donor/acceptor region
                // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
                junctionSolution[0] = (spliceSite2 > variantStart);
            } else {
                SoNames.add(leftSpliceSiteTag);  // donor/acceptor depending on transcript strand
                // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
                junctionSolution[0] = (spliceSite2 > variantStart);
            }
        } else if (regionsOverlap(spliceSite1 + 2, spliceSite1 + 7, variantStart, variantEnd)) {
            // Insertion coordinates are passed to this function as (variantStart-1,variantStart)
            if (!(variantStart == (spliceSite1 + 7))) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
            }
            // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            junctionSolution[0] = (variantStart <= spliceSite2 || variantEnd <= spliceSite2);
            // Insertion coordinates are passed to this function as (variantStart-1,variantStart)
        } else if (regionsOverlap(spliceSite1 - 3, spliceSite1 - 1, variantStart, variantEnd) && !(variantEnd == (spliceSite1 - 3))) {
            SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
        }

        if (regionsOverlap(spliceSite2 - 1, spliceSite2, variantStart, variantEnd)) {  // Variant donor/acceptor
            if (variantStart == spliceSite2) {  // Insertion between last nt of the intron (3' end), first nt of the exon (5' end)
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);  // Inserted nts considered part of the coding sequence
            } else if (variantStart == (spliceSite2 - 2)) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);  // Inserted nts considered out of the donor/acceptor region
                // BE CAREFUL: there are introns shorter than 14nts, and even just 1nt long!! (22:36587846)
                junctionSolution[0] = (spliceSite1 < variantEnd);
            } else {
                SoNames.add(rightSpliceSiteTag);  // donor/acceptor depending on transcript strand
                // BE CAREFUL: there are introns shorter than 14nts, and even just 1nt long!! (22:36587846)
                junctionSolution[0] = (spliceSite1 < variantEnd);
            }
        } else if (regionsOverlap(spliceSite2 - 7, spliceSite2 - 2, variantStart, variantEnd)) {
            // Insertion coordinates are passed to this function as (variantStart-1,variantStart) {
            if (!(variantEnd == (spliceSite2 - 7))) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
            }
            // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            junctionSolution[0] = (spliceSite1 <= variantStart || spliceSite1 <= variantEnd);
            // Insertion coordinates are passed to this function as (variantStart-1,variantStart) {
        } else if (regionsOverlap(spliceSite2 + 1, spliceSite2 + 3, variantStart, variantEnd) && !((variantStart == (spliceSite2 + 3)))) {
            SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
        }

    }

}
