package org.opencb.cellbase.mongodb.impl;

import org.bson.Document;
import org.junit.Test;
import org.opencb.cellbase.core.api.TranscriptDBAdaptor;
import org.opencb.cellbase.mongodb.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 27/04/16.
 */
public class TranscriptMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {
    @Test
    public void nativeGet() throws Exception {

        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor("hsapiens", "GRCh37");
        Query query = new Query(TranscriptDBAdaptor.QueryParams.REGION.key(), "1:816481-825251");
        QueryResult queryResult = transcriptDBAdaptor.nativeGet(query, new QueryOptions());
        assertEquals(queryResult.getNumResults(), 1);
        assertEquals(((Document) queryResult.getResult().get(0)).get("id"), "ENST00000594233");

        query = new Query(TranscriptDBAdaptor.QueryParams.XREFS.key(), "Q9UL59");
        QueryOptions queryOptions = new QueryOptions("include", "transcripts.id");
        queryResult = transcriptDBAdaptor.nativeGet(query, queryOptions);

        int a = 1;
    }

}