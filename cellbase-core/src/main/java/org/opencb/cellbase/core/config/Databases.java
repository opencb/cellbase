/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.core.config;

import java.util.Map;

/**
 * Created by imedina on 16/09/16.
 */
public class Databases {

    private DatabaseCredentials mongodb;
    private Map<String, DatabaseCredentials> neo4j;

    public Databases() {
    }

    public Databases(DatabaseCredentials mongodb, Map<String, DatabaseCredentials> neo4j) {
        this.mongodb = mongodb;
        this.neo4j = neo4j;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Databases{");
        sb.append("mongodb=").append(mongodb);
        sb.append(", neo4j=").append(neo4j);
        sb.append('}');
        return sb.toString();
    }

    public DatabaseCredentials getMongodb() {
        return mongodb;
    }

    public Databases setMongodb(DatabaseCredentials mongodb) {
        this.mongodb = mongodb;
        return this;
    }

    public Map<String, DatabaseCredentials> getNeo4j() {
        return neo4j;
    }

    public Databases setNeo4j(Map<String, DatabaseCredentials> neo4j) {
        this.neo4j = neo4j;
        return this;
    }
}
