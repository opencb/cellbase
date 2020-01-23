/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.core.api;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.variant.annotation.hgvs.HgvsCalculator;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by imedina on 25/11/15.
 */
public interface FeatureDBAdaptor<T> extends CellBaseDBAdaptor<T> {

    String MERGE = "merge";
    String REGION = "region";

    default QueryResult first() {
        return first(new QueryOptions());
    }

    default QueryResult first(QueryOptions options) {
        if (options == null) {
            options = new QueryOptions();
        }
        options.put("limit", 1);
        return nativeGet(new Query(), options);
    }

    QueryResult<T> next(Query query, QueryOptions options);

    QueryResult nativeNext(Query query, QueryOptions options);

    default QueryResult<T> getByRegion(Region region, QueryOptions options) {
        Query query = new Query("region", region.toString());
        return get(query, options);
    }

    default List<QueryResult<T>> getByRegion(List<Region> regions, QueryOptions options) {
        if (options.containsKey(MERGE) && (Boolean) options.get(MERGE)) {
            Query query = new Query(REGION, String.join(",",
                    regions.stream().map((region) -> region.toString()).collect(Collectors.toList())));
            QueryResult<T> queryResult = get(query, options);
            return Collections.singletonList(queryResult);
        } else {
            List<QueryResult<T>> results = new ArrayList<>(regions.size());
            for (Region region : regions) {
                Query query = new Query("region", region.toString());
                results.add(get(query, options));
            }
            return results;
        }
    }

    QueryResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options);

    default List<QueryResult> getIntervalFrequencies(List<Query> queries, int intervalSize, QueryOptions options) {
        Objects.requireNonNull(queries);
        List<QueryResult> queryResults = new ArrayList<>(queries.size());
        for (Query query : queries) {
            queryResults.add(getIntervalFrequencies(query, intervalSize, options));
        }
        return queryResults;
    }

    default QueryResult<T> getByVariant(Variant variant, QueryOptions options) {
        return getByVariant(variant, null, options);
    }

    default QueryResult<T> getByVariant(Variant variant, GenomeDBAdaptor genomeDBAdaptor, QueryOptions options) {
        Query query;
        if (VariantType.CNV.equals(variant.getType())) {
            query = new Query(VariantDBAdaptor.QueryParams.CHROMOSOME.key(), variant.getChromosome())
                    .append(VariantDBAdaptor.QueryParams.CI_START_LEFT.key(), variant.getSv().getCiStartLeft())
                    .append(VariantDBAdaptor.QueryParams.CI_START_RIGHT.key(), variant.getSv().getCiStartRight())
                    .append(VariantDBAdaptor.QueryParams.CI_END_LEFT.key(), variant.getSv().getCiEndLeft())
                    .append(VariantDBAdaptor.QueryParams.CI_END_RIGHT.key(), variant.getSv().getCiEndRight())
                    .append(VariantDBAdaptor.QueryParams.REFERENCE.key(), variant.getReference())
                    .append(VariantDBAdaptor.QueryParams.ALTERNATE.key(), variant.getAlternate());
        } else {
            query = new Query(VariantDBAdaptor.QueryParams.CHROMOSOME.key(), variant.getChromosome())
                    .append(VariantDBAdaptor.QueryParams.START.key(), variant.getStart())
                    .append(VariantDBAdaptor.QueryParams.REFERENCE.key(), variant.getReference())
                    .append(VariantDBAdaptor.QueryParams.ALTERNATE.key(), variant.getAlternate());
            if (options.get("checkAminoAcidChange") != null && (Boolean) options.get("checkAminoAcidChange")
                    && genomeDBAdaptor != null) {
                List<Gene> batchedGeneList = (List<Gene>) options.get("batchGeneList");
                if (batchedGeneList != null && !batchedGeneList.isEmpty()) {
                    HgvsCalculator hgvsCalculator = new HgvsCalculator(genomeDBAdaptor);
                    List<String> hgvs = hgvsCalculator.run(variant, batchedGeneList);
                    query.append(ClinicalDBAdaptor.QueryParams.HGVS.key(), hgvs);
                }
            }
        }
        QueryResult<T> queryResult = get(query, options);
        queryResult.setId(variant.toString());

        return queryResult;
    }

    default List<QueryResult<T>> getByVariant(List<Variant> variants, QueryOptions options) {
        List<QueryResult<T>> results = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            results.add(getByVariant(variant, options));
        }
        return results;
    }

}
