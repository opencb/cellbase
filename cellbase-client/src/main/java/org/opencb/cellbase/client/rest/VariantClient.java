package org.opencb.cellbase.client.rest;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public final class VariantClient extends FeatureClient<Variant> {

    public VariantClient(String species, String assembly, ClientConfiguration configuration) {
        super(species, assembly, configuration);
        this.clazz = Variant.class;

        this.category = "genomic";
        this.subcategory = "variant";
    }

    public QueryResponse<VariantAnnotation> getAnnotations(String id, QueryOptions options) throws IOException {
        return getAnnotations(Arrays.asList(id.split(",")), options, false);
    }

    public QueryResponse<VariantAnnotation> getAnnotations(String id, QueryOptions options, boolean post) throws IOException {
        return getAnnotations(Arrays.asList(id.split(",")), options, post);
    }

    public QueryResponse<VariantAnnotation> getAnnotations(List<String> ids, QueryOptions options) throws IOException {
        return getAnnotations(ids, options, false);
    }

    public QueryResponse<VariantAnnotation> getAnnotations(List<String> ids, QueryOptions options, boolean post) throws IOException {
        return execute(ids, "annotation", options, VariantAnnotation.class, post);
    }
}
