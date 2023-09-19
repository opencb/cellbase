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

package org.opencb.cellbase.lib.impl.core;

import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.commons.datastore.core.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface CellBaseCoreDBAdaptor<Q extends AbstractQuery, T> extends Iterable<T> {

    int MAX_ROWS = 50000;

    default CellBaseDataResult<T> query(Q query) throws CellBaseException {
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
        result.setNumMatches(iterator.getNumMatches());
        result.setNumResults(results.size());
//        result.setResultType(T);
        if (results.size() > MAX_ROWS) {
            Event event = new Event(Event.Type.ERROR, "", "Too many elements. " + results.size()
                    + " records found but " + MAX_ROWS + " is maximum allowed. Add more filters or use an iterator");
            if (result.getEvents() == null) {
                result.setEvents(new ArrayList<>());
            }
            result.getEvents().add(event);
        } else {
            // only return results if valid number of results
            result.setResults(results);
        }
        return result;
    }

    default List<CellBaseDataResult<T>> query(List<Q> queries) throws CellBaseException {
        List<CellBaseDataResult<T>> results = new ArrayList<>();
        for (Q query : queries) {
            results.add(query(query));
        }
        return results;
    }

    @Override
    default CellBaseIterator<T> iterator() {
        try {
            return iterator(null);
        } catch (CellBaseException e) {
            e.printStackTrace();
        }
        return null;
    }

    CellBaseIterator<T> iterator(Q query) throws CellBaseException;

    default CellBaseDataResult<Long> count(Q query) throws CellBaseException {
        query.setCount(true);
        query.setSkip(0);
        query.setLimit(0);
        CellBaseDataResult<T> queryResults = query(query);
        CellBaseDataResult<Long> countResults = new CellBaseDataResult<>();
        countResults.setResults(Collections.singletonList(queryResults.getNumMatches()));
        return countResults;
    }

    CellBaseDataResult<T> aggregationStats(Q query);

    CellBaseDataResult<T> groupBy(Q query) throws CellBaseException;

    CellBaseDataResult<String> distinct(Q query) throws CellBaseException;

    List<CellBaseDataResult<T>> info(List<String> ids, ProjectionQueryOptions queryOptions, int dataRelease, String apiKey)
            throws CellBaseException;
}
