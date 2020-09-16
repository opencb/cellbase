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
    @QueryParameter(id = "source")
    private List<String> sources;
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
        sb.append(", sources=").append(sources);
        sb.append(", synonyms=").append(synonyms);
        sb.append(", xrefs=").append(xrefs);
        sb.append(", parents=").append(parents);
        sb.append(", children=").append(children);
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

    public List<String> getSources() {
        return sources;
    }

    public OntologyQuery setSources(List<String> sources) {
        this.sources = sources;
        return this;
    }


    public static final class OntologyQueryBuilder {
        protected Integer limit;
        protected Integer skip;
        protected Boolean count = false;
        protected String sort;
        protected Order order;
        protected String facet;
        protected List<String> includes;
        protected List<String> excludes;
        private List<String> ids;
        private List<String> names;
        private List<String> namespaces;
        private List<String> sources;
        private List<String> synonyms;
        private List<String> xrefs;
        private LogicalList<String> parents;
        private LogicalList<String> children;

        private OntologyQueryBuilder() {
        }

        public static OntologyQueryBuilder anOntologyQuery() {
            return new OntologyQueryBuilder();
        }

        public OntologyQueryBuilder withIds(List<String> ids) {
            this.ids = ids;
            return this;
        }

        public OntologyQueryBuilder withNames(List<String> names) {
            this.names = names;
            return this;
        }

        public OntologyQueryBuilder withNamespaces(List<String> namespaces) {
            this.namespaces = namespaces;
            return this;
        }

        public OntologyQueryBuilder withSources(List<String> sources) {
            this.sources = sources;
            return this;
        }

        public OntologyQueryBuilder withSynonyms(List<String> synonyms) {
            this.synonyms = synonyms;
            return this;
        }

        public OntologyQueryBuilder withXrefs(List<String> xrefs) {
            this.xrefs = xrefs;
            return this;
        }

        public OntologyQueryBuilder withParents(LogicalList<String> parents) {
            this.parents = parents;
            return this;
        }

        public OntologyQueryBuilder withChildren(LogicalList<String> children) {
            this.children = children;
            return this;
        }

        public OntologyQueryBuilder withLimit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public OntologyQueryBuilder withSkip(Integer skip) {
            this.skip = skip;
            return this;
        }

        public OntologyQueryBuilder withCount(Boolean count) {
            this.count = count;
            return this;
        }

        public OntologyQueryBuilder withSort(String sort) {
            this.sort = sort;
            return this;
        }

        public OntologyQueryBuilder withOrder(Order order) {
            this.order = order;
            return this;
        }

        public OntologyQueryBuilder withFacet(String facet) {
            this.facet = facet;
            return this;
        }

        public OntologyQueryBuilder withIncludes(List<String> includes) {
            this.includes = includes;
            return this;
        }

        public OntologyQueryBuilder withExcludes(List<String> excludes) {
            this.excludes = excludes;
            return this;
        }

        public OntologyQuery build() {
            OntologyQuery ontologyQuery = new OntologyQuery();
            ontologyQuery.setIds(ids);
            ontologyQuery.setNames(names);
            ontologyQuery.setNamespaces(namespaces);
            ontologyQuery.setSources(sources);
            ontologyQuery.setSynonyms(synonyms);
            ontologyQuery.setXrefs(xrefs);
            ontologyQuery.setParents(parents);
            ontologyQuery.setChildren(children);
            ontologyQuery.setLimit(limit);
            ontologyQuery.setSkip(skip);
            ontologyQuery.setCount(count);
            ontologyQuery.setSort(sort);
            ontologyQuery.setOrder(order);
            ontologyQuery.setFacet(facet);
            ontologyQuery.setIncludes(includes);
            ontologyQuery.setExcludes(excludes);
            return ontologyQuery;
        }
    }
}
