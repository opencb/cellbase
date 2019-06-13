package org.opencb.cellbase.core.variant;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;
import org.opencb.biodata.tools.variant.VariantNormalizer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PopulationFrequencyPhasedQueryManager extends AnnotationBasedPhasedQueryManager<PopulationFrequency> {
    private static final char SHIFTED_POSITION_CHARACTER = '-';
//    @Override
//    public List<QueryResult<Variant>> run(List<Variant> variantList, List<QueryResult<Variant>> variantQueryResultList) {
//        // Go through all input variants and their corresponding query results
//        for (int j = 0; j < variantQueryResultList.size(); j++) {
//            QueryResult<Variant> variantQueryResult = variantQueryResultList.get(j);
//            if (!variantQueryResult.getResult().isEmpty()) {
//                // Only one variant record should be present per assembly.
//                if (variantQueryResult.getResult().size() > 1) {
//                    throw new RuntimeException("Unexpected: more than one result found for variant "
//                            + "collection for variant " + variantQueryResult.getId() + ". Please, check.");
//                }
//
//                Variant matchedVariant = variantQueryResult.getResult().get(0);
//                Variant queryVariant = variantList.get(j);
//                // Phase is stored at the evidence entry level, e.g.: there might be two ClinVar RCVs for one
//                // variant:
//                //   - In the first the variant is submitted as part of an MNV and therefore it is phased
//                //   - In the second one the variant is submitted singleton and therefore it is not phased
//                // both RCVs will be integrated in the same Variant object after decomposition as separate EvidenceEntry
//                // objects, each with its corresponding phase information
//                List<Variant> databaseHaplotype = getHaplotype(matchedVariant);
//                // Haplotype empty if variant is not phased
//                if (!databaseHaplotype.isEmpty()) {
//                    // Sample   Cellbase-Database  Match
//                    // -------------------------------
//                    // SNV      MNV                X
//                    // MNV      MNV                ✓
//                    // Missing genotypes in the input list will be considered as wildcards towards finding a
//                    // matching haplotype (MNV) in the input list, since otherwise the clinical variant would not be
//                    // returned
//                    if (!sameHaplotype(queryVariant, variantList, databaseHaplotype)) {
//                        reset(variantQueryResult);
//                    }
//                    // Sample   Cellbase-Database  Match
//                    // -------------------------------
//                    // SNV      SNV                ✓
//                    // MNV      SNV                ✓
//                }
//            }M
//        }
//
//        return variantQueryResultList;
//
//    }

//    private List<Variant> getHaplotype(Variant variant) {
//        String phaseSet = getSampleAttribute(variant, PHASE_SET_TAG);
//        if (StringUtils.isNotBlank(phaseSet)) {
//            return Variant.parseVariants(phaseSet);
//        }
//
//        return Collections.emptyList();
//    }

    @Override
    protected List<Variant> getHaplotype(PopulationFrequency populationFrequency, Variant variant) {
        // Assuming only simple variants make it to this point
        // Simple variants - there's no complex haplotype, i.e. there's no set of phased variants.
        // Also detects whether decomposition took place for MNVs, as decomposed MNV allele strings must have at least
        // one lowercase character or one "-" indicating alignment shifting
        if ((populationFrequency.getRefAllele().length() == 1 && populationFrequency.getAltAllele().length() == 1)
                || StringUtils.isEmpty(populationFrequency.getRefAllele())
                || StringUtils.isEmpty(populationFrequency.getAltAllele())
                || !wasDecomposed(populationFrequency)) {
            return Collections.emptyList();
        }

        return alignmentToVariantList(populationFrequency.getRefAllele(),
                populationFrequency.getAltAllele(),
                variant);

    }

    private boolean wasDecomposed(PopulationFrequency populationFrequency) {
        for (char nucleotide : populationFrequency.getRefAllele().toCharArray()) {
            if (SHIFTED_POSITION_CHARACTER == nucleotide || Character.isLowerCase(nucleotide)) {
                return true;
            }
        }

        return false;
    }

    private List<Variant> alignmentToVariantList(String refAllele, String altAllele, Variant matchedVariant) {

        // Need to calculate the start coordinate of the alignment - matchedVariant might not be the first simple
        // variant that forms the MNV
        List<VariantNormalizer.VariantKeyFields> keyFieldsList
                = VariantNormalizer.decomposeAlignmentSingleVariants(refAllele.toUpperCase(),
                altAllele.toUpperCase(),
                getMNVStart(refAllele, altAllele, matchedVariant), null);

        return keyFieldsList.stream().map((keyFields) -> (new Variant(matchedVariant.getChromosome(),
                keyFields.getStart(),
                keyFields.getEnd(),
                keyFields.getReference(),
                keyFields.getAlternate()))).collect(Collectors.toList());

    }

    private int getMNVStart(String refAllele, String altAllele, Variant matchedVariant) {
        int uppercasePosition = getFirstUppercase(refAllele);

        if (uppercasePosition != -1) {
            // Remove the relative position within the MNV
            return matchedVariant.getStart() - uppercasePosition;
        }

        uppercasePosition = getFirstUppercase(altAllele);

        if (uppercasePosition != -1) {
            // Remove the relative position within the MNV
            return matchedVariant.getStart() - uppercasePosition;
        }

        throw new IllegalArgumentException("Malformed MNV alignment " + refAllele + "/" + altAllele + ". At least one"
                + " uppercase character is expected in any of the strings to be able to re-construct genomic "
                + " coordinates of the original MNV");

    }

    private int getFirstUppercase(String alleleString) {
        int i = 0;
        for (char nucleotide : alleleString.toCharArray()) {
            if (SHIFTED_POSITION_CHARACTER != nucleotide && Character.isUpperCase(nucleotide)) {
                return i;
            }
            i++;
        }

        return -1;
    }

    @Override
    protected List<PopulationFrequency> getAnnotationObjectList(Variant variant) {
        return variant.getAnnotation().getPopulationFrequencies();
    }
}
