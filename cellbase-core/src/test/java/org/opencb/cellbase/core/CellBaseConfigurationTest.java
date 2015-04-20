/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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