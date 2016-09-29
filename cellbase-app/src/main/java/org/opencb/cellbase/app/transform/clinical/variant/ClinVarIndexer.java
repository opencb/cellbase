package org.opencb.cellbase.app.transform.clinical.variant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.formats.variant.clinvar.ClinvarParser;
import org.opencb.biodata.formats.variant.clinvar.v24jaxb.*;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * Created by fjlopez on 28/09/16.
 */
public class ClinVarIndexer {

    private static final String CLINVAR_NAME = "clinvar";
    private final Path clinvarXMLFile;
    private final Path clinvarSummaryFile;
    private final Path clinvarEFOFile;
    private final Logger logger;
    private final String assembly;
    private RocksDB rdb;
    private int numberIndexedRecords = 0;
    private int numberNewVariants = 0;
    private int numberVariantUpdates = 0;
    private int totalNumberRecords = 0;

    public ClinVarIndexer(Path clinvarXMLFile, Path clinvarSummaryFile, Path clinvarEFOFile, String assembly,
                          RocksDB rdb) {
        logger = LoggerFactory.getLogger(this.getClass());
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
            this.printSummary();
        } catch (RocksDBException e) {
            logger.error("Error reading/writing from/to the RocksDB index while indexing ClinVar");
            throw e;
        } catch (JAXBException e) {
            logger.error("Error unmarshalling clinvar Xml file " + clinvarXMLFile + ": " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error indexing clinvar Xml file: " + e.getMessage());
        }
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
        addNewEntries(variantTraitAssociation, publicSet);
        ObjectWriter jsonObjectWriter = mapper.writer();
        rdb.put(key, jsonObjectWriter.writeValueAsBytes(variantTraitAssociation));
    }

    private void addNewEntries(VariantTraitAssociation variantTraitAssociation, PublicSetType publicSet) {

        String accession = publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc();
        String clinicalSignificance = publicSet.getReferenceClinVarAssertion().getClinicalSignificance().getDescription();
        List<String> disease = getDisease(publicSet);
        String inheritanceModel = getInheritanceModel(publicSet);
        String reviewStatus = publicSet.getReferenceClinVarAssertion().getClinicalSignificance().getReviewStatus().name();
        String source = CLINVAR_NAME;
        List<String> geneNames = getGeneNames(publicSet);
        List<String> germlineBibliography = null;
        List<String> somaticBibliography = null;
        Germline germline = null;
        Somatic somatic = null;
        somatic.se
        for (ObservationSet observationSet : publicSet.getReferenceClinVarAssertion().getObservedIn()) {
            if (observationSet.getSample().getOrigin().equalsIgnoreCase("germline")) {
                germlineBibliography = addBibliographyFromObservationSet(germlineBibliography, observationSet);
            } else {
                somaticBibliography = addBibliographyFromObservationSet(somaticBibliography, observationSet);
            }

        }

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

    private List<String> getDisease(PublicSetType publicSet) {
        Set<String> diseaseList = new HashSet<>();
        for (TraitType trait : publicSet.getReferenceClinVarAssertion().getTraitSet().getTrait()) {
            if (!trait.getType().equalsIgnoreCase("disease")) {
                logger.error("Trait type != disease - no action defined for these traits");
                System.exit(1);
            } else {
                for (SetElementSetType setElementSet : trait.getName()) {
                    diseaseList.add(setElementSet.getElementValue().getValue());
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
        if (clinvarSummaryFile.toFile().getName().endsWith(".gz")) {
            bufferedReader =
                    new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(clinvarSummaryFile.toFile()))));
        } else {
            bufferedReader = Files.newBufferedReader(clinvarSummaryFile, Charset.defaultCharset());
        }

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

    class SequenceLocation {
        private final String chromosome;
        private final int start;
        private final int end;
        private final String reference;
        private final String alternate;

        public SequenceLocation(String chromosome, int start, int end, String reference, String alternate) {
            this.chromosome = chromosome;
            this.start = start;
            this.end = end;
            this.reference = reference;
            this.alternate = alternate;
        }

        public String getChromosome() {
            return chromosome;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getReference() {
            return reference;
        }

        public String getAlternate() {
            return alternate;
        }
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
