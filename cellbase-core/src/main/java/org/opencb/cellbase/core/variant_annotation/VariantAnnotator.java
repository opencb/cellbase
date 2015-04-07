package org.opencb.cellbase.core.variant_annotation;

import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.client.CellBaseClient;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by fjlopez on 02/03/15.
 */
public class VariantAnnotator implements Callable<Integer> {

    private Logger logger;
    private BlockingQueue<List<GenomicVariant>> variantQueue;
    private BlockingQueue<List<VariantAnnotation>> variantAnnotationQueue;
    private CellBaseClient cellBaseClient;

    public VariantAnnotator(BlockingQueue<List<GenomicVariant>> variantQueue,
                            BlockingQueue<List<VariantAnnotation>> variantAnnotationQueue, CellBaseClient cellBaseClient) {

        this.variantQueue = variantQueue;
        this.variantAnnotationQueue = variantAnnotationQueue;
        this.cellBaseClient = cellBaseClient;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public Integer call() {
        Integer annotatedObjects = 0;
        boolean finished = false;
        while (!finished) {
            try {
                logger.info("Annotator waits for new variants");
                List<GenomicVariant> batch = variantQueue.take();
                logger.info("Annotator receives " + batch.size() + " new variants");
                if (batch == VariantAnnotatorRunner.VARIANT_POISON_PILL) {
                    logger.info("Annotator finishes");
                    finished = true;
                } else {
                    logger.info("Annotator sends " + batch.size() + " new variants for annotation. Waiting for the result.");
                    QueryResponse<QueryResult<VariantAnnotation>> response =
                            cellBaseClient.getFullAnnotation(CellBaseClient.Category.genomic,
//                                    CellBaseClient.SubCategory.variant, batch, new QueryOptions());
                                    CellBaseClient.SubCategory.variant, batch, new QueryOptions("post",true));
                    List<VariantAnnotation> variantAnnotationList = new ArrayList<>(batch.size());
                    for (QueryResult<VariantAnnotation> queryResult : response.getResponse()) {
                        variantAnnotationList.add(queryResult.getResult().get(0));
                    }
                    logger.info("Annotator queues for writing "+batch.size()+" new variants and their annotation");
                    variantAnnotationQueue.put(variantAnnotationList);
                    annotatedObjects += variantAnnotationList.size();
                }
            } catch (InterruptedException e) {
                logger.error("Annotator thread interrupted: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error annotating batch: " + e.getMessage());
            }
        }
        logger.debug("'annotation' finished. " + annotatedObjects + " records annotated");
        try {
            // Poison Pill to variant writer so it knows that there are no more batchs to consume
            variantAnnotationQueue.put(VariantAnnotatorRunner.ANNOTATION_POISON_PILL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return annotatedObjects;

    }
}
