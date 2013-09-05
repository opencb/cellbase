package org.opencb.cellbase.core.common.core;


public class Cytoband {

	private String name;
	private String stain;
	private int start;
	private int end;
	
	public Cytoband() {

	}

	public Cytoband(String name, String stain, int start, int end) {
		super();
		this.name = name;
		this.stain = stain;
		this.start = start;
		this.end = end;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStain() {
		return stain;
	}

	public void setStain(String stain) {
		this.stain = stain;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
	
	
}
