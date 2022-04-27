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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.release.DataRelease;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.FacetField;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.List;

public class ReleaseMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor {

    private MongoDBCollection mongoDBCollection;

    private final String DATA_RELEASE_COLLECTION_NAME = "data_release";

    public ReleaseMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        init();
    }

    private void init() {
        logger.debug("ReleaseMongoDBAdaptor: in 'constructor'");
        mongoDBCollection = mongoDataStore.getCollection(DATA_RELEASE_COLLECTION_NAME);
    }

    public CellBaseDataResult<DataRelease> getAll() {
        return new CellBaseDataResult<>(mongoDBCollection.find(new BsonDocument(), null, DataRelease.class, new QueryOptions()));
    }

    public DataResult insert(DataRelease dataRelease) throws JsonProcessingException {
        Document document = Document.parse(new ObjectMapper().writeValueAsString(dataRelease));
        return mongoDBCollection.insert(document, QueryOptions.empty());
    }

    public void update(int release, String field, Object value) {
        Bson query = Filters.eq("release", release);
        Document projection = new Document(field, true);
        Bson update = Updates.set(field, value);
        QueryOptions queryOptions = new QueryOptions("replace", true);
        mongoDBCollection.findAndUpdate(query, projection, null, update, queryOptions);
    }

    @Override
    public CellBaseDataResult query(AbstractQuery query) {
        return null;
    }

    @Override
    public List<CellBaseDataResult> query(List queries) {
        return null;
    }

    @Override
    public CellBaseIterator iterator(AbstractQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<Long> count(AbstractQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(AbstractQuery query) {
        return null;
    }

    @Override
    public List<CellBaseDataResult> info(List ids, ProjectionQueryOptions queryOptions, int dataRelease) {
        return null;
    }

    @Override
    public CellBaseDataResult<FacetField> aggregationStats(AbstractQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(AbstractQuery query) {
        return null;
    }
}
