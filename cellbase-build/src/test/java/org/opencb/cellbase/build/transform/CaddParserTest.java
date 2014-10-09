package org.opencb.cellbase.build.transform;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * @author Luis Miguel Cruz.
 * @since October 08, 2014 
 */
public class CaddParserTest {

    @Test
    public void testParse() throws Exception {
    	String chrName = "X";
        Path inputFilePath = Paths.get("/home/lcruz/Escritorio/whole_genome_SNVs_inclAnno_test_file.tsv.gz");
        Path outputFilePath = Paths.get("/home/lcruz/Escritorio/test_cadd.json");


        CaddParser p = new CaddParser(inputFilePath, outputFilePath);
        p.parse(chrName);
    }
}