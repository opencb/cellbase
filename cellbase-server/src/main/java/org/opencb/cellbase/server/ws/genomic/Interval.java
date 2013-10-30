package org.opencb.cellbase.server.ws.genomic;

@Deprecated
public class Interval {
	public int start;
	public int end;
	public float value;
	
	public Interval(int start, int end, float value) {
		this.start = start;
		this.end = end;
		this.value = value;
	}
}
