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

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;

import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AbstractQuery extends QueryOptions {

    protected Logger logger;

    final static int DEFAULT_LIMIT = 10;
    final static int DEFAULT_SKIP = 0;

    public AbstractQuery() {
    }

    public static Object of(MultivaluedMap<String, String> map, Class clazz)
            throws NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Object query = clazz.newInstance();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String fieldName = entry.getKey();
            List<String> values = entry.getValue();
            Field field = clazz.getField(fieldName);
            if (field.getType().equals("Boolean")) {
                Boolean bool = Boolean.parseBoolean(values.get(0));
                BeanUtils.setProperty(query, fieldName, bool);
            } else if (field.getType().equals("Integer")) {
                Integer intValue = Integer.parseInt(values.get(0));
                BeanUtils.setProperty(query, fieldName, intValue);
            } else if (field.getType().equals("List")) {
                List<String> valuesArray = Arrays.asList(values.get(0).split(","));
                BeanUtils.setProperty(query, fieldName, valuesArray);
            } else {
                BeanUtils.setProperty(query, fieldName, values.get(0));
            }
        }
        return query;
    }

}
