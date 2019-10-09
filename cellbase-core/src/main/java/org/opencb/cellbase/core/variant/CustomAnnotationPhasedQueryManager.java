/*
 * Copyright 2015-2020 OpenCB
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

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Collections;
import java.util.List;

public class CustomAnnotationPhasedQueryManager extends PhasedQueryManager {

    @Override
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
                List<Variant> databaseHaplotype = getHaplotype(matchedVariant);
                // Haplotype empty if EvidenceEntry/PopulationFrequency is not phased
                if (!databaseHaplotype.isEmpty()) {
                    // Sample   Cellbase  Match
                    // -------------------------------
                    // SNV      MNV       X
                    // MNV      MNV       ✓
                    // Missing genotypes in the input list will be considered as wildcards towards finding a
                    // matching haplotype (MNV) in the input list, since otherwise the clinical variant would not be
                    // returned
                    if (!sameHaplotype(queryVariant, variantList, databaseHaplotype)) {
                        reset(variantQueryResult);
                    }
                    // Sample   Cellbase  Match
                    // -------------------------------
                    // SNV      SNV       ✓
                    // MNV      SNV       ✓
                }
            }
        }

        return variantQueryResultList;
    }

    private List<Variant> getHaplotype(Variant variant) {
        String phaseSet = getSampleAttribute(variant, PHASE_SET_TAG);

        if (StringUtils.isNotBlank(phaseSet)) {
            return Variant.parseVariants(phaseSet);
        }

        return Collections.emptyList();
    }

}
