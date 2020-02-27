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

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MongoQueryUtils {

    public static Map<String, Class<?>> loadPropertiesMap(Class clazz) {
        final ObjectMapper objectMapper = new ObjectMapper();
        BeanDescription beanDescription = objectMapper.getSerializationConfig().introspect(objectMapper.constructType(clazz));
        Map<String, Class<?>> internalPropertiesMap = new HashMap<>(beanDescription.findProperties().size() * 2);
        for (BeanPropertyDefinition property : beanDescription.findProperties()) {
            internalPropertiesMap.put(property.getName(), property.getRawPrimaryType());
        }
        return internalPropertiesMap;
    }

    public static Map<String, QueryParameter> loadAnnotationMap(Class clazz) {
        Map<String, QueryParameter> annotations = new HashMap<>();
        for (Field declaredField : FieldUtils.getAllFields(clazz)) {
            QueryParameter declaredAnnotation = declaredField.getDeclaredAnnotation(QueryParameter.class);
            if (declaredAnnotation != null) {
                annotations.put(declaredField.getName(), declaredAnnotation);
            }
        }
        return annotations;
    }

}
