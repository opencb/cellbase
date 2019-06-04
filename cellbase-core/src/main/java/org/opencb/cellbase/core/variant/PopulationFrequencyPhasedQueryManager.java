package org.opencb.cellbase.core.variant;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;

import java.util.Collections;
import java.util.List;

public class PopulationFrequencyPhasedQueryManager extends PhasedQueryManager<PopulationFrequency> {
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
//            }
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
    protected List<Variant> getHaplotype(PopulationFrequency populationFrequency) {
        try {
            return Variant.parseVariants(populationFrequency.getAltAllele());

        // Allele string cannot be parsed into multiple variant objects, it's probably just a single nucleotide
        // sequence
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<PopulationFrequency> getAnnotationObjectList(Variant variant) {
        return variant.getAnnotation().getPopulationFrequencies();
    }
}
