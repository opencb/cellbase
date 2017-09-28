package org.opencb.cellbase.core.cache;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.config.CacheProperties;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.redisson.Redisson;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.KryoCodec;
import org.redisson.config.Config;

import java.util.*;
import java.util.regex.Pattern;


/**
 * Created by imedina on 20/10/16.
 */
public class CacheManager {

    private final String DATABASE = "cb:";
    private CellBaseConfiguration cellBaseConfiguration;
    private Config redissonConfig;
    private static RedissonClient redissonClient;
    private CacheProperties cache;
    private boolean redisState;

    public CacheManager() {
    }

    public CacheManager(CellBaseConfiguration configuration) {

        if (configuration != null && configuration.getCache() != null) {
            this.cellBaseConfiguration = configuration;
            cache = configuration.getCache();
            redissonConfig = new Config();
            redisState = true;
            String host = (StringUtils.isNotEmpty(cache.getHost()))
                    ? cache.getHost() : cache.DEFAULT_HOST;
            redissonConfig.useSingleServer().setAddress("redis://" + host);
            String codec = (StringUtils.isNotEmpty(cache.getSerialization()))
                    ? cache.getSerialization() : cache.DEFAULT_SERIALIZATION;
            if ("Kryo".equalsIgnoreCase(codec)) {
                redissonConfig.setCodec(new KryoCodec());
            } else if ("JSON".equalsIgnoreCase(codec)) {
                redissonConfig.setCodec(new JsonJacksonCodec());
            }
        }
    }

    public <T> QueryResult<T> get(String key, Class<T> clazz) {

        QueryResult<T> queryResult = new QueryResult<T>();
        if (isActive()) {
            long start = System.currentTimeMillis();
            RMap<Integer, Map<String, Object>> map = getRedissonClient().getMap(key);
            Map<Integer, Map<String, Object>> result = new HashMap<Integer, Map<String, Object>>();
            Set<Integer> set = new HashSet<Integer>(Arrays.asList(0));

            try {
                result = map.getAll(set);
            } catch (RedisConnectionException e) {
                redisState = false;
                queryResult.setWarningMsg("Unable to connect to Redis Cache, Please query WITHOUT Cache (Falling back to Database)");
                return queryResult;
            }
            if (!result.isEmpty()) {
                queryResult = (QueryResult<T>) result.get(0).get("result");
                // we are getting two objects, second one is query, not used at the moment
                queryResult.setWarningMsg("Data is originated from Redis Cache !!!");
                queryResult.setDbTime((int) (System.currentTimeMillis() - start));
            }
        }
        return queryResult;
    }

    public void set(String key, Query query, QueryResult queryResult) {

        if (isActive() && queryResult.getDbTime() > cache.getSlowThreshold()) {
            RMap<Integer, Map<String, Object>> map = getRedissonClient().getMap(key);
            Map<String, Object> record = new HashMap<String, Object>();
            record.put("query", query);
            record.put("result", queryResult);
            try {
                map.fastPut(0, record);
            } catch (RedisConnectionException e) {
                redisState = false;
                queryResult.setWarningMsg("Unable to connect to Redis Cache, Please query WITHOUT Cache (Falling back to Database)");
            }
        }
    }

    public String createKey(String species, String subcategory, Query query, QueryOptions queryOptions) {

        queryOptions.remove("cache");
        StringBuilder key = new StringBuilder(DATABASE);
        key.append(cellBaseConfiguration.getVersion()).append(":").append(species).append(":")
                .append(subcategory);
        SortedMap<String, SortedSet<Object>> map = new TreeMap<String, SortedSet<Object>>();
        for (String item : query.keySet()) {
            map.put(item.toLowerCase(), new TreeSet<Object>(query.getAsStringList(item)));
        }
        for (String item : queryOptions.keySet()) {
            map.put(item.toLowerCase(), new TreeSet<Object>(queryOptions.getAsStringList(item)));
        }
        String sha1 = DigestUtils.sha1Hex(map.toString());
        key.append(":").append(sha1);
        queryOptions.add("cache", "true");
        return key.toString();
    }

    public boolean isActive() {
        return cache.isActive() && redisState;
    }

    public void clear() {
        RKeys redisKeys = getRedissonClient().getKeys();
        redisKeys.deleteByPattern(DATABASE + "*");
    }

    public void clear(Pattern pattern) {
        RKeys redisKeys = getRedissonClient().getKeys();
        redisKeys.deleteByPattern(pattern.toString());
    }

    public void close() {
        if (redissonClient != null) {
            redissonClient.shutdown();
            redissonClient = null;
        }
    }

    private synchronized RedissonClient getRedissonClient() {
        if (redissonClient == null) {
            redissonClient = Redisson.create(redissonConfig);
        }
        return redissonClient;
    }
}
