package org.opencb.cellbase.app.transform;

import org.junit.Test;
import org.mortbay.util.ajax.JSON;
import org.opencb.biodata.models.core.OntologyTerm;
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 06/10/17.
 */
public class OntologiesParserTest extends GenericParserTest<OntologyTerm> {

    public OntologiesParserTest() {
        super(OntologyTerm.class);
    }

    @Test
    public void parse() throws Exception {
        Path ontologiesFilesDir = Paths.get(getClass().getResource("/ontologies").getPath());
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "ontologies.test");
        (new OntologiesParser(ontologiesFilesDir, serializer)).parse();
        serializer.close();
        assertEquals(loadOntologyTermSet(Paths.get(getClass().getResource("/ontologies/ontologies.test.json.gz").getFile())),
                loadOntologyTermSet(Paths.get("/tmp/ontologies.test.json.gz")));
    }

    private Set<OntologyTerm> loadOntologyTermSet(Path path) throws IOException {
        Set<OntologyTerm> ontologyTermSet = new HashSet<>(16);

        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(path)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                ontologyTermSet.add(jsonObjectMapper.convertValue(JSON.parse(line), OntologyTerm.class));
                line = bufferedReader.readLine();
            }
        }

        return ontologyTermSet;
    }


}