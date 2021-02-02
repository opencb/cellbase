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

package org.opencb.cellbase.core.api;

import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.api.query.QueryParameter;

import java.util.List;
import java.util.Map;

public class GenomeQuery extends AbstractQuery {

    @QueryParameter(id = "name")
    private List<String> names;

    @QueryParameter(id = "region")
    private List<Region> regions;

    public GenomeQuery() {
    }

    public GenomeQuery(Map<String, String> params) throws QueryException {
        super(params);
    }


    @Override
    protected void validateQuery() {
        // nothing to validate
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GenomeQuery{");
        sb.append("names=").append(names);
        sb.append(", regions=").append(regions);
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

    public List<Region> getRegions() {
        return regions;
    }

    public GenomeQuery setRegions(List<Region> regions) {
        this.regions = regions;
        return this;
    }

    public static final class GenomeQueryBuilder {
        protected Integer limit;
        protected Integer skip;
        protected Boolean count = false;
        protected String sort;
        protected Order order;
        protected String facet;
        protected List<String> includes;
        protected List<String> excludes;
        private List<String> names;
        private List<Region> regions;

        private GenomeQueryBuilder() {
        }

        public static GenomeQueryBuilder aGenomeQuery() {
            return new GenomeQueryBuilder();
        }

        public GenomeQueryBuilder withNames(List<String> names) {
            this.names = names;
            return this;
        }

        public GenomeQueryBuilder withRegions(List<Region> regions) {
            this.regions = regions;
            return this;
        }

        public GenomeQueryBuilder withLimit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public GenomeQueryBuilder withSkip(Integer skip) {
            this.skip = skip;
            return this;
        }

        public GenomeQueryBuilder withCount(Boolean count) {
            this.count = count;
            return this;
        }

        public GenomeQueryBuilder withSort(String sort) {
            this.sort = sort;
            return this;
        }

        public GenomeQueryBuilder withOrder(Order order) {
            this.order = order;
            return this;
        }

        public GenomeQueryBuilder withFacet(String facet) {
            this.facet = facet;
            return this;
        }

        public GenomeQueryBuilder withIncludes(List<String> includes) {
            this.includes = includes;
            return this;
        }

        public GenomeQueryBuilder withExcludes(List<String> excludes) {
            this.excludes = excludes;
            return this;
        }

        public GenomeQuery build() {
            GenomeQuery genomeQuery = new GenomeQuery();
            genomeQuery.setNames(names);
            genomeQuery.setRegions(regions);
            genomeQuery.setLimit(limit);
            genomeQuery.setSkip(skip);
            genomeQuery.setCount(count);
            genomeQuery.setSort(sort);
            genomeQuery.setOrder(order);
            genomeQuery.setFacet(facet);
            genomeQuery.setIncludes(includes);
            genomeQuery.setExcludes(excludes);
            return genomeQuery;
        }
    }
}
