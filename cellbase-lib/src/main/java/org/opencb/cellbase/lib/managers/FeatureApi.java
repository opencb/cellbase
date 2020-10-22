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

import org.opencb.cellbase.lib.impl.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.queries.AbstractQuery;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.core.api.queries.CellBaseQueryOptions;
import org.opencb.cellbase.core.api.queries.QueryException;
import org.opencb.cellbase.core.result.CellBaseDataResult;

import java.util.ArrayList;
import java.util.List;

public interface FeatureApi<Q extends AbstractQuery, T> {

    CellBaseCoreDBAdaptor getDBAdaptor();

    default CellBaseDataResult<T> search(Q query) throws QueryException, IllegalAccessException {
        query.setDefaults();
        query.validate();
        return getDBAdaptor().query(query);
    }

    default List<CellBaseDataResult<T>> search(List<Q> queries) throws QueryException, IllegalAccessException {
        List<CellBaseDataResult<T>> results = new ArrayList<>();
        for (Q query : queries) {
            results.add(getDBAdaptor().query(query));
        }
        return results;
    }

    default List<CellBaseDataResult<T>> info(List<String> ids, CellBaseQueryOptions queryOptions) {
        List<CellBaseDataResult<T>> results = getDBAdaptor().info(ids, queryOptions);
        return results;
    }

    default CellBaseDataResult<String> distinct(Q query) {
        query.setCount(Boolean.FALSE);
        return getDBAdaptor().distinct(query);
    }

    default CellBaseIterator<T> iterator(Q query) {
        return getDBAdaptor().iterator(query);
    }
}
