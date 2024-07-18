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

import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

public class CellBaseDBAdaptor extends MongoDBAdaptor {

    public static final String DATA_RELEASE_SEPARATOR = "__v";

    public CellBaseDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);
    }

    public static String buildCollectionName(String data, int release) {
        String name = data + DATA_RELEASE_SEPARATOR + release;
        return name;
    }

    public MongoDBCollection getMongoDBCollection(String data, int release) throws CellBaseException {
        return DataReleaseSingleton.getInstance().getMongoDBCollection(mongoDataStore.getDatabaseName(), data, release);
    }
}
