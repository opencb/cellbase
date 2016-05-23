package org.opencb.cellbase.client.rest;

import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.List;

/**
 * Created by swaathi on 20/05/16.
 */
public class TranscriptClient extends ParentRestClient<Transcript> {
    public TranscriptClient(ClientConfiguration configuration) {
        super(configuration);
        this.clazz = Transcript.class;

        this.category = "feature";
        this.subcategory = "transcript";
    }

    public QueryResponse<Gene> getGene(String id, Query query) throws IOException {
        return execute(id, "gene", query, Gene.class);
    }

    public QueryResponse<Variant> getVariation(String id, Query query) throws IOException {
        return execute(id, "variation", query, Variant.class);
    }

    public QueryResponse<String> getSequence(String id, Query query) throws IOException {
        return execute(id, "sequence", query, String.class);
    }

    public QueryResponse<Entry> getProtein(String id, Query query) throws IOException {
        return execute(id, "protein", query, Entry.class);
    }

    public QueryResponse<List> getProteinFunctionPrediction(String id, Query query) throws IOException {
        return execute(id, "function_prediction", query, List.class);
    }
}
