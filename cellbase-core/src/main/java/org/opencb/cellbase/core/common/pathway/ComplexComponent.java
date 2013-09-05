package org.opencb.cellbase.core.common.pathway;

public class ComplexComponent {

	public enum TYPE {Protein, SmallMollecule, DNA, DNARegion, RNA, RNARegion, Unknown};

	private String name;
	private String type;
	private String dbName;
	private String dbId;

	public ComplexComponent(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public static String getHeader() {
		return "#name\ttype\tdb name\tdb id";
	}

	public String toString() {
		return name + "\t" + type + "\t" + dbName + "\t" + dbId;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getDbId() {
		return dbId;
	}
	public void setDbId(String dbId) {
		this.dbId = dbId;
	}	

}
