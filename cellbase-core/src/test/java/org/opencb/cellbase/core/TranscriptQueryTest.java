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

package org.opencb.cellbase.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.api.queries.QueryException;
import org.opencb.cellbase.core.api.queries.TranscriptQuery;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TranscriptQueryTest {

    private TranscriptQuery transcriptQuery;
    private Map<String, String> paramMap;

    @BeforeEach
    public void beforeEach() {
        transcriptQuery = new TranscriptQuery();
        paramMap = new HashMap<>();
    }

    @Test
    public void testQuery() throws QueryException {
        paramMap.put("id", "ENST00000342992");
        paramMap.put("biotype", "myBiotype");
        paramMap.put("xrefs", "a,b,c");

        paramMap.put("name", "transcriptName");

        paramMap.put("annotationFlags", "flag1,flag2");
        paramMap.put("tfbs.name", "tfbs1,tfbs2");

        transcriptQuery = new TranscriptQuery(paramMap);
        assertEquals("ENST00000342992", transcriptQuery.getTranscriptsId().get(0));

        assertEquals("myBiotype", transcriptQuery.getTranscriptsBiotype().get(0));

        assertEquals("a", transcriptQuery.getTranscriptsXrefs().get(0));
        assertEquals("b", transcriptQuery.getTranscriptsXrefs().get(1));
        assertEquals("c", transcriptQuery.getTranscriptsXrefs().get(2));

        assertEquals("transcriptName", transcriptQuery.getTranscriptsName().get(0));

        assertEquals("flag1", transcriptQuery.getTranscriptsAnnotationFlags().get(0));
        assertEquals("flag2", transcriptQuery.getTranscriptsAnnotationFlags().get(1));

        assertEquals("tfbs1", transcriptQuery.getTranscriptsTfbsId().get(0));
        assertEquals("tfbs2", transcriptQuery.getTranscriptsTfbsId().get(1));
    }


}