package org.opencb.cellbase.app.transform;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.opencb.biodata.models.core.OntologyTerm;
import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.commons.ProgressLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fjlopez on 20/09/17.
 */
public class OntologiesParser extends CellBaseParser {
    private static final char UNDERSCORE = '_';
    private static final char COLON = ':';
    private final Path filesDir;
    private final static String SUBCLASSOF = "subClassOf";
    private final static String IAO_0000115 = "IAO_0000115";

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
        logger.info("Parsing Gene Ontology...");
        logger.info("Creating ontology model...");
        Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        logger.info("Parsing file into ontology model");
        model.read(fr, null);

        ProgressLogger progressLogger = new ProgressLogger("Parsed terms:", model.size(), 200);
        NodeIterator nodeIterator = model.listObjects();
        while (nodeIterator.hasNext()) {
            RDFNode rdfNode = nodeIterator.next();
            OntologyTerm term = parseTermData(rdfNode.asResource());
            serializer.serialize(term);
            progressLogger.increment(1);
        }
    }

    private OntologyTerm parseTermData(Resource resource) {
        OntologyTerm ontologyTerm = new OntologyTerm();
        StmtIterator stmtIterator = resource.listProperties();
        List<String> parentList = new ArrayList<>();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            switch (statement.getPredicate().getLocalName()) {
                case SUBCLASSOF:
                    if (statement.getResource() != null && statement.getResource().getLocalName() != null) {
                        parentList.add(statement.getResource().getLocalName().replace(UNDERSCORE, COLON));
                    }
                    break;
                case IAO_0000115:
                    ontologyTerm.setDefinition(statement.getString());
                    break;

            }
            term.setId();
            if (statement.getPredicate().getLocalName().equals("subClassOf")) {
                logger.info("{}: {}", statement.getPredicate().getLocalName(), statement.getResource().getLocalName());
            } else {
                logger.info("{}: {}", statement.getPredicate().getLocalName(), statement.getString());
            }
        }
        ontologyTerm.setSubClassOf(parentList);

        //        logger.info(rdfNode.asResource().getId().toString());
//        logger.info(rdfNode.asResource().getNameSpace());
//        logger.info(rdfNode.asResource().getProperty(model.createProperty("id")).getString());


//        NodeIterator nodeIterator = model.listObjects();

//        while (iter.hasNext()) {
//            statement = iter.next();
//            int a = 1;
//        }

    }

}
