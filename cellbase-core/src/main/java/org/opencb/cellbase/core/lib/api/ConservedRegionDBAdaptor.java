package org.opencb.cellbase.core.lib.api;


import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResponse;

import java.util.List;


public interface ConservedRegionDBAdaptor {

    public QueryResponse getAllByRegion(Region region, QueryOptions options);

    public QueryResponse getAllByRegionList(List<Region> regions, QueryOptions options);


}
