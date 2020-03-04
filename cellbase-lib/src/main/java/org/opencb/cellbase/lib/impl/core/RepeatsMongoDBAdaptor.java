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
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.core.RepeatsDBAdaptor;
import org.opencb.cellbase.core.api.queries.AbstractQuery;
import org.opencb.cellbase.core.api.queries.CellBaseIterator;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.FacetField;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by fjlopez on 10/05/17.
 */
public class RepeatsMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor {
    private static final String REPEAT_COLLECTION = "repeats";

    public RepeatsMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDatastore) {
        super(species, assembly, mongoDatastore);
        mongoDBCollection = mongoDataStore.getCollection(REPEAT_COLLECTION);

        logger.debug("RepeatsMongoDBAdaptor: in 'constructor'");

    }

    public CellBaseDataResult<Long> count(Query query) {
        return null;
    }

    public CellBaseDataResult distinct(Query query, String field) {
        return null;
    }

//    @Override
//    public CellBaseDataResult stats(Query query) {
//        return null;
//    }

    public CellBaseDataResult get(Query query, QueryOptions inputOptions) {
        Bson bson = parseQuery(query);
        QueryOptions options = addPrivateExcludeOptions(new QueryOptions(inputOptions));

        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return new CellBaseDataResult(mongoDBCollection.find(bson, null, Repeat.class, options));
    }

    public List<CellBaseDataResult> nativeGet(List<Query> queries, QueryOptions options) {
//        return new CellBaseDataResult(mongoDBCollection.find(new BsonDocument(), null));
        return null;
    }


    public CellBaseDataResult nativeGet(Query query, QueryOptions inputOptions) {
        Bson bson = parseQuery(query);
        QueryOptions options = addPrivateExcludeOptions(new QueryOptions(inputOptions));

        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return new CellBaseDataResult(mongoDBCollection.find(bson, options));
    }

    public Iterator iterator(Query query, QueryOptions options) {
        return null;
    }

    public Iterator nativeIterator(Query query, QueryOptions options) {
        return null;
    }

//    @Override
//    public CellBaseDataResult rank(Query query, String field, int numResults, boolean asc) {
//        return null;
//    }

    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
        return null;
    }

    public void forEach(Query query, Consumer action, QueryOptions options) {

    }

    public CellBaseDataResult groupBy(Query query, List fields, QueryOptions options) {
        return null;
    }

    public CellBaseDataResult next(Query query, QueryOptions options) {
        return null;
    }

    public CellBaseDataResult nativeNext(Query query, QueryOptions options) {
        return null;
    }

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

    @Override
    public CellBaseIterator iterator(AbstractQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<Long> count(AbstractQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(String field, AbstractQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<FacetField> aggregationStats(List fields, AbstractQuery query) {
        return null;
    }
}
