package org.opencb.cellbase.core.variant.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.MiRNAGene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.annotation.ConsequenceType;
import org.opencb.biodata.models.variant.annotation.ExpressionValue;
import org.opencb.biodata.models.variant.annotation.Score;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.GenomeSequenceFeature;
import org.opencb.cellbase.core.common.regulatory.RegulatoryRegion;
import org.opencb.cellbase.core.db.DBAdaptor;
import org.opencb.cellbase.core.db.api.core.GenomeDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by fjlopez on 23/07/15.
 */
public class ConsequenceTypeInsertionCalculator extends ConsequenceTypeCalculator {

    private int variantStart;
    private int variantEnd;
    private GenomeDBAdaptor genomeDBAdaptor;

    public ConsequenceTypeInsertionCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        this.genomeDBAdaptor = genomeDBAdaptor;
    }

    public List<ConsequenceType> run(GenomicVariant inputVariant, List<Gene> geneList,
                                     List<RegulatoryRegion> regulatoryRegionList) {

        List<ConsequenceType> consequenceTypeList = new ArrayList<>();
        variant = inputVariant;
        variantEnd = variant.getPosition();
        variantStart = variant.getPosition() - 1;
        boolean isIntegernic = false;
        for (Gene currentGene : geneList) {
            gene = currentGene;
            isIntegernic = isIntegernic || (variantEnd < gene.getStart() || variantStart > gene.getEnd());
            for (Transcript currentTranscript : gene.getTranscripts()) {
                transcript = currentTranscript;
                consequenceType = new ConsequenceType();
                consequenceType.setGeneName(gene.getName());
                consequenceType.setEnsemblGeneId(gene.getId());
                consequenceType.setEnsemblTranscriptId(transcript.getId());
                consequenceType.setStrand(transcript.getStrand());
                consequenceType.setBiotype(transcript.getBiotype());
                SoNames.clear();

                if (transcript.getStrand().equals("+")) {
                    // Check variant overlaps transcript start/end coordinates
                    if (variantEnd > transcript.getStart() && variantStart < transcript.getEnd()) {
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
                    // Check variant overlaps transcript start/end coordinates
                    if (variantEnd > transcript.getStart() && variantStart < transcript.getEnd()) {
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

        if (consequenceTypeList.size() == 0 && isIntegernic) {
            consequenceTypeList.add(new ConsequenceType(VariantAnnotationUtils.INTERGENIC_VARIANT));
        }

        solveRegulatoryRegions(regulatoryRegionList, consequenceTypeList);

        return consequenceTypeList;
    }

    private void solveNonCodingNegativeTranscript() {
        Exon exon = transcript.getExons().get(0);
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;

        if(firstCdsPhase==-1 && transcript.getGenomicCodingEnd()>=exon.getStart()) {
            firstCdsPhase = exon.getPhase();
        }
        if(variantStart < exon.getEnd()) {
            if(variantEnd >= exon.getStart()) {  // Variant end within the exon
                cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                consequenceType.setcDnaPosition(cdnaVariantStart);
                if(variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                } else{  // Only variant start within the exon  ---ES||||||||||||----
                    cdnaVariantEnd = cdnaVariantStart + 1;  // To account for those insertions in the last nucleotide of the exon
                }
            }
        } else if(variantStart == exon.getEnd()) {
            cdnaVariantEnd = cdnaExonEnd - (variantEnd - exon.getStart());
            cdnaVariantStart = cdnaVariantEnd - 1;  // To account for those insertions in the first nucleotide of the exon
        }

        int exonCounter = 1;
        while(exonCounter<transcript.getExons().size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            int prevSpliceSite = exon.getStart()-1;
            if(firstCdsPhase==-1 && exon.getGenomicCodingEnd()>=exon.getStart()) {  // Set firsCdsPhase only when the first coding exon is reached
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(exon.getEnd()+1, prevSpliceSite, VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_DONOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if(variantStart < exon.getEnd()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if(variantEnd >= exon.getStart()) {  // Variant end within the exon
                    cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                    consequenceType.setcDnaPosition(cdnaVariantStart);
                    if(variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||SE|||||||||----
                        cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                    }
                }
            } else if(variantStart == exon.getEnd()) {  // Only variant start within the exon  ----||||||||||||||SE---
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
            } else {  // Variant does not include this exon, variant is located before this exon
                variantAhead = false;
            }
            exonCounter++;
        }
        solveMiRNA(cdnaVariantStart, cdnaVariantEnd, junctionSolution[1]);
    }

    private void solveCodingNegativeTranscript() {
        Exon exon = transcript.getExons().get(0);
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;

        if(firstCdsPhase==-1 && transcript.getGenomicCodingEnd()>=exon.getStart()) {
            firstCdsPhase = exon.getPhase();
        }
        if(variantStart < exon.getEnd()) {
            if(variantEnd >= exon.getStart()) {  // Variant end within the exon
                cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                consequenceType.setcDnaPosition(cdnaVariantStart);
                if(variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                } else{  // Only variant start within the exon  ---ES||||||||||||----
                    cdnaVariantEnd = cdnaVariantStart + 1;  // To account for those insertions in the last nucleotide of the exon
                }
            }
        } else if(variantStart == exon.getEnd()) {
            cdnaVariantEnd = cdnaExonEnd - (variantEnd - exon.getStart());
            cdnaVariantStart = cdnaVariantEnd - 1;  // To account for those insertions in the first nucleotide of the exon
        }

        int exonCounter = 1;
        while(exonCounter<transcript.getExons().size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            int prevSpliceSite = exon.getStart()-1;
            transcriptSequence = exon.getSequence()+transcriptSequence;
            if(firstCdsPhase==-1 && exon.getGenomicCodingEnd()>=exon.getStart()) {  // Set firsCdsPhase only when the first coding exon is reached
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(exon.getEnd()+1, prevSpliceSite, VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_DONOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if(variantStart < exon.getEnd()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if(variantEnd >= exon.getStart()) {  // Variant end within the exon
                    cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                    consequenceType.setcDnaPosition(cdnaVariantStart);
                    if(variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||SE|||||||||----
                        cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                    }
                }
            } else if(variantStart == exon.getEnd()) {  // Only variant start within the exon  ----||||||||||||||SE---
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
            } else {  // Variant does not include this exon, variant is located before this exon
                variantAhead = false;
            }
            exonCounter++;
        }
        // Is not intron variant (both ends fall within the same intron)
        if(!junctionSolution[1]) {
            solveExonVariantInNegativeTranscript(splicing, transcriptSequence, cdnaVariantStart, cdnaVariantEnd,
                    firstCdsPhase);
        }
    }

    private void solveExonVariantInNegativeTranscript(boolean splicing, String transcriptSequence,
                                                      int cdnaVariantStart, int cdnaVariantEnd, int firstCdsPhase) {
        if(variantEnd > transcript.getGenomicCodingEnd()) {
            if(transcript.getEnd()>transcript.getGenomicCodingEnd() || transcript.unconfirmedStart()) {// Check transcript has 3 UTR
                SoNames.add(VariantAnnotationUtils.FIVE_PRIME_UTR_VARIANT);
            }
        } else if(variantEnd>=transcript.getGenomicCodingStart()) {
            int cdnaCodingStart = transcript.getCdnaCodingStart();  // Need to define a local cdnaCodingStart because may modified in two lines below
            if(cdnaVariantStart!=-1) {  // cdnaVariantStart may be null if variantEnd falls in an intron
                if(transcript.unconfirmedStart()) {
                    cdnaCodingStart -= ((3-firstCdsPhase)%3);
                }
                int cdsVariantStart = cdnaVariantStart - cdnaCodingStart + 1;
                consequenceType.setCdsPosition(cdsVariantStart);
                consequenceType.setAaPosition(((cdsVariantStart - 1) / 3) + 1);
            }
            if(variantStart >= transcript.getGenomicCodingStart()) {  // Variant start also within coding region
                solveCodingExonVariantInNegativeTranscript(transcriptSequence, cdnaCodingStart, cdnaVariantStart,
                        cdnaVariantEnd);
            } else if(transcript.getStart()<transcript.getGenomicCodingStart() || transcript.unconfirmedEnd()) {// Check transcript has 3 UTR)
                    SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
            }
        } else {
            if(transcript.getStart()<transcript.getGenomicCodingStart() || transcript.unconfirmedEnd()) {// Check transcript has 3 UTR)
                SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
            }
        }
    }

    private void solveCodingExonVariantInNegativeTranscript(String transcriptSequence, int cdnaCodingStart,
                                                            int cdnaVariantStart, int cdnaVariantEnd) {
        if(cdnaVariantStart != -1) {  // Insertion
            if(cdnaVariantStart<(cdnaCodingStart+2) && transcript.unconfirmedStart()) {  // cdnaVariantStart=null if variant is intronic. cdnaCodingStart<1 if cds_start_NF and phase!=0
                SoNames.add(VariantAnnotationUtils.INITIATOR_CODON_VARIANT);
            }
            int finalNtPhase = (transcriptSequence.length() - cdnaCodingStart) % 3;
            if ((cdnaVariantStart >= (transcriptSequence.length() - finalNtPhase)) &&
                    (transcript.getStart() == transcript.getGenomicCodingStart()) && finalNtPhase != 2) {  //  Variant in the last codon of a transcript without stop codon. finalNtPhase==2 if the cds length is multiple of 3.
                SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);
            }
            if(variant.getAlternative().length()%3 == 0) {
                SoNames.add(VariantAnnotationUtils.INFRAME_INSERTION);
            } else {
                SoNames.add(VariantAnnotationUtils.FRAMESHIFT_VARIANT);
            }
            solveStopCodonNegativeInsertion(transcriptSequence, cdnaCodingStart, cdnaVariantEnd); // Be careful, cdnaVariantEnd is being used in this case!!!
        } else {
            SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
        }

    }

    private void solveStopCodonNegativeInsertion(String transcriptSequence, Integer cdnaCodingStart,
                                                 Integer cdnaVariantEnd) {
        Integer variantPhaseShift = (cdnaVariantEnd - cdnaCodingStart) % 3;
        int modifiedCodonStart = cdnaVariantEnd - variantPhaseShift;
        String reverseCodon = new StringBuilder(transcriptSequence.substring(transcriptSequence.length() - modifiedCodonStart - 2,
                transcriptSequence.length() - modifiedCodonStart + 1)).reverse().toString(); // Rigth limit of the substring sums +1 because substring does not include that position
        String reverseTranscriptSequence = new StringBuilder(transcriptSequence.substring(((transcriptSequence.length()-cdnaVariantEnd)>2)?(transcriptSequence.length()-cdnaVariantEnd-2):0,  // Be careful reaching the end of the transcript sequence
                transcriptSequence.length() - cdnaVariantEnd + 1)).reverse().toString(); // Rigth limit of the substring sums +1 because substring does not include that position
        char[] referenceCodonArray = reverseCodon.toCharArray();
        referenceCodonArray[0] = VariantAnnotationUtils.complementaryNt.get(referenceCodonArray[0]);
        referenceCodonArray[1] = VariantAnnotationUtils.complementaryNt.get(referenceCodonArray[1]);
        referenceCodonArray[2] = VariantAnnotationUtils.complementaryNt.get(referenceCodonArray[2]);
        char[] modifiedCodonArray = referenceCodonArray.clone();
        char[] altArray = (new StringBuilder(variant.getAlternative()).reverse().toString()).toCharArray();
        int i=0;
        int reverseTranscriptSequencePosition = 0;
        int modifiedCodonPosition;
        int modifiedCodonPositionStart=variantPhaseShift;
        do {
            for(modifiedCodonPosition=modifiedCodonPositionStart;
                (modifiedCodonPosition<3 && i<variant.getAlternative().length()); modifiedCodonPosition++) {  // Paste alternative nt in the corresponding codon position
                modifiedCodonArray[modifiedCodonPosition] = VariantAnnotationUtils.complementaryNt.get(altArray[i]);
                i++;
            }
            for(;modifiedCodonPosition<3;modifiedCodonPosition++) {  // Concatenate reference codon nts after alternative nts
                if(reverseTranscriptSequencePosition>=reverseTranscriptSequence.length()) {
                    int genomicCoordinate = transcript.getStart() - (reverseTranscriptSequencePosition - reverseTranscriptSequence.length() + 1);
                    modifiedCodonArray[modifiedCodonPosition] =
                            VariantAnnotationUtils.complementaryNt.get(((GenomeSequenceFeature) genomeDBAdaptor.getSequenceByRegion(variant.getChromosome(),
                            genomicCoordinate, genomicCoordinate + 1, new QueryOptions()).getResult().get(0)).getSequence().charAt(0));
                } else {
                    modifiedCodonArray[modifiedCodonPosition] =
                            VariantAnnotationUtils.complementaryNt.get(reverseTranscriptSequence.charAt(reverseTranscriptSequencePosition));
                    reverseTranscriptSequencePosition++;
                }
            }
            decideStopCodonModificationAnnotation(SoNames, String.valueOf(referenceCodonArray), modifiedCodonArray);
            modifiedCodonPositionStart = 0;  // Reset the position where the next modified codon must be started to be filled
        } while(i<variant.getAlternative().length());  // All posible new codons generated by the inserted sequence must be checked

    }

    private void solveTranscriptFlankingRegions(String leftRegionTag, String rightRegionTag) {
        // Variant within -5kb region
        if(variantEnd>(transcript.getStart()-5001) && variantStart<transcript.getStart()) {
            // Variant within -2kb region
            if(variantEnd>(transcript.getStart()-2001)) {
                SoNames.add("2KB_" + leftRegionTag);
            } else {
                SoNames.add(leftRegionTag);
            }
        }
        // Variant within +5kb region
        if(variantEnd>transcript.getEnd() && variantStart<(transcript.getEnd()+5001)) {
            // Variant within +2kb region
            if(variantStart<(transcript.getEnd()+2001)) {
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
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;

        if(transcript.getGenomicCodingStart()<=exon.getEnd()) {
            firstCdsPhase = exon.getPhase();
        }

        if(variantEnd > exon.getStart()) {
            if(variantStart <= exon.getEnd()) { // Variant start within the exon (this is a insertion, variantEnd=variantStart+1)
                cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                consequenceType.setcDnaPosition(cdnaVariantStart);
                if(variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||SE||||||||----
                    cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                } else{  // Only variant start within the exon  ---||||||||||||SE----
                    cdnaVariantEnd = cdnaVariantStart + 1;  // To account for those insertions in the last nucleotide of the exon
                }
            }
        } else if(variantEnd == exon.getStart()) {  // Only variant end within the exon  ----E|||||||||||||----
            // We do not contemplate that variant end can be located before this exon since this is the first exon
            cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
            cdnaVariantStart = cdnaVariantEnd - 1;  // To account for those insertions in the first nucleotide of the exon
        }

        int exonCounter = 1;
        while(exonCounter<transcript.getExons().size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            int prevSpliceSite = exon.getEnd()+1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            if(firstCdsPhase==-1 && transcript.getGenomicCodingStart()<=exon.getEnd()) {  // Set firsCdsPhase only when the first coding exon is reached
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(prevSpliceSite, exon.getStart()-1, VariantAnnotationUtils.SPLICE_DONOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT, junctionSolution);

            splicing = (splicing || junctionSolution[0]);

            if(variantEnd > exon.getStart()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if(variantStart <= exon.getEnd()) {  // Variant start within the exon
                    cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                    consequenceType.setcDnaPosition(cdnaVariantStart);
                    if(variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||SE||||||||----
                        cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                    } else{  // Only variant start within the exon  ---||||||||||||SE----
                        cdnaVariantEnd = cdnaVariantStart + 1;  // To account for those insertions in the 5' end of an intron
                    }
                }
            } else if(variantEnd == exon.getStart()) { // Only variant end within the exon  ---SE|||||||||||||----
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                cdnaVariantStart = cdnaVariantEnd - 1;  // To account for those insertions in the 3' end of an intron
            } else {  // Variant does not include this exon, variant is located before this exon
                variantAhead = false;
            }
            exonCounter++;
        }
        solveMiRNA(cdnaVariantStart, cdnaVariantEnd, junctionSolution[1]);
    }

    private void solveCodingPositiveTranscript() {

        Exon exon = transcript.getExons().get(0);
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;

        if(transcript.getGenomicCodingStart()<=exon.getEnd()) {
            firstCdsPhase = exon.getPhase();
        }

        if(variantEnd > exon.getStart()) {
            if(variantStart <= exon.getEnd()) { // Variant start within the exon (this is a insertion, variantEnd=variantStart+1)
                cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                consequenceType.setcDnaPosition(cdnaVariantStart);
                if(variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||SE||||||||----
                    cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                } else{  // Only variant start within the exon  ---||||||||||||SE----
                    cdnaVariantEnd = cdnaVariantStart + 1;  // To account for those insertions in the last nucleotide of the exon
                }
            }
        } else if(variantEnd == exon.getStart()) {  // Only variant end within the exon  ----E|||||||||||||----
                // We do not contemplate that variant end can be located before this exon since this is the first exon
                cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                cdnaVariantStart = cdnaVariantEnd - 1;  // To account for those insertions in the first nucleotide of the exon
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

            if(variantEnd > exon.getStart()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if(variantStart <= exon.getEnd()) {  // Variant start within the exon
                    cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                    consequenceType.setcDnaPosition(cdnaVariantStart);
                    if(variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||SE||||||||----
                        cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                    } else{  // Only variant start within the exon  ---||||||||||||SE----
                        cdnaVariantEnd = cdnaVariantStart + 1;  // To account for those insertions in the 5' end of an intron
                    }
                }
            } else if(variantEnd == exon.getStart()) { // Only variant end within the exon  ---SE|||||||||||||----
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                cdnaVariantStart = cdnaVariantEnd - 1;  // To account for those insertions in the 3' end of an intron
            } else {  // Variant does not include this exon, variant is located before this exon
                variantAhead = false;
            }
            exonCounter++;
        }
        // Is not intron variant (both ends fall within the same intron)
        if(!junctionSolution[1]) {
            solveExonVariantInPositiveTranscript(splicing, transcriptSequence, cdnaVariantStart, cdnaVariantEnd,
                    firstCdsPhase);
        }
    }

    private void solveExonVariantInPositiveTranscript(boolean splicing, String transcriptSequence,
                                                      int cdnaVariantStart, int cdnaVariantEnd, int firstCdsPhase) {
        if(variantStart<transcript.getGenomicCodingStart()) {
            if(transcript.getStart()<transcript.getGenomicCodingStart() || transcript.unconfirmedStart()) {// Check transcript has 3 UTR
                SoNames.add(VariantAnnotationUtils.FIVE_PRIME_UTR_VARIANT);
            }
        } else if(variantStart <= transcript.getGenomicCodingEnd()) {  // Variant start within coding region
            int cdnaCodingStart = transcript.getCdnaCodingStart();  // Need to define a local cdnaCodingStart because may modified in two lines below
            if(cdnaVariantStart!=-1) {  // cdnaVariantStart may be -1 if variantStart falls in an intron
                if(transcript.unconfirmedStart()) {
                    cdnaCodingStart -= ((3-firstCdsPhase)%3);
                }
                int cdsVariantStart = cdnaVariantStart - cdnaCodingStart + 1;
                consequenceType.setCdsPosition(cdsVariantStart);
                consequenceType.setAaPosition(((cdsVariantStart - 1)/3)+1);
            }
            if(variantEnd <= transcript.getGenomicCodingEnd()) {  // Variant end also within coding region
                solveCodingExonVariantInPositiveTranscript(transcriptSequence, cdnaCodingStart, cdnaVariantStart);
            } else if(transcript.getEnd()>transcript.getGenomicCodingEnd() || transcript.unconfirmedEnd()) {// Check transcript has 3 UTR)
                SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
            }
        } else if(transcript.getEnd()>transcript.getGenomicCodingEnd() || transcript.unconfirmedEnd()) {// Check transcript has 3 UTR)
            SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
        }
    }

    private void solveCodingExonVariantInPositiveTranscript(String transcriptSequence, int cdnaCodingStart,
                                                            int cdnaVariantStart) {
        if(cdnaVariantStart != -1) {  // Insertion. Be careful: insertion coordinates are special, alternative nts are pasted between cdnaVariantStart and cdnaVariantEnd
            if(cdnaVariantStart<(cdnaCodingStart+2) && transcript.unconfirmedStart()) {  // cdnaVariantStart=null if variant is intronic. cdnaCodingStart<1 if cds_start_NF and phase!=0
                SoNames.add(VariantAnnotationUtils.INITIATOR_CODON_VARIANT);
            }
            int finalNtPhase = (transcriptSequence.length() - cdnaCodingStart) % 3;
            if ((cdnaVariantStart >= (transcriptSequence.length() - finalNtPhase)) &&
                    (transcript.getEnd() == transcript.getGenomicCodingEnd()) && finalNtPhase != 2) {  //  Variant in the last codon of a transcript without stop codon. finalNtPhase==2 if the cds length is multiple of 3.
                SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);
            }
            if(variant.getAlternative().length()%3 == 0) {
                SoNames.add(VariantAnnotationUtils.INFRAME_INSERTION);
            } else {
                SoNames.add(VariantAnnotationUtils.FRAMESHIFT_VARIANT);
            }
            solveStopCodonPositiveInsertion(transcriptSequence, cdnaCodingStart, cdnaVariantStart);
        } else {
            SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
        }
    }

    private void solveStopCodonPositiveInsertion(String transcriptSequence, Integer cdnaCodingStart,
                                                 Integer cdnaVariantStart) {
        Integer variantPhaseShift = (cdnaVariantStart + 1 - cdnaCodingStart) % 3; // Sum 1 to cdnaVariantStart because of the peculiarities of insertion coordinates: cdnaVariantStart coincides with the vcf position, the actual substituted nt is the one on the right
        int modifiedCodonStart = cdnaVariantStart + 1 - variantPhaseShift;
        String referenceCodon = transcriptSequence.substring(modifiedCodonStart - 1, modifiedCodonStart + 2);  // -1 and +2 because of base 0 String indexing
        char[] modifiedCodonArray = referenceCodon.toCharArray();
        int i=0;
        int transcriptSequencePosition = cdnaVariantStart;  // indexing over transcriptSequence is 0 based, transcriptSequencePosition points to cdnaVariantEnd actually
        int modifiedCodonPosition;
        int modifiedCodonPositionStart = variantPhaseShift;
        do {
            for (modifiedCodonPosition = modifiedCodonPositionStart;
                 (modifiedCodonPosition < 3 && i < variant.getAlternative().length()); modifiedCodonPosition++) {  // Paste alternative nt in the corresponding codon position
                modifiedCodonArray[modifiedCodonPosition] = variant.getAlternative().toCharArray()[i];
                i++;
            }
            for (; modifiedCodonPosition < 3; modifiedCodonPosition++) {  // Concatenate reference codon nts after alternative nts
                if(transcriptSequencePosition>=transcriptSequence.length()) {
                    int genomicCoordinate = transcript.getEnd() +
                            (transcriptSequencePosition - transcriptSequence.length()) + 1;
                    modifiedCodonArray[modifiedCodonPosition] =
                            ((GenomeSequenceFeature) genomeDBAdaptor.getSequenceByRegion(variant.getChromosome(),
                            genomicCoordinate, genomicCoordinate+1, new QueryOptions()).getResult().get(0)).getSequence().charAt(0);
                } else {
                    modifiedCodonArray[modifiedCodonPosition] = transcriptSequence.charAt(transcriptSequencePosition);
                }
                transcriptSequencePosition++;
            }
            decideStopCodonModificationAnnotation(SoNames, referenceCodon, modifiedCodonArray);
            modifiedCodonPositionStart = 0;  // Reset the position where the next modified codon must be started to be filled
        } while(i<variant.getAlternative().length());  // All posible new codons generated by the inserted sequence must be checked
    }

    private void solveJunction(Integer spliceSite1, Integer spliceSite2, String leftSpliceSiteTag,
                           String rightSpliceSiteTag, boolean[] junctionSolution) {
        junctionSolution[0] = false;  // Is splicing variant in non-coding region
        junctionSolution[1] = false;  // Variant is intronic and both ends fall within the intron

        if(regionsOverlap(spliceSite1+2, spliceSite2-2, variantStart, variantEnd)) {  // Variant overlaps the rest of intronic region (splice region within the intron and/or rest of intron)
            SoNames.add(VariantAnnotationUtils.INTRON_VARIANT);
        }
        if(variantStart>=spliceSite1 && variantEnd<=spliceSite2) {
            junctionSolution[1] = true;  // variant start & end fall within the intron
        }

        if(regionsOverlap(spliceSite1, spliceSite1 + 1, variantStart, variantEnd)) {  // Variant donor/acceptor
            if(variantEnd == spliceSite1) {  // Insertion between last nt of the exon (3' end), first nt of the intron (5' end)
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);  // Inserted nts considered part of the coding sequence
            } else if(variantEnd == (spliceSite1+2)) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);  // Inserted nts considered out of the donor/acceptor region
                junctionSolution[0] = (spliceSite2>variantStart);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            } else {
                SoNames.add(leftSpliceSiteTag);  // donor/acceptor depending on transcript strand
                junctionSolution[0] = (spliceSite2>variantStart);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            }
        } else if(regionsOverlap(spliceSite1+2, spliceSite1+7, variantStart, variantEnd)) {
            if(!(variantStart==(spliceSite1+7))) {  // Insertion coordinates are passed to this function as (variantStart-1,variantStart)
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
            }
            junctionSolution[0] = (variantStart<=spliceSite2 || variantEnd<=spliceSite2);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
        } else if(regionsOverlap(spliceSite1-3, spliceSite1-1, variantStart, variantEnd) && !(variantEnd==(spliceSite1-3))) {  // Insertion coordinates are passed to this function as (variantStart-1,variantStart)
            SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
        }

        if(regionsOverlap(spliceSite2-1, spliceSite2, variantStart, variantEnd)) {  // Variant donor/acceptor
            if(variantStart == spliceSite2) {  // Insertion between last nt of the intron (3' end), first nt of the exon (5' end)
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);  // Inserted nts considered part of the coding sequence
            } else if(variantStart == (spliceSite2-2)) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);  // Inserted nts considered out of the donor/acceptor region
                junctionSolution[0] = (spliceSite1<variantEnd);  //  BE CAREFUL: there are introns shorter than 14nts, and even just 1nt long!! (22:36587846)
            } else {
                SoNames.add(rightSpliceSiteTag);  // donor/acceptor depending on transcript strand
                junctionSolution[0] = (spliceSite1<variantEnd);  //  BE CAREFUL: there are introns shorter than 14nts, and even just 1nt long!! (22:36587846)
            }
        } else if(regionsOverlap(spliceSite2-7, spliceSite2-2, variantStart, variantEnd)) {
            if(!(variantEnd==(spliceSite2-7))) {  // Insertion coordinates are passed to this function as (variantStart-1,variantStart) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
            }
            junctionSolution[0] = (spliceSite1<=variantStart || spliceSite1<=variantEnd);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
        } else if(regionsOverlap(spliceSite2+1, spliceSite2+3, variantStart, variantEnd) && !((variantStart==(spliceSite2+3)))) {  // Insertion coordinates are passed to this function as (variantStart-1,variantStart) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
        }

    }

}
