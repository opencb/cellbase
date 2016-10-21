package org.opencb.cellbase.core.cache;

import org.apache.commons.codec.digest.DigestUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.KryoCodec;

import java.util.*;


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

    public String createKey(String species, String subcategory, Query query, QueryOptions queryOptions) {
        StringBuilder key = new StringBuilder();
        if (queryOptions.getBoolean("cache", false)) {
            key.append("cb:").append(cellBaseConfiguration.getVersion()).append(":").append(species).append(":")
                    .append(subcategory);

            SortedMap<String, SortedSet<Object>> map = new TreeMap<String, SortedSet<Object>>();
            // TODO: remove cache from options
            for (String item: query.keySet()) {
                map.put(item.toLowerCase(), new TreeSet<Object>(Arrays.asList(query.get(item))));
            }

            for (String item: queryOptions.keySet()) {
                map.put(item.toLowerCase(), new TreeSet<Object>(Arrays.asList(queryOptions.get(item))));
            }

            String sha1 = DigestUtils.sha1Hex(map.toString());

            key.append(":").append(sha1);

            // cellBase
            // version We get from CellBaseConfiguration
            // CB:version:species:collection
            // Sort query and queryOptions

            // get SHA1

        }
        return key.toString();
    }
}
