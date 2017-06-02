package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.List;

//import org.opencb.cellbase.core.db.api.core.GenomeDBAdaptor;

/**
 * Created by fjlopez on 05/08/15.
 */
public class ConsequenceTypeDeletionCalculator extends ConsequenceTypeGenericRegionCalculator {
    private boolean isBigDeletion;
//    private GenomeDBAdaptor genomeDBAdaptor;

    public ConsequenceTypeDeletionCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        super();
        this.genomeDBAdaptor = genomeDBAdaptor;
    }

    @Override
    public List<ConsequenceType> run(Variant inputVariant, List<Gene> geneList, List<RegulatoryFeature> regulatoryRegionList) {
        List<ConsequenceType> consequenceTypeList = new ArrayList<>();
        variant = inputVariant;
        variantEnd = variant.getEnd();
//        variantEnd = variant.getStart() + variant.getReference().length() - 1;
        variantStart = variant.getStart();
        isBigDeletion = ((variantEnd - variantStart) > BIG_VARIANT_SIZE_THRESHOLD);
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
        solveRegulatoryRegions(regulatoryRegionList, consequenceTypeList);
        return consequenceTypeList;
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
//                if (variant.getReference().length() % 3 == 0) {
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
                            get(genomeDBAdaptor.getGenomicSequence(query, new QueryOptions()).getResult().get(0).getSequence().charAt(0));
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
            boolean useMitochondrialCode = variant.getChromosome().equals("MT");
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
//                if (variant.getReference().length() % 3 == 0) {
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
                    modifiedCodonArray[codonPosition] = genomeDBAdaptor
                            .getGenomicSequence(query, new QueryOptions()).getResult().get(0).getSequence().charAt(0);
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
            boolean useMitochondrialCode = variant.getChromosome().equals("MT");
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


