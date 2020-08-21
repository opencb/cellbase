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
import org.opencb.cellbase.core.api.queries.CellBaseIterator;
import org.opencb.cellbase.core.api.queries.ProjectionQueryOptions;
import org.opencb.cellbase.core.api.queries.RegulationQuery;
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
 * Created by imedina on 07/12/15.
 */
public class RegulationMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor<RegulationQuery, RegulatoryFeature> {

    public RegulationMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        ensemblCollection = mongoDataStore.getCollection("regulatory_region");

        logger.debug("RegulationMongoDBAdaptor: in 'constructor'");
    }


//    @Override
//    public CellBaseDataResult<RegulatoryFeature> next(Query query, QueryOptions options) {
//        return null;
//    }
//
//    @Override
//    public CellBaseDataResult nativeNext(Query query, QueryOptions options) {
//        return null;
//    }
//
//    @Override
//    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
//        Bson bsonQuery = parseQuery(query);
//        return groupBy(bsonQuery, field, "name", options);
//    }
//
//    @Override
//    public CellBaseDataResult groupBy(Query query, List<String> fields, QueryOptions options) {
//        Bson bsonQuery = parseQuery(query);
//        return groupBy(bsonQuery, fields, "name", options);
//    }
//
//    @Override
//    public CellBaseDataResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
//        if (query.getString(QueryParams.REGION.key()) != null) {
//            Region region = Region.parseRegion(query.getString(QueryParams.REGION.key()));
//            Bson bsonDocument = parseQuery(query);
//            return getIntervalFrequencies(bsonDocument, region, intervalSize, options);
//        }
//        return null;
//    }
//
//    @Override
//    public CellBaseDataResult<Long> count(Query query) {
//        Bson bsonDocument = parseQuery(query);
//        return new CellBaseDataResult<>(mongoDBCollection.count(bsonDocument));
//    }
//
//    @Override
//    public CellBaseDataResult distinct(Query query, String field) {
//        Bson bsonDocument = parseQuery(query);
//        return new CellBaseDataResult<>(mongoDBCollection.distinct(field, bsonDocument));
//    }

//    @Override
//    public CellBaseDataResult stats(Query query) {
//        return null;
//    }

//    @Override
//    public CellBaseDataResult<RegulatoryFeature> get(Query query, QueryOptions inputOptions) {
//        Bson bson = parseQuery(query);
//        QueryOptions options = addPrivateExcludeOptions(new QueryOptions(inputOptions));
//        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
//        return new CellBaseDataResult<>(mongoDBCollection.find(bson, null, RegulatoryFeature.class, options));
//    }
//
//    @Override
//    public CellBaseDataResult nativeGet(AbstractQuery query) {
//        return new CellBaseDataResult<>(mongoDBCollection.find(new BsonDocument(), null));
//    }
//
//    @Override
//    public CellBaseDataResult nativeGet(Query query, QueryOptions options) {
//        Bson bson = parseQuery(query);
//        return new CellBaseDataResult<>(mongoDBCollection.find(bson, options));
//    }
//
//    @Override
//    public Iterator<RegulatoryFeature> iterator(Query query, QueryOptions options) {
//        return null;
//    }


//    public Iterator nativeIterator(Query query, QueryOptions options) {
//        Bson bson = parseQuery(query);
//        return mongoDBCollection.nativeQuery().find(bson, options);
//    }
//
//    @Override
//    public void forEach(Query query, Consumer<? super Object> action, QueryOptions options) {
//
//    }
//
//    private Bson parseQuery(Query query) {
//        List<Bson> andBsonList = new ArrayList<>();
//        createRegionQuery(query, RegulationDBAdaptor.QueryParams.REGION.key(),
//                MongoDBCollectionConfiguration.REGULATORY_REGION_CHUNK_SIZE, andBsonList);
//
//        createOrQuery(query, RegulationDBAdaptor.QueryParams.NAME.key(), "name", andBsonList);
//        createOrQuery(query, RegulationDBAdaptor.QueryParams.FEATURE_TYPE.key(), "featureType", andBsonList);
//        createOrQuery(query, RegulationDBAdaptor.QueryParams.FEATURE_CLASS.key(), "featureClass", andBsonList);
//        createOrQuery(query, RegulationDBAdaptor.QueryParams.CELL_TYPES.key(), "cellTypes", andBsonList);
//        createOrQuery(query, RegulationDBAdaptor.QueryParams.SCORE.key(), "score", andBsonList);
//
//        if (andBsonList.size() > 0) {
//            return Filters.and(andBsonList);
//        } else {
//            return new Document();
//        }
//    }

    public Bson parseQuery(RegulationQuery query) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : query.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "region":
                        createRegionQuery(query, value, andBsonList);
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        logger.info("regulation parsed query: " + andBsonList.toString());
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
                = ensemblCollection.iterator(null, bson, projection, converter, queryOptions);
        return new CellBaseIterator<>(iterator);
    }

    @Override
    public List<CellBaseDataResult<RegulatoryFeature>> info(List<String> ids, ProjectionQueryOptions queryOptions) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(RegulationQuery query) {
        Bson bsonDocument = parseQuery(query);
        return new CellBaseDataResult<>(ensemblCollection.distinct(query.getFacet(), bsonDocument));
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

