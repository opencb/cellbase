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
