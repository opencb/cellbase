package org.opencb.cellbase.app.transform.clinical.variant;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.opencb.biodata.formats.variant.clinvar.ClinvarParser;
import org.opencb.biodata.formats.variant.clinvar.v24jaxb.*;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by fjlopez on 28/09/16.
 */
public class ClinVarIndexer extends ClinicalIndexer {

    private static final String CLINVAR_NAME = "clinvar";
    private static final int VARIANT_SUMMARY_CLINSIG_COLUMN = 6;
    private static final int VARIANT_SUMMARY_GENE_COLUMN = 4;
    private static final int VARIANT_SUMMARY_REVIEW_COLUMN = 24;
    private static final int VARIANT_SUMMARY_ORIGIN_COLUMN = 14;
    private static final int VARIANT_SUMMARY_PHENOTYPE_COLUMN = 13;
    private static final int VARIATION_ALLELE_ALLELE_COLUMN = 2;
    private static final int VARIATION_ALLELE_VARIATION_COLUMN = 0;
    private static final String SOMATIC = "somatic";
    private static final String CLINSIG_FIELD_NAME = "ClinicalSignificance";
    private static final String REVIEW_FIELD_NAME = "ReviewStatus";
    private static final String TRAIT = "trait";
    private static final String MODE_OF_INHERITANCE = "modeOfInheritance";
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
                          Path clinvarEFOFile, String assembly, RocksDB rdb) {
        super();
        this.rdb = rdb;
        this.clinvarXMLFile = clinvarXMLFile;
        this.clinvarSummaryFile = clinvarSummaryFile;
        this.clinvarVariationAlleleFile = clinvarVariationAlleleFile;
        this.clinvarEFOFile = clinvarEFOFile;
        this.assembly = assembly;
    }

    public void index() throws RocksDBException {
        try {
            Map<String, EFO> traitsToEfoTermsMap = loadEFOTerms();
            Map<String, SequenceLocation> rcvTo37SequenceLocation = parseVariantSummary(traitsToEfoTermsMap);

            logger.info("Unmarshalling clinvar file " + clinvarXMLFile + " ...");
            JAXBElement<ReleaseType> clinvarRelease = unmarshalXML(clinvarXMLFile);
            logger.info("Done");

            logger.info("Serializing clinvar records that have Sequence Location for Assembly " + assembly + " ...");
            ProgressLogger progressLogger = new ProgressLogger("Parsed XML records:", clinvarRelease.getValue().getClinVarSet().size(),
                    200).setBatchSize(10000);
            for (PublicSetType publicSet : clinvarRelease.getValue().getClinVarSet()) {
                SequenceLocation sequenceLocation =
                        rcvTo37SequenceLocation.get(publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc());
                if (sequenceLocation != null) {
                    updateRocksDB(sequenceLocation, publicSet, traitsToEfoTermsMap);
                    numberIndexedRecords++;
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

    private void updateRocksDB(SequenceLocation sequenceLocation, String variationId, String[] lineFields,
                               Map<String, EFO> traitsToEfoTermsMap) throws RocksDBException, IOException {
        byte[] key = VariantAnnotationUtils.buildVariantId(sequenceLocation.getChromosome(),
                sequenceLocation.getStart(), sequenceLocation.getReference(),
                sequenceLocation.getAlternate()).getBytes();
        List<EvidenceEntry> evidenceEntryList = getEvidenceEntryList(key);
        addNewEntries(evidenceEntryList, variationId, lineFields, traitsToEfoTermsMap);
        rdb.put(key, jsonObjectWriter.writeValueAsBytes(evidenceEntryList));
    }

    private void updateRocksDB(SequenceLocation sequenceLocation, PublicSetType publicSet,
                               Map<String, EFO> traitsToEfoTermsMap) throws RocksDBException, IOException {

        byte[] key = VariantAnnotationUtils.buildVariantId(sequenceLocation.getChromosome(),
                sequenceLocation.getStart(), sequenceLocation.getReference(),
                sequenceLocation.getAlternate()).getBytes();
        List<EvidenceEntry> evidenceEntryList = getEvidenceEntryList(key);
        addNewEntries(evidenceEntryList, publicSet, traitsToEfoTermsMap);
        rdb.put(key, jsonObjectWriter.writeValueAsBytes(evidenceEntryList));
    }

    private void addNewEntries(List<EvidenceEntry> evidenceEntryList, String variationId, String[] lineFields,
                               Map<String, EFO> traitsToEfoTermsMap) {

        EvidenceSource evidenceSource = new EvidenceSource(EtlCommons.CLINVAR_DATA, null, null);
        // Create a set to avoid situations like germline;germline;germline
        List<AlleleOrigin> alleleOrigin = null;
        if (!EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_ORIGIN_COLUMN])) {
            Set<String> originSet = new HashSet<String>(Arrays.asList(lineFields[VARIANT_SUMMARY_ORIGIN_COLUMN]
                    .toLowerCase().split(";")));
            alleleOrigin = getAlleleOriginList(new ArrayList<>(originSet));
        }

        List<HeritableTrait> heritableTraitList = null;
        if (!EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_PHENOTYPE_COLUMN])) {
            Set<String> phenotypeSet = new HashSet<String>(Arrays.asList(lineFields[VARIANT_SUMMARY_PHENOTYPE_COLUMN]
                    .toLowerCase().split(";")));
            heritableTraitList = phenotypeSet.stream()
                    .map((phenotype) -> new HeritableTrait(phenotype, null)).collect(Collectors.toList());
        }

        List<GenomicFeature> genomicFeatureList = null;
        if (!EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_GENE_COLUMN])) {
            String[] geneList = lineFields[VARIANT_SUMMARY_GENE_COLUMN].split(",");
            genomicFeatureList = new ArrayList<>(geneList.length);
            for (String geneString : geneList) {
                genomicFeatureList.add(createGeneGenomicFeature(geneString));
            }
        }

        List<Property> additionalProperties = new ArrayList<>(2);
        VariantClassification variantClassification = null;
        if (!EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_CLINSIG_COLUMN])) {
            variantClassification = getVariantClassification(lineFields[VARIANT_SUMMARY_CLINSIG_COLUMN]);
            additionalProperties.add(new Property(null, CLINSIG_FIELD_NAME, lineFields[VARIANT_SUMMARY_CLINSIG_COLUMN]));
        }

        ConsistencyStatus consistencyStatus = null;
        if (!EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_REVIEW_COLUMN])) {
            consistencyStatus = getConsistencyStatus(lineFields[VARIANT_SUMMARY_REVIEW_COLUMN]);
            additionalProperties.add(new Property(null, REVIEW_FIELD_NAME, lineFields[VARIANT_SUMMARY_REVIEW_COLUMN]));
        }

        EvidenceEntry evidenceEntry = new EvidenceEntry(evidenceSource, null, null,
                "https://www.ncbi.nlm.nih.gov/clinvar/variation/" + variationId, variationId, null,
                !alleleOrigin.isEmpty() ? alleleOrigin : null, heritableTraitList, genomicFeatureList,
                variantClassification, null,
                null, consistencyStatus, null, null, null,
                null, additionalProperties, null);

        evidenceEntryList.add(evidenceEntry);
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

    private void addNewEntries(List<EvidenceEntry> evidenceEntryList, PublicSetType publicSet,
                               Map<String, EFO> traitsToEfoTermsMap) throws JsonProcessingException {

        List<Property> additionalProperties = new ArrayList<>(3);
        EvidenceSource evidenceSource = new EvidenceSource(EtlCommons.CLINVAR_DATA, null, null);
        String accession = publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc();

        VariantClassification variantClassification = getVariantClassification(publicSet.getReferenceClinVarAssertion()
                .getClinicalSignificance().getDescription());
        additionalProperties.add(new Property(null, CLINSIG_FIELD_NAME, publicSet.getReferenceClinVarAssertion()
                .getClinicalSignificance().getDescription()));

        ConsistencyStatus consistencyStatus = getConsistencyStatus(publicSet.getReferenceClinVarAssertion()
                .getClinicalSignificance().getReviewStatus().name());
        additionalProperties.add(new Property(null, REVIEW_FIELD_NAME, publicSet.getReferenceClinVarAssertion()
                .getClinicalSignificance().getReviewStatus().name()));

        List<GenomicFeature> genomicFeatureList = getGenomicFeature(publicSet);
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
                !alleleOrigin.isEmpty() ? alleleOrigin : null, heritableTraitList, genomicFeatureList,
                variantClassification, null,
                null, consistencyStatus, null, null, null,
                null, additionalProperties, bibliography);

        evidenceEntryList.add(evidenceEntry);

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
    private ModeOfInheritance getInheritanceModel(TraitType trait, Map<String, String> sourceInheritableTrait)
            throws JsonProcessingException {
        Set<String> inheritanceModelSet = new HashSet<>();
//        for (TraitType trait : publicSet.getReferenceClinVarAssertion().getTraitSet().getTrait()) {
        if (trait.getAttributeSet() != null) {
            for (TraitType.AttributeSet attributeSet : trait.getAttributeSet()) {
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

    private List<GenomicFeature> getGenomicFeature(PublicSetType publicSet) {
        Set<GenomicFeature> genomicFeatureSet = new HashSet<>();
        for (MeasureSetType.Measure measure : publicSet.getReferenceClinVarAssertion().getMeasureSet().getMeasure()) {
            if (measure.getMeasureRelationship() != null) {
                for (MeasureSetType.Measure.MeasureRelationship measureRelationship : measure.getMeasureRelationship()) {
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
        if (genomicFeatureSet.size() > 0) {
            return new ArrayList<>(genomicFeatureSet);
        } else {
            return null;
        }
    }

    private List<HeritableTrait> getHeritableTrait(PublicSetType publicSet, Map<String, EFO> traitsToEfoTermsMap,
                                                   List<Property> propertyList) throws JsonProcessingException {

        List<HeritableTrait> heritableTraitList
                = new ArrayList<>(publicSet.getReferenceClinVarAssertion().getTraitSet().getTrait().size());
        // To keep trait and inheritance modes as they appear in the source file
        List<Map<String, String>> sourceInheritableTraitList
                = new ArrayList<>(publicSet.getReferenceClinVarAssertion().getTraitSet().getTrait().size());

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
                Map<String, String> sourceInheritableTraitMap = new HashMap<>();
                sourceInheritableTraitMap.put(TRAIT, trait.getName().get(i).getElementValue().getValue());

                heritableTraitList.add(new HeritableTrait(trait.getName().get(i).getElementValue().getValue(),
                        getInheritanceModel(trait, sourceInheritableTraitMap)));

                sourceInheritableTraitList.add(sourceInheritableTraitMap);
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
            return null;
        } else {
            propertyList.add(new Property(null, MODE_OF_INHERITANCE,
                    jsonObjectWriter.writeValueAsString(sourceInheritableTraitList)));
            return heritableTraitList;
        }
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

    private Map<String, SequenceLocation> parseVariantSummary(Map<String, EFO> traitsToEfoTermsMap) throws IOException, RocksDBException {

        logger.info("Loading AlleleID -> variation ID map...");
        Map<String, String> alleleIdToVariationId = loadAlleleIdToVariationId();

        logger.info("Parsing {}...", clinvarSummaryFile);
        BufferedReader bufferedReader;
        bufferedReader = FileUtils.newBufferedReader(clinvarSummaryFile);

        ProgressLogger progressLogger = new ProgressLogger("Parsed variant summary lines:",
                () -> EtlCommons.countFileLines(clinvarSummaryFile), 200).setBatchSize(10000);

        Map<String, SequenceLocation> rcvToSequenceLocation = new HashMap<>();
        // Skip header, read first data line
        bufferedReader.readLine();
        String line = bufferedReader.readLine();
        while (line != null) {
            String[] parts = line.split("\t");
            // Check assembly
            if (parts[16].equals(assembly)) {
                SequenceLocation sequenceLocation = new SequenceLocation(parts[18], Integer.valueOf(parts[19]),
                        Integer.valueOf(parts[20]), parts[21], parts[22]);
                // Each line may contain more than one RCV; e.g.: RCV000000019;RCV000000020;RCV000000021;RCV000000022;...
                String[] rcvArray = parts[11].split(";");
                for (String rcv : rcvArray) {
                    rcvToSequenceLocation.put(rcv, sequenceLocation);
                }

                // Index the Germline/Somatic documents corresponding to the aggregated variation object
                if (!EtlCommons.isMissing(parts[0]) && alleleIdToVariationId.containsKey(parts[0])) {
                    updateRocksDB(sequenceLocation, alleleIdToVariationId.get(parts[0]), parts, traitsToEfoTermsMap);
                    numberIndexedRecords++;
                }
                totalNumberRecords++;
            }
            progressLogger.increment(1);
            line = bufferedReader.readLine();
        }

        bufferedReader.close();

        return rcvToSequenceLocation;
    }

    private Map<String, String> loadAlleleIdToVariationId() throws IOException {
        Map<String, String> alleleIdToVariationId = new HashMap<>();
        BufferedReader bufferedReader = FileUtils.newBufferedReader(clinvarVariationAlleleFile);

        String line = bufferedReader.readLine();
        while (line != null && line.startsWith("#")) {
            line = bufferedReader.readLine();
        }

        while (line != null) {
            String[] parts = line.split("\t");
            alleleIdToVariationId.put(parts[VARIATION_ALLELE_ALLELE_COLUMN], parts[VARIATION_ALLELE_VARIATION_COLUMN]);
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
        return (JAXBElement<ReleaseType>) ClinvarParser.loadXMLInfo(clinvarXmlFile.toString(), ClinvarParser.CLINVAR_CONTEXT_v24);
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

}
