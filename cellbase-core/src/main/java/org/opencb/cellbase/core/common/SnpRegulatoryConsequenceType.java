package org.opencb.cellbase.core.common;

public class SnpRegulatoryConsequenceType {

	private String snpName;
	private String featureName;
	private String featureType;
	private String chromosome;
	private int start;
	private int end;
	private String strand;
	private String transcriptStableId;
	private String geneStableId;
	private String geneName;
	private String biotype;

	public SnpRegulatoryConsequenceType(String snpName, String featureName,
			String featureType, String chromosome, int start, int end,
			String strand, String transcriptStableId, String geneStableId,
			String geneName, String biotype) {
		super();
		this.snpName = snpName;
		this.featureName = featureName;
		this.featureType = featureType;
		this.chromosome = chromosome;
		this.start = start;
		this.end = end;
		this.strand = strand;
		this.transcriptStableId = transcriptStableId;
		this.geneStableId = geneStableId;
		this.geneName = geneName;
		this.biotype = biotype;
	}
	public String getSnpName() {
		return snpName;
	}
	public void setSnpName(String snpName) {
		this.snpName = snpName;
	}
	public String getFeatureName() {
		return featureName;
	}
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}
	public String getFeatureType() {
		return featureType;
	}
	public void setFeatureType(String featureType) {
		this.featureType = featureType;
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
	public String getTranscriptStableId() {
		return transcriptStableId;
	}
	public void setTranscriptStableId(String transcriptStableId) {
		this.transcriptStableId = transcriptStableId;
	}
	public String getGeneStableId() {
		return geneStableId;
	}
	public void setGeneStableId(String geneStableId) {
		this.geneStableId = geneStableId;
	}
	public String getGeneName() {
		return geneName;
	}
	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}
	public String getBiotype() {
		return biotype;
	}
	public void setBiotype(String biotype) {
		this.biotype = biotype;
	}
	
	
}
