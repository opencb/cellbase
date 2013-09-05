package org.opencb.cellbase.core.common;

public class GenomicVariantConsequenceType {

	private String chromosome;
	private int position;
	private String referenceAllele;
	private String alternativeAllele;
	private String featureId;
	private String featureName;
	private String featureType;
	private String featureBiotype;
	private String featureChromosome;
	private int featureStart;
	private int featureEnd;
	private String featureStrand;

	private String snpId;
	private String ancestral;
	private String alternative;

	private String geneId;
	private String transcriptId;
	private String geneName;

	public String consequenceType;
	private String consequenceTypeObo;
	private String consequenceTypeDesc;
	private String consequenceTypeType;

	private int aaPosition;
	private String aminoacidChange;
	private String codonChange;


	public GenomicVariantConsequenceType(String chromosome, int position, String referenceAllele, String alternativeAllele,
			String featureId, String featureName, String featureType, String featureBiotype,
			String featureChromosome, int featureStart,
			int featureEnd, String featureStrand, String snpId,
			String ancestral, String alternative, String geneId,
			String transcriptId, String geneName, String consequenceType,
			String consequenceTypeObo, String consequenceTypeDesc,
			String consequenceTypeType, int aaPosition, String aminoacidChange,	String codonChange) {
		super();
		this.chromosome = chromosome;
		this.position = position;
		this.referenceAllele = referenceAllele;
		this.alternativeAllele = alternativeAllele;
		this.featureId = featureId;
		this.featureName = featureName;
		this.featureType = featureType;
		this.featureBiotype = featureBiotype;
		this.featureChromosome = featureChromosome;
		this.featureStart = featureStart;
		this.featureEnd = featureEnd;
		this.featureStrand = featureStrand;
		this.snpId = snpId;
		this.ancestral = ancestral;
		this.alternative = alternative;
		this.geneId = geneId;
		this.transcriptId = transcriptId;
		this.geneName = geneName;
		this.consequenceType = consequenceType;
		this.consequenceTypeObo = consequenceTypeObo;
		this.consequenceTypeDesc = consequenceTypeDesc;
		this.consequenceTypeType = consequenceTypeType;
		this.aaPosition = aaPosition;
		this.aminoacidChange = aminoacidChange;
		this.codonChange = codonChange;
	}


	public String toString(){
		StringBuilder br = new StringBuilder();
		return br.append(chromosome).append("\t")
				.append(position).append("\t")
				.append(referenceAllele).append("\t")
				.append(alternativeAllele).append("\t")
				.append(featureId).append("\t")
				.append(featureName).append("\t")
				.append(featureType).append("\t")
				.append(featureBiotype).append("\t")
				.append(featureChromosome).append("\t")
				.append(featureStart).append("\t")
				.append(featureEnd).append("\t")
				.append(featureStrand).append("\t")
				.append(snpId).append("\t")
				.append(ancestral).append("\t")
				.append(alternative).append("\t")
				.append(geneId).append("\t")
				.append(transcriptId).append("\t")
				.append(geneName).append("\t")
				.append(consequenceType).append("\t")
				.append(consequenceTypeObo).append("\t")
				.append(consequenceTypeDesc).append("\t")
				.append(consequenceTypeType).append("\t")
				.append(aaPosition).append("\t")
				.append(aminoacidChange).append("\t")
				.append(codonChange).toString();
	}
}