package org.opencb.cellbase.core.cache;

import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.KryoCodec;


/**
 * Created by imedina on 20/10/16.
 */
public class CacheManager {
    private CellBaseConfiguration cellBaseConfiguration;
    private Config redissonConfig;
    private RedissonClient redissonClient;


    public CacheManager() {
    }

    public CacheManager(CellBaseConfiguration configuration) {

        if (configuration != null) {
            String codec = configuration.getCache().getSerialization();
            redissonConfig = new Config();
            redissonConfig.useSingleServer().setAddress(configuration.getCache().getHost());

            if ("Kryo".equalsIgnoreCase(codec)) {
                redissonConfig.setCodec(new KryoCodec());
            } else if ("JSON".equalsIgnoreCase(codec)) {
                redissonConfig.setCodec(new JsonJacksonCodec());
            }
            this.redissonClient = Redisson.create(redissonConfig);
        }

    }


    public <T> QueryResult<T> get(String key, Class<T> clazz) {
        //redissonClient.getMap()
        //if empty return null else type cast object
        return null;
    }

    public void set(String key, Query query, QueryResult queryResult) {
//  redissonClient.getMap(key);
        // insert the object into map with key and query and queryResult
    }

    public String createKey(String species, String collection, String subcategory, Query query, QueryOptions queryOptions) {
        String key = "";
        if (queryOptions.getBoolean("cache", false)) {
            key = "set";
            // cellBase
            // version We get from CellBaseConfiguration
            // CB:version:species:collection
            // Sort query and queryOptions

            // get SHA1

        }
        return key;
    }
}
