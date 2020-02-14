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

import org.opencb.cellbase.core.api.core.RepeatsDBAdaptor;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.List;

public class RepeatsManager extends AbstractManager {

    private RepeatsDBAdaptor repeatsDBAdaptor;

    public RepeatsManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() {
        repeatsDBAdaptor = dbAdaptorFactory.getRepeatsDBAdaptor(species, assembly);
    }

    public List<CellBaseDataResult> getByRegion(Query query, QueryOptions queryOptions, String region) {
        List<Query> queries = createQueries(query, region, RepeatsDBAdaptor.QueryParams.REGION.key());
        List<CellBaseDataResult> queryResults = repeatsDBAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < queries.size(); i++) {
            queryResults.get(i).setId((String) queries.get(i).get(RepeatsDBAdaptor.QueryParams.REGION.key()));
        }
        return queryResults;
    }
}
