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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AbstractQuery extends org.opencb.cellbase.core.api.queries.QueryOptions {

    protected ObjectMapper objectMapper;
    protected Logger logger;

    public static final int DEFAULT_LIMIT = 10;
    public static final int DEFAULT_SKIP = 0;
    public static final int MAX_RECORDS = 1000;

    public AbstractQuery() {
        init();
    }

    public AbstractQuery(Map<String, String> params) {
        this();

        updateParams(params);
    }

    private void init() {
        objectMapper = new ObjectMapper();
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public void updateParams(ObjectMap objectMap) {
        try {
            objectMapper.updateValue(this, objectMap);
        } catch (JsonMappingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void updateParams(Map<String, String> params) {
        try {
            Map<String, Object> objectHashMap = new HashMap<>();
            for (Map.Entry<String, Class<?>> entry : loadPropertiesMap().entrySet()) {
                String value = params.get(entry.getKey());
                if (value != null) {
                    if (Collection.class.isAssignableFrom(entry.getValue())) {
                        objectHashMap.put(entry.getKey(), Arrays.asList(value.split(",")));
                    } else {
                        objectHashMap.put(entry.getKey(), params.get(entry.getKey()));
                    }
                }
            }
            objectMapper.updateValue(this, objectHashMap);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Map<String, Class<?>> loadPropertiesMap() {
        BeanDescription beanDescription = objectMapper.getSerializationConfig().introspect(objectMapper.constructType(this.getClass()));
        Map<String, Class<?>> internalPropertiesMap = new HashMap<>(beanDescription.findProperties().size() * 2);
        for (BeanPropertyDefinition property : beanDescription.findProperties()) {
            internalPropertiesMap.put(property.getName(), property.getRawPrimaryType());
        }
        return internalPropertiesMap;
    }

    /**
     * Checks if values for query are legal, e.g. >= 0 and <= MAX Checks the following parameters:
     *
     *  - SKIP
     *  - LIMIT
     *
     * NULL values are considered valid.
     * @throws CellbaseException if the skip or limit values are invalid
     */
    public void validate() throws CellbaseException {
        Integer skip = getSkip();
        Integer limit = getLimit();

        if (skip != null) {
            if (skip < 0) {
                throw new CellbaseException("Invalid value for skip field " + skip + ". Must be greater than zero");
            }
            if (skip > MAX_RECORDS) {
                throw new CellbaseException("Invalid value for skip field " + skip + ". Must be less than " + MAX_RECORDS);
            }
        }

        if (limit != null) {
            if (limit < 0) {
                throw new CellbaseException("Invalid value for limit field " + limit + ". Must be greater than zero");
            }
            if (limit > MAX_RECORDS) {
                throw new CellbaseException("Invalid value for limit field " + limit + ". Must be less than " + MAX_RECORDS);
            }
        }
        return;
    }

    public void setDefaults() {
        if (limit == null) {
            setLimit(DEFAULT_LIMIT);
        }
        if (skip == null) {
            setSkip(DEFAULT_SKIP);
        }
    }

    // temporary method because java commons still uses query options
    public QueryOptions toQueryOptions() {
        QueryOptions queryOptions = new org.opencb.commons.datastore.core.QueryOptions();
        queryOptions.put(QueryOptions.SKIP, skip);
        queryOptions.put(QueryOptions.LIMIT, limit);
        queryOptions.put(QueryOptions.COUNT, count);
        queryOptions.put(QueryOptions.INCLUDE, StringUtils.join(includes));
        queryOptions.put(QueryOptions.EXCLUDE, StringUtils.join(excludes));
        queryOptions.put(QueryOptions.SORT, sort);
        queryOptions.put(QueryOptions.ORDER, order);
        queryOptions.put(QueryOptions.TIMEOUT, timeout);
        queryOptions.put(QueryOptions.FACET, facet);
        return queryOptions;
    }

    // temporary method because java commons still uses query
    public Query toQuery() {
        return new Query();
    }
}
