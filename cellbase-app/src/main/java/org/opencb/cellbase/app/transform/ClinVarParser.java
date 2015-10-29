/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.app.transform;

import org.opencb.biodata.formats.variant.clinvar.ClinvarParser;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.*;
import org.opencb.cellbase.core.common.clinical.ClinvarPublicSet;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by imedina on 26/09/14.
 */
public class ClinVarParser extends CellBaseParser{

    private static final String ASSEMBLY_PREFIX = "GRCh";
    public static final String GRCH37_ASSEMBLY = "37";
    public static final String GRCH38_ASSEMBLY = "38";
    private static final String PREFERRED_TYPE = "Preferred";
    public static final String EFO_ID = "EFO id";
    public static final String EFO_NAME = "EFO name";
    public static final String EFO_URL = "EFO URL";

    private final String selectedAssembly;

    private Path clinvarXmlFile;
    private Path efosFile;

    public ClinVarParser(Path clinvarXmlFile, Path efosFile, String assembly, CellBaseSerializer serializer) {
        super(serializer);
        this.clinvarXmlFile = clinvarXmlFile;
        this.efosFile = efosFile;
        this.selectedAssembly = ASSEMBLY_PREFIX + assembly;
    }

    public void parse() {
        try {
            logger.info("Unmarshalling clinvar file " + clinvarXmlFile + " ...");
            JAXBElement<ReleaseType> clinvarRelease = unmarshalXML(clinvarXmlFile);
            logger.info("Done");

            Map<String, EFO> traitsToEfoTermsMap = loadEFOTerms();
            Map<String, SequenceLocationType> rcvTo37SequenceLocation= loadSequenceLocation();  // TODO implement this method

            long serializedClinvarObjects = 0,
                    clinvarRecordsParsed = 0,
                    clinvarObjectsWithEfo = 0;

            logger.info("Serializing clinvar records that have Sequence Location for Assembly " + selectedAssembly + " ...");
            for (PublicSetType publicSet : clinvarRelease.getValue().getClinVarSet()) {
                SequenceLocationType sequenceLocation =
                        rcvTo37SequenceLocation.get(publicSet.getReferenceClinVarAssertion().getClinVarAccession().getAcc());
                if (sequenceLocation != null) {
                    ClinvarPublicSet clinvarPublicSet = new ClinvarPublicSet(sequenceLocation.getChr(),
                            sequenceLocation.getStart().intValue(),
                            sequenceLocation.getStop().intValue(),
                            sequenceLocation.getReferenceAllele(),
                            sequenceLocation.getAlternateAllele(),
                            publicSet);
                    if (clinvarRecordHasAssociatedEfos(clinvarPublicSet, traitsToEfoTermsMap)) {
                        clinvarObjectsWithEfo++;
                    }
                    serializer.serialize(clinvarPublicSet);
                    serializedClinvarObjects++;
                }
                clinvarRecordsParsed++;
            }
            logger.info("Done");
            this.printSummary(clinvarRecordsParsed, serializedClinvarObjects, clinvarObjectsWithEfo);


        } catch (JAXBException e) {
            logger.error("Error unmarshalling clinvar Xml file "+ clinvarXmlFile + ": " + e.getMessage());
        } catch (IOException e) {
            logger.error("File not found: "+ clinvarXmlFile + ": " + e.getMessage());
        }
    }

    private Map<String, EFO> loadEFOTerms() {
        if (efosFile != null) {
            logger.info("Loading EFO terms ...");
            Map<String, EFO> efoTerms = new HashMap<>();
            try (Stream<String> linesStream = Files.lines(efosFile)) {
                linesStream.forEach(line -> addEfoTermToMap(line, efoTerms));
                logger.info("Done");
                return efoTerms;
            } catch (IOException e) {
                logger.error("Error loading EFO file: " + e.getMessage());
                logger.error("EFO terms won't be added");
            }
        }else {
            logger.warn("No EFO terms file present: EFO terms won't be added");
        }
        return null;
    }

    private void addEfoTermToMap(String line, Map<String, EFO> efoTerms) {
        String[] columns = line.split("\t");
        efoTerms.put(columns[0], new EFO(columns[2], columns[3], columns[1]));
    }

    private boolean clinvarRecordHasAssociatedEfos(ClinvarPublicSet clinvarPublicSet, Map<String, EFO> efoTerms) {
        if (efosFile != null) {
            boolean hasEfo = false;
            List<TraitType> traits = clinvarPublicSet.getClinvarSet().getReferenceClinVarAssertion().getTraitSet().getTrait();
            for (TraitType trait : traits) {
                hasEfo = traitHasEfo(efoTerms, hasEfo, trait);
            }
            return hasEfo;
        }
        return false;
    }

    private boolean traitHasEfo(Map<String, EFO> efoTerms, boolean hasEfo, TraitType trait) {
        List<SetElementSetType> traitNames = trait.getName();
        String preferredTraitName = getPreferredTraitName(traitNames);
        if (preferredTraitName != null) {
            EFO efo = efoTerms.get(preferredTraitName);
            if (efo != null) {
                hasEfo = true;
                addEfoToClinvarTraitNames(trait.getName(), efo);
            }
        }
        return hasEfo;
    }

    private String getPreferredTraitName(List<SetElementSetType> traitNames) {
        for (SetElementSetType name: traitNames) {
            if (name.getElementValue().getType().equals(PREFERRED_TYPE)) {
                return name.getElementValue().getValue();
            }
        }
        return null;
    }

    private void addEfoToClinvarTraitNames(List<SetElementSetType> names, EFO efo) {
        addClinvarTraitName(names, EFO_ID, efo.id);
        addClinvarTraitName(names, EFO_NAME, efo.name);
        addClinvarTraitName(names, EFO_URL, efo.url);
    }

    private void addClinvarTraitName(List<SetElementSetType> names, String type, String value){
        SetElementSetType.ElementValue efoIdValue = new SetElementSetType.ElementValue();
        efoIdValue.setType(type);
        efoIdValue.setValue(value);
        SetElementSetType efoElement = new SetElementSetType();
        efoElement.setElementValue(efoIdValue);
        names.add(efoElement);
    }

    private void printSummary(long clinvarRecordsParsed, long serializedClinvarObjects, long clinvarObjectsWithEfo) {
        NumberFormat formatter = NumberFormat.getInstance();
        logger.info("");
        logger.info("Summary");
        logger.info("=======");
        logger.info("Processed " + formatter.format(clinvarRecordsParsed) + " clinvar records");
        logger.info("Serialized " + formatter.format(serializedClinvarObjects) + " '" + ClinvarPublicSet.class.getName() + "' objects");
        if (clinvarRecordsParsed != serializedClinvarObjects) {
            logger.info(formatter.format(clinvarRecordsParsed - serializedClinvarObjects) + " clinvar records not serialized because don't have complete Sequence Location for assembly " + selectedAssembly);
        }
        if (efosFile != null) {
            NumberFormat percentageFormatter = NumberFormat.getPercentInstance();
            logger.info(formatter.format(clinvarObjectsWithEfo) + " clinvar records (" + percentageFormatter.format((double) clinvarObjectsWithEfo / serializedClinvarObjects) + " of serialized) have at least one associated EFO term");
        }
    }

    @Deprecated
    private ClinvarPublicSet buildClinvarPublicSet(PublicSetType publicSet, SequenceLocationType sequenceLocation) {
        //Variant variant = obtainVariant(publicSet);
        ClinvarPublicSet clinvarPublicSet = null;
//        SequenceLocationType sequenceLocation = obtainAssembly37SequenceLocation(publicSet);
        if (sequenceLocation != null) {

            clinvarPublicSet = new ClinvarPublicSet(sequenceLocation.getChr(),
                    sequenceLocation.getStart().intValue(),
                    sequenceLocation.getStop().intValue(),
                    sequenceLocation.getReferenceAllele(),
                    sequenceLocation.getAlternateAllele(),
                    publicSet);
        }
        return clinvarPublicSet;
    }

    @Deprecated
    private SequenceLocationType obtainAssembly37SequenceLocation(PublicSetType publicSet) {
        for (MeasureSetType.Measure measure : publicSet.getReferenceClinVarAssertion().getMeasureSet().getMeasure()) {
            for (SequenceLocationType location :  measure.getSequenceLocation()) {
                if (validLocation(location)) {
                    return location;
                }
            }
        }
        return null;
    }

    private boolean validLocation(SequenceLocationType location) {
        return location.getAssembly().startsWith(selectedAssembly) &&
                location.getReferenceAllele() != null && 
                location.getAlternateAllele() != null &&
                location.getStart() != null &&
                location.getStop() != null;
    }

    private JAXBElement<ReleaseType> unmarshalXML(Path clinvarXmlFile) throws JAXBException, IOException {
        return (JAXBElement<ReleaseType>) ClinvarParser.loadXMLInfo(clinvarXmlFile.toString(), ClinvarParser.CLINVAR_CONTEXT_v19);
    }

    class EFO {
        private final String id;
        private final String name;
        private final String url;

        public EFO(String id, String name, String URL ) {

            this.id = id;
            this.name = name;
            url = URL;
        }
    }
}
