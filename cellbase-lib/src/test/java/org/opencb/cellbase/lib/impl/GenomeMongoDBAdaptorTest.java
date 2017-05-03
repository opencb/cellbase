package org.opencb.cellbase.lib.impl;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.junit.Test;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.avro.Cytoband;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by fjlopez on 18/04/16.
 */
public class GenomeMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {
    @Test
    public void getChromosomeInfo() throws Exception {
        GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor("hsapiens", "GRCh37");
        QueryResult queryResult = dbAdaptor.getChromosomeInfo("20", new QueryOptions());
        assertEquals(Integer.valueOf(64444167),
                ((Document) ((List) ((Document) queryResult.getResult().get(0)).get("chromosomes")).get(0)).get("size"));
    }

    @Test
    public void getGenomicSequence() {
        GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor("hsapiens", "GRCh37");
        QueryResult<GenomeSequenceFeature> queryResult = dbAdaptor.getGenomicSequence(new Query("region", "1:1-1999"), new QueryOptions());
        assertEquals(StringUtils.repeat("N", 1999), queryResult.getResult().get(0).getSequence());

        queryResult = dbAdaptor.getGenomicSequence(new Query("region", "17:63971994-63972004"), new QueryOptions());
        assertEquals("GAGAAAAAACC", queryResult.getResult().get(0).getSequence());

        queryResult = dbAdaptor.getGenomicSequence(new Query("region", "13:47933990-47934003"), new QueryOptions());
        assertEquals("TTCATTTTTAGATT", queryResult.getResult().get(0).getSequence());
    }

    @Test
    public void getCytoband() {
        GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor("hsapiens", "GRCh37");
        List<QueryResult<Cytoband>> queryResultList
                = dbAdaptor.getCytoband(Arrays.asList(new Region("19:55799900-55803000"),
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