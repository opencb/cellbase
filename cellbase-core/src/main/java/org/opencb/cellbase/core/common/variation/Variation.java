package org.opencb.cellbase.core.common.variation;

import java.util.ArrayList;
import java.util.List;

public class Variation {

	// private String _id;
	private String id; 			//0
	private String chromosome; 		//1
	private int start; 				//2
	private int end; 				//3
	private String strand; 			//4
	private String type;		    // Allowed type values: SNV, MUT, INDEL, SV
	private String reference;
	private String alternate;
	private String alleleString;
	private String ancestralAllele;
	private String displayConsequenceType;
	private List<String> consequenceTypes;

//	private String species;
//	private String assembly;
//	private String source;
//	private String version;

	private List<TranscriptVariation> transcriptVariations = new ArrayList<>();
	private Phenotype phenotype;
	private List<SampleGenotype> samples = new ArrayList<>();
	private List<PopulationFrequency> populationFrequencies;
	private List<Xref> xrefs = new ArrayList<>();
	
	// Required
//	private String featureId;

	// Optional
	private String minorAllele;
	private String minorAlleleFreq;
	private String validationStatus;
	private String evidence;
	// private String variantSeSeq;
	// private String variantReads;
	
	// private List<String> variantEffect

	public Variation() {
		consequenceTypes = new ArrayList<>();
		transcriptVariations = new ArrayList<>();
		samples = new ArrayList<>();
		populationFrequencies = new ArrayList<>();
		xrefs = new ArrayList<>();
	}

	public Variation(String id, String chromosome, String type, int start, int end, String strand, String reference,
			String alternate, String alleleString, String ancestralAllele, String displayConsequenceType, /*String species, String assembly, String source, String version,*/
			List<String> consequencesTypes, List<TranscriptVariation> transcriptVariations, Phenotype phenotype, List<SampleGenotype> samples,
			List<PopulationFrequency> populationFrequencies, List<Xref> xrefs, /*String featureId,*/ String minorAllele,
			String minorAlleleFreq, String validationStatus, String evidence) {
		this.id = id;
		this.chromosome = chromosome;
		this.type = type;
		this.start = start;
		this.end = end;
		this.strand = strand;
		this.reference = reference;
		this.alternate = alternate;
		this.alleleString = alleleString;
		this.ancestralAllele = ancestralAllele;
		this.displayConsequenceType = displayConsequenceType;

//		this.species = species;
//		this.assembly = assembly;
//		this.source = source;
//		this.version = version;

		this.consequenceTypes = consequencesTypes;
		this.transcriptVariations = transcriptVariations;
		this.phenotype = phenotype;
		this.samples = samples;
		this.populationFrequencies = populationFrequencies;
		this.xrefs = xrefs;
		
//		this.featureId = featureId;
		this.minorAllele = minorAllele;
		this.minorAlleleFreq = minorAlleleFreq;
		this.validationStatus = validationStatus;
		this.evidence = evidence;
	}


//	public String getSpecies() {
//		return species;
//	}
//
//	public void setSpecies(String species) {
//		this.species = species;
//	}
//
//	public String getAssembly() {
//		return assembly;
//	}
//
//	public void setAssembly(String assembly) {
//		this.assembly = assembly;
//	}
//
//	public String getSource() {
//		return source;
//	}
//
//	public void setSource(String source) {
//		this.source = source;
//	}
//
//	public String getVersion() {
//		return version;
//	}
//
//	public void setVersion(String version) {
//		this.version = version;
//	}

	public List<SampleGenotype> getSamples() {
		return samples;
	}

	public void setSamples(List<SampleGenotype> samples) {
		this.samples = samples;
	}

	public void setXrefs(List<Xref> xrefs) {
		this.xrefs = xrefs;
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

	public List<PopulationFrequency> getPopulationFrequencies() {
		return populationFrequencies;
	}

	public void setPopulationFrequencies(List<PopulationFrequency> populationFrequencies) {
		this.populationFrequencies = populationFrequencies;
	}

//	public String getFeatureId() {
//		return featureId;
//	}
//
//	public void setFeatureId(String featureId) {
//		this.featureId = featureId;
//	}

	public String getMinorAllele() {
		return minorAllele;
	}

	public void setMinorAllele(String minorAllele) {
		this.minorAllele = minorAllele;
	}

	public String getMinorAlleleFreq() {
		return minorAlleleFreq;
	}

	public void setMinorAlleleFreq(String variantFreq) {
		this.minorAlleleFreq = variantFreq;
	}

	public String getValidationStatus() {
		return validationStatus;
	}

	public void setValidationStatus(String validationStatus) {
		this.validationStatus = validationStatus;
	}

	public String getReference() {
		return reference;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getAlternate() {
		return alternate;
	}

	public void setAlternate(String alternate) {
		this.alternate = alternate;
	}

	public List<Xref> getXrefs() {
		return xrefs;
	}

	public void setXrefs(Xref xrefs) {
		this.xrefs.add(xrefs);
	}

	public String getAlleleString() {
		return alleleString;
	}

	public void setAlleleString(String alleleString) {
		this.alleleString = alleleString;
	}

	public List<TranscriptVariation> getTranscriptVariations() {
		return transcriptVariations;
	}

	public void setTranscriptVariations(List<TranscriptVariation> transcriptVariations) {
		this.transcriptVariations = transcriptVariations;
	}

	public List<String> getConsequenceTypes() {
		return consequenceTypes;
	}

	public void setConsequenceTypes(List<String> consequenceTypes) {
		this.consequenceTypes = consequenceTypes;
	}

	public String getDisplayConsequenceType() {
		return displayConsequenceType;
	}

	public void setDisplayConsequenceType(String displayConsequenceType) {
		this.displayConsequenceType = displayConsequenceType;
	}

	public String getEvidence() {
		return evidence;
	}

	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}

	public String getAncestralAllele() {
		return ancestralAllele;
	}

	public void setAncestralAllele(String ancestralAllele) {
		this.ancestralAllele = ancestralAllele;
	}

	public Phenotype getPhenotype() {
		return phenotype;
	}

	public void setPhenotype(Phenotype phenotype) {
		this.phenotype = phenotype;
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
