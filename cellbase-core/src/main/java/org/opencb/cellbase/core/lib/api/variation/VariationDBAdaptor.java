package org.opencb.cellbase.core.lib.api.variation;


import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResponse;

import java.util.List;

public interface VariationDBAdaptor {

	
    public QueryResponse getById(String id, QueryOptions options);

    public QueryResponse getAllByIdList(List<String> idList, QueryOptions options);

    
	public QueryResponse getAllByPosition(String chromosome, int position, QueryOptions options);

	public QueryResponse getAllByPosition(Position position, QueryOptions options);
	
	public QueryResponse getAllByPositionList(List<Position> positionList, QueryOptions options);
	

	public QueryResponse getAllByRegion(String chromosome, int start, int end, QueryOptions options);
    
    public QueryResponse getAllByRegion(Region region, QueryOptions options);

    public QueryResponse getAllByRegionList(List<Region> regions, QueryOptions options);

    public QueryResponse getAllIntervalFrequencies(Region region, QueryOptions options);
    
//    public List<Variation> getById(String id, List<String> exclude);
//
//    List<List<Variation>> getByIdList(List<String> idList, List<String> exclude);
//
//    public List<Variation> getByRegion(String chromosome, int start, int end, List<String> consequence_types, List<String> exclude);
//
//    public List<List<Variation>> getByRegionList(List<Region> regions, List<String> exclude);
//
//    public List<List<Variation>> getByRegionList(List<Region> regions, List<String> consequence_types, List<String> exclude);
//
//    public String getAllIntervalFrequencies(Region region, int interval);

}
