package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.MiRNAGene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.annotation.ConsequenceTypeMappings;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.ProteinVariantAnnotation;
import org.opencb.biodata.models.variant.avro.SequenceOntologyTerm;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by fjlopez on 19/06/15.
 */
public abstract class ConsequenceTypeCalculator {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected HashSet<String> SoNames = new HashSet<>();
    protected ConsequenceType consequenceType;
    protected Gene gene;
    protected Transcript transcript;
    protected Variant variant;
    protected GenomeDBAdaptor genomeDBAdaptor;
    protected Boolean imprecise = true;
    protected Integer svExtraPadding = 0;
    protected Integer cnvExtraPadding = 0;

    protected static final String IMPRECISE = "imprecise";
    protected static final String SV_EXTRA_PADDING = "svExtraPadding";
    protected static final String CNV_EXTRA_PADDING = "cnvExtraPadding";

    public abstract List<ConsequenceType> run(Variant variant, List<Gene> geneList,
                                              boolean[] overlapsRegulatoryRegion, QueryOptions queryOptions);

    protected void parseQueryParam(QueryOptions queryOptions) {
        imprecise = (Boolean) queryOptions.get(IMPRECISE) != null ? (Boolean) queryOptions.get(IMPRECISE) : true;
        svExtraPadding = (Integer) queryOptions.get(SV_EXTRA_PADDING) != null
                ? (Integer) queryOptions.get(SV_EXTRA_PADDING) : 0;
        cnvExtraPadding = (Integer) queryOptions.get(CNV_EXTRA_PADDING) != null
                ? (Integer) queryOptions.get(CNV_EXTRA_PADDING) : 0;
    }

    protected void solvePositiveTranscript(List<ConsequenceType> consequenceTypeList) {
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
                consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                consequenceTypeList.add(consequenceType);
                break;
            /**
             * Non-coding biotypes
             */
            default:
                solveNonCodingPositiveTranscript();
                consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                consequenceTypeList.add(consequenceType);
                break;
        }
    }

    protected void solveNegativeTranscript(List<ConsequenceType> consequenceTypeList) {
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
                consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                consequenceTypeList.add(consequenceType);
                break;
            /**
             * Non-coding biotypes
             */
            default:
                solveNonCodingNegativeTranscript();
                consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                consequenceTypeList.add(consequenceType);
                break;
        }
    }

    protected abstract void solveNonCodingNegativeTranscript();

    protected abstract void solveCodingNegativeTranscript();

    protected abstract void solveNonCodingPositiveTranscript();

    protected abstract void solveCodingPositiveTranscript();

    protected int setCdsAndProteinPosition(int cdnaVariantPosition, int firstCdsPhase, int cdnaCodingStart) {
        if (cdnaVariantPosition != -1) {  // cdnaVariantStart may be null if variantEnd falls in an intron
            if (transcript.unconfirmedStart()) {
                cdnaCodingStart -= ((3 - firstCdsPhase) % 3);
            }
            int cdsVariantStart = cdnaVariantPosition - cdnaCodingStart + 1;
            consequenceType.setCdsPosition(cdsVariantStart);
            // First place where protein variant annotation is added to the Consequence type,
            // must create the ProteinVariantAnnotation object
            ProteinVariantAnnotation proteinVariantAnnotation = new ProteinVariantAnnotation();
            proteinVariantAnnotation.setPosition(((cdsVariantStart - 1) / 3) + 1);
            consequenceType.setProteinVariantAnnotation(proteinVariantAnnotation);
        }
        return cdnaCodingStart;
    }

    protected Boolean regionsOverlap(Integer region1Start, Integer region1End, Integer region2Start, Integer region2End) {
        return (region2Start <= region1End && region2End >= region1Start);
    }

    protected void solveIntergenic(List<ConsequenceType> consequenceTypeList, boolean isIntergenic) {
        if (consequenceTypeList.size() == 0 && isIntergenic) {
            HashSet<String> intergenicName = new HashSet<>();
            intergenicName.add(VariantAnnotationUtils.INTERGENIC_VARIANT);
            ConsequenceType consequenceType = new ConsequenceType();
            consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(intergenicName));
            consequenceTypeList.add(consequenceType);
        }
    }

    protected void solveRegulatoryRegions(boolean[] overlapsRegulatoryRegion, List<ConsequenceType> consequenceTypeList) {
        if (overlapsRegulatoryRegion[0]) {
            ConsequenceType consequenceType = new ConsequenceType();
            SequenceOntologyTerm sequenceOntologyTerm = newSequenceOntologyTerm(VariantAnnotationUtils.REGULATORY_REGION_VARIANT);
            consequenceType.setSequenceOntologyTerms(Collections.singletonList(sequenceOntologyTerm));
            consequenceTypeList.add(consequenceType);
            if (overlapsRegulatoryRegion[1]) {
                consequenceType = new ConsequenceType();
                sequenceOntologyTerm = newSequenceOntologyTerm(VariantAnnotationUtils.TF_BINDING_SITE_VARIANT);
                consequenceType.setSequenceOntologyTerms(Collections.singletonList(sequenceOntologyTerm));
                consequenceTypeList.add(consequenceType);
            }
        }



//        if (regulatoryRegionList != null && !regulatoryRegionList.isEmpty()) {
//            ConsequenceType consequenceType = new ConsequenceType();
//            SequenceOntologyTerm sequenceOntologyTerm = newSequenceOntologyTerm(VariantAnnotationUtils.REGULATORY_REGION_VARIANT);
//            consequenceType.setSequenceOntologyTerms(Collections.singletonList(sequenceOntologyTerm));
//            consequenceTypeList.add(consequenceType);
//            logger.debug("Checking {} regulatory regions for variant: {}:{}-{}:{}", regulatoryRegionList.size(),
//                    variant.getChromosome(), variant.getStart(), variant.getEnd(), variant.getAlternate());
//            boolean tfbsFound = false;
//            for (int i = 0; (i < regulatoryRegionList.size() && !tfbsFound); i++) {
//                String regulatoryRegionType = regulatoryRegionList.get(i).getFeatureType();
//                tfbsFound = regulatoryRegionType != null && (regulatoryRegionType.equals(RegulationDBAdaptor.FeatureType.TF_binding_site)
//                        || regulatoryRegionList.get(i).getFeatureType().equals(RegulationDBAdaptor.FeatureType.TF_binding_site_motif));
//            }
//            if (tfbsFound) {
//                consequenceType = new ConsequenceType();
//                sequenceOntologyTerm = newSequenceOntologyTerm(VariantAnnotationUtils.TF_BINDING_SITE_VARIANT);
//                consequenceType.setSequenceOntologyTerms(Collections.singletonList(sequenceOntologyTerm));
//                consequenceTypeList.add(consequenceType);
//            }
//        }
    }

    protected void decideStopCodonModificationAnnotation(Set<String> soNames, String referenceCodon,
                                                         String modifiedCodon, boolean useMitochondrialCode) {

        Map<String, Boolean> replacementMap;
        if (useMitochondrialCode) {
            replacementMap = VariantAnnotationUtils.MT_IS_SYNONYMOUS_CODON.get(referenceCodon);
        } else {
            replacementMap = VariantAnnotationUtils.IS_SYNONYMOUS_CODON.get(referenceCodon);
        }
        if (replacementMap != null) {
            Boolean isSynonymous = replacementMap.get(modifiedCodon);
            if (isSynonymous != null) {
                if (isSynonymous) {
                    if (VariantAnnotationUtils.isStopCodon(useMitochondrialCode, referenceCodon)) {
                        soNames.add(VariantAnnotationUtils.STOP_RETAINED_VARIANT);
                    }
                } else {
                    if (VariantAnnotationUtils.isStopCodon(useMitochondrialCode, String.valueOf(referenceCodon))) {
                        soNames.add(VariantAnnotationUtils.STOP_LOST);
                    } else if (VariantAnnotationUtils.isStopCodon(useMitochondrialCode, modifiedCodon)) {
                        soNames.add(VariantAnnotationUtils.STOP_GAINED);
                    }
                }
            }
        }
    }

    protected void solveMiRNA(int cdnaVariantStart, int cdnaVariantEnd, boolean isIntronicVariant) {
        if (transcript.getBiotype().equals(VariantAnnotationUtils.MIRNA)) {  // miRNA with miRBase data
            if (gene.getMirna() != null) {
                if (cdnaVariantStart == -1) {  // Probably deletion starting before the miRNA location
                    cdnaVariantStart = 1;       // Truncate to the first transcript position to avoid null exception
                }
                if (cdnaVariantEnd == -1) {    // Probably deletion ending after the miRNA location
                    cdnaVariantEnd = gene.getMirna().getSequence().length();  // Truncate to the last transcript position to avoid NPE
                }
                List<MiRNAGene.MiRNAMature> miRNAMatureList = gene.getMirna().getMatures();
                int i = 0;
                while (i < miRNAMatureList.size() && !regionsOverlap(miRNAMatureList.get(i).cdnaStart,
                        miRNAMatureList.get(i).cdnaEnd, cdnaVariantStart, cdnaVariantEnd)) {
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
            } else {
                addNonCodingSOs(isIntronicVariant);
            }
        } else {
            addNonCodingSOs(isIntronicVariant);
        }
    }

    protected void addNonCodingSOs(boolean isIntronicVariant) {
        if (!isIntronicVariant) {  // Exon variant
            SoNames.add(VariantAnnotationUtils.NON_CODING_TRANSCRIPT_EXON_VARIANT);
        }
        SoNames.add(VariantAnnotationUtils.NON_CODING_TRANSCRIPT_VARIANT);
    }

    protected List<SequenceOntologyTerm> getSequenceOntologyTerms(HashSet<String> soNames) {
        List<SequenceOntologyTerm> sequenceOntologyTerms = new ArrayList<>(soNames.size());
        for (String name : soNames) {
            sequenceOntologyTerms.add(newSequenceOntologyTerm(name));
        }
        return sequenceOntologyTerms;
    }

    private SequenceOntologyTerm newSequenceOntologyTerm(String name) {
        return new SequenceOntologyTerm(ConsequenceTypeMappings.getSoAccessionString(name), name);
    }

    protected int updateNegativeInsertionCodonArrays(String reverseTranscriptSequence,
                                                     char[] formattedReferenceCodon1Array,
                                                     int reverseTranscriptSequencePosition, int modifiedCodonPosition,
                                                     char[] formattedModifiedCodonArray, char[] modifiedCodonArray) {
        for (; modifiedCodonPosition < 3; modifiedCodonPosition++) {  // Concatenate reference codon nts after alternative nts
            if (reverseTranscriptSequencePosition >= reverseTranscriptSequence.length()) {
                int genomicCoordinate = transcript.getStart()
                        - (reverseTranscriptSequencePosition - reverseTranscriptSequence.length() + 1);
                Query query = new Query(GenomeDBAdaptor.QueryParams.REGION.key(), variant.getChromosome()
                        + ":" + genomicCoordinate
                        + "-" + (genomicCoordinate + 1));
                modifiedCodonArray[modifiedCodonPosition] = VariantAnnotationUtils.COMPLEMENTARY_NT
                        .get(genomeDBAdaptor.getGenomicSequence(query, new QueryOptions())
                                .getResult().get(0).getSequence().charAt(0));
            } else {
                modifiedCodonArray[modifiedCodonPosition] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(
                        reverseTranscriptSequence.charAt(reverseTranscriptSequencePosition));
            }
            reverseTranscriptSequencePosition++;

            // Edit modified nt to make it upper-case in the formatted strings
            formattedReferenceCodon1Array[modifiedCodonPosition]
                    = Character.toUpperCase(formattedReferenceCodon1Array[modifiedCodonPosition]);
            formattedModifiedCodonArray[modifiedCodonPosition]
                    = Character.toUpperCase(modifiedCodonArray[modifiedCodonPosition]);
        }
        return reverseTranscriptSequencePosition;
    }

    protected int updatePositiveInsertionCodonArrays(String transcriptSequence, char[] modifiedCodonArray,
                                                     int transcriptSequencePosition, int modifiedCodonPosition,
                                                     char[] formattedReferenceCodonArray,
                                                     char[] formattedModifiedCodonArray) {
        for (; modifiedCodonPosition < 3; modifiedCodonPosition++) {  // Concatenate reference codon nts after alternative nts
            if (transcriptSequencePosition >= transcriptSequence.length()) {
                int genomicCoordinate = transcript.getEnd() + (transcriptSequencePosition - transcriptSequence.length()) + 1;
//                        modifiedCodonArray[modifiedCodonPosition] = ((GenomeSequenceFeature) genomeDBAdaptor.getSequenceByRegion(
//                                variant.getChromosome(), genomicCoordinate, genomicCoordinate + 1,
//                                new QueryOptions()).getResult().get(0)).getSequence().charAt(0);
                Query query = new Query(GenomeDBAdaptor.QueryParams.REGION.key(), variant.getChromosome()
                        + ":" + genomicCoordinate
                        + "-" + (genomicCoordinate + 1));
                modifiedCodonArray[modifiedCodonPosition] = genomeDBAdaptor.getGenomicSequence(query, new QueryOptions())
                        .getResult().get(0).getSequence().charAt(0);
            } else {
                modifiedCodonArray[modifiedCodonPosition] = transcriptSequence.charAt(transcriptSequencePosition);
            }
            transcriptSequencePosition++;

            // Edit modified nt to make it upper-case in the formatted strings
            formattedReferenceCodonArray[modifiedCodonPosition]
                    = Character.toUpperCase(formattedReferenceCodonArray[modifiedCodonPosition]);
            formattedModifiedCodonArray[modifiedCodonPosition]
                    = Character.toUpperCase(modifiedCodonArray[modifiedCodonPosition]);

        }
        return transcriptSequencePosition;
    }

    protected boolean setInsertionAlleleAminoacidChange(String referenceCodon, char[] modifiedCodonArray,
                                                        char[] formattedReferenceCodonArray,
                                                        char[] formattedModifiedCodonArray,
                                                        boolean useMitochondrialCode, boolean firstCodon) {
        // Set codon str, protein ref and protein alt ONLY for the first codon mofified by the insertion
        if (firstCodon) {
            firstCodon = false;
            // Only the exact codon where the deletion starts is set
            consequenceType.setCodon(String.valueOf(formattedReferenceCodonArray) + "/"
                    + String.valueOf(formattedModifiedCodonArray));
            // Assumes proteinVariantAnnotation attribute is already initialized
            consequenceType
                    .getProteinVariantAnnotation()
                    .setReference(VariantAnnotationUtils.getAminoacid(useMitochondrialCode, referenceCodon));
            consequenceType
                    .getProteinVariantAnnotation()
                    .setAlternate(VariantAnnotationUtils.getAminoacid(useMitochondrialCode,
                            String.valueOf(modifiedCodonArray)));
        }
        return firstCodon;
    }



}
