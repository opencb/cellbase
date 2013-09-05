package org.opencb.cellbase.core.lib.api;

import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResponse;

import java.util.List;


public interface ChromosomeDBAdaptor {

	public QueryResponse getAll(QueryOptions options);

	public QueryResponse getById(String id, QueryOptions options);

	public QueryResponse getAllByIdList(List<String> idList, QueryOptions options);

    public QueryResponse getAllCytobandsById(String id, QueryOptions options);

    public QueryResponse getAllCytobandsByIdList(List<String> id, QueryOptions options);
    
//	List<Cytoband> getCytobandByName(String name);
//	List<List<Cytoband>> getCytobandByNameList(List<String> nameList);
//	List<String> getChromosomeNames();
}
