package org.opencb.cellbase.client.rest;

import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.List;

/**
 * Created by swaathi on 25/05/16.
 */
public class ProteinClient extends FeatureClient<Entry> {

    public ProteinClient(ClientConfiguration configuration) {
        super(configuration);

        this.clazz = Entry.class;

        this.category = "feature";
        this.subcategory = "protein";
    }

    public QueryResponse<List> getSubstitutionScores(String id, Query query) throws IOException {
        return execute(id, "substitution_scores", query, List.class);
    }

    public QueryResponse<String> getSequence(String id, Query query) throws IOException {
        return execute(id, "sequence", query, String.class);
    }
}
