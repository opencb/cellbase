package org.opencb.cellbase.lib.builders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.MirnaTarget;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GeneBuilderIndexerTest {

    @Test
    public void testPareMirTarFile() throws IOException {

        Path mirtarbasePath = Paths.get(this.getClass().getClassLoader().getResource("regulation/mmu_MTI.csv").getPath());
        MiRTarBaseIndexer indexer = new MiRTarBaseIndexer();
        Map<String, List<MirnaTarget>> result = indexer.index(mirtarbasePath);
        System.out.println("result.size() = " + result.size());
        for (Map.Entry<String, List<MirnaTarget>> entry : result.entrySet()) {
            System.out.println("------");
            System.out.println("entry.getKey() = " + entry.getKey());
            for (MirnaTarget mirnaTarget : entry.getValue()) {
                System.out.println("mirnaTarget = " + mirnaTarget);
            }
        }

        Assertions.assertEquals(27, result.size());
        Assertions.assertEquals(3, result.get("Arid4b").size());
        for (String mirt : Arrays.asList("MIRT003749", "MIRT003750", "MIRT003751")) {
            Assertions.assertTrue(result.get("Arid4b").stream().anyMatch(m -> m.getId().equalsIgnoreCase(mirt)));
        }

        Assertions.assertEquals(4, result.get("Mylip").size());
        for (String mirt : Arrays.asList("MIRT002978", "MIRT002327", "MIRT002976", "MIRT002975")) {
            Assertions.assertTrue(result.get("Mylip").stream().anyMatch(m -> m.getId().equalsIgnoreCase(mirt)));
        }

        Assertions.assertEquals(1, result.get("Tlr4").size());
        Assertions.assertEquals("MIRT003037", result.get("Tlr4").get(0).getId());
        Assertions.assertEquals("mmu-let-7e-5p", result.get("Tlr4").get(0).getSourceId());

        Assertions.assertEquals(1, result.get("Aldoa").size());
        Assertions.assertEquals("MIRT003133", result.get("Aldoa").get(0).getId());
        Assertions.assertEquals("mmu-miR-122-5p", result.get("Aldoa").get(0).getSourceId());
        Assertions.assertEquals(4, result.get("Aldoa").get(0).getTargets().size());
        for (String pubmed : Arrays.asList("18158304", "16258535", "16459310", "18438401")) {
            Assertions.assertTrue(result.get("Aldoa").get(0).getTargets().stream().anyMatch(t -> t.getPubmed().equalsIgnoreCase(pubmed)));
        }

    }
}