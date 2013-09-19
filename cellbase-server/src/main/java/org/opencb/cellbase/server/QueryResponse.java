package org.opencb.cellbase.server;

import org.opencb.cellbase.core.lib.dbquery.DBObjectMap;

import java.util.HashMap;
import java.util.Map;

public class QueryResponse extends DBObjectMap {

    private static final long serialVersionUID = -2978952531219554024L;

    public QueryResponse() {
        initialize();
    }

    public QueryResponse(int size) {
        super(size);
        initialize();
    }

    public QueryResponse(String key, Object value) {
        super(key, value);
        initialize();
    }

    private void initialize() {
        this.put("dbVersion", "v3");
        this.put("apiVersion", "v2");
        this.put("time", "");
        this.put("warningMsg", "");
        this.put("errorMsg", "");
    }

}
