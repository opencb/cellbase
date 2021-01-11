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
import org.opencb.cellbase.core.api.queries.OntologyQuery;
import org.opencb.cellbase.core.api.queries.QueryException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OntologyQueryTest {

    private OntologyQuery query;
    private Map<String, String> paramMap;

    @BeforeEach
    public void beforeEach() {
        query = new OntologyQuery();
        paramMap = new HashMap<>();
    }

    @Test
    public void testOnto() throws QueryException {
        paramMap.put("id", "GO:1234");
        paramMap.put("name", "go term name");

        paramMap.put("namespace", "my namespace");
        paramMap.put("synonyms", "syn1,synonym 2");
        paramMap.put("xrefs", "xref1,xref2");
        paramMap.put("parents", "parent1,parent2");
        paramMap.put("children", "child1,child2");

        query = new OntologyQuery(paramMap);
        assertEquals("GO:1234", query.getIds().get(0));

        assertEquals("go term name", query.getNames().get(0));

        assertEquals("my namespace", query.getNamespaces().get(0));

        assertEquals("syn1", query.getSynonyms().get(0));
        assertEquals("synonym 2", query.getSynonyms().get(1));

        assertEquals("xref1", query.getXrefs().get(0));
        assertEquals("xref2", query.getXrefs().get(1));

        assertEquals("parent1", query.getParents().get(0));
        assertEquals("parent2", query.getParents().get(1));
        assertEquals("child1", query.getChildren().get(0));
        assertEquals("child2", query.getChildren().get(1));
    }
}
