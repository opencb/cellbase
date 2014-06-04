package org.opencb.cellbase.build.transform;

import org.junit.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class CaddParserTest {

    @Test
    public void testParse() throws Exception {
        // setup


        // ejecucion

        Path ipath = Paths.get("/home/antonior/tmp/test_cadd.txt");
        Path opath = Paths.get("/home/antonior/tmp/test_cadd.json");



        CaddParser.parse(ipath,opath );

        // comprobacion

    }
}