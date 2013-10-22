package org.opencb.cellbase.core.common.core;

public class GenomeSequenceChunk {

	private String chromosome;
	private String chunkId;
	private int start;
	private int end;
    private String sequenceType;
	private String sequence;

	
	public GenomeSequenceChunk(String chromosome, String chunkId, int start, int end, String sequence) {
        this(chromosome, chunkId, start, end, "", sequence);
	}

    public GenomeSequenceChunk(String chromosome, String chunkId, int start, int end, String sequenceType, String sequence) {
		this.chromosome = chromosome;
		this.chunkId = chunkId;
		this.start = start;
		this.end = end;
        this.sequenceType = sequenceType;
		this.sequence = sequence;
	}
	
	
	public String getChromosome() {
		return chromosome;
	}
	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	
	public String getChunkId() {
		return chunkId;
	}

	public void setChunkId(String chunkId) {
		this.chunkId = chunkId;
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


    public String getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(String sequenceType) {
        this.sequenceType = sequenceType;
    }


	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
}
