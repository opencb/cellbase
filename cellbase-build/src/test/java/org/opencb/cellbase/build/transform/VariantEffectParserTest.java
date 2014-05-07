package org.opencb.cellbase.build.transform;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencb.biodata.models.variant.effect.VariantEffect;
import org.opencb.cellbase.build.transform.serializers.json.JsonSerializer;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class VariantEffectParserTest {
    
    private static Path file;
    private static JsonSerializer<VariantEffect> serializer;
    
    @BeforeClass
    public static void setUpClass() throws URISyntaxException {
        URL resource = VariantEffectParserTest.class.getResource("/vep-example-output.txt");
        file = Paths.get(resource.toURI());
        
        serializer = new JsonSerializer<>(Paths.get("/tmp"), Paths.get("vep-example-output"));
        serializer.open();
        serializer.pre();
    }
    
    @AfterClass
    public static void tearDownClass() {
        serializer.post();
        serializer.close();
    }

    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        VariantEffectParser instance = new VariantEffectParser(serializer);
        int numEffectsWritten = instance.parse(file);
        Assert.assertEquals(3, numEffectsWritten);
    }
    
}
