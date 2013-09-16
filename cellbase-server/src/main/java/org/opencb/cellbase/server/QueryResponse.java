package org.opencb.cellbase.server;

import org.opencb.cellbase.core.lib.dbquery.DBObjectMap;

import java.util.HashMap;
import java.util.Map;

public class QueryResponse extends DBObjectMap {

	private static final long serialVersionUID = -2978952531219554024L;

	private Map<String, Object> metadata;

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
		metadata = new HashMap<String, Object>();
		metadata.put("dbVersion", "v3");
		metadata.put("apiVersion", "v2");
		metadata.put("time", "");
		metadata.put("warningMsg", "");
		metadata.put("errorMsg", "");
		this.put("metadata", metadata);
		
//		this.put("result", "");
	}
	
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
	
	
}
