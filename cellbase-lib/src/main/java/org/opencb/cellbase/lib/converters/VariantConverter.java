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

package org.opencb.cellbase.lib.converters;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;

import java.io.IOException;
import java.io.UncheckedIOException;

public class VariantConverter extends GenericDocumentComplexConverter<Variant> {

    private final ObjectMapper objectMapper;

    public VariantConverter() {
        super(Variant.class);

        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        this.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public VariantConverter(ObjectMapper objectMapper) {
        super(Variant.class);

        this.objectMapper = objectMapper;
    }

    @Override
    public Variant convertToDataModelType(Document document) {
        try {
            restoreDots(document);
            restoreId(document);
            String json = objectMapper.writeValueAsString(document);
            return objectMapper.readValue(json, Variant.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // if the id is too long to index, we store temporarily in the _originalId field
    // when querying, put the id back
    private void restoreId(Document document) {
        if (document.get("_originalId") != null) {
            document.put("id", document.get("_originalId"));
        }
    }
}
