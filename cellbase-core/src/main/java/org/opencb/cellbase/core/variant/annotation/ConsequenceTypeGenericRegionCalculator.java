package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.ExonOverlap;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fjlopez on 29/03/17.
 */
public class ConsequenceTypeGenericRegionCalculator extends ConsequenceTypeCalculator {
    protected int variantStart;
    protected int variantEnd;
    protected static final int BIG_VARIANT_SIZE_THRESHOLD = 50;

    public List<ConsequenceType> run(Variant inputVariant, List<Gene> geneList, boolean[] overlapsRegulatoryRegion,
                                     QueryOptions queryOptions) {
        parseQueryParam(queryOptions);
        List<ConsequenceType> consequenceTypeList = new ArrayList<>();
        variant = inputVariant;
        variantEnd = getEnd(svExtraPadding);
        variantStart = getStart(svExtraPadding);
//        isBigDeletion = ((variantEnd - variantStart) > BIG_VARIANT_SIZE_THRESHOLD);
        boolean isIntergenic = true;
        for (Gene currentGene : geneList) {
            gene = currentGene;
            for (Transcript currentTranscript : gene.getTranscripts()) {
                isIntergenic = isIntergenic && (variantEnd < currentTranscript.getStart() || variantStart > currentTranscript.getEnd());
                transcript = currentTranscript;
                consequenceType = new ConsequenceType();
                consequenceType.setGeneName(gene.getName());
                consequenceType.setEnsemblGeneId(gene.getId());
                consequenceType.setEnsemblTranscriptId(transcript.getId());
                consequenceType.setStrand(transcript.getStrand());
                consequenceType.setBiotype(transcript.getBiotype());
                consequenceType.setTranscriptAnnotationFlags(transcript.getAnnotationFlags() != null
                        ? new ArrayList<>(transcript.getAnnotationFlags()) : null);
                SoNames.clear();

                if (transcript.getStrand().equals("+")) {
                    // whole transcript affected
                    if (variantStart <= transcript.getStart() && variantEnd >= transcript.getEnd()) {
                        SoNames.add(VariantAnnotationUtils.STRUCTURAL_VARIANT);
                        consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                        consequenceTypeList.add(consequenceType);
                    } else if (regionsOverlap(transcript.getStart(), transcript.getEnd(), variantStart, variantEnd)) {
                        solvePositiveTranscript(consequenceTypeList);
                    } else {
                        solveTranscriptFlankingRegions(VariantAnnotationUtils.UPSTREAM_GENE_VARIANT,
                                VariantAnnotationUtils.DOWNSTREAM_GENE_VARIANT);
                        if (SoNames.size() > 0) { // Variant does not overlap gene region, just may have upstream/downstream annotations
                            consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                            consequenceTypeList.add(consequenceType);
                        }
                    }
                } else {
                    if (variantStart <= transcript.getStart() && variantEnd >= transcript.getEnd()) { // whole trans. affected
                        SoNames.add(VariantAnnotationUtils.STRUCTURAL_VARIANT);
//                        consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                        consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                        consequenceTypeList.add(consequenceType);
                    } else if (regionsOverlap(transcript.getStart(), transcript.getEnd(), variantStart, variantEnd)) {
//                        if (isBigDeletion) {  // Big deletion
//                            SoNames.add(VariantAnnotationUtils.FEATURE_TRUNCATION);
//                        }
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

    protected void solveTranscriptFlankingRegions(String leftRegionTag, String rightRegionTag) {
        // Variant overlaps with -5kb region
        if (regionsOverlap(transcript.getStart() - 5000, transcript.getStart() - 1, variantStart, variantEnd)) {
            // Variant overlaps with -2kb region
            if (regionsOverlap(transcript.getStart() - 2000, transcript.getStart() - 1, variantStart, variantEnd)) {
                SoNames.add("2KB_" + leftRegionTag.replace(DOWN_UP_STREAM_GENE_TAG, ""));
            } else {
                SoNames.add(leftRegionTag);
            }
        }
        // Variant overlaps with +5kb region
        if (regionsOverlap(transcript.getEnd() + 1, transcript.getEnd() + 5000, variantStart, variantEnd)) {
            // Variant overlaps with +2kb region
            if (regionsOverlap(transcript.getEnd() + 1, transcript.getEnd() + 2000, variantStart, variantEnd)) {
                SoNames.add("2KB_" + rightRegionTag.replace(DOWN_UP_STREAM_GENE_TAG, ""));
            } else {
                SoNames.add(rightRegionTag);
            }
        }
    }

    protected void solveNonCodingNegativeTranscript() {
        Exon exon = transcript.getExons().get(0);
        int exonSize = exon.getEnd() - exon.getStart() + 1;
        String exonStringSuffix = "/" + transcript.getExons().size();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;
//        Integer variantStartExonNumber = null;
//        Integer variantEndExonNumber = null;
        List<ExonOverlap> exonOverlap = new ArrayList<>(transcript.getExons().size());


        if (firstCdsPhase == -1 && transcript.getGenomicCodingEnd() >= exon.getStart()) {
            firstCdsPhase = exon.getPhase();
        }
        if (variantEnd <= exon.getEnd()) {
            if (variantEnd >= exon.getStart()) {  // Variant end within the exon
                cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                consequenceType.setCdnaPosition(cdnaVariantStart);
//                variantStartExonNumber = exon.getExonNumber();
                if (variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                    exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                            (variantEnd - variantStart + 1) * 100f / exonSize));
                } else {
                    exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                            (variantEnd - exon.getStart() + 1) * 100f / exonSize));
                }
            }
        } else if (variantStart >= exon.getStart()) {
            // We do not contemplate that variant end can be located before this exon since this is the first exon
            cdnaVariantEnd = cdnaExonEnd - (variantEnd - exon.getStart());
//            variantEndExonNumber = exon.getExonNumber();
            exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                    (exon.getEnd() - variantStart + 1) * 100f / exonSize));
        // Variant includes the whole exon. Variant end is located before the exon, variant start is located after the exon
        } else {
            exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix, 100f));
        }

        int exonCounter = 1;
        // This is not a do-while since we cannot call solveJunction until
        while (exonCounter < transcript.getExons().size() && variantAhead) {
            int prevSpliceSite = exon.getStart() - 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            exonSize = exon.getEnd() - exon.getStart() + 1;
            // Set firsCdsPhase only when the first coding exon is reached
            if (firstCdsPhase == -1 && transcript.getGenomicCodingEnd() >= exon.getStart()) {
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(exon.getEnd() + 1, prevSpliceSite, VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_DONOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if (variantEnd <= exon.getEnd()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variantEnd >= exon.getStart()) {  // Variant end within the exon
                    cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                    consequenceType.setCdnaPosition(cdnaVariantStart);
//                    variantStartExonNumber = exon.getExonNumber();
                    if (variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                        cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                        exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                                (variantEnd - variantStart + 1) * 100f / exonSize));
                    } else {
                        exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                                (variantEnd - exon.getStart() + 1) * 100f / exonSize));
                    }
                }
            } else if (variantStart >= exon.getStart()) {
                if (variantStart <= exon.getEnd()) {  // Only variant start within the exon  ----||||||||||E||||----
                    cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                    exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                            (exon.getEnd() - variantStart + 1) * 100f / exonSize));
//                    variantEndExonNumber = exon.getExonNumber();
                } else {  // Variant does not include this exon, variant is located before this exon
                    variantAhead = false;
                }
            } else {  // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix, 100f));
            }
            exonCounter++;
        }
//        consequenceType.setExonNumber(variantStartExonNumber != null ? variantStartExonNumber : variantEndExonNumber);
        if (exonOverlap.size() > 0) {
            consequenceType.setExonOverlap(exonOverlap);
        }
        solveMiRNA(cdnaVariantStart, cdnaVariantEnd, junctionSolution[1]);
    }

    protected void solveCodingNegativeTranscript() {
        Exon exon = transcript.getExons().get(0);
        int exonSize = exon.getEnd() - exon.getStart() + 1;
        String exonStringSuffix = "/" + transcript.getExons().size();
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;
//        Integer variantStartExonNumber = null;
//        Integer variantEndExonNumber = null;
        List<ExonOverlap> exonOverlap = new ArrayList<>(transcript.getExons().size());

        if (firstCdsPhase == -1 && transcript.getGenomicCodingEnd() >= exon.getStart()) {
            firstCdsPhase = exon.getPhase();
        }
        if (variantEnd <= exon.getEnd()) {
            if (variantEnd >= exon.getStart()) {  // Variant end within the exon
                cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                consequenceType.setCdnaPosition(cdnaVariantStart);
//                variantStartExonNumber = exon.getExonNumber();
                if (variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                    exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                            (variantEnd - variantStart + 1) * 100f / exonSize));
                } else {
                    exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                            (variantEnd - exon.getStart() + 1) * 100f / exonSize));
                }

            }
        } else if (variantStart >= exon.getStart()) {
            // We do not contemplate that variant end can be located before this exon since this is the first exon
            cdnaVariantEnd = cdnaExonEnd - (variantEnd - exon.getStart());
//            variantEndExonNumber = exon.getExonNumber();
            exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                    (exon.getEnd() - variantStart + 1) * 100f / exonSize));
            // Variant includes the whole exon. Variant end is located before the exon, variant start is located after the exon
        } else {
            exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix, 100f));
        }

        int exonCounter = 1;
        // This is not a do-while since we cannot call solveJunction  until
        while (exonCounter < transcript.getExons().size() && variantAhead) {
            int prevSpliceSite = exon.getStart() - 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            exonSize = exon.getEnd() - exon.getStart() + 1;
            transcriptSequence = exon.getSequence() + transcriptSequence;
            // Set firsCdsPhase only when the first coding exon is reached
            if (firstCdsPhase == -1 && transcript.getGenomicCodingEnd() >= exon.getStart()) {
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(exon.getEnd() + 1, prevSpliceSite, VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_DONOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if (variantEnd <= exon.getEnd()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variantEnd >= exon.getStart()) {  // Variant end within the exon
                    cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                    consequenceType.setCdnaPosition(cdnaVariantStart);
//                    variantStartExonNumber = exon.getExonNumber();
                    if (variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                        cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                        exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                                (variantEnd - variantStart + 1) * 100f / exonSize));
                    } else {
                        exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                                (variantEnd - exon.getStart() + 1) * 100f / exonSize));
                    }
                }
            } else if (variantStart >= exon.getStart()) {
                if (variantStart <= exon.getEnd()) {  // Only variant start within the exon  ----||||||||||E||||----
                    cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                    exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                            (exon.getEnd() - variantStart + 1) * 100f / exonSize));
//                    variantEndExonNumber = exon.getExonNumber();
                } else {  // Variant does not include this exon, variant is located before this exon
                    variantAhead = false;
                }
            } else {  // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix, 100f));
            }
            exonCounter++;
        }
        if (exonOverlap.size() > 0) {
            consequenceType.setExonOverlap(exonOverlap);
        }
//        consequenceType.setExonNumber(variantStartExonNumber != null ? variantStartExonNumber : variantEndExonNumber);
        // Is not intron variant (both ends fall within the same intron)
        if (!junctionSolution[1]) {
            solveExonVariantInNegativeTranscript(splicing, transcriptSequence, cdnaVariantStart, cdnaVariantEnd,
                    firstCdsPhase);
        }

    }

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
                    SoNames.add(VariantAnnotationUtils.INITIATOR_CODON_VARIANT);
                }
                if (variantStart < (transcript.getGenomicCodingStart() + 3)) {
                    SoNames.add(VariantAnnotationUtils.TERMINATOR_CODON_VARIANT);
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
            cdnaCodingStart = setCdsAndProteinPosition(cdnaVariantStart, firstCdsPhase, cdnaCodingStart);
            if (variantStart >= transcript.getGenomicCodingStart()) {  // Variant start also within coding region
                solveCodingExonVariantInNegativeTranscript(splicing, transcriptSequence, cdnaCodingStart, cdnaVariantStart,
                        cdnaVariantEnd);
            } else {
                // Check transcript has 3 UTR)
                if (transcript.getStart() < transcript.getGenomicCodingStart() || transcript.unconfirmedEnd()) {
                    SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
                }
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
                SoNames.add(VariantAnnotationUtils.TERMINATOR_CODON_VARIANT);
            }
            // Check transcript has 3 UTR)
        } else if (transcript.getStart() < transcript.getGenomicCodingStart() || transcript.unconfirmedEnd()) {
            SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
        }
    }

    protected void solveCodingExonVariantInNegativeTranscript(boolean splicing, String transcriptSequence, int cdnaCodingStart,
                                                            int cdnaVariantStart, int cdnaVariantEnd) {
        Boolean codingAnnotationAdded = false;

        // cdnaVariantStart=null if variant is intronic. cdnaCodingStart<1 if cds_start_NF and phase!=0
        if (cdnaVariantStart != -1 && cdnaVariantStart < (cdnaCodingStart + 3) && (cdnaCodingStart > 0
                || !transcript.unconfirmedStart())) {
            SoNames.add(VariantAnnotationUtils.INITIATOR_CODON_VARIANT);
            codingAnnotationAdded = true;
        }
        if (cdnaVariantEnd != -1) {
            int finalNtPhase = (transcript.getCdnaCodingEnd() - cdnaCodingStart) % 3;
            Boolean stopToSolve = true;
            // just checks cdnaVariantStart!=null because no splicing means cdnaVariantEnd is also != null
            if (!splicing && cdnaVariantStart != -1) {
                codingAnnotationAdded = true;
//                if (variant.getReferenceStart().length() % 3 == 0) {
//                    SoNames.add(VariantAnnotationUtils.INFRAME_DELETION);
//                } else {
//                    SoNames.add(VariantAnnotationUtils.FRAMESHIFT_VARIANT);
//                }
                stopToSolve = false;  // Stop codon annotation will be solved in the line below.
                solveStopCodonNegativeVariant(transcriptSequence, cdnaCodingStart, cdnaVariantStart, cdnaVariantEnd);
            }
            if (cdnaVariantEnd >= (transcript.getCdnaCodingEnd() - finalNtPhase)) {
                if (finalNtPhase != 2) {
//                if (transcript.unconfirmedEnd() && (finalNtPhase != 2)) {
                    SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);
                } else if (stopToSolve) {  // Only if stop codon annotation was not already solved in the if block above
                    SoNames.add(VariantAnnotationUtils.TERMINATOR_CODON_VARIANT);
                }
            }
        }
        if (!codingAnnotationAdded) {
            SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
        }
    }

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

            boolean useMitochondrialCode = variant.getChromosome().equals("MT");
            if (VariantAnnotationUtils.isStopCodon(useMitochondrialCode, referenceCodon1)
                    || VariantAnnotationUtils.isStopCodon(useMitochondrialCode, referenceCodon2)) {
                SoNames.add(VariantAnnotationUtils.TERMINATOR_CODON_VARIANT);
            } else {
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
            }
        }
    }

    protected void solveJunction(Integer spliceSite1, Integer spliceSite2, String leftSpliceSiteTag,
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
            if ((variantEnd - variantStart) <= BIG_VARIANT_SIZE_THRESHOLD) {  // Big cnvs should not be annotated with such a detail
                SoNames.add(leftSpliceSiteTag);  // donor/acceptor depending on transcript strand
                // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
                junctionSolution[0] = (variantStart <= spliceSite2 || variantEnd <= spliceSite2);
            } else {
                // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
                junctionSolution[0] = (variantStart <= spliceSite2 || variantEnd <= spliceSite2);
            }
        } else if (regionsOverlap(spliceSite1 + 2, spliceSite1 + 7, variantStart, variantEnd)) {
            // Insertion coordinates are passed to this function as (variantStart-1,variantStart)
            if ((variantEnd - variantStart) <= BIG_VARIANT_SIZE_THRESHOLD) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
            }
            // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            junctionSolution[0] = (variantStart <= spliceSite2 || variantEnd <= spliceSite2);
        } else if (regionsOverlap(spliceSite1 - 3, spliceSite1 - 1, variantStart, variantEnd)
                // Insertion coordinates are passed to this function as (variantStart-1,variantStart)
                && ((variantEnd - variantStart) <= BIG_VARIANT_SIZE_THRESHOLD)) {
            SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
        }

        if (regionsOverlap(spliceSite2 - 1, spliceSite2, variantStart, variantEnd)) {  // Variant donor/acceptor
            if ((variantEnd - variantStart) <= BIG_VARIANT_SIZE_THRESHOLD) {  // Big cnvs should not be annotated with such a detail
                SoNames.add(rightSpliceSiteTag);  // donor/acceptor depending on transcript strand
                // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
                junctionSolution[0] = (spliceSite1 <= variantStart || spliceSite1 <= variantEnd);
            } else {
                // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
                junctionSolution[0] = (spliceSite1 <= variantStart || spliceSite1 <= variantEnd);
            }
        } else if (regionsOverlap(spliceSite2 - 7, spliceSite2 - 2, variantStart, variantEnd)) {
            // Insertion coordinates are passed to this function as (variantStart-1,variantStart) {
            if ((variantEnd - variantStart) <= BIG_VARIANT_SIZE_THRESHOLD) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
            }
            // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            junctionSolution[0] = (spliceSite1 <= variantStart || spliceSite1 <= variantEnd);
        } else if (regionsOverlap(spliceSite2 + 1, spliceSite2 + 3, variantStart, variantEnd)
                // Insertion coordinates are passed to this function as (variantStart-1,variantStart) {
                && ((variantEnd - variantStart) <= BIG_VARIANT_SIZE_THRESHOLD)) {
            SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
        }
    }

    protected void solveCodingPositiveTranscript() {
        Exon exon = transcript.getExons().get(0);
        int exonSize = exon.getEnd() - exon.getStart() + 1;
        String exonStringSuffix = "/" + transcript.getExons().size();
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;
//        Integer variantStartExonNumber = null;
//        Integer variantEndExonNumber = null;
        List<ExonOverlap> exonOverlap = new ArrayList<>(transcript.getExons().size());

        if (transcript.getGenomicCodingStart() <= exon.getEnd()) {
            firstCdsPhase = exon.getPhase();
        }
        if (variantStart >= exon.getStart()) {
            if (variantStart <= exon.getEnd()) {  // Variant start within the exon
                cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                consequenceType.setCdnaPosition(cdnaVariantStart);
//                variantStartExonNumber = exon.getExonNumber();
                if (variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                    exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                            (variantEnd - variantStart + 1) * 100f / exonSize));
                } else {
                    exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                            (exon.getEnd() - variantStart + 1) * 100f / exonSize));
                }
            }
        } else if (variantEnd <= exon.getEnd()) {
            // We do not contemplate that variant end can be located before this exon since this is the first exon
            cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
            exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                    (variantEnd - exon.getStart() + 1) * 100f / exonSize));
//            variantEndExonNumber = exon.getExonNumber();
        // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
        } else {
            exonOverlap.add(new ExonOverlap(String.valueOf(exon.getExonNumber()), 100f));
        }


        int exonCounter = 1;
        // This is not a do-while since we cannot call solveJunction  until
        while (exonCounter < transcript.getExons().size() && variantAhead) {
            int prevSpliceSite = exon.getEnd() + 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            exonSize = exon.getEnd() - exon.getStart() + 1;
            transcriptSequence = transcriptSequence + exon.getSequence();
            // Set firsCdsPhase only when the first coding exon is reached
            if (firstCdsPhase == -1 && transcript.getGenomicCodingStart() <= exon.getEnd()) {
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(prevSpliceSite, exon.getStart() - 1, VariantAnnotationUtils.SPLICE_DONOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if (variantStart >= exon.getStart()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variantStart <= exon.getEnd()) {  // Variant start within the exon
                    cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                    consequenceType.setCdnaPosition(cdnaVariantStart);
//                    variantStartExonNumber = exon.getExonNumber();
                    if (variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                        cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                        exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                                (variantEnd - variantStart + 1) * 100f / exonSize));
                    } else {
                        exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                                (exon.getEnd() - variantStart + 1) * 100f / exonSize));
                    }
                }
            } else if (variantEnd <= exon.getEnd()) {
                if (variantEnd >= exon.getStart()) {  // Only variant end within the exon  ----||||||||||E||||----
                    cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                    cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                    exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                            (variantEnd - exon.getStart() + 1) * 100f / exonSize));
//                    variantEndExonNumber = exon.getExonNumber();
                } else {  // Variant does not include this exon, variant is located before this exon
                    variantAhead = false;
                }
            } else {  // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix, 100f));
            }
            exonCounter++;
        }
        if (exonOverlap.size() > 0) {
            consequenceType.setExonOverlap(exonOverlap);
        }
//        consequenceType.setExonNumber(variantStartExonNumber != null ? variantStartExonNumber : variantEndExonNumber);
        // Is not intron variant (both ends fall within the same intron)
        if (!junctionSolution[1]) {
            solveExonVariantInPositiveTranscript(splicing, transcriptSequence, cdnaVariantStart, cdnaVariantEnd,
                    firstCdsPhase);
        }

    }

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
                    SoNames.add(VariantAnnotationUtils.INITIATOR_CODON_VARIANT);
                }
                if (variantEnd > (transcript.getGenomicCodingEnd() - 3)) {
                    SoNames.add(VariantAnnotationUtils.TERMINATOR_CODON_VARIANT);
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
            cdnaCodingStart = setCdsAndProteinPosition(cdnaVariantStart, firstCdsPhase, cdnaCodingStart);
            if (variantEnd <= transcript.getGenomicCodingEnd()) {  // Variant end also within coding region
                solveCodingExonVariantInPositiveTranscript(splicing, transcriptSequence, cdnaCodingStart,
                        cdnaVariantStart, cdnaVariantEnd);
            } else {
                // Check transcript has 3 UTR)
                if (transcript.getEnd() > transcript.getGenomicCodingEnd() || transcript.unconfirmedEnd()) {
                    SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
                }
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
                SoNames.add(VariantAnnotationUtils.TERMINATOR_CODON_VARIANT);
            }
            // Check transcript has 3 UTR)
        } else if (transcript.getEnd() > transcript.getGenomicCodingEnd() || transcript.unconfirmedEnd()) {
            SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
        }
    }

    protected void solveCodingExonVariantInPositiveTranscript(boolean splicing, String transcriptSequence, int cdnaCodingStart,
                                                            int cdnaVariantStart, int cdnaVariantEnd) {
        // This will indicate wether it is needed to add the "coding_sequence_variant" annotation or not
        boolean codingAnnotationAdded = false;

        // cdnaVariantStart=null if variant is intronic. cdnaCodingStart<1 if cds_start_NF and phase!=0
        if (cdnaVariantStart != -1 && cdnaVariantStart < (cdnaCodingStart + 3) && (cdnaCodingStart > 0 || !transcript.unconfirmedStart())) {
            SoNames.add(VariantAnnotationUtils.INITIATOR_CODON_VARIANT);
            codingAnnotationAdded = true;
        }
        if (cdnaVariantEnd != -1) {
            int finalNtPhase = (transcript.getCdnaCodingEnd() - cdnaCodingStart) % 3;
            Boolean stopToSolve = true;
            // just checks cdnaVariantStart!=null because no splicing means cdnaVariantEnd is also != null
            if (!splicing && cdnaVariantStart != -1) {
                codingAnnotationAdded = true;
//                if (variant.getReferenceStart().length() % 3 == 0) {
//                    SoNames.add(VariantAnnotationUtils.INFRAME_DELETION);
//                } else {
//                    SoNames.add(VariantAnnotationUtils.FRAMESHIFT_VARIANT);
//                }
                stopToSolve = false;  // Stop codon annotation will be solved in the line below.
                solveStopCodonPositiveVariant(transcriptSequence, cdnaCodingStart, cdnaVariantStart, cdnaVariantEnd);
            }
            if (cdnaVariantEnd >= (transcript.getCdnaCodingEnd() - finalNtPhase)) {
                if (finalNtPhase != 2) {
//                if (transcript.unconfirmedEnd() && (finalNtPhase != 2)) {
                    SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);
                } else if (stopToSolve) {  // Only if stop codon annotation was not already solved in the if block above
                    SoNames.add(VariantAnnotationUtils.TERMINATOR_CODON_VARIANT);
                }
            }
        }
        if (!codingAnnotationAdded) {
            SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
        }
    }

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
            boolean useMitochondrialCode = variant.getChromosome().equals("MT");
            if (VariantAnnotationUtils.isStopCodon(useMitochondrialCode, referenceCodon1)
                    || VariantAnnotationUtils.isStopCodon(useMitochondrialCode, referenceCodon2)) {
                SoNames.add(VariantAnnotationUtils.TERMINATOR_CODON_VARIANT);
            } else {
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
            }
        }
    }

    protected void solveNonCodingPositiveTranscript() {
        Exon exon = transcript.getExons().get(0);
        int exonSize = exon.getEnd() - exon.getStart() + 1;
        String exonStringSuffix = "/" + transcript.getExons().size();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;
//        Integer variantStartExonNumber = null;
//        Integer variantEndExonNumber = null;
        List<ExonOverlap> exonOverlap = new ArrayList<>(transcript.getExons().size());

        if (transcript.getGenomicCodingStart() <= exon.getEnd()) {
            firstCdsPhase = exon.getPhase();
        }
        if (variantStart >= exon.getStart()) {
            if (variantStart <= exon.getEnd()) {  // Variant start within the exon
                cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                consequenceType.setCdnaPosition(cdnaVariantStart);
//                variantStartExonNumber = exon.getExonNumber();
                if (variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                    exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                            (variantEnd - variantStart + 1) * 100f / exonSize));
                } else {
                    exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                            (exon.getEnd() - variantStart + 1) * 100f / exonSize));
                }

            }
        } else if (variantEnd <= exon.getEnd()) {
            // We do not contemplate that variant end can be located before this exon since this is the first exon
            cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
            exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                    (variantEnd - exon.getStart() + 1) * 100f / exonSize));
//            variantEndExonNumber = exon.getExonNumber();
            // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
        } else {
            exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix, 100f));
        }

        int exonCounter = 1;
        // This is not a do-while since we cannot call solveJunction until
        while (exonCounter < transcript.getExons().size() && variantAhead) {
            int prevSpliceSite = exon.getEnd() + 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            exonSize = exon.getEnd() - exon.getStart() + 1;
            // Set firsCdsPhase only when the first coding exon is reached
            if (firstCdsPhase == -1 && transcript.getGenomicCodingStart() <= exon.getEnd()) {
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(prevSpliceSite, exon.getStart() - 1, VariantAnnotationUtils.SPLICE_DONOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if (variantStart >= exon.getStart()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variantStart <= exon.getEnd()) {  // Variant start within the exon
                    cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                    consequenceType.setCdnaPosition(cdnaVariantStart);
//                    variantStartExonNumber = exon.getExonNumber();
                    if (variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                        cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                        exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                                (variantEnd - variantStart + 1) * 100f / exonSize));
                    } else {
                        exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                                (exon.getEnd() - variantStart + 1) * 100f / exonSize));
                    }
                }
            } else if (variantEnd <= exon.getEnd()) {
                if (variantEnd >= exon.getStart()) {  // Only variant end within the exon  ----||||||||||E||||----
                    cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                    cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                    exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix,
                            (variantEnd - exon.getStart() + 1) * 100f / exonSize));
//                    variantEndExonNumber = exon.getExonNumber();
                } else {  // Variant does not include this exon, variant is located before this exon
                    variantAhead = false;
                }
            } else {  // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                exonOverlap.add(new ExonOverlap(exon.getExonNumber() + exonStringSuffix, 100f));
            }
            exonCounter++;
        }
        if (exonOverlap.size() > 0) {
            consequenceType.setExonOverlap(exonOverlap);
        }
//        consequenceType.setExonNumber(variantStartExonNumber != null ? variantStartExonNumber : variantEndExonNumber);
        solveMiRNA(cdnaVariantStart, cdnaVariantEnd, junctionSolution[1]);
    }



}
