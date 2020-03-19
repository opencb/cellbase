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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class OntologyQuery extends AbstractQuery {

    @QueryParameter(id = "id")
    private List<String> ids;
    @QueryParameter(id = "name")
    private List<String> names;
    @QueryParameter(id = "namespace")
    private List<String> namespaces;
    @QueryParameter(id = "synonyms")
    private List<String> synonyms;
    @QueryParameter(id = "xrefs")
    private List<String> xrefs;
    @QueryParameter(id = "parents")
    private LogicalList<String> parents;
    @QueryParameter(id = "children")
    private LogicalList<String> children;

    public OntologyQuery() {
    }

    public OntologyQuery(Map<String, String> params) throws QueryException {
        super(params);
    }

    private OntologyQuery(Builder builder) {
        setIncludes(builder.includes);
        setExcludes(builder.excludes);
        setLimit(builder.limit);
        setSkip(builder.skip);
        setCount(builder.count);
        setSort(builder.sort);
        setOrder(builder.order);
        setFacet(builder.facet);
        setIds(builder.ids);
        setNames(builder.names);
        setNamespaces(builder.namespaces);
        setSynonyms(builder.synonyms);
        setXrefs(builder.xrefs);
        setParents(builder.parents);
        setChildren(builder.children);
    }

    @Override
    protected void validateQuery() {
        // nothing to validate
        return;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OntologyQuery{");
        sb.append("ids=").append(ids);
        sb.append(", names=").append(names);
        sb.append(", namespaces=").append(namespaces);
        sb.append(", synonyms=").append(synonyms);
        sb.append(", xrefs=").append(xrefs);
        sb.append(", parents=").append(parents);
        sb.append(", children=").append(children);
        sb.append(", objectMapper=").append(objectMapper);
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

    public List<String> getIds() {
        return ids;
    }

    public OntologyQuery setIds(List<String> ids) {
        this.ids = ids;
        return this;
    }

    public List<String> getNames() {
        return names;
    }

    public OntologyQuery setNames(List<String> names) {
        this.names = names;
        return this;
    }

    public List<String> getNamespaces() {
        return namespaces;
    }

    public OntologyQuery setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
        return this;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public OntologyQuery setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
        return this;
    }

    public List<String> getXrefs() {
        return xrefs;
    }

    public OntologyQuery setXrefs(List<String> xrefs) {
        this.xrefs = xrefs;
        return this;
    }

    public LogicalList<String> getParents() {
        return parents;
    }

    public OntologyQuery setParents(LogicalList<String> parents) {
        this.parents = parents;
        return this;
    }

    public LogicalList<String> getChildren() {
        return children;
    }

    public OntologyQuery setChildren(LogicalList<String> children) {
        this.children = children;
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
        private ObjectMapper objectMapper;
        private List<String> ids;
        private List<String> names;
        private List<String> namespaces;
        private List<String> synonyms;
        private List<String> xrefs;
        private LogicalList<String> parents;
        private LogicalList<String> children;

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

        public Builder withObjectMapper(ObjectMapper val) {
            objectMapper = val;
            return this;
        }

        public Builder withIds(List<String> val) {
            ids = val;
            return this;
        }

        public Builder withNames(List<String> val) {
            names = val;
            return this;
        }

        public Builder withNamespaces(List<String> val) {
            namespaces = val;
            return this;
        }

        public Builder withSynonyms(List<String> val) {
            synonyms = val;
            return this;
        }

        public Builder withXrefs(List<String> val) {
            xrefs = val;
            return this;
        }

        public Builder withParents(LogicalList<String> val) {
            parents = val;
            return this;
        }

        public Builder withChildren(LogicalList<String> val) {
            children = val;
            return this;
        }

        public OntologyQuery build() {
            return new OntologyQuery(this);
        }
    }
}
