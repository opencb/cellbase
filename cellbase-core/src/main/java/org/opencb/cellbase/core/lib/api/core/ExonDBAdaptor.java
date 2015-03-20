package org.opencb.cellbase.core.lib.api.core;

import org.opencb.biodata.models.core.Exon;
import org.opencb.cellbase.core.lib.api.FeatureDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface ExonDBAdaptor extends FeatureDBAdaptor {


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


	public List<Exon> getAllByEnsemblIdList(List<String> ensemblIdList);
	
	public List<Exon> getByEnsemblTranscriptId(String transcriptId);
	
	public List<List<Exon>> getByEnsemblTranscriptIdList(List<String> transcriptIdList);

	public List<Exon> getByEnsemblGeneId(String geneId);
	
	public List<List<Exon>> getByEnsemblGeneIdList(List<String> geneIdList);

}
