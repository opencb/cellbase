package org.opencb.cellbase.build.transform;

import org.junit.Test;
import java.nio.file.Path;
import java.nio.file.Paths;


import static org.junit.Assert.*;

public class CosmicParserTest {

    @Test
    public void testParse() throws Exception {
        // setup


        // ejecucion

        Path ipath = Paths.get("/home/jpflorido/tmp/CosmicMutantExport_v68.tsv");
        Path opath = Paths.get("/home/jpflorido/tmp/outputCosmic.json");



        CosmicParser.parse(ipath,opath);

        // comprobacion

    }
}