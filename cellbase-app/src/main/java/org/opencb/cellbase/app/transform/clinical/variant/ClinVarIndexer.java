package org.opencb.cellbase.app.transform.clinical.variant;

import org.opencb.biodata.formats.variant.clinvar.ClinvarParser;
import org.opencb.biodata.formats.variant.clinvar.v24jaxb.*;
import org.opencb.biodata.models.variant.avro.Germline;
import org.opencb.biodata.models.variant.avro.Somatic;
import org.opencb.biodata.models.variant.avro.Submission;
import org.opencb.biodata.models.variant.avro.VariantTraitAssociation;
import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
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
    private final Path clinvarXMLFile;
    private final Path clinvarSummaryFile;
    private final Path clinvarVariationAlleleFile;
    private final Path clinvarEFOFile;
    private final String assembly;
    private RocksDB rdb;
    private int numberSomaticRecords = 0;
    private int numberGermlineRecords = 0;
    private int numberNoDiseaseTrait = 0;
    private int numberMultipleInheritanceModels = 0;

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
            for (PublicSetType publicSet : clinvarRelease.getValue().getClinVarSet()) {
                SequenceLocation sequenceLocation =
                        rcvTo37SequenceLocation.get(publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc());
                if (sequenceLocation != null) {
                    updateRocksDB(sequenceLocation, publicSet, traitsToEfoTermsMap);
                    numberIndexedRecords++;
                }
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
        VariantTraitAssociation variantTraitAssociation = getVariantTraitAssociation(key);
        addNewEntries(variantTraitAssociation, variationId, lineFields, traitsToEfoTermsMap);
        rdb.put(key, jsonObjectWriter.writeValueAsBytes(variantTraitAssociation));
    }

    private void updateRocksDB(SequenceLocation sequenceLocation, PublicSetType publicSet,
                               Map<String, EFO> traitsToEfoTermsMap) throws RocksDBException, IOException {

        byte[] key = VariantAnnotationUtils.buildVariantId(sequenceLocation.getChromosome(),
                sequenceLocation.getStart(), sequenceLocation.getReference(),
                sequenceLocation.getAlternate()).getBytes();
        VariantTraitAssociation variantTraitAssociation = getVariantTraitAssociation(key);
        addNewEntries(variantTraitAssociation, publicSet, traitsToEfoTermsMap);
        rdb.put(key, jsonObjectWriter.writeValueAsBytes(variantTraitAssociation));
    }

    private VariantTraitAssociation getVariantTraitAssociation(byte[] key) throws RocksDBException, IOException {
        byte[] dbContent = rdb.get(key);
        VariantTraitAssociation variantTraitAssociation;
        List<Germline> germline;
        List<Somatic> somatic;
        if (dbContent == null) {
            variantTraitAssociation = new VariantTraitAssociation();
            germline = new ArrayList<>();
            somatic = new ArrayList<>();
            variantTraitAssociation.setGermline(germline);
            variantTraitAssociation.setSomatic(somatic);
            numberNewVariants++;
        } else {
            variantTraitAssociation = mapper.readValue(dbContent, VariantTraitAssociation.class);
            numberVariantUpdates++;
        }
        return variantTraitAssociation;
    }

    private void addNewEntries(VariantTraitAssociation variantTraitAssociation, String variationId, String[] lineFields,
                               Map<String, EFO> traitsToEfoTermsMap) {

        String clinicalSignificance = EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_CLINSIG_COLUMN])
                ? null : lineFields[VARIANT_SUMMARY_CLINSIG_COLUMN];
        List<String> geneNames = EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_GENE_COLUMN])
                ? null: Arrays.asList(lineFields[VARIANT_SUMMARY_GENE_COLUMN].split(","));
        String reviewStatus = EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_REVIEW_COLUMN])
                ? null : lineFields[VARIANT_SUMMARY_REVIEW_COLUMN];

        Germline germline = null;
        Somatic somatic = null;

//        if (!EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_ORIGIN_COLUMN])) {
        // Create a set to avoid situations like germline;germline;germline
        Set<String> originSet = new HashSet<String>(Arrays.asList(lineFields[VARIANT_SUMMARY_ORIGIN_COLUMN]
                .toLowerCase().split(";")));
        boolean addGermline = true;
        if (originSet.contains(SOMATIC)) {
            somatic = new Somatic();
            somatic.setSource(CLINVAR_NAME);
            somatic.setReviewStatus(reviewStatus);
            somatic.setGeneNames(geneNames);
            somatic.setAccession(variationId);
            somatic.setPrimaryHistology(EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_PHENOTYPE_COLUMN])
                    ? null : lineFields[VARIANT_SUMMARY_PHENOTYPE_COLUMN]);
            variantTraitAssociation.getSomatic().add(somatic);

            // Origin for a number of the variants may not be clear, values found in the database for this field are:
            // {"germline",  "unknown",  "inherited",  "maternal",  "de novo",  "paternal",  "not provided",  "somatic",
            // "uniparental",  "biparental",  "tested-inconclusive",  "not applicable"}
            // For the sake of simplicity and since it's not clear what to do with the rest of tags, we'll classify
            // as somatic only those with the "somatic" tag and the rest will be stored as germline
            addGermline = originSet.size() > 1;
        }

        // Origin for a number of the variants may not be clear, values found in the database for this field are:
        // {"germline",  "unknown",  "inherited",  "maternal",  "de novo",  "paternal",  "not provided",  "somatic",
        // "uniparental",  "biparental",  "tested-inconclusive",  "not applicable"}
        // For the sake of simplicity and since it's not clear what to do with the rest of tags, we'll classify
        // as somatic only those with the "somatic" tag and the rest will be stored as germline
        if (addGermline) {
            germline = new Germline();
            germline.setAccession(variationId);
            germline.setClinicalSignificance(clinicalSignificance);
            germline.setDisease(EtlCommons.isMissing(lineFields[VARIANT_SUMMARY_PHENOTYPE_COLUMN])
                    ? null : Arrays.asList(lineFields[VARIANT_SUMMARY_PHENOTYPE_COLUMN]));
            germline.setReviewStatus(reviewStatus);
            germline.setSource(CLINVAR_NAME);
            germline.setGeneNames(geneNames);
            variantTraitAssociation.getGermline().add(germline);
        }
//        }
    }

    private void addNewEntries(VariantTraitAssociation variantTraitAssociation, PublicSetType publicSet,
                               Map<String, EFO> traitsToEfoTermsMap) {

        String accession = publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc();
        String clinicalSignificance = publicSet.getReferenceClinVarAssertion().getClinicalSignificance().getDescription();
        String reviewStatus = publicSet.getReferenceClinVarAssertion().getClinicalSignificance().getReviewStatus().name();
        List<String> geneNames = getGeneNames(publicSet);
        List<Submission> submissions = getSubmissionList(publicSet);
        List<String> germlineBibliography = new ArrayList<>();
        List<String> somaticBibliography = new ArrayList<>();
        Germline germline = null;
        Somatic somatic = null;
        boolean hasGermlineAnnotation = false;
        boolean hasSomaticAnnotation = false;
        for (ObservationSet observationSet : publicSet.getReferenceClinVarAssertion().getObservedIn()) {
            // Origin for a number of the variants may not be clear, values found in the database for this field are:
            // {"germline",  "unknown",  "inherited",  "maternal",  "de novo",  "paternal",  "not provided",  "somatic",
            // "uniparental",  "biparental",  "tested-inconclusive",  "not applicable"}
            // For the sake of simplicity and since it's not clear what to do with the rest of tags, we'll classify
            // as somatic only those with the "somatic" tag and the rest will be stored as germline
            if (observationSet.getSample().getOrigin().equalsIgnoreCase("somatic")) {
                hasSomaticAnnotation = true;
                somaticBibliography = addBibliographyFromObservationSet(somaticBibliography, observationSet);
            } else {
                hasGermlineAnnotation = true;
                germlineBibliography = addBibliographyFromObservationSet(germlineBibliography, observationSet);
            }

        }

        if (hasSomaticAnnotation) {
            somatic = new Somatic();
            somatic.setBibliography(germlineBibliography);
            somatic.setSource(CLINVAR_NAME);
            somatic.setReviewStatus(reviewStatus);
            somatic.setGeneNames(geneNames);
            somatic.setSubmissions(submissions);
            somatic.setAccession(accession);
            somatic.setPrimaryHistology(getPreferredTraitName(publicSet, traitsToEfoTermsMap));
            variantTraitAssociation.getSomatic().add(somatic);
            if (somaticBibliography.size() > 0) {
                somatic.setBibliography(somaticBibliography);
            }
            numberSomaticRecords++;
        }
        if (hasGermlineAnnotation) {
            germline = new Germline();
            germline.setAccession(accession);
            germline.setClinicalSignificance(clinicalSignificance);
            germline.setDisease(getDisease(publicSet, traitsToEfoTermsMap));
            germline.setReviewStatus(reviewStatus);
            germline.setSource(CLINVAR_NAME);
            germline.setInheritanceModel(getInheritanceModel(publicSet));
            germline.setGeneNames(geneNames);
            germline.setSubmissions(submissions);
            variantTraitAssociation.getGermline().add(germline);
            if (germlineBibliography.size() > 0) {
                germline.setBibliography(germlineBibliography);
            }
            numberGermlineRecords++;
        }

    }

    private List<Submission> getSubmissionList(PublicSetType publicSet) {
        List<Submission> submissionList = new ArrayList<>(publicSet.getClinVarAssertion().size());
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
//                date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(measureTraitType.getClinicalSignificance()
//                        .getDateLastEvaluated().getMillisecond());
            } else {
                date = String.format("%04d", measureTraitType.getClinVarAccession().getDateUpdated().getYear())
                        + String.format("%02d", measureTraitType.getClinVarAccession().getDateUpdated().getMonth())
                        + String.format("%02d", measureTraitType.getClinVarAccession().getDateUpdated().getDay());
//                date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(measureTraitType.getClinVarAccession()
//                        .getDateUpdated().getMillisecond());
            }
            submissionList.add(new Submission(measureTraitType.getClinVarSubmissionID().getSubmitter(), date));
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

    private List<String> getInheritanceModel(PublicSetType publicSet) {
        Set<String> inheritanceModelSet = new HashSet<>();
        for (TraitType trait : publicSet.getReferenceClinVarAssertion().getTraitSet().getTrait()) {
            if (trait.getAttributeSet() != null) {
                for (TraitType.AttributeSet attributeSet : trait.getAttributeSet()) {
                    if (attributeSet.getAttribute().getType()
                            != null && attributeSet.getAttribute().getType().equalsIgnoreCase("modeofinheritance")) {
                        inheritanceModelSet.add(attributeSet.getAttribute().getValue().toLowerCase());
                    }
                }
            }
        }

        if (inheritanceModelSet.size() == 0) {
            return null;
        } else if (inheritanceModelSet.size() > 1) {
            numberMultipleInheritanceModels++;
            return new ArrayList<>(inheritanceModelSet);
        } else {
            return new ArrayList<>(inheritanceModelSet);
        }
    }

    private List<String> getGeneNames(PublicSetType publicSet) {
        Set<String> geneIdSet = new HashSet<>();
        for (MeasureSetType.Measure measure : publicSet.getReferenceClinVarAssertion().getMeasureSet().getMeasure()) {
            if (measure.getMeasureRelationship() != null) {
                for (MeasureSetType.Measure.MeasureRelationship measureRelationship : measure.getMeasureRelationship()) {
                    if (measureRelationship.getSymbol() != null) {
                        for (SetElementSetType setElementSet : measureRelationship.getSymbol()) {
                            if (setElementSet.getElementValue() != null) {
                                if (setElementSet.getElementValue().getValue() != null) {
                                    geneIdSet.add(setElementSet.getElementValue().getValue());
                                }
                            }
                        }
                    }
                }
            }
        }
        if (geneIdSet.size() > 0) {
            return new ArrayList<>(geneIdSet);
        } else {
            return null;
        }
    }

    private List<String> getDisease(PublicSetType publicSet, Map<String, EFO> traitsToEfoTermsMap) {
        Set<String> diseaseList = new HashSet<>();
        for (TraitType trait : publicSet.getReferenceClinVarAssertion().getTraitSet().getTrait()) {
//            if (!trait.getType().equalsIgnoreCase("disease")) {
//                logger.warn("Entry {}. trait type = {}. No action defined for these traits. Skipping entry",
//                        publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc(), trait.getType());
////                System.exit(1);
//            } else {
            for (SetElementSetType setElementSet : trait.getName()) {
                diseaseList.add(setElementSet.getElementValue().getValue());
                if (setElementSet.getElementValue().getType().equalsIgnoreCase("preferred")) {
                    if (traitsToEfoTermsMap != null  // May be null if no traits -> EFO file is provided
                            && traitsToEfoTermsMap.get(setElementSet.getElementValue().getValue()) != null) {
                        diseaseList.add(traitsToEfoTermsMap.get(setElementSet.getElementValue().getValue()).name);
                    }
                }
            }
//            }
        }

        if (diseaseList.size() == 0) {
            logger.warn("Entry {}. No \"disease\" entry found among the traits",
                    publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc());
            numberNoDiseaseTrait++;
        }

        return new ArrayList<>(diseaseList);
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
                }

            }
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
