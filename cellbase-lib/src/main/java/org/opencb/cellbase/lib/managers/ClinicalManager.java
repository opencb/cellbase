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
import org.opencb.cellbase.core.api.ClinicalVariantQuery;
import org.opencb.cellbase.core.api.query.CellBaseQueryOptions;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.lib.impl.core.ClinicalMongoDBAdaptor;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.lib.token.DataAccessTokenUtils;
import org.opencb.cellbase.lib.token.TokenFilteredVariantIterator;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.*;
import java.util.stream.Collectors;

import static org.opencb.cellbase.core.api.query.AbstractQuery.DATA_ACCESS_TOKEN;
import static org.opencb.commons.datastore.core.QueryOptions.EXCLUDE;
import static org.opencb.commons.datastore.core.QueryOptions.INCLUDE;

public class ClinicalManager extends AbstractManager implements AggregationApi<ClinicalVariantQuery, Variant> {

    private ClinicalMongoDBAdaptor clinicalDBAdaptor;

    public ClinicalManager(String species, CellBaseConfiguration configuration) throws CellBaseException {
        this(species, null, configuration);
    }

    public ClinicalManager(String species, String assembly, CellBaseConfiguration configuration) throws CellBaseException {
        super(species, assembly, configuration);

        this.init();
    }

    private void init() throws CellBaseException {
        clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(new GenomeManager(species, assembly, configuration));
    }

    @Override
    public CellBaseCoreDBAdaptor getDBAdaptor() {
        return clinicalDBAdaptor;
    }

    @Override
    public CellBaseDataResult<Variant> search(ClinicalVariantQuery query) throws QueryException, IllegalAccessException, CellBaseException {
        query.setDefaults();
        query.validate();
        CellBaseDataResult<Variant> results = getDBAdaptor().query(query);

        Set<String> validSources = tokenManager.getValidSources(query.getToken(), DataAccessTokenUtils.UNLICENSED_CLINICAL_DATA);

        // Check if is necessary to use the token licensed variant iterator
        if (DataAccessTokenUtils.needFiltering(validSources, DataAccessTokenUtils.LICENSED_CLINICAL_DATA)) {
            return DataAccessTokenUtils.filterDataSources(results, validSources);
        } else {
            return results;
        }
    }

    @Override
    public List<CellBaseDataResult<Variant>> search(List<ClinicalVariantQuery> queries) throws QueryException, IllegalAccessException,
            CellBaseException {
        List<CellBaseDataResult<Variant>> results = new ArrayList<>();
        for (ClinicalVariantQuery query : queries) {
            results.add(search(query));
        }
        return results;
    }

    @Override
    public List<CellBaseDataResult<Variant>> info(List<String> ids, CellBaseQueryOptions queryOptions, int dataRelease, String token)
            throws CellBaseException {
        List<CellBaseDataResult<Variant>> results = getDBAdaptor().info(ids, queryOptions, dataRelease, token);

        Set<String> validSources = tokenManager.getValidSources(token, DataAccessTokenUtils.UNLICENSED_CLINICAL_DATA);

        // Check if is necessary to use the token licensed variant iterator
        if (DataAccessTokenUtils.needFiltering(validSources, DataAccessTokenUtils.LICENSED_CLINICAL_DATA)) {
            return DataAccessTokenUtils.filterDataSources(results, validSources);
        } else {
            return results;
        }
    }

    @Override
    public CellBaseIterator<Variant> iterator(ClinicalVariantQuery query) throws CellBaseException {
        Set<String> validSources = tokenManager.getValidSources(query.getToken(), DataAccessTokenUtils.UNLICENSED_CLINICAL_DATA);

        // Check if is necessary to use the token licensed variant iterator
        if (DataAccessTokenUtils.needFiltering(validSources, DataAccessTokenUtils.LICENSED_CLINICAL_DATA)) {
            return new TokenFilteredVariantIterator(getDBAdaptor().iterator(query), validSources);
        } else {
            return getDBAdaptor().iterator(query);
        }
    }

    public CellBaseDataResult<Variant> search(Query query, QueryOptions queryOptions) throws CellBaseException {
        CellBaseDataResult<Variant> result = clinicalDBAdaptor.nativeGet(query, queryOptions);

        Set<String> validSources = tokenManager.getValidSources(queryOptions.getString(DATA_ACCESS_TOKEN),
                DataAccessTokenUtils.UNLICENSED_CLINICAL_DATA);


        List<String> includes = null;
        if (queryOptions.containsKey(INCLUDE)) {
            includes = Arrays.asList(queryOptions.getString(INCLUDE).split(","));
        }
        List<String> excludes = null;
        if (queryOptions.containsKey(EXCLUDE)) {
            includes = Arrays.asList(queryOptions.getString(EXCLUDE).split(","));
        }

        // Check if is necessary to use the token licensed variant iterator
        if (DataAccessTokenUtils.needFiltering(validSources, DataAccessTokenUtils.LICENSED_CLINICAL_DATA)) {
            return DataAccessTokenUtils.filterDataSources(result, validSources);
        } else {
            return result;
        }
    }

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
        return new CellBaseDataResult<String>("consistency_labels", 0, Collections.emptyList(),
                consistencyLabels.size(), consistencyLabels, consistencyLabels.size());
    }

    public CellBaseDataResult<String> getVariantTypes() {
        List<String> variantTypes = Arrays.stream(VariantType.values())
                .map((value) -> value.name()).collect(Collectors.toList());
        return new CellBaseDataResult<String>("variant_types", 0, Collections.emptyList(),
                variantTypes.size(), variantTypes, variantTypes.size());
    }

    public List<CellBaseDataResult<Variant>> getByVariant(List<Variant> variants, List<Gene> geneList,
                                                          QueryOptions queryOptions, int dataRelease) throws CellBaseException {
        List<CellBaseDataResult<Variant>> results = clinicalDBAdaptor.getByVariant(variants, geneList, queryOptions, dataRelease);

        Set<String> validSources = tokenManager.getValidSources(queryOptions.getString(DATA_ACCESS_TOKEN),
                DataAccessTokenUtils.UNLICENSED_CLINICAL_DATA);

        List<String> includes = null;
        if (queryOptions.containsKey(INCLUDE)) {
            includes = Arrays.asList(queryOptions.getString(INCLUDE).split(","));
        }
        List<String> excludes = null;
        if (queryOptions.containsKey(EXCLUDE)) {
            includes = Arrays.asList(queryOptions.getString(EXCLUDE).split(","));
        }

        // Check if is necessary to use the token licensed variant iterator
        if (DataAccessTokenUtils.needFiltering(validSources, DataAccessTokenUtils.LICENSED_CLINICAL_DATA)) {
            return DataAccessTokenUtils.filterDataSources(results, validSources);
        } else {
            return results;
        }
    }
}
