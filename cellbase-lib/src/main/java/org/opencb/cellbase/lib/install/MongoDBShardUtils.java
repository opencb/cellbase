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

package org.opencb.cellbase.lib.install;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang.StringUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.mongodb.MongoDBIndexUtils;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;

import org.opencb.cellbase.core.common.Species;
import org.slf4j.LoggerFactory;

public class MongoDBShardUtils {

    /**
     * Add shards.
     *
     * @param mongoDataStore Database name
     * @param cellBaseConfiguration config file with database details.
     * @param species the species name and assembly for the database being sharded
     * @throws CellbaseException if configuration isn't valid
     */
    public static void shard(MongoDataStore mongoDataStore, CellBaseConfiguration cellBaseConfiguration, Species species)
            throws CellbaseException {
        SpeciesConfiguration speciesConfiguration = cellBaseConfiguration.getSpeciesConfig(species.getSpecies());
        if (speciesConfiguration == null) {
            LoggerFactory.getLogger(MongoDBShardUtils.class).warn("No sharding config found for '" + species.getSpecies() + "'");
            return;
        }

        List<SpeciesConfiguration.ShardConfig> shards = speciesConfiguration.getShards();
        for (SpeciesConfiguration.ShardConfig shardConfig : shards) {

            // create collection
            String collectionName = shardConfig.getCollection();
            if (StringUtils.isEmpty(collectionName)) {
                throw new CellbaseException("Sharding failed: collection name not found in config");
            }
            mongoDataStore.createCollection(collectionName);

            // create index
            List<String> keys = shardConfig.getKey();
            Map<String, ObjectMap> indexes = new HashMap<>();
            Map<String, Integer> keyMap = new HashMap<>();
            for (String key : keys) {
                keyMap.put(key, 1);
            }
            HashMap<String, String> options = new HashMap<>();
            options.put("background", "true");

            indexes.put("fields", new ObjectMap((Map) keyMap));
            indexes.put("options", new ObjectMap((Map) options));
            MongoDBIndexUtils.createIndex(mongoDataStore, collectionName, Arrays.asList(indexes));

            int shardCount = shardConfig.getNumberOfShards();
            String rangeKey = shardConfig.getRangeKey();

            String fullCollectionName = mongoDataStore.getDatabaseName() + "." + collectionName;

            MongoDatabase adminDB = mongoDataStore.getAdminDB();

            // shard the collection
            DBObject cmd = new BasicDBObject("shardCollection", fullCollectionName).
                    append("key", new BasicDBObject(keyMap));
//                    adminDB.(cmd);

            // TODO add ranges
        }
    }
}
