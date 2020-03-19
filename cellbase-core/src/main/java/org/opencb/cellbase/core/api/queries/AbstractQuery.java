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
import com.google.common.base.Splitter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

public abstract class AbstractQuery extends CellBaseQueryOptions {

    protected ObjectMapper objectMapper;
    protected Logger logger;

    public static final int DEFAULT_LIMIT = 50;
    public static final int DEFAULT_SKIP = 0;
//    public static final int MAX_RECORDS = 1000;

    // list of fields in this class
    private Map<String, Field> classFields;
    // key = transcripts.biotype, value = transcriptsBiotype
    private Map<String, String> dotNotationToCamelCase;
    // list of fields in this class, and associated type
    private Map<String, Class<?>> classAttributesToType;
    // key = camelCase name (transcriptsBiotype) to annotations
    private Map<String, QueryParameter> annotations;

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

    /**
     * @return map of fieldName (dot notation) to field value
     * @throws IllegalAccessException if field is not accessible
     */
    public ObjectMap toObjectMap() throws IllegalAccessException {
        classAttributesToType = getClassAttributesToType();
        annotations = getAnnotations();
        classFields = getClassFields();
        QueryOptions queryOptions = toQueryOptions();
        ObjectMap queryMap = new ObjectMap();
        for (Map.Entry<String, Class<?>> entry : classAttributesToType.entrySet()) {
            String fieldNameCamelCase = entry.getKey();
            String dotNotationName = annotations.get(fieldNameCamelCase).id();
            Field field = classFields.get(fieldNameCamelCase);
            field.setAccessible(true);
            Object value = field.get(this);
            // don't add query options to the actual query
            if (value != null && !queryOptions.containsKey(dotNotationName) && !"exclude".equals(dotNotationName)) {
                queryMap.put(dotNotationName, value);
            }
        }
        logger.info("toObjectMap(): " + queryMap.safeToString());
        return queryMap;
    }

    public void updateParams(Map<String, String> uriParams) {
        classAttributesToType = getClassAttributesToType();
        annotations = getAnnotations();
        try {
            Map<String, Object> objectHashMap = new HashMap<>();
            for (Map.Entry<String, Class<?>> entry : classAttributesToType.entrySet()) {
                String fieldNameDotNotation = null;
                String fieldNameCamelCase = entry.getKey();
                Class fieldType = entry.getValue();
                QueryParameter queryParameter = annotations.get(fieldNameCamelCase);
                if (queryParameter != null) {
                    fieldNameDotNotation = queryParameter.id();
                }
                if (fieldNameDotNotation == null) {
                    // field has no annotation
                    continue;
                }
                String value = uriParams.get(fieldNameDotNotation.replace("\\.", "\\\\."));
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
            }
            objectMapper.updateValue(this, objectHashMap);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Map<String, Class<?>> loadPropertiesMap() {
        final ObjectMapper objectMapper = new ObjectMapper();
        BeanDescription beanDescription = objectMapper.getSerializationConfig().introspect(objectMapper.constructType(this.getClass()));
        Map<String, Class<?>> internalPropertiesMap = new HashMap<>(beanDescription.findProperties().size() * 2);
        for (BeanPropertyDefinition property : beanDescription.findProperties()) {
            internalPropertiesMap.put(property.getName(), property.getRawPrimaryType());
        }
        return internalPropertiesMap;
    }

    public Map<String, QueryParameter> loadAnnotationMap() {
        Map<String, QueryParameter> annotations = new HashMap<>();
        for (Field declaredField : FieldUtils.getAllFields(this.getClass())) {
            QueryParameter declaredAnnotation = declaredField.getDeclaredAnnotation(QueryParameter.class);
            if (declaredAnnotation != null) {
                annotations.put(declaredField.getName(), declaredAnnotation);
            }
        }
        return annotations;
    }

    protected abstract void validateQuery() throws QueryException;

    public void validate() throws QueryException, IllegalAccessException {
        validateParams();

        // Execute private checks
        this.validateQuery();
    }

    private Map<String, Class<?>> getClassAttributesToType() {
        if (classAttributesToType == null) {
            classAttributesToType = loadPropertiesMap();
        }
        return classAttributesToType;
    }

    private Map<String, QueryParameter> getAnnotations() {
        if (annotations == null) {
            annotations = loadAnnotationMap();
        }
        return annotations;
    }

    private Map<String, Field> getClassFields() {
        if (classFields == null) {
            classFields = new HashMap<>();
            for (Field declaredField : FieldUtils.getAllFields(this.getClass())) {
                classFields.put(declaredField.getName(), declaredField);
            }
        }
        return classFields;
    }

    private Map<String, String> getDotNotationToCamelCase() {
        if (dotNotationToCamelCase == null) {
            dotNotationToCamelCase = new HashMap<>();
            for (Map.Entry<String, Class<?>> entry : getClassAttributesToType().entrySet()) {
                String fieldNameCamelCase = entry.getKey();
                QueryParameter queryParameter = annotations.get(fieldNameCamelCase);
                if (queryParameter != null) {
                    String fieldNameDotNotation = queryParameter.id();
                    dotNotationToCamelCase.put(fieldNameDotNotation, fieldNameCamelCase);
                }
            }
        }
        return dotNotationToCamelCase;
    }

    private void validateParams() throws QueryException, IllegalAccessException {
        for (Map.Entry<String, Class<?>> classEntry : getClassAttributesToType().entrySet()) {
            String fieldNameCamelCase = classEntry.getKey();
            QueryParameter queryParameter = getAnnotations().get(fieldNameCamelCase);
            if (queryParameter == null) {
                // no annotation for this field, carry on
                continue;
            }

            Field field = getClassFields().get(fieldNameCamelCase);
            field.setAccessible(true);
            Object value = field.get(this);

            if (value == null) {
                if (queryParameter.required()) {
                    throw new QueryException(fieldNameCamelCase + " is required");
                } else {
                    // field is not required, and is empty. nothing to do.
                    continue;
                }
            }

            checkDependsOn(fieldNameCamelCase, queryParameter.dependsOn());

            checkAllowedValues(fieldNameCamelCase, queryParameter.allowedValues(), value);

            validateMin(fieldNameCamelCase, queryParameter.min(), value);

            validateMax(fieldNameCamelCase, queryParameter.max(), value);
        }

    }

    private void checkDependsOn(String fieldNameCamelCase, String requiredFieldDotNotation) throws IllegalAccessException, QueryException {
        if (StringUtils.isNotEmpty(requiredFieldDotNotation)) {
            String requiredFieldCamelCase = getDotNotationToCamelCase().get(requiredFieldDotNotation);
            Field requiredField = getClassFields().get(requiredFieldCamelCase);
            if (requiredField.get(this) == null) {
                throw new QueryException(requiredFieldCamelCase + " is required because " + fieldNameCamelCase + " has a value");
            }
        }
    }

    private void checkAllowedValues(String fieldNameCamelCase, String[] allowedValuesArray, Object value) throws QueryException {
        List<String> allowedValues = Arrays.asList(allowedValuesArray);
        if (allowedValues.size() > 0 && StringUtils.isNotEmpty(allowedValues.get(0))) {
            if (value instanceof List) {
                List<String> values = (List<String>) value;
                for (String s : values) {
                    if (!allowedValues.contains(s)) {
                        throw new QueryException(s + " is not a legal value for " + fieldNameCamelCase);
                    }
                }
            } else {
                if (!allowedValues.contains(value)) {
                    throw new QueryException(value + " is not a legal value for " + fieldNameCamelCase);
                }
            }
        }
    }

    private void validateMax(String fieldNameCamelCase, String max, Object value) throws QueryException {
        if (StringUtils.isNotEmpty(max)) {
            double doubleValue = new Double((Integer) value);
            if (doubleValue > Double.parseDouble(max)) {
                throw new QueryException(fieldNameCamelCase + " must be less than " + max + " but was " + doubleValue);
            }
        }
    }

    private void validateMin(String fieldNameCamelCase, String min, Object value) throws QueryException {
        if (StringUtils.isNotEmpty(min)) {
            double doubleValue = new Double((Integer) value);
            if (doubleValue < Double.parseDouble(min)) {
                throw new QueryException(fieldNameCamelCase + " must be at least " + min + " but was " + doubleValue);
            }
        }
    }

    public void setDefaults() {
        if (limit == null) {
            setLimit(DEFAULT_LIMIT);
        }
        if (skip == null) {
            setSkip(DEFAULT_SKIP);
        }
        if (count == null) {
            count = Boolean.FALSE;
        }
    }

    public QueryOptions toQueryOptions() {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put(QueryOptions.SKIP, skip);
        queryOptions.put(QueryOptions.LIMIT, limit);
        queryOptions.put(QueryOptions.COUNT, count);
        if (CollectionUtils.isNotEmpty(includes)) {
//            queryOptions.getAsStringList(QueryOptions.INCLUDE).addAll(Splitter.on(",").splitToList(includes.get(0)));
            queryOptions.put(QueryOptions.INCLUDE, StringUtils.join(this.includes, ","));
        }
        if (CollectionUtils.isNotEmpty(excludes)) {
            queryOptions.getAsStringList(QueryOptions.EXCLUDE).addAll(Splitter.on(",").splitToList(excludes.get(0)));
        }
        if (StringUtils.isNotEmpty(sort)) {
            queryOptions.put(QueryOptions.SORT, sort);
            queryOptions.put(QueryOptions.ORDER, order);
        }
        if (StringUtils.isNotEmpty(facet)) {
            queryOptions.put(QueryOptions.FACET, facet);
        }
        return queryOptions;
    }
}
