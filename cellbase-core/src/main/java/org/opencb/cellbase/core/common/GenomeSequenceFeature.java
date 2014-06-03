package org.opencb.cellbase.core.common;

public class GenomeSequenceFeature {

	private String sequenceName;
	private int start;
	private int end;
	private int strand;
	private String sequenceType;
	private String assembly;
	private String sequence;

	public GenomeSequenceFeature(String chromosome, int start, int end, String sequence){
		this.sequenceName = chromosome;
		this.start = start;
		this.end = end;
		this.strand = 1;
		this.sequence = sequence;
	}
	
	public GenomeSequenceFeature(String chromosome, int start, int end, int strand, String sequence){
		this.sequenceName = chromosome;
		this.start = start;
		this.end = end;
		this.strand = strand;
		this.sequence = sequence;
	}

    public GenomeSequenceFeature(String chromosome, int start, int end, int strand, String sequenceType, String assembly, String sequence){
        this.sequenceName = chromosome;
        this.start = start;
        this.end = end;
        this.strand = strand;
        this.sequenceType = sequenceType;
        this.assembly = assembly;
        this.sequence = sequence;
    }

	public int getStart() {
		return start;
	}

	public String getSequenceName() {
		return sequenceName;
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

    public String getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(String sequenceType) {
        this.sequenceType = sequenceType;
    }

    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }
}
