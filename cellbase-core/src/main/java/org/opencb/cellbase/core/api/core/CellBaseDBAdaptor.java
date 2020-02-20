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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by imedina on 25/11/15.
 */
public interface CellBaseDBAdaptor<T> extends Iterable<T> {


//    int insert(List objectList);

    CellBaseDataResult<Long> update(List objectList, String field, String[] innerFields);

//    CellBaseDataResult<Long> update(Query query, ObjectMap parameters);


    default CellBaseDataResult<Long> count() {
        return count(new AbstractQuery());
    }

    CellBaseDataResult<Long> count(AbstractQuery query);


    default CellBaseDataResult<String> distinct(String field) {
        return distinct(new AbstractQuery(), field);
    }

    CellBaseDataResult<String> distinct(AbstractQuery query, String field);


//    default CellBaseDataResult stats() {
//        return stats(new Query());
//    }
//
//    CellBaseDataResult stats(Query query);

    /*
     Main methods to query.
     */
    CellBaseDataResult<T> get(AbstractQuery query);

    default List<CellBaseDataResult<T>> get(List<AbstractQuery> queries) {
        Objects.requireNonNull(queries);
        List<CellBaseDataResult<T>> cellBaseDataResults = new ArrayList<>(queries.size());
        for (AbstractQuery query : queries) {
            cellBaseDataResults.add(get(query));
        }
        return cellBaseDataResults;
    }

    CellBaseDataResult nativeGet(AbstractQuery query);

    default List<CellBaseDataResult> nativeGet(List<AbstractQuery> queries) {
        Objects.requireNonNull(queries);
        List<CellBaseDataResult> cellBaseDataResults = new ArrayList<>(queries.size());
        for (AbstractQuery query : queries) {
            cellBaseDataResults.add(nativeGet(query));
        }
        return cellBaseDataResults;
    }

    @Override
    default Iterator<T> iterator() {
        return iterator(new AbstractQuery());
    }

    default Iterator nativeIterator() {
        return nativeIterator(new AbstractQuery());
    }

    Iterator<T> iterator(AbstractQuery query);

    Iterator nativeIterator(AbstractQuery query);

    CellBaseDataResult groupBy(AbstractQuery query, String field);

    CellBaseDataResult groupBy(AbstractQuery query, List<String> fields);

    @Override
    default void forEach(Consumer action) {
        forEach(new AbstractQuery(), action);
    }

    void forEach(AbstractQuery query, Consumer<? super Object> action);

}
