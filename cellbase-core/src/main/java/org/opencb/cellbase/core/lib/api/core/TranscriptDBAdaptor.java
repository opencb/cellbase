package org.opencb.cellbase.core.lib.api.core;

import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.lib.api.FeatureDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface TranscriptDBAdaptor extends FeatureDBAdaptor {


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


	public QueryResult getAllByEnsemblExonId(String ensemblExonId, QueryOptions options);

	public List<QueryResult> getAllByEnsemblExonIdList(List<String> ensemblExonIdList, QueryOptions options);
	
	
	public QueryResult getAllByTFBSId(String tfbsId, QueryOptions options);

	public List<QueryResult> getAllByTFBSIdList(List<String> tfbsIdList, QueryOptions options);
	
	
//	public List<Transcript> getAllByProteinName(String proteinName);
//
//	public List<List<Transcript>> getAllByProteinNameList(List<String> proteinNameList);
	
	
	public List<Transcript> getAllByMirnaMature(String mirnaID);
	
	public List<List<Transcript>> getAllByMirnaMatureList(List<String> mirnaIDList);

}
