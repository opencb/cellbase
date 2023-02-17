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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.variant.annotation.CellBaseNormalizerSequenceAdaptor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CellBaseNormalizerSequenceAdaptorTest  extends GenericMongoDBAdaptorTest {

    private CellBaseNormalizerSequenceAdaptor cellBaseNormalizerSequenceAdaptor;

    public CellBaseNormalizerSequenceAdaptorTest() {
    }

    @BeforeEach
    public void setUp() {
        try {
            clearDB(CELLBASE_DBNAME);

            dataRelease = 1;
            createDataRelease();

            Path path = Paths.get(getClass()
                    .getResource("/genome/genome_info.json").toURI());
            loadRunner.load(path, "genome_info", dataRelease);
            updateDataRelease(dataRelease, "genome_info", Collections.emptyList());

            path = Paths.get(getClass()
                    .getResource("/genome/genome_sequence.test.json.gz").toURI());
            loadRunner.load(path, "genome_sequence", dataRelease);
            updateDataRelease(dataRelease, "genome_sequence", Collections.emptyList());

            cellBaseNormalizerSequenceAdaptor = new CellBaseNormalizerSequenceAdaptor(
                    cellBaseManagerFactory.getGenomeManager(SPECIES, ASSEMBLY), dataRelease);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled
    public void testGenomicSequenceChromosomeNotPresent() throws Exception {
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            cellBaseNormalizerSequenceAdaptor.query("1234", 1, 1999);
        });
        assertEquals("Unable to find entry for 1234:1-1999", exception.getMessage());
    }

    @Test
    @Disabled
    public void testGenomicSequenceQueryStartEndOutOfRightBound() throws Exception {
        // Both start & end out of the right bound
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            cellBaseNormalizerSequenceAdaptor.query("17", 73973989, 73974999);
        });
        assertEquals("Unable to find entry for 17:73973989-73974999", exception.getMessage());

    }

    @Test
    @Disabled
    public void testGenomicSequenceQueryEndOutOfRightBound() throws Exception {
        // start within the bounds, end out of the right bound. Should return last 10 nts.
        String result = cellBaseNormalizerSequenceAdaptor.query("17", 63973989, 63974999);
        assertEquals("TCAAGACCAGC", result);

    }

    @Test
    @Disabled
    public void testGenomicSequenceQueryStartOutOfLeftBound() throws Exception {
        // the left coordinate is out of bounds, but the right one is not.
        String result = cellBaseNormalizerSequenceAdaptor.query("17", 63969989, 63970000);
        assertEquals("GGAGAGAGAAA", result);
    }

}