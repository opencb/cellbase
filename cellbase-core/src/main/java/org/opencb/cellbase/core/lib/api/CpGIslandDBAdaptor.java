package org.opencb.cellbase.core.lib.api;

import org.opencb.biodata.models.core.CpGIsland;
import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;

import java.util.List;


public interface CpGIslandDBAdaptor {
	
	public List<CpGIsland> getAllByRegion(Region region);
	
	public List<List<CpGIsland>> getAllByRegionList(List<Region> regionList);
	
	
	public List<IntervalFeatureFrequency> getAllIntervalFrequencies(Region region, int interval);
}
