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

import java.util.List;

public class QueryOptions extends ProjectionQueryOptions {

    @QueryParameter(id = "limit", min = "0")
    protected Integer limit;

    @QueryParameter(id = "skip", min = "0")
    protected Integer skip;

    @QueryParameter(id = "count")
    protected Boolean count;

    @QueryParameter(id = "sort")
    protected String sort;

    @QueryParameter(id = "order", dependsOn = "sort", allowedValues = {"ASCENDING", "DESCENDING"})
    protected Order order;

    @QueryParameter(id = "facet")
    protected String facet;

    enum Order {
        ASCENDING,
        DESCENDING
    }

    public QueryOptions() {
    }

    public QueryOptions(Integer limit, Integer skip, Boolean count, String sort, Order order, String facet) {
        this.limit = limit;
        this.skip = skip;
        this.count = count;
        this.sort = sort;
        this.order = order;
        this.facet = facet;
    }

    public QueryOptions(Integer limit, Integer skip, Boolean count, String sort, Order order, String facet,
                        List<String> includes, List<String> excludes) {
        super(includes, excludes);

        this.limit = limit;
        this.skip = skip;
        this.count = count;
        this.sort = sort;
        this.order = order;
        this.facet = facet;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryOptions{");
        sb.append("limit=").append(limit);
        sb.append(", skip=").append(skip);
        sb.append(", count=").append(count);
        sb.append(", sort='").append(sort).append('\'');
        sb.append(", order=").append(order);
        sb.append(", facet='").append(facet).append('\'');
        sb.append(", includes=").append(includes);
        sb.append(", excludes=").append(excludes);
        sb.append('}');
        return sb.toString();
    }

    public Integer getLimit() {
        return limit;
    }

    public QueryOptions setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Integer getSkip() {
        return skip;
    }

    public QueryOptions setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }

    public Boolean getCount() {
        return count;
    }

    public QueryOptions setCount(Boolean count) {
        this.count = count;
        return this;
    }

    public String getSort() {
        return sort;
    }

    public QueryOptions setSort(String sort) {
        this.sort = sort;
        return this;
    }

    public Order getOrder() {
        return order;
    }

    public QueryOptions setOrder(Order order) {
        this.order = order;
        return this;
    }

    public String getFacet() {
        return facet;
    }

    public QueryOptions setFacet(String facet) {
        this.facet = facet;
        return this;
    }
}
