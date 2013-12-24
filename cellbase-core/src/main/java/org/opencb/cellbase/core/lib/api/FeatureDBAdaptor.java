package org.opencb.cellbase.core.lib.api;

import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;


public interface FeatureDBAdaptor {

	
	public QueryResult getAll(QueryOptions options);

	public QueryResult next(String chromosome, int position, QueryOptions options);


	
//	public List<String> getAllIds();
//
//
//	public Map<String, Object> getInfo(String id);
//
//	public List<Map<String, Object>> getInfoByIdList(List<String> idList);
//
//	public Map<String, Object> getFullInfo(String id);
//
//	public List<Map<String, Object>> getFullInfoByIdList(List<String> idList);
//
//
//
//
//	public Region getRegionById(String id);
//
//	public List<Region> getAllRegionsByIdList(List<String> idList);
//
//
//	public String getSequenceById(String id);
//
//	public List<String> getAllSequencesByIdList(List<String> idList);

}
