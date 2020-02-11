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
;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.core.ClinicalDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.cellbase.core.result.CellBaseDataResult;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Created by fjlopez on 09/12/15.
 */
@Disabled
public class ClinicalLegacyMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public ClinicalLegacyMongoDBAdaptorTest() throws IOException { super(); }

    @Test
    public void testNativeGet() throws Exception {

        ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalLegacyDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions1 = new QueryOptions();

        Query query1 = new Query();
        query1.put("phenotype", "alzheimer");
        queryOptions1.add("limit", 30);
        queryOptions1.add("include", "clinvarSet.referenceClinVarAssertion.clinVarAccession.acc");
        CellBaseDataResult CellBaseDataResult1 = clinicalDBAdaptor.nativeGet(query1, queryOptions1);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(89, CellBaseDataResult1.getNumTotalResults());
        assertEquals(30, CellBaseDataResult1.getNumResults());
        boolean found = false;
        for (Object resultObject : CellBaseDataResult1.getResults()) {
            if (((String) ((Document)((Document) ((Document) ((Document) resultObject).get("clinvarSet"))
                    .get("referenceClinVarAssertion")).get("clinVarAccession")).get("acc")).equals("RCV000019769")) {
                found = true;
                break;
            }
        }
        assertEquals(found, true);

        Query query2 = new Query();
        query2.put("phenotype", "myelofibrosis");
        QueryOptions queryOptions2 = new QueryOptions();
        queryOptions2.add("limit", 30);
        CellBaseDataResult CellBaseDataResult2 = clinicalDBAdaptor.nativeGet(query2, queryOptions2);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(7739, CellBaseDataResult2.getNumTotalResults());
        assertEquals(30, CellBaseDataResult2.getNumResults());

        query2.put("source", "cosmic");
        queryOptions2.put("include", "mutationID");
        CellBaseDataResult CellBaseDataResult3 = clinicalDBAdaptor.nativeGet(query2, queryOptions2);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(7733, CellBaseDataResult3.getNumTotalResults());
        found = false;
        for (Object resultObject : CellBaseDataResult3.getResults()) {
            String mutationID = (String) ((Document) resultObject).get("mutationID");
            if (mutationID != null && mutationID.equals("COSM12600")) {
                found = true;
                break;
            }
        }
        assertEquals(found, true);
//        assertEquals(((Document) CellBaseDataResult3.getResult().get(14)).get("mutationID"), "COSM12600");

        Query query4 = new Query();
        query4.put("region", new Region("2", 170360030, 170362030));
        QueryOptions queryOptions4 = new QueryOptions();
        queryOptions4.add("limit", 30);
        queryOptions4.add("include", "mutationID,clinvarSet.referenceClinVarAssertion.clinVarAccession.acc");
        CellBaseDataResult CellBaseDataResult4 = clinicalDBAdaptor.nativeGet(query4, queryOptions4);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(23, CellBaseDataResult4.getNumTotalResults());
        for (Object resultObject : CellBaseDataResult4.getResults()) {
            String mutationID = (String) ((Document) resultObject).get("mutationID");
            if (mutationID != null && mutationID.equals("COSM4624460")) {
                found = true;
                break;
            }
        }
        assertEquals(found, true);

        found = false;
        for (Object resultObject : CellBaseDataResult4.getResults()) {
            Document clinvarSet = ((Document) ((Document) resultObject).get("clinvarSet"));
            if ((clinvarSet != null) && (((String) ((Document)((Document) clinvarSet.get("referenceClinVarAssertion"))
                    .get("clinVarAccession")).get("acc")).equals("RCV000171500"))) {
                found = true;
                break;
            }
        }
        assertEquals(found, true);

//        assertEquals(((Document) CellBaseDataResult4.getResult().get(4)).get("mutationID"), "COSM4624460");
//        assertEquals(((Document)((Document) ((Document) ((Document) CellBaseDataResult4.getResult().get(1)).get("clinvarSet"))
//                .get("referenceClinVarAssertion")).get("clinVarAccession")).get("acc"), "RCV000171500");

        Query query5 = new Query();
        query5.put("clinvar-significance", "Likely_pathogenic");
        QueryOptions queryOptions5 = new QueryOptions();
        queryOptions5.add("limit", 30);
//        queryOptions5.add("include", "mutationID,clinvarSet.referenceClinVarAssertion.clinVarAccession.acc");
        CellBaseDataResult CellBaseDataResult5 = clinicalDBAdaptor.nativeGet(query5, queryOptions5);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(10048, CellBaseDataResult5.getNumTotalResults());

        Query query6 = new Query();
        query6.put("gene", "APOE");
        QueryOptions queryOptions6 = new QueryOptions();
        queryOptions6.add("limit", 30);
        queryOptions6.put("sort", "chromosome,start");
        CellBaseDataResult CellBaseDataResult6 = clinicalDBAdaptor.nativeGet(query6, queryOptions6);
        // Check sorted output
        int previousStart = -1;
        for (Document document : (List<Document>) CellBaseDataResult6.getResults()) {
            assertTrue(previousStart < document.getInteger("start"));
            System.out.println("document = " + document.get("chromosome") + ", " + document.get("start"));
        }
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(103, CellBaseDataResult6.getNumTotalResults());

        queryOptions6.remove("sort");
        query6.put("source", "clinvar");
        CellBaseDataResult CellBaseDataResult7 = clinicalDBAdaptor.nativeGet(query6, queryOptions6);
        assertEquals(43, CellBaseDataResult7.getNumTotalResults());

        query6.put("source", "cosmic");
        CellBaseDataResult CellBaseDataResult8 = clinicalDBAdaptor.nativeGet(query6, queryOptions6);
        assertEquals(60, CellBaseDataResult8.getNumTotalResults());
        assertEquals(((Document) CellBaseDataResult8.getResults().get(0)).get("geneName"), "APOE");

        Query query7 = new Query();
        query7.put("cosmicId","COSM306824");
        query7.put("source", "cosmic");
        QueryOptions options = new QueryOptions();
        CellBaseDataResult CellBaseDataResult9 = clinicalDBAdaptor.nativeGet(query7, options);
        assertNotNull(CellBaseDataResult9.getResults());
        assertEquals(((Document)CellBaseDataResult9.getResults().get(0)).get("geneName"), "FMN2");

    }


}