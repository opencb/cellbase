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

package org.opencb.cellbase.core.api.core;

import org.opencb.cellbase.core.api.queries.AbstractQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.FacetField;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface CellBaseMongoDBAdaptor<Q extends AbstractQuery, T> extends Iterable<T> {

    default CellBaseDataResult<T> query(Q query) {
        CellBaseDataResult<T> result = new CellBaseDataResult<>();
        List<T> results = new ArrayList<>();
        Iterator<T> iterator = iterator(query);
        while (iterator.hasNext() && results.size() < 100000) {
            T next = iterator.next();
            results.add(next);
        }
        result.setResults(results);
        return result;
    }

    default List<CellBaseDataResult<T>> query(List<Q> queries) {
        List<CellBaseDataResult<T>> results = new ArrayList<>();
        for (Q query : queries) {
            results.add(query(query));
        }
        return results;
    }

    @Override
    default Iterator<T> iterator() {
        return iterator(null);
    }

    Iterator<T> iterator(Q query);

//    CellBaseDataResult<Document> nativeQuery(Q query);

    CellBaseDataResult<Long> count(Q query);

//    Iterator<Document> nativeIterator(Q query);

    CellBaseDataResult<FacetField> aggregationStats(List<String> fields, Q query);

    CellBaseDataResult<String> distinct(String field, Q query);

}
