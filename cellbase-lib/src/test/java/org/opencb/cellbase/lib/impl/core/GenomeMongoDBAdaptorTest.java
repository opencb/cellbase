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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opencb.biodata.models.core.Chromosome;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by fjlopez on 18/04/16.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GenomeMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {
    private GenomeMongoDBAdaptor dbAdaptor;

    public GenomeMongoDBAdaptorTest() throws Exception {
        super();
    }

    @BeforeAll
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
        query.setNames(Collections.singletonList("1"));
        CellBaseDataResult<Chromosome> cellBaseDataResult = dbAdaptor.query(query);
        logger.error("cellBaseDataResult.getResults()" + cellBaseDataResult.getResults().size());
        Chromosome chromosome = cellBaseDataResult.getResults().get(0);
        assertEquals("1", chromosome.getName());
        assertEquals(248956422, chromosome.getSize());

    }

    @Test
    public void getGenomicSequence() {
        CellBaseDataResult<GenomeSequenceFeature> cellBaseDataResult = dbAdaptor.getGenomicSequence(new Query("region", "1:1-1999"), new QueryOptions());
        assertEquals(StringUtils.repeat("N", 1999), cellBaseDataResult.getResults().get(0).getSequence());

        cellBaseDataResult = dbAdaptor.getGenomicSequence(new Query("region", "17:63971994-63972004"), new QueryOptions());
        assertEquals("GAGAAAAAACC", cellBaseDataResult.getResults().get(0).getSequence());

        cellBaseDataResult = dbAdaptor.getGenomicSequence(new Query("region", "13:47933990-47934003"), new QueryOptions());
        assertEquals("TTCATTTTTAGATT", cellBaseDataResult.getResults().get(0).getSequence());
    }

    @Test
    public void testGenomicSequenceChromosomeNotPresent() {
        CellBaseDataResult<GenomeSequenceFeature> cellBaseDataResult = dbAdaptor.getSequence(new Region("1234:1-1999"), new QueryOptions());
        assertEquals(0, cellBaseDataResult.getNumResults());
    }

    @Test
    public void testGenomicSequenceQueryOutOfBounds() {
        // Both start & end out of the right bound
        CellBaseDataResult<GenomeSequenceFeature> cellBaseDataResult = dbAdaptor
                .getSequence(new Region("17", 73973989, 73974999), new QueryOptions());
        assertEquals(0, cellBaseDataResult.getNumResults());

        // start within the bounds, end out of the right bound. Should return last 10 nts.
        cellBaseDataResult = dbAdaptor
                .getSequence(new Region("17", 63973989, 63974999), new QueryOptions());
        assertEquals(1, cellBaseDataResult.getNumResults());
        assertEquals("TCAAGACCAGC", cellBaseDataResult.getResults().get(0).getSequence());

        // Start out of the left bound
        cellBaseDataResult = dbAdaptor
                .getSequence(new Region("1", -100, 1999), new QueryOptions());
        assertEquals(0, cellBaseDataResult.getNumResults());
    }


    @Test
    public void testGetCytoband() {
        List<Region> regions = Arrays.asList(new Region("19:55799900-55803000"),
                new Region("11:121300000-124030001"));
        List<CellBaseDataResult<Cytoband>> cellBaseDataResultList = dbAdaptor.getCytobands(regions);

        assertEquals(2, cellBaseDataResultList.size());
        CellBaseDataResult<Cytoband> result = cellBaseDataResultList.get(0);
        logger.error("result + " + result.toString());
        assertEquals(2, cellBaseDataResultList.get(0).getNumResults());
        String[] names1 = {"q13.42", "q13.43",};
        for (int i = 0; i < cellBaseDataResultList.get(0).getNumResults(); i++) {
            assertEquals(names1[i], cellBaseDataResultList.get(0).getResults().get(i).getName());
        }

        assertEquals(3, cellBaseDataResultList.get(1).getNumResults());
        String[] names2 = {"q23.3","q24.1","q24.2",};
        for (int i = 0; i < cellBaseDataResultList.get(1).getNumResults(); i++) {
            assertEquals(names2[i], cellBaseDataResultList.get(1).getResults().get(i).getName());
        }

    }
}