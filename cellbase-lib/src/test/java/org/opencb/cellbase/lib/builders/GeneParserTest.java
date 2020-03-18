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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
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


public class GeneParserTest {
    private GeneParser geneParser;
    private ObjectMapper jsonObjectMapper;
    public GeneParserTest() throws URISyntaxException {
        init();
    }

    @BeforeEach
    public void init() throws URISyntaxException {
        Path genomeSequenceFastaFile
                = Paths.get(GeneParserTest.class.getResource("/gene/Homo_sapiens.GRCh38.fa.gz").toURI());
        Path geneDirectoryPath = Paths.get(GeneParserTest.class.getResource("/gene").toURI());
        // put the results in /tmp
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "gene",
                true);
        SpeciesConfiguration species = new SpeciesConfiguration("hsapiens", "Homo sapiens",
                "human", null, null, null);
        geneParser = new GeneParser(geneDirectoryPath, genomeSequenceFastaFile, species, serializer);
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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

    @Test
    public void testTranscriptSequence() throws Exception {
        geneParser.parse();
        final String TRANSCRIPT_SEQUENCE = "GTTAACTTGCCGTCAGCCTTTTCTTTGACCTCTTCTTTCTGTTCATGTGTATTTGCTGTCTCTTAGCCCAGACTTCCCGTGTCCTTTCCACCGGGCCTTTGAGAGGTCACAGGGTCTTGATGCTGTGGTCTTCATCTGCAGGTGTCTGACTTCCAGCAACTGCTGGCCTGTGCCAGGGTGCAAGCTGAGCACTGGAGTGGAGTTTTCCTGTGGAGAGGAGCCATGCCTAGAGTGGGATGGGCCATTGTTCATCTTCTGGCCCCTGTTGTCTGCATGTAACTTAATACCACAACCAGGCATAGGGGAAAGATTGGAGGAAAGATGAGTGAGAGCATCAACTTCTCTCACAACCTAGGCCAGTGTGTGGTGATGCCAGGCATGCCCTTCCCCAGCATCAGGTCTCCAGAGCTGCAGAAGACGACGGCCGACTTGGATCACACTCTTGTGAGTGTCCCCAGTGTTGCAGAGGCAGGGCCATCAGGCACCAAAGGGATTCTGCCAGCATAGTGCTCCTGGACCAGTGATACACCCGGCACCCTGTCCTGGACACGCTGTTGGCCTGGATCTGAGCCCTGGTGGAGGTCAAAGCCACCTTTGGTTCTGCCATTGCTGCTGTGTGGAAGTTCACTCCTGCCTTTTCCTTTCCCTAGAGCCTCCACCACCCCGAGATCACATTTCTCACTGCCTTTTGTCTGCCCAGTTTCACCAGAAGTAGGCCTCTTCCTGACAGGCAGCTGCACCACTGCCTGGCGCTGTGCCCTTCCTTTGCTCTGCCCGCTGGAGACGGTGTTTGTCATGGGCCTGGTCTGCAGGGATCCTGCTACAAAGGTGAAACCCAGGAGAGTGTGGAGTCCAGAGTGTTGCCAGGACCCAGGCACAGGCATTAGTGCCCGTTGGAGAAAACAGGGGAATCCCGAAGAAATGGTGGGTCCTGGCCATCCGTGAGATCTTCCCAGGGCAGCTCCCCTCTGTGGAATCCAATCTGTCTTCCATCCTGCGTGGCCGAGGGCCAGGCTTCTCACTGGGCCTCTGCAGGAGGCTGCCATTTGTCCTGCCCACCTTCTTAGAAGCGAGACGGAGCAGACCCATCTGCTACTGCCCTTTCTATAATAACTAAAGTTAGCTGCCCTGGACTATTCACCCCCTAGTCTCAATTTAAGAAGATCCCCATGGCCACAGGGCCCCTGCCTGGGGGCTTGTCACCTCCCCCACCTTCTTCCTGAGTCATTCCTGCAGCCTTGCTCCCTAACCTGCCCCACAGCCTTGCCTGGATTTCTATCTCCCTGGCTTGGTGCCAGTTCCTCCAAGTCGATGGCACCTCCCTCCCTCTCAACCACTTGAGCAAACTCCAAGACATCTTCTACCCCAACACCAGCAATTGTGCCAAGGGCCATTAGGCTCTCAGCATGACTATTTTTAGAGACCCCGTGTCTGTCACTGAAACCTTTTTTGTGGGAGACTATTCCTCCCATCTGCAACAGCTGCCCCTGCTGACTGCCCTTCTCTCCTCCCTCTCATCCCAGAGAAACAGGTCAGCTGGGAGCTTCTGCCCCCACTGCCTAGGGACCAACAGGGGCAGGAGGCAGTCACTGACCCCGAGACGTTTGCATCCTGCACAGCTAGAGATCCTTTATTAAAAGCACACTGTTGGTTTCTG";
        List<Gene> genes = loadSerializedGenes("/tmp/gene.json.gz");
        Transcript transcript = getTranscript("ENST00000456328", genes);
        assertNotNull(transcript);
        assertEquals(TRANSCRIPT_SEQUENCE, transcript.getcDnaSequence());
    }

    private Transcript getTranscript(String transcriptId, List<Gene> geneList) {
        for (Gene gene : geneList) {
            for (Transcript transcript : gene.getTranscripts()) {
                if (transcript.getId().equals(transcriptId)) {
                    return transcript;
                }
            }
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
            }
        }
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
