package org.opencb.cellbase.mongodb.impl;

import org.bson.Document;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.cellbase.core.api.ProteinDBAdaptor;
import org.opencb.cellbase.mongodb.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 14/04/16.
 */
public class ProteinMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    @Test
    public void get() throws Exception {
        ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions = new QueryOptions("exclude", new ArrayList<>(Arrays.asList("_id", "_chunkIds")));
        queryOptions.put("limit", 3);
        QueryResult<Entry> queryResult = proteinDBAdaptor.get(new Query(), queryOptions);
        assertEquals(queryResult.getResult().size(), 3);
        assertEquals(queryResult.getNumTotalResults(), 20193);
        assertEquals(queryResult.getResult().get(0).getAccession().get(1), "B2R8Q1");
        assertEquals(queryResult.getResult().get(1).getAccession().get(0), "Q9UKT9");
        assertEquals(queryResult.getResult().get(2).getName().get(0), "MKS1_HUMAN");
    }

    @Test
    public void nativeGet() throws Exception {
        ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor("hsapiens", "GRCh37");
        QueryResult<Document> queryResult = proteinDBAdaptor.nativeGet(new Query(), new QueryOptions("limit", 3));
        assertEquals(queryResult.getResult().size(), 3);
        assertEquals(queryResult.getNumResults(), 3);
        assertEquals(queryResult.getNumTotalResults(), 20193);
        assertEquals(((List) queryResult.getResult().get(0).get("accession")).get(0), "Q9UL59");
        assertEquals(((List) queryResult.getResult().get(0).get("accession")).get(1), "B2R8Q1");
        assertEquals(((List) queryResult.getResult().get(1).get("accession")).get(0), "Q9UKT9");
        assertEquals(((List) queryResult.getResult().get(2).get("name")).get(0), "MKS1_HUMAN");
    }

}