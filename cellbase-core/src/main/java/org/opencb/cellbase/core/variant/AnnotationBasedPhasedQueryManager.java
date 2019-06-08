package org.opencb.cellbase.core.variant;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;

public abstract class AnnotationBasedPhasedQueryManager<T> extends PhasedQueryManager {

    public List<QueryResult<Variant>> run(List<Variant> variantList, List<QueryResult<Variant>> variantQueryResultList) {
        // Go through all input variants and their corresponding query results
        for (int j = 0; j < variantQueryResultList.size(); j++) {
            QueryResult<Variant> variantQueryResult = variantQueryResultList.get(j);
            if (!variantQueryResult.getResult().isEmpty()) {
                // Variants are normalised and data from each of the sources (COSMIC, ClinVar, DOCM, etc.) integrated
                // during the build process. Only one variant record should be present per assembly.
                if (variantQueryResult.getResult().size() > 1) {
                    throw new RuntimeException("Unexpected: more than one result found in the clinical variant "
                            + "collection for variant " + variantQueryResult.getId() + ". Please, check.");
                }

                Variant matchedVariant = variantQueryResult.getResult().get(0);
                Variant queryVariant = variantList.get(j);
                List<T> annotationObjectList = getAnnotationObjectList(matchedVariant);
                // Phase is stored at the evidence entry/population frequency level, e.g.: there might be two ClinVar
                // RCVs for one variant:
                //   - In the first the variant is submitted as part of an MNV and therefore it is phased
                //   - In the second one the variant is submitted singleton and therefore it is not phased
                // both RCVs will be integrated in the same Variant object after decomposition as separate EvidenceEntry
                // objects, each with its corresponding phase information
                int i = 0;
                while (i < annotationObjectList.size()) {
                    T annotationObject = annotationObjectList.get(i);
                    List<Variant> databaseHaplotype = getHaplotype(annotationObject, matchedVariant);
                    // Haplotype empty if EvidenceEntry/PopulationFrequency is not phased
                    if (databaseHaplotype.isEmpty()) {
                        i++;
                    } else {
                        // Sample   Cellbase  Match
                        // -------------------------------
                        // SNV      MNV       X
                        // MNV      MNV       ✓
                        // Missing genotypes in the input list will be considered as wildcards towards finding a
                        // matching haplotype (MNV) in the input list, since otherwise the clinical variant would not be
                        // returned
                        if (sameHaplotype(queryVariant, variantList, databaseHaplotype)) {
                            i++;
                        } else {
                            annotationObjectList.remove(i);
                        }
                        // Sample   Cellbase  Match
                        // -------------------------------
                        // SNV      SNV       ✓
                        // MNV      SNV       ✓
                    }
                }

                // Remove whole variant from the query result object if ended up without any evidence entry
                if (annotationObjectList.isEmpty()) {
                    reset(variantQueryResult);
                }
            }
        }

        return variantQueryResultList;
    }

    protected abstract List<Variant> getHaplotype(T annotationObject, Variant variant);

    protected abstract List<T> getAnnotationObjectList(Variant variant);

}
