package org.opencb.cellbase.lib.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opencb.cellbase.core.variant.annotation.CellBaseNormalizerSequenceAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class CellBaseNormalizerSequenceAdaptorTest  extends GenericMongoDBAdaptorTest {

    private CellBaseNormalizerSequenceAdaptor cellBaseNormalizerSequenceAdaptor;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public CellBaseNormalizerSequenceAdaptorTest() throws IOException {
    }

    @Before
    public void setUp() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/genome/genome_info.json").toURI());
        loadRunner.load(path, "genome_info");
        path = Paths.get(getClass()
                .getResource("/genome/genome_sequence.test.json.gz").toURI());
        loadRunner.load(path, "genome_sequence");
        cellBaseNormalizerSequenceAdaptor = new CellBaseNormalizerSequenceAdaptor(dbAdaptorFactory
                .getGenomeDBAdaptor("hsapiens", "GRCh37"));
    }

    @Test
    public void testGenomicSequenceChromosomeNotPresent() throws Exception {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Unable to find entry for 1234:1-1999");
        cellBaseNormalizerSequenceAdaptor.query("1234", 1, 1999);
    }

    @Test
    public void testGenomicSequenceQueryStartEndOutOfRightBound() throws Exception {
        // Both start & end out of the right bound
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Unable to find entry for 17:73973989-73974999");
        cellBaseNormalizerSequenceAdaptor.query("17", 73973989, 73974999);
    }

    @Test
    public void testGenomicSequenceQueryEndOutOfRightBound() throws Exception {
        // start within the bounds, end out of the right bound. Should return last 10 nts.
        String result = cellBaseNormalizerSequenceAdaptor.query("17", 63973989, 63974999);
        assertEquals("TCAAGACCAGC", result);

    }

    @Test
    public void testGenomicSequenceQueryStartOutOfLeftBound() throws Exception {
        // start within the bounds, end out of the right bound. Should return last 10 nts.
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Unable to find entry for 1:-100-1999");
        cellBaseNormalizerSequenceAdaptor.query("1", -100, 1999);
    }


}