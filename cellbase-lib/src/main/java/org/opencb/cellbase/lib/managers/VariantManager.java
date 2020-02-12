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

import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.api.core.VariantDBAdaptor;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Arrays;
import java.util.List;

public class VariantManager extends AbstractManager {

    public VariantManager(CellBaseConfiguration configuration) {
        super(configuration);
    }

    public CellBaseDataResult<Variant> search(Query query, QueryOptions queryOptions, String species, String assembly) {
        logger.debug("Searching variants");
        VariantDBAdaptor dbAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species, assembly);
        return dbAdaptor.nativeGet(query, queryOptions);
    }

    public CellBaseDataResult<Variant> groupBy(Query query, QueryOptions queryOptions, String species, String assembly, String fields) {
        logger.debug("Querying for groupby");
        VariantDBAdaptor dbAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species, assembly);
        return dbAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
    }

    public CellBaseDataResult<Variant> aggregationStats(Query query, QueryOptions queryOptions, String species, String assembly, String fields) {
        logger.debug("Querying for aggregation stats");
        VariantDBAdaptor dbAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species, assembly);
        queryOptions.put(QueryOptions.COUNT, true);
        return dbAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
    }

    public List<CellBaseDataResult> info(Query query, QueryOptions queryOptions, String species, String assembly, String genes) {
        logger.debug("Querying for variant info");
        VariantDBAdaptor dbAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species, assembly);
        List<Query> queries = createQueries(query, genes, VariantDBAdaptor.QueryParams.XREFS.key());
        List<CellBaseDataResult> queryResults = dbAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < queries.size(); i++) {
            queryResults.get(i).setId((String) queries.get(i).get(VariantDBAdaptor.QueryParams.XREFS.key()));
        }
        return queryResults;
    }

    public boolean validateRegionInput(String regions) {
        List<Region> regionList = Region.parseRegions(regions);
        // check for regions bigger than 10Mb
        if (regionList != null) {
            for (Region r : regionList) {
                if ((r.getEnd() - r.getStart()) > 10000000) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<CellBaseDataResult> getByRegion(Query query, QueryOptions queryOptions, String species, String assembly, String regions) {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species, assembly);
        if (hasHistogramQueryParam(queryOptions)) {
            List<Query> queries = createQueries(query, regions, GeneDBAdaptor.QueryParams.REGION.key());
            List<CellBaseDataResult> queryResults = variationDBAdaptor.getIntervalFrequencies(queries,
                    getHistogramIntervalSize(queryOptions), queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResults.get(i).setId(queries.get(i).getString(GeneDBAdaptor.QueryParams.REGION.key()));
            }
            return queryResults;
        } else {
            query.put(VariantDBAdaptor.QueryParams.REGION.key(), regions);
            logger.debug("query = " + query.toJson());
            logger.debug("queryOptions = " + queryOptions.toJson());
            List<Query> queries = createQueries(query, regions, VariantDBAdaptor.QueryParams.REGION.key());
            List<CellBaseDataResult> queryResults = variationDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResults.get(i).setId((String) queries.get(i).get(VariantDBAdaptor.QueryParams.REGION.key()));
            }
            return queryResults;
        }
    }
}
