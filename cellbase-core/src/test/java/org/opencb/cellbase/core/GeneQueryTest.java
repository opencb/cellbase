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

import org.junit.Test;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.commons.datastore.core.ObjectMap;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class GeneQueryTest {

    @Test
    public void testQuery() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("ids", "1");
        GeneQuery geneQuery = new GeneQuery();
        geneQuery.updateParams(paramMap);
        assertEquals("1", geneQuery.getIds().get(0));

        geneQuery.updateParams(new ObjectMap("ids", "a,b,c"));
        assertEquals("a", geneQuery.getIds().get(0));
        assertEquals("b", geneQuery.getIds().get(1));
        assertEquals("c", geneQuery.getIds().get(2));

        geneQuery.updateParams(new ObjectMap("annotationDrugsGene", "x,y"));
        assertEquals("x", geneQuery.getAnnotationDrugsGene().get(0));
        assertEquals("y", geneQuery.getAnnotationDrugsGene().get(1));
    }

    @Test
    public void testRegions() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("regions", "1:6635137-6635325,1:7777777-8888888");
        GeneQuery geneQuery = new GeneQuery();
        geneQuery.updateParams(paramMap);
        Region region = new Region("1:6635137-6635325");
        Region region1 = new Region("1:7777777-8888888");
        assertEquals(region, geneQuery.getRegions().get(0));
        assertEquals(region1, geneQuery.getRegions().get(1));
    }

    @Test
    public void testLimit() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("limit", "1");
        GeneQuery geneQuery = new GeneQuery();
        geneQuery.updateParams(paramMap);
        assertEquals(Integer.valueOf(1), geneQuery.getLimit());

        geneQuery.updateParams(new ObjectMap("limit", 0));
        assertEquals(Integer.valueOf(0), geneQuery.getLimit());

        geneQuery.updateParams(new ObjectMap("limit", -1));
        assertEquals(Integer.valueOf(-1), geneQuery.getLimit());
    }

    @Test
    public void testSkip() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("skip", "1");
        GeneQuery geneQuery = new GeneQuery();
        geneQuery.updateParams(paramMap);
        assertEquals(Integer.valueOf(1), geneQuery.getSkip());

        geneQuery.updateParams(new ObjectMap("skip", 0));
        assertEquals(Integer.valueOf(0), geneQuery.getSkip());

        geneQuery.updateParams(new ObjectMap("skip", -1));
        assertEquals(Integer.valueOf(-1), geneQuery.getSkip());
    }

    @Test
    public void testExcludes() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("excludes", "_id,_chunkId");
        GeneQuery geneQuery = new GeneQuery();
        geneQuery.updateParams(paramMap);
        assertEquals("_id", geneQuery.getExcludes().get(0));
        assertEquals("_chunkId", geneQuery.getExcludes().get(1));
    }

    @Test
    public void testCount() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("count", true);
        GeneQuery geneQuery = new GeneQuery();
        geneQuery.updateParams(paramMap);
        assertTrue(geneQuery.getCount());

        geneQuery.updateParams(new ObjectMap("count", Boolean.FALSE));
        assertFalse(geneQuery.getCount());
    }
}