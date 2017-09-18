package org.opencb.cellbase.client.rest;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;

/**
 * Created by fjlopez on 07/07/17.
 */
public class ClinicalVariantClient extends ParentRestClient<Variant> {
    public ClinicalVariantClient(String species, String assembly, ClientConfiguration configuration) {
        super(species, assembly, configuration);

        this.clazz = Variant.class;

        this.category = "clinical";
        this.subcategory = "variant";

    }

    public QueryResponse<Variant> search(Query query, QueryOptions queryOptions) throws IOException {
        return execute("search", query, queryOptions, clazz);
    }

    public QueryResponse<String> alleleOriginLabels() throws IOException {
        return execute("allele_origin_labels", new Query(), new QueryOptions(), String.class);
    }

    public QueryResponse<String> clinsigLabels() throws IOException {
        return execute("clinsig_labels", new Query(), new QueryOptions(), String.class);
    }

    public QueryResponse<String> consistencyLabels() throws IOException {
        return execute("consistency_labels", new Query(), new QueryOptions(), String.class);
    }

    public QueryResponse<String> modeInheritanceLabels() throws IOException {
        return execute("mode_inheritance_labels", new Query(), new QueryOptions(), String.class);
    }

    public QueryResponse<String> variantTypes() throws IOException {
        return execute("type", new Query(), new QueryOptions(), String.class);
    }
}
