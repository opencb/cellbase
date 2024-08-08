package org.opencb.cellbase.lib.builders;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.MirnaTarget;
import org.opencb.biodata.models.core.TargetGene;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

public class GeneBuilderIndexerTest {

    @Test
    public void testPareMirTarFile() throws IOException {

        Path mirtarbasePath = Paths.get(this.getClass().getClassLoader().getResource("regulation/hsa_MTI.xlsx").getPath());
        MiRTarBaseIndexer indexer = new MiRTarBaseIndexer();
        Map<String, List<MirnaTarget>> result = indexer.index(mirtarbasePath);

        Assertions.assertEquals(5, result.size());

        List<Pair<String, String>> pairs = Arrays.asList(new ImmutablePair<>("WASH7P", "MIRT000002"),
                new ImmutablePair<>("CXCR4", "MIRT000006"),
                new ImmutablePair<>("CYP7A1", "MIRT000012"),
                new ImmutablePair<>("STAT5A", "MIRT000018"),
                new ImmutablePair<>("RASGRP1", "MIRT000019"));


        for (Pair<String, String> pair : pairs) {
            Assertions.assertTrue(result.containsKey(pair.getKey()));
            Assertions.assertEquals(pair.getValue(), result.get(pair.getKey()).get(0).getId());
        }

        // MIRT000018	hsa-miR-222-3p	Homo sapiens	STAT5A	6776	Homo sapiens	qRT-PCR//Luciferase reporter assay//Western blot	Functional MTI	20489169
        // MIRT000018	hsa-miR-222-3p	Homo sapiens	STAT5A	6776	Homo sapiens	Luciferase reporter assay	Functional MTI	24736554
        Assertions.assertEquals(1, result.get("STAT5A").size());
        Assertions.assertEquals("hsa-miR-222-3p", result.get("STAT5A").get(0).getSourceId());
        Assertions.assertEquals(2, result.get("STAT5A").get(0).getTargets().size());
        for (TargetGene target : result.get("STAT5A").get(0).getTargets()) {
            switch (target.getPubmed()) {
                case "20489169": {
                    Assertions.assertEquals("Functional MTI", target.getEvidence());
                    Assertions.assertEquals("qRT-PCR//Luciferase reporter assay//Western blot", target.getExperiment());
                    break;
                }
                case "24736554": {
                    Assertions.assertEquals("Functional MTI", target.getEvidence());
                    Assertions.assertEquals("Luciferase reporter assay", target.getExperiment());
                    break;
                }
                default: {
                    fail();
                }
            }
        }
    }
}