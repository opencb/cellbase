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

package org.opencb.cellbase.core.api.key;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.SpliceScore;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.EvidenceEntry;
import org.opencb.cellbase.core.result.CellBaseDataResult;

import java.util.*;

public final class ApiKeyLicensedDataUtils {

    public static final int NUM_SPLICE_SCORE_SOURCES = 2;
    public static final Set<String> LICENSED_SPLICE_SCORES_DATA = new HashSet<>(Collections.singletonList("spliceai"));
    public static final Set<String> UNLICENSED_SPLICE_SCORES_DATA = new HashSet<>(Collections.singletonList("mmsplice"));

    public static final int NUM_CLINICAL_SOURCES = 3;
    public static final Set<String> LICENSED_CLINICAL_DATA = new HashSet<>(Arrays.asList("cosmic", "hgmd"));
    public static final Set<String> UNLICENSED_CLINICAL_DATA = new HashSet<>(Collections.singletonList("clinvar"));

    private ApiKeyLicensedDataUtils() {
    }

    public static boolean needFiltering(Set<String> inputSources, Set<String> licensedSources) {
        for (String licensedSource : licensedSources) {
            if (!inputSources.contains(licensedSource)) {
                return true;
            }
        }
        return false;
    }

    public static <T> CellBaseDataResult<T> filterDataSources(CellBaseDataResult<T> results, Set<String> validSources) {
        List<T> list = new ArrayList<>();
        for (T result : results.getResults()) {
            T filtered = null;
            if (result instanceof Variant) {
                filtered = (T) filterDataSources((Variant) result, validSources);
            } else if (result instanceof SpliceScore) {
                filtered = (T) filterDataSources((SpliceScore) result, validSources);
            }
            if (filtered != null) {
                list.add(filtered);
            }
        }
        results.setResults(list);
        results.setNumResults(list.size());
        results.setNumMatches(list.size());

        return results;
    }

    public static Variant filterDataSources(Variant variant, Set<String> validSources) {
        if (variant.getAnnotation() != null && CollectionUtils.isNotEmpty(variant.getAnnotation().getTraitAssociation())) {
            // Filtering clinical data sources
            List<EvidenceEntry> filteredTraits = new ArrayList<>();
            for (EvidenceEntry trait : variant.getAnnotation().getTraitAssociation()) {
                if (validSources.contains(trait.getSource().getName().toLowerCase())) {
                    filteredTraits.add(trait);
                }
            }
            variant.getAnnotation().setTraitAssociation(filteredTraits);
        }
        return variant;
    }

    public static List<CellBaseDataResult<Variant>> filterDataSources(List<CellBaseDataResult<Variant>> results,
                                                                      Set<String> validTokenSources) {
        List<CellBaseDataResult<Variant>> output = new ArrayList<>();
        for (CellBaseDataResult<Variant> result : results) {
            output.add(filterDataSources(result, validTokenSources));
        }
        return output;
    }

    private static SpliceScore filterDataSources(SpliceScore spliceScore, Set<String> validSources) {
        // Filtering clinical data sources
        if (spliceScore != null && StringUtils.isNotEmpty(spliceScore.getSource())
                && validSources.contains(spliceScore.getSource().toLowerCase())) {
            return spliceScore;
        }
        return null;
    }
}
