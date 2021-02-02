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
import org.opencb.cellbase.core.result.CellBaseDataResult;

public interface AggregationApi<Q extends AbstractQuery, T> extends FeatureApi<Q, T> {

    default CellBaseDataResult<T> count(Q query) {
        query.setCount(Boolean.TRUE);
        return getDBAdaptor().count(query);
    }

    default CellBaseDataResult<T> groupBy(Q query) {
        query.setCount(Boolean.FALSE);
        return getDBAdaptor().groupBy(query);
    }

    default CellBaseDataResult<T> aggregationStats(Q query) {
        query.setCount(Boolean.TRUE);
        return getDBAdaptor().groupBy(query);
    }

}
