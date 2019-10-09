package org.opencb.cellbase.lib.impl;

import org.bson.Document;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.cellbase.core.api.ProteinDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 14/04/16.
 */
public class ProteinMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public ProteinMongoDBAdaptorTest() throws IOException {
        super();
    }

    @Before
    public void setUp() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/protein/protein.test.json.gz").toURI());
        loadRunner.load(path, "protein");
    }

    @Test
    public void get() throws Exception {
        ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions = new QueryOptions(QueryOptions.EXCLUDE, new ArrayList<>(Arrays.asList("_id", "_chunkIds")));
        queryOptions.put(QueryOptions.LIMIT, 3);
        queryOptions.put(QueryOptions.INCLUDE, "accession,name");
        QueryResult<Entry> queryResult = proteinDBAdaptor.get(new Query(), queryOptions);
        assertEquals(queryResult.getResult().size(), 3);
        assertEquals(queryResult.getNumTotalResults(), 4);
        queryResult = proteinDBAdaptor.get(new Query(ProteinDBAdaptor.QueryParams.ACCESSION.key(),
                "B2R8Q1,Q9UKT9"), queryOptions);
        assertEquals(queryResult.getResult().get(0).getAccession().get(1), "B2R8Q1");
        assertEquals(queryResult.getResult().get(1).getAccession().get(0), "Q9UKT9");
        queryResult = proteinDBAdaptor.get(new Query(ProteinDBAdaptor.QueryParams.NAME.key(),
                "MKS1_HUMAN"), queryOptions);
        assertEquals(queryResult.getResult().get(0).getName().get(0), "MKS1_HUMAN");
    }

    @Test
    public void nativeGet() throws Exception {
        ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions = new QueryOptions(QueryOptions.LIMIT, 3);
        QueryResult<Document> queryResult = proteinDBAdaptor.nativeGet(new Query(), queryOptions);
        assertEquals(queryResult.getResult().size(), 3);
        assertEquals(queryResult.getNumResults(), 3);
        assertEquals(queryResult.getNumTotalResults(), 4);
        queryResult = proteinDBAdaptor.nativeGet(new Query(ProteinDBAdaptor.QueryParams.ACCESSION.key(),
                "B2R8Q1,Q9UKT9"), queryOptions);
        assertEquals(((List) queryResult.getResult().get(0).get("accession")).get(0), "Q9UL59");
        assertEquals(((List) queryResult.getResult().get(0).get("accession")).get(1), "B2R8Q1");
        assertEquals(((List) queryResult.getResult().get(1).get("accession")).get(0), "Q9UKT9");
        queryResult = proteinDBAdaptor.nativeGet(new Query(ProteinDBAdaptor.QueryParams.NAME.key(),
                "MKS1_HUMAN"), queryOptions);
        assertEquals(((List) queryResult.getResult().get(0).get("name")).get(0), "MKS1_HUMAN");
    }

}