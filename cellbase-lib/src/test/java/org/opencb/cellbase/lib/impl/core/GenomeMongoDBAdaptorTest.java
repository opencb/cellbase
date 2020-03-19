/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.lib.impl.core;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.avro.Cytoband;
import org.opencb.cellbase.core.api.queries.GenomeQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by fjlopez on 18/04/16.
 */
public class GenomeMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {
    private GenomeMongoDBAdaptor dbAdaptor;

    public GenomeMongoDBAdaptorTest() throws Exception {
        super();
        setUp();
    }

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
        GenomeQuery query = new GenomeQuery();
        CellBaseDataResult CellBaseDataResult = dbAdaptor.query(query);
        assertEquals(Integer.valueOf(64444167),
                ((Document) ((List) ((Document) CellBaseDataResult.getResults().get(0)).get("chromosomes")).get(0)).get("size"));
    }

    @Test
    public void getGenomicSequence() {
        CellBaseDataResult<GenomeSequenceFeature> CellBaseDataResult = dbAdaptor.getGenomicSequence(new Query("region", "1:1-1999"), new QueryOptions());
        assertEquals(StringUtils.repeat("N", 1999), CellBaseDataResult.getResults().get(0).getSequence());

        CellBaseDataResult = dbAdaptor.getGenomicSequence(new Query("region", "17:63971994-63972004"), new QueryOptions());
        assertEquals("GAGAAAAAACC", CellBaseDataResult.getResults().get(0).getSequence());

        CellBaseDataResult = dbAdaptor.getGenomicSequence(new Query("region", "13:47933990-47934003"), new QueryOptions());
        assertEquals("TTCATTTTTAGATT", CellBaseDataResult.getResults().get(0).getSequence());
    }

    @Test
    public void testGenomicSequenceChromosomeNotPresent() {
        CellBaseDataResult<GenomeSequenceFeature> CellBaseDataResult = dbAdaptor.getSequence(new Region("1234:1-1999"), new QueryOptions());
        assertEquals(0, CellBaseDataResult.getResults().size());
    }

    @Test
    public void testGenomicSequenceQueryOutOfBounds() {
        // Both start & end out of the right bound
        CellBaseDataResult<GenomeSequenceFeature> CellBaseDataResult = dbAdaptor
                .getSequence(new Region("17", 73973989, 73974999), new QueryOptions());
        assertEquals(0, CellBaseDataResult.getResults().size());

        // start within the bounds, end out of the right bound. Should return last 10 nts.
        CellBaseDataResult = dbAdaptor
                .getSequence(new Region("17", 63973989, 63974999), new QueryOptions());
        assertEquals(1, CellBaseDataResult.getResults().size());
        assertEquals("TCAAGACCAGC", CellBaseDataResult.getResults().get(0).getSequence());

        // Start out of the left bound
        CellBaseDataResult = dbAdaptor
                .getSequence(new Region("1", -100, 1999), new QueryOptions());
        assertEquals(0, CellBaseDataResult.getResults().size());


    }

    @Test
    public void getCytoband() {
        List<CellBaseDataResult<Cytoband>> CellBaseDataResultList
                = dbAdaptor.getCytobands(Arrays.asList(new Region("19:55799900-55803000"),
                new Region("11:121300000-124030001")));

        assertEquals(2, CellBaseDataResultList.size());

        assertEquals(2, CellBaseDataResultList.get(0).getNumTotalResults());
        String[] names1 = {"q13.42", "q13.43",};
        for (int i = 0; i < CellBaseDataResultList.get(0).getNumResults(); i++) {
            assertEquals(names1[i], CellBaseDataResultList.get(0).getResults().get(i).getName());
        }

        assertEquals(3, CellBaseDataResultList.get(1).getNumTotalResults());
        String[] names2 = {"q23.3","q24.1","q24.2",};
        for (int i = 0; i < CellBaseDataResultList.get(1).getNumResults(); i++) {
            assertEquals(names2[i], CellBaseDataResultList.get(1).getResults().get(i).getName());
        }

    }
}