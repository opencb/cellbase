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

package org.opencb.cellbase.core.config;

import java.util.List;
import java.util.Map;

/**
 * Created by imedina on 19/08/16.
 */
public class MongoDBDatabaseCredentials extends DatabaseCredentials {

    private List<ReplicaSet> shards;
    private String host;
    private String user;
    private String password;
    private Map<String, String> options;

    public MongoDBDatabaseCredentials() {
    }

    public MongoDBDatabaseCredentials(String host, String user, String password, List<ReplicaSet> shards, Map<String, String> options) {
        super(host, user, password, options);
        this.shards = shards;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatabaseProperties{");
        sb.append("host='").append(host).append('\'');
        sb.append(", user='").append(user).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", replicaSets='").append(shards).append('\'');
        sb.append(", options=").append(options);
        sb.append('}');
        return sb.toString();
    }

    public List<ReplicaSet> getShards() {
        return shards;
    }

    public MongoDBDatabaseCredentials setShards(List<ReplicaSet> shards) {
        this.shards = shards;
        return this;
    }

    public static class ReplicaSet {
        private String id;
        private String nodes;

        /**
         * @return the replicaset name, e.g. rs0
         */
        public String getId() {
            return id;
        }

        /**
         * @param id label for the replicaset, e.g. rs0
         * @return the replicaset of interest
         */
        public ReplicaSet setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * @return nodes for replica set, e.g. cb-mongo-shard1-1:27017,cb-mongo-shard1-2:27017,cb-mongo-shard1-3:27017
         */
        public String getNodes() {
            return nodes;
        }

        /**
         * @param nodes nodes for replica set, e.g. cb-mongo-shard1-1:27017,cb-mongo-shard1-2:27017,cb-mongo-shard1-3:27017
         * @return nodes for this replica set
         */
        public ReplicaSet setNodes(String nodes) {
            this.nodes = nodes;
            return this;
        }
    }
}
