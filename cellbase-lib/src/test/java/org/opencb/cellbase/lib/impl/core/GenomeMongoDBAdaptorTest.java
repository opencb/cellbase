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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opencb.biodata.models.core.Chromosome;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.avro.Cytoband;
import org.opencb.cellbase.core.api.GenomeQuery;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.GenomeManager;
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

    private GenomeManager genomeManager;

    public GenomeMongoDBAdaptorTest() throws Exception {
        super();
    }

    @BeforeAll
    public void setUp() throws Exception {
        clearDB(CELLBASE_DBNAME);

        createDataRelease();
        dataRelease = 1;

        Path path = Paths.get(getClass().getResource("/genome/genome_info.json").toURI());
        loadRunner.load(path, "genome_info", dataRelease);
        updateDataRelease(dataRelease, "genome_info", Collections.emptyList());

        path = Paths.get(getClass().getResource("/genome/genome_sequence.test.json.gz").toURI());
        loadRunner.load(path, "genome_sequence", dataRelease);
        updateDataRelease(dataRelease, "genome_sequence", Collections.emptyList());

        genomeManager = cellBaseManagerFactory.getGenomeManager(SPECIES, ASSEMBLY);
    }

    @Test
    @Disabled
    public void getChromosomeInfo() throws Exception {
        GenomeQuery query = new GenomeQuery();
        query.setNames(Collections.singletonList("1"));
        query.setDataRelease(dataRelease);
        CellBaseDataResult<Chromosome> cellBaseDataResult = genomeManager.search(query);
        logger.error("cellBaseDataResult.getResults().size() = " + cellBaseDataResult.getResults().size());
        Chromosome chromosome = cellBaseDataResult.getResults().get(0);
        assertEquals("1", chromosome.getName());
        assertEquals(248956422, chromosome.getSize());
    }

    @Test
    @Disabled
    public void getGenomicSequence() throws CellBaseException {
        CellBaseDataResult<GenomeSequenceFeature> cellBaseDataResult = genomeManager.getGenomicSequence(new Query("region", "1:1-1999"), new QueryOptions(), dataRelease);
        assertEquals(StringUtils.repeat("N", 1999), cellBaseDataResult.getResults().get(0).getSequence());

        cellBaseDataResult = genomeManager.getGenomicSequence(new Query("region", "17:63971994-63972004"), new QueryOptions(), dataRelease);
        assertEquals("GAGAAAAAACC", cellBaseDataResult.getResults().get(0).getSequence());

        cellBaseDataResult = genomeManager.getGenomicSequence(new Query("region", "13:47933990-47934003"), new QueryOptions(), dataRelease);
        assertEquals("TTCATTTTTAGATT", cellBaseDataResult.getResults().get(0).getSequence());
    }

    @Test
    @Disabled
    public void testGenomicSequenceChromosomeNotPresent() throws CellBaseException {
        CellBaseDataResult<GenomeSequenceFeature> cellBaseDataResult = genomeManager
                .getSequence(new Region("1234:1-1999"), new QueryOptions(), dataRelease);
        assertEquals(0, cellBaseDataResult.getNumResults());
    }

    @Test
    @Disabled
    public void testGenomicSequenceQueryOutOfBounds() throws CellBaseException {
        // Both start & end out of the right bound
        CellBaseDataResult<GenomeSequenceFeature> cellBaseDataResult = genomeManager
                .getSequence(new Region("17", 73973989, 73974999), new QueryOptions(), dataRelease);
        assertEquals(0, cellBaseDataResult.getNumResults());

        // start within the bounds, end out of the right bound. Should return last 10 nts.
        cellBaseDataResult = genomeManager.getSequence(new Region("17", 63973989, 63974999), new QueryOptions(), dataRelease);
        assertEquals(1, cellBaseDataResult.getNumResults());
        assertEquals("TCAAGACCAGC", cellBaseDataResult.getResults().get(0).getSequence());

        // Start out of the left bound, end in bound. should return nts.
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.add("count", "true");
        cellBaseDataResult = genomeManager.getSequence(new Region("17", 63960000, 63970000), queryOptions, dataRelease);
        assertEquals(1, cellBaseDataResult.getNumResults());
    }


    @Test
    @Disabled
    public void testGetCytoband() throws CellBaseException {
        List<Region> regions = Arrays.asList(new Region("19:55799900-55803000"), new Region("11:121300000-124030001"));
        List<CellBaseDataResult<Cytoband>> cellBaseDataResultList = genomeManager.getCytobands(regions, dataRelease);

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
