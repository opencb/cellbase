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
import org.opencb.cellbase.core.api.core.CellBaseMongoDBAdaptor;
import org.opencb.cellbase.core.api.queries.AbstractQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.FacetField;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.Iterator;
import java.util.List;

/**
 * Created by fjlopez on 07/06/16.
 */
public class MetaMongoDBAdaptor extends MongoDBAdaptor implements CellBaseMongoDBAdaptor {

    public MetaMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("metadata");
        logger.debug("MetaMongoDBAdaptor: in 'constructor'");
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
    public Iterator iterator(AbstractQuery query) {
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
