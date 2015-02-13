package org.opencb.cellbase.core.lib.api;

import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.common.Position;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;

public interface GeneDBAdaptor extends FeatureDBAdaptor {


	public QueryResult getAll(QueryOptions options);

    public QueryResult next(String id, QueryOptions options);


	public QueryResult getAllById(String id, QueryOptions options);

	public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);

	public QueryResult getAllBiotypes(QueryOptions options);

	// Now they are filters of getAll
//	public QueryResult getAllByBiotype(String biotype);
//	public List<Gene> getAllByBiotypeList(List<String> biotypeList);

	public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options);

	public QueryResult getAllByPosition(Position position, QueryOptions options);

	public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);


	public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);

	public QueryResult getAllByRegion(Region region, QueryOptions options);

	public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);



//	public List<Gene> getAllBySnpId(String snpId);
//
//	public List<List<Gene>> getAllBySnpIdList(List<String> snpIdList);
//
//	public List<Gene> getAllByTf(String id);
//
//	public List<List<Gene>> getAllByTfList(List<String> idList);
//
//	public List<Gene> getAllByTfName(String tfName);
//
//	public List<List<Gene>> getAllByTfNameList(List<String> tfNameList);
//
//	public List<Gene> getAllTargetsByTf(String id);
//
//	public List<List<Gene>> getAllTargetsByTfList(List<String> idList);
//
//	public List<Gene> getAllByMiRnaMature(String mirbaseId);
//
//	public List<List<Gene>> getAllByMiRnaMatureList(List<String> mirbaseIds);
//
//	public List<Gene> getAllTargetsByMiRnaMature(String mirbaseId);
//
//	public List<List<Gene>> getAllTargetsByMiRnaMatureList(List<String> mirbaseIds);

	public QueryResult getAllIntervalFrequencies(Region region, QueryOptions options);

    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions options);

}
