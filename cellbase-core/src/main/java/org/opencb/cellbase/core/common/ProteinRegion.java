package org.opencb.cellbase.core.common;

public class ProteinRegion {

	private String uniprotId;
	private String ensemblTranscript;
	private int start;
	private int end;
	private String sequence;
	private String codon;
	
	
	public ProteinRegion(String uniprotId, String ensemblTranscript, int start, int end, String sequence, String codon) {
		super();
		this.uniprotId = uniprotId;
		this.ensemblTranscript = ensemblTranscript;
		this.start = start;
		this.end = end;
		this.sequence = sequence;
		this.codon = codon;
	}

	
	public String getUniprotId() {
		return uniprotId;
	}

	public void setUniprotId(String uniprotId) {
		this.uniprotId = uniprotId;
	}

	
	public String getEnsemblTranscript() {
		return ensemblTranscript;
	}

	public void setEnsemblTranscript(String ensemblTranscript) {
		this.ensemblTranscript = ensemblTranscript;
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
	
	
	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	
	public String getCodon() {
		return codon;
	}

	public void setCodon(String codon) {
		this.codon = codon;
	}

}
