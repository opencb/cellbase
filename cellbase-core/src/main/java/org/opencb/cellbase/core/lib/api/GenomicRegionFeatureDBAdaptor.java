package org.opencb.cellbase.core.lib.api;

import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.Region;

import java.util.List;


// Como no es una fetaure no deberia implementas FeatureDBAdaptor no?
//public interface FeatureMapDBAdaptor extends FeatureDBAdaptor {	

@Deprecated
public interface GenomicRegionFeatureDBAdaptor {
	
	
	public String getByRegion(String chromosome, int start, int end);
	
	public String getByRegion(Region region);
	
	public List<String> getAllByRegionList(List<Region> regions);

	
	public String getByRegion(Region region, List<String> sources);	// sources: gene, exon, snp, ...
	
	public List<String> getAllByRegionList(List<Region> regions, List<String> sources);

	public List<String> getByVariants(List<GenomicVariant> variants, List<String> sources);

	List<String> getByVariants(List<GenomicVariant> variants);
	
//	public List<FeatureMap> getFeatureMapsByRegion(Region region);

//	public HashMap<String, List<String>> getConsequenceTypeSoAccession(String chromosome, int position);
//
//	HashMap<String, List<String>> getConsequenceTypeSoAccession(String chromosome,int position, String alternativeAllele);
	
	
//	public GenomicRegionFeatures getAllByRegion(Region region, List<String> featureTypes); // types: variation, regulatory, core, ...
//	public List<GenomicRegionFeatures> getAllByRegion(List<Region> regions, List<String> featureTypes);

	
}
