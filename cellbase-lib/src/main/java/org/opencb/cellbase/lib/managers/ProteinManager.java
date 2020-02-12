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

import org.forester.protein.Protein;
import org.opencb.cellbase.core.api.core.ProteinDBAdaptor;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Arrays;
import java.util.List;

public class ProteinManager extends AbstractManager {

    public ProteinManager(CellBaseConfiguration configuration) {
        super(configuration);
    }

    public CellBaseDataResult<Protein> search(Query query, QueryOptions queryOptions, String species, String assembly) {
        logger.debug("Searching proteins");
        ProteinDBAdaptor dbAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        return dbAdaptor.nativeGet(query, queryOptions);
    }

    public CellBaseDataResult<Protein> groupBy(Query query, QueryOptions queryOptions, String species, String assembly, String fields) {
        logger.debug("Querying for groupby");
        ProteinDBAdaptor dbAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        return dbAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
    }

    public CellBaseDataResult<Protein> aggregationStats(Query query, QueryOptions queryOptions, String species, String assembly, String fields) {
        logger.debug("Querying for aggregation stats");
        ProteinDBAdaptor dbAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        queryOptions.put(QueryOptions.COUNT, true);
        return dbAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
    }

    public List<CellBaseDataResult> info(Query query, QueryOptions queryOptions, String species, String assembly, String genes) {
        logger.debug("Querying for protein info");
        ProteinDBAdaptor dbAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        List<Query> queries = createQueries(genes, ProteinDBAdaptor.QueryParams.XREFS.key());
        List<CellBaseDataResult> queryResults = dbAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < queries.size(); i++) {
            queryResults.get(i).setId((String) queries.get(i).get(ProteinDBAdaptor.QueryParams.XREFS.key()));
        }
        return queryResults;
    }
}


