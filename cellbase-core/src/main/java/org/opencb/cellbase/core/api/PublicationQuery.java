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

import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.api.query.QueryParameter;

import java.util.List;
import java.util.Map;

public class PublicationQuery extends AbstractQuery {

    @QueryParameter(id = "id")
    private List<String> ids;

    @QueryParameter(id = "author")
    private List<String> authors;

    @QueryParameter(id = "title")
    private List<String> titles;

    @QueryParameter(id = "keyword")
    private List<String> keywords;

    @QueryParameter(id = "abstract")
    private List<String> abstracts;

    public PublicationQuery() {
    }

    public PublicationQuery(Map<String, String> params) throws QueryException {
        super(params);
    }


    @Override
    protected void validateQuery() {
        // nothing to validate
        return;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PublicationQuery{");
        sb.append("ids=").append(ids);
        sb.append(", authors=").append(authors);
        sb.append(", titles=").append(titles);
        sb.append(", keywords=").append(keywords);
        sb.append(", abstracts=").append(abstracts);
        sb.append('}');
        return sb.toString();
    }

    public List<String> getIds() {
        return ids;
    }

    public PublicationQuery setIds(List<String> ids) {
        this.ids = ids;
        return this;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public PublicationQuery setAuthors(List<String> authors) {
        this.authors = authors;
        return this;
    }

    public List<String> getTitles() {
        return titles;
    }

    public PublicationQuery setTitles(List<String> titles) {
        this.titles = titles;
        return this;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public PublicationQuery setKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public List<String> getAbstracts() {
        return abstracts;
    }

    public PublicationQuery setAbstracts(List<String> abstracts) {
        this.abstracts = abstracts;
        return this;
    }

    public static final class PublicationQueryBuilder {
        protected Integer limit;
        protected Integer skip;
        protected Boolean count = false;
        protected String sort;
        protected Order order;
        protected String facet;
        protected List<String> includes;
        protected List<String> excludes;
        private List<String> ids;
        private List<String> authors;
        private List<String> titles;
        private List<String> keywords;
        private List<String> abstracts;

        private PublicationQueryBuilder() {
        }

        public static PublicationQueryBuilder aPublicationQuery() {
            return new PublicationQueryBuilder();
        }

        public PublicationQueryBuilder withIds(List<String> ids) {
            this.ids = ids;
            return this;
        }

        public PublicationQueryBuilder withAuthors(List<String> authors) {
            this.authors = authors;
            return this;
        }

        public PublicationQueryBuilder withTitles(List<String> titles) {
            this.titles = titles;
            return this;
        }

        public PublicationQueryBuilder withKewords(List<String> keywords) {
            this.keywords = keywords;
            return this;
        }

        public PublicationQueryBuilder withAbstracts(List<String> abstracts) {
            this.abstracts = abstracts;
            return this;
        }

        public PublicationQueryBuilder withLimit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public PublicationQueryBuilder withSkip(Integer skip) {
            this.skip = skip;
            return this;
        }

        public PublicationQueryBuilder withCount(Boolean count) {
            this.count = count;
            return this;
        }

        public PublicationQueryBuilder withSort(String sort) {
            this.sort = sort;
            return this;
        }

        public PublicationQueryBuilder withOrder(Order order) {
            this.order = order;
            return this;
        }

        public PublicationQueryBuilder withFacet(String facet) {
            this.facet = facet;
            return this;
        }

        public PublicationQueryBuilder withIncludes(List<String> includes) {
            this.includes = includes;
            return this;
        }

        public PublicationQueryBuilder withExcludes(List<String> excludes) {
            this.excludes = excludes;
            return this;
        }

        public PublicationQuery build() {
            PublicationQuery publicationQuery = new PublicationQuery();
            publicationQuery.setIds(ids);
            publicationQuery.setAuthors(authors);
            publicationQuery.setTitles(titles);
            publicationQuery.setKeywords(keywords);
            publicationQuery.setAbstracts(abstracts);
            publicationQuery.setLimit(limit);
            publicationQuery.setSkip(skip);
            publicationQuery.setCount(count);
            publicationQuery.setSort(sort);
            publicationQuery.setOrder(order);
            publicationQuery.setFacet(facet);
            publicationQuery.setIncludes(includes);
            publicationQuery.setExcludes(excludes);
            return publicationQuery;
        }
    }
}
