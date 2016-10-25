package org.opencb.cellbase.core.cache;

import java.util.*;
import java.util.regex.Pattern;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.core.RMap;
import org.redisson.core.RKeys;
import org.redisson.RedissonClient;
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

    private final String dataBase = "cb:";
    private CellBaseConfiguration cellBaseConfiguration;
    private Config redissonConfig;
    private RedissonClient redissonClient;
    private boolean redisState;

    public CacheManager() {
    }

    public CacheManager(CellBaseConfiguration configuration) {

        if (configuration != null && configuration.getCache() != null) {
            this.cellBaseConfiguration = configuration;
            redissonConfig = new Config();
            redissonConfig.useSingleServer().setAddress(configuration.getCache().getHost());
            String codec = configuration.getCache().getSerialization();
            redisState = true;

            if ("Kryo".equalsIgnoreCase(codec)) {
                redissonConfig.setCodec(new KryoCodec());
            } else if ("JSON".equalsIgnoreCase(codec)) {
                redissonConfig.setCodec(new JsonJacksonCodec());
            }
        }
    }


    public <T> QueryResult<T> get(String key, Class<T> clazz) {

        long start = System.currentTimeMillis();
        redissonClient = Redisson.create(redissonConfig);
        RMap<Integer, Map<String, Object>> map = redissonClient.getMap(key);
        Map<Integer, Map<String, Object>> result = new HashMap<Integer, Map<String, Object>>();
        QueryResult<T> queryResult = new QueryResult<T>();
        Set<Integer> set = new HashSet<Integer>(Arrays.asList(0));

        try {
            result = map.getAll(set);
        } catch (RedisConnectionException e) {
            redisState = false;
            queryResult.setWarningMsg("Unable to connect to Redis Cache, Please query WITHOUT Cache (Falling back to Database)");
            return queryResult;
        }

        if (!result.isEmpty()) {
            Object resultMap= result.get(0).get("result");
            queryResult =(QueryResult<T>) resultMap;
            queryResult.setDbTime((int) (System.currentTimeMillis() - start));
        }
        redissonClient.shutdown();
        return queryResult;
    }

    public void set(String key, Query query, QueryResult queryResult) {

        if (queryResult.getDbTime() > cellBaseConfiguration.getCache().getSlowThreshold()) {
            redissonClient = Redisson.create(redissonConfig);
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
            redissonClient.shutdown();
        }
    }

    public String createKey(String species, String subcategory, Query query, QueryOptions queryOptions) {

        queryOptions.remove("cache");
        StringBuilder key = new StringBuilder(dataBase);
        key.append(cellBaseConfiguration.getVersion()).append(":").append(species).append(":")
                    .append(subcategory);
        SortedMap<String, SortedSet<Object>> map = new TreeMap<String, SortedSet<Object>>();

        for (String item: query.keySet()) {
            map.put(item.toLowerCase(), new TreeSet<Object>(query.getAsStringList(item)));
        }

        for (String item: queryOptions.keySet()) {
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
        redissonClient = Redisson.create(redissonConfig);
        RKeys redisKeys = redissonClient.getKeys();
        redisKeys.deleteByPattern(dataBase + "*");
        redissonClient.shutdown();
    }

    public void clear(Pattern pattern) {
        redissonClient = Redisson.create(redissonConfig);
        RKeys redisKeys = redissonClient.getKeys();
        redisKeys.deleteByPattern(pattern.toString());
        redissonClient.shutdown();
    }

}
