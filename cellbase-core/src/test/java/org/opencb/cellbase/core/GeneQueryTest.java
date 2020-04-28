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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.api.queries.LogicalList;
import org.opencb.cellbase.core.api.queries.QueryException;
import org.opencb.commons.datastore.core.ObjectMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class GeneQueryTest {

    private GeneQuery geneQuery;
    private Map<String, String> paramMap;

    @BeforeEach
    public void beforeEach() {
        geneQuery = new GeneQuery();
        paramMap = new HashMap<>();
    }

    @Test
    public void testQuery() throws QueryException {
        paramMap.put("id", "geneId123");
        paramMap.put("biotype", "a,b,c");

        paramMap.put("annotation.drugs.gene", "x,y");

        paramMap.put("annotation.expression.tissue", "subthalamic nucleus");
        paramMap.put("annotation.expression.value", "UP");

        geneQuery = new GeneQuery(paramMap);
        assertEquals("geneId123", geneQuery.getIds().get(0));

        assertEquals("a", geneQuery.getBiotypes().get(0));
        assertEquals("b", geneQuery.getBiotypes().get(1));
        assertEquals("c", geneQuery.getBiotypes().get(2));

        assertEquals("x", geneQuery.getAnnotationDrugsGene().get(0));
        assertEquals("y", geneQuery.getAnnotationDrugsGene().get(1));

        assertEquals("subthalamic nucleus", geneQuery.getAnnotationExpressionTissue().get(0));
        assertEquals("UP", geneQuery.getAnnotationExpressionValue().get(0));
    }



    @Test
    public void testFacets() throws QueryException {
        paramMap.put("facet", "biotype");
        geneQuery = new GeneQuery(paramMap);
        assertEquals("biotype", geneQuery.getFacet());
    }

//    @Test
//    public void testBadParam() throws QueryException {
//        paramMap.put("xyz", "this should throw an exception!");
//
//        Assertions.assertThrows(QueryException.class, () -> {
//            geneQuery = new GeneQuery(paramMap);
//        });
//    }

    @Test
    public void testRegions() throws QueryException {
        paramMap.put("region", "1:6635137-6635325,1:7777777-8888888");
        geneQuery.updateParams(paramMap);
        Region region = new Region("1:6635137-6635325");
        Region region1 = new Region("1:7777777-8888888");
        assertEquals(region, geneQuery.getRegions().get(0));
        assertEquals(region1, geneQuery.getRegions().get(1));
    }

    @Test
    public void testLimit() throws QueryException {
        paramMap.put("limit", "1");
        geneQuery.updateParams(paramMap);
        assertEquals(Integer.valueOf(1), geneQuery.getLimit());

        paramMap.put("limit", "0");
        geneQuery.updateParams(paramMap);
        assertEquals(Integer.valueOf(0), geneQuery.getLimit());

        paramMap.put("limit", "-1");
        geneQuery.updateParams(paramMap);
        assertEquals(Integer.valueOf(-1), geneQuery.getLimit());
    }

    @Test
    public void testSkip() throws QueryException {
        paramMap.put("skip", "1");
        geneQuery.updateParams(paramMap);
        assertEquals(Integer.valueOf(1), geneQuery.getSkip());

        paramMap.put("skip", "0");
        geneQuery.updateParams(paramMap);
        assertEquals(Integer.valueOf(0), geneQuery.getSkip());

        paramMap.put("skip", "-1");
        geneQuery.updateParams(paramMap);
        assertEquals(Integer.valueOf(-1), geneQuery.getSkip());
    }

    @Test
    public void testExcludes() throws QueryException {
        paramMap.put("exclude", "_id,_chunkId");
        geneQuery.updateParams(paramMap);
        assertEquals("_id", geneQuery.getExcludes().get(0));
        assertEquals("_chunkId", geneQuery.getExcludes().get(1));
    }

    @Test
    public void testCount() throws QueryException {
        paramMap.put("count", "true");
        geneQuery.updateParams(paramMap);
        assertTrue(geneQuery.getCount());

        ObjectMap objectMap = geneQuery.toQueryOptions();
        Object count = objectMap.get("count");
        assertTrue((Boolean) count);

        paramMap.put("count", "false");
        geneQuery.updateParams(paramMap);
        assertFalse(geneQuery.getCount());

        objectMap = geneQuery.toQueryOptions();
        count = objectMap.get("count");
        assertFalse((Boolean) count);
    }

    @Test
    public void testValidate() throws QueryException, NoSuchFieldException, IllegalAccessException {
        paramMap.put("id", "1");
        paramMap.put("name", "42");
        paramMap.put("biotype", "a,b,c");
        paramMap.put("annotation.drugs.gene", "x,y");

        paramMap.put("limit", "11");
        paramMap.put("skip", "-1");
        geneQuery = new GeneQuery(paramMap);
        Assertions.assertThrows(QueryException.class, () -> {
            geneQuery.validate();
        });

        paramMap.put("limit", "-123456789");
        paramMap.put("skip", "0");
        geneQuery = new GeneQuery(paramMap);
        Assertions.assertThrows(QueryException.class, () -> {
            geneQuery.validate();
        });

        // legal
        paramMap.put("sort", "-123456789");
        paramMap.put("order", "DESCENDING");
        geneQuery = new GeneQuery(paramMap);

        // illegal
        paramMap.put("order", "DESCENDING");
        geneQuery = new GeneQuery(paramMap);
        Assertions.assertThrows(QueryException.class, () -> {
            geneQuery.validate();
        });
    }

    @Test
    public void testBuild() {
        LogicalList geneList = new LogicalList(Arrays.asList("My gene", "another-gene"));
        geneQuery = new GeneQuery.Builder().withIds(Arrays.asList("1")).withBiotypes(Arrays.asList("a", "b", "c"))
                .withAnnotationDrugsGene(geneList).build();

        assertEquals("1", geneQuery.getIds().get(0));

        assertEquals("a", geneQuery.getBiotypes().get(0));
        assertEquals("b", geneQuery.getBiotypes().get(1));
        assertEquals("c", geneQuery.getBiotypes().get(2));

        assertEquals("My gene", geneQuery.getAnnotationDrugsGene().get(0));
        assertEquals("another-gene", geneQuery.getAnnotationDrugsGene().get(1));
    }
}