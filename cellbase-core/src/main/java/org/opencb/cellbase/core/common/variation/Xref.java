package org.opencb.cellbase.core.common.variation;

public class Xref {

	private String id; 			//1
	private String source; 		//2
	private String version; 	//3

	public Xref(String id, String source) {
		this(id, source, "");
	}
	
	public Xref(String id, String source, String version) {
		this.id = id;
		this.source = source;
		this.version = version;
	}
	

	public String getDataBase() {
		return source;
	}

	public void setDataBase(String dataBase) {
		this.source = dataBase;
	}
	

	public String getCrossReference() {
		return id;
	}

	public void setCrossReference(String crossReference) {
		this.id = crossReference;
	}
	

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	

}
