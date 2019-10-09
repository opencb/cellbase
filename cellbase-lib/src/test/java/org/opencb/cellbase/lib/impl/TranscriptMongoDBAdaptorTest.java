package org.opencb.cellbase.lib.impl;

import org.bson.Document;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.core.api.TranscriptDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public TranscriptMongoDBAdaptorTest() throws IOException {
        super();
    }

    @Before
    public void setUp() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/transcript/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene");
    }

    @Test
    public void nativeGet() throws Exception {

        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor("hsapiens", "GRCh37");
        Query query = new Query(TranscriptDBAdaptor.QueryParams.REGION.key(), "1:816481-825251");
        QueryResult queryResult = transcriptDBAdaptor.nativeGet(query, new QueryOptions());
        assertEquals(queryResult.getNumResults(), 1);
        assertEquals(((Document) queryResult.getResult().get(0)).size(), 18);
        assertEquals(((Document) queryResult.getResult().get(0)).get("id"), "ENST00000594233");

        query = new Query(TranscriptDBAdaptor.QueryParams.REGION.key(), "1:31851-44817");
        queryResult = transcriptDBAdaptor.nativeGet(query, new QueryOptions());
        assertEquals(queryResult.getNumResults(), 2);
        assertTrue(transcriptIdEquals(queryResult, Arrays.asList("ENST00000417324", "ENST00000461467")));

        query = new Query(TranscriptDBAdaptor.QueryParams.XREFS.key(), "Q9UL59");
        QueryOptions queryOptions = new QueryOptions("include", "id");
        queryResult = transcriptDBAdaptor.nativeGet(query, queryOptions);
        assertEquals(queryResult.getNumResults(), 2);
        assertEquals(1, ((Document) queryResult.getResult().get(0)).size());
        assertEquals(1, ((Document) queryResult.getResult().get(1)).size());
        assertTrue(transcriptIdEquals(queryResult, Arrays.asList("ENST00000278314", "ENST00000536068")));

        query = new Query(TranscriptDBAdaptor.QueryParams.BIOTYPE.key(), "protein_coding");
        query.put(TranscriptDBAdaptor.QueryParams.XREFS.key(), "BRCA2");
        queryOptions = new QueryOptions("include", "transcripts.id");
        queryResult = transcriptDBAdaptor.nativeGet(query, queryOptions);
        assertEquals("Number of transcripts with biotype protein_coding", 3, queryResult.getNumTotalResults());

    }

    private boolean transcriptIdEquals(QueryResult queryResult, List<String> transcriptIdList) {
        Set<String> set1 = (Set) queryResult.getResult().stream()
                .map(result -> ((String) ((Document) result).get("id"))).collect(Collectors.toSet());
        Set<String> set2 = new HashSet<>(transcriptIdList);

        return set1.equals(set2);
    }


}