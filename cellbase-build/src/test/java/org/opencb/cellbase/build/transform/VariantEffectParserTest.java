package org.opencb.cellbase.build.transform;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.lib.mongodb.serializer.MongoDBSerializer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class VariantEffectParserTest {
    
    private static Path file;
//    private static JsonSerializer<VariantEffect> serializer;
    private static CellBaseSerializer serializer;

    @BeforeClass
    public static void setUpClass() throws URISyntaxException, IOException {
        URL resource = VariantEffectParserTest.class.getResource("/vep-example-output.txt");
        file = Paths.get(resource.toURI());

        serializer = new MongoDBSerializer(Paths.get("/tmp/"));
//        serializer = new JsonSerializer<>(Paths.get("/tmp/vep-example-output"));
//        serializer.open();
//        serializer.pre();
    }
    
    @AfterClass
    public static void tearDownClass() {
//        serializer.post();
        serializer.close();
    }

    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        VariantEffectParser instance = new VariantEffectParser(file, serializer);
        instance.parse();
//        Assert.assertEquals(3, numEffectsWritten);
    }
    
}
