package org.opencb.cellbase.core.lib.api;


import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.common.core.Transcript;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;

public interface TranscriptDBAdaptor extends FeatureDBAdaptor {


    public QueryResult getAllById(String id, QueryOptions options);

    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);

    public QueryResult next(String id, QueryOptions options);

	
//	public List<String> getAllEnsemblIds();
//
//	public Transcript getByEnsemblId(String ensemblId);
//	
//	public List<Transcript> getAllByEnsemblIdList(List<String> ensemblIdList);
//
//	public List<List<Transcript>> getAllByName(String name, List<String> exclude);
//
//	public List<List<List<Transcript>>> getAllByNameList(List<String> nameList, List<String> exclude);
//	
//	public List<Transcript> getByEnsemblGeneId(String ensemblGeneId);
//	
//	public List<List<Transcript>> getByEnsemblGeneIdList(List<String> ensemblGeneIdList);

	
//	public List<Transcript> getAllByBiotype(String biotype);
//	
//	public List<Transcript> getAllByBiotypeList(List<String> biotypeList);
	

//	public List<Transcript> getAllByPosition(String chromosome, int position);
//
//	public List<Transcript> getAllByPosition(Position position);

	public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options);
	
	public QueryResult getAllByPosition(Position position, QueryOptions options);
	
//	public List<List<Transcript>> getAllByPositionList(List<Position> positionList);
	
	public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);

	
//	public List<Transcript> getAllByRegion(String chromosome);
//
//	public List<Transcript> getAllByRegion(String chromosome, int start);
//
//	public List<Transcript> getAllByRegion(String chromosome, int start, int end);
//
//	public List<Transcript> getAllByRegion(String chromosome, int start, int end, List<String> biotypeList);
	
	public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);

//	public List<Transcript> getAllByRegion(Region region);
//
//	public List<Transcript> getAllByRegion(Region region, List<String> biotypeList);

	public QueryResult getAllByRegion(Region region, QueryOptions options);
	
	
//	public List<List<Transcript>> getAllByRegionList(List<Region> regionList);
//	
//	public List<List<Transcript>> getAllByRegionList(List<Region> regions, List<String> biotypeList);

	public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);
	
	
	
//	public List<Transcript> getAllByCytoband(String chromosome, String cytoband);
//	
//	
//	public List<Transcript> getAllBySnpId(String snpId);
//	
//	public List<List<Transcript>> getAllBySnpIdList(List<String> snpIdList);


	public QueryResult getAllByEnsemblExonId(String ensemblExonId, QueryOptions options);

	public List<QueryResult> getAllByEnsemblExonIdList(List<String> ensemblExonIdList, QueryOptions options);
	
	
	public QueryResult getAllByTFBSId(String tfbsId, QueryOptions options);

	public List<QueryResult> getAllByTFBSIdList(List<String> tfbsIdList, QueryOptions options);
	
	
	public List<Transcript> getAllByProteinName(String proteinName);
	
	public List<List<Transcript>> getAllByProteinNameList(List<String> proteinNameList);
	
	
	public List<Transcript> getAllByMirnaMature(String mirnaID);
	
	public List<List<Transcript>> getAllByMirnaMatureList(List<String> mirnaIDList);
	
	
}
