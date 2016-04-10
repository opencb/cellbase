package org.opencb.cellbase.app.cli.variant.annotation;

import org.opencb.biodata.models.variant.Variant;

import java.util.List;

/**
 * Created by fjlopez on 07/04/16.
 */
public class VariantAnnotationDiff {

    private List<SequenceOntologyTermComparisonObject> sequenceOntology;
    private Variant variant;

    public VariantAnnotationDiff() {}

    public List<SequenceOntologyTermComparisonObject> getSequenceOntology() {
        return sequenceOntology;
    }

    public void setSequenceOntology(List<SequenceOntologyTermComparisonObject> sequenceOntology) {
        this.sequenceOntology = sequenceOntology;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }
}
