package org.opencb.cellbase.core.cache;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.KryoCodec;
import org.redisson.core.RKeys;
import org.redisson.core.RMap;

import java.util.*;
import java.util.regex.Pattern;


/**
 * Created by imedina on 20/10/16.
 */
public class CacheManager {

    private CellBaseConfiguration cellBaseConfiguration;

    private Config redissonConfig;
    private RedissonClient redissonClient;
    private boolean redisState;

    private static final String PREFIX_DATABASE_KEY = "cb:";

    public CacheManager() {
    }

    public CacheManager(CellBaseConfiguration configuration) {

        if (configuration != null && configuration.getCache() != null) {
            this.cellBaseConfiguration = configuration;

            redissonConfig = new Config();
            String host = (StringUtils.isNotEmpty(configuration.getCache().getHost()))
                    ? configuration.getCache().getHost()
                    : "localhost:6379";
            redissonConfig.useSingleServer().setAddress(host);

            // We read codec from Configuration file, default codec is Kryo
            String codec = configuration.getCache().getSerialization();
            if (StringUtils.isNotEmpty(codec) && "JSON".equalsIgnoreCase(codec)) {
                redissonConfig.setCodec(new JsonJacksonCodec());
            } else {
                redissonConfig.setCodec(new KryoCodec());
            }

            // TODO We need to find out a proper way to discover if REDIS is alive before getting the error from GET
            redisState = true;

            redissonClient = Redisson.create(redissonConfig);
        }
    }


    public <T> QueryResult<T> get(String key) {

        QueryResult<T> queryResult = new QueryResult<>();
        if (isActive()) {
            long start = System.currentTimeMillis();
            RMap<Integer, Map<String, Object>> map = redissonClient.getMap(key);

            try {
                // We only retrieve the first field of the HASH, which is the only one that exist.
                Map<Integer, Map<String, Object>> result = map.getAll(new HashSet<>(Collections.singletonList(0)));

                if (result != null && !result.isEmpty()) {
                    Object resultMap= result.get(0).get("result");
                    queryResult =(QueryResult<T>) resultMap;
                    queryResult.setDbTime((int) (System.currentTimeMillis() - start));
                }
            } catch (RedisConnectionException e) {
                redisState = false;
                queryResult.setWarningMsg("Unable to connect to Redis Cache, Please query WITHOUT Cache (Falling back to Database)");
                return queryResult;
            }
        }
        return queryResult;
    }

    public void set(String key, Query query, QueryResult queryResult) {

        if (isActive()) {
            if (queryResult.getDbTime() >= cellBaseConfiguration.getCache().getSlowThreshold()) {
                RMap<Integer, Map<String, Object>> map = redissonClient.getMap(key);
                Map<String, Object> record = new HashMap<>();
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
    }

    public String createKey(String species, String subcategory, Query query, QueryOptions queryOptions) {

        queryOptions.remove("cache");
        StringBuilder key = new StringBuilder(PREFIX_DATABASE_KEY);
        key.append(cellBaseConfiguration.getVersion()).append(":").append(species).append(":").append(subcategory);
        SortedMap<String, SortedSet<Object>> map = new TreeMap<>();

        for (String item: query.keySet()) {
            map.put(item.toLowerCase(), new TreeSet<>(query.getAsStringList(item)));
        }

        for (String item: queryOptions.keySet()) {
            map.put(item.toLowerCase(), new TreeSet<>(queryOptions.getAsStringList(item)));
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
        RKeys redisKeys = redissonClient.getKeys();
        redisKeys.deleteByPattern(PREFIX_DATABASE_KEY + "*");
    }

    public void clear(Pattern pattern) {
        RKeys redisKeys = redissonClient.getKeys();
        redisKeys.deleteByPattern(pattern.toString());
    }

    public void close() {
        redissonClient.shutdown();
    }

}
