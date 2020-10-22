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

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.iterator.CellBaseIterator;
import org.opencb.cellbase.core.api.queries.ProjectionQueryOptions;
import org.opencb.cellbase.core.api.queries.RegulationQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.CellBaseMongoDBIterator;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by imedina on 07/12/15.
 */
public class RegulationMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor<RegulationQuery, RegulatoryFeature> {

    public RegulationMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("regulatory_region");

        logger.debug("RegulationMongoDBAdaptor: in 'constructor'");
    }

    public Bson parseQuery(RegulationQuery query) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : query.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "region":
                        createRegionQuery(query, value, MongoDBCollectionConfiguration.REGULATORY_REGION_CHUNK_SIZE, andBsonList);
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        logger.debug("regulation parsed query: " + andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    @Override
    public CellBaseIterator<RegulatoryFeature> iterator(RegulationQuery query) {
        Bson bson = parseQuery(query);
        QueryOptions queryOptions = query.toQueryOptions();
        Bson projection = getProjection(query);
        GenericDocumentComplexConverter<RegulatoryFeature> converter = new GenericDocumentComplexConverter<>(RegulatoryFeature.class);
        MongoDBIterator<RegulatoryFeature> iterator
                = mongoDBCollection.iterator(null, bson, projection, converter, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public List<CellBaseDataResult<RegulatoryFeature>> info(List<String> ids, ProjectionQueryOptions queryOptions) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(RegulationQuery query) {
        Bson bsonDocument = parseQuery(query);
        return new CellBaseDataResult<>(mongoDBCollection.distinct(query.getFacet(), bsonDocument));
    }
    @Override
    public CellBaseDataResult<Long> count(RegulationQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult aggregationStats(RegulationQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(RegulationQuery query) {
        return null;
    }

}

