package org.opencb.cellbase.core.properties;

import org.junit.Before;
import org.junit.Test;
import org.opencb.cellbase.core.CellBaseConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class CellBaseConfigurationTest {

    CellBaseConfiguration cellBaseConfiguration;

    @Before
    public void setUp() throws Exception {
        Path cellbasePropertiesJsonFile = Paths.get(CellBaseConfigurationTest.class.getResource("/cellBaseProperties_test.json").toURI());
        cellBaseConfiguration = CellBaseConfiguration.load(cellbasePropertiesJsonFile);
    }

    @Test
    public void defaultOutdir() {
       assertEquals("/tmp", cellBaseConfiguration.getDefaultOutdir());
    }

    @Test
    public void vertebrates() {
        assertEquals(3, cellBaseConfiguration.getSpecies().getVertebrates().size());
    }

    @Test
    public void metazoa() {
        assertEquals(1, cellBaseConfiguration.getSpecies().getMetazoa().size());
    }

    @Test
    public void fungi() {
        assertEquals(1, cellBaseConfiguration.getSpecies().getFungi().size());
    }

    @Test
    public void plants() {
        assertEquals(2, cellBaseConfiguration.getSpecies().getPlants().size());
    }

}