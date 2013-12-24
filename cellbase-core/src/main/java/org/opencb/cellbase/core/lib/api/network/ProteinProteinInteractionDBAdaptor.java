package org.opencb.cellbase.core.lib.api.network;

import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/5/13
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ProteinProteinInteractionDBAdaptor {

    public QueryResult getAll(QueryOptions options);

    public QueryResult getAllById(String id, QueryOptions options);

    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);


    public QueryResult getAllByInteractorId(String id, QueryOptions options);

    public List<QueryResult> getAllByInteractorIdList(List<String> idList, QueryOptions options);


}
