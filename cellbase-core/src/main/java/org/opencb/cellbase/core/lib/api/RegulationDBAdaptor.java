package org.opencb.cellbase.core.lib.api;


import org.opencb.cellbase.core.common.GenericFeature;
import org.opencb.cellbase.core.common.Region;

import java.util.List;

public interface RegulationDBAdaptor {

    public List<GenericFeature> getByRegion(String chromosome, int start, int end, List<String> types);

    public List<List<GenericFeature>> getByRegionList(List<Region> regions);

    public List<List<GenericFeature>> getByRegionList(List<Region> regions, List<String> types);


}
