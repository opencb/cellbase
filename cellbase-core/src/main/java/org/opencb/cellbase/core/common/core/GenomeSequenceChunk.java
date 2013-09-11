package org.opencb.cellbase.core.common.core;

public class GenomeSequenceChunk {

	private String chromosome;
	private int chunk;
	private int start;
	private int end;
//    private String chunkId;
    private String sequenceType;
	private String sequence;

	
	public GenomeSequenceChunk(String chromosome, int chunk, int start, int end, String sequence) {
        this(chromosome, chunk, start, end, "", sequence);
	}

    public GenomeSequenceChunk(String chromosome, int chunk, int start, int end, String sequenceType, String sequence) {
		this.chromosome = chromosome;
		this.chunk = chunk;
		this.start = start;
		this.end = end;
//        this.chunkId = chunkId;
        this.sequenceType = sequenceType;
		this.sequence = sequence;
	}
	
	
	public String getChromosome() {
		return chromosome;
	}
	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	
	public int getChunk() {
		return chunk;
	}
	public void setChunk(int chunk) {
		this.chunk = chunk;
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


//    public String getChunkId() {
//        return chunkId;
//    }
//
//    public void setChunkId(String chunkId) {
//        this.chunkId = chunkId;
//    }


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
