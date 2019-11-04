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

package org.opencb.cellbase.lib.impl;

import org.hamcrest.CoreMatchers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.variant.avro.Expression;
import org.opencb.biodata.models.variant.avro.ExpressionCall;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.cellbase.core.result.CellBaseDataResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by fjlopez on 08/10/15.
 */
public class GeneMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public GeneMongoDBAdaptorTest() throws IOException { super(); }

    @BeforeEach
    public void setUp() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene");
    }

// The functionality below is no longer available
//    @Test
//    public void testGetStatsById() throws Exception {
//        GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor("hsapiens", "GRCh37");
//        // TODO: enable when new adaptors are implemented
//        CellBaseDataResult CellBaseDataResult = geneDBAdaptor.getStatsById("BRCA2", new QueryOptions());
//        Map resultDBObject = (Map) CellBaseDataResult.getResults().get(0);
//        assertEquals((String)resultDBObject.get("chromosome"),"13");
//        assertEquals((String)resultDBObject.get("name"),"BRCA2");
//        assertTrue((Integer)resultDBObject.get("start") == 32973805);
//        assertTrue((Integer) resultDBObject.get("length") == 84195);
//        assertTrue((Integer) resultDBObject.get("length") == 84195);
//        assertEquals((String)resultDBObject.get("id"),"ENSG00000139618");
//        Map clinicalDBObject = (Map)resultDBObject.get("clinicalVariantStats");
//        Map soSummaryDBObject = (Map)clinicalDBObject.get("soSummary");
//        assertTrue((Integer)((Map) soSummaryDBObject.get("SO:0001583")).get("count")==3121);
//        assertTrue((Integer)((Map) soSummaryDBObject.get("SO:0001822")).get("count")==72);
//        assertTrue((Integer)((Map) soSummaryDBObject.get("SO:0001627")).get("count")==226);
//        Map clinicalSignificanceDBObject = (Map)clinicalDBObject.get("clinicalSignificanceSummary");
//        assertTrue((Integer)clinicalSignificanceDBObject.get("Benign")==362);
//        assertTrue((Integer) clinicalSignificanceDBObject.get("Likely pathogenic")==35);
//        assertTrue((Integer) clinicalSignificanceDBObject.get("conflicting data from submitters")==279);
//
//    }

    @Test
    public void get() throws Exception {
        GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor("hsapiens", "GRCh37");
        Query query = new Query(GeneDBAdaptor.QueryParams.ANNOTATION_EXPRESSION_TISSUE.key(), "synovial");
        query.put(GeneDBAdaptor.QueryParams.ANNOTATION_EXPRESSION_VALUE.key(), "DOWN");
        QueryOptions queryOptions = new QueryOptions("include", "id,name");
        CellBaseDataResult<Gene> CellBaseDataResult = geneDBAdaptor.get(query, queryOptions);
        // WARNING: these values below may slightly change from one data version to another
        assertEquals(22, CellBaseDataResult.getNumMatches());
        assertThat(CellBaseDataResult.getResults().stream().map(gene -> gene.getName()).collect(Collectors.toList()),
                CoreMatchers.hasItems("BRCA2", "TTN", "MTATP8P1", "PLEKHN1", "HES4", "AGRN",
                        "TNFRSF18", "FAM132A", "UBE2J2", "SCNN1D", "ACAP3", "GLTPD1"));
        assertThat(CellBaseDataResult.getResults().stream().map(gene -> gene.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("ENSG00000237094","ENSG00000240409","ENSG00000177757","ENSG00000228794",
                        "ENSG00000230699","ENSG00000187583","ENSG00000187642","ENSG00000188290","ENSG00000188157",
                        "ENSG00000217801","ENSG00000131591", "ENSG00000184163","ENSG00000160087","ENSG00000162572",
                        "ENSG00000131584","ENSG00000224051","ENSG00000175756","ENSG00000235098","ENSG00000179403",
                        "ENSG00000139618"));

        // These two genes are UP for synovial membrane - cannot be returned
        assertThat(CellBaseDataResult.getResults().stream().map(gene -> gene.getId()).collect(Collectors.toList()),
                CoreMatchers.not(CoreMatchers.hasItems("ENSG00000187608", "ENSG00000149968")));

        query = new Query(GeneDBAdaptor.QueryParams.ANNOTATION_EXPRESSION_TISSUE.key(), "synovial");
        query.put(GeneDBAdaptor.QueryParams.ANNOTATION_EXPRESSION_VALUE.key(), "DOWN");
        queryOptions = new QueryOptions("include", "id,name,annotation.expression");
        queryOptions.put("limit", "10");
        CellBaseDataResult = geneDBAdaptor.get(query, queryOptions);
        boolean found = false;
        for (Gene gene : CellBaseDataResult.getResults()) {
            if (gene.getId().equals("ENSG00000237094")) {
                for (Expression expression : gene.getAnnotation().getExpression()) {
                    if (expression.getFactorValue().equals("synovial membrane")
                            && expression.getExperimentId().equals("E-MTAB-37")
                            && expression.getTechnologyPlatform().equals("A-AFFY-44")
                            && expression.getExpression().equals(ExpressionCall.DOWN)) {
                        found = true;
                        break;
                    }

                }
            }
            if (found) {
                break;
            }
        }
        assertEquals(true, found);
    }
}