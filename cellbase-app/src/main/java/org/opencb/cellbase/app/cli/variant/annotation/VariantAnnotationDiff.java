package org.opencb.cellbase.app.cli.variant.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;

import java.util.List;

/**
 * Created by fjlopez on 07/04/16.
 */
public class VariantAnnotationDiff {

    private List<SequenceOntologyTermComparisonObject> sequenceOntology;
    private VariantAnnotation variantAnnotation;

    public VariantAnnotationDiff() {}

    public List<SequenceOntologyTermComparisonObject> getSequenceOntology() {
        return sequenceOntology;
    }

    public void setSequenceOntology(List<SequenceOntologyTermComparisonObject> sequenceOntology) {
        this.sequenceOntology = sequenceOntology;
    }

    public VariantAnnotation getVariantAnnotation() {
        return variantAnnotation;
    }

    public void setVariantAnnotation(VariantAnnotation variantAnnotation) {
        this.variantAnnotation = variantAnnotation;
    }
}
