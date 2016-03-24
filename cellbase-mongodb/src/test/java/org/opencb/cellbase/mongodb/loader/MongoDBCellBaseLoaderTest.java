/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.mongodb.loader;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.cellbase.core.loader.LoadRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MongoDBCellBaseLoaderTest {

    private static MongoDBCellBaseLoader loader;

    @Before
    public void setUp() throws Exception {
        // json lines
        String firstElement = "{\"alternate\":\"C\",\"reference\":\"T\",\"chromosome\":\"19\",\"start\":45411941,\"end\":45411941,\"geneName\":\"APOE\"}";
        String secondElement = "{\"alternate\":\"A\",\"reference\":\"G\",\"chromosome\":\"10\",\"start\":129839177,\"end\":129839177,\"geneName\":\"PTPRE\"}";
        String thirdElement = " {\"alternate\":\"C\",\"reference\":\"T\",\"chromosome\":\"19\",\"start\":45411941,\"end\":45411941,\"geneName\":\"APOE\",\"mutationGRCh37Strand\":\"+\",\"primarySite\":\"large_intestine\",\"mutationAA\":\"p.C130R\",\"tumourOrigin\":\"primary\",\"histologySubtype\":\"adenocarcinoma\",\"accessionNumber\":\"ENST00000252486\",\"mutationID\":\"3749517\",\"mutationCDS\":\"c.388T>C\",\"sampleName\":\"TCGA-AH-6644-01\",\"primaryHistology\":\"carcinoma\",\"mutationGRCh37GenomePosition\":\"19:45411941-45411941\",\"mutationDescription\":\"Substitution - Missense\",\"genomeWideScreen\":\"y\",\"idSample\":\"1651586\",\"mutationSomaticStatus\":\"Confirmed somatic variant\",\"siteSubtype\":\"rectum\",\"geneCDSLength\":954,\"hgncId\":\"613\",\"sampleSource\":\"NS\",\"age\":73.0,\"snp\":true,\"idStudy\":375,\"id_tumour\":\"1566373\"}";

        // batchs
        List<String> firstBatch = new ArrayList<>();
        firstBatch.add(firstElement);
        firstBatch.add(secondElement);
        List<String> secondBatch = new ArrayList<>();
        secondBatch.add(thirdElement);

        // queue
        BlockingQueue<List<String>> queue = new ArrayBlockingQueue<>(3);
        queue.put(firstBatch);
        queue.put(secondBatch);
        queue.put(LoadRunner.POISON_PILL);

        // connection params
        Map<String, String> params = new HashMap<>();
//        params.put(CellBaseLoader.CELLBASE_HOST, "localhost");
//        params.put(CellBaseLoader.CELLBASE_PORT, "27017");
//        params.put(CellBaseLoader.CELLBASE_DATABASE_NAME_PROPERTY, "cellbaseTest");

        // loader
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug");
//        loader = new MongoDBCellBaseLoader(queue, "cosmic", params);
//        loader.init();
    }

    @After
    public void tearDown() throws Exception {
        loader.close();
    }

    @Ignore
    @Test
    public void testRun() throws Exception {
        loader.call();
    }

    // TODO: test chunk ids
}