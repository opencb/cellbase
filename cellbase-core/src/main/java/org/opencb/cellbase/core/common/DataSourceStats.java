/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.core.common;

public class DataSourceStats {

	private int nbPathways;
	private int nbInteractions;
	private int nbPhysicalEntities;
	private int nbProteins;
	private int nbGenes;
	private int nbComplexes;
	private int nbCellularLocations;
	private int nbPubXrefs;

	
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("#Pathways").append("\t").append("Interactions").append("\t").append("Pysical entities").append("\t").append("Proteins").append("\t").append("Genes").append("\t").append("Complexes").append("\t").append("Cellular locations").append("\t").append("Publication xrefs").append("\n");
		sb.append(nbPathways).append("\t").append(nbInteractions).append("\t").append(nbPhysicalEntities).append("\t").append(nbProteins).append("\t").append(nbGenes).append("\t").append(nbComplexes).append("\t").append(nbCellularLocations).append("\t").append(this.nbPubXrefs).append("\n");

		return sb.toString();
	}

	public String toJson() {
		StringBuilder sb = new StringBuilder();

		sb.append("\"stats\" : {");
		sb.append("\"Pathways\": ").append(nbPathways).append(",");
		sb.append("\"Interactions\": ").append(nbInteractions).append(",");
		sb.append("\"Physical entities\": ").append(nbPhysicalEntities).append(",");
		sb.append("\"Genes\": ").append(nbGenes).append(",");
		sb.append("\"Proteins\": ").append(nbProteins).append(",");
		sb.append("\"Complexes\": ").append(nbComplexes).append(",");
		sb.append("\"Cellular locations\": ").append(nbCellularLocations).append(",");
		sb.append("\"Publication xrefs\": ").append(nbPubXrefs);
		sb.append("}");

		return sb.toString();
	}

	
	public int getNbPathways() {
		return nbPathways;
	}
	
	public void setNbPathways(int nbPathways) {
		this.nbPathways = nbPathways;
	}
	
	
	public int getNbInteractions() {
		return nbInteractions;
	}
	
	public void setNbInteractions(int nbInteractions) {
		this.nbInteractions = nbInteractions;
	}
	
	
	public int getNbPhysicalEntities() {
		return nbPhysicalEntities;
	}
	
	public void setNbPhysicalEntities(int nbPhysicalEntities) {
		this.nbPhysicalEntities = nbPhysicalEntities;
	}
	
	
	public int getNbProteins() {
		return nbProteins;
	}
	
	public void setNbProteins(int nbProteins) {
		this.nbProteins = nbProteins;
	}
	
	
	public int getNbGenes() {
		return nbGenes;
	}
	
	public void setNbGenes(int nbGenes) {
		this.nbGenes = nbGenes;
	}
	
	
	public int getNbComplexes() {
		return nbComplexes;
	}
	
	public void setNbComplexes(int nbComplexes) {
		this.nbComplexes = nbComplexes;
	}
	
	
	public int getNbCellularLocations() {
		return nbCellularLocations;
	}
	
	public void setNbCellularLocations(int nbCellularLocations) {
		this.nbCellularLocations = nbCellularLocations;
	}
	
	
	public int getNbPubXrefs() {
		return nbPubXrefs;
	}
	
	public void setNbPubXrefs(int nbPubXrefs) {
		this.nbPubXrefs = nbPubXrefs;
	}

}
