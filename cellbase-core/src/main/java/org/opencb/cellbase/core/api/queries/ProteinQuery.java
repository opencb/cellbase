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
    private List<String> keywords;

    public ProteinQuery() {
    }

    public ProteinQuery(Map<String, String> params) throws QueryException {
        super(params);
    }

    private ProteinQuery(Builder builder) {
        setIncludes(builder.includes);
        setExcludes(builder.excludes);
        setLimit(builder.limit);
        setSkip(builder.skip);
        setCount(builder.count);
        setSort(builder.sort);
        setOrder(builder.order);
        setFacet(builder.facet);
        setAccessions(builder.accessions);
        setNames(builder.names);
        setGenes(builder.genes);
        setXrefs(builder.xrefs);
        setKeywords(builder.keywords);
    }

    @Override
    protected void validateQuery() throws QueryException {

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

    public List<String> getKeywords() {
        return keywords;
    }

    public ProteinQuery setKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProteinQuery{");
        sb.append("accessions=").append(accessions);
        sb.append(", names=").append(names);
        sb.append(", genes=").append(genes);
        sb.append(", xrefs=").append(xrefs);
        sb.append(", keywords=").append(keywords);
        sb.append('}');
        return sb.toString();
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
        private List<String> accessions;
        private List<String> names;
        private List<String> genes;
        private List<String> xrefs;
        private List<String> keywords;

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

        public Builder withAccessions(List<String> val) {
            accessions = val;
            return this;
        }

        public Builder withNames(List<String> val) {
            names = val;
            return this;
        }

        public Builder withGenes(List<String> val) {
            genes = val;
            return this;
        }

        public Builder withXrefs(List<String> val) {
            xrefs = val;
            return this;
        }

        public Builder withKeywords(List<String> val) {
            keywords = val;
            return this;
        }

        public ProteinQuery build() {
            return new ProteinQuery(this);
        }
    }
}
