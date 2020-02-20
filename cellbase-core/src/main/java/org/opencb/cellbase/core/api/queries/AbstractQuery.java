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

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbstractQuery<T>  {

    protected List<String> includes;
    protected List<String> excludes;
    protected Integer skip;
    protected Integer limit;
    protected String sort;
    protected String facet;
    protected String timeout;
    protected Boolean count;
    protected Logger logger;

    final static int DEFAULT_LIMIT = 10;
    final static int DEFAULT_SKIP = 0;

    public AbstractQuery() {
    }

//    public T of(Map<String, Object> map) throws JsonProcessingException {
//        ObjectMapper objectMapper= new ObjectMapper();
//        String value = objectMapper.writeValueAsString(map);
//        return objectMapper.readValue(value, (Class) T);
//    }

//    public QueryOptions addQueryOption(String key, Object value) {
//        if (queryOptions == null) {
//            queryOptions = new QueryOptions();
//        }
//        queryOptions.put(key, value);
//        return queryOptions;
//    }
//
//    public QueryOptions getQueryOptions() {
//        return queryOptions;
//    }
//
//    public AbstractQuery setQueryOptions(QueryOptions queryOptions) {
//        this.queryOptions = queryOptions;
//        return this;
//    }

    public List<String> getIncludes() {
        return includes;
    }

    public AbstractQuery<T> setIncludes(List<String> includes) {
        this.includes = includes;
        return this;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public AbstractQuery<T> setExcludes(List<String> excludes) {
        this.excludes = excludes;
        return this;
    }

    public AbstractQuery<T> addExcludes(String excludes) {
        if (this.excludes == null) {
            this.excludes = new ArrayList<>();
        }
        this.excludes.addAll(Arrays.asList(excludes.split(",")));
        return this;
    }

    public Integer getSkip() {
        return skip;
    }

    public AbstractQuery<T> setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }

    public AbstractQuery<T> addSkipIfAbsent() {
        if (skip == null) {
            skip = DEFAULT_SKIP;
        }
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public AbstractQuery<T> setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public AbstractQuery<T> addLimitIfAbsent() {
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
        return this;
    }

    public String getSort() {
        return sort;
    }

    public AbstractQuery<T> setSort(String sort) {
        this.sort = sort;
        return this;
    }

    public String getFacet() {
        return facet;
    }

    public AbstractQuery<T> setFacet(String facet) {
        this.facet = facet;
        return this;
    }

    public String getTimeout() {
        return timeout;
    }

    public AbstractQuery<T> setTimeout(String timeout) {
        this.timeout = timeout;
        return this;
    }

    public Boolean getCount() {
        return count;
    }

    public AbstractQuery<T> setCount(Boolean count) {
        this.count = count;
        return this;
    }

    @Override
    public String toString() {
        return "AbstractQuery{" +
                "includes=" + includes +
                ", excludes=" + excludes +
                ", skip=" + skip +
                ", limit=" + limit +
                ", sort='" + sort + '\'' +
                ", facet='" + facet + '\'' +
                ", timeout='" + timeout + '\'' +
                '}';
    }
}
