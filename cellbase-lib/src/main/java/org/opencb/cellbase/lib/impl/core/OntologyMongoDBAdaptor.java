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

package org.opencb.cellbase.lib.impl.core;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.OboTerm;
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.queries.CellBaseIterator;
import org.opencb.cellbase.core.api.queries.OntologyQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by fjlopez on 07/06/16.
 */
public class OntologyMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor<OntologyQuery, OboTerm> {

    public OntologyMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("ontology");
        logger.debug("OntologyMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public CellBaseIterator iterator(OntologyQuery query) {
        Bson bson = parseQuery(query);
        QueryOptions queryOptions = query.toQueryOptions();
        Bson projection = getProjection(query);
        GenericDocumentComplexConverter<OboTerm> converter = new GenericDocumentComplexConverter<>(OboTerm.class);
        MongoDBIterator<OboTerm> iterator = mongoDBCollection.iterator(null, bson, projection, converter, queryOptions);
        return new CellBaseIterator<>(iterator);
    }

    @Override
    public CellBaseDataResult<Long> count(OntologyQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(OntologyQuery query) {
        Bson bsonDocument = parseQuery(query);
        return new CellBaseDataResult<>(mongoDBCollection.distinct(query.getFacet(), bsonDocument));
    }

    @Override
    public CellBaseDataResult<OboTerm> aggregationStats(OntologyQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(OntologyQuery query) {
        Bson bsonQuery = parseQuery(query);
        logger.info("geneQuery: {}", bsonQuery.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return groupBy(bsonQuery, query, "name");
    }

    public Bson parseQuery(OntologyQuery query) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : query.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        logger.info("ontology parsed query: " + andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

}
