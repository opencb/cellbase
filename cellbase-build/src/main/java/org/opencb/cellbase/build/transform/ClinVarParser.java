package org.opencb.cellbase.build.transform;

import org.opencb.biodata.formats.feature.refseq.RefseqUtils;
import org.opencb.biodata.formats.variant.clinvar.ClinvarParser;
import org.opencb.biodata.formats.variant.clinvar.ClinvarPublicSet;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.MeasureSetType;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.PublicSetType;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.ReleaseType;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.SequenceLocationType;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.nio.file.Path;

/**
 * Created by imedina on 26/09/14.
 */
public class ClinVarParser extends CellBaseParser{

    public static final String GRCH37_ASSEMBLY = "GRCh37";
    private Path clinvarXmlFile;

    public ClinVarParser(CellBaseSerializer serializer, Path clinvarXmlFile) {
        super(serializer);
        this.clinvarXmlFile = clinvarXmlFile;
    }

    public void parse() {
        try {
            logger.info("Unmarshalling clinvar file " + clinvarXmlFile + " ...");
            JAXBElement<ReleaseType> clinvarRelease = unmarshalXML(clinvarXmlFile);
            logger.info("Done");

            long serializedClinvarObjects = 0,
                    clinvarRecordsParsed = 0;
            logger.info("Serializing clinvar records that have Sequence Location for Assemlby " + GRCH37_ASSEMBLY + " ...");
            for (PublicSetType publicSet : clinvarRelease.getValue().getClinVarSet()) {
                ClinvarPublicSet clinvarPublicSet = buildClinvarPublicSet(publicSet);
                if (clinvarPublicSet != null) {
                    serialize(clinvarPublicSet);
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
        logger.info("");
        logger.info("Summary");
        logger.info("=======");
        logger.info("Processed " + clinvarRecordsParsed + " clinvar records");
        logger.info("Serialized " + serializedClinvarObjects + " '" + ClinvarPublicSet.class.getName() + "' objects");
        if (clinvarRecordsParsed != serializedClinvarObjects) {
            logger.info((clinvarRecordsParsed - serializedClinvarObjects) + " clinvar records not serialized because don't have complete Sequence Location for assembly " + GRCH37_ASSEMBLY);
        }
    }

    private ClinvarPublicSet buildClinvarPublicSet(PublicSetType publicSet) {
        //Variant variant = obtainVariant(publicSet);
        ClinvarPublicSet clinvarPublicSet = null;
        SequenceLocationType sequenceLocation = obtainAssembly37SequenceLocation(publicSet);
        if (sequenceLocation != null) {
            clinvarPublicSet = new ClinvarPublicSet(RefseqUtils.refseqNCAccessionToChromosome(sequenceLocation.getAccession()),
                    sequenceLocation.getStart().longValue(),
                    sequenceLocation.getStop().longValue(),
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
        return location.getAssembly().startsWith(GRCH37_ASSEMBLY) && 
                location.getReferenceAllele() != null && 
                location.getAlternateAllele() != null &&
                location.getStart() != null &&
                location.getStop() != null;
    }

    private JAXBElement<ReleaseType> unmarshalXML(Path clinvarXmlFile) throws JAXBException {
        return (JAXBElement<ReleaseType>) ClinvarParser.loadXMLInfo(clinvarXmlFile.toString(), ClinvarParser.CLINVAR_CONTEXT_v19);
    }

//    public void parse(Path uniprotFilesDir) throws IOException {
//        Files.exists(uniprotFilesDir);
//        objectMapper = new ObjectMapper();
//        ClinvarParser clinVarParser = new ClinvarParser();
//        try {
//            File[] files = uniprotFilesDir.toFile().listFiles(new FilenameFilter() {
//                @Override
//                public boolean accept(File dir, String name) {
//                    return name.endsWith(".xml");
//                }
//            });
//
//            for(File file: files) {
////                System.out.println("processing... "+file.toString());
//                JAXBElement<ReleaseType> uniprot =
//                for(PublicSetType publicSetType: uniprot.getValue().getClinVarSet()) {
//                    System.out.println(objectMapper.writeValueAsString(publicSetType));
////                return;
//                }
//            }
//
//        } catch (JAXBException e) {
//            e.printStackTrace();
//        } finally {
////			pw.close();
//        }
//
//    }
}
