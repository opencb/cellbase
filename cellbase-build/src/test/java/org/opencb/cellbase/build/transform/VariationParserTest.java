package org.opencb.cellbase.build.transform;

import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class VariationParserTest {

    @Test
    public void testParse() throws Exception {
        URL resource = VariantEffectParserTest.class.getResource("/variationParser");
        Path inputDir = Paths.get(resource.toURI());
        VariationParser variationParser = new VariationParser(inputDir, null);
        variationParser.parse();
    }
}