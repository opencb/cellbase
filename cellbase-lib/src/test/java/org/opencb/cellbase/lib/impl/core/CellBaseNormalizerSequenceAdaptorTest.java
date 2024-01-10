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
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.variant.annotation.CellBaseNormalizerSequenceAdaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CellBaseNormalizerSequenceAdaptorTest  extends GenericMongoDBAdaptorTest {

    private CellBaseNormalizerSequenceAdaptor cellBaseNormalizerSequenceAdaptor;

    public CellBaseNormalizerSequenceAdaptorTest() throws CellBaseException {
        super();
        cellBaseNormalizerSequenceAdaptor = new CellBaseNormalizerSequenceAdaptor(
                cellBaseManagerFactory.getGenomeManager(SPECIES, ASSEMBLY), dataRelease.getRelease());
    }

    @Test
    public void testGenomicSequenceChromosomeNotPresent() throws Exception {
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            cellBaseNormalizerSequenceAdaptor.query("1234", 1, 1999);
        });
        assertEquals("Unable to find entry for 1234:1-1999", exception.getMessage());
    }

    @Test
    public void testGenomicSequenceQueryStartEndOutOfRightBound() throws Exception {
        // Both start & end out of the right bound
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            cellBaseNormalizerSequenceAdaptor.query("17", 73973989, 73974999);
        });
        assertEquals("Unable to find entry for 17:73973989-73974999", exception.getMessage());
    }

    @Test
    public void testGenomicSequenceQueryEndOutOfRightBound() throws Exception {
        // start within the bounds, end out of the right bound. Should return last 10 nts.
        //        { "sequenceName" : "13", "start" : 39856000, "end" : 39857999 }
        //        { "sequenceName" : "13", "start" : 39858000, "end" : 39859999 }
        String result = cellBaseNormalizerSequenceAdaptor.query("13", 39859989, 39869999);
        assertEquals("ACAAATGATTT", result);
    }

    @Test
    public void testGenomicSequenceQueryStartOutOfLeftBound() throws Exception {
        // the left coordinate is out of bounds, but the right one is not.
        //        { "sequenceName" : "13", "start" : 39856000, "end" : 39857999 }
        //        { "sequenceName" : "13", "start" : 39858000, "end" : 39859999 }
        String result = cellBaseNormalizerSequenceAdaptor.query("13", 39857989, 39858000);
        assertEquals("TTTATTAATGGC", result);
    }
}