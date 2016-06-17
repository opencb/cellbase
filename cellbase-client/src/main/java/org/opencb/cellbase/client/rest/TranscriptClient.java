package org.opencb.cellbase.client.rest;

import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.QueryOptions;
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

    public QueryResponse<Gene> getGene(String id, QueryOptions options) throws IOException {
        return execute(id, "gene", options, Gene.class);
    }

    public QueryResponse<Variant> getVariation(String id, QueryOptions options) throws IOException {
        return execute(id, "variation", options, Variant.class);
    }

    public QueryResponse<String> getSequence(String id, QueryOptions options) throws IOException {
        return execute(id, "sequence", options, String.class);
    }

    public QueryResponse<Entry> getProtein(String id, QueryOptions options) throws IOException {
        return execute(id, "protein", options, Entry.class);
    }

    public QueryResponse<List> getProteinFunctionPrediction(String id, QueryOptions options) throws IOException {
        return execute(id, "function_prediction", options, List.class);
    }
}
