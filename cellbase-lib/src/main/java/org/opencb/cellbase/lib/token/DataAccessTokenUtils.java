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

package org.opencb.cellbase.lib.token;

import org.apache.commons.collections4.CollectionUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.EvidenceEntry;
import org.opencb.cellbase.core.result.CellBaseDataResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataAccessTokenUtils {

    public static boolean checkAllowedDataSources(List<String> includes, List<String> excludes, Set<String> tokenSources) {
        if (CollectionUtils.isEmpty(tokenSources)) {
            // cosmic and hgmd must be filtered
            return true;
        }

        // TODO: check includes/excludes to decide if data sources must be checked/filtered
//        if (CollectionUtils.isNotEmpty(includes)) {
//            // Take into account includes
//            if (includes.contains("annotation"))
//        } else if (CollectionUtils.isNotEmpty(excludes)) {
//            // Otherwise, exludes
//        }
        return true;
    }

    public static Set<String> getValidSources(Set<String> validTokenSources) {
        Set<String> validSources = new HashSet<>();

        // Licensed data sources
        //   - clinical: cosmic and hgmd
        validSources.add("clinvar");

        // Add valid data sources from token
        if (CollectionUtils.isNotEmpty(validTokenSources)) {
            validSources.addAll(validTokenSources);
        }
        return validSources;
    }

    public static Variant filterDataSources(Variant variant, Set<String> validSources) {
        if (variant.getAnnotation() != null && CollectionUtils.isNotEmpty(variant.getAnnotation().getTraitAssociation())) {
            List<EvidenceEntry> filteredTraits = new ArrayList<>();
            for (EvidenceEntry trait : variant.getAnnotation().getTraitAssociation()) {
                if (validSources.contains(trait.getSource().getName())) {
                    filteredTraits.add(trait);
                }
            }
            variant.getAnnotation().setTraitAssociation(filteredTraits);
        }
        return variant;
    }

    public static CellBaseDataResult<Variant> filterDataSources(CellBaseDataResult<Variant> results, Set<String> validSources) {
        List<Variant> variants = new ArrayList<>();
        for (Variant variant : results.getResults()) {
            variants.add(filterDataSources(variant, validSources));
        }
        results.setResults(variants);

        return results;
    }

    public static List<CellBaseDataResult<Variant>> filterDataSources(List<CellBaseDataResult<Variant>> results,
                                                                      Set<String> validTokenSources) {
        List<CellBaseDataResult<Variant>> output = new ArrayList<>();
        for (CellBaseDataResult<Variant> result : results) {
            output.add(filterDataSources(result, validTokenSources));
        }
        return output;
    }
}
