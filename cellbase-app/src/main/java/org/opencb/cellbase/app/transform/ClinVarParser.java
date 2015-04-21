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
import org.opencb.cellbase.core.common.clinical.ClinvarPublicSet;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.MeasureSetType;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.PublicSetType;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.ReleaseType;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.SequenceLocationType;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.nio.file.Path;
import java.text.NumberFormat;

/**
 * Created by imedina on 26/09/14.
 */
public class ClinVarParser extends CellBaseParser{

    private static final String ASSEMBLY_PREFIX = "GRCh";
    public static final String GRCH37_ASSEMBLY = "37";
    public static final String GRCH38_ASSEMBLY = "38";

    private final String selectedAssembly;

    private Path clinvarXmlFile;

    public ClinVarParser(Path clinvarXmlFile, String assembly, CellBaseSerializer serializer) {
        super(serializer);
        this.clinvarXmlFile = clinvarXmlFile;
        this.selectedAssembly = ASSEMBLY_PREFIX + assembly;
    }

    public void parse() {
        try {
            logger.info("Unmarshalling clinvar file " + clinvarXmlFile + " ...");
            JAXBElement<ReleaseType> clinvarRelease = unmarshalXML(clinvarXmlFile);
            logger.info("Done");

            long serializedClinvarObjects = 0,
                    clinvarRecordsParsed = 0;
            logger.info("Serializing clinvar records that have Sequence Location for Assembly " + selectedAssembly + " ...");
            for (PublicSetType publicSet : clinvarRelease.getValue().getClinVarSet()) {
                ClinvarPublicSet clinvarPublicSet = buildClinvarPublicSet(publicSet);
                if (clinvarPublicSet != null) {
                    serializer.serialize(clinvarPublicSet);
                    serializedClinvarObjects++;
                }
                clinvarRecordsParsed++;
            }
            logger.info("Done");
            this.printSummary(clinvarRecordsParsed, serializedClinvarObjects);

        } catch (JAXBException e) {
            logger.error("Error unmarshalling clinvar Xml file "+ clinvarXmlFile + ": " + e.getMessage());
        }
    }

    private void printSummary(long clinvarRecordsParsed, long serializedClinvarObjects) {
        NumberFormat formatter = NumberFormat.getInstance();
        logger.info("");
        logger.info("Summary");
        logger.info("=======");
        logger.info("Processed " + formatter.format(clinvarRecordsParsed) + " clinvar records");
        logger.info("Serialized " + formatter.format(serializedClinvarObjects) + " '" + ClinvarPublicSet.class.getName() + "' objects");
        if (clinvarRecordsParsed != serializedClinvarObjects) {
            logger.info(formatter.format(clinvarRecordsParsed - serializedClinvarObjects) + " clinvar records not serialized because don't have complete Sequence Location for assembly " + selectedAssembly);
        }
    }

    private ClinvarPublicSet buildClinvarPublicSet(PublicSetType publicSet) {
        //Variant variant = obtainVariant(publicSet);
        ClinvarPublicSet clinvarPublicSet = null;
        SequenceLocationType sequenceLocation = obtainAssembly37SequenceLocation(publicSet);
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

    private JAXBElement<ReleaseType> unmarshalXML(Path clinvarXmlFile) throws JAXBException {
        return (JAXBElement<ReleaseType>) ClinvarParser.loadXMLInfo(clinvarXmlFile.toString(), ClinvarParser.CLINVAR_CONTEXT_v19);
    }
}
