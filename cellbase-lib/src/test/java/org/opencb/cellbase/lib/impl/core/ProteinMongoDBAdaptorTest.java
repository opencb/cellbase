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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.formats.protein.uniprot.v202003jaxb.Entry;
import org.opencb.cellbase.core.api.ProteinQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.ProteinManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Created by fjlopez on 14/04/16.
 */
public class ProteinMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public ProteinMongoDBAdaptorTest() throws Exception {
        super();

        setUp();
    }

    @BeforeEach
    public void setUp() throws Exception {
        clearDB(CELLBASE_DBNAME);

        createDataRelease();
        dataRelease = 1;

        Path path = Paths.get(getClass().getResource("/protein/protein.test.json.gz").toURI());
        loadRunner.load(path, "protein", dataRelease);
        updateDataRelease(dataRelease, "protein", Collections.emptyList());
    }

    @Test
    @Disabled
    public void testQuery() throws Exception {
        ProteinManager proteinManager = cellBaseManagerFactory.getProteinManager(SPECIES, ASSEMBLY);
        ProteinQuery query = new ProteinQuery();
        query.setExcludes(new ArrayList<>(Arrays.asList("_id", "_chunkIds")));
        query.setLimit(3);
        query.setIncludes(new ArrayList<>(Arrays.asList("accession", "name")));
        query.setCount(Boolean.TRUE);
        query.setDataRelease(dataRelease);
//        QueryOptions queryOptions = new QueryOptions(QueryOptions.EXCLUDE, new ArrayList<>(Arrays.asList("_id", "_chunkIds")));
//        queryOptions.put(QueryOptions.LIMIT, 3);
//        queryOptions.put(QueryOptions.INCLUDE, "accession,name");
        CellBaseDataResult<Entry> CellBaseDataResult = proteinManager.search(query);
        assertEquals(3, CellBaseDataResult.getResults().size());
        assertEquals(4, CellBaseDataResult.getNumMatches());

        query = new ProteinQuery();
        query.setAccessions(new ArrayList<>(Arrays.asList("B2R8Q1","Q9UKT9")));
        query.setDataRelease(dataRelease);
//        CellBaseDataResult = proteinDBAdaptor.search(new Query(ProteinDBAdaptor.QueryParams.ACCESSION.key(),
//                "B2R8Q1,Q9UKT9"), queryOptions);
        CellBaseDataResult = proteinManager.search(query);
        assertEquals("B2R8Q1", CellBaseDataResult.getResults().get(0).getAccession().get(1));
        assertEquals("Q9UKT9", CellBaseDataResult.getResults().get(1).getAccession().get(0));
//        CellBaseDataResult = proteinDBAdaptor.search(new Query(ProteinDBAdaptor.QueryParams.NAME.key(),
//                "MKS1_HUMAN"), queryOptions);

        query = new ProteinQuery();
        query.setNames(new ArrayList<>(Collections.singletonList("MKS1_HUMAN")));
        query.setDataRelease(dataRelease);
        CellBaseDataResult = proteinManager.search(query);
//        CellBaseDataResult = proteinDBAdaptor.query(new Query(ProteinDBAdaptor.QueryParams.NAME.key(),
//                "MKS1_HUMAN"), queryOptions);
        assertEquals("MKS1_HUMAN", CellBaseDataResult.getResults().get(0).getName().get(0));
    }

}
