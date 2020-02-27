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

import org.bson.BsonDocument;
import org.bson.Document;
import org.opencb.cellbase.core.api.core.CellBaseDBAdaptor;
import org.opencb.cellbase.core.api.queries.AbstractQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by fjlopez on 07/06/16.
 */
public class MetaMongoDBAdaptor extends MongoDBAdaptor implements CellBaseDBAdaptor<Document> {

    public MetaMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);

        mongoDBCollection = mongoDataStore.getCollection("metadata");

        logger.debug("MetaMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public CellBaseDataResult<Long> update(List objectList, String field, String[] innerFields) {
        return null;
    }

    @Override
    public CellBaseDataResult<Long> count(Query query) {
        return new CellBaseDataResult<>(mongoDBCollection.count());
    }

    @Override
    public CellBaseDataResult distinct(Query query, String field) {
        return null;
    }

//    @Override
//    public CellBaseDataResult stats(Query query) {
//        return null;
//    }

    @Override
    public CellBaseDataResult get(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult nativeGet(AbstractQuery query) {
        return new CellBaseDataResult<>(mongoDBCollection.find(new BsonDocument(), null));
    }

    @Override
    public CellBaseDataResult nativeGet(Query query, QueryOptions inputOptions) {
        QueryOptions options = addPrivateExcludeOptions(new QueryOptions(inputOptions));
        return new CellBaseDataResult<>(mongoDBCollection.find(new BsonDocument(), options));
    }

    @Override
    public Iterator iterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        return null;
    }

//    @Override
//    public CellBaseDataResult rank(Query query, String field, int numResults, boolean asc) {
//        return null;
//    }

    @Override
    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(Query query, List fields, QueryOptions options) {
        return null;
    }

    @Override
    public void forEach(Query query, Consumer action, QueryOptions options) { }
}
