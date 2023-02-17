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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.variant.avro.Constraint;
import org.opencb.biodata.models.variant.avro.Expression;
import org.opencb.biodata.models.variant.avro.ExpressionCall;
import org.opencb.cellbase.core.api.GeneQuery;
import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.LogicalList;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.GeneManager;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by fjlopez on 08/10/15.
 */
public class GeneMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public GeneMongoDBAdaptorTest() throws IOException {
        super();
    }

    @BeforeEach
    public void setUp() throws Exception {
        clearDB(CELLBASE_DBNAME);

        createDataRelease();
        dataRelease = 1;

        Path path = Paths.get(getClass().getResource("/gene/gene-test.json.gz").toURI());
        loadRunner.load(path, "gene", dataRelease);
        updateDataRelease(dataRelease, "gene", Collections.emptyList());
    }

    @Test
    @Disabled
    public void testQueryId() throws Exception {
//       GeneMongoDBAdaptor  geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor("hsapiens", "GRCh37");
        GeneManager geneManager = cellBaseManagerFactory.getGeneManager(SPECIES, ASSEMBLY);

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("id", "ENSG00000223972");
        paramMap.put("include", "id,name,start,end");
        paramMap.put(AbstractQuery.DATA_RELEASE, String.valueOf(dataRelease));

        GeneQuery geneQuery = new GeneQuery(paramMap);
        geneQuery.setCount(Boolean.TRUE);

        CellBaseDataResult<Gene> cellBaseDataResult = geneManager.search(geneQuery);
        // WARNING: these values below may slightly change from one data version to another
        assertEquals(1, cellBaseDataResult.getNumMatches());
        assertThat(cellBaseDataResult.getResults().stream().map(gene -> gene.getName()).collect(Collectors.toList()),
                CoreMatchers.hasItems("DDX11L1"));

    }

    @Test
    @Disabled
    public void testQuery() throws Exception {
        GeneManager geneManager = cellBaseManagerFactory.getGeneManager(SPECIES, ASSEMBLY);

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("annotation.expression.tissue", "brain");
        paramMap.put("annotation.expression.value", "UP");
        paramMap.put("include", "id,name");
        paramMap.put(AbstractQuery.DATA_RELEASE, String.valueOf(dataRelease));

        GeneQuery geneQuery = new GeneQuery(paramMap);
        geneQuery.setCount(Boolean.TRUE);

        CellBaseDataResult<Gene> cellBaseDataResult = geneManager.search(geneQuery);
        // WARNING: these values below may slightly change from one data version to another
        assertEquals(6, cellBaseDataResult.getNumMatches());
        assertThat(cellBaseDataResult.getResults().stream().map(gene -> gene.getName()).collect(Collectors.toList()),
                CoreMatchers.hasItems("DDX11L1", "OR4F5", "AL627309.2", "RNU6-1100P", "AP006222.1", "RPL23AP24"));
        assertThat(cellBaseDataResult.getResults().stream().map(gene -> gene.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("ENSG00000223972","ENSG00000186092","ENSG00000239906","ENSG00000222623",
                        "ENSG00000228463","ENSG00000236679"));

        // These two genes are UP for synovial membrane - cannot be returned
        assertThat(cellBaseDataResult.getResults().stream().map(gene -> gene.getId()).collect(Collectors.toList()),
                CoreMatchers.not(CoreMatchers.hasItems("ENSG00000187608", "ENSG00000149968")));

//        query = new Query(GeneDBAdaptor.QueryParams.ANNOTATION_EXPRESSION_TISSUE.key(), "synovial");
//        query.put(GeneDBAdaptor.QueryParams.ANNOTATION_EXPRESSION_VALUE.key(), "DOWN");
//        queryOptions = new QueryOptions("include", "id,name,annotation.expression");
//        queryOptions.put("limit", "10");

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
            if (gene.getId().equals("ENSG00000223972")) {
                for (Expression expression : gene.getAnnotation().getExpression()) {
                    if (expression.getFactorValue().equals("subthalamic nucleus")
                            && expression.getExperimentId().equals("E-GEOD-7307")
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


//
//   constraints":[{"source":"gnomAD","method":"pLoF","name":"oe_mis","value":0.81001},
//   {"source":"gnomAD","method":"pLoF","name":"oe_syn","value":0.91766},
//   {"source":"gnomAD","method":"pLoF","name":"oe_lof","value":0.85584}]}},
    // exac_pLI 0.17633
    // exac_oe_lof 0.45091
    @Test
    @Disabled
    public void testConstraints() throws Exception {
        GeneManager geneManager = cellBaseManagerFactory.getGeneManager(SPECIES, ASSEMBLY);

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("constraints", "oe_lof<=0.85585");
        paramMap.put(AbstractQuery.DATA_RELEASE, String.valueOf(dataRelease));
        GeneQuery geneQuery = new GeneQuery(paramMap);
        CellBaseDataResult<Gene> cellBaseDataResult = geneManager.search(geneQuery);
        assertEquals(1, cellBaseDataResult.getNumResults());
        List<Constraint> constraints = cellBaseDataResult.getResults().get(0).getAnnotation().getConstraints();
        assertEquals(5, constraints.size());

        paramMap = new HashMap<>();
        paramMap.put("constraints", "oe_mis>0.8");
        paramMap.put(AbstractQuery.DATA_RELEASE, String.valueOf(dataRelease));
        geneQuery = new GeneQuery(paramMap);
        cellBaseDataResult = geneManager.search(geneQuery);
        assertEquals(1, cellBaseDataResult.getNumResults());

        paramMap = new HashMap<>();
        paramMap.put("constraints", "oe_syn=0.91766");
        paramMap.put(AbstractQuery.DATA_RELEASE, String.valueOf(dataRelease));
        geneQuery = new GeneQuery(paramMap);
        cellBaseDataResult = geneManager.search(geneQuery);
        assertEquals(1, cellBaseDataResult.getNumResults());

        paramMap = new HashMap<>();
        paramMap.put("constraints", " exac_pLI<0.17633");
        paramMap.put(AbstractQuery.DATA_RELEASE, String.valueOf(dataRelease));
        geneQuery = new GeneQuery(paramMap);
        cellBaseDataResult = geneManager.search(geneQuery);
        assertEquals(0, cellBaseDataResult.getNumResults());

        paramMap = new HashMap<>();
        paramMap.put("constraints", "exac_oe_lof>=0.45091");
        paramMap.put(AbstractQuery.DATA_RELEASE, String.valueOf(dataRelease));
        geneQuery = new GeneQuery(paramMap);
        cellBaseDataResult = geneManager.search(geneQuery);
        assertEquals(1, cellBaseDataResult.getNumResults());
    }
}
