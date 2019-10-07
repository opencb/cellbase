/*
 * Copyright 2015-2019 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.core.variant;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.EvidenceEntry;
import org.opencb.biodata.models.variant.avro.Property;

import java.util.Collections;
import java.util.List;

public class ClinicalPhasedQueryManager extends AnnotationBasedPhasedQueryManager<EvidenceEntry> {

    private static final String HAPLOTYPE_PROPERTY = "haplotype";

//    /**
//     * Implements the following matching logic.
//     *
//     * Sample   Cellbase-ClinVar  Match
//     * -------------------------------
//     * SNV      SNV               ✓
//     * SNV      MNV               X
//     * MNV      SNV               ✓
//     * MNV      MNV               ✓
//     *
//     * @param variantList list of Variant objects used as an input for the ClinicalDBAdaptor query method
//     * @param variantQueryResultList list of QueryResults obtained as a result of the corresponding query within the
//     *                               ClinicalDBAdaptor
//     * @return the same variantQueryResultList instance edited as appropriate according to the phasing logic described
//     * above.
//     */
//    @Override
//    public List<QueryResult<Variant>> run(List<Variant> variantList, List<QueryResult<Variant>> variantQueryResultList) {
//        // Go through all input variants and their corresponding query results
//        for (int j = 0; j < variantQueryResultList.size(); j++) {
//            QueryResult<Variant> variantQueryResult = variantQueryResultList.get(j);
//            if (!variantQueryResult.getResult().isEmpty()) {
//                // Variants are normalised and data from each of the sources (COSMIC, ClinVar, DOCM, etc.) integrated
//                // during the build process. Only one variant record should be present per assembly.
//                if (variantQueryResult.getResult().size() > 1) {
//                    throw new RuntimeException("Unexpected: more than one result found in the clinical variant "
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
//                int i = 0;
//                while (i < matchedVariant.getAnnotation().getTraitAssociation().size()) {
//                    EvidenceEntry evidenceEntry = matchedVariant.getAnnotation().getTraitAssociation().get(i);
//                    List<Variant> clinicalHaplotype = getHaplotype(evidenceEntry);
//                    // Haplotype empty if EvidenceEntry is not phased
//                    if (clinicalHaplotype.isEmpty()) {
//                        i++;
//                    } else {
//                        // Sample   Cellbase-ClinVar  Match
//                        // -------------------------------
//                        // SNV      MNV               X
//                        // MNV      MNV               ✓
//                        // Missing genotypes in the input list will be considered as wildcards towards finding a
//                        // matching haplotype (MNV) in the input list, since otherwise the clinical variant would not be
//                        // returned
//                        if (sameHaplotype(queryVariant, variantList, clinicalHaplotype)) {
//                            i++;
//                        } else {
//                            matchedVariant.getAnnotation().getTraitAssociation().remove(i);
//                        }
//                        // Sample   Cellbase-ClinVar  Match
//                        // -------------------------------
//                        // SNV      SNV               ✓
//                        // MNV      SNV               ✓
//                    }
//                }
//
//                // Remove whole variant from the query result object if ended up without any evidence entry
//                if (matchedVariant.getAnnotation().getTraitAssociation().isEmpty()) {
//                    reset(variantQueryResult);
//                }
//            }
//        }
//
//        return variantQueryResultList;
//    }

    @Override
    protected List<Variant> getHaplotype(EvidenceEntry evidenceEntry, Variant variant) {
        List<Property> additionalProperties = evidenceEntry.getAdditionalProperties();
        if (additionalProperties != null && !additionalProperties.isEmpty()) {
            for (Property property : additionalProperties) {
                // Only one haplotype property expected; will select the first one and discard the rest anyway
                if (property.getName().equals(HAPLOTYPE_PROPERTY)) {
                    return Variant.parseVariants(property.getValue());
                }
            }

        }

        return Collections.emptyList();
    }

    @Override
    protected List<EvidenceEntry> getAnnotationObjectList(Variant variant) {
        return variant.getAnnotation().getTraitAssociation();
    }
}
