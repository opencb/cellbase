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

package org.opencb.cellbase.lib.impl.core;

import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.pharma.PharmaChemical;
import org.opencb.cellbase.core.api.PharmaChemicalQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.PharmacogenomicsManager;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opencb.cellbase.core.ParamConstants.DATA_RELEASE_PARAM;

/**
 * Created by jtarraga on 08/21/23.
 */
public class PharmacogenomicsMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public PharmacogenomicsMongoDBAdaptorTest() throws IOException {
        super();
    }

    @Test
    public void testQueryName() throws Exception {
        PharmacogenomicsManager pharmacogenomicsManager = cellBaseManagerFactory.getPharmacogenomicsManager(SPECIES, ASSEMBLY);

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("name", "galantamine");
        paramMap.put("include", "id,name");
        paramMap.put(DATA_RELEASE_PARAM, String.valueOf(dataRelease.getRelease()));

        PharmaChemicalQuery chemicalQuery = new PharmaChemicalQuery(paramMap);
        chemicalQuery.setCount(Boolean.TRUE);

        CellBaseDataResult<PharmaChemical> cellBaseDataResult = pharmacogenomicsManager.search(chemicalQuery);

        assertEquals(1, cellBaseDataResult.getNumMatches());
        assertEquals("PA449726", cellBaseDataResult.first().getId());
    }

    @Test
    public void testQuery() throws Exception {
        PharmacogenomicsManager pharmacogenomicsManager = cellBaseManagerFactory.getPharmacogenomicsManager(SPECIES, ASSEMBLY);

        PharmaChemicalQuery chemicalQuery = new PharmaChemicalQuery();
        chemicalQuery.setGeneNames(Collections.singletonList("PRKCE"));
        chemicalQuery.setDataRelease(dataRelease.getRelease());
        chemicalQuery.setCount(Boolean.TRUE);

        CellBaseDataResult<PharmaChemical> cellBaseDataResult = pharmacogenomicsManager.search(chemicalQuery);

        assertEquals(6, cellBaseDataResult.getNumMatches());
    }
}
