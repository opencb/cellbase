package org.opencb.cellbase.build.transform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.formats.variant.clinvar.ClinvarParser;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.MeasureTraitType;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.PublicSetType;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.ReleaseType;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by imedina on 26/09/14.
 */
public class ClinVarParser {

    private CellBaseSerializer serializer;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    ObjectMapper objectMapper;

    public ClinVarParser(CellBaseSerializer serializer) {
        this.serializer = serializer;
    }

    public void parse(Path uniprotFilesDir) throws IOException {
        Files.exists(uniprotFilesDir);
        objectMapper = new ObjectMapper();
        ClinvarParser clinVarParser = new ClinvarParser();
        try {
            File[] files = uniprotFilesDir.toFile().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            });

            for(File file: files) {
//                System.out.println("processing... "+file.toString());
                JAXBElement<ReleaseType> uniprot = (JAXBElement<ReleaseType>) clinVarParser.loadXMLInfo(file.toString(), ClinvarParser.CLINVAR_CONTEXT_v19);
                for(PublicSetType publicSetType: uniprot.getValue().getClinVarSet()) {
                    System.out.println(objectMapper.writeValueAsString(publicSetType));
//                return;
                }
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        } finally {
//			pw.close();
        }

    }
}
