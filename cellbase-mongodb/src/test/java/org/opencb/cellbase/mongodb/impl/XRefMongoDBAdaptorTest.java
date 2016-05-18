package org.opencb.cellbase.mongodb.impl;

import org.bson.Document;
import org.junit.Test;
import org.opencb.cellbase.core.api.XRefDBAdaptor;
import org.opencb.cellbase.mongodb.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 09/05/16.
 */
public class XRefMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {
    @Test
    public void contains() throws Exception {
        XRefDBAdaptor xRefDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor("hsapiens", "GRCh37");
        QueryResult xrefs = xRefDBAdaptor.contains("BRCA2", new QueryOptions());
        Set<String> reference = new HashSet<>(Arrays.asList("ENSG00000185515", "ENSG00000139618", "ENSG00000107949",
                "ENSG00000083093", "ENSG00000170037"));
        Set<String> set = (Set) xrefs.getResult().stream()
                .map(result -> ((String) ((Document) result).get("id"))).collect(Collectors.toSet());
        assertEquals(reference, set);
    }

}