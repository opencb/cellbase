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
import org.opencb.biodata.formats.protein.uniprot.v202003jaxb.Entry;
import org.opencb.cellbase.core.api.ProteinQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.ProteinManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Created by fjlopez on 14/04/16.
 */
public class ProteinMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public ProteinMongoDBAdaptorTest() throws Exception {
        super();
    }

    @Test
    public void testQuery() throws Exception {
        ProteinManager proteinManager = cellBaseManagerFactory.getProteinManager(SPECIES, ASSEMBLY);
        ProteinQuery query = new ProteinQuery();
        query.setExcludes(new ArrayList<>(Arrays.asList("_id", "_chunkIds")));
        query.setLimit(3);
        query.setIncludes(new ArrayList<>(Arrays.asList("accession", "name")));
        query.setCount(Boolean.TRUE);
        query.setDataRelease(dataRelease.getRelease());
        CellBaseDataResult<Entry> CellBaseDataResult = proteinManager.search(query);
        assertEquals(3, CellBaseDataResult.getResults().size());
        assertEquals(17, CellBaseDataResult.getNumMatches());

        query = new ProteinQuery();
        query.setAccessions(new ArrayList<>(Arrays.asList("P02649","Q86VF7","Q16535")));
        query.setDataRelease(dataRelease.getRelease());
        CellBaseDataResult = proteinManager.search(query);
        assertTrue(CellBaseDataResult.getResults().get(0).getAccession().contains("P02649"));
        assertTrue(CellBaseDataResult.getResults().get(1).getAccession().contains("Q86VF7"));
        assertTrue(CellBaseDataResult.getResults().get(2).getAccession().contains("Q16535"));

        query = new ProteinQuery();
        query.setNames(new ArrayList<>(Collections.singletonList("FMR1_HUMAN")));
        query.setDataRelease(dataRelease.getRelease());
        CellBaseDataResult = proteinManager.search(query);
        assertTrue(CellBaseDataResult.getResults().get(0).getName().contains("FMR1_HUMAN"));
    }
}
