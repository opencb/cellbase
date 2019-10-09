package org.opencb.cellbase.lib.impl;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.avro.Cytoband;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by fjlopez on 18/04/16.
 */
public class GenomeMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {
    private GenomeDBAdaptor dbAdaptor;

    public GenomeMongoDBAdaptorTest() throws IOException {
        super();
    }

    @Before
    public void setUp() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/genome/genome_info.json").toURI());
        loadRunner.load(path, "genome_info");
        path = Paths.get(getClass()
                .getResource("/genome/genome_sequence.test.json.gz").toURI());
        loadRunner.load(path, "genome_sequence");
        dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor("hsapiens", "GRCh37");
    }

    @Test
    public void getChromosomeInfo() throws Exception {
        QueryResult queryResult = dbAdaptor.getChromosomeInfo("20", new QueryOptions());
        assertEquals(Integer.valueOf(64444167),
                ((Document) ((List) ((Document) queryResult.getResult().get(0)).get("chromosomes")).get(0)).get("size"));
    }

    @Test
    public void getGenomicSequence() {
        QueryResult<GenomeSequenceFeature> queryResult = dbAdaptor.getGenomicSequence(new Query("region", "1:1-1999"), new QueryOptions());
        assertEquals(StringUtils.repeat("N", 1999), queryResult.getResult().get(0).getSequence());

        queryResult = dbAdaptor.getGenomicSequence(new Query("region", "17:63971994-63972004"), new QueryOptions());
        assertEquals("GAGAAAAAACC", queryResult.getResult().get(0).getSequence());

        queryResult = dbAdaptor.getGenomicSequence(new Query("region", "13:47933990-47934003"), new QueryOptions());
        assertEquals("TTCATTTTTAGATT", queryResult.getResult().get(0).getSequence());
    }

    @Test
    public void testGenomicSequenceChromosomeNotPresent() {
        QueryResult<GenomeSequenceFeature> queryResult = dbAdaptor.getSequence(new Region("1234:1-1999"), new QueryOptions());
        assertEquals(0, queryResult.getResult().size());
    }

    @Test
    public void testGenomicSequenceQueryOutOfBounds() {
        // Both start & end out of the right bound
        QueryResult<GenomeSequenceFeature> queryResult = dbAdaptor
                .getSequence(new Region("17", 73973989, 73974999), new QueryOptions());
        assertEquals(0, queryResult.getResult().size());

        // start within the bounds, end out of the right bound. Should return last 10 nts.
        queryResult = dbAdaptor
                .getSequence(new Region("17", 63973989, 63974999), new QueryOptions());
        assertEquals(1, queryResult.getResult().size());
        assertEquals("TCAAGACCAGC", queryResult.getResult().get(0).getSequence());

        // Start out of the left bound
        queryResult = dbAdaptor
                .getSequence(new Region("1", -100, 1999), new QueryOptions());
        assertEquals(0, queryResult.getResult().size());


    }

    @Test
    public void getCytoband() {
        List<QueryResult<Cytoband>> queryResultList
                = dbAdaptor.getCytobands(Arrays.asList(new Region("19:55799900-55803000"),
                new Region("11:121300000-124030001")));

        assertEquals(2, queryResultList.size());

        assertEquals(2, queryResultList.get(0).getNumTotalResults());
        String[] names1 = {"q13.42", "q13.43",};
        for (int i = 0; i < queryResultList.get(0).getNumResults(); i++) {
            assertEquals(names1[i], queryResultList.get(0).getResult().get(i).getName());
        }

        assertEquals(3, queryResultList.get(1).getNumTotalResults());
        String[] names2 = {"q23.3","q24.1","q24.2",};
        for (int i = 0; i < queryResultList.get(1).getNumResults(); i++) {
            assertEquals(names2[i], queryResultList.get(1).getResult().get(i).getName());
        }

    }
}