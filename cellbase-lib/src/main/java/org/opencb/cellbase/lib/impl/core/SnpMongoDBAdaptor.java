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
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Snp;
import org.opencb.cellbase.core.api.SnpQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.lib.iterator.CellBaseMongoDBIterator;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.opencb.cellbase.core.ParamConstants.API_KEY_PARAM;
import static org.opencb.cellbase.core.ParamConstants.DATA_RELEASE_PARAM;

public class SnpMongoDBAdaptor extends CellBaseDBAdaptor implements CellBaseCoreDBAdaptor<SnpQuery, Snp> {

    private static final GenericDocumentComplexConverter<Snp> CONVERTER;

    static {
        CONVERTER = new GenericDocumentComplexConverter<>(Snp.class);
    }

    public SnpMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        this.init();
    }

    private void init() {
        logger.debug("SnpMongoDBAdaptor: in 'constructor'");

        mongoDBCollectionByRelease = buildCollectionByReleaseMap("snp");
    }

    @Override
    public CellBaseIterator<Snp> iterator(SnpQuery query) throws CellBaseException {
        Bson bson = parseQuery(query);
        Bson projection = getProjection(query);
        QueryOptions queryOptions = query.toQueryOptions();

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        MongoDBIterator<Snp> iterator = mongoDBCollection.iterator(null, bson, projection, CONVERTER, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public List<CellBaseDataResult<Snp>> info(List<String> ids, ProjectionQueryOptions queryOptions, int dataRelease, String apiKey)
            throws CellBaseException {
        List<CellBaseDataResult<Snp>> results = new ArrayList<>();
        Bson projection = getProjection(queryOptions);
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
        for (String id : ids) {
            List<Bson> orBsonList = new ArrayList<>();
            orBsonList.add(Filters.eq("id", id));
            Bson bson = Filters.or(orBsonList);
            results.add(new CellBaseDataResult<>(mongoDBCollection.find(bson, projection, CONVERTER, new QueryOptions())));
        }
        return results;
    }

    @Override
    public CellBaseDataResult<String> distinct(SnpQuery query) throws CellBaseException {
        Bson bsonQuery = parseQuery(query);
        logger.info("snpQuery distinct: {}", bsonQuery.toBsonDocument().toJson());
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        return new CellBaseDataResult<>(mongoDBCollection.distinct(query.getFacet(), bsonQuery, String.class));
    }

    @Override
    public CellBaseDataResult<Snp> aggregationStats(SnpQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(SnpQuery query) throws CellBaseException {
        Bson bsonQuery = parseQuery(query);
        logger.info("snpQuery groupBy: {}", bsonQuery.toBsonDocument().toJson());
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        return groupBy(bsonQuery, query, "name", mongoDBCollection);
    }

    public CellBaseDataResult<Snp> startsWith(String id, QueryOptions options, int dataRelease) throws CellBaseException {
        Bson regex = Filters.regex("id", Pattern.compile("^" + id));
        Bson projection = null;
        if (options.containsKey(QueryOptions.INCLUDE)) {
            projection = Projections.include(options.getAsStringList(QueryOptions.INCLUDE));
        } else if (options.containsKey(QueryOptions.EXCLUDE)) {
            projection = Projections.exclude(options.getAsStringList(QueryOptions.EXCLUDE));
        }

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
        return new CellBaseDataResult<>(mongoDBCollection.find(regex, projection, CONVERTER, options));
    }

    public Bson parseQuery(SnpQuery query) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : query.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "position": {
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.INTEGER, andBsonList);
                        break;
                    }
                    case DATA_RELEASE_PARAM:
                    case API_KEY_PARAM:  {
                        // Do nothing
                        break;
                    }
                    default: {
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        logger.info("SnpMongoDBAdaptor parsed query: {}", andBsonList);
        if (!andBsonList.isEmpty()) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }
}
