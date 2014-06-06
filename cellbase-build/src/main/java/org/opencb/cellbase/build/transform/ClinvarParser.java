package org.opencb.cellbase.build.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.picard.reference.FastaSequenceIndex;
import net.sf.picard.reference.IndexedFastaSequenceFile;
import org.gbpa.mutalyzer.webservice.Mutalyzer;
import org.gbpa.mutalyzer.webservice.MutalyzerService;
import org.gbpa.mutalyzer.webservice.StringArray;
import org.opencb.biodata.formats.feature.refseq.Refseq;
import org.opencb.biodata.formats.variant.hgvs.Hgvs;
import org.opencb.biodata.models.variant.clinical.clinvar.v1_5jaxb.MeasureSetType;
import org.opencb.biodata.models.variant.clinical.clinvar.v1_5jaxb.PublicSetType;
import org.opencb.biodata.models.variant.clinical.clinvar.v1_5jaxb.ReleaseType;
import org.opencb.biodata.models.variant.clinical.clinvar.v1_5jaxb.SequenceLocationType;


import javax.sound.midi.Sequence;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by parce on 5/22/14.
 */
public class ClinvarParser {

    public static final String CLINVAR_CONTEXT_V1_5 = "org.opencb.biodata.models.variant.clinical.clinvar.v1_5jaxb";

    // TODO: variable solo para desarrollo, borrar
    private Map<String, Long> cuentaCosas = new TreeMap<>();

    private Path clinvarXmlFile;
    private Path outputFile;
    private IndexedFastaSequenceFile genomeSequenceFastaFile;

    public Mutalyzer mutalyzerClient;

    private ObjectMapper jsonMapper;

    public ClinvarParser(Path clinvarXmlFile, Path outputFile) {
        this.clinvarXmlFile = clinvarXmlFile;
        this.outputFile = outputFile;
        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        this.mutalyzerClient = new MutalyzerService().getMutalyzer();

    }

    public ClinvarParser (Path clinvarXmlFile, Path genomeSequenceFastaFile, Path genomeSequenceFastaIndexFile, Path outputFile) {
        this(clinvarXmlFile, outputFile);
        this.genomeSequenceFastaFile =new IndexedFastaSequenceFile(genomeSequenceFastaFile.toFile(), new FastaSequenceIndex(genomeSequenceFastaIndexFile.toFile()));

    }


    public void parseClinvar() {
        try {
            ReleaseType clinvarRelease = unmarshalXML(clinvarXmlFile);
            SortedMap<SequenceLocationType,String> clinvarMap;
            clinvarMap = this.sortClinvarsetByLocation(clinvarRelease);
            writeOutputFile(clinvarMap);
        } catch (JAXBException e) {
            // TODO: logger?
            System.out.println("Error unmarshalling clinvar xml file " + clinvarXmlFile);
            e.printStackTrace();
        }
    }

    private void writeOutputFile(SortedMap<SequenceLocationType, String> clinvarMap) {
        try (PrintWriter outputWriter = new PrintWriter(Files.newBufferedWriter(outputFile, Charset.defaultCharset(), StandardOpenOption.CREATE));) {
            for (Map.Entry<SequenceLocationType,String> entry : clinvarMap.entrySet()) {
                SequenceLocationType location = entry.getKey();
                String clinvarJson = entry.getValue();
                outputWriter.println(location.getChr() + "\t" + location.getStart() + "\t" + location.getReferenceAllele() + "\t" + location.getAlternateAllele() + "\t" + clinvarJson);
            }
        } catch (IOException e) {
            // TODO: logger?
            System.out.println("Error opening output file " + outputFile + " for writting: " + e.getMessage());
        }
    }


    private SortedMap<SequenceLocationType,String> sortClinvarsetByLocation(ReleaseType clinvarRelease) {
        SortedMap<SequenceLocationType,String> clinvarMap = new TreeMap<>(new SequenceLocationComparator());
        for (PublicSetType clinvarSet : clinvarRelease.getClinVarSet()) {
            SequenceLocationType location = getSequenceLocation(clinvarSet);
            if (location != null) {
                try {
                    String clinvarJson = this.jsonMapper.writeValueAsString(clinvarSet);
                    clinvarMap.put(location, clinvarJson);
                    this.cuenta("Transformados");
                } catch (JsonProcessingException e) {
                    // TODO: dejar un mensaje??
                    this.cuenta("Error Procesado JSON");
                    //e.printStackTrace();
                }

            }
        }
        return clinvarMap;
    }


    private static ReleaseType unmarshalXML(Path inputFile) throws JAXBException {
        JAXBElement<ReleaseType> obj = null;
        JAXBContext jaxbContext = JAXBContext.newInstance(CLINVAR_CONTEXT_V1_5);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Long init = System.currentTimeMillis();
        obj = (JAXBElement)unmarshaller.unmarshal(inputFile.toFile());
        ReleaseType release = obj.getValue();
        long end = System.currentTimeMillis();
        // TODO: eliminar estos mensajes de debug
        System.out.println("Read "  + release.getClinVarSet().size() + " clinvar elements in " + ((end - init)/1000) + "s.");
        return release;
    }

    private SequenceLocationType getSequenceLocation(PublicSetType clinVarSet) {
        SequenceLocationType sequenceLocation = null;
        sequenceLocation = this.obtainSequenceLocationFromHgvs(clinVarSet);
        if (sequenceLocation != null) {
            cuenta("sequenceLocationFromHgvs");
        } else {
            cuenta("sequenceLocation null");
        }

        // TODO: Sacamos esto a la clase Fasta??? ¿Hace falta? Creo que no porque ya lo hacemos en el hgvs
        //getReferenceAndAlternative(sequenceLocation);
        return sequenceLocation;
    }


    private SequenceLocationType obtainSequenceLocationFromHgvs(PublicSetType clinVarSet) {
        String genomicHgvs = null;
        for (MeasureSetType.Measure measure : clinVarSet.getReferenceClinVarAssertion().getMeasureSet().getMeasure()) {
            // bucle para contar los hgvs del reference
            for (MeasureSetType.Measure.AttributeSet attributeSet : measure.getAttributeSet()) {
                String attributeType = attributeSet.getAttribute().getType();
                if (attributeType.substring(0, 4).equalsIgnoreCase(Hgvs.HGVS)) {
                    String hgvs = attributeSet.getAttribute().getValue();
                    if (hgvs.startsWith(Refseq.REFSEQ_CHROMOSOME_ACCESION_TAG)) {
                        // TODO: ¿no tendremos que comprobar aqui si el cromosoma es HG37 o 38?
                        cuenta("genomicHGVS");
                        genomicHgvs = hgvs;
                        break;
                    } else {
                        //System.out.println("HGVS: " + hgvs);
                        try {
                            StringArray hgvsArray = mutalyzerClient.numberConversion("hg19", hgvs, null);
                            if (hgvsArray != null) {
                                if ("[]".equals(hgvsArray.getString())) {
                                    // TODO: probar si alguna vez entra aqui, que no estoy seguro
                                    hgvsArray = mutalyzerClient.numberConversion("hg18", hgvs, null);
                                }
                                if (hgvsArray.getString().get(0).startsWith(Refseq.REFSEQ_CHROMOSOME_ACCESION_TAG)) {
                                    genomicHgvs = hgvsArray.getString().get(0);
                                    cuenta("nonGenomicHgvs");
                                    break;
                                }
                            }
                        } catch (SOAPFaultException ex) {
                            this.cuenta("SOAPFault");
                        }
                    }
                }
            }
        }
        if (genomicHgvs == null) {
            this.cuenta("no Hgvs valido");
            return null;
        }

        return new Hgvs(genomicHgvs).getSequenceLocation(genomeSequenceFastaFile);
    }

    // TODO: solo desarrollo, borrar
    private void cuenta(String key) {
        Long cuenta = cuentaCosas.get(key);
        if (cuenta == null) {
            cuentaCosas.put(key, new Long(1));
        } else {
            cuentaCosas.put(key, cuenta + 1);
        }
    }

    public void setGenomeSequenceFastaFile(IndexedFastaSequenceFile indexedFastaFile) {
        this.genomeSequenceFastaFile = indexedFastaFile;
    }

    class SequenceLocationComparator implements Comparator<SequenceLocationType> {

        public int compare(SequenceLocationType loc1, SequenceLocationType loc2) {
            if (!loc1.getAccession().equals(loc2.getAccession())) {
                return loc1.getAccession().compareTo(loc2.getAccession());
            } else {
                return compareSameChromosome(loc1, loc2);
            }
        }

        private int compareSameChromosome(SequenceLocationType loc1, SequenceLocationType loc2) {
            if (!loc1.getStart().equals(loc2.getStart())) {
                return loc1.getStart().compareTo(loc2.getStart());
            } else if (!loc1.getReferenceAllele().equals(loc2.getReferenceAllele())) {
                return loc1.getReferenceAllele().compareTo(loc2.getReferenceAllele());
            } else if (!loc1.getAlternateAllele().equals(loc2.getAlternateAllele())) {
                return loc1.getAlternateAllele().compareTo(loc2.getAlternateAllele());
            } else {
                return 0;
            }
        }
    }
}
