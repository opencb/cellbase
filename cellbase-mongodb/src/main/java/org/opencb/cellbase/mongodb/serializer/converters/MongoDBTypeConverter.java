package org.opencb.cellbase.mongodb.serializer.converters;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.cellbase.core.serializer.CellBaseTypeConverter;

/**
 * Created by imedina on 01/09/14.
 */
public abstract class MongoDBTypeConverter<DataModel, StorageSchema> implements CellBaseTypeConverter<DataModel, StorageSchema> {

    protected ObjectMapper jsonObjectMapper;
    protected ObjectWriter jsonObjectWriter;

    protected MongoDBTypeConverter() {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectWriter = jsonObjectMapper.writer();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

}
