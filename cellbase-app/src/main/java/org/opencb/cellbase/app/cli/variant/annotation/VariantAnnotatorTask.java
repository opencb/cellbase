package org.opencb.cellbase.app.cli.variant.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.opencb.commons.run.ParallelTaskRunner;

import java.util.List;

/**
 * Created by fjlopez on 11/02/16.
 */
public class VariantAnnotatorTask implements
        ParallelTaskRunner.TaskWithException<Variant, Variant, Exception> {

    private List<VariantAnnotator> variantAnnotatorList;

    public VariantAnnotatorTask(List<VariantAnnotator> variantAnnotatorList) {
        this.variantAnnotatorList = variantAnnotatorList;
    }

    public void pre() {
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.open();
        }
    }

    public List<Variant> apply(List<Variant> batch) throws Exception {
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.run(batch);
        }
        return batch;
    }

    public void post() {
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.close();
        }
    }

}
