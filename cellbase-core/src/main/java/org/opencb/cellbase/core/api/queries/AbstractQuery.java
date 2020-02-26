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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

public abstract class AbstractQuery extends org.opencb.cellbase.core.api.queries.QueryOptions {

    protected ObjectMapper objectMapper;
    protected Logger logger;

    public static final int DEFAULT_LIMIT = 10;
    public static final int DEFAULT_SKIP = 0;
    public static final int MAX_RECORDS = 1000;

    public AbstractQuery() {
        init();
    }

    public AbstractQuery(Map<String, String> params) throws QueryException {
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

    public void updateParams(Map<String, String> uriParams) throws QueryException {
        // list of fields in this class
        Map<String, Class<?>> stringClassMap = loadPropertiesMap();
        Map<String, QueryParameter> annotations = this.loadAnnotationMap();

        Map<String, String> params = new HashMap<>(uriParams);
        try {
            Map<String, Object> objectHashMap = new HashMap<>();
            for (Map.Entry<String, Class<?>> entry : stringClassMap.entrySet()) {
                String fieldNameDotNotation = entry.getKey();
                String fieldNameCamelCase = entry.getKey();
                Class<?> fieldType = entry.getValue();
                // if this field has an annotation, use that field name instead
                for (Map.Entry<String, QueryParameter> stringQueryParameterEntry : annotations.entrySet()) {
                    if (stringQueryParameterEntry.getKey().equals(entry.getKey())) {
                        fieldNameDotNotation = stringQueryParameterEntry.getValue().id();
//                        fieldNameCamelCase = stringQueryParameterEntry.getKey();
                    }
                }
                String value = params.get(fieldNameDotNotation.replace("\\.", "\\\\."));
                if (value != null) {
                    if (Collection.class.isAssignableFrom(fieldType)) {
                        if (LogicalList.class.isAssignableFrom(fieldType)) {
                            // AND
                            if (value.contains(";")) {
                                List valuesList = Arrays.asList(value.split(";"));
                                objectHashMap.put(fieldNameCamelCase, new LogicalList(valuesList, true));
                            // OR
                            } else {
                                List valuesList = Arrays.asList(value.split(","));
                                objectHashMap.put(fieldNameCamelCase, new LogicalList(valuesList, false));
                            }
                        } else {
                            objectHashMap.put(fieldNameCamelCase, Arrays.asList(value.split(",")));
                        }
                    } else {
                        objectHashMap.put(fieldNameCamelCase, value);
                    }
                }
                params.remove(fieldNameDotNotation);
            }
            if (!params.isEmpty()) {
                throw new QueryException("Invalid query parameter found: " + params.keySet().toString());
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

    private Map<String, QueryParameter> loadAnnotationMap() {
        Map<String, QueryParameter> annotations = new HashMap<>();
        for (Field declaredField : FieldUtils.getAllFields(this.getClass())) {
            QueryParameter declaredAnnotation = declaredField.getDeclaredAnnotation(QueryParameter.class);
            if (declaredAnnotation != null) {
                annotations.put(declaredField.getName(), declaredAnnotation);
//                System.out.println(declaredField.getName() + " = " + declaredAnnotation);
            }
        }
//        for (Field declaredField : org.opencb.cellbase.core.api.queries.QueryOptions.class.getDeclaredFields()) {
//            QueryParameter declaredAnnotation = declaredField.getDeclaredAnnotation(QueryParameter.class);
//            if (declaredAnnotation != null) {
//                annotations.put(declaredField.getName(), declaredAnnotation);
//                System.out.println(declaredField.getName() + " = " + declaredAnnotation);
//            }
//        }
        return annotations;
    }



    protected abstract void validateQuery() throws QueryException;

    /**
     * Checks if values for query are legal, e.g. >= 0 and <= MAX Checks the following parameters:
     *
     *  - SKIP
     *  - LIMIT
     *
     * NULL values are considered valid.
     * @throws QueryException if the skip or limit values are invalid.
     */
    public void validate() throws QueryException {
        this.checkIncludeAndExclude();
        this.checkLimitAndSkip();
        this.checkSortAndOrder();

        // Execute private checks
        this.validateQuery();
    }

    private void checkIncludeAndExclude() throws QueryException {
        if (CollectionUtils.isNotEmpty(includes) && CollectionUtils.isNotEmpty(excludes)) {
            Collection intersection = CollectionUtils.intersection(includes, excludes);
            if (intersection.size() > 0) {
                throw new QueryException("");
            }
        }
    }

    private void checkLimitAndSkip() throws QueryException {
        Integer limit = getLimit();
        if (limit != null) {
            if (limit < 0) {
                throw new QueryException("Invalid value for limit field " + limit + ". Must be greater than zero");
            }
//            if (limit > MAX_RECORDS) {
//                throw new QueryException("Invalid value for limit field " + limit + ". Must be less than " + MAX_RECORDS);
//            }
        }

        Integer skip = getSkip();
        if (skip != null) {
            if (skip < 0) {
                throw new QueryException("Invalid value for skip field " + skip + ". Must be greater than zero");
            }
//            if (skip > MAX_RECORDS) {
//                throw new QueryException("Invalid value for skip field " + skip + ". Must be less than " + MAX_RECORDS);
//            }
        }
    }

    private void checkSortAndOrder() throws QueryException {
        if (order != null && StringUtils.isEmpty(sort)) {
            throw new QueryException("");
        }
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
        queryOptions.put(QueryOptions.FACET, facet);
        return queryOptions;
    }
}
