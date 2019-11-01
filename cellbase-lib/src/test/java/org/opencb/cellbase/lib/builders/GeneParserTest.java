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

package org.opencb.cellbase.lib.builders;


import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.opencb.biodata.formats.feature.gtf.Gtf;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.config.Species;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class GeneParserTest {
    private GeneParser geneParser;
    private static final Species SPECIES = new Species("hsapiens", "Homo sapiens", "human", null, null);
    public GeneParserTest() throws URISyntaxException {
        init();
    }

    @BeforeEach
    public void init() throws URISyntaxException {
        Path genomeSequenceFastaFile = Paths.get(getClass().getResource("/gene/Homo_sapiens.GRCh38.fa.gz").toURI());
        Path geneDirectoryPath = Paths.get(getClass().getResource("/gene").toURI());
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "regulatory_region", true);
        geneParser = new GeneParser(geneDirectoryPath, genomeSequenceFastaFile, SPECIES, serializer);
    }

    @Test
    public void testProcessExons() throws Exception {
        Transcript transcript = new Transcript();
        Exon exon = new Exon();
        int cdna = 1;
        int cds = 2;
        Gtf gtf = new Gtf("chr1", "source", "feature", 1, 2, "3", "+", "4", null);
        int cdsCount = geneParser.processExons(transcript, exon, cdna, cds, gtf);
        assertEquals(4, cdsCount);
    }
}
