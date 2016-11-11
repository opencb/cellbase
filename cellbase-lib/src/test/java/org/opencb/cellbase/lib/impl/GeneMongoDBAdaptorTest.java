package org.opencb.cellbase.lib.impl;

import org.junit.Test;
import org.opencb.biodata.models.core.Gene;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import static org.junit.Assert.*;

/**
 * Created by swaathi on 11/11/16.
 */
public class GeneMongoDBAdaptorTest extends GenericMongoDBAdaptorTest{

    public GeneMongoDBAdaptorTest() { super(); }

    @Test
    public void next() throws Exception {
        GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor("hsapiens", "GRCh37");
        QueryResult next = geneDBAdaptor.next(new Query("name", "BRCA2"), new QueryOptions());
        Gene gene = (Gene) next.getResult().get(0);
        assertEquals("next gene is wrong", "IFIT1P1", gene.getName());
    }

}