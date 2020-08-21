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

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.ProteinVariantAnnotation;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.api.core.GenomeDBAdaptor;
import org.opencb.cellbase.core.api.core.VariantDBAdaptor;
import org.opencb.cellbase.lib.managers.GenomeManager;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by fjlopez on 05/08/15.
 */
public class ConsequenceTypeDeletionCalculator extends ConsequenceTypeGenericRegionCalculator {
    private boolean isBigDeletion;
//    private GenomeDBAdaptor genomeDBAdaptor;

    public ConsequenceTypeDeletionCalculator(GenomeManager genomeManager) {
        super();
        this.genomeManager = genomeManager;
    }

    @Override
    public List<ConsequenceType> run(Variant inputVariant, List<Gene> geneList, boolean[] overlapsRegulatoryRegion,
                                     QueryOptions queryOptions) {
        parseQueryParam(queryOptions);
        List<ConsequenceType> consequenceTypeList = new ArrayList<>();
        variant = inputVariant;
        int extraPadding = VariantType.CNV.equals(variant.getType()) ? cnvExtraPadding : svExtraPadding;
        variantEnd = getEnd(extraPadding);
        variantStart = getStart(extraPadding);
        isBigDeletion = ((variantEnd - variantStart) > BIG_VARIANT_SIZE_THRESHOLD);
        boolean isIntergenic = true;
        for (Gene currentGene : geneList) {
            gene = currentGene;
            String source = getSource(gene.getId());
            for (Transcript currentTranscript : gene.getTranscripts()) {
                isIntergenic = isIntergenic && (variantEnd < currentTranscript.getStart() || variantStart > currentTranscript.getEnd());
                transcript = currentTranscript;
                consequenceType = new ConsequenceType();
                consequenceType.setGeneName(gene.getName());
                consequenceType.setGeneId(gene.getId());
                consequenceType.setTranscriptId(transcript.getId());
                if (VariantDBAdaptor.QueryParams.ENSEMBL.key().equalsIgnoreCase(source)) {
                    consequenceType.setEnsemblGeneId(gene.getId());
                    consequenceType.setEnsemblTranscriptId(transcript.getId());
                }
                consequenceType.setStrand(transcript.getStrand());
                consequenceType.setBiotype(transcript.getBiotype());
                consequenceType.setSource(source);
                consequenceType.setTranscriptAnnotationFlags(transcript.getAnnotationFlags() != null
                        ? new ArrayList<>(transcript.getAnnotationFlags()) : null);
                SoNames.clear();

                if (transcript.getStrand().equals("+")) {
                    // Deletion - whole transcript removed
                    if (variantStart <= transcript.getStart() && variantEnd >= transcript.getEnd()) {
                        SoNames.add(VariantAnnotationUtils.TRANSCRIPT_ABLATION);
//                        consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                        consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                        consequenceTypeList.add(consequenceType);
                    } else if (regionsOverlap(transcript.getStart(), transcript.getEnd(), variantStart, variantEnd)) {
                        if (isBigDeletion) {  // Big deletion
                            SoNames.add(VariantAnnotationUtils.FEATURE_TRUNCATION);
                        }
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
                    if (variantStart <= transcript.getStart() && variantEnd >= transcript.getEnd()) { // Deletion - whole trans. removed
                        SoNames.add(VariantAnnotationUtils.TRANSCRIPT_ABLATION);
//                        consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                        consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                        consequenceTypeList.add(consequenceType);
                    } else if (regionsOverlap(transcript.getStart(), transcript.getEnd(), variantStart, variantEnd)) {
                        if (isBigDeletion) {  // Big deletion
                            SoNames.add(VariantAnnotationUtils.FEATURE_TRUNCATION);
                        }
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

    @Override
    protected void solveExonVariantInNegativeTranscript(boolean splicing, String transcriptSequence,
                                                      int cdnaVariantStart, int cdnaVariantEnd, int firstCdsPhase) {
        if (variantEnd > transcript.getGenomicCodingEnd()) {
            if (transcript.getEnd() > transcript.getGenomicCodingEnd() || transcript.unconfirmedStart()) { // Check transcript has 3 UTR
                SoNames.add(VariantAnnotationUtils.FIVE_PRIME_UTR_VARIANT);
            }
            if (variantStart <= transcript.getGenomicCodingEnd()) {
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
                // cdnaCodingStart < 1 if cds_start_NF and phase!=0
                if (transcript.getCdnaCodingStart() > 0 || !transcript.unconfirmedStart()) {
                    // We just consider doing a fancier prediction with the start codon if both variant start and end
                    // fall within the transcript sequence
                    // ---NNNNNNNNCATTTTTTT
                    //    deletion  |---|
                    if (cdnaVariantStart != -1 && cdnaVariantEnd != -1
                        && (cdnaVariantEnd - transcript.getCdnaCodingStart()) < 3
                        && (cdnaVariantEnd - transcript.getCdnaCodingStart()) >= 0) {
                        solveStartCodonNegativeVariant(transcriptSequence, transcript.getCdnaCodingStart(),
                                cdnaVariantStart, cdnaVariantEnd);
                    } else {
                        SoNames.add(VariantAnnotationUtils.START_LOST);
                    }
                }
                if (variantStart < (transcript.getGenomicCodingStart() + 3)) {
                    SoNames.add(VariantAnnotationUtils.STOP_LOST);
                    if (variantStart < transcript.getGenomicCodingStart()) {
                        if (transcript.getStart() < transcript.getGenomicCodingStart()
                                || transcript.unconfirmedEnd()) { // Check transcript has 3 UTR)
                            SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
                        }
                    }
                }
            }
        } else if (variantEnd >= transcript.getGenomicCodingStart()) {
            // Need to define a local cdnaCodingStart because may modified in two lines below
            int cdnaCodingStart = transcript.getCdnaCodingStart();
            if (cdnaVariantStart != -1) {  // cdnaVariantStart may be null if variantEnd falls in an intron
                if (transcript.unconfirmedStart()) {
                    cdnaCodingStart -= ((3 - firstCdsPhase) % 3);
                }
                int cdsVariantStart = cdnaVariantStart - cdnaCodingStart + 1;
                consequenceType.setCdsPosition(cdsVariantStart);
                // First place where protein variant annotation is added to the Consequence type,
                // must create the ProteinVariantAnnotation object
                ProteinVariantAnnotation proteinVariantAnnotation = new ProteinVariantAnnotation();
                proteinVariantAnnotation.setPosition(((cdsVariantStart - 1) / 3) + 1);
                consequenceType.setProteinVariantAnnotation(proteinVariantAnnotation);
            }
            if (variantStart >= transcript.getGenomicCodingStart()) {  // Variant start also within coding region
                solveCodingExonVariantInNegativeTranscript(splicing, transcriptSequence, cdnaCodingStart, cdnaVariantStart,
                        cdnaVariantEnd);
            } else {
                // Check transcript has 3 UTR)
                if (transcript.getStart() < transcript.getGenomicCodingStart() || transcript.unconfirmedEnd()) {
                    SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
                }
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
                SoNames.add(VariantAnnotationUtils.STOP_LOST);
            }
            // Check transcript has 3 UTR)
        } else if (transcript.getStart() < transcript.getGenomicCodingStart() || transcript.unconfirmedEnd()) {
            SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
        }
    }

    private void solveStartCodonNegativeVariant(String transcriptSequence, int cdnaCodingStart, int cdnaVariantStart,
                                                int cdnaVariantEnd) {
        // Not necessary to include % 3 since if we get here we already know that the difference is < 3
        Integer variantPhaseShift = cdnaVariantEnd - cdnaCodingStart;
        // Get reference codon
        int modifiedCodonStart = cdnaVariantEnd - variantPhaseShift;
        char[] modifiedCodonArray = getReverseComplementaryCodon(transcriptSequence, modifiedCodonStart);
        String referenceCodon = String.valueOf(modifiedCodonArray);
        // Continue checking only if reference codon is an actual start codon (Met)
        if (VariantAnnotationUtils.isStartCodon(MT.equals(variant.getChromosome()), String.valueOf(modifiedCodonArray))) {
            int i = transcriptSequence.length() - cdnaVariantStart + 1;  // Position (0 based index) in transcriptSequence of
            // the first nt after the deletion
            int codonPosition;
            // If we get here, cdnaVariantStart and cdnaVariantEnd != -1; this is an assumption that was made just before
            // calling this method
            for (codonPosition = variantPhaseShift; codonPosition >= 0; codonPosition--) {
                char substitutingNt;
                // Means we've reached the transcript.start
                if (i >= transcriptSequence.length()) {
                    int genomicCoordinate = transcript.getEnd() + (transcriptSequence.length() - i + 1); // + 1 since i moves
                    // in base 0 (see above)
                    Query query = new Query(GenomeDBAdaptor.QueryParams.REGION.key(), variant.getChromosome()
                            + ":" + genomicCoordinate
                            + "-" + (genomicCoordinate + 1));
                    substitutingNt = VariantAnnotationUtils.COMPLEMENTARY_NT
                            .get(genomeManager.getGenomicSequence(query, new QueryOptions()).getResults().get(0)
                                    .getSequence().charAt(0));
                } else {
                    // Paste reference nts after deletion in the corresponding codon position
                    substitutingNt = VariantAnnotationUtils.COMPLEMENTARY_NT.get(transcriptSequence.charAt(i));
                }
                modifiedCodonArray[codonPosition] = substitutingNt;
                i++;
            }
            // End of codon has been reached, all positions found equal to the referenceCodonArray
            if (VariantAnnotationUtils.isSynonymousCodon(MT.equals(variant.getChromosome()), referenceCodon,
                    String.valueOf(modifiedCodonArray))) {
                SoNames.add(VariantAnnotationUtils.START_RETAINED_VARIANT);
            } else {
                SoNames.add(VariantAnnotationUtils.START_LOST);
            }
        }
    }

    @Override
    protected void solveExonVariantInPositiveTranscript(boolean splicing, String transcriptSequence,
                                                      int cdnaVariantStart, int cdnaVariantEnd, int firstCdsPhase) {
        if (variantStart < transcript.getGenomicCodingStart()) {
            // Check transcript has 3 UTR
            if (transcript.getStart() < transcript.getGenomicCodingStart() || transcript.unconfirmedStart()) {
                SoNames.add(VariantAnnotationUtils.FIVE_PRIME_UTR_VARIANT);
            }
            if (variantEnd >= transcript.getGenomicCodingStart()) {
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
                // cdnaCodingStart<1 if cds_start_NF and phase!=0
                if (transcript.getCdnaCodingStart() > 0 || !transcript.unconfirmedStart()) {
                    // We just consider doing a fancier prediction with the start codon if both variant start and end
                    // fall within the transcript sequence
                    // ---NNNNNNNNCATTTTTTT
                    //    deletion  |---|
                    if (cdnaVariantStart != -1 && cdnaVariantEnd != -1
                            && (cdnaVariantEnd - transcript.getCdnaCodingStart()) < 3
                            && (cdnaVariantEnd - transcript.getCdnaCodingStart()) >= 0) {
                        solveStartCodonPositiveVariant(transcriptSequence, transcript.getCdnaCodingStart(),
                                cdnaVariantStart, cdnaVariantEnd);
                    } else {
                        SoNames.add(VariantAnnotationUtils.START_LOST);
                    }
                }
                if (variantEnd > (transcript.getGenomicCodingEnd() - 3)) {
                    SoNames.add(VariantAnnotationUtils.STOP_LOST);
                    if (variantEnd > transcript.getGenomicCodingEnd()) {
                        // Check transcript has 3 UTR)
                        if (transcript.getEnd() > transcript.getGenomicCodingEnd() || transcript.unconfirmedStart()) {
                            SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
                        }
                    }
                }
            }
        } else if (variantStart <= transcript.getGenomicCodingEnd()) {  // Variant start within coding region
            // Need to define a local cdnaCodingStart because may modified in two lines below
            int cdnaCodingStart = transcript.getCdnaCodingStart();
            if (cdnaVariantStart != -1) {  // cdnaVariantStart may be null if variantStart falls in an intron
                if (transcript.unconfirmedStart()) {
                    cdnaCodingStart -= ((3 - firstCdsPhase) % 3);
                }
                int cdsVariantStart = cdnaVariantStart - cdnaCodingStart + 1;
                consequenceType.setCdsPosition(cdsVariantStart);
                // First place where protein variant annotation is added to the Consequence type,
                // must create the ProteinVariantAnnotation object
                ProteinVariantAnnotation proteinVariantAnnotation = new ProteinVariantAnnotation();
                proteinVariantAnnotation.setPosition(((cdsVariantStart - 1) / 3) + 1);
                consequenceType.setProteinVariantAnnotation(proteinVariantAnnotation);
            }
            if (variantEnd <= transcript.getGenomicCodingEnd()) {  // Variant end also within coding region
                solveCodingExonVariantInPositiveTranscript(splicing, transcriptSequence, cdnaCodingStart,
                        cdnaVariantStart, cdnaVariantEnd);
            } else {
                // Check transcript has 3 UTR)
                if (transcript.getEnd() > transcript.getGenomicCodingEnd() || transcript.unconfirmedEnd()) {
                    SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
                }
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
                SoNames.add(VariantAnnotationUtils.STOP_LOST);
            }
            // Check transcript has 3 UTR)
        } else if (transcript.getEnd() > transcript.getGenomicCodingEnd() || transcript.unconfirmedEnd()) {
            SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
        }
    }

    private void solveStartCodonPositiveVariant(String transcriptSequence, int cdnaCodingStart, int cdnaVariantStart,
                                                  int cdnaVariantEnd) {
        // Not necessary to include % 3 since if we get here we already know that the difference is < 3
        Integer variantPhaseShift = cdnaVariantEnd - cdnaCodingStart;
        int modifiedCodonStart = cdnaVariantEnd - variantPhaseShift;
        // Complementary of start codon ATG - Met (already reversed)
        String referenceCodon = transcriptSequence.substring(modifiedCodonStart - 1, modifiedCodonStart - 1 + 3);
        char[] modifiedCodonArray = referenceCodon.toCharArray();
        // Continue checking only if reference codon is actually a start codon (Met)
        if (VariantAnnotationUtils.isStartCodon(MT.equals(variant.getChromosome()), referenceCodon)) {
            int i = cdnaVariantStart - 1 - 1;  // - 1 to get the first position right before the deletion. An additional
            // - 1 to set base 0
            int codonPosition;
            // If we get here, cdnaVariantStart and cdnaVariantEnd != -1; this is an assumption that was made just before
            // calling this method
            for (codonPosition = variantPhaseShift; codonPosition >= 0; codonPosition--) {
                char substitutingNt;
                // Means we've reached the beginning of the transcript, i.e. transcript.start
                if (i < 0) {
                    int genomicCoordinate = transcript.getStart() + i; // recall that i is negative if we get here
                    Query query = new Query(GenomeDBAdaptor.QueryParams.REGION.key(), variant.getChromosome()
                            + ":" + genomicCoordinate
                            + "-" + (genomicCoordinate + 1));
                    substitutingNt = genomeManager
                            .getGenomicSequence(query, new QueryOptions()).getResults().get(0).getSequence().charAt(0);
                } else {
                    // Paste reference nts after deletion in the corresponding codon position
                    substitutingNt = transcriptSequence.charAt(i);
                }
                modifiedCodonArray[codonPosition] = substitutingNt;
                i--;
            }
            if (VariantAnnotationUtils.isSynonymousCodon(MT.equals(variant.getChromosome()), referenceCodon,
                    String.valueOf(modifiedCodonArray))) {
                SoNames.add(VariantAnnotationUtils.START_RETAINED_VARIANT);
            } else {
                SoNames.add(VariantAnnotationUtils.START_LOST);
            }
        }
    }

    @Override
    protected void solveCodingExonVariantInNegativeTranscript(boolean splicing, String transcriptSequence, int cdnaCodingStart,
                                                            int cdnaVariantStart, int cdnaVariantEnd) {
        Boolean codingAnnotationAdded = false;

        // cdnaVariantStart=null if variant is intronic. cdnaCodingStart<1 if cds_start_NF and phase!=0
        if (cdnaVariantStart != -1 && cdnaVariantStart < (cdnaCodingStart + 3) && (cdnaCodingStart > 0
                || !transcript.unconfirmedStart())) {
            SoNames.add(VariantAnnotationUtils.START_LOST);
            codingAnnotationAdded = true;
        }
        if (cdnaVariantEnd != -1) {
            int finalNtPhase = (transcript.getCdnaCodingEnd() - cdnaCodingStart) % 3;
            Boolean stopToSolve = true;
            // just checks cdnaVariantStart!=null because no splicing means cdnaVariantEnd is also != null
            if (!splicing && cdnaVariantStart != -1) {
                codingAnnotationAdded = true;
                if (variant.getLength() % 3 == 0) {
//                if (variant.getReferenceStart().length() % 3 == 0) {
                    SoNames.add(VariantAnnotationUtils.INFRAME_DELETION);
                } else {
                    SoNames.add(VariantAnnotationUtils.FRAMESHIFT_VARIANT);
                }
                stopToSolve = false;  // Stop codon annotation will be solved in the line below.
                solveStopCodonNegativeVariant(transcriptSequence, cdnaCodingStart, cdnaVariantStart, cdnaVariantEnd);
            }
            if (cdnaVariantEnd >= (transcript.getCdnaCodingEnd() - finalNtPhase)) {
                if (finalNtPhase != 2) {
//                if (transcript.unconfirmedEnd() && (finalNtPhase != 2)) {
                    SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);
                } else if (stopToSolve) {  // Only if stop codon annotation was not already solved in the if block above
                    SoNames.add(VariantAnnotationUtils.STOP_LOST);
                }
            }
        }
        if (!codingAnnotationAdded) {
            SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
        }
    }

    @Override
    protected void solveStopCodonNegativeVariant(String transcriptSequence, int cdnaCodingStart,
                                               int cdnaVariantStart, int cdnaVariantEnd) {
        Integer variantPhaseShift1 = (cdnaVariantStart - cdnaCodingStart) % 3;
        Integer variantPhaseShift2 = (cdnaVariantEnd - cdnaCodingStart) % 3;
        int modifiedCodon1Start = cdnaVariantStart - variantPhaseShift1;
        int modifiedCodon2Start = cdnaVariantEnd - variantPhaseShift2;
        if (modifiedCodon1Start > 0 && (modifiedCodon2Start + 2) <= transcriptSequence.length()) {
            String reverseCodon1 = new StringBuilder(transcriptSequence.substring(transcriptSequence.length() - modifiedCodon1Start - 2,
                    // Rigth limit of the substring sums +1 because substring does not include that position
                    transcriptSequence.length() - modifiedCodon1Start + 1)).reverse().toString();
            String reverseCodon2 = new StringBuilder(transcriptSequence.substring(transcriptSequence.length() - modifiedCodon2Start - 2,
                    // Rigth limit of the substring sums +1 because substring does not include that position
                    transcriptSequence.length() - modifiedCodon2Start + 1)).reverse().toString();
            String reverseTranscriptSequence = new StringBuilder(
                    transcriptSequence.substring(((transcriptSequence.length() - cdnaVariantEnd) > 2)
                                    ? (transcriptSequence.length() - cdnaVariantEnd - 3)
                                    : 0,  // Be careful reaching the end of the transcript sequence
                            // Rigth limit of the substring -2 because substring does not include that position
                            transcriptSequence.length() - cdnaVariantEnd)).reverse().toString();
            char[] referenceCodon1Array = reverseCodon1.toCharArray();
            referenceCodon1Array[0] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(referenceCodon1Array[0]);
            referenceCodon1Array[1] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(referenceCodon1Array[1]);
            referenceCodon1Array[2] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(referenceCodon1Array[2]);
            String referenceCodon1 = String.valueOf(referenceCodon1Array);
            char[] referenceCodon2Array = reverseCodon2.toCharArray();
            referenceCodon2Array[0] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(referenceCodon2Array[0]);
            referenceCodon2Array[1] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(referenceCodon2Array[1]);
            referenceCodon2Array[2] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(referenceCodon2Array[2]);
            String referenceCodon2 = String.valueOf(referenceCodon2Array);
            char[] modifiedCodonArray = referenceCodon1Array.clone();

            int i = 0;
            int codonPosition;

            // Char array to contain the upper/lower-case formatted string for the codon change, e.g. aGT/ATG
            char[] formattedReferenceCodon1Array = String.valueOf(referenceCodon1Array).toLowerCase().toCharArray();

            // BE CAREFUL: this method is assumed to be called after checking that cdnaVariantStart and cdnaVariantEnd
            // are within coding sequence (both of them within an exon).
            for (codonPosition = variantPhaseShift1; codonPosition < 3; codonPosition++) {
                if (i >= reverseTranscriptSequence.length()) {
                    int genomicCoordinate = transcript.getStart() - (i - reverseTranscriptSequence.length() + 1);
//                    modifiedCodonArray[codonPosition] = VariantAnnotationUtils.COMPLEMENTARY_NT.
//                            get(((GenomeSequenceFeature) genomeDBAdaptor.getSequenceByRegion(variant.getChromosome(),
//                                    genomicCoordinate, genomicCoordinate + 1,
//                                    new QueryOptions()).getResult().get(0)).getSequence().charAt(0));
                    Query query = new Query(GenomeDBAdaptor.QueryParams.REGION.key(), variant.getChromosome()
                            + ":" + genomicCoordinate
                            + "-" + (genomicCoordinate + 1));
                    modifiedCodonArray[codonPosition] = VariantAnnotationUtils.COMPLEMENTARY_NT.
                            get(genomeManager.getGenomicSequence(query, new QueryOptions()).getResults().get(0).getSequence().charAt(0));
                } else {
                    // Paste reference nts after deletion in the corresponding codon position
                    modifiedCodonArray[codonPosition] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(reverseTranscriptSequence.charAt(i));
                }

                // Edit modified nt to make it upper-case in the formatted strings
                formattedReferenceCodon1Array[codonPosition] = Character.toUpperCase(formattedReferenceCodon1Array[codonPosition]);

                i++;
            }

            // Only the exact codon where the deletion starts is set
            consequenceType.setCodon(String.valueOf(formattedReferenceCodon1Array) + "/"
                    + String.valueOf(modifiedCodonArray).toUpperCase());
            String modifiedCodon = String.valueOf(modifiedCodonArray);
            boolean useMitochondrialCode = variant.getChromosome().equals(MT);
            // Assumes proteinVariantAnnotation attribute is already initialized
            consequenceType
                    .getProteinVariantAnnotation()
                    .setReference(VariantAnnotationUtils.getAminoacid(useMitochondrialCode, referenceCodon1));
            consequenceType
                    .getProteinVariantAnnotation()
                    .setAlternate(VariantAnnotationUtils.getAminoacid(useMitochondrialCode, modifiedCodon));

            decideStopCodonModificationAnnotation(SoNames,
                    VariantAnnotationUtils.isStopCodon(useMitochondrialCode, referenceCodon2)
                    ? referenceCodon2 : referenceCodon1, modifiedCodon, useMitochondrialCode);
        }
    }



    @Override
    protected void solveCodingExonVariantInPositiveTranscript(boolean splicing, String transcriptSequence, int cdnaCodingStart,
                                                            int cdnaVariantStart, int cdnaVariantEnd) {
        // This will indicate wether it is needed to add the "coding_sequence_variant" annotation or not
        boolean codingAnnotationAdded = false;

        // cdnaVariantStart=null if variant is intronic. cdnaCodingStart<1 if cds_start_NF and phase!=0
        if (cdnaVariantStart != -1 && cdnaVariantStart < (cdnaCodingStart + 3) && (cdnaCodingStart > 0 || !transcript.unconfirmedStart())) {
            SoNames.add(VariantAnnotationUtils.START_LOST);
            codingAnnotationAdded = true;
        }
        if (cdnaVariantEnd != -1) {
            int finalNtPhase = (transcript.getCdnaCodingEnd() - cdnaCodingStart) % 3;
            Boolean stopToSolve = true;
            // just checks cdnaVariantStart!=null because no splicing means cdnaVariantEnd is also != null
            if (!splicing && cdnaVariantStart != -1) {
                codingAnnotationAdded = true;
                if (variant.getLength() % 3 == 0) {
//                if (variant.getReferenceStart().length() % 3 == 0) {
                    SoNames.add(VariantAnnotationUtils.INFRAME_DELETION);
                } else {
                    SoNames.add(VariantAnnotationUtils.FRAMESHIFT_VARIANT);
                }
                stopToSolve = false;  // Stop codon annotation will be solved in the line below.
                solveStopCodonPositiveVariant(transcriptSequence, cdnaCodingStart, cdnaVariantStart, cdnaVariantEnd);
            }
            if (cdnaVariantEnd >= (transcript.getCdnaCodingEnd() - finalNtPhase)) {
                if (finalNtPhase != 2) {
//                if (transcript.unconfirmedEnd() && (finalNtPhase != 2)) {
                    SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);
                } else if (stopToSolve) {  // Only if stop codon annotation was not already solved in the if block above
                    SoNames.add(VariantAnnotationUtils.STOP_LOST);
                }
            }
        }
        if (!codingAnnotationAdded) {
            SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
        }
    }

    @Override
    protected void solveStopCodonPositiveVariant(String transcriptSequence, int cdnaCodingStart, int cdnaVariantStart,
                                               int cdnaVariantEnd) {
        Integer variantPhaseShift1 = (cdnaVariantStart - cdnaCodingStart) % 3;
        Integer variantPhaseShift2 = (cdnaVariantEnd - cdnaCodingStart) % 3;
        int modifiedCodon1Start = cdnaVariantStart - variantPhaseShift1;
        int modifiedCodon2Start = cdnaVariantEnd - variantPhaseShift2;
        if (modifiedCodon1Start > 0 && (modifiedCodon2Start + 2) <= transcriptSequence.length()) {
            // -1 and +2 because of base 0 String indexing
            String referenceCodon1 = transcriptSequence.substring(modifiedCodon1Start - 1, modifiedCodon1Start + 2);
            String referenceCodon2 = transcriptSequence.substring(modifiedCodon2Start - 1, modifiedCodon2Start + 2);
            char[] modifiedCodonArray = referenceCodon1.toCharArray();
            int i = cdnaVariantEnd;  // Position (0 based index) in transcriptSequence of the first nt after the deletion
            int codonPosition;

            // Char array to contain the upper/lower-case formatted strings for the codon change, e.g. aGT/ATG
            char[] formattedReferenceCodon1Array = referenceCodon1.toLowerCase().toCharArray();

            // BE CAREFUL: this method is assumed to be called after checking that cdnaVariantStart and cdnaVariantEnd
            // are within coding sequence (both of them within an exon).
            for (codonPosition = variantPhaseShift1; codonPosition < 3; codonPosition++) {
                if (i >= transcriptSequence.length()) {
                    int genomicCoordinate = transcript.getEnd() + (i - transcriptSequence.length()) + 1;
//                    modifiedCodonArray[codonPosition] = ((GenomeSequenceFeature) genomeDBAdaptor
//                            .getSequenceByRegion(variant.getChromosome(), genomicCoordinate, genomicCoordinate + 1,
//                                    new QueryOptions()).getResult().get(0)).getSequence().charAt(0);
                    Query query = new Query(GenomeDBAdaptor.QueryParams.REGION.key(), variant.getChromosome()
                            + ":" + genomicCoordinate
                            + "-" + (genomicCoordinate + 1));
                    modifiedCodonArray[codonPosition] = genomeManager
                            .getGenomicSequence(query, new QueryOptions()).getResults().get(0).getSequence().charAt(0);
                } else {
                    // Paste reference nts after deletion in the corresponding codon position
                    modifiedCodonArray[codonPosition] = transcriptSequence.charAt(i);
                }

                // Edit modified nt to make it upper-case in the formatted strings
                formattedReferenceCodon1Array[codonPosition] = Character.toUpperCase(formattedReferenceCodon1Array[codonPosition]);

                i++;
            }

            // Only the exact codon where the deletion starts is set
            consequenceType.setCodon(String.valueOf(formattedReferenceCodon1Array) + "/"
                    + String.valueOf(modifiedCodonArray).toUpperCase());
            String modifiedCodon = String.valueOf(modifiedCodonArray);
            boolean useMitochondrialCode = variant.getChromosome().equals(MT);
            // Assumes proteinVariantAnnotation attribute is already initialized
            consequenceType
                    .getProteinVariantAnnotation()
                    .setReference(VariantAnnotationUtils.getAminoacid(useMitochondrialCode, referenceCodon1));
            consequenceType
                    .getProteinVariantAnnotation()
                    .setAlternate(VariantAnnotationUtils.getAminoacid(useMitochondrialCode, modifiedCodon));

            decideStopCodonModificationAnnotation(SoNames,
                    VariantAnnotationUtils.isStopCodon(useMitochondrialCode, referenceCodon2)
                    ? referenceCodon2 : referenceCodon1, modifiedCodon, useMitochondrialCode);
        }
    }

}


