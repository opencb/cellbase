package org.opencb.cellbase.app.transform.utils;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 9/25/13
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class GenomeSequenceUtilsTest {

    @Test
    public void testGetGenomeSequence() throws Exception {
        Map<String, String> sequenceMap = GenomeSequenceUtils.getGenomeSequence(Paths.get("/home/imedina/cellbase_v3/hsapiens/sequence/Homo_sapiens.GRCh37.p12.fa.gz"));
        Iterator<Map.Entry<String, String>> iter = sequenceMap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            System.out.println(entry.getKey()+": "+entry.getValue().length());
        }
    }

    @Test
    public void testGetGenomeSequenceGZipped() throws Exception {
        Map<String, byte[]> sequenceMap = GenomeSequenceUtils.getGenomeSequenceGZipped(Paths.get("/home/imedina/cellbase_v3/hsapiens/sequence/Homo_sapiens.GRCh37.p12.fa.gz"));
        Iterator<Map.Entry<String, byte[]>> iter = sequenceMap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<String, byte[]> entry = iter.next();
            System.out.println(entry.getKey()+": "+StringUtils.gunzip(entry.getValue()).length());
        }
    }

    @Test
    public void testGetSequenceByChromosomeName() throws Exception {

    }
}
