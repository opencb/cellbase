/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.mongodb.loader.converters;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.cellbase.core.loader.CellBaseTypeConverter;

/**
 * Created by imedina on 01/09/14.
 */
public abstract class MongoDBTypeConverter<DATAMODEL, STORAGESCHEMA> implements CellBaseTypeConverter<DATAMODEL, STORAGESCHEMA> {

    protected ObjectMapper jsonObjectMapper;
    protected ObjectWriter jsonObjectWriter;

    protected MongoDBTypeConverter() {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectWriter = jsonObjectMapper.writer();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

}
