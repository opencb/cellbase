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
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.api.TranscriptQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.TranscriptManager;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by fjlopez on 27/04/16.
 */
public class TranscriptMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public TranscriptMongoDBAdaptorTest() {
        super();
    }

    @Test
    public void testQuery() throws Exception {
        TranscriptManager transcriptManager = cellBaseManagerFactory.getTranscriptManager(SPECIES, ASSEMBLY);
        TranscriptQuery query = new TranscriptQuery();
        Region region = Region.parseRegion("19:44905791-44906393");
        query.setRegions(new ArrayList<>(Arrays.asList(region)));
        query.setCount(Boolean.TRUE);
        query.setDataRelease(dataRelease.getRelease());
        CellBaseDataResult<Transcript> cellBaseDataResult = transcriptManager.search(query);

        assertEquals(5, cellBaseDataResult.getNumResults());
        Transcript transcript = cellBaseDataResult.getResults().get(0);
        assertTrue(transcript.getId().startsWith("ENST00000446996"));

        region = Region.parseRegion("19:44905791-44907393");
        query = new TranscriptQuery();
        query.setCount(Boolean.TRUE);
        query.setRegions(Collections.singletonList(region));
        query.setDataRelease(dataRelease.getRelease());
        cellBaseDataResult = transcriptManager.search(query);
        assertEquals(5, cellBaseDataResult.getNumResults());
        assertTrue(transcriptIdEquals(cellBaseDataResult, Arrays.asList("ENST00000446996", "ENST00000485628", "ENST00000252486",
                "ENST00000434152", "ENST00000425718")));

        query = new TranscriptQuery();
        query.setTranscriptsXrefs(Collections.singletonList("A0A087WSZ2"));
        query.setCount(Boolean.TRUE);
        query.setIncludes(Collections.singletonList("id"));
        query.setDataRelease(dataRelease.getRelease());
        cellBaseDataResult = transcriptManager.search(query);
        assertEquals(1, cellBaseDataResult.getNumResults());

        assertEquals("ENST00000502692", cellBaseDataResult.getResults().get(0).getId().split("\\.")[0]);

        query = new TranscriptQuery();
        query.setCount(Boolean.TRUE);
        query.setTranscriptsBiotype(Collections.singletonList("protein_coding"));
        query.setTranscriptsXrefs(Collections.singletonList("BRCA1"));
        query.setIncludes(Collections.singletonList("transcripts.id"));
        query.setDataRelease(dataRelease.getRelease());
        cellBaseDataResult = transcriptManager.search(query);
        assertEquals(27, cellBaseDataResult.getNumResults());
    }

    private boolean transcriptIdEquals(CellBaseDataResult cellBaseDataResult, List<String> transcriptIdList) {
        Set<String> set1 = (Set) cellBaseDataResult.getResults().stream()
                .map(result -> (((Transcript) result).getId().split("\\.")[0])).collect(Collectors.toSet());
        Set<String> set2 = new HashSet<>(transcriptIdList);

        return set1.equals(set2);
    }


}