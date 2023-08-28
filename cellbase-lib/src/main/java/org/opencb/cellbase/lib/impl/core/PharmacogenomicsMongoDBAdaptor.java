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
import org.opencb.biodata.models.pharma.PharmaChemical;
import org.opencb.cellbase.core.api.PharmaChemicalQuery;
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

/**
 * Created by jtarraga on 9/4/23.
 */
public class PharmacogenomicsMongoDBAdaptor extends CellBaseDBAdaptor
        implements CellBaseCoreDBAdaptor<PharmaChemicalQuery, PharmaChemical> {

    private static final GenericDocumentComplexConverter<PharmaChemical> CONVERTER;

    static {
        CONVERTER = new GenericDocumentComplexConverter<>(PharmaChemical.class);
    }

    public PharmacogenomicsMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        this.init();
    }

    private void init() {
        mongoDBCollectionByRelease = buildCollectionByReleaseMap("pharmacogenomics");

        logger.debug("PharmacogenomicsMongoDBAdaptor initialised");
    }

    @Override
    public CellBaseDataResult<PharmaChemical> aggregationStats(PharmaChemicalQuery query) {
        logger.error("Not implemented yet");
        return null;
    }

    @Override
    public List<CellBaseDataResult<PharmaChemical>> info(List<String> ids, ProjectionQueryOptions queryOptions, int dataRelease,
                                                         String apiKey) throws CellBaseException {
        List<CellBaseDataResult<PharmaChemical>> results = new ArrayList<>();
        Bson projection = getProjection(queryOptions);
        for (String id : ids) {
            List<Bson> orBsonList = new ArrayList<>(ids.size());
            orBsonList.add(Filters.eq("id", id));
            orBsonList.add(Filters.eq("name", id));
            Bson query = Filters.or(orBsonList);
            MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
            results.add(new CellBaseDataResult<>(mongoDBCollection.find(query, projection, CONVERTER, new QueryOptions())));
        }
        return results;
    }

    @Override
    public CellBaseIterator<PharmaChemical> iterator(PharmaChemicalQuery query) throws CellBaseException {
        Bson bson = parseQuery(query);
        QueryOptions queryOptions = query.toQueryOptions();
        Bson projection = getProjection(query);
        MongoDBIterator<PharmaChemical> iterator;
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        iterator = mongoDBCollection.iterator(null, bson, projection, CONVERTER, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public CellBaseDataResult<String> distinct(PharmaChemicalQuery query) throws CellBaseException {
        Bson bsonDocument = parseQuery(query);
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        return new CellBaseDataResult<>(mongoDBCollection.distinct(query.getFacet(), bsonDocument, String.class));
    }

    @Override
    public CellBaseDataResult<PharmaChemical> groupBy(PharmaChemicalQuery query) throws CellBaseException {
        throw new CellBaseException("Not implemented yet");
    }

    public Bson parseQuery(PharmaChemicalQuery pharmaQuery) {
        List<Bson> andBsonList = new ArrayList<>();
        boolean visited = false;
        try {
            for (Map.Entry<String, Object> entry : pharmaQuery.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "token":
                    case "apiKey":
                    case "dataRelease":
                        // do nothing
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        logger.debug("pharmacogenomics parsed query: " + andBsonList);
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }
}
