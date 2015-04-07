package org.opencb.cellbase.core.lib.api.core;

import org.opencb.cellbase.core.lib.api.FeatureDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface GeneDBAdaptor extends FeatureDBAdaptor {


	public QueryResult getAllById(String id, QueryOptions options);

	public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);


    /**
     * This method search the given 'id' in the XRefs array
     * @param id Any possible XRef id
     * @param options
     * @return Any gene found having that Xref id
     */
    public QueryResult getAllByXref(String id, QueryOptions options);

    public List<QueryResult> getAllByXrefList(List<String> idList, QueryOptions options);


	public QueryResult getAllBiotypes(QueryOptions options);

	public QueryResult getAllTargetsByTf(String id);

	public List<QueryResult> getAllTargetsByTfList(List<String> idList);


//	public QueryResult getAllByTf(String id);
//
//	public List<QueryResult> getAllByTfList(List<String> idList);
//
//	public List<Gene> getAllByTfName(String tfName);
//
//	public List<List<Gene>> getAllByTfNameList(List<String> tfNameList);
//
//	public List<Gene> getAllByMiRnaMature(String mirbaseId);
//
//	public List<List<Gene>> getAllByMiRnaMatureList(List<String> mirbaseIds);

}
