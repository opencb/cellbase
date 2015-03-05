package org.opencb.cellbase.core.lib.api;

import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface ChromosomeDBAdaptor {

    public QueryResult speciesInfoTmp(String id, QueryOptions options);

	public QueryResult getAll(QueryOptions options);

	public QueryResult getById(String id, QueryOptions options);

	public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);

    public QueryResult getAllCytobandsById(String id, QueryOptions options);

    public List<QueryResult> getAllCytobandsByIdList(List<String> id, QueryOptions options);
    
//	List<Cytoband> getCytobandByName(String name);
//	List<List<Cytoband>> getCytobandByNameList(List<String> nameList);
//	List<String> getChromosomeNames();
}
