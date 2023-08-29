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

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.variant.avro.Constraint;
import org.opencb.biodata.models.variant.avro.Expression;
import org.opencb.biodata.models.variant.avro.ExpressionCall;
import org.opencb.cellbase.core.api.GeneQuery;
import org.opencb.cellbase.core.api.query.LogicalList;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.GeneManager;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opencb.cellbase.core.ParamConstants.DATA_RELEASE_PARAM;

/**
 * Created by fjlopez on 08/10/15.
 */
public class GeneMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public GeneMongoDBAdaptorTest() throws IOException {
        super();
    }

    @Test
    public void testQueryId() throws Exception {
        GeneManager geneManager = cellBaseManagerFactory.getGeneManager(SPECIES, ASSEMBLY);

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("id", "ENSG00000248746");
        paramMap.put("include", "id,name,start,end");
        paramMap.put(DATA_RELEASE_PARAM, String.valueOf(dataRelease));

        GeneQuery geneQuery = new GeneQuery(paramMap);
        geneQuery.setCount(Boolean.TRUE);

        CellBaseDataResult<Gene> cellBaseDataResult = geneManager.search(geneQuery);
        // WARNING: these values below may slightly change from one data version to another
        assertEquals(1, cellBaseDataResult.getNumMatches());
        assertThat(cellBaseDataResult.getResults().stream().map(gene -> gene.getName()).collect(Collectors.toList()),
                CoreMatchers.hasItems("ACTN3"));
    }

    @Test
    public void testQuery() throws Exception {
        GeneManager geneManager = cellBaseManagerFactory.getGeneManager(SPECIES, ASSEMBLY);

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("annotation.expression.tissue", "brain");
        paramMap.put("annotation.expression.value", "UP");
        paramMap.put("include", "id,name");
        paramMap.put(DATA_RELEASE_PARAM, String.valueOf(dataRelease));

        GeneQuery geneQuery = new GeneQuery(paramMap);
        geneQuery.setCount(Boolean.TRUE);

        CellBaseDataResult<Gene> cellBaseDataResult = geneManager.search(geneQuery);
        // WARNING: these values below may slightly change from one data version to another
        assertEquals(12, cellBaseDataResult.getNumMatches());
        assertThat(cellBaseDataResult.getResults().stream().map(gene -> gene.getName()).collect(Collectors.toList()),
                CoreMatchers.hasItems("APOE", "BRCA1", "CFTR", "CYP2D6", "DMD", "EGFR", "FMR1"));
        assertThat(cellBaseDataResult.getResults().stream().map(gene -> gene.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("ENSG00000130203","ENSG00000012048","ENSG00000001626","ENSG00000100197",
                        "ENSG00000198947","ENSG00000146648"));

        geneQuery = new GeneQuery();
        geneQuery.setAnnotationExpressionTissue(new LogicalList(Collections.singletonList("brain")));
        geneQuery.setAnnotationExpressionValue(new LogicalList(Collections.singletonList("UP")));
        List<String> includes = new ArrayList<>();
        includes.add("id");
        includes.add("name");
        includes.add("annotation.expression");
        geneQuery.setIncludes(includes);
        geneQuery.setLimit(10);
        geneQuery.setCount(Boolean.TRUE);
        geneQuery.setDataRelease(dataRelease);
        cellBaseDataResult = geneManager.search(geneQuery);
        boolean found = false;
        for (Gene gene : cellBaseDataResult.getResults()) {
            if (gene.getId().equals("ENSG00000130203")) {
                for (Expression expression : gene.getAnnotation().getExpression()) {
                    if (expression.getFactorValue().equals("placenta")
                            && expression.getExperimentId().equals("E-MTAB-37")
                            && expression.getTechnologyPlatform().equals("A-AFFY-44")
                            && expression.getExpression().equals(ExpressionCall.UP)) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testConstraints() throws Exception {
        GeneManager geneManager = cellBaseManagerFactory.getGeneManager(SPECIES, ASSEMBLY);

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("constraints", "oe_lof<=0.85585");
        paramMap.put(DATA_RELEASE_PARAM, String.valueOf(dataRelease));
        GeneQuery geneQuery = new GeneQuery(paramMap);
        CellBaseDataResult<Gene> cellBaseDataResult = geneManager.search(geneQuery);
        assertEquals(12, cellBaseDataResult.getNumResults());
        List<Constraint> constraints = cellBaseDataResult.getResults().get(0).getAnnotation().getConstraints();
        assertEquals(5, constraints.size());

        paramMap = new HashMap<>();
        paramMap.put("constraints", "oe_mis>0.8");
        paramMap.put(DATA_RELEASE_PARAM, String.valueOf(dataRelease));
        geneQuery = new GeneQuery(paramMap);
        cellBaseDataResult = geneManager.search(geneQuery);
        assertEquals(9, cellBaseDataResult.getNumResults());

        paramMap = new HashMap<>();
        paramMap.put("constraints", "oe_syn=0.91766");
        paramMap.put(DATA_RELEASE_PARAM, String.valueOf(dataRelease));
        geneQuery = new GeneQuery(paramMap);
        cellBaseDataResult = geneManager.search(geneQuery);
        assertEquals(0, cellBaseDataResult.getNumResults());

        paramMap = new HashMap<>();
        paramMap.put("constraints", " exac_pLI<0.17633");
        paramMap.put(DATA_RELEASE_PARAM, String.valueOf(dataRelease));
        geneQuery = new GeneQuery(paramMap);
        cellBaseDataResult = geneManager.search(geneQuery);
        assertEquals(0, cellBaseDataResult.getNumResults());

        paramMap = new HashMap<>();
        paramMap.put("constraints", "exac_oe_lof>=0.45091");
        paramMap.put(DATA_RELEASE_PARAM, String.valueOf(dataRelease));
        geneQuery = new GeneQuery(paramMap);
        cellBaseDataResult = geneManager.search(geneQuery);
        assertEquals(7, cellBaseDataResult.getNumResults());
    }
}
