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
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.api.TranscriptQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.TranscriptManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by fjlopez on 27/04/16.
 */
public class TranscriptMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public TranscriptMongoDBAdaptorTest() throws Exception {
        super();
    }

    @BeforeEach
    public void setUp() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass().getResource("/transcript/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene");
    }

    @Test
    public void testQuery() throws Exception {
//        TranscriptMongoDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor("hsapiens", "GRCh37");
        TranscriptManager transcriptManager = cellBaseManagerFactory.getTranscriptManager("hsapiens", "GRCh38");
//        Query query = new Query(TranscriptDBAdaptor.QueryParams.REGION.key(), "1:816481-825251");
        TranscriptQuery query = new TranscriptQuery();
        Region region = Region.parseRegion("1:816481-825251");
        query.setRegions(new ArrayList<>(Arrays.asList(region)));
        query.setCount(Boolean.TRUE);
        CellBaseDataResult<Transcript> cellBaseDataResult = transcriptManager.search(query);

        assertEquals(1, cellBaseDataResult.getNumResults());
        //assertEquals(((Document) CellBaseDataResult.getResults().get(0)).size(), 18);
        Transcript transcript = cellBaseDataResult.getResults().get(0);
        assertEquals("ENST00000594233", transcript.getId());
//        assertEquals(((Document) CellBaseDataResult.getResults().get(0)).get("id"), "ENST00000594233");

//        query = new Query(TranscriptDBAdaptor.QueryParams.REGION.key(), "1:31851-44817");
        region = Region.parseRegion("1:31851-44817");
        query = new TranscriptQuery();
        query.setCount(Boolean.TRUE);
        query.setRegions(Collections.singletonList(region));
        cellBaseDataResult = transcriptManager.search(query);
        assertEquals(2, cellBaseDataResult.getNumResults());
        assertTrue(transcriptIdEquals(cellBaseDataResult, Arrays.asList("ENST00000417324", "ENST00000461467")));

//        query = new Query(TranscriptDBAdaptor.QueryParams.XREFS.key(), "Q9UL59");
        query = new TranscriptQuery();
        query.setTranscriptsXrefs(Collections.singletonList("Q9UL59"));
        query.setCount(Boolean.TRUE);
        query.setIncludes(Collections.singletonList("id"));
        cellBaseDataResult = transcriptManager.search(query);
        assertEquals(2, cellBaseDataResult.getNumResults());
//        assertEquals(1, ((Document) cellBaseDataResult.getResults().get(0)).size());
//        assertEquals(1, ((Document) cellBaseDataResult.getResults().get(1)).size());

        assertTrue(transcriptIdEquals(cellBaseDataResult, Arrays.asList("ENST00000278314", "ENST00000536068")));

//        query = new Query(TranscriptDBAdaptor.QueryParams.BIOTYPE.key(), "protein_coding");
        query = new TranscriptQuery();
        query.setCount(Boolean.TRUE);
        query.setTranscriptsBiotype(Collections.singletonList("protein_coding"));
        query.setTranscriptsXrefs(Collections.singletonList("BRCA2"));
//        query.put(TranscriptDBAdaptor.QueryParams.XREFS.key(), "BRCA2");
//        queryOptions = new QueryOptions("include", "transcripts.id");
        query.setIncludes(Collections.singletonList("transcripts.id"));
        cellBaseDataResult = transcriptManager.search(query);
        assertEquals(3, cellBaseDataResult.getNumResults());

    }

    private boolean transcriptIdEquals(CellBaseDataResult cellBaseDataResult, List<String> transcriptIdList) {
        Set<String> set1 = (Set) cellBaseDataResult.getResults().stream()
                .map(result -> ((String) ((Transcript) result).getId())).collect(Collectors.toSet());
        Set<String> set2 = new HashSet<>(transcriptIdList);

        return set1.equals(set2);
    }


}