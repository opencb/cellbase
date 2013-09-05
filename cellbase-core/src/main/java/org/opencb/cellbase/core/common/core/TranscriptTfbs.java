package org.opencb.cellbase.core.common.core;

public class TranscriptTfbs {
	private String tfName;
	private String pwm;
	private String chromosome;
	private int start;
	private int end;
	private String strand;
	private int relativeStart;
	private int relativeEnd;
	private float score;
	
	public TranscriptTfbs(String tfName, String pwm, String chromosome,
			Integer start, Integer end, String strand, Integer relativeStart,
			Integer relativeEnd, Float score) {
		super();
		this.tfName = tfName;
		this.pwm = pwm;
		this.chromosome = chromosome;
		this.start = start;
		this.end = end;
		this.strand = strand;
		this.relativeStart = relativeStart;
		this.relativeEnd = relativeEnd;
		this.score = score;
	}

	public String getTfName() {
		return tfName;
	}

	public void setTfName(String tfName) {
		this.tfName = tfName;
	}

	public String getPwm() {
		return pwm;
	}

	public void setPwm(String pwm) {
		this.pwm = pwm;
	}

	public String getChromosome() {
		return chromosome;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
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

	public String getStrand() {
		return strand;
	}

	public void setStrand(String strand) {
		this.strand = strand;
	}

	public int getRelativeStart() {
		return relativeStart;
	}

	public void setRelativeStart(int relativeStart) {
		this.relativeStart = relativeStart;
	}

	public int getRelativeEnd() {
		return relativeEnd;
	}

	public void setRelativeEnd(int relativeEnd) {
		this.relativeEnd = relativeEnd;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}
	
	
	
}
