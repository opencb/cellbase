package org.opencb.cellbase.build.transform;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * @author Luis Miguel Cruz.
 * @since October 08, 2014 
 */
public class CosmicParserTest {

    @Test
    public void testParse() throws Exception {
        Path inputFilePath = Paths.get("/home/lcruz/Escritorio/CosmicMutantExport_v68.tsv");
        Path outputFolderPath = Paths.get("/home/lcruz/Escritorio/Cosmic/");


        CosmicParser p = new CosmicParser(inputFilePath, outputFolderPath);
        p.parse();
    }
}