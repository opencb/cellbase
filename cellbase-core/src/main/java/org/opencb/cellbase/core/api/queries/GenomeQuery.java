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
import java.util.Map;

public class GenomeQuery extends AbstractQuery {

    @QueryParameter(id = "name")
    private List<String> names;

    public GenomeQuery() {
    }

    public GenomeQuery(Map<String, String> params) throws QueryException {
        super(params);
    }

    private GenomeQuery(Builder builder) {
        setIncludes(builder.includes);
        setExcludes(builder.excludes);
        setLimit(builder.limit);
        setSkip(builder.skip);
        setCount(builder.count);
        setSort(builder.sort);
        setOrder(builder.order);
        setFacet(builder.facet);
        setNames(builder.names);
    }

    @Override
    protected void validateQuery() {
        // nothing to validate
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GenomeQuery{");
        sb.append("names=").append(names);
        sb.append(", limit=").append(limit);
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

    public List<String> getNames() {
        return names;
    }

    public GenomeQuery setNames(List<String> names) {
        this.names = names;
        return this;
    }


    public static final class Builder {
        private List<String> includes;
        private List<String> excludes;
        private Integer limit;
        private Integer skip;
        private Boolean count;
        private String sort;
        private Order order;
        private String facet;
        private List<String> names;

        public Builder() {
        }

        public Builder withIncludes(List<String> val) {
            includes = val;
            return this;
        }

        public Builder withExcludes(List<String> val) {
            excludes = val;
            return this;
        }

        public Builder withLimit(Integer val) {
            limit = val;
            return this;
        }

        public Builder withSkip(Integer val) {
            skip = val;
            return this;
        }

        public Builder withCount(Boolean val) {
            count = val;
            return this;
        }

        public Builder withSort(String val) {
            sort = val;
            return this;
        }

        public Builder withOrder(Order val) {
            order = val;
            return this;
        }

        public Builder withFacet(String val) {
            facet = val;
            return this;
        }

        public Builder withNames(List<String> val) {
            names = val;
            return this;
        }

        public GenomeQuery build() {
            return new GenomeQuery(this);
        }
    }
}
