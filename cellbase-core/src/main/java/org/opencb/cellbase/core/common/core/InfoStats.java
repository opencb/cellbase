package org.opencb.cellbase.core.common.core;

import java.util.List;

public class InfoStats {
	private String species;
	private List<Chromosome> chromosomes;

	public InfoStats() {

	}

	public InfoStats(String species, List<Chromosome> chromosomes) {
		super();
		this.species = species;
		this.chromosomes = chromosomes;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public List<Chromosome> getChromosomes() {
		return chromosomes;
	}

	public void setChromosomes(List<Chromosome> chromosomes) {
		this.chromosomes = chromosomes;
	}

}
