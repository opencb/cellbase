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

    private static final String VARIANT_STRING_PATTERN = "[ACGT]*";
    private VariantAnnotator variantAnnotator;

    public BenchmarkTask(VariantAnnotator variantAnnotator) {
        this.variantAnnotator = variantAnnotator;
    }

    public void pre() {
        variantAnnotator.open();
    }

    public List<Pair<VariantAnnotationDiff, VariantAnnotationDiff>> apply(List<VariantAnnotation> batch) {
        removeInvalidVariants(batch);
        List<Variant> cellBaseBatch = createEmptyVariantList(batch);
        variantAnnotator.run(cellBaseBatch);
        List<Pair<VariantAnnotationDiff, VariantAnnotationDiff>> comparisonResultList = new ArrayList<>();
        for (int i = 0; i < batch.size(); i++) {
            // Variants such as MT:453:TTT:ATT are skipped for the benchmark - will not have CellBase annotation
            // compatible with VEP annotation and therefore consequenceTypeList = null
            if (batch.get(i).getConsequenceTypes() != null
                    && cellBaseBatch.get(i).getAnnotation().getConsequenceTypes() != null) {
                Pair<VariantAnnotationDiff, VariantAnnotationDiff> comparisonResult = compare(batch.get(i),
                        cellBaseBatch.get(i).getAnnotation());
                comparisonResult.getLeft().setVariantAnnotation(batch.get(i));
                comparisonResult.getRight().setVariantAnnotation(cellBaseBatch.get(i).getAnnotation());
                comparisonResultList.add(comparisonResult);
            }
        }
        return comparisonResultList;
    }

    private void removeInvalidVariants(List<VariantAnnotation> variantAnnotationList) {
        int i = 0;
        while (i < variantAnnotationList.size()) {
            if (isValid(variantAnnotationList.get(i))) {
                i++;
            } else {
                variantAnnotationList.remove(i);
            }
        }
    }

    /**
     * Checks whether a variant is valid.
     *
     * @param variantAnnotation Variant object to be checked.
     * @return   true/false depending on whether 'variant' does contain valid values. Currently just a simple check of
     * reference/alternate attributes being strings of [A,C,G,T] of length >= 0 is performed to detect cases such as
     * 19:13318673:(CAG)4:(CAG)5 which are not currently supported by CellBase. Ref and alt alleles must be different
     * as well for the variant to be valid. Functionality of the method may be improved in the future.
     */
    private boolean isValid(VariantAnnotation variantAnnotation) {
        return (variantAnnotation.getAlternate().matches(VARIANT_STRING_PATTERN)
//                && variantAnnotation.getReference().matches(VARIANT_STRING_PATTERN)
                && !variantAnnotation.getAlternate().equals(variantAnnotation.getReference()));
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
        Set<SequenceOntologyTermComparisonObject> sequenceOntologySet1bak = new HashSet<>(sequenceOntologySet1);
        sequenceOntologySet1.removeAll(sequenceOntologySet2);
        sequenceOntologySet2.removeAll(sequenceOntologySet1bak);
        if (sequenceOntologySet1.size() > 0) {
            result.getLeft().setSequenceOntology(new ArrayList(sequenceOntologySet1));
        }
        if (sequenceOntologySet2.size() > 0) {
            result.getRight().setSequenceOntology(new ArrayList(sequenceOntologySet2));
        }
    }

    private Set<SequenceOntologyTermComparisonObject> getSequenceOntologySet(List<ConsequenceType> consequenceTypeList) {
        if (consequenceTypeList != null) {
            Set<SequenceOntologyTermComparisonObject> set = new HashSet<>(consequenceTypeList.size());
            for (ConsequenceType consequenceType : consequenceTypeList) {
                for (SequenceOntologyTerm sequenceOntologyTerm : consequenceType.getSequenceOntologyTerms()) {
                    // Expected many differences depending on the regulatory source databases used by the annotators.
                    // Better skip regulatory_region_variant annotations
                    if (!(sequenceOntologyTerm.getName().equals(VariantAnnotationUtils.REGULATORY_REGION_VARIANT)
                            || sequenceOntologyTerm.getName().equals(VariantAnnotationUtils.TF_BINDING_SITE_VARIANT))) {
                        set.add(new SequenceOntologyTermComparisonObject(consequenceType.getEnsemblTranscriptId(),
                                sequenceOntologyTerm));
                    }
                }
            }

            return set;
        } else {
            return null;
        }
    }

    private List<Variant> createEmptyVariantList(List<VariantAnnotation> variantAnnotationList) {
        List<Variant> newVariantList = new ArrayList<>(variantAnnotationList.size());
        for (VariantAnnotation variantAnnotation : variantAnnotationList) {
            newVariantList.add(new Variant(variantAnnotation.getChromosome(), variantAnnotation.getStart(),
                    variantAnnotation.getReference(), variantAnnotation.getAlternate()));
        }

        return newVariantList;
    }

    public void post() {
        variantAnnotator.close();
    }
}
