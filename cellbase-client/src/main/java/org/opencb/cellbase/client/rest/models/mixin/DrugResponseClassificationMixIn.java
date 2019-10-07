/*
 * Copyright 2015-2019 OpenCB
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

package org.opencb.cellbase.client.rest.models.mixin;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.opencb.biodata.models.variant.avro.DrugResponseClassification;

import java.io.IOException;

/**
 * Created on 07/01/19.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
@JsonDeserialize(using = DrugResponseClassificationMixIn.Deserializer.class)
public interface DrugResponseClassificationMixIn {
    class Deserializer extends JsonDeserializer<DrugResponseClassification> {
        @Override
        public DrugResponseClassification deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JsonProcessingException {
            String value = jsonParser.getValueAsString();
            if (value == null) {
                return null;
            } else {
                try {
                    return DrugResponseClassification.valueOf(value);
                } catch (IllegalArgumentException e) {
                    // TODO: Map old to new values
//                        switch (value.toLowerCase()) {
//                            case "responsive":
//                                return DrugResponseClassification.???
//
//                        }
                    return null;
                }
            }
        }
    }

}
