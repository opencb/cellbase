package org.opencb.cellbase.core.lib.api.variation;


import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.common.variation.MutationPhenotypeAnnotation;

import java.util.List;

public interface MutationDBAdaptor {
	
	public List<MutationPhenotypeAnnotation> getAllMutationPhenotypeAnnotationByGeneName(String geneName);
	
	public List<List<MutationPhenotypeAnnotation>> getAllMutationPhenotypeAnnotationByGeneNameList(List<String> geneNameList);
	
	public List<MutationPhenotypeAnnotation> getAllMutationPhenotypeAnnotationByEnsemblTranscript(String ensemblTranscript);
	
	public List<List<MutationPhenotypeAnnotation>> getAllMutationPhenotypeAnnotationByEnsemblTranscriptList(List<String> ensemblTranscriptList);
	
	
	public List<MutationPhenotypeAnnotation> getAllMutationPhenotypeAnnotationByPosition(Position position);
	
	public List<List<MutationPhenotypeAnnotation>> getAllMutationPhenotypeAnnotationByPositionList(List<Position> position);
	
	
	
	public List<MutationPhenotypeAnnotation> getAllByRegion(Region region);
	
	public List<List<MutationPhenotypeAnnotation>> getAllByRegionList(List<Region> regionList);

	
	public List<IntervalFeatureFrequency> getAllIntervalFrequencies(Region region, int interval);
	
}
