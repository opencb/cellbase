package org.opencb.cellbase.mongodb.impl;

import org.bson.Document;
import org.junit.Test;
import org.opencb.cellbase.core.api.TranscriptDBAdaptor;
import org.opencb.cellbase.mongodb.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        assertEquals(((Document) queryResult.getResult().get(0)).size(), 10);
        assertEquals(((Document) queryResult.getResult().get(0)).get("id"), "ENST00000594233");

        query = new Query(TranscriptDBAdaptor.QueryParams.REGION.key(), "1:31851-44817");
        queryResult = transcriptDBAdaptor.nativeGet(query, new QueryOptions());
        assertEquals(queryResult.getNumResults(), 2);
        assertTrue(transcriptIdEquals(queryResult, Arrays.asList("ENST00000417324", "ENST00000461467")));

        query = new Query(TranscriptDBAdaptor.QueryParams.XREFS.key(), "Q9UL59");
        QueryOptions queryOptions = new QueryOptions("include", "transcripts.id");
        queryResult = transcriptDBAdaptor.nativeGet(query, queryOptions);
        assertEquals(queryResult.getNumResults(), 2);
        assertEquals(((Document) queryResult.getResult().get(0)).size(), 1);
        assertEquals(((Document) queryResult.getResult().get(1)).size(), 1);
        assertTrue(transcriptIdEquals(queryResult, Arrays.asList("ENST00000278314", "ENST00000536068")));
    }

    private boolean transcriptIdEquals(QueryResult queryResult, List<String> transcriptIdList) {
        Set<String> set1 = (Set) queryResult.getResult().stream()
                .map(result -> ((String) ((Document) result).get("id"))).collect(Collectors.toSet());
        Set<String> set2 = new HashSet<>(transcriptIdList);

        return set1.equals(set2);
    }


}