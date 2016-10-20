package org.opencb.cellbase.core.cache;

import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.commons.datastore.core.QueryResult;

/**
 * Created by imedina on 20/10/16.
 */
public class CacheManager {


    public CacheManager() {
    }

    public CacheManager(CellBaseConfiguration configuration) {


    }


    public <T> QueryResult<T> get(String key, Class<T> clazz) {

        return null;
    }

    public void set(QueryResult queryResult) {

    }

}
