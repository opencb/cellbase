package org.opencb.cellbase.core.lib.api;



import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.common.core.CpGIsland;

import java.util.List;


public interface CpGIslandDBAdaptor {
	
	public List<CpGIsland> getAllByRegion(Region region);
	
	public List<List<CpGIsland>> getAllByRegionList(List<Region> regionList);
	
	
	public List<IntervalFeatureFrequency> getAllIntervalFrequencies(Region region, int interval);
}
