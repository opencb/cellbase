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

import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.CellBaseQueryOptions;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;

import java.util.ArrayList;
import java.util.List;

public interface FeatureApi<Q extends AbstractQuery, T> {

    CellBaseCoreDBAdaptor<Q, T> getDBAdaptor();

    default CellBaseDataResult<T> search(Q query) throws QueryException, IllegalAccessException, CellBaseException {
        query.setDefaults();
        query.validate();
        return getDBAdaptor().query(query);
    }

    default List<CellBaseDataResult<T>> search(List<Q> queries) throws QueryException, IllegalAccessException, CellBaseException {
        List<CellBaseDataResult<T>> results = new ArrayList<>();
        for (Q query : queries) {
            results.add(getDBAdaptor().query(query));
        }
        return results;
    }

    default List<CellBaseDataResult<T>> info(List<String> ids, CellBaseQueryOptions queryOptions, int dataRelease, String apiKey)
            throws CellBaseException {
        return getDBAdaptor().info(ids, queryOptions, dataRelease, apiKey);
    }

    default CellBaseDataResult<String> distinct(Q query) throws CellBaseException {
        query.setCount(Boolean.FALSE);
        return getDBAdaptor().distinct(query);
    }

    default CellBaseIterator<T> iterator(Q query) throws CellBaseException {
        return getDBAdaptor().iterator(query);
    }
}
