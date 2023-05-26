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
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.FacetField;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public DataResult<DataRelease> update(int release, List<String> versions) throws CellBaseException {
        DataRelease currDataRelease = mongoDBCollection.find(Filters.eq("release", release), null, DataRelease.class, QueryOptions.empty())
                .first();

        Map<Integer, List<String>> toUpdate = new HashMap<>();
        for (String version : versions) {
            DataResult<DataRelease> result = mongoDBCollection.find(Filters.eq("activeByDefaultIn", version), null, DataRelease.class,
                    QueryOptions.empty());
            if (result.getNumResults() > 1) {
                throw new CellBaseException("There's something wrong in the CellBase MongoDB. CellBase version " + version + " has"
                        + " multiple data releases: " + StringUtils.join(result.getResults().stream().map(dr -> dr.getRelease()), ","));
            }
            if (result.getNumResults() == 1) {
                DataRelease dr = result.first();
                if (!toUpdate.containsKey(dr.getRelease())) {
                    toUpdate.put(dr.getRelease(), dr.getActiveByDefaultIn());
                }
                toUpdate.get(dr.getRelease()).remove(version);
            }
        }

        // Start a transaction
        ClientSession session = mongoDataStore.startSession();
        try {
            session.startTransaction(TransactionOptions.builder().build());

            // Update data releases by removing versions
            for (Map.Entry<Integer, List<String>> entry : toUpdate.entrySet()) {
                update(entry.getKey(), "activeByDefaultIn", entry.getValue());
            }

            // Update data release by adding versions
            List<String> vers = new ArrayList<>(currDataRelease.getActiveByDefaultIn());
            vers.addAll(versions);
            update(release, "activeByDefaultIn", vers);

            // Commit the transaction
            session.commitTransaction();
        } catch (Exception e) {
            // Roll back the transaction if any operation fails
            session.abortTransaction();
            System.err.println("Transaction rolled back: " + e.getMessage());
        } finally {
            session.close();
        }

        return  mongoDBCollection.find(Filters.eq("release", release), null, DataRelease.class, QueryOptions.empty());
    }

    public DataResult update(int release, String field, Object value) {
        Bson query = Filters.eq("release", release);
        Document projection = new Document(field, true);
        Bson update = Updates.set(field, value);
        QueryOptions queryOptions = new QueryOptions("replace", true);
        return mongoDBCollection.findAndUpdate(query, projection, null, update, DataRelease.class, queryOptions);
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
    public List<CellBaseDataResult> info(List ids, ProjectionQueryOptions queryOptions, int dataRelease, String token) {
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
