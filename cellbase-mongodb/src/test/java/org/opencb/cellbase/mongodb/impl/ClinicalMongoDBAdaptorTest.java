package org.opencb.cellbase.mongodb.impl;

import org.junit.Test;
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
        Query query = new Query();
        query.put("phenotype", "alzheimer");
        queryOptions.add("limit", 30);
        QueryResult queryResult = clinicalDBAdaptor.nativeGet(query, queryOptions);
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