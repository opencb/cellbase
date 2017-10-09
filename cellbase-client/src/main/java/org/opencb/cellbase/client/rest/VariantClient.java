package org.opencb.cellbase.client.rest;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public final class VariantClient extends FeatureClient<Variant> {

    public VariantClient(String species, String assembly, ClientConfiguration configuration) {
        super(species, assembly, configuration);
        this.clazz = Variant.class;

        this.category = "genomic";
        this.subcategory = "variant";
    }

    @Deprecated
    public QueryResponse<VariantAnnotation> getAnnotationByVariantIds(String id, QueryOptions options) throws IOException {
        return this.getAnnotationByVariantIds(Arrays.asList(id.split(",")), options, false);
    }

    @Deprecated
    public QueryResponse<VariantAnnotation> getAnnotationByVariantIds(String id, QueryOptions options, boolean post) throws IOException {
        return this.getAnnotationByVariantIds(Arrays.asList(id.split(",")), options, post);
    }

    public QueryResponse<VariantAnnotation> getAnnotationByVariantIds(List<String> ids, QueryOptions options) throws IOException {
        return this.getAnnotationByVariantIds(ids, options, false);
    }

    public QueryResponse<VariantAnnotation> getAnnotationByVariantIds(List<String> ids, QueryOptions options, boolean post)
            throws IOException {
        return execute(ids, "annotation", options, VariantAnnotation.class, post);
    }


    public QueryResponse<Variant> annotate(List<Variant> variants, QueryOptions options) throws IOException {
        return annotate(variants, options, false);
    }

    public QueryResponse<Variant> annotate(List<Variant> variants, QueryOptions options, boolean post) throws IOException {
        List<String> variantIds = getVariantAnnotationIds(variants);
        QueryResponse<VariantAnnotation> annotations = this.getAnnotationByVariantIds(variantIds, options, post);

        int timePerId = annotations.getTime() / variants.size();
        List<QueryResult<Variant>> annotatedVariants = new ArrayList<>(variants.size());
        for (int i = 0; i < variants.size(); i++) {
            variants.get(i).setAnnotation(annotations.getResponse().get(i).first());
            annotatedVariants.add(new QueryResult<>(variantIds.get(i), timePerId, 1, 1, "", "",
                    Collections.singletonList(variants.get(i))));
        }

        return new QueryResponse<>(configuration.getVersion(), annotations.getTime(), options, annotatedVariants);
    }

    public QueryResponse<VariantAnnotation> getAnnotation(List<Variant> variants, QueryOptions options) throws IOException {
        return getAnnotation(variants, options, false);
    }

    public QueryResponse<VariantAnnotation> getAnnotation(List<Variant> variants, QueryOptions options, boolean post) throws IOException {
        return execute(getVariantAnnotationIds(variants), "annotation", options, VariantAnnotation.class, post);
    }


    // FIXME Next two methods should be moved near the Variant Annotation tool
    public String getVariantAnnotationId(Variant variant) {
        return variant.getChromosome() + ":" + variant.getStart() + ":" + variant.getReference() + ":" + variant.getAlternate();
    }

    public List<String> getVariantAnnotationIds(List<Variant> variants) {
        if (variants == null) {
            return null;
        }

        List<String> variantIds = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            variantIds.add(getVariantAnnotationId(variant));
        }
        return variantIds;
    }

}
