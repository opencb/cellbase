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


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opencb.biodata.formats.feature.gff.Gff2;
import org.opencb.biodata.formats.feature.gtf.Gtf;
import org.opencb.biodata.models.core.*;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GeneBuilderTest {
    private GeneBuilder geneParser;
    private ObjectMapper jsonObjectMapper;
    private static final SpeciesConfiguration SPECIES = new SpeciesConfiguration("hsapiens", "Homo sapiens", "human", null, null, null);
    public GeneBuilderTest() throws URISyntaxException, CellbaseException {
        init();
    }

    @BeforeAll
    public void init() throws URISyntaxException, CellbaseException {
        Path genomeSequenceFastaFile
                = Paths.get(GeneBuilderTest.class.getResource("/gene/Homo_sapiens.GRCh38.fa").toURI());
        Path geneDirectoryPath = Paths.get(GeneBuilderTest.class.getResource("/gene").toURI());
        // put the results in /tmp
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "gene",
                true);
        SpeciesConfiguration species = new SpeciesConfiguration("hsapiens", "Homo sapiens",
                "human", null, null, null);
        geneParser = new GeneBuilder(geneDirectoryPath, genomeSequenceFastaFile, species, serializer);
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void testRocksdb() throws Exception {
        geneParser.parse();

        List<Gene> genes = loadSerializedGenes("/tmp/gene.json.gz");

        Gene gene = getGene("ENSG00000227232", genes);

        assertNotNull(gene);
        assertEquals("WASP family homolog 7, pseudogene [Source:HGNC Symbol;Acc:HGNC:38034]", gene.getDescription());

        MiRnaGene miRNAGene = gene.getMirna();
        assertNotNull(miRNAGene);
        assertEquals("UGGGAUGAGGUAGUAGGUUGUAUAGUUUUAGGGUCACACCCACCACUGGGAGAUAACUAUACAAUCUACUGUCUUUCCUA", miRNAGene.getSequence());
        assertEquals("UNCHANGED", miRNAGene.getStatus());
        assertEquals("MI0000060", miRNAGene.getAccession());
        assertEquals("hsa-let-7a-1", miRNAGene.getId());
        assertEquals(2, miRNAGene.getMatures().size());

        List<MiRnaMature> matures = miRNAGene.getMatures();
        for (MiRnaMature mature : matures) {


            if (mature.getId().equals("hsa-let-7a-5p")) {
                assertEquals("UGAGGUAGUAGGUUGUAUAGUU", mature.getSequence());
                assertEquals("MIMAT0000062", mature.getAccession());
                assertEquals("5", String.valueOf(mature.getStart()));
                assertEquals("27", String.valueOf(mature.getEnd()));
            }

            if (mature.getId().equals("hsa-let-7a-3p")) {
                assertEquals("CUAUACAAUCUACUGUCUUUC", mature.getSequence());
                assertEquals("MIMAT0004481", mature.getAccession());
                assertEquals("56", String.valueOf(mature.getStart()));
                assertEquals("77", String.valueOf(mature.getEnd()));
            }
        }

        GeneAnnotation annotation = gene.getAnnotation();
        assertEquals(1, annotation.getTargets().size());
        MiRnaTarget target = annotation.getTargets().get(0);
        assertEquals("MIRT000002", target.getId());
        assertEquals("miRTarBase", target.getSource());
        assertEquals("hsa-miR-20a-5p", target.getSourceId());
        assertEquals(3, target.getTargets().size());

        Transcript transcript = getTranscript(gene, "ENST00000488147");
        assertEquals(15, transcript.getXrefs().size());

    }

    private Xref getXref(Transcript transcript, String xrefId) {
        for (Xref xref : transcript.getXrefs()) {
            if (xref.getId().equals(xrefId)) {
                return xref;
            }
        }
        return null;
    }

    /**
     * Checks a case in which the stop codon is the first 3 nts of an exon. ENSE00003800362 is in the negative strand.
     * genomicCodingEnd, cdnaCodingStart and cdsStart must be set "manually" within the parser as there's no CDS line
     * in the GTF since the stop codon itself is not part of the coding sequence (but historically considered part of
     * the coding region in CellBase)
     */
    @Test
    public void testEdgeExonCodingStart() throws Exception {
        geneParser.parse();
        List<Gene> genes = loadSerializedGenes("/tmp/gene.json.gz");
        Exon exon = getExon("ENSE00003800362", genes);
        assertNotNull(exon);
        assertEquals(28477630, exon.getGenomicCodingEnd());
        assertEquals(1302, exon.getCdnaCodingStart());
        assertEquals(1198, exon.getCdsStart());
    }

    private Exon getExon(String exonId, List<Gene> genes) {
        for (Gene gene : genes) {
            for (Transcript transcript : gene.getTranscripts()) {
                for (Exon exon : transcript.getExons()) {
                    if (exonId.equals(exon.getId())) {
                        return exon;
                    }
                }
            }
        }
        return null;
    }

    private Gene getGene(String id, List<Gene> genes) {
        for (Gene gene : genes) {
            if (id.equals(gene.getId())) {
                return gene;
            }
        }
        return null;
    }

    @Test
    public void testTranscriptSequenceAndVersion() throws Exception {
        geneParser.parse();
        final String TRANSCRIPT_SEQUENCE = "GTTAACTTGCCGTCAGCCTTTTCTTTGACCTCTTCTTTCTGTTCATGTGTATTTGCTGTCTCTTAGCCCAGACTTCCCGTGTCCTTTCCACCGGGCCTTTGAGAGGTCACAGGGTCTTGATGCTGTGGTCTTCATCTGCAGGTGTCTGACTTCCAGCAACTGCTGGCCTGTGCCAGGGTGCAAGCTGAGCACTGGAGTGGAGTTTTCCTGTGGAGAGGAGCCATGCCTAGAGTGGGATGGGCCATTGTTCATCTTCTGGCCCCTGTTGTCTGCATGTAACTTAATACCACAACCAGGCATAGGGGAAAGATTGGAGGAAAGATGAGTGAGAGCATCAACTTCTCTCACAACCTAGGCCAGTGTGTGGTGATGCCAGGCATGCCCTTCCCCAGCATCAGGTCTCCAGAGCTGCAGAAGACGACGGCCGACTTGGATCACACTCTTGTGAGTGTCCCCAGTGTTGCAGAGGCAGGGCCATCAGGCACCAAAGGGATTCTGCCAGCATAGTGCTCCTGGACCAGTGATACACCCGGCACCCTGTCCTGGACACGCTGTTGGCCTGGATCTGAGCCCTGGTGGAGGTCAAAGCCACCTTTGGTTCTGCCATTGCTGCTGTGTGGAAGTTCACTCCTGCCTTTTCCTTTCCCTAGAGCCTCCACCACCCCGAGATCACATTTCTCACTGCCTTTTGTCTGCCCAGTTTCACCAGAAGTAGGCCTCTTCCTGACAGGCAGCTGCACCACTGCCTGGCGCTGTGCCCTTCCTTTGCTCTGCCCGCTGGAGACGGTGTTTGTCATGGGCCTGGTCTGCAGGGATCCTGCTACAAAGGTGAAACCCAGGAGAGTGTGGAGTCCAGAGTGTTGCCAGGACCCAGGCACAGGCATTAGTGCCCGTTGGAGAAAACAGGGGAATCCCGAAGAAATGGTGGGTCCTGGCCATCCGTGAGATCTTCCCAGGGCAGCTCCCCTCTGTGGAATCCAATCTGTCTTCCATCCTGCGTGGCCGAGGGCCAGGCTTCTCACTGGGCCTCTGCAGGAGGCTGCCATTTGTCCTGCCCACCTTCTTAGAAGCGAGACGGAGCAGACCCATCTGCTACTGCCCTTTCTATAATAACTAAAGTTAGCTGCCCTGGACTATTCACCCCCTAGTCTCAATTTAAGAAGATCCCCATGGCCACAGGGCCCCTGCCTGGGGGCTTGTCACCTCCCCCACCTTCTTCCTGAGTCATTCCTGCAGCCTTGCTCCCTAACCTGCCCCACAGCCTTGCCTGGATTTCTATCTCCCTGGCTTGGTGCCAGTTCCTCCAAGTCGATGGCACCTCCCTCCCTCTCAACCACTTGAGCAAACTCCAAGACATCTTCTACCCCAACACCAGCAATTGTGCCAAGGGCCATTAGGCTCTCAGCATGACTATTTTTAGAGACCCCGTGTCTGTCACTGAAACCTTTTTTGTGGGAGACTATTCCTCCCATCTGCAACAGCTGCCCCTGCTGACTGCCCTTCTCTCCTCCCTCTCATCCCAGAGAAACAGGTCAGCTGGGAGCTTCTGCCCCCACTGCCTAGGGACCAACAGGGGCAGGAGGCAGTCACTGACCCCGAGACGTTTGCATCCTGCACAGCTAGAGATCCTTTATTAAAAGCACACTGTTGGTTTCTG";
        List<Gene> genes = loadSerializedGenes("/tmp/gene.json.gz");
        Transcript transcript = getTranscript("ENST00000456328", genes);
        assertNotNull(transcript);
        assertEquals(TRANSCRIPT_SEQUENCE, transcript.getcDnaSequence());
        assertEquals(2, transcript.getVersion());
        assertEquals("havana", transcript.getSource());
        assertEquals("1", transcript.getSupportLevel());
    }

    private Transcript getTranscript(Gene gene, String transcriptId) {
        for (Transcript transcript : gene.getTranscripts()) {
            if (transcript.getId().equals(transcriptId)) {
                return transcript;
            }
        }
        return null;
    }

    private Transcript getTranscript(String transcriptId, List<Gene> geneList) {
        for (Gene gene : geneList) {
            return getTranscript(gene, transcriptId);
        }
        return null;
    }

    @Test
    public void testProteinSequence() throws Exception {
        geneParser.parse();
        final String PROTEIN_SEQUENCE = "MVTEFIFLGLSDSQELQTFLFMLFFVFYGGIVFGNLLIVITVVSDSHLHSPMYFLLANLSLIDLSLSSVTAPKMITDFFSQRKVISFKGCLVQIFLLHFFGGSEMVILIAMGFDRYIAICKPLHYTTIMCGNACVGIMAVTWGIGFLHSVSQLAFAVHLLFCGPNEVDSFYCDLPRVIKLACTDTYRLDIMVIANSGVLTVCSFVLLIISYTIILMTIQHRPLDKSSKALSTLTAHITVVLLFFGPCVFIYAWPFPIKSLDKFLAVFYSVITPLLNPIIYTLRNKDMKTAIRQLRKWDAHSSVKF";
        List<Gene> genes = loadSerializedGenes("/tmp/gene.json.gz");
        assertEquals(15, genes.size());
        for (Gene gene : genes) {
            if (gene.getId().equals("ENSG00000223972")) {
                for (Transcript transcript : gene.getTranscripts()) {
                    if (transcript.getId().equals("ENST00000456328")) {
                        assertEquals(PROTEIN_SEQUENCE, transcript.getProteinSequence());
                    }
                }
                assertEquals("havana", gene.getSource());
                assertEquals(5, gene.getVersion());
            }
        }
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
        List<TranscriptTfbs> transcriptTfbs = geneParser.addTranscriptTfbstoList(tfbs, transcript,"1", new ArrayList<>());

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

    private List<Gene> loadSerializedGenes(String fileName) {
        List<Gene> geneList = new ArrayList();

        try {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(Paths.get(fileName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                geneList.add(jsonObjectMapper.readValue(line, Gene.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
            assertFalse(false);
        }

        return geneList;
    }


}
