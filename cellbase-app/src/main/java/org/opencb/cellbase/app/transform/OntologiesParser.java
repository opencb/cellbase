package org.opencb.cellbase.app.transform;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by fjlopez on 20/09/17.
 */
public class OntologiesParser extends CellBaseParser {
    private final Path filesDir;

    public OntologiesParser(Path filesDir, CellBaseFileSerializer serializer) {
        super(serializer);
        this.filesDir = filesDir;
    }

    @Override
    public void parse() throws Exception {
        logger.info("Parsing ontologies...");
        if (Files.exists(filesDir.resolve(EtlCommons.GO_FILE))) {
            parseGoFile(filesDir.resolve(EtlCommons.GO_FILE));
        } else {
            logger.warn("No Gene Ontology file found {}", EtlCommons.GO_FILE);
            logger.warn("Skipping Gene Ontology file parsing. Gene Ontology data models will not be built.");
        }

    }

    private void parseGoFile(Path filePath) throws FileNotFoundException {
        File f;
        FileReader fr;
        f = new File(filePath.toString());
        fr = new FileReader(f);
        logger.info("Creating ontology model...");
        Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        logger.info("Parsing file into ontology model");
        model.read(fr, null);

        NodeIterator nodeIterator = model.listObjects();
        RDFNode rdfNode = nodeIterator.next();
        logger.info(rdfNode.asResource().getLocalName());
//        logger.info(rdfNode.asResource().getId().toString());
//        logger.info(rdfNode.asResource().getNameSpace());
        logger.info(rdfNode.asResource().getProperty(model.createProperty("id")).getString());


//        NodeIterator nodeIterator = model.listObjects();

//        while (iter.hasNext()) {
//            statement = iter.next();
//            int a = 1;
//        }

    }
}
