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

    public CellBaseDataResult(int time, List<Event> events, int numResults, List<T> results, long numMatches,
                              long numInserted, long numUpdated, long numDeleted, ObjectMap attributes) {
        super(time, events, numResults, results, numMatches, numInserted, numUpdated, numDeleted, attributes);
    }

    public CellBaseDataResult(DataResult<T> result) {
        this(result.getTime(), result.getEvents(), result.getNumResults(), result.getResults(), result.getNumMatches(),
                result.getNumInserted(), result.getNumUpdated(), result.getNumDeleted(), result.getAttributes());
    }

    public CellBaseDataResult(String id, int time, int numResults, long numMatches, List<Event> events) {
        super(time, events, numResults, null, numMatches);
    }

    public CellBaseDataResult(String id, int time, int numResults, long numMatches, List<Event> events, List<T> results) {
        super(time, events, numResults, results, numMatches);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
