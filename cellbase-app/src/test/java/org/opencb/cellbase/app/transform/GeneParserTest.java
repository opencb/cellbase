package org.opencb.cellbase.app.transform;

import org.junit.Ignore;
import org.junit.Test;
import org.opencb.cellbase.app.serializers.CellBaseSerializer;
import org.opencb.cellbase.app.serializers.json.JsonParser;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GeneParserTest {

    private static CellBaseSerializer serializer = null;
    private static String JSON_SERIALIZER = "org.opencb.cellbase.core.serializer.DefaultJsonSerializer";

    @Ignore
    @Test
    public void testParse() throws Exception {

        String serializerClass = "json";
        Path outputPath = Paths.get("/tmp/");
        CellBaseSerializer serializer = new JsonParser(outputPath, "gene");

        GeneParser geneParser = new GeneParser(Paths.get("/tmp/homo_sapiens/gene/"),
                Paths.get("/tmp/homo_sapiens/sequence/Homo_sapiens.GRCh37.p13.fa.gz"), serializer);

        geneParser.parse();
    }

}