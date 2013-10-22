package org.opencb.cellbase.build.transform;

import org.opencb.commons.bioformats.protein.uniprot.UniprotParser;
import org.opencb.commons.bioformats.protein.uniprot.v140jaxb.Uniprot;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProteinParser {

	public static void parseUniprotToJson(File uniprotFile, String species, File outputFile) throws IOException {
		Path uniprotPath = Paths.get(uniprotFile.toURI());
		Files.exists(uniprotPath);

//		Gson gson = new Gson();

		UniprotParser up = new UniprotParser();
		PrintWriter pw = new PrintWriter(Files.newOutputStream(Paths.get(outputFile.toURI())));
		try {
			Uniprot uniprot = (Uniprot) up.loadXMLInfo(uniprotPath.toString(), UniprotParser.UNIPROT_CONTEXT_v135);
//			for(Entry entry: uniprot.getEntry()) {
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
			pw.close();
		}
		
	}
}
