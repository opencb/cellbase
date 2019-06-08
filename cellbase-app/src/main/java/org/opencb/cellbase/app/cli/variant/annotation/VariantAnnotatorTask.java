package org.opencb.cellbase.app.cli.variant.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.tools.variant.VariantNormalizer;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.opencb.commons.run.ParallelTaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fjlopez on 11/02/16.
 */
public class VariantAnnotatorTask implements
        ParallelTaskRunner.TaskWithException<Variant, Variant, Exception> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final VariantNormalizer normalizer;
    private List<VariantAnnotator> variantAnnotatorList;

    public VariantAnnotatorTask(List<VariantAnnotator> variantAnnotatorList) {
        this(variantAnnotatorList, null);
    }

    public VariantAnnotatorTask(List<VariantAnnotator> variantAnnotatorList, VariantNormalizer normalizer) {
        this.variantAnnotatorList = variantAnnotatorList;
        this.normalizer = normalizer;
    }

    public void pre() {
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.open();
        }
    }

    public List<Variant> apply(List<Variant> batch) throws Exception {

        List<Variant> normalizedVariantList;
        if (normalizer != null) {
            normalizedVariantList = new ArrayList<>(batch.size());
            for (Variant variant : batch) {
                try {
                    normalizedVariantList.addAll(normalizer.apply(Collections.singletonList(variant)));
                } catch (RuntimeException e) {
                    logger.warn("Error found during variant normalization. Variant: {}", variant.toString());
                    logger.warn("This variant will be skipped and annotation will continue");
                    logger.warn("Full stack trace", e);
                }
            }
        } else {
            normalizedVariantList = batch;
        }

        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.run(normalizedVariantList);
        }
        return batch;
    }

    public void post() {
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.close();
        }
    }

}
