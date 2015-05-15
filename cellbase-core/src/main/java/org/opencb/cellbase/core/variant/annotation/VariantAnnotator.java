package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;

import java.util.List;

/**
 * Created by fjlopez on 28/04/15.
 */
public interface VariantAnnotator {

    public boolean open();
    public List<VariantAnnotation> run(List<Variant> variantList);
    public void setVariantAnnotationList(List<VariantAnnotation> variantAnnotationList);
    public boolean close();

}
