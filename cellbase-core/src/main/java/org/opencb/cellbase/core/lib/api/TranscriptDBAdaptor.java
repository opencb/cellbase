package org.opencb.cellbase.core.lib.api;

import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.common.Position;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface TranscriptDBAdaptor extends FeatureDBAdaptor {


    public QueryResult getAllById(String id, QueryOptions options);

    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);

    public QueryResult next(String id, QueryOptions options);


//	public List<Transcript> getAllByPosition(String chromosome, int position);
//
//	public List<Transcript> getAllByPosition(Position position);

	public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options);
	
	public QueryResult getAllByPosition(Position position, QueryOptions options);
	
	public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);

	
	public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);

	public QueryResult getAllByRegion(Region region, QueryOptions options);

	public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);
	
	
	
	public QueryResult getAllByEnsemblExonId(String ensemblExonId, QueryOptions options);

	public List<QueryResult> getAllByEnsemblExonIdList(List<String> ensemblExonIdList, QueryOptions options);
	
	
	public QueryResult getAllByTFBSId(String tfbsId, QueryOptions options);

	public List<QueryResult> getAllByTFBSIdList(List<String> tfbsIdList, QueryOptions options);
	
	
	public List<Transcript> getAllByProteinName(String proteinName);
	
	public List<List<Transcript>> getAllByProteinNameList(List<String> proteinNameList);
	
	
	public List<Transcript> getAllByMirnaMature(String mirnaID);
	
	public List<List<Transcript>> getAllByMirnaMatureList(List<String> mirnaIDList);

}
