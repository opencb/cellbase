package org.opencb.cellbase.build.transform;

import org.opencb.biodata.formats.protein.uniprot.UniprotParser;
import org.opencb.biodata.formats.protein.uniprot.v201311jaxb.Entry;
import org.opencb.biodata.formats.protein.uniprot.v201311jaxb.OrganismNameType;
import org.opencb.biodata.formats.protein.uniprot.v201311jaxb.Uniprot;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class ProteinParser extends CellBaseParser {

    private String species;
    private Path uniprotFilesDir;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public ProteinParser(Path uniprotFilesDir, String species, CellBaseSerializer serializer) {
        super(serializer);

        this.uniprotFilesDir = uniprotFilesDir;
        this.species = species;
    }


    @Override
    public void parse() throws IOException {
        Files.exists(uniprotFilesDir);

        UniprotParser up = new UniprotParser();
//		PrintWriter pw = new PrintWriter(Files.newOutputStream(Paths.get(outputFile.toURI())));
        try {
            File[] files = uniprotFilesDir.toFile().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            });

            for (File file : files) {
                Uniprot uniprot = (Uniprot) up.loadXMLInfo(file.toString(), UniprotParser.UNIPROT_CONTEXT_v201311);

                for (Entry entry : uniprot.getEntry()) {
//                    System.out.println(entry.getOrganism().getName().get(0).getValue());
                    String entryOrganism = null;
                    Iterator<OrganismNameType> iter = entry.getOrganism().getName().iterator();
                    while (iter.hasNext()) {
                        entryOrganism = iter.next().getValue();
//                        if(entryOrganism.contains(species)) {
                        if (entryOrganism.equals(species)) {
                            serializer.serialize(entry);
                        }
                    }
                }
            }
//
// for(Entry entry: uniprot.getEntry()) {
////				System.out.println(entry.getOrganism().getName().get(0).getValue());
//				String entryOrganism = null;
//				Iterator<OrganismNameType> iter = entry.getOrganism().getName().iterator();
//				while(iter.hasNext()) {
////					System.out.println(iter.next().getValue());
//					entryOrganism = iter.next().getValue();
//					if(entryOrganism.contains(species)) {
//						pw.println(gson.toJson(entry));
//					}
//				}
//			}

        } catch (JAXBException e) {
            e.printStackTrace();
        } finally {
//			pw.close();
        }

    }
}
