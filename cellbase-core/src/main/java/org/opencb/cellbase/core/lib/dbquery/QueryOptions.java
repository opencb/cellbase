package org.opencb.cellbase.core.lib.dbquery;


public class QueryOptions extends DBObjectMap {


	private static final long serialVersionUID = -6331081481906004636L;

	
	public QueryOptions() {
		
	}
	
	public QueryOptions(String key, Object value) {
		super(key, value);
	}
	
	public QueryOptions(String json) {
		super(json);
	}
	
	
}
