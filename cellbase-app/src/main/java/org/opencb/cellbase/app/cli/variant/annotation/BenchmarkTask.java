package org.opencb.cellbase.app.cli.variant.annotation;

import org.apache.commons.lang3.tuple.Pair;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.SequenceOntologyTerm;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.opencb.commons.run.ParallelTaskRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by fjlopez on 07/04/16.
 */
public class BenchmarkTask implements ParallelTaskRunner.Task<VariantAnnotation, Pair<VariantAnnotationDiff, VariantAnnotationDiff>> {

    private VariantAnnotator variantAnnotator;

    public BenchmarkTask(VariantAnnotator variantAnnotator) {
        this.variantAnnotator = variantAnnotator;
    }

    public void pre() {
        variantAnnotator.open();
    }

    public List<Pair<VariantAnnotationDiff, VariantAnnotationDiff>> apply(List<VariantAnnotation> batch) {
        List<Variant> cellBaseBatch = createEmptyVariantList(batch);
        variantAnnotator.run(cellBaseBatch);
        List<Pair<VariantAnnotationDiff, VariantAnnotationDiff>> comparisonResultList = new ArrayList<>();
        for (int i = 0; i < batch.size(); i++) {
            Pair<VariantAnnotationDiff, VariantAnnotationDiff> comparisonResult = compare(batch.get(i),
                    cellBaseBatch.get(i).getAnnotation());
            if (comparisonResult != null) {
                comparisonResult.getLeft().setVariantAnnotation(batch.get(i));
                comparisonResult.getRight().setVariantAnnotation(cellBaseBatch.get(i).getAnnotation());
                comparisonResultList.add(comparisonResult);
            }
        }
        return comparisonResultList;
    }

    private Pair<VariantAnnotationDiff, VariantAnnotationDiff> compare(VariantAnnotation variant1, VariantAnnotation variant2) {
        Pair<VariantAnnotationDiff, VariantAnnotationDiff> result
                = Pair.of(new VariantAnnotationDiff(), new VariantAnnotationDiff());
        compareSequenceOntologyTerms(result, variant1.getConsequenceTypes(),
                variant2.getConsequenceTypes());

        return result;
    }

    private void compareSequenceOntologyTerms(Pair<VariantAnnotationDiff, VariantAnnotationDiff> result,
                                              List<ConsequenceType> consequenceTypeList1,
                                              List<ConsequenceType> consequenceTypeList2) {
        Set<SequenceOntologyTermComparisonObject> sequenceOntologySet1 = getSequenceOntologySet(consequenceTypeList1);
        Set<SequenceOntologyTermComparisonObject> sequenceOntologySet2 = getSequenceOntologySet(consequenceTypeList2);
        sequenceOntologySet1.removeAll(sequenceOntologySet2);
        sequenceOntologySet2.removeAll(sequenceOntologySet1);
        result.getLeft().setSequenceOntology(new ArrayList(sequenceOntologySet1));
        result.getRight().setSequenceOntology(new ArrayList(sequenceOntologySet2));
    }

    private Set<SequenceOntologyTermComparisonObject> getSequenceOntologySet(List<ConsequenceType> consequenceTypeList) {
        Set<SequenceOntologyTermComparisonObject> set = new HashSet<>(consequenceTypeList.size());
        for (ConsequenceType consequenceType : consequenceTypeList) {
            for (SequenceOntologyTerm sequenceOntologyTerm : consequenceType.getSequenceOntologyTerms()) {
                // Expected many differences depending on the regulatory source databases used by the annotators.
                // Better skip regulatory_region_variant annotations
                if (!sequenceOntologyTerm.getName().equals(VariantAnnotationUtils.REGULATORY_REGION_VARIANT)) {
                    set.add(new SequenceOntologyTermComparisonObject(consequenceType.getEnsemblTranscriptId(),
                            sequenceOntologyTerm));
                }
            }
        }

        return set;
    }

    private List<Variant> createEmptyVariantList(List<VariantAnnotation> variantList) {
        List<Variant> newVariantList = new ArrayList<>(variantList.size());
        for (Variant variant : variantList) {
            newVariantList.add(new Variant(variant.getChromosome(), variant.getStart(), variant.getReference(),
                    variant.getAlternate()));
        }

        return newVariantList;
    }

    public void post() {
        variantAnnotator.close();
    }
}
