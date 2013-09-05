package org.opencb.cellbase.core.common.core;

public class DBName {

	private String dbNameShort;
	private String dbName;
	
	public DBName(String dbNameShort, String dbName) {
		this.setDbNameShort(dbNameShort);
		this.setDbName(dbName);
	}

	public String getDbNameShort() {
		return dbNameShort;
	}

	public void setDbNameShort(String dbNameShort) {
		this.dbNameShort = dbNameShort;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	
	
}
