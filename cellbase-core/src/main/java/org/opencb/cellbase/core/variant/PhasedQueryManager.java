package org.opencb.cellbase.core.variant;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.StudyEntry;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Collections;
import java.util.List;

public abstract class PhasedQueryManager<T> {

    public static final String PHASE_SET_TAG = "PS";
    private static final String UNPHASED_GENOTYPE_SEPARATOR = "/";
    private static final String PHASED_GENOTYPE_SEPARATOR = "|";
    private static final String MISSING_VALUE = ".";
    private static final String GENOTYPE_TAG = "GT";
    private static final String REFERENCE = "0";


//    abstract List<QueryResult<Variant>> run(List<Variant> variantList, List<QueryResult<Variant>> variantQueryResult);

    public List<QueryResult<Variant>> run(List<Variant> variantList, List<QueryResult<Variant>> variantQueryResultList) {
        // Go through all input variants and their corresponding query results
        for (int j = 0; j < variantQueryResultList.size(); j++) {
            QueryResult<Variant> variantQueryResult = variantQueryResultList.get(j);
            if (!variantQueryResult.getResult().isEmpty()) {
                // Variants are normalised and data from each of the sources (COSMIC, ClinVar, DOCM, etc.) integrated
                // during the build process. Only one variant record should be present per assembly.
                if (variantQueryResult.getResult().size() > 1) {
                    throw new RuntimeException("Unexpected: more than one result found in the clinical variant "
                            + "collection for variant " + variantQueryResult.getId() + ". Please, check.");
                }

                Variant matchedVariant = variantQueryResult.getResult().get(0);
                Variant queryVariant = variantList.get(j);
                List<T> annotationObjectList = getAnnotationObjectList(matchedVariant);
                // Phase is stored at the evidence entry/population frequency level, e.g.: there might be two ClinVar
                // RCVs for one variant:
                //   - In the first the variant is submitted as part of an MNV and therefore it is phased
                //   - In the second one the variant is submitted singleton and therefore it is not phased
                // both RCVs will be integrated in the same Variant object after decomposition as separate EvidenceEntry
                // objects, each with its corresponding phase information
                int i = 0;
                while (i < annotationObjectList.size()) {
                    T annotationObject = annotationObjectList.get(i);
                    List<Variant> databaseHaplotype = getHaplotype(annotationObject);
                    // Haplotype empty if EvidenceEntry/PopulationFrequency is not phased
                    if (databaseHaplotype.isEmpty()) {
                        i++;
                    } else {
                        // Sample   Cellbase  Match
                        // -------------------------------
                        // SNV      MNV       X
                        // MNV      MNV       ✓
                        // Missing genotypes in the input list will be considered as wildcards towards finding a
                        // matching haplotype (MNV) in the input list, since otherwise the clinical variant would not be
                        // returned
                        if (sameHaplotype(queryVariant, variantList, databaseHaplotype)) {
                            i++;
                        } else {
                            annotationObjectList.remove(i);
                        }
                        // Sample   Cellbase  Match
                        // -------------------------------
                        // SNV      SNV       ✓
                        // MNV      SNV       ✓
                    }
                }

                // Remove whole variant from the query result object if ended up without any evidence entry
                if (annotationObjectList.isEmpty()) {
                    reset(variantQueryResult);
                }
            }
        }

        return variantQueryResultList;
    }

    protected abstract List<Variant> getHaplotype(T annotationObject);

    protected abstract List<T> getAnnotationObjectList(Variant variant);

    protected boolean sameHaplotype(Variant queryVariant, List<Variant> inputVariantList,
                                    List<Variant> clinicalHaplotype) {
        // TODO: phase depends on the sample. Phased queries constrained to just one sample. The code below is
        // TODO: arbitrarily selecting the first one
        String queryPhaseSet = getSampleAttribute(queryVariant, PHASE_SET_TAG);
        String queryGenotype = getSampleAttribute(queryVariant, GENOTYPE_TAG);
        // Checks whether each variant for this clinical MNV (haplotype) is in the input list AND if all those in phase
        // in the input list
        for (Variant clinicalVariant : clinicalHaplotype) {
            Variant queryVariant1 = getVariant(inputVariantList, clinicalVariant);
            // It is not the same haplotype (MNV) if current variant cannot be found in the input list OR it is not in
            // the same chromosome copy as the first query variant
            if (queryVariant1 == null || !potentiallyInPhase(queryPhaseSet, queryGenotype, queryVariant1)) {
                return false;
            }
        }
        return true;
    }

    private Variant getVariant(List<Variant> variantList, Variant variant) {
        for (Variant variant1 : variantList) {
            // TODO: simple chr, start, ref, alt matching here - shall implement something fancier
            if (variant.getChromosome().equals(variant1.getChromosome())
                    && variant.getStart().equals(variant1.getStart())
                    && variant.getReference().equals(variant1.getReference())
                    && variant.getAlternate().equals(variant1.getAlternate())) {
                return variant1;
            }
        }
        return null;
    }

    /**
     * Will ONLY return false when it's absolutely clear that they are not in phase, i.e. phase sets are the same,
     * ploidy is the same, genotype is NOT missing, alleles are not reference and the alleles match in their
     * corresponding positions. Any of those conditions fail and will return true, since alleles could potentially be in
     * phase. This is in the interest of avoiding false negatives, i.e. two variants in phase might determine that a
     * certain result is not returned; two variants NOT in phase will not cause removal of any result from the
     * Query Result.
     * @param phaseSet String indicating the phase set; if two variants have the same phase set means we can detect
     *                 if the alternate alleles are or not in the same chromosome copy. Different or missing phase sets
     *                 indicate we cannot.
     * @param genotype VCF-like String of the form 0/1, 0|1, 1, ... that indicates the relative chromosome copy of the
     *                 alternate allele. TODO: multi allelic positions are not supported by this method.
     * @param variant  Variant object which phase and genotype are about to be checked against phaseSet and genotype
     * @return boolean to indicate whether are potentially in phase (cis) or not. See description above for more details
     */
    private boolean potentiallyInPhase(String phaseSet, String genotype, Variant variant) {
        // Missing values used as wildcard here: if phase set is not available will allow it to match with any other PS
        if (phaseSet == null) {
            return true;
        }

        // TODO: phase depends on the sample. Phased queries constrained to just one sample. The code below is
        // TODO: arbitrarily selecting the first one
        // Missing values used as wildcard here: if phase set is not available will allow it to match with any other PS
        String phaseSet1 = getSampleAttribute(variant, PHASE_SET_TAG);
        if (phaseSet1 == null) {
            return true;
        }

        // None of the PS is missing
        if (phaseSet.equals(phaseSet1)) {

            // TODO: phase depends on the sample. Phased queries constrained to just one sample. The code below is
            // TODO: arbitrarily selecting the first one
            String genotype1 = getSampleAttribute(variant, GENOTYPE_TAG);

            // Checks that in both genotypes there's something different than a reference allele, i.e. that none of
            // them is 0/0 (or 0 for haploid)
            if (potentiallyPresentAlternate(genotype) && potentiallyPresentAlternate(genotype1)) {

                // Missing values used as wildcard here: if genotype is not available or the genotype is not phased (which
                // should not occurr since PS is present) will allow it to match with any other genotype
                if (genotype == null || genotype.contains(UNPHASED_GENOTYPE_SEPARATOR)) {
                    return true;
                }

                // Missing values used as wildcard here: if genotype is not available or the genotype is not phased (which
                // should not occurr since PS is present) will allow it to match with any other genotype
                if (genotype1 == null || genotype1.contains(UNPHASED_GENOTYPE_SEPARATOR)) {
                    return true;
                }

                // None of the genotypes fully missing nor un-phased
                String[] genotypeParts = genotype.split(PHASED_GENOTYPE_SEPARATOR);
                String[] genotypeParts1 = genotype1.split(PHASED_GENOTYPE_SEPARATOR);

                // TODO: code below might not work for multiallelic positions
                // For hemizygous variants lets just consider that the phase is the same if each alternate allele is present
                // in both genotypes
                // First genotype alternate hemizygous
                if (genotypeParts.length == 1 && !genotypeParts[0].equals(REFERENCE)) {
                    // First and second genotype alternate hemizygous
                    if (genotypeParts1.length == 1 && !genotypeParts1[0].equals(REFERENCE)) {
                        return alternateAlleleMatch(genotypeParts[0], genotypeParts1[0]);
                        // First genotype alternate hemizygous, second genotype diploid
                    } else {
                        return alternateAlleleMatch(genotypeParts[0], genotypeParts1[0])
                                || alternateAlleleMatch(genotypeParts[0], genotypeParts1[2]);
                    }
                    // Second genotype alternate hemizygous
                } else if (genotypeParts1.length == 1 && !genotypeParts1[0].equals(REFERENCE)) {
                    // First genotype diploid, second genotype alternate hemizygous
                    return alternateAlleleMatch(genotypeParts1[0], genotypeParts[0])
                            || alternateAlleleMatch(genotypeParts1[0], genotypeParts[2]);

                    // Both genotypes diploid
                } else {
                    return alternateAlleleMatch(genotypeParts[0], genotypeParts1[0])
                            || alternateAlleleMatch(genotypeParts[2], genotypeParts1[2]);
                }
            // At least one of the genotypes contains just reference alleles. Clearly, alleles cannot be in phase since
            // one of them is not even present!
            } else {
                return false;
            }

        // If PS is different, as understood from VCF definition, this does not necessarily mean that both alleles
        // are not in the same copy but rather that it's unknown, i.e. each of them falls in two distinct regions for
        // which the phase was detected. Therefore, if the PS is different, still have to return true to stay on the
        // safe side
        } else {
            return true;
        }

    }

    /**
     * Alternate is potentially present if the genotype is missing or there's something different than a '0'.
     * TODO: this code might not work properly for multiallelic positions.
     * @param genotype String codifying for the genotype in VCF-like way, e.g. 0/1, 1|0, 0, ...
     * @return whether an alternate allele is potentially present. Alternate is potentially present if the genotype is
     * missing or there's something different than a '0'.
     */
    private boolean potentiallyPresentAlternate(String genotype) {

                // Missing genotype
        return genotype == null
                // Diploid e.g. 0/1
                || (genotype.length() == 3 && StringUtils.countMatches(genotype, REFERENCE) < 2)
                // Haploid e.g. 0
                || (genotype.length() == 1 && !genotype.contains(REFERENCE));


    }

    private boolean alternateAlleleMatch(String allele, String allele1) {
        return !allele.equals(REFERENCE)
                && !allele1.equals(REFERENCE)
                && (allele.equals(allele1) || isMissing(allele) || isMissing(allele1));
    }

    public static String getSampleAttribute(Variant variant, String attributeName) {
        List<StudyEntry> studyEntryList = variant.getStudies();
        if (studyEntryList != null && !studyEntryList.isEmpty()) {
            // TODO: phase depends on the sample. Phased queries constrained to just one sample. The code below is
            // TODO: arbitrarily selecting the first one
            StudyEntry studyEntry = studyEntryList.get(0);
            int attributePosition = studyEntry.getFormat().indexOf(attributeName);
            if (attributePosition != -1) {
                List<List<String>> samplesData = studyEntry.getSamplesData();
                if (samplesData != null && !samplesData.isEmpty()) {
                    List<String> firstSampleData = samplesData.get(0);
                    if (firstSampleData != null
                            && !firstSampleData.isEmpty()
                            && attributePosition < firstSampleData.size()
                            && !isMissing(firstSampleData.get(attributePosition))) {
                        return firstSampleData.get(attributePosition);
                    }
                }
            }
        }

        return null;
    }

    public static boolean isMissing(String field) {
        return StringUtils.isBlank(field) || field.equals(MISSING_VALUE);
    }

    protected void reset(QueryResult<Variant> variantQueryResult) {
        variantQueryResult.setResult(Collections.emptyList());
        variantQueryResult.setNumResults(0);
        variantQueryResult.setNumTotalResults(0);
    }
}
