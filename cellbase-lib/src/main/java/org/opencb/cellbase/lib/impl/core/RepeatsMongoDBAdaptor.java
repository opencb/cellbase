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
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.cellbase.core.api.RepeatsQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
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

/**
 * Created by fjlopez on 10/05/17.
 */
public class RepeatsMongoDBAdaptor extends CellBaseDBAdaptor implements CellBaseCoreDBAdaptor<RepeatsQuery, Repeat> {

    private static final String REPEAT_COLLECTION = "repeats";

    public RepeatsMongoDBAdaptor(MongoDataStore mongoDatastore) {
        super(mongoDatastore);

        init();
    }

    private void init() {
        logger.debug("RepeatsMongoDBAdaptor: in 'constructor'");

        mongoDBCollectionByRelease = buildCollectionByReleaseMap(REPEAT_COLLECTION);
    }

    public Bson parseQuery(RepeatsQuery query) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : query.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "region":
                        createRegionQuery(query, value, MongoDBCollectionConfiguration.REPEATS_CHUNK_SIZE, andBsonList);
                        break;
                    case "token":
                    case "apiKey":
                    case "dataRelease":
                        // Do nothing
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        logger.debug("repeats parsed query: " + andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    @Override
    public CellBaseIterator iterator(RepeatsQuery query) throws CellBaseException {
        Bson bson = parseQuery(query);
        QueryOptions queryOptions = query.toQueryOptions();
        Bson projection = getProjection(query);
        GenericDocumentComplexConverter<Repeat> converter = new GenericDocumentComplexConverter<>(Repeat.class);
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        MongoDBIterator<Repeat> iterator = mongoDBCollection.iterator(null, bson, projection, converter, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public List<CellBaseDataResult<Repeat>> info(List<String> ids, ProjectionQueryOptions queryOptions, int dataRelease, String apiKey) {
        return null;
    }

    @Override
    public CellBaseDataResult<Long> count(RepeatsQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult aggregationStats(RepeatsQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(RepeatsQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(RepeatsQuery query) {
        return null;
    }

}
