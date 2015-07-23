package org.opencb.cellbase.core.variant.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.MiRNAGene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.annotation.ConsequenceType;
import org.opencb.biodata.models.variant.annotation.ExpressionValue;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.regulatory.RegulatoryRegion;
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

    public ConsequenceTypeInsertionCalculator() {
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
                                SoNames.add("NMD_transcript_variant");
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
                                SoNames.add("NMD_transcript_variant");
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
            solveExonVariantInPositiveTranscript(splicing, transcriptSequence, cdnaVariantPosition, firstCdsPhase);
        }
    }

    private void solveExonVariantInPositiveTranscript(boolean splicing, String transcriptSequence,
                                                      int cdnaVariantPosition, int firstCdsPhase) {
        if(variantStart<genomicCodingStart) {
//        if(variantStart<genomicCodingStart || (variantRef.equals("-") && variantStart.equals(genomicCodingStart))) {
//            variantEnd -= variantRef.equals("-")?1:0;  // Insertion coordinates are peculiar: the actual inserted nts are assumed to be pasted on the left of variantStart, be careful with left edges
            if(transcriptStart<genomicCodingStart || (transcriptFlags!=null && transcriptFlags.contains("cds_start_NF"))) {// Check transcript has 3 UTR
                SoNames.add("5_prime_UTR_variant");
            }
            if((variantEnd >= genomicCodingStart) && !(variantRef.equals("-") && variantEnd.equals(genomicCodingStart))) {
                SoNames.add("coding_sequence_variant");
                if(transcriptFlags==null || cdnaCodingStart>0 || !transcriptFlags.contains("cds_start_NF")) {  // cdnaCodingStart<1 if cds_start_NF and phase!=0
                    SoNames.add("initiator_codon_variant");
                }
                if(variantEnd>(genomicCodingEnd-3)) {
                    SoNames.add("stop_lost");
                    if (variantEnd > genomicCodingEnd) {
                        if (transcriptEnd > genomicCodingEnd || (transcriptFlags != null && transcriptFlags.contains("cds_end_NF"))) {// Check transcript has 3 UTR)
                            SoNames.add("3_prime_UTR_variant");
                        }
                    }
                }
            }
//            variantEnd += variantRef.equals("-")?1:0;  // Recover original value of variantEnd for next transcripts
        } else {
            if(variantStart <= genomicCodingEnd) {  // Variant start within coding region
                if(cdnaVariantStart!=null) {  // cdnaVariantStart may be null if variantStart falls in an intron
                    if(transcriptFlags!=null && transcriptFlags.contains("cds_start_NF")) {
                        cdnaCodingStart -= ((3-firstCdsPhase)%3);
//                        cdnaCodingStart -= firstCdsPhase;
                    }
                    int cdsVariantStart = cdnaVariantStart - cdnaCodingStart + 1;
                    consequenceTypeTemplate.setCdsPosition(cdsVariantStart);
                    consequenceTypeTemplate.setAaPosition(((cdsVariantStart - 1)/3)+1);
                }
                if(variantEnd <= genomicCodingEnd) {  // Variant end also within coding region
                    solvePositiveCodingEffect(splicing, transcriptSequence, chromosome, transcriptEnd, genomicCodingEnd,
                            cdnaCodingStart, cdnaCodingEnd, cdnaVariantStart, cdnaVariantEnd, transcriptFlags,
                            variantRef, variantAlt, SoNames, consequenceTypeTemplate);
                } else {
                    if(transcriptEnd>genomicCodingEnd || (transcriptFlags!=null && transcriptFlags.contains("cds_end_NF"))) {// Check transcript has 3 UTR)
                        SoNames.add("3_prime_UTR_variant");
                    }
                    if(!variantRef.equals("-")) {  // If it is an insertion, it is located between the genomicCodingEnd and the next base, does not affect the stop codon and is not part of the coding sequence
                        SoNames.add("coding_sequence_variant");
                        SoNames.add("stop_lost");
                    }
                }
            } else {
                if(transcriptEnd>genomicCodingEnd || (transcriptFlags!=null && transcriptFlags.contains("cds_end_NF"))) {// Check transcript has 3 UTR)
                    SoNames.add("3_prime_UTR_variant");
                }
            }
        }
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
