package org.opencb.cellbase.mongodb.impl;

import com.google.common.base.Splitter;
import org.bson.Document;
import org.junit.Test;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.mongodb.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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

}