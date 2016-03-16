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

import org.opencb.biodata.models.core.Region;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imedina on 25/11/15.
 */
public interface FeatureDBAdaptor<T> extends CellBaseDBAdaptor<T> {

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
        List<QueryResult<T>> results = new ArrayList<>(regions.size());
        for (Region region: regions) {
            Query query = new Query("region", region.toString());
            results.add(get(query, options));
        }
        return results;
    }

    QueryResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options);

}
