package org.opencb.cellbase.core.lib.api.core;

import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;

/**
 * Created by imedina on 10/12/13.
 */
public interface ProteinFunctionPredictorDBAdaptor {


    public QueryResult getAllByEnsemblTranscriptId(String transcriptId, QueryOptions options);

    public List<QueryResult> getAllByEnsemblTranscriptIdList(List<String> transcriptIdList, QueryOptions options);

    public QueryResult getByAaChange(String transcriptId, Integer aaPosition, String newAa, QueryOptions options);

}
