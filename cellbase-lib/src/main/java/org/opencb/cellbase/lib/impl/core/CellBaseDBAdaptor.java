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

import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CellBaseDBAdaptor extends MongoDBAdaptor {

    protected List<DataRelease> dataReleases;
    protected Map<Integer, MongoDBCollection> mongoDBCollectionByRelease;

    public static final String DATA_RELEASE_SEPARATOR = "__v";

    public static String buildCollectionName(String data, int release) {
        String name = data + DATA_RELEASE_SEPARATOR + release;
        return name;
    }

    public Map<Integer, MongoDBCollection> buildCollectionByReleaseMap(String data) {
        Map<Integer, MongoDBCollection> collectionMap = new HashMap<>();
        for (DataRelease dataRelease : dataReleases) {
            if (dataRelease.getCollections().containsKey(data)) {
                String collectionName = dataRelease.getCollections().get(data);
                collectionMap.put(dataRelease.getRelease(), mongoDataStore.getCollection(collectionName));
                if (dataRelease.isActiveByDefault()) {
                    collectionMap.put(0, mongoDataStore.getCollection(collectionName));
                }
            }
        }
        return collectionMap;
    }

    public MongoDBCollection getCollectionByRelease(Map<Integer, MongoDBCollection> collectionMap, Integer dataRelease) {
        int release = dataRelease == null ? 0 : dataRelease;
        if (!collectionMap.containsKey(release)) {
            logger.error("Data release (" + release + ") not found!!");
            return null;
        }
        return collectionMap.get(release);
    }

    public CellBaseDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);
        this.dataReleases = new ReleaseMongoDBAdaptor(mongoDataStore).getAll().getResults();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CellBaseDBAdaptor{");
        sb.append("dataReleasse=").append(dataReleases);
        sb.append('}');
        return sb.toString();
    }

    public List<DataRelease> getDataReleases() {
        return dataReleases;
    }

    public CellBaseDBAdaptor setDataReleases(List<DataRelease> dataReleases) {
        this.dataReleases = dataReleases;
        return this;
    }
}
