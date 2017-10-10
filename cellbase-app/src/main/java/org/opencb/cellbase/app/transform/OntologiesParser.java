package org.opencb.cellbase.app.transform;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
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
    private static final String SUBCLASSOF = "subClassOf";
    private static final String IAO_0000115 = "IAO_0000115";
    private static final String HASOBONAMESPACE = "hasOBONamespace";
    private static final String ID = "id";
    private static final String LABEL = "label";
    private static final String HASDBXREF = "hasDbXref";
    private static final String HASEXACTSYNONYM = "hasExactSynonym";
    private static final String HASALTERNATIVEID = "hasAlternativeId";

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
        FileReader fr = new FileReader(new File(filePath.toString()));
        logger.info("Parsing Gene Ontology...");
        logger.info("Creating ontology model...");
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        logger.info("Parsing file into ontology model");
        model.read(fr, null);

        ProgressLogger progressLogger = new ProgressLogger("Parsed terms:", model.size(), 200);
        ExtendedIterator<OntClass> iterator = model.listClasses();
        while (iterator.hasNext()) {
            OntClass ontClass = iterator.next();
            OntologyTerm ontologyTerm = parseTermData(ontClass.asResource());
            serializer.serialize(ontologyTerm);
            progressLogger.increment(1);
        }
    }

    private OntologyTerm parseTermData(Resource resource) {
        OntologyTerm ontologyTerm = new OntologyTerm();
        StmtIterator stmtIterator = resource.listProperties();
        List<String> parentList = new ArrayList<>();
        List<String> dbxrefList = new ArrayList<>();
        List<String> synonymList = new ArrayList<>();
        List<String> alternativeIdList = new ArrayList<>();
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
                case HASOBONAMESPACE:
                    ontologyTerm.setNamespace(statement.getString());
                    break;
                case ID:
                    ontologyTerm.setId(statement.getString());
                    break;
                case LABEL:
                    ontologyTerm.setTerm(statement.getString());
                    break;
                case HASDBXREF:
                    dbxrefList.add(statement.getString());
                    break;
                case HASEXACTSYNONYM:
                    synonymList.add(statement.getString());
                    break;
                case HASALTERNATIVEID:
                    alternativeIdList.add(statement.getString());
                    break;
                default:
                    break;
            }
//            if (statement.getPredicate().getLocalName().equals("subClassOf")) {
//                logger.info("{}: {}", statement.getPredicate().getLocalName(), statement.getResource().getLocalName());
//            } else {
//                logger.info("{}: {}", statement.getPredicate().getLocalName(), statement.getString());
//            }
        }
        ontologyTerm.setSubClassOf(parentList.isEmpty() ? null : parentList);
        ontologyTerm.setXrefs(dbxrefList.isEmpty() ? null : dbxrefList);
        ontologyTerm.setSynonyms(synonymList.isEmpty() ? null : synonymList);
        ontologyTerm.setAlternativeIds(alternativeIdList.isEmpty() ? null : alternativeIdList);

        return ontologyTerm;
    }

}
