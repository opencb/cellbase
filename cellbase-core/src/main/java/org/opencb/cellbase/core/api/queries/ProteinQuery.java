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

public class ProteinQuery extends AbstractQuery {

    public static final int DEFAULT_LIMIT = 20;

    @QueryParameter(id = "accession")
    private List<String> accessions;
    @QueryParameter(id = "name")
    private List<String> names;
    @QueryParameter(id = "gene")
    private List<String> genes;
    @QueryParameter(id = "xrefs")
    private List<String> xrefs;
    @QueryParameter(id = "keyword")
    private LogicalList<String> keywords;
    @QueryParameter(id = "feature.id")
    private LogicalList<String> featureIds;
    @QueryParameter(id = "feature.type")
    private LogicalList<String> featureTypes;

    public ProteinQuery() {
    }

    public ProteinQuery(Map<String, String> params) throws QueryException {
        super(params);
    }

    @Override
    protected void validateQuery() throws QueryException {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProteinQuery{");
        sb.append("accessions=").append(accessions);
        sb.append(", names=").append(names);
        sb.append(", genes=").append(genes);
        sb.append(", xrefs=").append(xrefs);
        sb.append(", keywords=").append(keywords);
        sb.append(", featureIds=").append(featureIds);
        sb.append(", featureTypes=").append(featureTypes);
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

    public List<String> getAccessions() {
        return accessions;
    }

    public ProteinQuery setAccessions(List<String> accessions) {
        this.accessions = accessions;
        return this;
    }

    public List<String> getNames() {
        return names;
    }

    public ProteinQuery setNames(List<String> names) {
        this.names = names;
        return this;
    }

    public List<String> getGenes() {
        return genes;
    }

    public ProteinQuery setGenes(List<String> genes) {
        this.genes = genes;
        return this;
    }

    public List<String> getXrefs() {
        return xrefs;
    }

    public ProteinQuery setXrefs(List<String> xrefs) {
        this.xrefs = xrefs;
        return this;
    }

    public LogicalList<String> getKeywords() {
        return keywords;
    }

    public ProteinQuery setKeywords(LogicalList<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public LogicalList<String> getFeatureIds() {
        return featureIds;
    }

    public ProteinQuery setFeatureIds(LogicalList<String> featureIds) {
        this.featureIds = featureIds;
        return this;
    }

    public LogicalList<String> getFeatureTypes() {
        return featureTypes;
    }

    public ProteinQuery setFeatureTypes(LogicalList<String> featureTypes) {
        this.featureTypes = featureTypes;
        return this;
    }


    public static final class ProteinQueryBuilder {
        public static int DEFAULT_LIMIT = 20;
        protected Integer limit;
        protected Integer skip;
        protected Boolean count = false;
        protected String sort;
        protected Order order;
        protected String facet;
        protected List<String> includes;
        protected List<String> excludes;
        private List<String> accessions;
        private List<String> names;
        private List<String> genes;
        private List<String> xrefs;
        private LogicalList<String> keywords;
        private LogicalList<String> featureIds;
        private LogicalList<String> featureTypes;

        private ProteinQueryBuilder() {
        }

        public static ProteinQueryBuilder aProteinQuery() {
            return new ProteinQueryBuilder();
        }

        public ProteinQueryBuilder withAccessions(List<String> accessions) {
            this.accessions = accessions;
            return this;
        }

        public ProteinQueryBuilder withNames(List<String> names) {
            this.names = names;
            return this;
        }

        public ProteinQueryBuilder withGenes(List<String> genes) {
            this.genes = genes;
            return this;
        }

        public ProteinQueryBuilder withXrefs(List<String> xrefs) {
            this.xrefs = xrefs;
            return this;
        }

        public ProteinQueryBuilder withKeywords(LogicalList<String> keywords) {
            this.keywords = keywords;
            return this;
        }

        public ProteinQueryBuilder withFeatureIds(LogicalList<String> featureIds) {
            this.featureIds = featureIds;
            return this;
        }

        public ProteinQueryBuilder withFeatureTypes(LogicalList<String> featureTypes) {
            this.featureTypes = featureTypes;
            return this;
        }

        public ProteinQueryBuilder withLimit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public ProteinQueryBuilder withSkip(Integer skip) {
            this.skip = skip;
            return this;
        }

        public ProteinQueryBuilder withCount(Boolean count) {
            this.count = count;
            return this;
        }

        public ProteinQueryBuilder withSort(String sort) {
            this.sort = sort;
            return this;
        }

        public ProteinQueryBuilder withOrder(Order order) {
            this.order = order;
            return this;
        }

        public ProteinQueryBuilder withFacet(String facet) {
            this.facet = facet;
            return this;
        }

        public ProteinQueryBuilder withIncludes(List<String> includes) {
            this.includes = includes;
            return this;
        }

        public ProteinQueryBuilder withExcludes(List<String> excludes) {
            this.excludes = excludes;
            return this;
        }

        public ProteinQuery build() {
            ProteinQuery proteinQuery = new ProteinQuery();
            proteinQuery.setAccessions(accessions);
            proteinQuery.setNames(names);
            proteinQuery.setGenes(genes);
            proteinQuery.setXrefs(xrefs);
            proteinQuery.setKeywords(keywords);
            proteinQuery.setFeatureIds(featureIds);
            proteinQuery.setFeatureTypes(featureTypes);
            proteinQuery.setLimit(limit);
            proteinQuery.setSkip(skip);
            proteinQuery.setCount(count);
            proteinQuery.setSort(sort);
            proteinQuery.setOrder(order);
            proteinQuery.setFacet(facet);
            proteinQuery.setIncludes(includes);
            proteinQuery.setExcludes(excludes);
            return proteinQuery;
        }
    }
}
