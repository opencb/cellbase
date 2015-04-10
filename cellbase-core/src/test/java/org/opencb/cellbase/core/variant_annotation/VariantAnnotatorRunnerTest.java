package org.opencb.cellbase.core.variant_annotation;

import org.junit.Test;
import org.opencb.cellbase.core.client.CellBaseClient;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class VariantAnnotatorRunnerTest {

    @Test
    public void testReadInputFile() throws URISyntaxException {
        VariantAnnotatorRunner variantAnnotatorRunner = new VariantAnnotatorRunner(Paths.get("/tmp/test1.vcf"),
                Paths.get("/tmp/test.json"), new CellBaseClient("wwwdev.ebi.ac.uk", 1, "/hola/adios", "", ""), 4);
//        variantAnnotatorRunner.readInputFile();
    }
}