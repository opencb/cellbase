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

package org.opencb.cellbase.lib.managers;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.queries.ClinicalVariantQuery;
import org.opencb.cellbase.core.common.clinical.ClinicalVariant;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.ClinicalMongoDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClinicalManager extends AbstractManager implements AggregationApi<ClinicalVariantQuery, ClinicalVariant> {

    private ClinicalMongoDBAdaptor clinicalDBAdaptor;

    public ClinicalManager(String species, String assembly, CellBaseConfiguration configuration)
            throws CellbaseException {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() throws CellbaseException {
        clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(species, assembly);
    }

    @Override
    public CellBaseCoreDBAdaptor getDBAdaptor() {
        return clinicalDBAdaptor;
    }

    public CellBaseDataResult<Variant> search(Query query, QueryOptions queryOptions) {
        return clinicalDBAdaptor.nativeGet(query, queryOptions);
    }

//    public List<CellBaseDataResult> getPhenotypeGeneRelations(Query query, QueryOptions queryOptions) {
//        Set<String> sourceContent = query.getAsStringList(ClinicalDBAdaptor.QueryParams.SOURCE.key()) != null
//                ? new HashSet<>(query.getAsStringList(ClinicalDBAdaptor.QueryParams.SOURCE.key())) : null;
//        List<CellBaseDataResult> cellBaseDataResultList = new ArrayList<>();
//        if (sourceContent == null || sourceContent.contains("clinvar")) {
//            cellBaseDataResultList.add(getClinvarPhenotypeGeneRelations(queryOptions));
//
//        }
//        if (sourceContent == null || sourceContent.contains("gwas")) {
//            cellBaseDataResultList.add(getGwasPhenotypeGeneRelations(queryOptions));
//        }
//
//        return cellBaseDataResultList;
//    }

    public CellBaseDataResult<String> getAlleleOriginLabels() {
        List<String> alleleOriginLabels = Arrays.stream(AlleleOrigin.values())
                .map((value) -> value.name()).collect(Collectors.toList());
        return new CellBaseDataResult<String>("allele_origin_labels", 0, Collections.emptyList(),
                alleleOriginLabels.size(), alleleOriginLabels, alleleOriginLabels.size());
    }

    public CellBaseDataResult<String> getModeInheritanceLabels() {
        List<String> modeInheritanceLabels = Arrays.stream(ModeOfInheritance.values())
                .map((value) -> value.name()).collect(Collectors.toList());
        return new CellBaseDataResult<String>("mode_inheritance_labels", 0, Collections.emptyList(),
                modeInheritanceLabels.size(), modeInheritanceLabels, modeInheritanceLabels.size());
    }

    public CellBaseDataResult<String> getClinsigLabels() {
        List<String> clinsigLabels = Arrays.stream(ClinicalSignificance.values())
                .map((value) -> value.name()).collect(Collectors.toList());
        return new CellBaseDataResult<String>("clinsig_labels", 0, Collections.emptyList(),
                clinsigLabels.size(), clinsigLabels, clinsigLabels.size());
    }

    public CellBaseDataResult<String> getConsistencyLabels() {
        List<String> consistencyLabels = Arrays.stream(ConsistencyStatus.values())
                .map((value) -> value.name()).collect(Collectors.toList());
        return  new CellBaseDataResult<String>("consistency_labels", 0, Collections.emptyList(),
                consistencyLabels.size(), consistencyLabels, consistencyLabels.size());
    }

    public CellBaseDataResult<String> getVariantTypes() {
        List<String> variantTypes = Arrays.stream(VariantType.values())
                .map((value) -> value.name()).collect(Collectors.toList());
        return new CellBaseDataResult<String>("variant_types", 0, Collections.emptyList(),
                variantTypes.size(), variantTypes, variantTypes.size());
    }

    public List<CellBaseDataResult<Variant>> getByVariant(List<Variant> variants, List<Gene> geneList,
                                                          QueryOptions queryOptions) {
        return clinicalDBAdaptor.getByVariant(variants, geneList, queryOptions);
    }
}
