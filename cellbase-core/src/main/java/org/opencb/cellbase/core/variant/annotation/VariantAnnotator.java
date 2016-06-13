package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.variant.Variant;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by fjlopez on 28/04/15.
 */
public interface VariantAnnotator {

    boolean open();

    void run(List<Variant> variantList) throws InterruptedException, ExecutionException;

    boolean close();

}
