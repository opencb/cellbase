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

import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by imedina on 25/11/15.
 */
public interface CellBaseDBAdaptor<T> extends Iterable<T> {

    CellBaseDataResult<Long> update(List objectList, String field, String[] innerFields);

    default CellBaseDataResult<Long> count() {
        return count(new Query());
    }

    CellBaseDataResult<Long> count(Query query);

    default CellBaseDataResult<String> distinct(String field) {
        return distinct(new Query(), field);
    }

    CellBaseDataResult<String> distinct(Query query, String field);

//    default CellBaseDataResult stats() {
//        return stats(new Query());
//    }

//    CellBaseDataResult stats(Query query);

    /*
     Main methods to query.
     */
    CellBaseDataResult<T> get(Query query, QueryOptions options);

    default List<CellBaseDataResult<T>> get(List<Query> queries, QueryOptions options) {
        Objects.requireNonNull(queries);
        List<CellBaseDataResult<T>> queryResults = new ArrayList<>(queries.size());
        for (Query query : queries) {
            queryResults.add(get(query, options));
        }
        return queryResults;
    }

    CellBaseDataResult nativeGet(Query query, QueryOptions options);

    default List<CellBaseDataResult> nativeGet(List<Query> queries, QueryOptions options) {
        Objects.requireNonNull(queries);
        List<CellBaseDataResult> queryResults = new ArrayList<>(queries.size());
        for (Query query : queries) {
            queryResults.add(nativeGet(query, options));
        }
        return queryResults;
    }

//    CellBaseDataResult nativeGet(AbstractQuery query);

    @Override
    default Iterator<T> iterator() {
        return iterator(new Query(), new QueryOptions());
    }

    default Iterator nativeIterator() {
        return nativeIterator(new Query(), new QueryOptions());
    }

    Iterator<T> iterator(Query query, QueryOptions options);

    Iterator nativeIterator(Query query, QueryOptions options);

//    CellBaseDataResult rank(Query query, String field, int numResults, boolean asc);

    CellBaseDataResult groupBy(Query query, String field, QueryOptions options);

    CellBaseDataResult groupBy(Query query, List<String> fields, QueryOptions options);

    @Override
    default void forEach(Consumer action) {
        forEach(new Query(), action, new QueryOptions());
    }

    void forEach(Query query, Consumer<? super Object> action, QueryOptions options);

}
