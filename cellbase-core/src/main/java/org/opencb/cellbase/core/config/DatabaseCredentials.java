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
public class DatabaseCredentials {

    private String host;
    private String user;
    private String password;
    private Map<String, String> options;
    private List<ReplicaSet> replicaSets;


    public DatabaseCredentials() {
    }

    public DatabaseCredentials(String host, String user, String password, List<ReplicaSet> replicaSets, Map<String, String> options) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.options = options;
        this.replicaSets = replicaSets;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatabaseProperties{");
        sb.append("host='").append(host).append('\'');
        sb.append(", user='").append(user).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", replicaSets='").append(replicaSets).append('\'');
        sb.append(", options=").append(options);
        sb.append('}');
        return sb.toString();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<ReplicaSet> getReplicaSets() {
        return replicaSets;
    }

    public DatabaseCredentials setReplicaSets(List<ReplicaSet> replicaSets) {
        this.replicaSets = replicaSets;
        return this;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public static class ReplicaSet {
        private String name;
        private String nodes;

        /**
         * @return the replicaset name, e.g. rs0
         */
        public String getName() {
            return name;
        }

        /**
         * @param name label for the replicaset, e.g. rs0
         * @return the replicaset of interest
         */
        public ReplicaSet setName(String name) {
            this.name = name;
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
