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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.formats.feature.gff.Gff2;
import org.opencb.biodata.formats.feature.gtf.Gtf;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.TranscriptTfbs;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;


public class GeneBuilderTest {
    private GeneBuilder geneParser;
    private static final SpeciesConfiguration SPECIES = new SpeciesConfiguration("hsapiens", "Homo sapiens", "human", null, null, null);
    public GeneBuilderTest() throws URISyntaxException {
        init();
    }

    @BeforeEach
    public void init() throws URISyntaxException {
        Path genomeSequenceFastaFile = Paths.get(getClass().getResource("/gene/Homo_sapiens.GRCh38.fa.gz").toURI());
        Path geneDirectoryPath = Paths.get(getClass().getResource("/gene").toURI());
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "gene", true);
        geneParser = new GeneBuilder(geneDirectoryPath, genomeSequenceFastaFile, SPECIES, serializer);
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

    @Test
    public void testaddTranscriptTfbstoList() throws Exception {

        String attributes = "binding_matrix_stable_id=ENSPFM0542;epigenomes_with_experimental_evidence=SK-N.%2CMCF-7%2CH1-hESC_3%2CHCT116;stable_id=ENSM00208374688;transcription_factor_complex=TEAD4::ESRRB";
        String source = null;
        String sequenceName = "1";
        String feature = "TF_binding_site";
        int start = 10000;
        int end = 100100;
        String score = "1.2870005";
        String strand = "+";
        String frame = null;

        Gff2 tfbs = new Gff2(sequenceName, source, feature, start, end, score, strand, frame, attributes);
        Gtf transcript = new Gtf(sequenceName, source, feature, start, end, score, strand, frame, new HashMap<>());
        ArrayList<TranscriptTfbs> transcriptTfbs = geneParser.addTranscriptTfbstoList(tfbs, transcript,"1", new ArrayList<>());

        assertEquals(1, transcriptTfbs.size());
        TranscriptTfbs result = transcriptTfbs.get(0);

        assertEquals(sequenceName, result.getChromosome());
        assertEquals(feature, result.getType());
        assertEquals(start, result.getStart());
        assertEquals(end, result.getEnd());
        assertEquals(score, String.valueOf(result.getScore()));
        assertEquals("ENSPFM0542", result.getPfmId());
        assertEquals("ENSM00208374688", result.getId());
        assertEquals(2, result.getTranscriptionFactors().size());
    }
}
