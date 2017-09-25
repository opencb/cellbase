package org.opencb.cellbase.core.cache;

import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.config.CacheProperties;
import org.redisson.Redisson;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.codec.KryoCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.client.RedisConnectionException;
import org.opencb.commons.datastore.core.Query;
import org.apache.commons.codec.digest.DigestUtils;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.cellbase.core.config.CellBaseConfiguration;


/**
 * Created by imedina on 20/10/16.
 */
public class CacheManager {

    private final String DATABASE = "cb:";
    private CellBaseConfiguration cellBaseConfiguration;
    private Config redissonConfig;
    private RedissonClient redissonClient;
    private boolean redisState;

    public CacheManager() {
    }

    public CacheManager(CellBaseConfiguration configuration) {

        CacheProperties cache;

        if (configuration != null && configuration.getCache() != null) {
            this.cellBaseConfiguration = configuration;
            cache = configuration.getCache();
            redissonConfig = new Config();
            redisState = true;

            String host = (StringUtils.isNotEmpty(cache.getHost()))
                    ? cache.getHost()
                    : CacheProperties.DEFAULT_HOST;
            redissonConfig.useSingleServer().setAddress("redis://" + host);

            String codec = (StringUtils.isNotEmpty(cache.getSerialization()))
                    ? cache.getSerialization()
                    : CacheProperties.DEFAULT_SERIALIZATION;


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
            redissonClient = getRedissonClient();
            RMap<Integer, Map<String, Object>> map = redissonClient.getMap(key);
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
                Object resultMap = result.get(0).get("result");
                queryResult = (QueryResult<T>) resultMap;
                queryResult.setDbTime((int) (System.currentTimeMillis() - start));
            }
        }
        return queryResult;
    }

    public void set(String key, Query query, QueryResult queryResult) {

        if (isActive() && queryResult.getDbTime() > cellBaseConfiguration.getCache().getSlowThreshold()) {
            redissonClient = getRedissonClient();
            RMap<Integer, Map<String, Object>> map = redissonClient.getMap(key);
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
        return cellBaseConfiguration.getCache().isActive() && redisState;
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
