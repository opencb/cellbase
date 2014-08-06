package org.opencb.cellbase.lib.mongodb.serializer.converter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONSerializers;
import org.opencb.biodata.models.core.Gene;
import org.opencb.cellbase.core.serializer.CellBaseTypeConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imedina on 06/07/14.
 */
public class GeneConverter implements CellBaseTypeConverter<Gene, DBObject> {

    private final int CHUNK_SIZE = 5000;

    private ObjectMapper jsonObjectMapper;
    private ObjectWriter jsonObjectWriter;

    public GeneConverter() {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectWriter = jsonObjectMapper.writer();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public DBObject convertToStorageSchema(Gene gene) {
        DBObject document = null;
        try {
            document = (DBObject) JSON.parse(jsonObjectWriter.writeValueAsString(gene));

            String chunkIdSuffix = CHUNK_SIZE/1000 + "k";
            int chunkStart = (gene.getStart() - 5000) / CHUNK_SIZE;
            int chunkEnd = (gene.getEnd() + 5000) / CHUNK_SIZE;

            List<String> chunkIdsList = new ArrayList<>(chunkEnd-chunkStart+1);
            for(int i=chunkStart; i<=chunkEnd; i++) {
                chunkIdsList.add(gene.getChromosome()+"_"+i+"_"+chunkIdSuffix);
            }

            document.put("chunkIds", chunkIdsList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return document;
    }


    @Override
    public Gene convertToDataModel(DBObject dbObject) {
        return null;
    }

}
