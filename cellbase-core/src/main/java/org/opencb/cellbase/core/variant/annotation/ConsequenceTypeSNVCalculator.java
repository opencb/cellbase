package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.MiRNAGene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.ExonOverlap;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fjlopez on 22/06/15.
 */
public class ConsequenceTypeSNVCalculator extends ConsequenceTypeCalculator {

    public ConsequenceTypeSNVCalculator() {
    }

    public List<ConsequenceType> run(Variant inputVariant, List<Gene> geneList, boolean[] overlapsRegulatoryRegion,
                                     QueryOptions queryOptions) {

        List<ConsequenceType> consequenceTypeList = new ArrayList<>();
        variant = inputVariant;
        boolean isIntergenic = true;
        for (Gene currentGene : geneList) {
            gene = currentGene;
            for (Transcript currentTranscript : gene.getTranscripts()) {
                isIntergenic = isIntergenic && (variant.getStart() < currentTranscript.getStart()
                        || variant.getStart() > currentTranscript.getEnd());
                transcript = currentTranscript;
                consequenceType = new ConsequenceType();
                consequenceType.setGeneName(gene.getName());
                consequenceType.setEnsemblGeneId(gene.getId());
                consequenceType.setEnsemblTranscriptId(transcript.getId());
                consequenceType.setStrand(transcript.getStrand());
                consequenceType.setBiotype(transcript.getBiotype());
                consequenceType.setTranscriptAnnotationFlags(transcript.getAnnotationFlags() != null
                        ? new ArrayList<>(transcript.getAnnotationFlags())
                        : null);
                SoNames.clear();

                if (transcript.getStrand().equals("+")) {
                    // Check variant overlaps transcript start/end coordinates
                    if (variant.getStart() >= transcript.getStart() && variant.getStart() <= transcript.getEnd()) {
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
                    // Check overlaps transcript start/end coordinates
                    if (variant.getStart() >= transcript.getStart() && variant.getStart() <= transcript.getEnd()) {
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
        int exonSize = exon.getEnd() - exon.getStart() + 1;
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);  // cdnaExonEnd poinst to the same base than exonStart
        int cdnaVariantPosition = -1;
        boolean[] junctionSolution = {false, false};
        ExonOverlap exonOverlap = null;

        if (variant.getStart() <= exon.getEnd() && variant.getStart() >= exon.getStart()) {  // Variant within the exon
            cdnaVariantPosition = cdnaExonEnd - (variant.getStart() - exon.getStart());
            consequenceType.setCdnaPosition(cdnaVariantPosition);
            exonOverlap = new ExonOverlap(
                    (new StringBuilder()).append(exon.getExonNumber()).append("/").append(transcript.getExons().size())
                            .toString(),
                    100f / exonSize);
//            consequenceType.setExonNumber(exon.getExonNumber());
        }

        int exonCounter = 1;
        // This is not a do-while since we cannot call solveJunction until
        while (exonCounter < transcript.getExons().size() && variantAhead) {
            int prevSpliceSite = exon.getStart() - 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            exonSize = exon.getEnd() - exon.getStart() + 1;
            transcriptSequence = exon.getSequence() + transcriptSequence;
            solveJunction(exon.getEnd() + 1, prevSpliceSite, VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_DONOR_VARIANT, junctionSolution);

            if (variant.getStart() <= exon.getEnd()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variant.getStart() >= exon.getStart()) {  // Variant end within the exon
                    cdnaVariantPosition = cdnaExonEnd - (variant.getStart() - exon.getStart());
                    consequenceType.setCdnaPosition(cdnaVariantPosition);
                    exonOverlap = new ExonOverlap(
                            (new StringBuilder()).append(exon.getExonNumber()).append("/").append(transcript.getExons().size())
                                    .toString(),
                            100f / exonSize);
//                    consequenceType.setExonNumber(exon.getExonNumber());
                }
            } else {
                variantAhead = false;
            }
            exonCounter++;
        }
        if (exonOverlap != null) {
            consequenceType.setExonOverlap(Collections.singletonList(exonOverlap));
        }
        solveMiRNA(cdnaVariantPosition, junctionSolution[1]);
    }

    protected void solveCodingNegativeTranscript() {

        Exon exon = transcript.getExons().get(0);
        int exonSize = exon.getEnd() - exon.getStart() + 1;
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);  // cdnaExonEnd poinst to the same base than exonStart
        int cdnaVariantPosition = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;
        ExonOverlap exonOverlap = null;

        if (transcript.getGenomicCodingEnd() >= exon.getStart()) {
            firstCdsPhase = exon.getPhase();
        }
        if (variant.getStart() <= exon.getEnd() && variant.getStart() >= exon.getStart()) {  // Variant within the exon
            cdnaVariantPosition = cdnaExonEnd - (variant.getStart() - exon.getStart());
            consequenceType.setCdnaPosition(cdnaVariantPosition);
            exonOverlap = new ExonOverlap(
                    (new StringBuilder()).append(exon.getExonNumber()).append("/").append(transcript.getExons().size())
                            .toString(),
                    100f / exonSize);
//            consequenceType.setExonNumber(exon.getExonNumber());
        }

        int exonCounter = 1;
        // This is not a do-while since we cannot call solveJunction until
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

            if (variant.getStart() <= exon.getEnd()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variant.getStart() >= exon.getStart()) {  // Variant end within the exon
                    cdnaVariantPosition = cdnaExonEnd - (variant.getStart() - exon.getStart());
                    consequenceType.setCdnaPosition(cdnaVariantPosition);
                    exonOverlap = new ExonOverlap(
                            (new StringBuilder()).append(exon.getExonNumber()).append("/").append(transcript.getExons()
                                    .size()).toString(),
                            100f / exonSize);
//                    consequenceType.setExonNumber(exon.getExonNumber());
                }
            } else {
                variantAhead = false;
            }
            exonCounter++;
        }
        if (exonOverlap != null) {
            consequenceType.setExonOverlap(Collections.singletonList(exonOverlap));
        }
        // Is not intron variant (both ends fall within the same intron)
        if (!junctionSolution[1]) {
            solveExonVariantInNegativeTranscript(splicing, transcriptSequence, cdnaVariantPosition, firstCdsPhase);
        }
    }

    private void solveExonVariantInNegativeTranscript(boolean splicing, String transcriptSequence,
                                                      int cdnaVariantPosition, int firstCdsPhase) {
        if (variant.getStart() > transcript.getGenomicCodingEnd()
                && (transcript.getEnd() > transcript.getGenomicCodingEnd()
                || transcript.unconfirmedStart())) { // Check transcript has 3 UTR
            SoNames.add(VariantAnnotationUtils.FIVE_PRIME_UTR_VARIANT);
        } else if (variant.getStart() >= transcript.getGenomicCodingStart()) {
            // Need to define a local cdnaCodingStart because may modified in two lines below
            int cdnaCodingStart = transcript.getCdnaCodingStart();
            cdnaCodingStart = setCdsAndProteinPosition(cdnaVariantPosition, firstCdsPhase, cdnaCodingStart);
            solveCodingExonVariantInNegativeTranscript(splicing, transcriptSequence, cdnaCodingStart, cdnaVariantPosition);
        } else {
            if (transcript.getStart() < transcript.getGenomicCodingStart() || transcript.unconfirmedEnd()) { // Check transcript has 3 UTR)
                SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
            }
        }
    }

    private void solveCodingExonVariantInNegativeTranscript(Boolean splicing, String transcriptSequence,
                                                            int cdnaCodingStart, int cdnaVariantPosition) {

        Boolean codingAnnotationAdded = false;
        if (cdnaVariantPosition != -1) {
//            int finalNtPhase = (transcriptSequence.length() - cdnaCodingStart) % 3;
            int finalNtPhase = (transcript.getCdnaCodingEnd() - cdnaCodingStart) % 3;
            if (!splicing) {
//                if ((cdnaVariantPosition >= (transcriptSequence.length() - finalNtPhase)) &&
                //  Variant in the last codon of a transcript without stop codon. finalNtPhase==2 if the cds length is multiple of 3.
                if ((cdnaVariantPosition >= (transcript.getCdnaCodingEnd() - finalNtPhase)) && finalNtPhase != 2) {
//                        (transcript.getStart()==transcript.getGenomicCodingStart()) && finalNtPhase != 2) {
                    // If that is the case and variant ocurs in the last complete/incomplete codon, no coding prediction is needed
                    SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);
                } else if (cdnaVariantPosition > (cdnaCodingStart + 2)
                        || cdnaCodingStart > 0) { // cdnaCodingStart<1 if cds_start_NF and phase!=0
                    Integer variantPhaseShift = (cdnaVariantPosition - cdnaCodingStart) % 3;
                    int modifiedCodonStart = cdnaVariantPosition - variantPhaseShift;
                    String reverseCodon =
                            new StringBuilder(transcriptSequence.substring(transcriptSequence.length() - modifiedCodonStart - 2,
                                    // Rigth limit of the substring sums +1 because substring does not include that position
                                    transcriptSequence.length() - modifiedCodonStart + 1)).reverse().toString();
                    char[] referenceCodon = reverseCodon.toCharArray();
                    referenceCodon[0] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(referenceCodon[0]);
                    referenceCodon[1] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(referenceCodon[1]);
                    referenceCodon[2] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(referenceCodon[2]);
                    char[] modifiedCodonArray = referenceCodon.clone();
                    modifiedCodonArray[variantPhaseShift] =
                            VariantAnnotationUtils.COMPLEMENTARY_NT.get(variant.getAlternate().toCharArray()[0]);
                    codingAnnotationAdded = true;
                    String referenceA = VariantAnnotationUtils.getAminoacid(variant.getChromosome().equals("MT"),
                            String.valueOf(referenceCodon));
                    String alternativeA = VariantAnnotationUtils.getAminoacid(variant.getChromosome().equals("MT"),
                            String.valueOf(modifiedCodonArray));

                    if (VariantAnnotationUtils.isSynonymousCodon(variant.getChromosome().equals("MT"),
                            String.valueOf(referenceCodon), String.valueOf(modifiedCodonArray))) {
                        if (VariantAnnotationUtils.isStopCodon(variant.getChromosome().equals("MT"),
                                String.valueOf(referenceCodon))) {
                            SoNames.add(VariantAnnotationUtils.STOP_RETAINED_VARIANT);
                        } else {  // coding end may be not correctly annotated (incomplete_terminal_codon_variant),
                            // but if the length of the cds%3=0, annotation should be synonymous variant
                            SoNames.add(VariantAnnotationUtils.SYNONYMOUS_VARIANT);
                        }
                    } else {
                        // The !transcript.unconfirmedStart condition may seem weird but it's correct. If coding start
                        // is clearly defined, can be considered start_lost variant
                        if (cdnaVariantPosition < (cdnaCodingStart + 3)
                                && !transcript.unconfirmedStart()) {
//                                && (!transcript.unconfirmedStart()
//                                    || VariantAnnotationUtils.isStartCodon(MT.equals(variant.getChromosome()),
//                                                                            String.valueOf(referenceCodon)))) {
                            // Gary - initiator codon SO terms not compatible with the terms below
                            SoNames.add(VariantAnnotationUtils.START_LOST);
                            if (VariantAnnotationUtils.isStopCodon(variant.getChromosome().equals("MT"),
                                    String.valueOf(modifiedCodonArray))) {
                                // Gary - initiator codon SO terms not compatible with the terms below
                                SoNames.add(VariantAnnotationUtils.STOP_GAINED);
                            }
                        } else if (VariantAnnotationUtils.isStopCodon(variant.getChromosome().equals("MT"),
                                String.valueOf(referenceCodon))) {
                            SoNames.add(VariantAnnotationUtils.STOP_LOST);
                        } else {
                            SoNames.add(VariantAnnotationUtils
                                    .isStopCodon(variant.getChromosome().equals("MT"), String.valueOf(modifiedCodonArray))
                                    ? VariantAnnotationUtils.STOP_GAINED : VariantAnnotationUtils.MISSENSE_VARIANT);
                        }
                    }
                    // Set consequenceTypeTemplate.aChange
//                    consequenceType.setAaChange(referenceA + "/" + alternativeA);
                    consequenceType.getProteinVariantAnnotation().setReference(referenceA);
                    consequenceType.getProteinVariantAnnotation().setAlternate(alternativeA);
                    // Fill consequenceTypeTemplate.codon leaving only the nt that changes in uppercase.
                    // Careful with upper/lower case letters
                    char[] referenceCodonArray = String.valueOf(referenceCodon).toLowerCase().toCharArray();
                    referenceCodonArray[variantPhaseShift] = Character.toUpperCase(referenceCodonArray[variantPhaseShift]);
                    modifiedCodonArray = String.valueOf(modifiedCodonArray).toLowerCase().toCharArray();
                    modifiedCodonArray[variantPhaseShift] = Character.toUpperCase(modifiedCodonArray[variantPhaseShift]);
                    consequenceType.setCodon(String.valueOf(referenceCodonArray) + "/" + String.valueOf(modifiedCodonArray));
                }
            }
        }
        if (!codingAnnotationAdded) {
            SoNames.add("coding_sequence_variant");
        }
    }

    private void solveTranscriptFlankingRegions(String leftRegionTag, String rightRegionTag) {
        // Variant within -5kb region
        if (variant.getStart() > (transcript.getStart() - 5001) && variant.getStart() < transcript.getStart()) {
            // Variant within -2kb region
            if (variant.getStart() > (transcript.getStart() - 2001)) {
                SoNames.add("2KB_" + leftRegionTag.replace(DOWN_UP_STREAM_GENE_TAG, ""));
            } else {
                SoNames.add(leftRegionTag);
            }
        }
        // Variant within +5kb region
        if (variant.getStart() > transcript.getEnd() && variant.getStart() < (transcript.getEnd() + 5001)) {
            // Variant within +2kb region
            if (variant.getStart() < (transcript.getEnd() + 2001)) {
                SoNames.add("2KB_" + rightRegionTag.replace(DOWN_UP_STREAM_GENE_TAG, ""));
            } else {
                SoNames.add(rightRegionTag);
            }
        }
    }

    protected void solveNonCodingPositiveTranscript() {
        Exon exon = transcript.getExons().get(0);
        int exonSize = exon.getEnd() - exon.getStart() + 1;
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantPosition = -1;
        boolean[] junctionSolution = {false, false};
        junctionSolution[0] = false;
        junctionSolution[1] = false;
        ExonOverlap exonOverlap = null;

        if (variant.getStart() >= exon.getStart()) {
            if (variant.getStart() <= exon.getEnd()) {  // Variant start within the exon
                cdnaVariantPosition = cdnaExonEnd - (exon.getEnd() - variant.getStart());
                consequenceType.setCdnaPosition(cdnaVariantPosition);
                exonOverlap = new ExonOverlap(
                        (new StringBuilder()).append(exon.getExonNumber()).append("/").append(transcript.getExons()
                                .size()).toString(),
                        100f / exonSize);
//                consequenceType.setExonNumber(exon.getExonNumber());
            }
        }

        int exonCounter = 1;
        // This is not a do-while since we cannot call solveJunction  until
        while (exonCounter < transcript.getExons().size() && variantAhead) {
            int prevSpliceSite = exon.getEnd() + 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            exonSize = exon.getEnd() - exon.getStart() + 1;
            solveJunction(prevSpliceSite, exon.getStart() - 1, VariantAnnotationUtils.SPLICE_DONOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT, junctionSolution);

            if (variant.getStart() >= exon.getStart()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variant.getStart() <= exon.getEnd()) {  // Variant within the exon
                    cdnaVariantPosition = cdnaExonEnd - (exon.getEnd() - variant.getStart());
                    consequenceType.setCdnaPosition(cdnaVariantPosition);
                    exonOverlap = new ExonOverlap(
                            (new StringBuilder()).append(exon.getExonNumber()).append("/").append(transcript.getExons()
                                    .size()).toString(),
                            100f / exonSize);
//                    consequenceType.setExonNumber(exon.getExonNumber());
                }
            } else {
                variantAhead = false;
            }
            exonCounter++;
        }
        if (exonOverlap != null) {
            consequenceType.setExonOverlap(Collections.singletonList(exonOverlap));
        }
        solveMiRNA(cdnaVariantPosition, junctionSolution[1]);
    }

    private void solveMiRNA(int cdnaVariantPosition, boolean isIntronicVariant) {
        if (transcript.getBiotype().equals(VariantAnnotationUtils.MIRNA)) {  // miRNA with miRBase data
            if (gene.getMirna() != null) {
                // This if provides equivalent functionality to the one in the original (before refactoring) version,
                // may be modified in the future
                if (cdnaVariantPosition == -1) {
                    SoNames.add(VariantAnnotationUtils.MATURE_MIRNA_VARIANT);
                } else {
                    List<MiRNAGene.MiRNAMature> miRNAMatureList = gene.getMirna().getMatures();
                    int i = 0;
                    while (i < miRNAMatureList.size() && (cdnaVariantPosition < miRNAMatureList.get(i).cdnaStart
                            || cdnaVariantPosition > miRNAMatureList.get(i).cdnaEnd)) {
                        i++;
                    }
                    if (i < miRNAMatureList.size()) {  // Variant overlaps at least one mature miRNA
                        SoNames.add(VariantAnnotationUtils.MATURE_MIRNA_VARIANT);
                    } else {
                        addNonCodingSOs(isIntronicVariant);
                    }
                }
            } else {
                addNonCodingSOs(isIntronicVariant);
            }
        } else {
            addNonCodingSOs(isIntronicVariant);
        }
    }

    protected void solveCodingPositiveTranscript() {

        Exon exon = transcript.getExons().get(0);
        int exonSize = exon.getEnd() - exon.getStart() + 1;
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantPosition = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;
        ExonOverlap exonOverlap = null;

        if (transcript.getGenomicCodingStart() <= exon.getEnd()) {
            firstCdsPhase = exon.getPhase();
        }
        if (variant.getStart() >= exon.getStart() && variant.getStart() <= exon.getEnd()) {  // Variant start within the exon
            cdnaVariantPosition = cdnaExonEnd - (exon.getEnd() - variant.getStart());
            consequenceType.setCdnaPosition(cdnaVariantPosition);
            exonOverlap = new ExonOverlap(
                    (new StringBuilder()).append(exon.getExonNumber()).append("/").append(transcript.getExons().size())
                            .toString(),
                    100f / exonSize);
//            consequenceType.setExonNumber(exon.getExonNumber());
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

            if (variant.getStart() >= exon.getStart()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variant.getStart() <= exon.getEnd()) {  // Variant within the exon
                    cdnaVariantPosition = cdnaExonEnd - (exon.getEnd() - variant.getStart());
                    consequenceType.setCdnaPosition(cdnaVariantPosition);
                    exonOverlap = new ExonOverlap(
                            (new StringBuilder()).append(exon.getExonNumber()).append("/").append(transcript.getExons()
                                    .size()).toString(),
                            100f / exonSize);
//                    consequenceType.setExonNumber(exon.getExonNumber());
                }
            } else {
                variantAhead = false;
            }
            exonCounter++;
        }
        if (exonOverlap != null) {
            consequenceType.setExonOverlap(Collections.singletonList(exonOverlap));
        }
        // Is not intron variant (both ends fall within the same intron)
        if (!junctionSolution[1]) {
            solveExonVariantInPositiveTranscript(splicing, transcriptSequence, cdnaVariantPosition, firstCdsPhase);
        }
    }

    private void solveExonVariantInPositiveTranscript(boolean splicing, String transcriptSequence, int cdnaVariantPosition,
                                                      int firstCdsPhase) {
        if (variant.getStart() < transcript.getGenomicCodingStart()
                && (transcript.getStart() < transcript.getGenomicCodingStart()
                || transcript.unconfirmedStart())) { // Check transcript has 5 UTR
            SoNames.add(VariantAnnotationUtils.FIVE_PRIME_UTR_VARIANT);
        } else if (variant.getStart() <= transcript.getGenomicCodingEnd()) {  // Variant start within coding region
            // Need to define a local cdnaCodingStart because may modified in two lines below
            int cdnaCodingStart = transcript.getCdnaCodingStart();
            cdnaCodingStart = setCdsAndProteinPosition(cdnaVariantPosition, firstCdsPhase, cdnaCodingStart);
            solveCodingExonVariantInPositiveTranscript(splicing, transcriptSequence, cdnaCodingStart,
                    cdnaVariantPosition);
        } else {
            if (transcript.getEnd() > transcript.getGenomicCodingEnd() || transcript.unconfirmedEnd()) { // Check transcript has 3 UTR)
                SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
            }
        }
    }

    private void solveCodingExonVariantInPositiveTranscript(boolean splicing, String transcriptSequence, int cdnaCodingStart,
                                                            int cdnaVariantPosition) {
        // This will indicate whether it is needed to add the "coding_sequence_variant" annotation or not
        boolean codingAnnotationAdded = false;
        if (cdnaVariantPosition != -1) {
            int finalNtPhase = (transcript.getCdnaCodingEnd() - cdnaCodingStart) % 3;
            if (!splicing) {
                //  Variant in the last codon of a transcript without stop codon. finalNtPhase==2 if the cds length is multiple of 3.
                if ((cdnaVariantPosition >= (transcript.getCdnaCodingEnd() - finalNtPhase)) && finalNtPhase != 2) {
                    // If not, avoid calculating reference/modified codon
                    SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);
                } else if (cdnaVariantPosition > (cdnaCodingStart + 2)
                        || cdnaCodingStart > 0) {  // cdnaCodingStart<1 if cds_start_NF and phase!=0
                    int variantPhaseShift = (cdnaVariantPosition - cdnaCodingStart) % 3;
                    int modifiedCodonStart = cdnaVariantPosition - variantPhaseShift;
                    // -1 and +2 because of base 0 String indexing
                    String referenceCodon = transcriptSequence.substring(modifiedCodonStart - 1, modifiedCodonStart + 2);
                    char[] modifiedCodonArray = referenceCodon.toCharArray();
                    modifiedCodonArray[variantPhaseShift] = variant.getAlternate().toCharArray()[0];
                    String referenceA =
                            VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), referenceCodon);
                    String alternativeA =
                            VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()),
                                    String.valueOf(modifiedCodonArray));
                    codingAnnotationAdded = true;
                    if (VariantAnnotationUtils.isSynonymousCodon(MT.equals(variant.getChromosome()),
                            referenceCodon, String.valueOf(modifiedCodonArray))) {
                        if (VariantAnnotationUtils.isStopCodon(MT.equals(variant.getChromosome()), referenceCodon)) {
                            SoNames.add(VariantAnnotationUtils.STOP_RETAINED_VARIANT);
                        } else {  // coding end may be not correctly annotated (incomplete_terminal_codon_variant),
                            // but if the length of the cds%3=0, annotation should be synonymous variant
                            SoNames.add(VariantAnnotationUtils.SYNONYMOUS_VARIANT);
                        }
                    } else {
                        if (cdnaVariantPosition < (cdnaCodingStart + 3)
                                && !transcript.unconfirmedStart()) {

//                                && (!transcript.unconfirmedStart()
//                                    || VariantAnnotationUtils.isStartCodon(MT.equals(variant.getChromosome()), referenceCodon))) {
                            // Gary - initiator codon SO terms not compatible with the terms below
                            SoNames.add(VariantAnnotationUtils.START_LOST);
                            if (VariantAnnotationUtils.isStopCodon(MT.equals(variant.getChromosome()),
                                    String.valueOf(modifiedCodonArray))) {
                                // Gary - initiator codon SO terms not compatible with the terms below
                                SoNames.add(VariantAnnotationUtils.STOP_GAINED);
                            }
                        } else if (VariantAnnotationUtils.isStopCodon(MT.equals(variant.getChromosome()),
                                String.valueOf(referenceCodon))) {
                            SoNames.add(VariantAnnotationUtils.STOP_LOST);
                        } else {
                            SoNames.add(VariantAnnotationUtils.isStopCodon(MT.equals(variant.getChromosome()),
                                    String.valueOf(modifiedCodonArray))
                                    ? VariantAnnotationUtils.STOP_GAINED : VariantAnnotationUtils.MISSENSE_VARIANT);
                        }
                    }
                    // Set consequenceTypeTemplate.aChange
                    consequenceType.getProteinVariantAnnotation().setReference(referenceA);
                    consequenceType.getProteinVariantAnnotation().setAlternate(alternativeA);
                    // Set consequenceTypeTemplate.codon leaving only the nt that changes in uppercase.
                    // Careful with upper/lower case letters
                    char[] referenceCodonArray = referenceCodon.toLowerCase().toCharArray();
                    referenceCodonArray[variantPhaseShift] = Character.toUpperCase(referenceCodonArray[variantPhaseShift]);
                    modifiedCodonArray = String.valueOf(modifiedCodonArray).toLowerCase().toCharArray();
                    modifiedCodonArray[variantPhaseShift] = Character.toUpperCase(modifiedCodonArray[variantPhaseShift]);
                    consequenceType.setCodon(String.valueOf(referenceCodonArray) + "/" + String.valueOf(modifiedCodonArray));
                }
            }
        }
        if (!codingAnnotationAdded) {
            SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
        }
    }

    private void solveJunction(Integer spliceSite1, Integer spliceSite2, String leftSpliceSiteTag,
                               String rightSpliceSiteTag, boolean[] junctionSolution) {

        junctionSolution[0] = false;  // Is splicing variant in non-coding region
        junctionSolution[1] = false;  // Variant is intronic and both ends fall within the intron

        // Variant overlaps the rest of intronic region (splice region within the intron and/or rest of intron)
        if (variant.getStart() > (spliceSite1 + 1) && variant.getStart() < (spliceSite2 - 1)) {
            SoNames.add(VariantAnnotationUtils.INTRON_VARIANT);
            junctionSolution[1] = true;  // variant falls within the intron
        } else {
            if (variant.getStart() >= spliceSite1 && variant.getStart() <= spliceSite2) {
                junctionSolution[1] = true;  // variant falls within the intron
            }
        }

        if (variant.getStart().equals(spliceSite1) || variant.getStart().equals(spliceSite1 + 1)) {  // Variant donor/acceptor
            SoNames.add(leftSpliceSiteTag);  // donor/acceptor depending on transcript strand
            // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            junctionSolution[0] = (variant.getStart() <= spliceSite2);
        } else {
            if (variant.getStart() > (spliceSite1 + 1) && variant.getStart() < (spliceSite1 + 8)) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
                // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
                junctionSolution[0] = (variant.getStart() <= spliceSite2);
            } else {
                // Insertion coordinates are passed to this function as (variantStart-1,variantStart)
                if (variant.getStart() > (spliceSite1 - 4) && variant.getStart() < spliceSite1) {
                    SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
                }
            }
        }

        if (variant.getStart().equals(spliceSite2 - 1) || variant.getStart().equals(spliceSite2)) {  // Variant donor/acceptor
            SoNames.add(rightSpliceSiteTag);  // donor/acceptor depending on transcript strand
            // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            junctionSolution[0] = (variant.getStart() >= spliceSite1);
        } else {
            if (variant.getStart() < (spliceSite2 - 1) && variant.getStart() > (spliceSite2 - 8)) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
                // BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
                junctionSolution[0] = (spliceSite1 <= variant.getStart());
            } else {
                // Insertion coordinates are passed to this function as (variantStart-1,variantStart)
                if (variant.getStart() < (spliceSite2 + 4) && variant.getStart() > spliceSite2) {
                    SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
                }
            }
        }

    }


}
