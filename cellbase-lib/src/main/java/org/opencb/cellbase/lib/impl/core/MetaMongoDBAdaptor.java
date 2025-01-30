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

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.codehaus.jackson.map.ObjectMapper;
import org.opencb.cellbase.core.api.key.ApiKeyStats;
import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.commons.datastore.core.FacetField;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fjlopez on 07/06/16.
 */
public class MetaMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor {

    private MongoDBCollection mongoDBCollection;
    private MongoDBCollection apiKeyStatsMongoDBCollection;

    public MetaMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        logger.debug("MetaMongoDBAdaptor: in 'constructor'");
        mongoDBCollection = mongoDataStore.getCollection("metadata");
        apiKeyStatsMongoDBCollection = mongoDataStore.getCollection("apikey_stats", WriteConcern.ACKNOWLEDGED, ReadPreference.primary());
    }

    public CellBaseDataResult getAll() {
        return new CellBaseDataResult<>(mongoDBCollection.find(new BsonDocument(), new QueryOptions()));
    }

    @Override
    public CellBaseDataResult query(AbstractQuery query) {
        return new CellBaseDataResult<>(mongoDBCollection.find(new BsonDocument(), null));
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
    public List<CellBaseDataResult> info(List ids, ProjectionQueryOptions queryOptions, int dataRelease, String apiKey) {
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

    public CellBaseDataResult getQuota(String apiKey, String date) {
        List<Bson> andBsonList = new ArrayList<>();
        andBsonList.add(Filters.eq("apiKey", apiKey));
        andBsonList.add(Filters.eq("date", date));
        Bson query = Filters.and(andBsonList);

        return new CellBaseDataResult<>(apiKeyStatsMongoDBCollection.find(query, null, ApiKeyStats.class, QueryOptions.empty()));
    }

    public CellBaseDataResult initApiKeyStats(String apiKey, String date) throws CellBaseException {
        try {
            ApiKeyStats apiKeyStats = new ApiKeyStats(apiKey, date);
            Document document = Document.parse(new ObjectMapper().writeValueAsString(apiKeyStats));
            return new CellBaseDataResult<>(apiKeyStatsMongoDBCollection.insert(document, QueryOptions.empty()));
        } catch (IOException e) {
            throw new CellBaseException("Error initializing quota for API key '" + apiKey.substring(0, 10) + "...': " + e.getMessage());
        }
    }

    public CellBaseDataResult incApiKeyStats(String apiKey, String date, long incNumQueries, long incDuration, long incBytes) {
        List<Bson> andBsonList = new ArrayList<>();
        andBsonList.add(Filters.eq("apiKey", apiKey));
        andBsonList.add(Filters.eq("date", date));
        Bson query = Filters.and(andBsonList);

        Bson update = Updates.combine(Updates.inc("numQueries", incNumQueries),
                Updates.inc("duration", incDuration),
                Updates.inc("bytes", incBytes));

        Document projection = new Document("numQueries", true)
                .append("duration", true)
                .append("bytes", true);

        QueryOptions queryOptions = new QueryOptions("replace", true);

        return new CellBaseDataResult<>(apiKeyStatsMongoDBCollection.findAndUpdate(query, projection, null, update, ApiKeyStats.class,
                queryOptions));
    }
}
