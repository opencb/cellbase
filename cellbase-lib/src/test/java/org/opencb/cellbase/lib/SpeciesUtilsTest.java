/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib;

import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.utils.SpeciesUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SpeciesUtilsTest extends GenericMongoDBAdaptorTest {

    public SpeciesUtilsTest() throws IOException, CellBaseException {
        super();
    }

    @Test
    public void testGetSpeciesShortname() {
        SpeciesConfiguration speciesConfiguration = new SpeciesConfiguration();
        speciesConfiguration.setScientificName("H. sapiens");
        String shortName = SpeciesUtils.getSpeciesShortname(speciesConfiguration);
        assertEquals("h_sapiens", shortName);

        List<SpeciesConfiguration.Assembly> assemblies = new ArrayList();
        SpeciesConfiguration.Assembly assembly = new SpeciesConfiguration.Assembly();
        assembly.setName(ASSEMBLY);
        assemblies.add(assembly);
        speciesConfiguration.setAssemblies(assemblies);
        shortName = SpeciesUtils.getSpeciesShortname(speciesConfiguration);
        assertEquals("h_sapiens", shortName);
    }

    @Test
    public void testGetDefaultAssembly() {
        SpeciesConfiguration.Assembly assembly = null;
        try {
            assembly = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, SPECIES);
        } catch (CellBaseException e) {
            e.printStackTrace();
        }
        assertEquals(ASSEMBLY.toLowerCase(Locale.ROOT), assembly.getName().toLowerCase(Locale.ROOT));

        SpeciesConfiguration speciesConfiguration = new SpeciesConfiguration();
        speciesConfiguration.setScientificName("H. sapiens");
        try {
            assembly = SpeciesUtils.getDefaultAssembly(speciesConfiguration);
        } catch (CellBaseException e) {
            e.printStackTrace();
        }
        assertEquals(ASSEMBLY.toLowerCase(Locale.ROOT), assembly.getName().toLowerCase(Locale.ROOT));
    }

    @Test
    public void testGetSpeciesConfiguration_Normalization() {
        // Test that various species name formats (scientific name, common name, ID)
        // all return the same SpeciesConfiguration with the correct species ID

        // Test with species ID (lowercase)
        SpeciesConfiguration speciesConfigById = SpeciesUtils.getSpeciesConfiguration(cellBaseConfiguration, "hsapiens");
        assertEquals("hsapiens", speciesConfigById.getId());

        // Test with scientific name (case insensitive)
        SpeciesConfiguration speciesConfigByScientific1 = SpeciesUtils.getSpeciesConfiguration(cellBaseConfiguration, "Homo sapiens");
        assertEquals("hsapiens", speciesConfigByScientific1.getId());

        SpeciesConfiguration speciesConfigByScientific2 = SpeciesUtils.getSpeciesConfiguration(cellBaseConfiguration, "homo sapiens");
        assertEquals("hsapiens", speciesConfigByScientific2.getId());

        SpeciesConfiguration speciesConfigByScientific3 = SpeciesUtils.getSpeciesConfiguration(cellBaseConfiguration, "HOMO SAPIENS");
        assertEquals("hsapiens", speciesConfigByScientific3.getId());

        // Test with URL-encoded space (as it would come from REST API)
        SpeciesConfiguration speciesConfigByScientific4 = SpeciesUtils.getSpeciesConfiguration(cellBaseConfiguration, "Homo Sapiens");
        assertEquals("hsapiens", speciesConfigByScientific4.getId());

        // All should return the same configuration object (same scientific name, etc.)
        assertEquals(speciesConfigById.getScientificName(), speciesConfigByScientific1.getScientificName());
        assertEquals(speciesConfigById.getScientificName(), speciesConfigByScientific2.getScientificName());
        assertEquals(speciesConfigById.getScientificName(), speciesConfigByScientific3.getScientificName());
        assertEquals(speciesConfigById.getScientificName(), speciesConfigByScientific4.getScientificName());
    }
}
