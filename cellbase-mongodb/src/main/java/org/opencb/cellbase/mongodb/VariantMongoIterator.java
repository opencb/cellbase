package org.opencb.cellbase.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.opencb.biodata.models.variant.Variant;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Created by fjlopez on 11/02/16.
 */
public class VariantMongoIterator implements Iterator<Variant> {

    private Iterator<Document> mongoCursor;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public VariantMongoIterator(Iterator<Document> mongoCursor) {
        this.mongoCursor = mongoCursor;
    }


    @Override
    public boolean hasNext() {
        return mongoCursor.hasNext();
    }

    @Override
    public Variant next() {
        Document next = mongoCursor.next();
        Variant variant = OBJECT_MAPPER.convertValue(next, Variant.class);

        return variant;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("can't remove from a VariantMongoIterator");
    }

    @Override
    public void forEachRemaining(Consumer<? super Variant> action) {
        throw new UnsupportedOperationException("can't for each a VariantMongoDBIterator");
    }
}
