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

package org.opencb.cellbase.core.api.queries;

import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;

public class AbstractQuery {

    protected QueryOptions queryOptions;

    protected Logger logger;

    public AbstractQuery() {
    }


//    public T of(Map<String, Object> map) throws JsonProcessingException {
//        ObjectMapper objectMapper= new ObjectMapper();
//        String value = objectMapper.writeValueAsString(map);
//        return objectMapper.readValue(value, (Class)T);
//    }

    public QueryOptions addQueryOption(String key, Object value) {
        if (queryOptions == null) {
            queryOptions = new QueryOptions();
        }
        queryOptions.put(key, value);
        return queryOptions;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractQuery{");
        sb.append("queryOptions=").append(queryOptions);
        sb.append('}');
        return sb.toString();
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public AbstractQuery setQueryOptions(QueryOptions queryOptions) {
        this.queryOptions = queryOptions;
        return this;
    }
}
