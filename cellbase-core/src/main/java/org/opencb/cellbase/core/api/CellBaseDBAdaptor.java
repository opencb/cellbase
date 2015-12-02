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

import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by imedina on 25/11/15.
 */
public interface CellBaseDBAdaptor<T> extends Iterable<T> {


    default QueryResult<Long> count() {
        return count(new Query());
    }

    QueryResult<Long> count(Query query);


    default QueryResult distinct(String field) {
        return distinct(new Query(), field);
    }

    QueryResult distinct(Query query, String field);


    default QueryResult stats() {
        return stats(new Query());
    }

    QueryResult stats(Query query);


    /*
     Main methods to query.
     */
    QueryResult first();

    QueryResult<T> get(Query query, QueryOptions options);

    default List<QueryResult<T>> get(List<Query> queries, QueryOptions options) {
        Objects.requireNonNull(queries);
        List<QueryResult<T>> queryResults = new ArrayList<>(queries.size());
        for (Query query : queries) {
            queryResults.add(get(query, options));
        }
        return queryResults;
    }

    QueryResult nativeGet(Query query, QueryOptions options);

    default List<QueryResult> nativeGet(List<Query> queries, QueryOptions options) {
        Objects.requireNonNull(queries);
        List<QueryResult> queryResults = new ArrayList<>(queries.size());
        for (Query query : queries) {
            queryResults.add(nativeGet(query, options));
        }
        return queryResults;
    }



    @Override
    default Iterator<T> iterator() {
        return iterator(new Query(), new QueryOptions());
    }

    default Iterator nativeIterator() {
        return nativeIterator(new Query(), new QueryOptions());
    }

    default Iterator nativeIiterator() {
        return nativeIterator(new Query(), new QueryOptions());
    }

    Iterator<T> iterator(Query query, QueryOptions options);

    Iterator nativeIterator(Query query, QueryOptions options);


    /*
     Iterator queries
     */
    @Override
    void forEach(Consumer action);

    void forEach(Query query, Consumer<? super Object> action, QueryOptions options);

}
