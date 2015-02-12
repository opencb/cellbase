package org.opencb.cellbase.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CellBaseConfigurationTest {

    CellBaseConfiguration cellBaseConfiguration;

    @Before
    public void setUp() throws Exception {
        cellBaseConfiguration = CellBaseConfiguration.load(CellBaseConfigurationTest.class.getResourceAsStream("/cellBaseProperties_test.json"));
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