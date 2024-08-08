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

package org.opencb.cellbase.lib.impl.core.singleton;

import org.opencb.commons.datastore.mongodb.MongoDBCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseInfo {
    private String dbName;
    private String species;
    private String assembly;
    private ReentrantReadWriteLock rwLock;
    private Map<Integer, Map<String, MongoDBCollection>> cacheData;

    public DatabaseInfo() {
        this.rwLock = new ReentrantReadWriteLock();
        this.cacheData = new HashMap<>();
    }

    public DatabaseInfo(String dbName, String species, String assembly, ReentrantReadWriteLock rwLock, Map<Integer,
            Map<String, MongoDBCollection>> cacheData) {
        this.dbName = dbName;
        this.species = species;
        this.assembly = assembly;
        this.rwLock = rwLock;
        this.cacheData = cacheData;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatabaseInfo{");
        sb.append("dbName='").append(dbName).append('\'');
        sb.append(", species='").append(species).append('\'');
        sb.append(", assembly='").append(assembly).append('\'');
        sb.append(", rwLock=").append(rwLock);
        sb.append(", cacheData=").append(cacheData);
        sb.append('}');
        return sb.toString();
    }

    public String getDbName() {
        return dbName;
    }

    public DatabaseInfo setDbName(String dbName) {
        this.dbName = dbName;
        return this;
    }

    public String getSpecies() {
        return species;
    }

    public DatabaseInfo setSpecies(String species) {
        this.species = species;
        return this;
    }

    public String getAssembly() {
        return assembly;
    }

    public DatabaseInfo setAssembly(String assembly) {
        this.assembly = assembly;
        return this;
    }

    public ReentrantReadWriteLock getRwLock() {
        return rwLock;
    }

    public DatabaseInfo setRwLock(ReentrantReadWriteLock rwLock) {
        this.rwLock = rwLock;
        return this;
    }

    public Map<Integer, Map<String, MongoDBCollection>> getCacheData() {
        return cacheData;
    }

    public DatabaseInfo setCacheData(Map<Integer, Map<String, MongoDBCollection>> cacheData) {
        this.cacheData = cacheData;
        return this;
    }
}
