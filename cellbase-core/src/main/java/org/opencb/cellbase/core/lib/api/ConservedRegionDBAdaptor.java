package org.opencb.cellbase.core.lib.api;

import org.opencb.biodata.models.feature.Region;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface ConservedRegionDBAdaptor {

    public QueryResult getAllByRegion(Region region, QueryOptions options);

    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);

    public List<QueryResult> getAllScoresByRegionList(List<Region> regions, QueryOptions options);

}
