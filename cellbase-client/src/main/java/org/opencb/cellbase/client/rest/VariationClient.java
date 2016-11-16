package org.opencb.cellbase.client.rest;

import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.biodata.models.core.Xref;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by swaathi on 23/05/16.
 */
public class VariationClient extends FeatureClient<Variant> {

    private static final int VARIANT_ANNOTATION_BATCH_SIZE = 200;
    private final VariationClient.VariantClient variantClient;

    public VariationClient(String species, ClientConfiguration configuration) {
        super(species, configuration);
        this.clazz = Variant.class;

        this.category = "feature";
        this.subcategory = "variation";
        variantClient = new VariationClient.VariantClient(species, configuration);
    }

    /**
     * Internal class to call to the separated endpoint for annotation.
     * Do not modify category or subcategory! Concurrent calls may cause unexpected behaviour
     */
    private static final class VariantClient extends FeatureClient<Variant> {
        private VariantClient(String species, ClientConfiguration configuration) {
            super(species, configuration);
            this.clazz = Variant.class;

            this.category = "genomic";
            this.subcategory = "variant";
        }

        private QueryResponse<VariantAnnotation> getAnnotations(List<String> ids, QueryOptions options, boolean post) throws IOException {
            return execute(ids, "annotation", options, VariantAnnotation.class, post);
        }
    }

    public QueryResponse<String> getAllConsequenceTypes(Query query) throws IOException {
        return execute("consequence_types", query, new QueryOptions(), String.class);
    }

    public QueryResponse<String> getConsequenceTypeById(String id, QueryOptions options) throws IOException {
        return execute(id, "consequence_type", options, String.class);
    }
//    check data model returned
    public QueryResponse<RegulatoryFeature> getRegulatory(String id, QueryOptions options) throws IOException {
        return execute(id, "regulatory", options, RegulatoryFeature.class);
    }

    public QueryResponse<String> getPhenotype(String id, QueryOptions options) throws IOException {
        return execute(id, "phenotype", options, String.class);
    }

    public QueryResponse<String> getSequence(String id, QueryOptions options) throws IOException {
        return execute(id, "sequence", options, String.class);
    }

    public QueryResponse<String> getPopulationFrequency(String id, QueryOptions options) throws IOException {
        return execute(id, "population_frequency", options, String.class);
    }

    public QueryResponse<Xref> getXref(String id, QueryOptions options) throws IOException {
        return execute(id, "xref", options, Xref.class);
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
//        //Do not modify this variables! Will fail with concurrent queries!
//        this.category = "genomic";
//        this.subcategory = "variant";
//        try {
//            return execute(ids, "annotation", options, VariantAnnotation.class, post);
//        } finally {
//            this.category = "feature";
//            this.subcategory = "variation";
//        }
        return variantClient.getAnnotations(ids, options, post);

//        if (options == null) {
//            options = new QueryOptions();
//        }
//        int numThreads = options.getInt("numThreads",4);
//
//        if (ids == null) {
//            return null;
//        }
//
//        // If the list contain less than VARIANT_ANNOTATION_BATCH_SIZE variants then we can call to the normal method.
//        if (ids.size() <= VARIANT_ANNOTATION_BATCH_SIZE) {
//            return getAnnotations(StringUtils.join(ids, ","), options);
//        }
//
//        // but if there are more than VARIANT_ANNOTATION_BATCH_SIZE variants then we launch several threads to increase performance.
//        // First we prepare the List of String with 200 ids per String.
//        List<String> idsList = new ArrayList<>();
//        if (ids.size() > VARIANT_ANNOTATION_BATCH_SIZE) {
//            int batchSize = VARIANT_ANNOTATION_BATCH_SIZE;
//            for (int batch = 0; batch < ids.size(); batch += VARIANT_ANNOTATION_BATCH_SIZE) {
//                // Swaathi, take a look to this commented line you implemented, there are several important issues (4 actually):
//                // idsList.add(i, ids.subList(batch, batch + 199).toString());
//
//                // Fixed code, please read it carefully:
//                if (batch + batchSize > ids.size()) {
//                    batchSize = ids.size() - batch;
//                }
//                String variantListString = StringUtils.join(ids.subList(batch, batch + batchSize), ",");
//                idsList.add(variantListString);
//            }
//        }
//
//        // Second we launch all calls using numThreads threads.
////        Future<QueryResponse<VariantAnnotation>> future;
//        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
//        List<Future<QueryResponse<VariantAnnotation>>> futureList = new ArrayList<>(idsList.size());
////        for (int i=0; i < 4; i++) {
////            future = executorService.submit(new AnnotatorRunnable(ids, options));
////        }
//        for (int i = 0; i < idsList.size(); i++) {
//            futureList.add(executorService.submit(new AnnotatorRunnable(Collections.singletonList(idsList.get(i)), options)));
//        }
//
////        QueryResponse<VariantAnnotation> response;
////        QueryResponse<VariantAnnotation> finalResponse = null;
////        List<VariantAnnotation> variantAnnotations = new ArrayList<>(ids.size());
//        List<QueryResult<VariantAnnotation>> queryResults = new ArrayList<>(ids.size());
//
//        for (Future<QueryResponse<VariantAnnotation>> responseFuture : futureList) {
//            try {
////                response = futureList.get(i).get();
////                queryResults.add((QueryResult<VariantAnnotation>) response.allResults());
//                while (!responseFuture.isDone()) {
//                    Thread.sleep(5);
//                }
//                queryResults.addAll(responseFuture.get().getResponse());
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
//        }
//
//        QueryResponse<VariantAnnotation> finalResponse = new QueryResponse<>();
//        finalResponse.setResponse(queryResults);
////        finalResponse.setResponse(queryResults);
////        try {
////            response = future.get();
////        } catch (InterruptedException | ExecutionException e) {
////            e.printStackTrace();
////        }
//        executorService.shutdown();
//
//        return finalResponse;
    }

//    class AnnotatorRunnable implements Callable<QueryResponse<VariantAnnotation>> {
//
//        private List<String> ids;
//        private QueryOptions options;
//
//        public AnnotatorRunnable(List<String> ids, QueryOptions options) {
//            this.ids = ids;
//            this.options = options;
//        }
//
//        @Override
//        public QueryResponse<VariantAnnotation> call() throws Exception {
//            return execute(ids, "annotation", options, VariantAnnotation.class);
//        }
//    }
}
