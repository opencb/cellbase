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
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.cellbase.core.api.core.RepeatsDBAdaptor;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by fjlopez on 10/05/17.
 */
public class RepeatsMongoDBAdaptor extends MongoDBAdaptor implements RepeatsDBAdaptor {
    private static final String REPEAT_COLLECTION = "repeats";

    public RepeatsMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDatastore) {
        super(species, assembly, mongoDatastore);
        mongoDBCollection = mongoDataStore.getCollection(REPEAT_COLLECTION);

        logger.debug("RepeatsMongoDBAdaptor: in 'constructor'");

    }

    @Override
    public CellBaseDataResult<Long> update(List objectList, String field, String[] innerFields) {
        return null;
    }

    @Override
    public CellBaseDataResult<Long> count(Query query) {
        return null;
    }

    @Override
    public CellBaseDataResult distinct(Query query, String field) {
        return null;
    }

    @Override
    public CellBaseDataResult stats(Query query) {
        return null;
    }

    @Override
    public CellBaseDataResult get(Query query, QueryOptions inputOptions) {
        Bson bson = parseQuery(query);
        QueryOptions options = addPrivateExcludeOptions(new QueryOptions(inputOptions));

        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return new CellBaseDataResult(mongoDBCollection.find(bson, null, Repeat.class, options));
    }

    @Override
    public CellBaseDataResult nativeGet(Query query, QueryOptions inputOptions) {
        Bson bson = parseQuery(query);
        QueryOptions options = addPrivateExcludeOptions(new QueryOptions(inputOptions));

        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return new CellBaseDataResult(mongoDBCollection.find(bson, options));
    }

    @Override
    public Iterator iterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult rank(Query query, String field, int numResults, boolean asc) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
        return null;
    }

    @Override
    public void forEach(Query query, Consumer action, QueryOptions options) {

    }

    @Override
    public CellBaseDataResult groupBy(Query query, List fields, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult next(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult nativeNext(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
        return null;
    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();
        createRegionQuery(query, RepeatsDBAdaptor.QueryParams.REGION.key(),
                MongoDBCollectionConfiguration.REPEATS_CHUNK_SIZE, andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }

    }

}
