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
import org.opencb.biodata.models.core.OntologyTerm;
import org.opencb.cellbase.core.api.OntologyQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.lib.iterator.CellBaseMongoDBIterator;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OntologyMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor<OntologyQuery, OntologyTerm> {

    private static final GenericDocumentComplexConverter<OntologyTerm> CONVERTER;

    static {
        CONVERTER = new GenericDocumentComplexConverter<>(OntologyTerm.class);
    }

    public OntologyMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        this.init();
    }

    private void init() {
        logger.debug("OntologyMongoDBAdaptor: in 'constructor'");

        mongoDBCollection = mongoDataStore.getCollection("ontology");
    }

    @Override
    public CellBaseIterator<OntologyTerm> iterator(OntologyQuery query) {
        Bson bson = parseQuery(query);
        Bson projection = getProjection(query);
        QueryOptions queryOptions = query.toQueryOptions();
        MongoDBIterator<OntologyTerm> iterator = mongoDBCollection.iterator(null, bson, projection, CONVERTER, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public List<CellBaseDataResult<OntologyTerm>> info(List<String> ids, ProjectionQueryOptions queryOptions) {
        List<CellBaseDataResult<OntologyTerm>> results = new ArrayList<>();
        Bson projection = getProjection(queryOptions);
        for (String id : ids) {
            List<Bson> orBsonList = new ArrayList<>(ids.size());
            orBsonList.add(Filters.eq("id", id));
            orBsonList.add(Filters.eq("name", id));
            Bson bson = Filters.or(orBsonList);
            results.add(new CellBaseDataResult<>(mongoDBCollection.find(bson, projection, CONVERTER, new QueryOptions())));
        }
        return results;
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
    public CellBaseDataResult<OntologyTerm> aggregationStats(OntologyQuery query) {
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
