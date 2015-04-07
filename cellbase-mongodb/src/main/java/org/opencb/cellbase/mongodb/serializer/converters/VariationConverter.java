package org.opencb.cellbase.mongodb.serializer.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imedina on 31/08/14.
 */
public class VariationConverter extends MongoDBTypeConverter<Variation, DBObject> {

    private int chunkSize;
    private String chunkIdSuffix;


    public VariationConverter() {
        this(MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE);
    }

    public VariationConverter(int chunkSize) {
        // Parent class initializes and configures Jackson objects
        super();

        this.chunkSize = chunkSize;
        this.chunkIdSuffix = this.chunkSize / 1000 + "k";
    }

    @Override
    public DBObject convertToStorageSchema(Variation variation) {
        DBObject document = null;
        try {
            document = (DBObject) JSON.parse(jsonObjectWriter.writeValueAsString(variation));

            int chunkStart = (variation.getStart()) / chunkSize;
            int chunkEnd = (variation.getEnd()) / chunkSize;

            List<String> chunkIdsList = new ArrayList<>(chunkEnd-chunkStart+1);
            for(int i=chunkStart; i<=chunkEnd; i++) {
                chunkIdsList.add(variation.getChromosome()+"_"+i+"_"+chunkIdSuffix);
            }

            document.put("chunkIds", chunkIdsList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return document;
    }

    @Override
    public Variation convertToDataModel(DBObject dbObject) {
        return null;
    }
}
