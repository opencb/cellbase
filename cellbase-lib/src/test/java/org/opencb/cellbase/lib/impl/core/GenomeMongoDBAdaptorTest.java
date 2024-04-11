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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opencb.biodata.models.core.Chromosome;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.avro.Cytoband;
import org.opencb.cellbase.core.api.GenomeQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.GenomeManager;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by fjlopez on 18/04/16.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GenomeMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    private GenomeManager genomeManager;

    public GenomeMongoDBAdaptorTest() throws Exception {
        super();
        genomeManager = cellBaseManagerFactory.getGenomeManager(SPECIES, ASSEMBLY);
    }

    @Test
    public void getChromosomeInfo() throws Exception {
        GenomeQuery query = new GenomeQuery();
        query.setNames(Collections.singletonList("1"));
        query.setDataRelease(dataRelease.getRelease());
        CellBaseDataResult<Chromosome> cellBaseDataResult = genomeManager.search(query);
        logger.error("cellBaseDataResult.getResults().size() = " + cellBaseDataResult.getResults().size());
        Chromosome chromosome = cellBaseDataResult.getResults().get(0);
        assertEquals("1", chromosome.getName());
        assertEquals(248956422, chromosome.getSize());
    }

    @Test
    public void getGenomicSequence() throws CellBaseException {
        CellBaseDataResult<GenomeSequenceFeature> cellBaseDataResult = genomeManager.getGenomicSequence(new Query("region", "1:1-1999"),
                new QueryOptions(), dataRelease.getRelease());
        // Inter-genic regions are not stored in the test dataset (maybe in the future should be stored)
        assertEquals(0, cellBaseDataResult.getNumResults());

        cellBaseDataResult = genomeManager.getGenomicSequence(new Query("region", "11:65497100-65497110"), new QueryOptions(),
                dataRelease.getRelease());
        assertEquals("GGTCATTGCTT", cellBaseDataResult.getResults().get(0).getSequence());

        cellBaseDataResult = genomeManager.getGenomicSequence(new Query("region", "9:126426800-126426815"), new QueryOptions(),
                dataRelease.getRelease());
        assertEquals("TAAGAGAGAAACAAGC", cellBaseDataResult.getResults().get(0).getSequence());
    }

    @Test
    public void testGenomicSequenceChromosomeNotPresent() throws CellBaseException {
        CellBaseDataResult<GenomeSequenceFeature> cellBaseDataResult = genomeManager.getSequence(new Region("1234:1-1999"),
                new QueryOptions(), dataRelease.getRelease());
        assertEquals(0, cellBaseDataResult.getNumResults());
    }

    @Test
    public void testGenomicSequenceQueryOutOfBounds() throws CellBaseException, QueryException, IllegalAccessException {
        // Both start & end out of the right bound
        CellBaseDataResult<GenomeSequenceFeature> cellBaseDataResult = genomeManager
                .getSequence(new Region("17", 43044999, 43045999), new QueryOptions(), dataRelease.getRelease());
        assertEquals(0, cellBaseDataResult.getNumResults());

        // start within the bounds, end out of the right bound. Should return last 10 nts.
        cellBaseDataResult = genomeManager.getSequence(new Region("17", 43043989, 43045999), new QueryOptions(), dataRelease.getRelease());
        assertEquals(1, cellBaseDataResult.getNumResults());
        assertEquals("ACAGGGATCTT", cellBaseDataResult.getResults().get(0).getSequence());

        // Start out of the left bound, end in bound. should return nts.
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.add("count", "true");
        cellBaseDataResult = genomeManager.getSequence(new Region("17", 7650000, 7660010), queryOptions, dataRelease.getRelease());
        assertEquals(1, cellBaseDataResult.getNumResults());
    }


    @Test
    public void testGetCytoband() throws CellBaseException {
        List<Region> regions = Arrays.asList(new Region("19:55799900-55803000"), new Region("11:121300000-124030001"));
        List<CellBaseDataResult<Cytoband>> cellBaseDataResultList = genomeManager.getCytobands(regions, dataRelease.getRelease());

        assertEquals(2, cellBaseDataResultList.size());
        CellBaseDataResult<Cytoband> result = cellBaseDataResultList.get(0);
        logger.error("result + " + result.toString());
        assertEquals(2, cellBaseDataResultList.get(0).getNumResults());
        String[] names1 = {"q13.42", "q13.43",};
        for (int i = 0; i < cellBaseDataResultList.get(0).getNumResults(); i++) {
            assertEquals(names1[i], cellBaseDataResultList.get(0).getResults().get(i).getName());
        }

        assertEquals(3, cellBaseDataResultList.get(1).getNumResults());
        String[] names2 = {"q23.3", "q24.1", "q24.2",};
        for (int i = 0; i < cellBaseDataResultList.get(1).getNumResults(); i++) {
            assertEquals(names2[i], cellBaseDataResultList.get(1).getResults().get(i).getName());
        }
    }
}
