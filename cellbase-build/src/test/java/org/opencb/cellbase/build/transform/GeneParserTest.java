package org.opencb.cellbase.build.transform;

import org.junit.Test;
import org.opencb.cellbase.core.serializer.DefaultJsonSerializer;

import java.nio.file.Paths;

import static org.junit.Assert.*;

public class GeneParserTest {

    @Test
    public void testParse() throws Exception {


        GeneParser geneParser = new GeneParser(new DefaultJsonSerializer(Paths.get("/tmp/")));
        geneParser.parse(Paths.get("/tmp/homo_sapiens/gene/"), Paths.get("/tmp/homo_sapiens/sequence/"));

    }
}