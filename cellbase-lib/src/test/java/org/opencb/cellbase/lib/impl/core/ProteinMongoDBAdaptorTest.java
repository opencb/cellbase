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
import org.opencb.cellbase.core.api.queries.ProteinQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Created by fjlopez on 14/04/16.
 */
public class ProteinMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public ProteinMongoDBAdaptorTest() throws Exception {
        super();
        setUp();
    }

    public void setUp() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/protein/protein.test.json.gz").toURI());
        loadRunner.load(path, "protein");
    }

    @Test
    public void testQuery() throws Exception {
        ProteinMongoDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor("hsapiens", "GRCh37");
        ProteinQuery query = new ProteinQuery();
        query.setExcludes(new ArrayList<>(Arrays.asList("_id", "_chunkIds")));
        query.setLimit(3);
        query.setIncludes(new ArrayList<>(Arrays.asList("accession", "name")));
//        QueryOptions queryOptions = new QueryOptions(QueryOptions.EXCLUDE, new ArrayList<>(Arrays.asList("_id", "_chunkIds")));
//        queryOptions.put(QueryOptions.LIMIT, 3);
//        queryOptions.put(QueryOptions.INCLUDE, "accession,name");
        CellBaseDataResult<Entry> CellBaseDataResult = proteinDBAdaptor.query(query);
        assertEquals(CellBaseDataResult.getResults().size(), 3);
        assertEquals(CellBaseDataResult.getNumMatches(), 4);

        query = new ProteinQuery();
        query.setAccessions(new ArrayList<>(Arrays.asList("B2R8Q1","Q9UKT9")));
//        CellBaseDataResult = proteinDBAdaptor.search(new Query(ProteinDBAdaptor.QueryParams.ACCESSION.key(),
//                "B2R8Q1,Q9UKT9"), queryOptions);
        CellBaseDataResult = proteinDBAdaptor.query(query);
        assertEquals(CellBaseDataResult.getResults().get(0).getAccession().get(1), "B2R8Q1");
        assertEquals(CellBaseDataResult.getResults().get(1).getAccession().get(0), "Q9UKT9");
//        CellBaseDataResult = proteinDBAdaptor.search(new Query(ProteinDBAdaptor.QueryParams.NAME.key(),
//                "MKS1_HUMAN"), queryOptions);

        query = new ProteinQuery();
        query.setNames(new ArrayList<>(Arrays.asList("MKS1_HUMAN")));
        CellBaseDataResult = proteinDBAdaptor.query(query);
//        CellBaseDataResult = proteinDBAdaptor.query(new Query(ProteinDBAdaptor.QueryParams.NAME.key(),
//                "MKS1_HUMAN"), queryOptions);
        assertEquals(CellBaseDataResult.getResults().get(0).getName().get(0), "MKS1_HUMAN");
    }

//    @Test
//    public void nativeGet() throws Exception {
//        ProteinCoreDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor("hsapiens", "GRCh37");
//        QueryOptions queryOptions = new QueryOptions(QueryOptions.LIMIT, 3);
//        CellBaseDataResult<Document> CellBaseDataResult = proteinDBAdaptor.nativeGet(new Query(), queryOptions);
//        assertEquals(CellBaseDataResult.getResults().size(), 3);
//        assertEquals(CellBaseDataResult.getNumResults(), 3);
//        assertEquals(CellBaseDataResult.getNumMatches(), 4);
//        CellBaseDataResult = proteinDBAdaptor.nativeGet(new Query(ProteinDBAdaptor.QueryParams.ACCESSION.key(),
//                "B2R8Q1,Q9UKT9"), queryOptions);
//        assertEquals(((List) CellBaseDataResult.getResults().get(0).get("accession")).get(0), "Q9UL59");
//        assertEquals(((List) CellBaseDataResult.getResults().get(0).get("accession")).get(1), "B2R8Q1");
//        assertEquals(((List) CellBaseDataResult.getResults().get(1).get("accession")).get(0), "Q9UKT9");
//        CellBaseDataResult = proteinDBAdaptor.nativeGet(new Query(ProteinDBAdaptor.QueryParams.NAME.key(),
//                "MKS1_HUMAN"), queryOptions);
//        assertEquals(((List) CellBaseDataResult.getResults().get(0).get("name")).get(0), "MKS1_HUMAN");
//    }

}