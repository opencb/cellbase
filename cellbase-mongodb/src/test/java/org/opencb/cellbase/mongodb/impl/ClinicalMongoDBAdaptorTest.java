package org.opencb.cellbase.mongodb.impl;

import org.bson.Document;
import org.junit.Test;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
import org.opencb.cellbase.mongodb.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 09/12/15.
 */
public class ClinicalMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public ClinicalMongoDBAdaptorTest() { super(); }

    @Test
    public void testNativeGet() throws Exception {

        ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions = new QueryOptions();

        Query query1 = new Query();
        query1.put("phenotype", "alzheimer");
        queryOptions.add("limit", 30);
        QueryResult queryResult1 = clinicalDBAdaptor.nativeGet(query1, queryOptions);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(queryResult1.getNumTotalResults(), 85);
        assertEquals(queryResult1.getNumResults(), 30);
        assertEquals(((Document)((Document) ((Document) ((Document) queryResult1.getResult().get(17)).get("clinvarSet"))
                .get("referenceClinVarAssertion")).get("clinVarAccession")).get("acc"), "RCV000019725");

        Query query2 = new Query();
        query2.put("phenotype", "myelofibrosis");
        queryOptions.add("limit", 30);
        QueryResult queryResult2 = clinicalDBAdaptor.nativeGet(query2, queryOptions);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(queryResult2.getNumTotalResults(), 7066);
        assertEquals(queryResult2.getNumResults(), 30);

        query2.put("source", "cosmic");
        QueryResult queryResult3 = clinicalDBAdaptor.nativeGet(query2, queryOptions);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(queryResult3.getNumTotalResults(), 7062);
        assertEquals(((Document) queryResult3.getResult().get(14)).get("mutationID"), "COSM12600");

        Query query4 = new Query();
        query4.put("region", new Region("2", 170360030, 170362030));
        QueryResult queryResult4 = clinicalDBAdaptor.nativeGet(query4, queryOptions);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(queryResult4.getNumTotalResults(), 10);
        assertEquals(((Document) queryResult4.getResult().get(4)).get("mutationID"), "COSM228320");
        assertEquals(((Document)((Document) ((Document) ((Document) queryResult4.getResult().get(7)).get("clinvarSet"))
                .get("referenceClinVarAssertion")).get("clinVarAccession")).get("acc"), "RCV000171500");

        Query query5 = new Query();
        query5.put("significance", "Likely_pathogenic");
        QueryResult queryResult5 = clinicalDBAdaptor.nativeGet(query5, queryOptions);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(queryResult5.getNumTotalResults(), 10);
        assertEquals(((Document) queryResult5.getResult().get(4)).get("mutationID"), "COSM228320");
        assertEquals(((Document)((Document) ((Document) ((Document) queryResult5.getResult().get(17)).get("clinvarSet"))
                .get("referenceClinVarAssertion")).get("clinVarAccession")).get("acc"), "RCV000171500");

//        queryOptions.add("source", "gwas");
//        queryOptions.add("phenotype", "ALZHEIMER DISEASE 2, DUE TO APOE4 ISOFORM");
//        queryOptions.addToListOption("phenotype", "ALZHEIMER");

//        queryOptions.addToListOption("phenotype", "retinitis");
//        queryOptions.addToListOption("phenotype", "diabetes");
//        queryOptions.addToListOption("region", new Region("3", 550000, 1166666));
//        queryOptions.add("region", "5:13759611-13799611");
//        queryOptions.addToListOption("region", new Region("1", 550000, 1166666));
//        queryOptions.addToListOption("gene", "APOE");
//        queryOptions.addToListOption("significance", "Likely_pathogenic");
//        queryOptions.addToListOption("review", "REVIEWED_BY_PROFESSIONAL_SOCIETY");
//        queryOptions.addToListOption("type", "Indel");
//        queryOptions.addToListOption("so", "missense_variant");
//        queryOptions.addToListOption("rs", "rs429358");
//        queryOptions.addToListOption("rcv", "RCV000019455");

//        ((List<String>) queryOptions.get("include")).remove(0);


    }
}