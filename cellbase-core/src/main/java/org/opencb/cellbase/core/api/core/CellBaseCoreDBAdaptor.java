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
import org.opencb.cellbase.core.api.queries.CellBaseIterator;
import org.opencb.cellbase.core.api.queries.ProjectionQueryOptions;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface CellBaseCoreDBAdaptor<Q extends AbstractQuery, T> extends Iterable<T> {

    int MAX_ROWS = 100000;

    default CellBaseDataResult<T> query(Q query) {
        List<T> results = new ArrayList<>();
        long time = System.currentTimeMillis();
        CellBaseIterator<T> iterator = iterator(query);
        while (iterator.hasNext() && results.size() < MAX_ROWS) {
            T next = iterator.next();
            results.add(next);
        }

        // close the database connection
        iterator.close();

        time = System.currentTimeMillis() - time;

        CellBaseDataResult<T> result = new CellBaseDataResult<>();
        result.setTime((int) time);
        result.setResults(results);
        result.setNumMatches(iterator.getNumMatches());
        result.setNumResults(results.size());
//        result.setResultType(T);
        if (results.size() > MAX_ROWS) {
            Event event = new Event(Event.Type.WARNING, "", "Max number of elements reached");
            if (result.getEvents() == null) {
                result.setEvents(new ArrayList<>());
            }
            result.getEvents().add(event);
        }
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
    default CellBaseIterator<T> iterator() {
        return iterator(null);
    }

    CellBaseIterator<T> iterator(Q query);

    default CellBaseDataResult<Long> count(Q query) {
        query.setCount(true);
        query.setLimit(0);
        CellBaseDataResult<T> queryResults = query(query);
        CellBaseDataResult<Long> countResults = new CellBaseDataResult<>();
        countResults.setResults(Collections.singletonList((long) queryResults.getNumResults()));
        return countResults;
    }

    CellBaseDataResult<T> aggregationStats(Q query);

    CellBaseDataResult<T> groupBy(Q query);

    CellBaseDataResult<String> distinct(Q query);

    List<CellBaseDataResult<T>> info(List<String> ids, ProjectionQueryOptions queryOptions);
}
