package org.opencb.cellbase.core.properties;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.opencb.cellbase.core.properties.PropertiesReader.readCellBaseProperties;

public class PropertiesReaderTest {

    CellBaseProperties cellBaseProperties;

    @Before
    public void setUp() throws Exception {
        Path cellbasePropertiesJsonFile = Paths.get(PropertiesReaderTest.class.getResource("/cellBaseProperties_test.json").toURI());
        cellBaseProperties = readCellBaseProperties(cellbasePropertiesJsonFile);
    }

    @Test
    public void defaultOutdir() {
       assertEquals("/tmp", cellBaseProperties.getDefaultOutdir());
    }

    @Test
    public void vertebrates() {
        assertEquals(3, cellBaseProperties.getSpecies().getVertebrates().size());
    }

    @Test
    public void metazoa() {
        assertEquals(1, cellBaseProperties.getSpecies().getMetazoa().size());
    }

    @Test
    public void fungi() {
        assertEquals(1, cellBaseProperties.getSpecies().getFungi().size());
    }

    @Test
    public void plants() {
        assertEquals(2, cellBaseProperties.getSpecies().getPlants().size());
    }

}