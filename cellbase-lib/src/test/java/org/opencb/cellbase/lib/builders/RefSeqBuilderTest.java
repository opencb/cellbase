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
import org.mortbay.util.ajax.JSON;
import org.opencb.biodata.models.core.*;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RefSeqBuilderTest {
    private RefSeqGeneBuilder geneParser;
    private ObjectMapper jsonObjectMapper;
    private static final SpeciesConfiguration SPECIES = new SpeciesConfiguration("hsapiens", "Homo sapiens", "human", null, null, null);
    public RefSeqBuilderTest() throws Exception {
        init();
    }

    @BeforeAll
    public void init() throws Exception {
        Path genomeSequenceFastaFile
                = Paths.get(RefSeqBuilderTest.class.getResource("/gene/Homo_sapiens.GRCh38.fa").toURI());
        Path geneDirectoryPath = Paths.get(RefSeqBuilderTest.class.getResource("/gene_refseq").toURI());
        // put the results in /tmp
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "refseq",
                true);
        SpeciesConfiguration species = new SpeciesConfiguration("hsapiens", "Homo sapiens",
                "human", null, null, null);
        geneParser = new RefSeqGeneBuilder(geneDirectoryPath, species, serializer);
        geneParser.parse();
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        serializer.close();
    }

    @Test
    public void testParse() throws Exception {

        List<Gene> genes = loadGenes(Paths.get("/tmp/refseq.json.gz"));
        assertEquals(1, genes.size());


        assertEquals(loadGenes(Paths.get(getClass().getResource("/gene_refseq/refseq.json.gz").getFile())),
                loadGenes(Paths.get("/tmp/refseq.json.gz")));
    }

    private List<Gene> loadGenes(Path path) throws IOException {
        List<Gene> genes = new ArrayList<>();
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(path)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                genes.add(jsonObjectMapper.convertValue(JSON.parse(line), Gene.class));
                line = bufferedReader.readLine();
            }
        }

        return genes;
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
//    @Test
//    public void testEdgeExonCodingStart() throws Exception {
//        geneParser.parse();
//        List<Gene> genes = loadSerializedGenes("/tmp/gene.json.gz");
//        Exon exon = getExon("ENSE00003800362", genes);
//        assertNotNull(exon);
//        assertEquals(28477630, exon.getGenomicCodingEnd());
//        assertEquals(1302, exon.getCdnaCodingStart());
//        assertEquals(1198, exon.getCdsStart());
//    }

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

//    @Test
//    public void testTranscriptSequenceAndVersion() throws Exception {
//        geneParser.parse();
//        final String TRANSCRIPT_SEQUENCE = "GTTAACTTGCCGTCAGCCTTTTCTTTGACCTCTTCTTTCTGTTCATGTGTATTTGCTGTCTCTTAGCCCAGACTTCCCGTGTCCTTTCCACCGGGCCTTTGAGAGGTCACAGGGTCTTGATGCTGTGGTCTTCATCTGCAGGTGTCTGACTTCCAGCAACTGCTGGCCTGTGCCAGGGTGCAAGCTGAGCACTGGAGTGGAGTTTTCCTGTGGAGAGGAGCCATGCCTAGAGTGGGATGGGCCATTGTTCATCTTCTGGCCCCTGTTGTCTGCATGTAACTTAATACCACAACCAGGCATAGGGGAAAGATTGGAGGAAAGATGAGTGAGAGCATCAACTTCTCTCACAACCTAGGCCAGTGTGTGGTGATGCCAGGCATGCCCTTCCCCAGCATCAGGTCTCCAGAGCTGCAGAAGACGACGGCCGACTTGGATCACACTCTTGTGAGTGTCCCCAGTGTTGCAGAGGCAGGGCCATCAGGCACCAAAGGGATTCTGCCAGCATAGTGCTCCTGGACCAGTGATACACCCGGCACCCTGTCCTGGACACGCTGTTGGCCTGGATCTGAGCCCTGGTGGAGGTCAAAGCCACCTTTGGTTCTGCCATTGCTGCTGTGTGGAAGTTCACTCCTGCCTTTTCCTTTCCCTAGAGCCTCCACCACCCCGAGATCACATTTCTCACTGCCTTTTGTCTGCCCAGTTTCACCAGAAGTAGGCCTCTTCCTGACAGGCAGCTGCACCACTGCCTGGCGCTGTGCCCTTCCTTTGCTCTGCCCGCTGGAGACGGTGTTTGTCATGGGCCTGGTCTGCAGGGATCCTGCTACAAAGGTGAAACCCAGGAGAGTGTGGAGTCCAGAGTGTTGCCAGGACCCAGGCACAGGCATTAGTGCCCGTTGGAGAAAACAGGGGAATCCCGAAGAAATGGTGGGTCCTGGCCATCCGTGAGATCTTCCCAGGGCAGCTCCCCTCTGTGGAATCCAATCTGTCTTCCATCCTGCGTGGCCGAGGGCCAGGCTTCTCACTGGGCCTCTGCAGGAGGCTGCCATTTGTCCTGCCCACCTTCTTAGAAGCGAGACGGAGCAGACCCATCTGCTACTGCCCTTTCTATAATAACTAAAGTTAGCTGCCCTGGACTATTCACCCCCTAGTCTCAATTTAAGAAGATCCCCATGGCCACAGGGCCCCTGCCTGGGGGCTTGTCACCTCCCCCACCTTCTTCCTGAGTCATTCCTGCAGCCTTGCTCCCTAACCTGCCCCACAGCCTTGCCTGGATTTCTATCTCCCTGGCTTGGTGCCAGTTCCTCCAAGTCGATGGCACCTCCCTCCCTCTCAACCACTTGAGCAAACTCCAAGACATCTTCTACCCCAACACCAGCAATTGTGCCAAGGGCCATTAGGCTCTCAGCATGACTATTTTTAGAGACCCCGTGTCTGTCACTGAAACCTTTTTTGTGGGAGACTATTCCTCCCATCTGCAACAGCTGCCCCTGCTGACTGCCCTTCTCTCCTCCCTCTCATCCCAGAGAAACAGGTCAGCTGGGAGCTTCTGCCCCCACTGCCTAGGGACCAACAGGGGCAGGAGGCAGTCACTGACCCCGAGACGTTTGCATCCTGCACAGCTAGAGATCCTTTATTAAAAGCACACTGTTGGTTTCTG";
//        List<Gene> genes = loadSerializedGenes("/tmp/gene.json.gz");
//        Transcript transcript = getTranscript("ENST00000456328", genes);
//        assertNotNull(transcript);
//        assertEquals(TRANSCRIPT_SEQUENCE, transcript.getcDnaSequence());
//        assertEquals(2, transcript.getVersion());
//        assertEquals("havana", transcript.getSource());
//        assertEquals("1", transcript.getSupportLevel());
//    }

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

//    @Test
//    public void testProteinSequence() throws Exception {
//        geneParser.parse();
//        final String PROTEIN_SEQUENCE = "MVTEFIFLGLSDSQELQTFLFMLFFVFYGGIVFGNLLIVITVVSDSHLHSPMYFLLANLSLIDLSLSSVTAPKMITDFFSQRKVISFKGCLVQIFLLHFFGGSEMVILIAMGFDRYIAICKPLHYTTIMCGNACVGIMAVTWGIGFLHSVSQLAFAVHLLFCGPNEVDSFYCDLPRVIKLACTDTYRLDIMVIANSGVLTVCSFVLLIISYTIILMTIQHRPLDKSSKALSTLTAHITVVLLFFGPCVFIYAWPFPIKSLDKFLAVFYSVITPLLNPIIYTLRNKDMKTAIRQLRKWDAHSSVKF";
//        List<Gene> genes = loadSerializedGenes("/tmp/gene.json.gz");
//        assertEquals(15, genes.size());
//        for (Gene gene : genes) {
//            if (gene.getId().equals("ENSG00000223972")) {
//                for (Transcript transcript : gene.getTranscripts()) {
//                    if (transcript.getId().equals("ENST00000456328")) {
//                        assertEquals(PROTEIN_SEQUENCE, transcript.getProteinSequence());
//                    }
//                }
//                assertEquals("havana", gene.getSource());
//                assertEquals("5", gene.getVersion());
//            }
//        }
//    }

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
