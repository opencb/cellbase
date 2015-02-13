package org.opencb.cellbase.core.lib.api;

import org.opencb.biodata.models.feature.Region;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface GenomeSequenceDBAdaptor {

	
	public QueryResult getByRegion(String chromosome, int start, int end, QueryOptions options);

	public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);

	public String getRevComp(String sequence);

	
}
