package org.opencb.cellbase.build.transform;

import junit.framework.TestCase;

/**
 * Created by lcruz on 26/05/14.
 */
public class GwasParserTest extends TestCase {
    public void testParseFile() throws Exception {
        String inputFilePath = "/home/lcruz/Escritorio/gwascatalog.txt";
        String outputFilePath = "/home/lcruz/Escritorio/salida.txt";
        String dbSnpsFilePath = getClass().getClassLoader()
						.getResource("resources/dbsnp").getPath()
						+ "/dbSnp137-00-All.vcf.gz";
        
        GwasParser p = new GwasParser(inputFilePath, outputFilePath, dbSnpsFilePath);
        
        //p.prueba();
        p.parseFile();
    }
}
