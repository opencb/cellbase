package org.opencb.cellbase.build.transform;

import java.nio.file.Path;
import java.nio.file.Paths;

import parser.GwasParser;
import junit.framework.TestCase;

/** @author lcruz
 *  @version 1.2.3
 *  @since April 28, 2014 */
public class GwasParserTest extends TestCase {
    public void testParseFile() throws Exception {
        Path inputFilePath = Paths.get("/home/lcruz/Escritorio/gwascatalog.txt");
        Path outputFilePath = Paths.get("/home/lcruz/Escritorio/salida.txt");
        Path dbSnpsFilePath = Paths.get(getClass().getClassLoader()
						.getResource("resources/dbsnp").getPath()
						+ "/dbSnp137-00-All.vcf.gz");
        
        GwasParser p = new GwasParser(inputFilePath, outputFilePath, dbSnpsFilePath);
        
        //p.prueba();
        p.parseFile();
    }
}