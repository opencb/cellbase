package org.opencb.cellbase.client.rest;

import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.client.rest.models.GroupByFields;
import org.opencb.cellbase.client.rest.models.GroupCount;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;

/**
 * Created by swaathi on 20/05/16.
 */
public class FeatureClient<T> extends ParentRestClient<T> {

    public FeatureClient(ClientConfiguration configuration) {
        super(configuration);
    }

    public  QueryResponse<T> next(String id) throws IOException {
        return execute(id, "next", null, clazz);
    }

    public QueryResponse<T> search(Query query) throws IOException {
        return execute("search", query, clazz);
    }

    public QueryResponse<GroupByFields> group(Query query) throws IOException {
        return execute("group", query, GroupByFields.class);
    }

    public QueryResponse<GroupCount> groupCount(Query query) throws IOException {
        return execute("group", query, GroupCount.class);
    }
}
