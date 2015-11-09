package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;

import java.util.List;

/**
 * Created by fjlopez on 28/04/15.
 */
public interface VariantAnnotator {

    boolean open();

    List<VariantAnnotation> run(List<Variant> variantList);

    void setVariantAnnotationList(List<VariantAnnotation> variantAnnotationList);

    boolean close();

}
