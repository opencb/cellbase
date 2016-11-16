package org.opencb.cellbase.app.transform.clinical.variant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.formats.variant.clinvar.ClinvarParser;
import org.opencb.biodata.formats.variant.clinvar.v24jaxb.*;
import org.opencb.biodata.models.variant.avro.Germline;
import org.opencb.biodata.models.variant.avro.Somatic;
import org.opencb.biodata.models.variant.avro.VariantTraitAssociation;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * Created by fjlopez on 28/09/16.
 */
public class ClinVarIndexer extends ClinicalIndexer {

    private static final String CLINVAR_NAME = "clinvar";
    private final Path clinvarXMLFile;
    private final Path clinvarSummaryFile;
    private final Path clinvarEFOFile;
    private final String assembly;
    private RocksDB rdb;
    private int numberSomaticRecords = 0;
    private int numberGermlineRecords = 0;

    public ClinVarIndexer(Path clinvarXMLFile, Path clinvarSummaryFile, Path clinvarEFOFile, String assembly,
                          RocksDB rdb) {
        super();
        this.rdb = rdb;
        this.clinvarXMLFile = clinvarXMLFile;
        this.clinvarSummaryFile = clinvarSummaryFile;
        this.clinvarEFOFile = clinvarEFOFile;
        this.assembly = assembly;
    }

    public void index() throws RocksDBException {
        try {
            logger.info("Unmarshalling clinvar file " + clinvarXMLFile + " ...");
            JAXBElement<ReleaseType> clinvarRelease = unmarshalXML(clinvarXMLFile);
            logger.info("Done");

            Map<String, EFO> traitsToEfoTermsMap = loadEFOTerms();
            Map<String, SequenceLocation> rcvTo37SequenceLocation = loadSequenceLocation();

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
    }

    private void updateRocksDB(SequenceLocation sequenceLocation, PublicSetType publicSet,
                               Map<String, EFO> traitsToEfoTermsMap) throws RocksDBException, IOException {

        byte[] key = VariantAnnotationUtils.buildVariantId(sequenceLocation.getChromosome(),
                sequenceLocation.getStart(), sequenceLocation.getReference(),
                sequenceLocation.getAlternate()).getBytes();
        byte[] dbContent = rdb.get(key);
        VariantTraitAssociation variantTraitAssociation;
        List<Germline> germline;
        List<Somatic> somatic;
        ObjectMapper mapper = new ObjectMapper();
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
        addNewEntries(variantTraitAssociation, publicSet, traitsToEfoTermsMap);
        ObjectWriter jsonObjectWriter = mapper.writer();
        rdb.put(key, jsonObjectWriter.writeValueAsBytes(variantTraitAssociation));
    }

    private void addNewEntries(VariantTraitAssociation variantTraitAssociation, PublicSetType publicSet,
                               Map<String, EFO> traitsToEfoTermsMap) {

        String accession = publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc();
        String clinicalSignificance = publicSet.getReferenceClinVarAssertion().getClinicalSignificance().getDescription();
        String reviewStatus = publicSet.getReferenceClinVarAssertion().getClinicalSignificance().getReviewStatus().name();
        List<String> geneNames = getGeneNames(publicSet);
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
            somatic.setBibliography(somaticBibliography);
            somatic.setAccession(accession);
            somatic.setPrimaryHistology(getPreferredTraitName(publicSet, traitsToEfoTermsMap));
            variantTraitAssociation.getSomatic().add(somatic);
            numberSomaticRecords++;
        }
        if (hasGermlineAnnotation) {
            germline = new Germline();
            germline.setAccession(accession);
            germline.setClinicalSignificance(clinicalSignificance);
            germline.setDisease(getDisease(publicSet, traitsToEfoTermsMap));
            germline.setReviewStatus(reviewStatus);
            germline.setSource(CLINVAR_NAME);
            germline.setBibliography(germlineBibliography);
            germline.setInheritanceModel(getInheritanceModel(publicSet));
            germline.setGeneNames(geneNames);
            variantTraitAssociation.getGermline().add(germline);
            numberGermlineRecords++;
        }

    }

    private String getPreferredTraitName(PublicSetType publicSet, Map<String, EFO> traitsToEfoTermsMap) {
        for (TraitType trait : publicSet.getReferenceClinVarAssertion().getTraitSet().getTrait()) {
            if (!trait.getType().equalsIgnoreCase("disease")) {
                logger.error("Trait type != disease - no action defined for these traits");
                System.exit(1);
            } else {
                for (SetElementSetType setElementSet : trait.getName()) {
                    if (setElementSet.getElementValue().getType().equalsIgnoreCase("preferred")) {
                        if (traitsToEfoTermsMap.get(setElementSet.getElementValue().getValue()) != null) {
                            return traitsToEfoTermsMap.get(setElementSet.getElementValue().getValue()).name;
                        } else {
                            return setElementSet.getElementValue().getValue();
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getInheritanceModel(PublicSetType publicSet) {
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
        if (inheritanceModelSet.size() > 1) {
            logger.error("More than one inheritance model was found within an RCV record - code is not ready for this");
            System.exit(1);
        }

        return inheritanceModelSet.iterator().next();
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
            if (!trait.getType().equalsIgnoreCase("disease")) {
                logger.error("Trait type != disease - no action defined for these traits");
                System.exit(1);
            } else {
                for (SetElementSetType setElementSet : trait.getName()) {
                    diseaseList.add(setElementSet.getElementValue().getValue());
                    if (setElementSet.getElementValue().getType().equalsIgnoreCase("preferred")) {
                        if (traitsToEfoTermsMap.get(setElementSet.getElementValue().getValue()) != null) {
                            diseaseList.add(traitsToEfoTermsMap.get(setElementSet.getElementValue().getValue()).name);
                        }
                    }
                }
            }
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

    private Map<String, SequenceLocation> loadSequenceLocation() throws IOException {
        logger.info("Loading ClinVar {} genomic coordinates, reference and alternate strings from {}...",
                assembly, clinvarSummaryFile);
        BufferedReader bufferedReader;
        bufferedReader = FileUtils.newBufferedReader(clinvarSummaryFile);

        Map<String, SequenceLocation> rcvToSequenceLocation = new HashMap<>();
        // Skip header, read first data line
        bufferedReader.readLine();
        String line = bufferedReader.readLine();
        while (line != null) {
            String[] parts = line.split("\t");
            // Check assembly
            if (parts[12].equals(assembly)) {
                SequenceLocation sequenceLocation = new SequenceLocation(parts[13], Integer.valueOf(parts[14]),
                        Integer.valueOf(parts[15]), parts[25], parts[26]);
                // Each line may contain more than one RCV; e.g.: RCV000000019;RCV000000020;RCV000000021;RCV000000022;...
                String[] rcvArray = parts[8].split(";");
                for (String rcv : rcvArray) {
                    rcvToSequenceLocation.put(rcv, sequenceLocation);
                }
            }
            line = bufferedReader.readLine();
        }
        return rcvToSequenceLocation;
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

        public EFO(String id, String name, String url) {
            this.id = id;
            this.name = name;
            this.url = url;
        }
    }

}
