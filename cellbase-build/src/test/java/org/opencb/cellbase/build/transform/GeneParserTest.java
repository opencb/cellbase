package org.opencb.cellbase.build.transform;

import org.junit.Test;

import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class GeneParserTest {

    private static CellBaseSerializer serializer = null;
    private static String JSON_SERIALIZER = "org.opencb.cellbase.core.serializer.DefaultJsonSerializer";

    @Test
    public void testParse() throws Exception {

        String serializerClass = "json";
//        Path outputPath = Paths.get("/tmp/");
//        serializer = (CellBaseSerializer) Class.forName(JSON_SERIALIZER).getConstructor(Path.class).newInstance(outputPath);
//
//        GeneParser geneParser = new GeneParser(serializer);
//
//        geneParser.parse(Paths.get("/tmp/test/"), Paths.get("/tmp/homo_sapiens/gene/homo_sapiens.gtf.gz"));
    }
}