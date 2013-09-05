package org.opencb.cellbase.core.common.variation;

public class PopulationFrequency {

	private String pop;
	private String refAllele;
	private String altAllele;
	private double refAlleleFreq;
	private double altAlleleFreq;

	public PopulationFrequency() {
	}

	public PopulationFrequency(String pop, String refAllele, String altAllele, double refAlleleFreq,
			double altAlleleFreq) {
		this.pop = pop;
		this.refAllele = refAllele;
		this.altAllele = altAllele;
		this.refAlleleFreq = refAlleleFreq;
		this.altAlleleFreq = altAlleleFreq;
	}

	public String getPop() {
		return pop;
	}

	public void setPop(String pop) {
		this.pop = pop;
	}

	public String getRefAllele() {
		return refAllele;
	}

	public void setRefAllele(String refAllele) {
		this.refAllele = refAllele;
	}

	public String getAltAllele() {
		return altAllele;
	}

	public void setAltAllele(String altAllele) {
		this.altAllele = altAllele;
	}

	public double getRefAlleleFreq() {
		return refAlleleFreq;
	}

	public void setRefAlleleFreq(double refAlleleFreq) {
		this.refAlleleFreq = refAlleleFreq;
	}

	public double getAltAlleleFreq() {
		return altAlleleFreq;
	}

	public void setAltAlleleFreq(double altAlleleFreq) {
		this.altAlleleFreq = altAlleleFreq;
	}

	// private double homRefAlleleFreq;
	// private double hetAlleleFreq;
	// private double HomAltAlleleFreq;

}
