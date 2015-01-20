package org.opencb.cellbase.build.transform;

import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.*;

public class VariationParserTest {

    @Test
    public void testParse() throws Exception {

        VariationParser variationParser = new VariationParser(Paths.get("/tmp/homo_sapiens/variation/"), null);

        variationParser.parse();

    }
}