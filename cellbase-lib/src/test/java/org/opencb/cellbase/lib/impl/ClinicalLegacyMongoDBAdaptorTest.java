package org.opencb.cellbase.lib.impl;

import org.bson.Document;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantTraitAssociation;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 09/12/15.
 */
@Ignore
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
        QueryResult queryResult1 = clinicalDBAdaptor.nativeGet(query1, queryOptions1);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(89, queryResult1.getNumTotalResults());
        assertEquals(30, queryResult1.getNumResults());
        boolean found = false;
        for (Object resultObject : queryResult1.getResult()) {
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
        QueryResult queryResult2 = clinicalDBAdaptor.nativeGet(query2, queryOptions2);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(7739, queryResult2.getNumTotalResults());
        assertEquals(30, queryResult2.getNumResults());

        query2.put("source", "cosmic");
        queryOptions2.put("include", "mutationID");
        QueryResult queryResult3 = clinicalDBAdaptor.nativeGet(query2, queryOptions2);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(7733, queryResult3.getNumTotalResults());
        found = false;
        for (Object resultObject : queryResult3.getResult()) {
            String mutationID = (String) ((Document) resultObject).get("mutationID");
            if (mutationID != null && mutationID.equals("COSM12600")) {
                found = true;
                break;
            }
        }
        assertEquals(found, true);
//        assertEquals(((Document) queryResult3.getResult().get(14)).get("mutationID"), "COSM12600");

        Query query4 = new Query();
        query4.put("region", new Region("2", 170360030, 170362030));
        QueryOptions queryOptions4 = new QueryOptions();
        queryOptions4.add("limit", 30);
        queryOptions4.add("include", "mutationID,clinvarSet.referenceClinVarAssertion.clinVarAccession.acc");
        QueryResult queryResult4 = clinicalDBAdaptor.nativeGet(query4, queryOptions4);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(23, queryResult4.getNumTotalResults());
        for (Object resultObject : queryResult4.getResult()) {
            String mutationID = (String) ((Document) resultObject).get("mutationID");
            if (mutationID != null && mutationID.equals("COSM4624460")) {
                found = true;
                break;
            }
        }
        assertEquals(found, true);

        found = false;
        for (Object resultObject : queryResult4.getResult()) {
            Document clinvarSet = ((Document) ((Document) resultObject).get("clinvarSet"));
            if ((clinvarSet != null) && (((String) ((Document)((Document) clinvarSet.get("referenceClinVarAssertion"))
                    .get("clinVarAccession")).get("acc")).equals("RCV000171500"))) {
                found = true;
                break;
            }
        }
        assertEquals(found, true);

//        assertEquals(((Document) queryResult4.getResult().get(4)).get("mutationID"), "COSM4624460");
//        assertEquals(((Document)((Document) ((Document) ((Document) queryResult4.getResult().get(1)).get("clinvarSet"))
//                .get("referenceClinVarAssertion")).get("clinVarAccession")).get("acc"), "RCV000171500");

        Query query5 = new Query();
        query5.put("clinvar-significance", "Likely_pathogenic");
        QueryOptions queryOptions5 = new QueryOptions();
        queryOptions5.add("limit", 30);
//        queryOptions5.add("include", "mutationID,clinvarSet.referenceClinVarAssertion.clinVarAccession.acc");
        QueryResult queryResult5 = clinicalDBAdaptor.nativeGet(query5, queryOptions5);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(10048, queryResult5.getNumTotalResults());

        Query query6 = new Query();
        query6.put("gene", "APOE");
        QueryOptions queryOptions6 = new QueryOptions();
        queryOptions6.add("limit", 30);
        queryOptions6.put("sort", "chromosome,start");
        QueryResult queryResult6 = clinicalDBAdaptor.nativeGet(query6, queryOptions6);
        // Check sorted output
        int previousStart = -1;
        for (Document document : (List<Document>) queryResult6.getResult()) {
            assertTrue(previousStart < document.getInteger("start"));
            System.out.println("document = " + document.get("chromosome") + ", " + document.get("start"));
        }
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(103, queryResult6.getNumTotalResults());

        queryOptions6.remove("sort");
        query6.put("source", "clinvar");
        QueryResult queryResult7 = clinicalDBAdaptor.nativeGet(query6, queryOptions6);
        assertEquals(43, queryResult7.getNumTotalResults());

        query6.put("source", "cosmic");
        QueryResult queryResult8 = clinicalDBAdaptor.nativeGet(query6, queryOptions6);
        assertEquals(60, queryResult8.getNumTotalResults());
        assertEquals(((Document) queryResult8.getResult().get(0)).get("geneName"), "APOE");

        Query query7 = new Query();
        query7.put("cosmicId","COSM306824");
        query7.put("source", "cosmic");
        QueryOptions options = new QueryOptions();
        QueryResult queryResult9 = clinicalDBAdaptor.nativeGet(query7, options);
        assertNotNull("Should return the queryResult of id=COSM306824", queryResult9.getResult());
        assertEquals(((Document)queryResult9.getResult().get(0)).get("geneName"), "FMN2");

    }

//    @Test
//    public void testGetAllByGenomicVariantList() throws Exception {
//        ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens", "GRCh38");
//        QueryOptions queryOptions = new QueryOptions();
//        List<QueryResult> queryResult = clinicalDBAdaptor
//                .getAllByGenomicVariantList(Collections.singletonList(new Variant("2:47414420:T:-")), queryOptions);
//
//        assertEquals("RCV000076755",
//                ((VariantTraitAssociation) queryResult.get(0).getResult().get(0)).getClinvar().get(0).getAccession());
//
//    }
}