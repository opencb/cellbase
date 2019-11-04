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

package org.opencb.cellbase.core.result;

import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.Event;
import org.opencb.commons.datastore.core.ObjectMap;

import java.util.ArrayList;
import java.util.List;

public class CellBaseDataResult<T> extends DataResult<T> {

    private String id;

    public CellBaseDataResult() {
    }

    public CellBaseDataResult(String id) {
        this.id = id;
    }

    public CellBaseDataResult(DataResult<T> result) {
        this("", result.getTime(), result.getEvents(), result.getNumResults(), result.getResults(), result.getNumMatches(),
                result.getNumInserted(), result.getNumUpdated(), result.getNumDeleted(), result.getAttributes());
    }

    public CellBaseDataResult(String id, int time, List<Event> events, long numMatches) {
        this(id, time, events, 0, new ArrayList<>(), numMatches);
    }

    public CellBaseDataResult(String id, int time, List<Event> events, int numResults, List<T> results, long numMatches) {
        this(id, time, events, numResults, results, numMatches, 0L, 0L, 0L, new ObjectMap());
    }

    public CellBaseDataResult(String id, int time, List<Event> events, int numResults, List<T> results, long numMatches,
                              long numInserted, long numUpdated, long numDeleted, ObjectMap attributes) {
        super(time, events, numResults, results, numMatches, numInserted, numUpdated, numDeleted, attributes);
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CellBaseDataResult{");
        sb.append("id='").append(id).append('\'');
        sb.append(", time=").append(time);
        sb.append(", events=").append(events);
        sb.append(", numResults=").append(numResults);
        sb.append(", results=").append(results);
        sb.append(", resultType='").append(resultType).append('\'');
        sb.append(", numMatches=").append(numMatches);
        sb.append(", numInserted=").append(numInserted);
        sb.append(", numUpdated=").append(numUpdated);
        sb.append(", numDeleted=").append(numDeleted);
        sb.append(", attributes=").append(attributes);
        sb.append('}');
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public CellBaseDataResult<T> setId(String id) {
        this.id = id;
        return this;
    }
}
