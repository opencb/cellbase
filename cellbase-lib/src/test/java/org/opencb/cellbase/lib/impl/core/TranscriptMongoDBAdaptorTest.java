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

import org.bson.Document;

import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.api.core.TranscriptDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.cellbase.core.result.CellBaseDataResult;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by fjlopez on 27/04/16.
 */
public class TranscriptMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {
    public TranscriptMongoDBAdaptorTest() throws Exception {
        super();
        setUp();
    }

    public void setUp() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/transcript/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene");
    }

    @Test
    public void nativeGet() throws Exception {

        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor("hsapiens", "GRCh37");
        Query query = new Query(TranscriptDBAdaptor.QueryParams.REGION.key(), "1:816481-825251");
        CellBaseDataResult CellBaseDataResult = transcriptDBAdaptor.nativeGet(query, new QueryOptions());
        assertEquals(CellBaseDataResult.getNumResults(), 1);
        assertEquals(((Document) CellBaseDataResult.getResults().get(0)).size(), 18);
        assertEquals(((Document) CellBaseDataResult.getResults().get(0)).get("id"), "ENST00000594233");

        query = new Query(TranscriptDBAdaptor.QueryParams.REGION.key(), "1:31851-44817");
        CellBaseDataResult = transcriptDBAdaptor.nativeGet(query, new QueryOptions());
        assertEquals(CellBaseDataResult.getNumResults(), 2);
        assertTrue(transcriptIdEquals(CellBaseDataResult, Arrays.asList("ENST00000417324", "ENST00000461467")));

        query = new Query(TranscriptDBAdaptor.QueryParams.XREFS.key(), "Q9UL59");
        QueryOptions queryOptions = new QueryOptions("include", "id");
        CellBaseDataResult = transcriptDBAdaptor.nativeGet(query, queryOptions);
        assertEquals(CellBaseDataResult.getNumResults(), 2);
        assertEquals(1, ((Document) CellBaseDataResult.getResults().get(0)).size());
        assertEquals(1, ((Document) CellBaseDataResult.getResults().get(1)).size());
        assertTrue(transcriptIdEquals(CellBaseDataResult, Arrays.asList("ENST00000278314", "ENST00000536068")));

        query = new Query(TranscriptDBAdaptor.QueryParams.BIOTYPE.key(), "protein_coding");
        query.put(TranscriptDBAdaptor.QueryParams.XREFS.key(), "BRCA2");
        queryOptions = new QueryOptions("include", "transcripts.id");
        CellBaseDataResult = transcriptDBAdaptor.nativeGet(query, queryOptions);
        assertEquals(3, CellBaseDataResult.getNumMatches());

    }

    private boolean transcriptIdEquals(CellBaseDataResult CellBaseDataResult, List<String> transcriptIdList) {
        Set<String> set1 = (Set) CellBaseDataResult.getResults().stream()
                .map(result -> ((String) ((Document) result).get("id"))).collect(Collectors.toSet());
        Set<String> set2 = new HashSet<>(transcriptIdList);

        return set1.equals(set2);
    }


}