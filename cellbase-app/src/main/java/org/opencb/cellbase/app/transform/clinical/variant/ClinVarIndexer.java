package org.opencb.cellbase.app.transform.clinical.variant;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.opencb.biodata.formats.variant.clinvar.ClinvarParser;
import org.opencb.biodata.formats.variant.clinvar.v53jaxb.*;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.ProgressLogger;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import org.opencb.biodata.formats.variant.clinvar.v24jaxb.*;

/**
 * Created by fjlopez on 28/09/16.
 */
public class ClinVarIndexer extends ClinicalIndexer {

    private static final String CLINVAR_CONTEXT_V53 = "org.opencb.biodata.formats.variant.clinvar.v53jaxb";

    private static final String CLINVAR_NAME = "clinvar";
    private static final int VARIANT_SUMMARY_CHR_COLUMN = 18;
    private static final int VARIANT_SUMMARY_START_COLUMN = 19;
    private static final int VARIANT_SUMMARY_END_COLUMN = 20;
    private static final int VARIANT_SUMMARY_REFERENCE_COLUMN = 21;
    private static final int VARIANT_SUMMARY_ALTERNATE_COLUMN = 22;
    private static final int VARIANT_SUMMARY_CLINSIG_COLUMN = 6;
    private static final int VARIANT_SUMMARY_GENE_COLUMN = 4;
    private static final int VARIANT_SUMMARY_REVIEW_COLUMN = 24;
    private static final int VARIANT_SUMMARY_ORIGIN_COLUMN = 14;
    private static final int VARIANT_SUMMARY_PHENOTYPE_COLUMN = 13;
    private static final int VARIATION_ALLELE_VARIATION_COLUMN = 0;
    private static final int VARIATION_ALLELE_TYPE_COLUMN = 1;
    private static final int VARIATION_ALLELE_ALLELE_COLUMN = 2;
    private static final String SOMATIC = "somatic";
    private static final String CLINICAL_SIGNIFICANCE_IN_SOURCE_FILE = "ClinicalSignificance_in_source_file";
    private static final String REVIEW_STATUS_IN_SOURCE_FILE = "ReviewStatus_in_source_file";
    private static final String TRAIT = "trait";
    private static final String MODE_OF_INHERITANCE = "modeOfInheritance";
    private static final String GENOTYPESET = "GenotypeSet";
    private static final String COMPOUND_HETEROZYGOTE = "CompoundHeterozygote";
    private static final String DIPLOTYPE = "Diplotype";
    private static final String VARIANT = "Variant";
    private final Path clinvarXMLFile;
    private final Path clinvarSummaryFile;
    private final Path clinvarVariationAlleleFile;
    private final Path clinvarEFOFile;
    private final String assembly;
    private int numberSomaticRecords = 0;
    private int numberGermlineRecords = 0;
    private int numberNoDiseaseTrait = 0;
    private int numberMultipleInheritanceModels = 0;
    private static final Set<ModeOfInheritance> DOMINANT_TERM_SET
            = new HashSet<>(Arrays.asList(ModeOfInheritance.monoallelic,
            ModeOfInheritance.monoallelic_maternally_imprinted,
            ModeOfInheritance.monoallelic_not_imprinted, ModeOfInheritance.monoallelic_paternally_imprinted));
    private static final Set<ModeOfInheritance> RECESSIVE_TERM_SET
            = new HashSet<>(Arrays.asList(ModeOfInheritance.biallelic));

    public ClinVarIndexer(Path clinvarXMLFile, Path clinvarSummaryFile, Path clinvarVariationAlleleFile,
                          Path clinvarEFOFile, boolean normalize, Path genomeSequenceFilePath, String assembly,
                          RocksDB rdb) throws FileNotFoundException {
        super(genomeSequenceFilePath);
        this.rdb = rdb;
        this.clinvarXMLFile = clinvarXMLFile;
        this.clinvarSummaryFile = clinvarSummaryFile;
        this.clinvarVariationAlleleFile = clinvarVariationAlleleFile;
        this.clinvarEFOFile = clinvarEFOFile;
        this.normalize = normalize;
        this.genomeSequenceFilePath = genomeSequenceFilePath;
        this.assembly = assembly;
    }

    public void index() throws RocksDBException {
        try {
            Map<String, EFO> traitsToEfoTermsMap = loadEFOTerms();
            Map<String, List<AlleleLocationData>> rcvToAlleleLocationData = parseVariantSummary(traitsToEfoTermsMap);

            logger.info("Unmarshalling clinvar file " + clinvarXMLFile + " ...");
            JAXBElement<ReleaseType> clinvarRelease = unmarshalXML(clinvarXMLFile);
            logger.info("Done");

            logger.info("Serializing clinvar records that have Sequence Location for Assembly " + assembly + " ...");
            ProgressLogger progressLogger = new ProgressLogger("Parsed XML records:", clinvarRelease.getValue().getClinVarSet().size(),
                    200).setBatchSize(10000);
            for (PublicSetType publicSet : clinvarRelease.getValue().getClinVarSet()) {
                List<AlleleLocationData> alleleLocationDataList =
                        rcvToAlleleLocationData.get(publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc());
                if (alleleLocationDataList != null) {
                    boolean success = false;
                    // Actually this list is currently not allowed to be > 2
                    for (int i = 0; i < alleleLocationDataList.size(); i++) {
                        String mateVariantString = getMateVariantStringByAlleleLocationData(i, alleleLocationDataList);
                        // updateRocksDB may fail (false) if normalisation process fails
                        success = success || updateRocksDB(alleleLocationDataList.get(i), publicSet, mateVariantString,
                                traitsToEfoTermsMap);
                    }
                    if (success) {
                        numberIndexedRecords++;
                    }
                }
                progressLogger.increment(1);
                totalNumberRecords++;
            }
            logger.info("Done");
            printSummary();
        } catch (RocksDBException e) {
            logger.error("Error reading/writing from/to the RocksDB index while indexing ClinVar");
            throw e;
        } catch (JAXBException e) {
            logger.error("Error unmarshalling clinvar Xml file " + clinvarXMLFile + ": " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error indexing clinvar Xml file: " + e.getMessage());
        }
    }

    private String getMateVariantStringByAlleleLocationData(int i, List<AlleleLocationData> alleleLocationDataList) {
        StringBuilder mateVariantString = new StringBuilder();
        // Generate a string with comma separated list of variant strings including all other variants but the one
        // in position i
        for (int j = 0; j < alleleLocationDataList.size(); j++) {
            if (j != i) {
                SequenceLocation sequenceLocation = alleleLocationDataList.get(j).getSequenceLocation();
                String variantString = getNormalisedVariantString(sequenceLocation.getChromosome(),
                        sequenceLocation.getStart(), sequenceLocation.getReference(),
                        sequenceLocation.getAlternate()).replace(" ", "");
                // May be null if normalisation fails
                if (variantString != null) {
                    // First variant string must avoid including the separator
                    if (mateVariantString.length() > 0) {
                        mateVariantString.append(",");
                    }
                    mateVariantString.append(variantString);
                }
            }
        }
        return mateVariantString.length() == 0 ? null : mateVariantString.toString();
    }

    private void printSummary() {
        logger.info("Total number of parsed ClinVar records: {}", totalNumberRecords);
        logger.info("Number of indexed Clinvar records: {}", numberIndexedRecords);
        logger.info("Number of new variants in ClinVar not previously indexed in RocksDB: {}", numberNewVariants);
        logger.info("Number of updated variants during ClinVar indexing: {}", numberVariantUpdates);
        logger.info("Number of ClinVar germline variants: {}", numberGermlineRecords);
        logger.info("Number of ClinVar somatic variants: {}", numberSomaticRecords);
        logger.info("Number of ClinVar records without a \"disease\" trait: {}", numberNoDiseaseTrait);
        logger.info("Number of ClinVar records with multiple inheritance models: {}", numberMultipleInheritanceModels);
    }

    private boolean updateRocksDB(SequenceLocation sequenceLocation, String variationId, String[] lineFields,
                               String mateVariantString, Map<String, EFO> traitsToEfoTermsMap)
            throws RocksDBException, IOException {
        byte[] key = getNormalisedKey(sequenceLocation.getChromosome(),
                sequenceLocation.getStart(), sequenceLocation.getReference(),
                sequenceLocation.getAlternate());
        // May be null if normalisation process failed
        if (key != null) {
            VariantAnnotation variantAnnotation = getVariantAnnotation(key);
            //        List<EvidenceEntry> evidenceEntryList = getEvidenceEntryList(key);
            addNewEntries(variantAnnotation, variationId, lineFields, mateVariantString, traitsToEfoTermsMap);
            rdb.put(key, jsonObjectWriter.writeValueAsBytes(variantAnnotation));
            return true;
        }

        return false;
    }

    private boolean updateRocksDB(AlleleLocationData alleleLocationData, PublicSetType publicSet,
                               String mateVariantString, Map<String, EFO> traitsToEfoTermsMap)
            throws RocksDBException, IOException {

        byte[] key = getNormalisedKey(alleleLocationData.getSequenceLocation().getChromosome(),
                alleleLocationData.getSequenceLocation().getStart(),
                alleleLocationData.getSequenceLocation().getReference(),
                alleleLocationData.getSequenceLocation().getAlternate());
        if (key != null) {
            VariantAnnotation variantAnnotation = getVariantAnnotation(key);
            //        List<EvidenceEntry> evidenceEntryList = getVariantAnnotation(key);
            addNewEntries(variantAnnotation, publicSet, alleleLocationData.getAlleleId(), mateVariantString,
                    traitsToEfoTermsMap);

            rdb.put(key, jsonObjectWriter.writeValueAsBytes(variantAnnotation));
            return true;
        }

        return false;
    }

    private void addNewEntries(VariantAnnotation variantAnnotation, String variationId, String[] lineFields,
                               String mateVariantString, Map<String, EFO> traitsToEfoTermsMap) {

        EvidenceSource evidenceSource = new EvidenceSource(EtlCommons.CLINVAR_DATA, null, null);
        // Create a set to avoid situations like germline;germline;germline
        List<AlleleOrigin> alleleOrigin = null;
        if (!EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_ORIGIN_COLUMN])) {
            Set<String> originSet = new HashSet<String>(Arrays.asList(lineFields[VARIANT_SUMMARY_ORIGIN_COLUMN]
                    .toLowerCase().split(";")));
            alleleOrigin = getAlleleOriginList(new ArrayList<>(originSet));
        }

        List<HeritableTrait> heritableTraitList = Collections.emptyList();
        if (!EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_PHENOTYPE_COLUMN])) {
            Set<String> phenotypeSet = new HashSet<String>(Arrays.asList(lineFields[VARIANT_SUMMARY_PHENOTYPE_COLUMN]
                    .toLowerCase().split(";")));
            heritableTraitList = phenotypeSet.stream()
                    .map((phenotype) -> new HeritableTrait(phenotype, null)).collect(Collectors.toList());
        }

        List<GenomicFeature> genomicFeatureList = Collections.emptyList();
        if (!EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_GENE_COLUMN])) {
            String[] geneList = lineFields[VARIANT_SUMMARY_GENE_COLUMN].split(",");
            genomicFeatureList = new ArrayList<>(geneList.length);
            for (String geneString : geneList) {
                genomicFeatureList.add(createGeneGenomicFeature(geneString));
            }
        }

        List<Property> additionalProperties = new ArrayList<>(3);
        VariantClassification variantClassification = null;
        if (!EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_CLINSIG_COLUMN])) {
            variantClassification = getVariantClassification(Arrays.asList(lineFields[VARIANT_SUMMARY_CLINSIG_COLUMN].split("[,/;]")));
            additionalProperties.add(new Property(null, CLINICAL_SIGNIFICANCE_IN_SOURCE_FILE,
                    lineFields[VARIANT_SUMMARY_CLINSIG_COLUMN]));
        }

        ConsistencyStatus consistencyStatus = null;
        if (!EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_REVIEW_COLUMN])) {
            consistencyStatus = getConsistencyStatus(lineFields[VARIANT_SUMMARY_REVIEW_COLUMN]);
            additionalProperties.add(new Property(null, REVIEW_STATUS_IN_SOURCE_FILE,
                    lineFields[VARIANT_SUMMARY_REVIEW_COLUMN]));
        }

        // Multiple vars within the same RCV. Maximum of two vars permitted so far
        if (mateVariantString != null) {
            additionalProperties.add(new Property(null, GENOTYPESET, mateVariantString));
        }

        EvidenceEntry evidenceEntry = new EvidenceEntry(evidenceSource, Collections.emptyList(), null,
                "https://www.ncbi.nlm.nih.gov/clinvar/variation/" + variationId, variationId, null,
                !(alleleOrigin == null || alleleOrigin.isEmpty()) ? alleleOrigin : null, heritableTraitList, genomicFeatureList,
                variantClassification, null,
                null, consistencyStatus, EthnicCategory.Z, null, null,
                null, additionalProperties, Collections.emptyList());

        variantAnnotation.getTraitAssociation().add(evidenceEntry);
    }

    private ConsistencyStatus getConsistencyStatus(String lineField) {
        for (String value : lineField.split("[,/;]")) {
            value = value.toLowerCase().trim();
            if (VariantAnnotationUtils.CLINVAR_REVIEW_TO_CONSISTENCY_STATUS.containsKey(value)) {
                return VariantAnnotationUtils.CLINVAR_REVIEW_TO_CONSISTENCY_STATUS.get(value);
            }
        }
        return null;
    }

    private VariantClassification getVariantClassification(String lineField) {
        VariantClassification variantClassification = new VariantClassification();
        for (String value : lineField.split("[,/;]")) {
            value = value.toLowerCase().trim();
            if (VariantAnnotationUtils.CLINVAR_CLINSIG_TO_ACMG.containsKey(value)) {
                // No value set
                if (variantClassification.getClinicalSignificance() == null) {
                    variantClassification.setClinicalSignificance(VariantAnnotationUtils.CLINVAR_CLINSIG_TO_ACMG.get(value));
                // Seen cases like Benign;Pathogenic;association;not provided;risk factor for the same record
                } else if (isBenign(VariantAnnotationUtils.CLINVAR_CLINSIG_TO_ACMG.get(value))
                        && isPathogenic(variantClassification.getClinicalSignificance())) {
                    logger.warn("Benign and Pathogenic clinical significances found for the same record");
                    logger.warn("Will set uncertain_significance instead");
                    variantClassification.setClinicalSignificance(ClinicalSignificance.uncertain_significance);
                }
            } else if (VariantAnnotationUtils.CLINVAR_CLINSIG_TO_TRAIT_ASSOCIATION.containsKey(value)) {
                variantClassification.setTraitAssociation(VariantAnnotationUtils.CLINVAR_CLINSIG_TO_TRAIT_ASSOCIATION.get(value));
            } else if (VariantAnnotationUtils.CLINVAR_CLINSIG_TO_DRUG_RESPONSE.containsKey(value)) {
                variantClassification.setDrugResponseClassification(VariantAnnotationUtils.CLINVAR_CLINSIG_TO_DRUG_RESPONSE.get(value));
            } else {
                logger.debug("No mapping found for referenceClinVarAssertion.clinicalSignificance {}", value);
                logger.debug("No value will be set at EvidenceEntry.variantClassification for this term");
            }
        }
        return variantClassification;
    }

    private boolean isPathogenic(ClinicalSignificance clinicalSignificance) {
        return ClinicalSignificance.pathogenic.equals(clinicalSignificance)
                || ClinicalSignificance.likely_pathogenic.equals(clinicalSignificance);
    }

    private boolean isBenign(ClinicalSignificance clinicalSignificance) {
        return ClinicalSignificance.benign.equals(clinicalSignificance)
                || ClinicalSignificance.likely_benign.equals(clinicalSignificance);
    }

    private void addNewEntries(VariantAnnotation variantAnnotation, PublicSetType publicSet, String alleleId,
                               String mateVariantString, Map<String, EFO> traitsToEfoTermsMap) throws JsonProcessingException {

        List<Property> additionalProperties = new ArrayList<>(3);
        EvidenceSource evidenceSource = new EvidenceSource(EtlCommons.CLINVAR_DATA, null, null);
        String accession = publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc();

        VariantClassification variantClassification = getVariantClassification(
                Arrays.asList(publicSet.getReferenceClinVarAssertion().getClinicalSignificance().getDescription().split("[,/;]")));
        additionalProperties.add(new Property(null, CLINICAL_SIGNIFICANCE_IN_SOURCE_FILE, publicSet.getReferenceClinVarAssertion()
                .getClinicalSignificance().getDescription()));

        ConsistencyStatus consistencyStatus = getConsistencyStatus(publicSet.getReferenceClinVarAssertion()
                .getClinicalSignificance().getReviewStatus().name());
        additionalProperties.add(new Property(null, REVIEW_STATUS_IN_SOURCE_FILE, publicSet.getReferenceClinVarAssertion()
                .getClinicalSignificance().getReviewStatus().name()));

        // Multiple vars within the same RCV. Maximum of two vars permitted so far
        if (mateVariantString != null) {
            additionalProperties.add(new Property(null, GENOTYPESET, mateVariantString));
        }

        // Compose heterozygous, for example, may provide more than one allele for a single RCV
        List<GenomicFeature> genomicFeatureList = getGenomicFeature(publicSet, alleleId);
        List<EvidenceSubmission> submissions = getSubmissionList(publicSet);

        List<String> bibliography = new ArrayList<>();
        Set<String> originSet = new HashSet<>(publicSet.getReferenceClinVarAssertion().getObservedIn().size());
        for (ObservationSet observationSet : publicSet.getReferenceClinVarAssertion().getObservedIn()) {
            // Origin for a number of the variants may not be clear, values found in the database for this field are:
            // {"germline",  "unknown",  "inherited",  "maternal",  "de novo",  "paternal",  "not provided",  "somatic",
            // "uniparental",  "biparental",  "tested-inconclusive",  "not applicable"}
            // For the sake of simplicity and since it's not clear what to do with the rest of tags, we'll classify
            // as somatic only those with the "somatic" tag and the rest will be stored as germline
            originSet.add(observationSet.getSample().getOrigin());
            bibliography = addBibliographyFromObservationSet(bibliography, observationSet);
        }
        List<AlleleOrigin> alleleOrigin = getAlleleOriginList(new ArrayList<>(originSet));

        List<HeritableTrait> heritableTraitList = getHeritableTrait(publicSet, traitsToEfoTermsMap, additionalProperties);

        EvidenceEntry evidenceEntry = new EvidenceEntry(evidenceSource, submissions, null,
                "https://www.ncbi.nlm.nih.gov/clinvar/" + accession, accession, null,
                !(alleleOrigin == null || alleleOrigin.isEmpty()) ? alleleOrigin : null, heritableTraitList, genomicFeatureList,
                variantClassification, null,
                null, consistencyStatus, EthnicCategory.Z, null, null,
                null, additionalProperties, bibliography);

        variantAnnotation.getTraitAssociation().add(evidenceEntry);

    }

    private List<EvidenceSubmission> getSubmissionList(PublicSetType publicSet) {
        List<EvidenceSubmission> submissionList = new ArrayList<>(publicSet.getClinVarAssertion().size());
        for (MeasureTraitType measureTraitType : publicSet.getClinVarAssertion()) {
            String date;
            // Try to provide the clinVarAssertion.clinicalSignificance.dateLastUpdated date. If does not exist, provide
            // the clinVarAccession.dateUpdated one. We are assuming thate clinVarAccession and
            // clinVarAccession.dateUpdated fields do always exist
            if (measureTraitType.getClinicalSignificance() != null
                    && measureTraitType.getClinicalSignificance().getDateLastEvaluated() != null) {
                date = String.format("%04d", measureTraitType.getClinicalSignificance().getDateLastEvaluated().getYear())
                        + String.format("%02d", measureTraitType.getClinicalSignificance().getDateLastEvaluated().getMonth())
                        + String.format("%02d", measureTraitType.getClinicalSignificance().getDateLastEvaluated().getDay());
            } else {
                date = String.format("%04d", measureTraitType.getClinVarAccession().getDateUpdated().getYear())
                        + String.format("%02d", measureTraitType.getClinVarAccession().getDateUpdated().getMonth())
                        + String.format("%02d", measureTraitType.getClinVarAccession().getDateUpdated().getDay());
            }
            submissionList.add(new EvidenceSubmission(measureTraitType.getClinVarSubmissionID().getSubmitter(), date,
                    null));
        }
        return submissionList;
    }

    private String getPreferredTraitName(PublicSetType publicSet, Map<String, EFO> traitsToEfoTermsMap) {
        for (TraitType trait : publicSet.getReferenceClinVarAssertion().getTraitSet().getTrait()) {
//            if (!trait.getType().equalsIgnoreCase("disease")) {
//                logger.warn("Entry {}. trait type = {}. No action defined for these traits. Skipping entry",
//                        publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc(), trait.getType());
////                System.exit(1);
//            } else {
            for (SetElementSetType setElementSet : trait.getName()) {
                if (setElementSet.getElementValue().getType().equalsIgnoreCase("preferred")) {
                    if (traitsToEfoTermsMap.get(setElementSet.getElementValue().getValue()) != null) {
                        return traitsToEfoTermsMap.get(setElementSet.getElementValue().getValue()).name;
                    } else {
                        return setElementSet.getElementValue().getValue();
                    }
                }
            }
//            }
        }
        logger.warn("Entry {}. No \"disease\" entry found among the traits",
                        publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc());
        numberNoDiseaseTrait++;

        return null;
    }

//    private List<String> getInheritanceModel(PublicSetType publicSet) {
//    private ModeOfInheritance getInheritanceModel(TraitType trait, Map<String, String> sourceInheritableTrait)
    private ModeOfInheritance getInheritanceModel(List<ReferenceAssertionType.AttributeSet> attributeSetList,
                                                  Map<String, String> sourceInheritableTrait)
            throws JsonProcessingException {
        Set<String> inheritanceModelSet = new HashSet<>();
//        for (TraitType trait : publicSet.getReferenceClinVarAssertion().getTraitSet().getTrait()) {
//        if (trait.getAttributeSet() != null) {
        if (attributeSetList != null) {
            for (ReferenceAssertionType.AttributeSet attributeSet : attributeSetList) {
                if (attributeSet.getAttribute().getType() != null
                        && attributeSet.getAttribute().getType().equalsIgnoreCase("modeofinheritance")) {
                    inheritanceModelSet.add(attributeSet.getAttribute().getValue().toLowerCase());
                }
            }
        }
//        }

        if (inheritanceModelSet.size() == 0) {
            return null;
        } else if (inheritanceModelSet.size() > 1) {
            sourceInheritableTrait.put(MODE_OF_INHERITANCE,
                    jsonObjectWriter.writeValueAsString(new ArrayList<>(inheritanceModelSet)));
            numberMultipleInheritanceModels++;
            return solveModeOfInheritanceConflict(inheritanceModelSet);
        } else {
            sourceInheritableTrait.put(MODE_OF_INHERITANCE, inheritanceModelSet.iterator().next());
            return getModeOfInheritance(inheritanceModelSet.iterator().next());
        }
    }

    private ModeOfInheritance solveModeOfInheritanceConflict(Set<String> inheritanceModelSet) {
        logger.warn("Multiple inheritance models found for a variant: {}",
                String.join(",", new ArrayList<>(inheritanceModelSet)));
        Set<ModeOfInheritance> modeOfInheritanceSet = inheritanceModelSet.stream()
                .map((modeofInheritanceString) -> getModeOfInheritance(modeofInheritanceString))
                .collect(Collectors.toSet());

        if (modeOfInheritanceSet.size() == 1
                || (modeOfInheritanceSet.size() == 2 && modeOfInheritanceSet.contains(null))) {
            modeOfInheritanceSet.remove(null);
            logger.warn("Selected inheritance model: {}", modeOfInheritanceSet.iterator().next());
            return modeOfInheritanceSet.iterator().next();
        } else {
            modeOfInheritanceSet.remove(null);
            modeOfInheritanceSet.removeAll(DOMINANT_TERM_SET);
            if (modeOfInheritanceSet.size() > 0) {
                modeOfInheritanceSet.removeAll(RECESSIVE_TERM_SET);
                if (modeOfInheritanceSet.size() > 0) {
                    logger.warn("No inheritance model selected, conflicting inheritance models found");
                    return null;
                } else {
                    logger.warn("Dominant and recessive models found, {} selected",
                            ModeOfInheritance.monoallelic_and_biallelic.name());
                    return ModeOfInheritance.monoallelic_and_biallelic;
                }
            }
        }

        return null;
    }

    private ModeOfInheritance getModeOfInheritance(String modeOfInheritance) {
        if (VariantAnnotationUtils.MODEOFINHERITANCE_MAP.containsKey(modeOfInheritance)) {
            return VariantAnnotationUtils.MODEOFINHERITANCE_MAP.get(modeOfInheritance);
        }
        return null;
    }

    private List<GenomicFeature> getGenomicFeature(PublicSetType publicSet, String alleleId) {
        if (publicSet.getReferenceClinVarAssertion().getMeasureSet() != null) {
            return getGenomicFeature(publicSet.getReferenceClinVarAssertion().getMeasureSet());
        // No measureSet means there must be genotypeSet
        } else if (publicSet.getReferenceClinVarAssertion().getGenotypeSet() != null) {
            for (MeasureSetType measureSet : publicSet.getReferenceClinVarAssertion().getGenotypeSet().getMeasureSet()) {
                if (measureSet.getMeasure() != null) {
                    for (MeasureType measure : measureSet.getMeasure()) {
                        if (measure.getID() != null && (new BigInteger(alleleId)).equals(measure.getID())) {
                            return getGenomicFeature(measureSet);
                        }
                    }
                }
            }
        }
        throw new RuntimeException("One of either MeasureSet or GenotypeSet attributes are required within "
                + "publicSet.getReferenceClinVarAssertion(). Also, if GenotypeSet is present, at least one MeasureSet"
                + " corresponding to each alleleId is required.Please check "
                + publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc());
    }

    private List<GenomicFeature> getGenomicFeature(MeasureSetType measureSet) {
        Set<GenomicFeature> genomicFeatureSet = new HashSet<>();
        for (MeasureType measure : measureSet.getMeasure()) {
            if (measure.getMeasureRelationship() != null) {
                for (MeasureType.MeasureRelationship measureRelationship : measure.getMeasureRelationship()) {
                    if (measureRelationship.getSymbol() != null) {
                        for (SetElementSetType setElementSet : measureRelationship.getSymbol()) {
                            if (setElementSet.getElementValue() != null) {
                                if (setElementSet.getElementValue().getValue() != null) {
                                    genomicFeatureSet.add(createGeneGenomicFeature(setElementSet.getElementValue().getValue()));
                                }
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(genomicFeatureSet);
    }

    private List<HeritableTrait> getHeritableTrait(PublicSetType publicSet, Map<String, EFO> traitsToEfoTermsMap,
                                                   List<Property> propertyList) throws JsonProcessingException {

        List<HeritableTrait> heritableTraitList
                = new ArrayList<>(publicSet.getReferenceClinVarAssertion().getTraitSet().getTrait().size());
        // To keep trait and inheritance modes as they appear in the source file
        List<Map<String, String>> sourceInheritableTraitList
                = new ArrayList<>(publicSet.getReferenceClinVarAssertion().getTraitSet().getTrait().size());

        Map<String, String> sourceInheritableTraitMap = new HashMap<>();
        // WARNING: in version 53 onwards of ClinVar schema the mode of inheritance is provided at the
        // root of the ReferenceClinvarAssertion rather than for each trait
        ModeOfInheritance modeOfInheritance
                = getInheritanceModel(publicSet.getReferenceClinVarAssertion().getAttributeSet(),
                        sourceInheritableTraitMap);

        for (TraitType trait : publicSet.getReferenceClinVarAssertion().getTraitSet().getTrait()) {
            // Look for the preferred trait name
            int i = 0;
            while (i < trait.getName().size()
                    && !trait.getName().get(i).getElementValue().getType().equalsIgnoreCase("preferred")) {
                i++;
            }
            // WARN: assuming there will always be a preferred trait name
            // Found preferred trait name
            if (i < trait.getName().size()) {
                Map<String, String> currentSourceInheritableTraitMap = new HashMap<>(sourceInheritableTraitMap);
                // WARNING: overwrites previous TRAIT entry if present in order to reuse the same object in each
                // iteration - in version 53 onwards of ClinVar schema the mode of inheritance is provided at the
                // root of the ReferenceClinvarAssertion rather than for each trait
                currentSourceInheritableTraitMap.put(TRAIT, trait.getName().get(i).getElementValue().getValue());

//                heritableTraitList.add(new HeritableTrait(trait.getName().get(i).getElementValue().getValue(),
//                        getInheritanceModel(trait, sourceInheritableTraitMap)));
                heritableTraitList.add(new HeritableTrait(trait.getName().get(i).getElementValue().getValue(),
                        modeOfInheritance));

                // This is to double-confirm that the new ClinVar (v53) schema is properly read. It will just check
                // that there are no inheritance modes provided within the trait
                if (traitInheritanceModesPresent(trait.getAttributeSet())) {
                    throw new RuntimeException("ClinVar record found providing inheritance mode withint the trait."
                            + " After ClinVar schema v53 inheritance mode is expected to be provided at the root"
                            + " of the ReferenceClinvarAssertion field.");
                }

                sourceInheritableTraitList.add(currentSourceInheritableTraitMap);
            } else {
                throw new IllegalArgumentException("ClinVar record found "
                        + publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc()
                        + " with no preferred trait provided");
            }
        }

        if (heritableTraitList.size() == 0) {
            logger.warn("Entry {}. No \"disease\" entry found among the traits",
                    publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc());
            numberNoDiseaseTrait++;
        } else {
            propertyList.add(new Property(null, MODE_OF_INHERITANCE,
                    jsonObjectWriter.writeValueAsString(sourceInheritableTraitList)));
        }
        return heritableTraitList;
    }

    private boolean traitInheritanceModesPresent(List<TraitType.AttributeSet> attributeSetList) {
        if (attributeSetList != null) {
            for (TraitType.AttributeSet attributeSet : attributeSetList) {
                if (attributeSet.getAttribute().getType() != null
                        && attributeSet.getAttribute().getType().equalsIgnoreCase("modeofinheritance")) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<String> addBibliographyFromObservationSet(List<String> germlineBibliography, ObservationSet observationSet) {
        for (ObservationSet.ObservedData observedData : observationSet.getObservedData()) {
            for (CitationType citation : observedData.getCitation()) {
                for (CitationType.ID id : citation.getID()) {
                    if (id.getSource().equalsIgnoreCase("pubmed")) {
                        if (germlineBibliography == null) {
                            germlineBibliography = new ArrayList<>();
                        }
                        germlineBibliography.add("PMID:" + id.getValue());
                    }
                }
            }
        }
        return germlineBibliography;
    }

    private Map<String, List<AlleleLocationData>> parseVariantSummary(Map<String, EFO> traitsToEfoTermsMap)
            throws IOException, RocksDBException {

        logger.info("Loading AlleleID -> variation ID map...");
        Map<String, List<VariationData>> alleleIdToVariationData = loadAlleleIdToVariationData();

        logger.info("Parsing {}...", clinvarSummaryFile);
        BufferedReader bufferedReader;
        bufferedReader = FileUtils.newBufferedReader(clinvarSummaryFile);

        ProgressLogger progressLogger = new ProgressLogger("Parsed variant summary lines:",
                () -> EtlCommons.countFileLines(clinvarSummaryFile), 200).setBatchSize(10000);

        Map<String, List<AlleleLocationData>> rcvToAlelleLocationData = new HashMap<>();
        // variationID -> [lineFields1, lineFields2] where lineFields* correspond to the two associated lines in
        // variant_summary.txt splitted by \t
        Map<String, List<String[]>> compoundVariationRecords = new HashMap<>();

        // Parse line by line variant_summary.txt. "Simple" lines corresponding to records involving just one variant
        // will be directly indexed in RocksDB. Lines corresponding to compound records will be saved in memory for
        // posterior parsing and indexing. This was decided to be done in this way in order to avoid saving all lines
        // in memory since may become too much in a near future.
        // Skip header, read first data line
        bufferedReader.readLine();
        String line = bufferedReader.readLine();
        while (line != null) {
            String[] parts = line.split("\t");
            // Check assembly
            if (parts[16].equals(assembly)) {
                SequenceLocation sequenceLocation = new SequenceLocation(parts[VARIANT_SUMMARY_CHR_COLUMN],
                        Integer.valueOf(parts[VARIANT_SUMMARY_START_COLUMN]),
                        Integer.valueOf(parts[VARIANT_SUMMARY_END_COLUMN]),
                        parts[VARIANT_SUMMARY_REFERENCE_COLUMN],
                        parts[VARIANT_SUMMARY_ALTERNATE_COLUMN]);
                // Each line may contain more than one RCV; e.g.: RCV000000019;RCV000000020;RCV000000021;RCV000000022;...
                // Also, RCV ids may be repeated in the same line!!! e.g RCV000540418;RCV000540418;RCV000540418;RCV000000066
                Set<String> rcvSet = new HashSet<>(Arrays.asList(parts[11].split(";")));
                // Fill in rcvToAlleleLocationData map
                for (String rcv : rcvSet) {
                    List<AlleleLocationData> alleleLocationDataList;
                    // One RCV may appear in multiple lines e.g. compound heterozygote
                    if (rcvToAlelleLocationData.get(rcv) == null) {
                        alleleLocationDataList = new ArrayList<>();
                        rcvToAlelleLocationData.put(rcv, alleleLocationDataList);
                    } else {
                        alleleLocationDataList = rcvToAlelleLocationData.get(rcv);
                    }
                    // Allele ID assumed to always be present
                    if (EtlCommons.isMissing(parts[0])) {
                        throw new RuntimeException("Allele id missing from variant_summary.txt. Aborting parsing. Line: "
                                + line);
                    } else {
                        alleleLocationDataList.add(new AlleleLocationData(parts[0], sequenceLocation));
                    }
                }
                // Index the Germline/Somatic documents corresponding to the aggregated variation object
                // !EtlCommons.isMissing(parts[0]) is also checked above and therefore redundant but kept it here
                // just in case
                if (!EtlCommons.isMissing(parts[0])) {
                    // One allele ID may be associated with multiple variation records e.g. 187140 -> [242617, 424712]
                    for (VariationData variationData : alleleIdToVariationData.get(parts[0])) {
                        // This is a "normal" line with just one variant being involved in this/these RCV records
                        if (VARIANT.equals(variationData.getType())) {
                            boolean success = updateRocksDB(sequenceLocation, variationData.getId(), parts, null,
                                    traitsToEfoTermsMap);
                            // updateRocksDB may fail (false) if normalisation process fails
                            if (success) {
                                numberIndexedRecords++;
                            }
                        // Save lines forming a compound variation/RCV record in memory, within a HashMap for
                        // posterior processing.
                        // In order to generate the EvidenceEntry object of compound variation records we need first to
                        // collect all the lines associated with it, so that we are able to generate mate variant
                        // strings for each of the forming variants
                        } else {
                            List<String[]> lineList;
                            // Check if there was a list of lines already initialised for this variation id
                            if (compoundVariationRecords.containsKey(variationData.getId())) {
                                lineList = compoundVariationRecords.get(variationData.getId());
                            } else {
                                 lineList = new ArrayList<>(2);
                                 compoundVariationRecords.put(variationData.getId(), lineList);
                            }
                            // Add current - splitted - line to the list for this variation id
                            lineList.add(parts);

                        }
                    }
                }
                totalNumberRecords++;
            }
            progressLogger.increment(1);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();

        // Drain compoundVariationRecords map by parsing the lines in it and creating corresponding EvidenceEntry objects
        logger.info("{} compound variation records found.", compoundVariationRecords.size());
        logger.info("Indexing compound variation records");
        for (String variationId : compoundVariationRecords.keySet()) {
            for (int i = 0; i < compoundVariationRecords.get(variationId).size(); i++) {
                String[] currentVarFields = compoundVariationRecords.get(variationId).get(i);
                boolean success = updateRocksDB(new SequenceLocation(currentVarFields[VARIANT_SUMMARY_CHR_COLUMN],
                                Integer.valueOf(currentVarFields[VARIANT_SUMMARY_START_COLUMN]),
                                Integer.valueOf(currentVarFields[VARIANT_SUMMARY_END_COLUMN]),
                                currentVarFields[VARIANT_SUMMARY_REFERENCE_COLUMN],
                                currentVarFields[VARIANT_SUMMARY_ALTERNATE_COLUMN]), variationId, currentVarFields,
                        getMateVariantStringByVariantSummaryRecord(i, compoundVariationRecords.get(variationId)),
                        traitsToEfoTermsMap);
                // updateRocksDB may fail (false) if normalisation process fails
                if (success) {
                    numberIndexedRecords++;
                }
            }
        }

        return rcvToAlelleLocationData;
    }

    private String getMateVariantStringByVariantSummaryRecord(int i, List<String[]> splitLineList) {
        StringBuilder mateVariantString = new StringBuilder();
        // Generate a string with comma separated list of variant strings including all other variants but the one
        // in position i
        for (int j = 0; j < splitLineList.size(); j++) {
            if (j != i) {
                // First variant string must avoid including the separator
                if (mateVariantString.length() > 0) {
                    mateVariantString.append(",");
                }
                String[] mateFields = splitLineList.get(j);
                mateVariantString.append(VariantAnnotationUtils.buildVariantId(mateFields[VARIANT_SUMMARY_CHR_COLUMN],
                        Integer.valueOf(mateFields[VARIANT_SUMMARY_START_COLUMN]),
                        mateFields[VARIANT_SUMMARY_REFERENCE_COLUMN],
                        mateFields[VARIANT_SUMMARY_ALTERNATE_COLUMN]).replace(" ", ""));
            }
        }
        return mateVariantString.length() == 0 ? null : mateVariantString.toString();
    }

    private Map<String, List<VariationData>> loadAlleleIdToVariationData() throws IOException {
        Map<String, List<VariationData>> alleleIdToVariationId = new HashMap<>();
        BufferedReader bufferedReader = FileUtils.newBufferedReader(clinvarVariationAlleleFile);

        String line = bufferedReader.readLine();
        while (line != null && line.startsWith("#")) {
            line = bufferedReader.readLine();
        }

        while (line != null) {
            String[] parts = line.split("\t");
            List<VariationData> variationDataList;
            variationDataList = alleleIdToVariationId.get(parts[VARIATION_ALLELE_ALLELE_COLUMN]);
            // One allele ID may be associated with multiple variation records e.g. 187140 -> [242617, 424712]
            if (variationDataList == null) {
                variationDataList = new ArrayList<>();
                alleleIdToVariationId.put(parts[VARIATION_ALLELE_ALLELE_COLUMN], variationDataList);
            }
//            else if (variationDataList.size() == 2) {
//                throw new RuntimeException("No more than two variation records per allele ID are currently modelled."
//                        + " Pleasee check line \n" + line + "\n of file " + clinvarVariationAlleleFile.toString());
//
//            }
            variationDataList.add(new VariationData(parts[VARIATION_ALLELE_VARIATION_COLUMN],
                    parts[VARIATION_ALLELE_TYPE_COLUMN]));
            line = bufferedReader.readLine();
        }
        bufferedReader.close();

        return alleleIdToVariationId;
    }

    private Map<String, EFO> loadEFOTerms() {
        if (clinvarEFOFile != null) {
            logger.info("Loading EFO terms ...");
            Map<String, EFO> efoTerms = new HashMap<>();
            try (Stream<String> linesStream = Files.lines(clinvarEFOFile)) {
                linesStream.forEach(line -> addEfoTermToMap(line, efoTerms));
                logger.info("Done");
                return efoTerms;
            } catch (IOException e) {
                logger.error("Error loading EFO file: " + e.getMessage());
                logger.error("EFO terms won't be added");
            }
        } else {
            logger.warn("No EFO terms file present: EFO terms won't be added");
        }
        return null;
    }

    private void addEfoTermToMap(String line, Map<String, EFO> efoTerms) {
        String[] columns = line.split("\t");
        efoTerms.put(columns[0], new ClinVarIndexer.EFO(columns[2], columns[3], columns[1]));
    }

    private JAXBElement<ReleaseType> unmarshalXML(Path clinvarXmlFile) throws JAXBException, IOException {
        return (JAXBElement<ReleaseType>) ClinvarParser.loadXMLInfo(clinvarXmlFile.toString(), CLINVAR_CONTEXT_V53);
    }

    class EFO {
        private final String id;
        private final String name;
        private final String url;

        EFO(String id, String name, String url) {
            this.id = id;
            this.name = name;
            this.url = url;
        }
    }

    private class AlleleLocationData {
        private String alleleId;
        private SequenceLocation sequenceLocation;

        AlleleLocationData(String alleleId, SequenceLocation sequenceLocation) {
            this.alleleId = alleleId;
            this.sequenceLocation = sequenceLocation;
        }

        String getAlleleId() {
            return alleleId;
        }

        void setAlleleId(String alleleId) {
            this.alleleId = alleleId;
        }

        SequenceLocation getSequenceLocation() {
            return sequenceLocation;
        }

        void setSequenceLocation(SequenceLocation sequenceLocation) {
            this.sequenceLocation = sequenceLocation;
        }
    }

    private class VariationData {
        private String id;
        private String type;

        VariationData(String id, String type) {
            this.id = id;
            this.type = type;
        }

        String getId() {
            return id;
        }

        void setId(String id) {
            this.id = id;
        }

        String getType() {
            return type;
        }

        void setType(String type) {
            this.type = type;
        }
    }
}
