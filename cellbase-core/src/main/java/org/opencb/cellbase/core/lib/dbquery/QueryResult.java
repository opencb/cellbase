package org.opencb.cellbase.core.lib.dbquery;


public class QueryResult extends DBObjectMap {


	private static final long serialVersionUID = 4427749106006400267L;

	
	public QueryResult() {
		initialize();
	}

	public QueryResult(int size) {
		super(size);
		initialize();
	}

	public QueryResult(String key, Object value) {
		super(key, value);
		initialize();
	}
	

	private void initialize() {
		this.put("dbTime", "");
		this.put("time", "");
		this.put("numResults", "");
		this.put("warningMsg", "");
		this.put("errorMsg", "");
		this.put("featureType", "");
		this.put("resultType", "");
		this.put("result", "");
	}
	
	
	/**
	 * Some shortcuts methods for most common attributes
	 * 
	 */
	public Object getDBTime() {
		return this.get("dbTime");
	}
	
	public void setDBTime(Object value) {
		this.put("dbTime", value);
	}
	
	
	public Object getResult() {
		return this.get("result");
	}
	
	public void setResult(Object value) {
		this.put("result", value);
	}
	
	
	public int getNumResults() {
		return this.getInt("numResults");
	}
	
	public void setNumResults(int value) {
		this.put("numResults", value);
	}
	
}
