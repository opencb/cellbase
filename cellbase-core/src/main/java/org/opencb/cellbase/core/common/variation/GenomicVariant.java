package org.opencb.cellbase.core.common.variation;

import java.util.ArrayList;
import java.util.List;

public class GenomicVariant {

	private String chromosome;
	private int position;
	private String reference;
	private String alternative;

	public GenomicVariant(String chromosome, int position, String alternative) {
		this(chromosome, position, null, alternative);
	}

	public GenomicVariant(String chromosome, int position, String reference, String alternative) {
		this.chromosome = chromosome;
		this.position = position;
		this.reference = reference;
		this.alternative = alternative;
	}


	public static GenomicVariant parseVariant(String variantString) {
		GenomicVariant genomicVariant = null;
		if(variantString != null && !variantString.equals("")) {
			//	if(regionString.indexOf(':') != -1) {
			String[] fields = variantString.split(":", -1);
			if(fields.length == 3) {
				genomicVariant = new GenomicVariant(fields[0], Integer.parseInt(fields[1]), fields[2]);
			}else {
				if(fields.length == 4) {
					genomicVariant = new GenomicVariant(fields[0], Integer.parseInt(fields[1]), fields[2], fields[3]);
				}else {
					genomicVariant = null;
				}
			}
			//	}else {
			//		genomicVariant = new GenomicVariant(regionString, 0, "");
			//	}
		}
		return genomicVariant;
	}

	public static List<GenomicVariant> parseVariants(String variantsString) {
		List<GenomicVariant> genomicVariants = null;
		if(variantsString != null && !variantsString.equals("")) {
			String[] variantItems = variantsString.split(",");
			genomicVariants = new ArrayList<>(variantItems.length);
//			String[] fields;
			for(String variantString: variantItems) {
				//	if(regionString.indexOf(':') != -1) {

                genomicVariants.add(parseVariant(variantString));

//				fields = variantString.split(":", -1);
//				if(fields.length == 3) {
//					genomicVariants.add(new GenomicVariant(fields[0], Integer.parseInt(fields[1]), fields[2]));
//				}else {
//					if(fields.length == 4) {
//						genomicVariants.add(new GenomicVariant(fields[0], Integer.parseInt(fields[1]), fields[2], fields[3]));
//					}else {
//						genomicVariants.add(null);
//					}
//				}

			}	
		}
		return genomicVariants;
	}

    public boolean isIndel() {
        return this.getReference().length() > 1 || this.getAlternative().length() > 1 || this.getReference().equals("-") || this.getAlternative().equals("-");
    }

    public boolean isStructuralVariation() {
        return this.getReference().length() > 50 || this.getAlternative().length() > 50 || this.getReference().equals("INS") || this.getAlternative().equals("INS") || this.getReference().equals("DEL") || this.getAlternative().equals("DEL");
    }

	/**
	 * 
	 * @param variantList
	 * @return A comma separated string with all the regions. If parameter is null then a null objects is returned, an empty string is returned if parameter size list is 0 
	 */
    @Deprecated
	public static String parseRegionList(List<GenomicVariant> variantList) {
		if(variantList == null) {
			return null;
		}else {
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<variantList.size()-1; i++) {
				if(variantList.get(i) != null) {
					sb.append(variantList.get(i).toString()).append(",");					
				}else {
					sb.append("null,");
				}
			}
			if(variantList.get(variantList.size()-1) != null) {
				sb.append(variantList.get(variantList.size()-1).toString());					
			}else {
				sb.append("null");
			}

			return sb.toString();
		}
	}


	@Override
	public String toString() {
		if(this.getReference() == null) {
			return chromosome+":"+position+":"+this.getAlternative();
		}else {
			return chromosome+":"+position+":"+this.getReference()+":"+this.getAlternative();
		}
	}


	/**
	 * @return the chromosome
	 */
	public String getChromosome() {
		return chromosome;
	}

	/**
	 * @param chromosome the chromosome to set
	 */
	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}


	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	
	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}
	
	
	public void setAlternative(String alternative) {
		this.alternative = alternative;
	}

	public String getAlternative() {
		return alternative;
	}

}
