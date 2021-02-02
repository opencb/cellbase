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
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.loader.LoadRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SpeciesUtilsTest {

    private static final String LOCALHOST = "localhost:27017";
    protected static final String GRCH37_DBNAME = "cellbase_hsapiens_grch37_v4";
    private static final String MONGODB_CELLBASE_LOADER = "org.opencb.cellbase.lib.loader.MongoDBCellBaseLoader";
    protected CellBaseConfiguration cellBaseConfiguration;

    protected final LoadRunner loadRunner;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public SpeciesUtilsTest() throws IOException {
        cellBaseConfiguration = CellBaseConfiguration.load(
                SpeciesUtilsTest.class.getClassLoader().getResourceAsStream("configuration.test.yaml"),
                CellBaseConfiguration.ConfigurationFileFormat.YAML);
        loadRunner = new LoadRunner(MONGODB_CELLBASE_LOADER, GRCH37_DBNAME, 2, cellBaseConfiguration);
    }

    @Test
    public void testGetSpeciesShortname() {
        SpeciesConfiguration speciesConfiguration = new SpeciesConfiguration();
        speciesConfiguration.setScientificName("H. sapiens");
        String shortName = SpeciesUtils.getSpeciesShortname(speciesConfiguration);
        assertEquals("h_sapiens", shortName);

        List<SpeciesConfiguration.Assembly> assemblies = new ArrayList();
        SpeciesConfiguration.Assembly assembly = new SpeciesConfiguration.Assembly();
        assembly.setName("grch37");
        assemblies.add(assembly);
        speciesConfiguration.setAssemblies(assemblies);
        shortName = SpeciesUtils.getSpeciesShortname(speciesConfiguration);
        assertEquals("h_sapiens", shortName);
    }

    @Test
    public void testGetDefaultAssembly() {
        SpeciesConfiguration.Assembly assembly = null;
        try {
            assembly = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, "hsapiens");
        } catch (CellbaseException e) {
            e.printStackTrace();
        }
        assertEquals("GRCh37", assembly.getName());

        SpeciesConfiguration speciesConfiguration = new SpeciesConfiguration();
        speciesConfiguration.setScientificName("H. sapiens");
        try {
            assembly = SpeciesUtils.getDefaultAssembly(speciesConfiguration);
        } catch (CellbaseException e) {
            e.printStackTrace();
        }
        assertEquals("GRCh37", assembly.getName());

    }
}
