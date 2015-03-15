package org.opencb.cellbase.mongodb.serializer.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.opencb.biodata.models.core.Gene;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imedina on 06/07/14.
 */
public class GeneConverter extends MongoDBTypeConverter<Gene, DBObject> {

    private String chunkIdSuffix;
    private int chunkSize;
    private final static int DEFAULT_CHUNK_SIZE = 5000;

    public GeneConverter() {
        this(DEFAULT_CHUNK_SIZE);
    }

    public GeneConverter(int chunkSize) {
        // Parent class initializes and configures Jackson objects
        super();

        this.chunkSize = chunkSize;
        this.chunkIdSuffix = this.chunkSize/1000 + "k";
    }

    @Override
    public DBObject convertToStorageSchema(Gene gene) {
        DBObject document = null;
        try {
            document = (DBObject) JSON.parse(jsonObjectWriter.writeValueAsString(gene));

            int chunkStart = (gene.getStart() - 5000) / chunkSize;
            int chunkEnd = (gene.getEnd() + 5000) / chunkSize;

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
