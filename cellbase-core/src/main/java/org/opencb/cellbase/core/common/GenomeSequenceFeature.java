package org.opencb.cellbase.core.common;

public class GenomeSequenceFeature {

	private String chromosome;
	private int start;
	private int end;
	private int strand;
	private String sequence;

	public GenomeSequenceFeature(String chromosome, int start, int end, String sequence){
		this.chromosome = chromosome;
		this.start = start;
		this.end = end;
		this.strand = 1;
		this.sequence = sequence;
	}
	
	public GenomeSequenceFeature(String chromosome, int start, int end, int strand, String sequence){
		this.chromosome = chromosome;
		this.start = start;
		this.end = end;
		this.strand = strand;
		this.sequence = sequence;
	}

	public int getStart() {
		return start;
	}

	public String getChromosome() {
		return chromosome;
	}

	public int getEnd() {
		return end;
	}
	
	public int getStrand() {
		return strand;
	}
	
	public void setStrand(int strand) {
		this.strand = strand;
	}

	public String getSequence() {
		return sequence;
	}
	
	public void setSequence(String value) {
		sequence = value;
	}
}
