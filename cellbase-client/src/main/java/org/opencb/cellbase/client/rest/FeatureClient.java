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

package org.opencb.cellbase.client.rest;

import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.client.rest.models.GroupByFields;
import org.opencb.cellbase.client.rest.models.GroupCount;
import org.opencb.cellbase.core.result.CellBaseDataResponse;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.io.IOException;

/**
 * Created by swaathi on 20/05/16.
 */
public class FeatureClient<T> extends ParentRestClient<T> {

    FeatureClient(String species, String assembly, String dataRelease, String apiKey, ClientConfiguration configuration) {
        super(species, assembly, dataRelease, apiKey, configuration);
    }


    public CellBaseDataResponse<T> next(String id) throws IOException {
        return execute(id, "next", null, clazz);
    }

    public CellBaseDataResponse<T> search(Query query, QueryOptions queryOptions) throws IOException {
        return execute("search", query, queryOptions, clazz);
    }

    public CellBaseDataResponse<GroupByFields> group(Query query, QueryOptions queryOptions) throws IOException {
        return execute("groupBy", query, queryOptions, GroupByFields.class);
    }

    public CellBaseDataResponse<GroupCount> groupCount(Query query, QueryOptions queryOptions) throws IOException {
        return execute("aggregationStats", query, queryOptions, GroupCount.class);
    }
}
