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

public class QueryOptions extends ProjectionQueryOptions {

    protected Integer skip;
    protected Integer limit;
    protected String sort;
    protected Boolean count;
    protected Order order;

    enum Order {
        ASCENDING,
        DESCENDING
    }

    public QueryOptions() {
    }

    public Integer getSkip() {
        return skip;
    }

    public QueryOptions setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public QueryOptions setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getSort() {
        return sort;
    }

    public QueryOptions setSort(String sort) {
        this.sort = sort;
        return this;
    }

    public Boolean getCount() {
        return count;
    }

    public QueryOptions setCount(Boolean count) {
        this.count = count;
        return this;
    }

    public Order getOrder() {
        return order;
    }

    public QueryOptions setOrder(Order order) {
        this.order = order;
        return this;
    }

    @Override
    public String toString() {
        return "QueryOptions{" +
                "skip=" + skip +
                ", limit=" + limit +
                ", sort='" + sort + '\'' +
                ", count=" + count +
                ", order=" + order +
                '}';
    }
}
