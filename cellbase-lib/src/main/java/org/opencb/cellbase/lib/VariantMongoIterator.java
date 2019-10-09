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

package org.opencb.cellbase.lib;

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
