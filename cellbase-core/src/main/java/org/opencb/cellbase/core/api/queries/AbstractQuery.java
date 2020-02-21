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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.apache.commons.beanutils.BeanUtils;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.commons.datastore.core.ObjectMap;
import org.slf4j.Logger;

import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class AbstractQuery extends QueryOptions {

    protected Logger logger;

    static final int DEFAULT_LIMIT = 10;
    static final int DEFAULT_SKIP = 0;
    static final int MAX_RECORDS = 1000;

    public AbstractQuery() {
    }

    public void updateParams(Map<String, Object> params) {
        ObjectMapper objectMapper = getObjectMapper();
        try {
            // Split string lists
            ObjectMap copy = new ObjectMap(params);
            for (Map.Entry<String, Class<?>> entry : loadPropertiesMap().entrySet()) {
                if (Collection.class.isAssignableFrom(entry.getValue())) {
                    copy.put(entry.getKey(), copy.getAsStringList(entry.getKey()));
                }
            }
            objectMapper.updateValue(this, copy);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private Map<String, Class<?>> internalPropertiesMap = null;
    private Map<String, Class<?>> loadPropertiesMap() {
        if (internalPropertiesMap == null) {
            ObjectMapper objectMapper = getObjectMapper();
            BeanDescription beanDescription = objectMapper.getSerializationConfig().introspect(objectMapper.constructType(this.getClass()));
            internalPropertiesMap = new HashMap<>(beanDescription.findProperties().size());
            for (BeanPropertyDefinition property : beanDescription.findProperties()) {
                internalPropertiesMap.put(property.getName(), property.getRawPrimaryType());
            }
        }
        return internalPropertiesMap;
    }

    public static <T> T of(Map<String, String> map, Class<T> clazz)
            throws NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException {
        T query = clazz.newInstance();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String fieldName = entry.getKey();
            String value = entry.getValue();
            Field field = clazz.getField(fieldName);
//            Method method = clazz.getMethod("set" + fieldName);
            if (fieldName.equals("region")) {
//                method.invoke(Region.parseRegions()); ....
                List<Region> regions = Region.parseRegions(value);
                BeanUtils.setProperty(query, fieldName, regions);
            } else {
                switch (field.getType().toString()) {
                    case "Boolean":
                        Boolean bool = Boolean.parseBoolean(value);
//                    method.invoke(bool);
                        BeanUtils.setProperty(query, fieldName, bool);
                        break;
                    case "Integer":
                        Integer intValue = Integer.parseInt(value);
                        BeanUtils.setProperty(query, fieldName, intValue);
                        break;
                    case "List":
                        List<String> valuesArray = Arrays.asList(value);
                        BeanUtils.setProperty(query, fieldName, valuesArray);
                        break;
                    default:
                        BeanUtils.setProperty(query, fieldName, value);
                        break;
                }
            }
        }
        return query;
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

}
