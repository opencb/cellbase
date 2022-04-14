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

import org.apache.commons.collections4.MapUtils;
import org.opencb.cellbase.core.release.DataRelease;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

public class CellBaseDBAdaptor extends MongoDBAdaptor{

    protected DataRelease dataRelease;

    public static final String DATA_RELEASE_SEPARATOR = "__v";

    public static String buildCollectionName(String data, int release) {
        String name = data + DATA_RELEASE_SEPARATOR + release;
        return name;
    }

    public String getCollectionName(String data) {
        if (dataRelease == null || MapUtils.isEmpty(dataRelease.getCollections()) || !dataRelease.getCollections().containsKey(data)) {
            return data;
        } else {
            return dataRelease.getCollections().get(data);
        }
    }

    public CellBaseDBAdaptor(DataRelease dataRelease, MongoDataStore mongoDataStore) {
        super(mongoDataStore);
        this.dataRelease = dataRelease;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CellBaseDBAdaptor{");
        sb.append("dataRelease=").append(dataRelease);
        sb.append('}');
        return sb.toString();
    }

    public DataRelease getDataRelease() {
        return dataRelease;
    }

    public CellBaseDBAdaptor setDataRelease(DataRelease dataRelease) {
        this.dataRelease = dataRelease;
        return this;
    }
}
