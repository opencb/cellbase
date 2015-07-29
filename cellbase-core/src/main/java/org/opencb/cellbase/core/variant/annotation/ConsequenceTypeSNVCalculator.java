package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.MiRNAGene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.annotation.ConsequenceType;
import org.opencb.biodata.models.variant.annotation.Score;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.regulatory.RegulatoryRegion;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.*;

/**
 * Created by fjlopez on 22/06/15.
 */
public class ConsequenceTypeSNVCalculator extends ConsequenceTypeCalculator {

    public ConsequenceTypeSNVCalculator() {
    }

    public List<ConsequenceType> run(GenomicVariant inputVariant, List<Gene> geneList,
                                     List<RegulatoryRegion> regulatoryRegionList) {
        return run(inputVariant, geneList, null, regulatoryRegionList);
    }

    public List<ConsequenceType> run(GenomicVariant inputVariant, List<Gene> geneList,
                                 Map<String,MiRNAGene> inputMiRNAMap, List<RegulatoryRegion> regulatoryRegionList) {

        List<ConsequenceType> consequenceTypeList = new ArrayList<>();
        variant = inputVariant;
        miRNAMap = inputMiRNAMap;
        boolean isIntegernic = true;
        for(Gene currentGene: geneList) {
            gene = currentGene;
            isIntegernic = isIntegernic && (variant.getPosition()<gene.getStart() || variant.getPosition()>gene.getEnd());
            for(Transcript currentTranscript : gene.getTranscripts()) {
                transcript = currentTranscript;
                consequenceType = new ConsequenceType();
                consequenceType.setGeneName(gene.getName());
                consequenceType.setEnsemblGeneId(gene.getId());
                consequenceType.setEnsemblTranscriptId(transcript.getId());
                consequenceType.setStrand(transcript.getStrand());
                consequenceType.setBiotype(transcript.getBiotype());
                SoNames.clear();

                if(transcript.getStrand().equals("+")) {
                    // Check variant overlaps transcript start/end coordinates
                    if(variant.getPosition()>=transcript.getStart() &&
                            variant.getPosition()<=transcript.getEnd()) {
                        switch (transcript.getBiotype()) {
                            /**
                             * Coding biotypes
                             */
                            case VariantAnnotationUtils.NONSENSE_MEDIATED_DECAY:
                                SoNames.add(VariantAnnotationUtils.NMD_TRANSCRIPT_VARIANT);
                            case VariantAnnotationUtils.IG_C_GENE:
                            case VariantAnnotationUtils.IG_D_GENE:
                            case VariantAnnotationUtils.IG_J_GENE:
                            case VariantAnnotationUtils.IG_V_GENE:
                            case VariantAnnotationUtils.TR_C_GENE:  // TR_C_gene
                            case VariantAnnotationUtils.TR_D_GENE:  // TR_D_gene
                            case VariantAnnotationUtils.TR_J_GENE:  // TR_J_gene
                            case VariantAnnotationUtils.TR_V_GENE:  // TR_V_gene
                            case VariantAnnotationUtils.POLYMORPHIC_PSEUDOGENE:
                            case VariantAnnotationUtils.PROTEIN_CODING:    // protein_coding
                            case VariantAnnotationUtils.NON_STOP_DECAY:    // non_stop_decay
                            case VariantAnnotationUtils.TRANSLATED_PROCESSED_PSEUDOGENE:
                            case VariantAnnotationUtils.TRANSLATED_UNPROCESSED_PSEUDOGENE:    // translated_unprocessed_pseudogene
                            case VariantAnnotationUtils.LRG_GENE:    // LRG_gene
                                solveCodingPositiveTranscript();
                                consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                                consequenceTypeList.add(consequenceType);
                                break;
                            /**
                             * Non-coding biotypes
                             */
                            default:
                                solveNonCodingPositiveTranscript();
                                consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                                consequenceTypeList.add(consequenceType);
                                break;
                        }
                    } else {
                        solveTranscriptFlankingRegions(VariantAnnotationUtils.UPSTREAM_GENE_VARIANT,
                                VariantAnnotationUtils.DOWNSTREAM_GENE_VARIANT);
                        if (SoNames.size() > 0) { // Variant does not overlap gene region, just may have upstream/downstream annotations
                            consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                            consequenceTypeList.add(consequenceType);
                        }
                    }

                } else {
                    // Check overlaps transcript start/end coordinates
                    if(variant.getPosition()>=transcript.getStart() &&
                            variant.getPosition()<=transcript.getEnd()) {
                        switch (transcript.getBiotype()) {
                            /**
                             * Coding biotypes
                             */
                            case VariantAnnotationUtils.NONSENSE_MEDIATED_DECAY:
                                SoNames.add(VariantAnnotationUtils.NMD_TRANSCRIPT_VARIANT);
                            case VariantAnnotationUtils.IG_C_GENE:
                            case VariantAnnotationUtils.IG_D_GENE:
                            case VariantAnnotationUtils.IG_J_GENE:
                            case VariantAnnotationUtils.IG_V_GENE:
                            case VariantAnnotationUtils.TR_C_GENE:  // TR_C_gene
                            case VariantAnnotationUtils.TR_D_GENE:  // TR_D_gene
                            case VariantAnnotationUtils.TR_J_GENE:  // TR_J_gene
                            case VariantAnnotationUtils.TR_V_GENE:  // TR_V_gene
                            case VariantAnnotationUtils.POLYMORPHIC_PSEUDOGENE:
                            case VariantAnnotationUtils.PROTEIN_CODING:    // protein_coding
                            case VariantAnnotationUtils.NON_STOP_DECAY:    // non_stop_decay
                            case VariantAnnotationUtils.TRANSLATED_PROCESSED_PSEUDOGENE:
                            case VariantAnnotationUtils.TRANSLATED_UNPROCESSED_PSEUDOGENE:    // translated_unprocessed_pseudogene
                            case VariantAnnotationUtils.LRG_GENE:    // LRG_gene
                                solveCodingNegativeTranscript();
                                consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                                consequenceTypeList.add(consequenceType);
                                break;
                            /**
                             * Non-coding biotypes
                             */
                            default:
                                solveNonCodingNegativeTranscript();
                                consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                                consequenceTypeList.add(consequenceType);
                                break;
                        }
                    } else {
                        solveTranscriptFlankingRegions(VariantAnnotationUtils.DOWNSTREAM_GENE_VARIANT,
                                VariantAnnotationUtils.UPSTREAM_GENE_VARIANT);
                        if (SoNames.size() > 0) { // Variant does not overlap gene region, just has upstream/downstream annotations
                            consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                            consequenceTypeList.add(consequenceType);
                        }
                    }
                }
            }
        }

        if(consequenceTypeList.size()==0 && isIntegernic) {
            consequenceTypeList.add(new ConsequenceType(VariantAnnotationUtils.INTERGENIC_VARIANT));
        }

        solveRegulatoryRegions(regulatoryRegionList, consequenceTypeList);

        return consequenceTypeList;

    }

    private void solveNonCodingNegativeTranscript() {

        Exon exon = transcript.getExons().get(0);
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd()-exon.getStart()+1);  // cdnaExonEnd poinst to the same base than exonStart
        int cdnaVariantPosition = -1;
        boolean[] junctionSolution = {false, false};

        if(variant.getPosition()<=exon.getEnd() && variant.getPosition()>=exon.getStart()) {  // Variant within the exon
            cdnaVariantPosition = cdnaExonEnd - (variant.getPosition() - exon.getStart());
            consequenceType.setcDnaPosition(cdnaVariantPosition);
        }

        int exonCounter = 1;
        while(exonCounter<transcript.getExons().size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            int prevSpliceSite = exon.getStart()-1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            transcriptSequence = exon.getSequence()+transcriptSequence;
            solveJunction(exon.getEnd()+1, prevSpliceSite, VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_DONOR_VARIANT, junctionSolution);

            if(variant.getPosition() <= exon.getEnd()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if(variant.getPosition() >= exon.getStart()) {  // Variant end within the exon
                    cdnaVariantPosition = cdnaExonEnd - (variant.getPosition() - exon.getStart());
                    consequenceType.setcDnaPosition(cdnaVariantPosition);
                }
            } else {
                    variantAhead = false;
            }
            exonCounter++;
        }
        solveMiRNA(cdnaVariantPosition, junctionSolution[1]);
    }

    private void solveCodingNegativeTranscript() {

        Exon exon = transcript.getExons().get(0);
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd()-exon.getStart()+1);  // cdnaExonEnd poinst to the same base than exonStart
        int cdnaVariantPosition = -1;
        int firstCdsPhase=-1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;

        if(transcript.getGenomicCodingEnd()>=exon.getStart()) {
            firstCdsPhase = exon.getPhase();
        }
        if(variant.getPosition()<=exon.getEnd() && variant.getPosition()>=exon.getStart()) {  // Variant within the exon
            cdnaVariantPosition = cdnaExonEnd - (variant.getPosition() - exon.getStart());
            consequenceType.setcDnaPosition(cdnaVariantPosition);
        }

        int exonCounter = 1;
        while(exonCounter<transcript.getExons().size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            int prevSpliceSite = exon.getStart()-1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            transcriptSequence = exon.getSequence()+transcriptSequence;
            if(firstCdsPhase==-1 && transcript.getGenomicCodingEnd()>=exon.getStart()) {  // Set firsCdsPhase only when the first coding exon is reached
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(exon.getEnd()+1, prevSpliceSite, VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_DONOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if(variant.getPosition() <= exon.getEnd()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if(variant.getPosition() >= exon.getStart()) {  // Variant end within the exon
                    cdnaVariantPosition = cdnaExonEnd - (variant.getPosition() - exon.getStart());
                    consequenceType.setcDnaPosition(cdnaVariantPosition);
                }
            } else {
                    variantAhead = false;
            }
            exonCounter++;
        }
        // Is not intron variant (both ends fall within the same intron)
        if(!junctionSolution[1]) {
            solveExonVariantInNegativeTranscript(splicing, transcriptSequence, cdnaVariantPosition, firstCdsPhase);
        }
    }

    private void solveExonVariantInNegativeTranscript(boolean splicing, String transcriptSequence,
                                                      int cdnaVariantPosition, int firstCdsPhase) {
        if(variant.getPosition() > transcript.getGenomicCodingEnd() &&
            (transcript.getEnd()>transcript.getGenomicCodingEnd() || transcript.unconfirmedStart())) {// Check transcript has 3 UTR
                SoNames.add(VariantAnnotationUtils.FIVE_PRIME_UTR_VARIANT);
        } else if(variant.getPosition()>=transcript.getGenomicCodingStart()) {
            int cdnaCodingStart = transcript.getCdnaCodingStart();  // Need to define a local cdnaCodingStart because may modified in two lines below
            if(cdnaVariantPosition!=-1) {  // cdnaVariantStart may be null if variantEnd falls in an intron
                if(transcript.unconfirmedStart()) {
                    cdnaCodingStart -= ((3-firstCdsPhase)%3);
                }
                int cdsVariantStart = cdnaVariantPosition - cdnaCodingStart + 1;
                consequenceType.setCdsPosition(cdsVariantStart);
                consequenceType.setAaPosition(((cdsVariantStart - 1)/3)+1);
            }
            solveCodingExonVariantInNegativeTranscript(splicing, transcriptSequence, cdnaCodingStart, cdnaVariantPosition);
        } else {
            if(transcript.getStart()<transcript.getGenomicCodingStart() || transcript.unconfirmedEnd()) {// Check transcript has 3 UTR)
                SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
            }
        }
    }

    private void solveCodingExonVariantInNegativeTranscript(Boolean splicing, String transcriptSequence,
                                                            int cdnaCodingStart, int cdnaVariantPosition) {

        Boolean codingAnnotationAdded = false;
        if(cdnaVariantPosition != -1) {
            int finalNtPhase = (transcriptSequence.length() - cdnaCodingStart) % 3;
            if (!splicing) {
                if ((cdnaVariantPosition >= (transcriptSequence.length() - finalNtPhase)) &&
                        (transcript.getStart()==transcript.getGenomicCodingStart()) && finalNtPhase != 2) {  //  Variant in the last codon of a transcript without stop codon. finalNtPhase==2 if the cds length is multiple of 3.
                    SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);                                       // If that is the case and variant ocurs in the last complete/incomplete codon, no coding prediction is needed
                } else if (cdnaVariantPosition>(cdnaCodingStart+2) || cdnaCodingStart>0) {  // cdnaCodingStart<1 if cds_start_NF and phase!=0
                    Integer variantPhaseShift = (cdnaVariantPosition - cdnaCodingStart) % 3;
                    int modifiedCodonStart = cdnaVariantPosition - variantPhaseShift;
                    String reverseCodon = new StringBuilder(transcriptSequence.substring(transcriptSequence.length() - modifiedCodonStart - 2,
                            transcriptSequence.length() - modifiedCodonStart + 1)).reverse().toString(); // Rigth limit of the substring sums +1 because substring does not include that position
                    char[] referenceCodon = reverseCodon.toCharArray();
                    referenceCodon[0] = VariantAnnotationUtils.complementaryNt.get(referenceCodon[0]);
                    referenceCodon[1] = VariantAnnotationUtils.complementaryNt.get(referenceCodon[1]);
                    referenceCodon[2] = VariantAnnotationUtils.complementaryNt.get(referenceCodon[2]);
                    char[] modifiedCodonArray = referenceCodon.clone();
                    modifiedCodonArray[variantPhaseShift] = VariantAnnotationUtils.complementaryNt.get(variant.getAlternative().toCharArray()[0]);
                    codingAnnotationAdded = true;
                    String referenceA = VariantAnnotationUtils.codonToA.get(String.valueOf(referenceCodon));
                    String alternativeA = VariantAnnotationUtils.codonToA.get(String.valueOf(modifiedCodonArray));

                    if (VariantAnnotationUtils.isSynonymousCodon.get(String.valueOf(referenceCodon)).get(String.valueOf(modifiedCodonArray))) {
                        if (VariantAnnotationUtils.isStopCodon(String.valueOf(referenceCodon))) {
                            SoNames.add(VariantAnnotationUtils.STOP_RETAINED_VARIANT);
                        } else {  // coding end may be not correctly annotated (incomplete_terminal_codon_variant), but if the length of the cds%3=0, annotation should be synonymous variant
                            SoNames.add(VariantAnnotationUtils.SYNONYMOUS_VARIANT);
                        }
                    } else {
                        if (cdnaVariantPosition<(cdnaCodingStart+3)) {
                            SoNames.add(VariantAnnotationUtils.INITIATOR_CODON_VARIANT);  // Gary - initiator codon SO terms not compatible with the terms below
                            if(VariantAnnotationUtils.isStopCodon(String.valueOf(modifiedCodonArray))) {
                                SoNames.add(VariantAnnotationUtils.STOP_GAINED);  // Gary - initiator codon SO terms not compatible with the terms below
                            }
                        } else if (VariantAnnotationUtils.isStopCodon(String.valueOf(referenceCodon))) {
                            SoNames.add(VariantAnnotationUtils.STOP_LOST);
                        } else {
                            SoNames.add(VariantAnnotationUtils.isStopCodon(String.valueOf(modifiedCodonArray)) ?
                                    VariantAnnotationUtils.STOP_GAINED : VariantAnnotationUtils.MISSENSE_VARIANT);
                        }
                    }
                    // Set consequenceTypeTemplate.aChange
                    consequenceType.setAaChange(referenceA + "/" + alternativeA);
                    // Fill consequenceTypeTemplate.codon leaving only the nt that changes in uppercase. Careful with upper/lower case letters
                    char[] referenceCodonArray = String.valueOf(referenceCodon).toLowerCase().toCharArray();
                    referenceCodonArray[variantPhaseShift] = Character.toUpperCase(referenceCodonArray[variantPhaseShift]);
                    modifiedCodonArray = String.valueOf(modifiedCodonArray).toLowerCase().toCharArray();
                    modifiedCodonArray[variantPhaseShift] = Character.toUpperCase(modifiedCodonArray[variantPhaseShift]);
                    consequenceType.setCodon(String.valueOf(referenceCodonArray) + "/" + String.valueOf(modifiedCodonArray));
                }
            }
        }
        if(!codingAnnotationAdded) {
            SoNames.add("coding_sequence_variant");
        }
    }

    private void solveTranscriptFlankingRegions(String leftRegionTag, String rightRegionTag) {
        // Variant within -5kb region
        if(variant.getPosition()>(transcript.getStart()-5001) && variant.getPosition()<transcript.getStart()) {
            // Variant within -2kb region
            if(variant.getPosition()>(transcript.getStart()-2001)) {
                SoNames.add("2KB_" + leftRegionTag);
            } else {
                SoNames.add(leftRegionTag);
            }
        }
        // Variant within +5kb region
        if(variant.getPosition()>transcript.getEnd() && variant.getPosition()<(transcript.getEnd()+5001)) {
            // Variant within +2kb region
            if(variant.getPosition()<(transcript.getEnd()+2001)) {
                SoNames.add("2KB_" + rightRegionTag);
            } else {
                SoNames.add(rightRegionTag);
            }
        }
    }

    private void solveNonCodingPositiveTranscript() {
        Exon exon = transcript.getExons().get(0);
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantPosition = -1;
        boolean[] junctionSolution = {false, false};
        junctionSolution[0] = false;
        junctionSolution[1] = false;

        if(variant.getPosition() >= exon.getStart()) {
            if(variant.getPosition() <= exon.getEnd()) {  // Variant start within the exon
                cdnaVariantPosition = cdnaExonEnd - (exon.getEnd() - variant.getPosition());
                consequenceType.setcDnaPosition(cdnaVariantPosition);
            }
        }

        int exonCounter = 1;
        while(exonCounter<transcript.getExons().size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            int prevSpliceSite = exon.getEnd()+1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            solveJunction(prevSpliceSite, exon.getStart()-1, VariantAnnotationUtils.SPLICE_DONOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT, junctionSolution);

            if(variant.getPosition() >= exon.getStart()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if(variant.getPosition() <= exon.getEnd()) {  // Variant within the exon
                    cdnaVariantPosition = cdnaExonEnd - (exon.getEnd() - variant.getPosition());
                    consequenceType.setcDnaPosition(cdnaVariantPosition);
                }
            } else {
                variantAhead = false;
            }
            exonCounter++;
        }
        solveMiRNA(cdnaVariantPosition, junctionSolution[1]);
    }

    private void solveMiRNA(int cdnaVariantPosition, boolean isIntronicVariant) {
        if (transcript.getBiotype().equals(VariantAnnotationUtils.MIRNA)) {  // miRNA with miRBase data
            MiRNAGene miRNAGene;
            if(miRNAMap!=null && (miRNAGene = miRNAMap.get(gene.getId()))!=null) {
                // This if provides equivalent functionality to the one in the original (before refactoring) version, may be modified in the future
                if (cdnaVariantPosition == -1) {
                    SoNames.add(VariantAnnotationUtils.MATURE_MIRNA_VARIANT);
                } else {
                    List<MiRNAGene.MiRNAMature> miRNAMatureList = miRNAGene.getMatures();
                    int i = 0;
                    while (i < miRNAMatureList.size() && (cdnaVariantPosition < miRNAMatureList.get(i).cdnaStart ||
                            cdnaVariantPosition > miRNAMatureList.get(i).cdnaEnd)) {
                        i++;
                    }
                    if (i < miRNAMatureList.size()) {  // Variant overlaps at least one mature miRNA
                        SoNames.add(VariantAnnotationUtils.MATURE_MIRNA_VARIANT);
                    } else {
                        if (!isIntronicVariant) {  // Exon variant
                            SoNames.add(VariantAnnotationUtils.NON_CODING_TRANSCRIPT_EXON_VARIANT);
                        }
                        SoNames.add(VariantAnnotationUtils.NON_CODING_TRANSCRIPT_VARIANT);
                    }
                }
            }
        } else {
            if (!isIntronicVariant) {  // Exon variant
                SoNames.add(VariantAnnotationUtils.NON_CODING_TRANSCRIPT_EXON_VARIANT);
            }
            SoNames.add(VariantAnnotationUtils.NON_CODING_TRANSCRIPT_VARIANT);
        }
    }

    private void solveCodingPositiveTranscript() {

        Exon exon = transcript.getExons().get(0);
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantPosition = -1;
        int firstCdsPhase=-1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;

        if(transcript.getGenomicCodingStart()<=exon.getEnd()) {
            firstCdsPhase = exon.getPhase();
        }
        if(variant.getPosition()>=exon.getStart()  && variant.getPosition()<=exon.getEnd()) {  // Variant start within the exon
            cdnaVariantPosition = cdnaExonEnd - (exon.getEnd() - variant.getPosition());
            consequenceType.setcDnaPosition(cdnaVariantPosition);
        }

        int exonCounter = 1;
        while(exonCounter<transcript.getExons().size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            int prevSpliceSite = exon.getEnd()+1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            transcriptSequence = transcriptSequence + exon.getSequence();
            if(firstCdsPhase==-1 && transcript.getGenomicCodingStart()<=exon.getEnd()) {  // Set firsCdsPhase only when the first coding exon is reached
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(prevSpliceSite, exon.getStart()-1, VariantAnnotationUtils.SPLICE_DONOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if(variant.getPosition() >= exon.getStart()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if(variant.getPosition() <= exon.getEnd()) {  // Variant within the exon
                    cdnaVariantPosition = cdnaExonEnd - (exon.getEnd() - variant.getPosition());
                    consequenceType.setcDnaPosition(cdnaVariantPosition);
                }
            } else {
                variantAhead = false;
            }
            exonCounter++;
        }

        // Is not intron variant (both ends fall within the same intron)
        if(!junctionSolution[1]) {
            solveExonVariantInPositiveTranscript(splicing, transcriptSequence, cdnaVariantPosition, firstCdsPhase);
        }
    }

    private void solveExonVariantInPositiveTranscript(boolean splicing, String transcriptSequence,
                                                      int cdnaVariantPosition, int firstCdsPhase) {
        if(variant.getPosition()<transcript.getGenomicCodingStart() &&
                (transcript.getStart()<transcript.getGenomicCodingStart() || transcript.unconfirmedStart())) {// Check transcript has 5 UTR
            SoNames.add(VariantAnnotationUtils.FIVE_PRIME_UTR_VARIANT);
        } else if(variant.getPosition()<=transcript.getGenomicCodingEnd()) {  // Variant start within coding region
            int cdnaCodingStart = transcript.getCdnaCodingStart();  // Need to define a local cdnaCodingStart because may modified in two lines below
            if(cdnaVariantPosition!=-1) {  // cdnaVariantStart may be -1 if variantStart falls in an intron
                if(transcript.unconfirmedStart()) {
                    cdnaCodingStart -= ((3-firstCdsPhase)%3);
                }
                int cdsVariantStart = cdnaVariantPosition - cdnaCodingStart + 1;
                consequenceType.setCdsPosition(cdsVariantStart);
                consequenceType.setAaPosition(((cdsVariantStart - 1)/3)+1);
            }
            solveCodingExonVariantInPositiveTranscript(splicing, transcriptSequence, cdnaCodingStart,
                    cdnaVariantPosition);
        } else {
            if(transcript.getEnd()>transcript.getGenomicCodingEnd() || transcript.unconfirmedEnd()) {// Check transcript has 3 UTR)
                SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
            }
        }
    }

    private void solveCodingExonVariantInPositiveTranscript(boolean splicing, String transcriptSequence,
                                                            int cdnaCodingStart, int cdnaVariantPosition) {

        boolean codingAnnotationAdded = false;  // This will indicate wether it is needed to add the "coding_sequence_variant" annotation or not
        if(cdnaVariantPosition != -1) {
            int finalNtPhase = (transcriptSequence.length() - cdnaCodingStart) % 3;
            if (!splicing) {
                if ((cdnaVariantPosition >= (transcriptSequence.length() - finalNtPhase)) &&
                        (transcript.getEnd()==transcript.getGenomicCodingEnd()) && finalNtPhase != 2) {  //  Variant in the last codon of a transcript without stop codon. finalNtPhase==2 if the cds length is multiple of 3.
                    SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);               //  If not, avoid calculating reference/modified codon
                } else if (cdnaVariantPosition>(cdnaCodingStart+2) || cdnaCodingStart>0) {  // cdnaCodingStart<1 if cds_start_NF and phase!=0
                    int variantPhaseShift = (cdnaVariantPosition - cdnaCodingStart) % 3;
                    int modifiedCodonStart = cdnaVariantPosition - variantPhaseShift;
                    String referenceCodon = transcriptSequence.substring(modifiedCodonStart - 1, modifiedCodonStart + 2);  // -1 and +2 because of base 0 String indexing
                    char[] modifiedCodonArray = referenceCodon.toCharArray();
                    modifiedCodonArray[variantPhaseShift] = variant.getAlternative().toCharArray()[0];
                    String referenceA = VariantAnnotationUtils.codonToA.get(referenceCodon);
                    String alternativeA = VariantAnnotationUtils.codonToA.get(String.valueOf(modifiedCodonArray));
                    codingAnnotationAdded = true;
                    if (VariantAnnotationUtils.isSynonymousCodon.get(referenceCodon).get(String.valueOf(modifiedCodonArray))) {
                        if (VariantAnnotationUtils.isStopCodon(referenceCodon)) {
                            SoNames.add(VariantAnnotationUtils.STOP_RETAINED_VARIANT);
                        } else {  // coding end may be not correctly annotated (incomplete_terminal_codon_variant), but if the length of the cds%3=0, annotation should be synonymous variant
                            SoNames.add(VariantAnnotationUtils.SYNONYMOUS_VARIANT);
                        }
                    } else {
                        if (cdnaVariantPosition<(cdnaCodingStart+3)) {
                            SoNames.add(VariantAnnotationUtils.INITIATOR_CODON_VARIANT);  // Gary - initiator codon SO terms not compatible with the terms below
                            if(VariantAnnotationUtils.isStopCodon(String.valueOf(modifiedCodonArray))) {
                                SoNames.add(VariantAnnotationUtils.STOP_GAINED);  // Gary - initiator codon SO terms not compatible with the terms below
                            }
                        } else if (VariantAnnotationUtils.isStopCodon(String.valueOf(referenceCodon))) {
                            SoNames.add(VariantAnnotationUtils.STOP_LOST);
                        } else {
                            SoNames.add(VariantAnnotationUtils.isStopCodon(String.valueOf(modifiedCodonArray)) ?
                                    VariantAnnotationUtils.STOP_GAINED : VariantAnnotationUtils.MISSENSE_VARIANT);
                        }
                    }
                    // Set consequenceTypeTemplate.aChange
                    consequenceType.setAaChange(referenceA + "/" + alternativeA);
                    // Set consequenceTypeTemplate.codon leaving only the nt that changes in uppercase. Careful with upper/lower case letters
                    char[] referenceCodonArray = referenceCodon.toLowerCase().toCharArray();
                    referenceCodonArray[variantPhaseShift] = Character.toUpperCase(referenceCodonArray[variantPhaseShift]);
                    modifiedCodonArray = String.valueOf(modifiedCodonArray).toLowerCase().toCharArray();
                    modifiedCodonArray[variantPhaseShift] = Character.toUpperCase(modifiedCodonArray[variantPhaseShift]);
                    consequenceType.setCodon(String.valueOf(referenceCodonArray) + "/" + String.valueOf(modifiedCodonArray));
                }
            }
        }
        if(!codingAnnotationAdded) {
            SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
        }
    }

    private void solveJunction(Integer spliceSite1, Integer spliceSite2, String leftSpliceSiteTag,
                               String rightSpliceSiteTag, boolean[] junctionSolution) {

        junctionSolution[0] = false;  // Is splicing variant in non-coding region
        junctionSolution[1] = false;  // Variant is intronic and both ends fall within the intron

        if(variant.getPosition()>(spliceSite1+1) && variant.getPosition()<(spliceSite2-1)) {  // Variant overlaps the rest of intronic region (splice region within the intron and/or rest of intron)
            SoNames.add(VariantAnnotationUtils.INTRON_VARIANT);
            junctionSolution[1] = true;  // variant falls within the intron
        } else {
            if(variant.getPosition()>=spliceSite1 && variant.getPosition()<=spliceSite2) {
                junctionSolution[1] = true;  // variant falls within the intron
            }
        }

        if(variant.getPosition()==spliceSite1 || variant.getPosition()==(spliceSite1+1)) {  // Variant donor/acceptor
            SoNames.add(leftSpliceSiteTag);  // donor/acceptor depending on transcript strand
            junctionSolution[0] = (variant.getPosition()<=spliceSite2);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
        } else {
            if(variant.getPosition()>(spliceSite1+1) && variant.getPosition()<(spliceSite1+8)) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
                junctionSolution[0] = (variant.getPosition()<=spliceSite2);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            } else {
                if(variant.getPosition()>(spliceSite1-4) && variant.getPosition()<spliceSite1) {  // Insertion coordinates are passed to this function as (variantStart-1,variantStart)
                    SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
                }
            }
        }

        if(variant.getPosition()==(spliceSite2-1) || variant.getPosition()==spliceSite2) {  // Variant donor/acceptor
            SoNames.add(rightSpliceSiteTag);  // donor/acceptor depending on transcript strand
            junctionSolution[0] = (variant.getPosition()>=spliceSite1);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
        } else {
            if(variant.getPosition()<(spliceSite2-1) && variant.getPosition()>(spliceSite2-8)) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
                junctionSolution[0] = (variant.getPosition()<=spliceSite2);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            } else {
                if(variant.getPosition()<(spliceSite2+4) && variant.getPosition()>spliceSite2) {  // Insertion coordinates are passed to this function as (variantStart-1,variantStart)
                    SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
                }
            }
        }

    }


}
