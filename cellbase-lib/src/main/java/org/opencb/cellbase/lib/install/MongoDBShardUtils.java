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

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.opencb.cellbase.core.common.Species;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.MongoDBDatabaseCredentials;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            LoggerFactory.getLogger(MongoDBShardUtils.class).warn("No config found for '" + species.getSpecies() + "'");
            return;
        }

        List<SpeciesConfiguration.ShardConfig> shards = speciesConfiguration.getShards();
        if (shards == null) {
            LoggerFactory.getLogger(MongoDBShardUtils.class).error("No sharding config found for '" + species.getSpecies() + "'");
            return;
        }

        for (SpeciesConfiguration.ShardConfig shardConfig : shards) {
            // create the collection, if it's there already do nothing
            String collectionName = createCollection(mongoDataStore, shardConfig);

            // set the keymap, e.g. chromosome, start, end. Also can be a single key
            Map<String, Object> keyMap = createKeyMap(shardConfig);

            // shard keys must be indexed FIRST
            createIndex(mongoDataStore, keyMap, collectionName);

            String databaseName = mongoDataStore.getDatabaseName();
            String fullCollectionName = mongoDataStore.getDatabaseName() + "." + collectionName;
            MongoClient mongoClient = mongoDataStore.getMongoClient();
            MongoDatabase adminDB = mongoClient
                    .getDatabase(cellBaseConfiguration.getDatabases().getMongodb().getOptions().get("authenticationDatabase"));

            // sh.enableSharding( "cellbase_hsapiens_grch37_v4" )
//            adminDB.runCommand(new Document("enableSharding", databaseName));

            // sh.shardCollection("cellbase_hsapiens_grch37_v4.variation", { "chromosome": 1, "start": 1, "end": 1 } )
            adminDB.runCommand(new Document("shardcollection", fullCollectionName).append("key", new Document(keyMap)));

            MongoDBDatabaseCredentials databaseCredentials = cellBaseConfiguration.getDatabases().getMongodb();
            List<MongoDBDatabaseCredentials.ReplicaSet> replicaSets = databaseCredentials.getShards();

            if (replicaSets == null || replicaSets.isEmpty()) {
                LoggerFactory.getLogger(MongoDBShardUtils.class).warn("No replicaset config found for '" + species.getSpecies() + "'");
                return;
            }

            // different from our shard key, this is the key used for the zones ONLY
            final String rangeKey = shardConfig.getRangeKey();

            int i = 0;
            for (SpeciesConfiguration.Zone zone : shardConfig.getZones()) {
                MongoDBDatabaseCredentials.ReplicaSet replicaSet = replicaSets.get(i++);

                // sh.addShard( "rs0/cb-mongo-shard1-1:27017,cb-mongo-shard1-2:27017,cb-mongo-shard1-3:27017" )
//                String replicaSetName = replicaSet.getId() + "/" + replicaSet.getNodes();
//                adminDB.runCommand(new Document("addShard", replicaSetName));

                // sh.addShardToZone("rs0", "zone0")
                adminDB.runCommand(new Document("addShardToZone", replicaSet.getId()).append("zone", zone.getName()));

                // put chromosome 1 in shard0
                //sh.addTagRange("cellbase_hsapiens_grch37_v4.variation", { "chromosome" :  "1" },  { "chromosome" :  "10"  }, "zone0" )
                List<SpeciesConfiguration.ShardRange> shardRanges = zone.getShardRanges();
                for (SpeciesConfiguration.ShardRange shardRange : shardRanges) {
                    adminDB.runCommand(new Document("updateZoneKeyRange", fullCollectionName)
                            .append("min", new Document(rangeKey, shardRange.getMinimum()))
                            .append("max", new Document(rangeKey, shardRange.getMaximum()))
                            .append("zone", zone.getName()));
                }
            }
        }
    }

    private static String createCollection(MongoDataStore mongoDataStore, SpeciesConfiguration.ShardConfig shardConfig)
            throws CellbaseException {
        String collectionName = shardConfig.getCollection();
        if (StringUtils.isEmpty(collectionName)) {
            throw new CellbaseException("Sharding failed: collection name not found in config");
        }
        if (mongoDataStore.getCollection(collectionName) == null) {
            mongoDataStore.createCollection(collectionName);
        }
        return collectionName;
    }

    private static void createIndex(MongoDataStore mongoDataStore, Map<String, Object> keyMap, String collectionName) {
        HashMap<String, String> options = new HashMap<>();
        options.put("background", "true");
        Map<String, ObjectMap> indexes = new HashMap<>();
        indexes.put("fields", new ObjectMap((Map) keyMap));
        indexes.put("options", new ObjectMap((Map) options));
        // FIXME We need to correctly call to MongoDBIndexUtils
//        MongoDBIndexUtils mongoDBIndexUtils = new MongoDBIndexUtils(mongoDataStore, null);
//        MongoDBIndexUtils.createIndexes(mongoDataStore, Collections.singletonList(indexes), false);
    }

    private static Map<String, Object> createKeyMap(SpeciesConfiguration.ShardConfig shardConfig) {
        List<String> keys = shardConfig.getKey();
        Map<String, Object> keyMap = new HashMap<>();
        for (String key : keys) {
            keyMap.put(key, 1);
        }
        return keyMap;
    }
}
