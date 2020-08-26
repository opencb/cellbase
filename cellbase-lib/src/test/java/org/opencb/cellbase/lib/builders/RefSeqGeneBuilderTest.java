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
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RefSeqGeneBuilderTest {

    private RefSeqGeneBuilder geneParser;
    private ObjectMapper jsonObjectMapper;

    public RefSeqGeneBuilderTest() throws Exception {
    }

    @BeforeAll
    public void init() throws Exception {
        Path geneDirectoryPath = Paths.get(RefSeqGeneBuilderTest.class.getResource("/gene_refseq").toURI());
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
        List<Gene> parsedGeneList = loadGenes(Paths.get("/tmp/refseq.json.gz"));

        assertEquals(12, parsedGeneList.size());

        Gene gene = getGene("KPNB1", parsedGeneList);

        assertEquals("3837", gene.getId());
        assertEquals("KPNB1", gene.getName());
        assertEquals("protein_coding", gene.getBiotype());
        assertEquals("karyopherin subunit beta 1", gene.getDescription());
        assertEquals("refseq", gene.getSource());
        assertEquals("KNOWN", gene.getStatus());
        assertEquals("17", gene.getChromosome());
        assertEquals(47649919, gene.getStart());
        assertEquals(47685505, gene.getEnd());
        assertEquals("+", gene.getStrand());
        assertEquals("1", gene.getVersion());


        Transcript transcript = getTranscript(gene,"NM_002265.6");
        String expectedXrefs = "[Xref{id='HGNC:6400', dbName='HGNC', dbDisplayName='HGNC', description=''}, Xref{id='CCDS11513.1', dbName='CCDS', dbDisplayName='CCDS', description=''}, Xref{id='3837', dbName='GeneID', dbDisplayName='GeneID', description=''}, Xref{id='602738', dbName='MIM', dbDisplayName='MIM', description=''}, Xref{id='NM_002265.6', dbName='RefSeq', dbDisplayName='RefSeq', description='null'}, Xref{id='NM_002265', dbName='RefSeq', dbDisplayName='RefSeq', description='null'}]";
        assertEquals(expectedXrefs, transcript.getXrefs().toString());

        assertEquals("NM_002265.6", transcript.getId());
        assertEquals("KPNB1", transcript.getName());
        assertEquals("protein_coding", transcript.getBiotype());
        assertEquals(47649919, transcript.getStart());
        assertEquals(47685505, transcript.getEnd());
        assertEquals("6", transcript.getVersion());

        // UTR in exon [+] :: NM_002265.6 - KPNB1
        // UTR in exon [-] :: NM_015305.4 - ANGEL1

        assertEquals(47650245, transcript.getGenomicCodingStart());
        assertEquals(47682404, transcript.getGenomicCodingEnd());
        assertEquals(327, transcript.getCdnaCodingStart());
        assertEquals(2957, transcript.getCdnaCodingEnd());
        assertEquals(2630, transcript.getCdsLength());

        assertEquals(22, transcript.getExons().size());

        String expected = "[Exon{id='NM_002265.6_1', chromosome='17', start=47649919, end=47650284, strand='+', genomicCodingStart=47650245, genomicCodingEnd=47650284, cdnaCodingStart=327, cdnaCodingEnd=366, cdsStart=1, cdsEnd=40, phase=0, exonNumber=1, sequence='null'}, Exon{id='NM_002265.6_2', chromosome='17', start=47650386, end=47650444, strand='+', genomicCodingStart=47650386, genomicCodingEnd=47650444, cdnaCodingStart=367, cdnaCodingEnd=425, cdsStart=41, cdsEnd=99, phase=2, exonNumber=2, sequence='null'}, Exon{id='NM_002265.6_3', chromosome='17', start=47652694, end=47652876, strand='+', genomicCodingStart=47652694, genomicCodingEnd=47652876, cdnaCodingStart=426, cdnaCodingEnd=608, cdsStart=100, cdsEnd=282, phase=0, exonNumber=3, sequence='null'}, Exon{id='NM_002265.6_4', chromosome='17', start=47656860, end=47657060, strand='+', genomicCodingStart=47656860, genomicCodingEnd=47657060, cdnaCodingStart=609, cdnaCodingEnd=809, cdsStart=283, cdsEnd=483, phase=0, exonNumber=4, sequence='null'}, Exon{id='NM_002265.6_5', chromosome='17', start=47658508, end=47658660, strand='+', genomicCodingStart=47658508, genomicCodingEnd=47658660, cdnaCodingStart=810, cdnaCodingEnd=962, cdsStart=484, cdsEnd=636, phase=0, exonNumber=5, sequence='null'}, Exon{id='NM_002265.6_6', chromosome='17', start=47661119, end=47661178, strand='+', genomicCodingStart=47661119, genomicCodingEnd=47661178, cdnaCodingStart=963, cdnaCodingEnd=1022, cdsStart=637, cdsEnd=696, phase=0, exonNumber=6, sequence='null'}, Exon{id='NM_002265.6_7', chromosome='17', start=47663089, end=47663178, strand='+', genomicCodingStart=47663089, genomicCodingEnd=47663178, cdnaCodingStart=1023, cdnaCodingEnd=1112, cdsStart=697, cdsEnd=786, phase=0, exonNumber=7, sequence='null'}, Exon{id='NM_002265.6_8', chromosome='17', start=47664159, end=47664269, strand='+', genomicCodingStart=47664159, genomicCodingEnd=47664269, cdnaCodingStart=1113, cdnaCodingEnd=1223, cdsStart=787, cdsEnd=897, phase=0, exonNumber=8, sequence='null'}, Exon{id='NM_002265.6_9', chromosome='17', start=47665057, end=47665158, strand='+', genomicCodingStart=47665057, genomicCodingEnd=47665158, cdnaCodingStart=1224, cdnaCodingEnd=1325, cdsStart=898, cdsEnd=999, phase=0, exonNumber=9, sequence='null'}, Exon{id='NM_002265.6_10', chromosome='17', start=47668186, end=47668410, strand='+', genomicCodingStart=47668186, genomicCodingEnd=47668410, cdnaCodingStart=1326, cdnaCodingEnd=1550, cdsStart=1000, cdsEnd=1224, phase=0, exonNumber=10, sequence='null'}, Exon{id='NM_002265.6_11', chromosome='17', start=47669678, end=47669869, strand='+', genomicCodingStart=47669678, genomicCodingEnd=47669869, cdnaCodingStart=1551, cdnaCodingEnd=1742, cdsStart=1225, cdsEnd=1416, phase=0, exonNumber=11, sequence='null'}, Exon{id='NM_002265.6_12', chromosome='17', start=47670702, end=47670832, strand='+', genomicCodingStart=47670702, genomicCodingEnd=47670832, cdnaCodingStart=1743, cdnaCodingEnd=1873, cdsStart=1417, cdsEnd=1547, phase=0, exonNumber=12, sequence='null'}, Exon{id='NM_002265.6_13', chromosome='17', start=47673018, end=47673165, strand='+', genomicCodingStart=47673018, genomicCodingEnd=47673165, cdnaCodingStart=1874, cdnaCodingEnd=2021, cdsStart=1548, cdsEnd=1695, phase=1, exonNumber=13, sequence='null'}, Exon{id='NM_002265.6_14', chromosome='17', start=47673490, end=47673561, strand='+', genomicCodingStart=47673490, genomicCodingEnd=47673561, cdnaCodingStart=2022, cdnaCodingEnd=2093, cdsStart=1696, cdsEnd=1767, phase=0, exonNumber=14, sequence='null'}, Exon{id='NM_002265.6_15', chromosome='17', start=47674638, end=47674782, strand='+', genomicCodingStart=47674638, genomicCodingEnd=47674782, cdnaCodingStart=2094, cdnaCodingEnd=2238, cdsStart=1768, cdsEnd=1912, phase=0, exonNumber=15, sequence='null'}, Exon{id='NM_002265.6_16', chromosome='17', start=47676409, end=47676491, strand='+', genomicCodingStart=47676409, genomicCodingEnd=47676491, cdnaCodingStart=2239, cdnaCodingEnd=2321, cdsStart=1913, cdsEnd=1995, phase=2, exonNumber=16, sequence='null'}, Exon{id='NM_002265.6_17', chromosome='17', start=47677020, end=47677127, strand='+', genomicCodingStart=47677020, genomicCodingEnd=47677127, cdnaCodingStart=2322, cdnaCodingEnd=2429, cdsStart=1996, cdsEnd=2103, phase=0, exonNumber=17, sequence='null'}, Exon{id='NM_002265.6_18', chromosome='17', start=47678046, end=47678189, strand='+', genomicCodingStart=47678046, genomicCodingEnd=47678189, cdnaCodingStart=2430, cdnaCodingEnd=2573, cdsStart=2104, cdsEnd=2247, phase=0, exonNumber=18, sequence='null'}, Exon{id='NM_002265.6_19', chromosome='17', start=47678308, end=47678413, strand='+', genomicCodingStart=47678308, genomicCodingEnd=47678413, cdnaCodingStart=2574, cdnaCodingEnd=2679, cdsStart=2248, cdsEnd=2353, phase=0, exonNumber=19, sequence='null'}, Exon{id='NM_002265.6_20', chromosome='17', start=47680020, end=47680134, strand='+', genomicCodingStart=47680020, genomicCodingEnd=47680134, cdnaCodingStart=2680, cdnaCodingEnd=2794, cdsStart=2354, cdsEnd=2468, phase=2, exonNumber=20, sequence='null'}, Exon{id='NM_002265.6_21', chromosome='17', start=47680508, end=47680669, strand='+', genomicCodingStart=47680508, genomicCodingEnd=47680669, cdnaCodingStart=2795, cdnaCodingEnd=2956, cdsStart=2469, cdsEnd=2630, phase=1, exonNumber=21, sequence='null'}, Exon{id='NM_002265.6_22', chromosome='17', start=47682404, end=47685505, strand='+', genomicCodingStart=47682404, genomicCodingEnd=47682404, cdnaCodingStart=2957, cdnaCodingEnd=2957, cdsStart=2631, cdsEnd=2631, phase=-1, exonNumber=22, sequence='null'}]";
        assertEquals(expected, transcript.getExons().toString());

        gene = getGene("ANGEL1", parsedGeneList);
        transcript = getTranscript(gene,"NM_015305.4");
        assertEquals(76789228, transcript.getGenomicCodingStart());
        assertEquals(76812827, transcript.getGenomicCodingEnd());
        assertEquals(56, transcript.getCdnaCodingStart());
        assertEquals(2068, transcript.getCdnaCodingEnd());
        assertEquals(2012, transcript.getCdsLength());

        assertEquals(10, transcript.getExons().size());

        expected = "[Exon{id='NM_015305.4_1', chromosome='14', start=76812764, end=76812882, strand='-', genomicCodingStart=76812764, genomicCodingEnd=76812827, cdnaCodingStart=56, cdnaCodingEnd=119, cdsStart=1, cdsEnd=64, phase=0, exonNumber=1, sequence='null'}, Exon{id='NM_015305.4_2', chromosome='14', start=76809059, end=76809643, strand='-', genomicCodingStart=76809059, genomicCodingEnd=76809643, cdnaCodingStart=120, cdnaCodingEnd=704, cdsStart=65, cdsEnd=649, phase=2, exonNumber=2, sequence='null'}, Exon{id='NM_015305.4_3', chromosome='14', start=76807922, end=76808148, strand='-', genomicCodingStart=76807922, genomicCodingEnd=76808148, cdnaCodingStart=705, cdnaCodingEnd=931, cdsStart=650, cdsEnd=876, phase=2, exonNumber=3, sequence='null'}, Exon{id='NM_015305.4_4', chromosome='14', start=76807433, end=76807502, strand='-', genomicCodingStart=76807433, genomicCodingEnd=76807502, cdnaCodingStart=932, cdnaCodingEnd=1001, cdsStart=877, cdsEnd=946, phase=0, exonNumber=4, sequence='null'}, Exon{id='NM_015305.4_5', chromosome='14', start=76806416, end=76806849, strand='-', genomicCodingStart=76806416, genomicCodingEnd=76806849, cdnaCodingStart=1002, cdnaCodingEnd=1435, cdsStart=947, cdsEnd=1380, phase=2, exonNumber=5, sequence='null'}, Exon{id='NM_015305.4_6', chromosome='14', start=76803786, end=76803912, strand='-', genomicCodingStart=76803786, genomicCodingEnd=76803912, cdnaCodingStart=1436, cdnaCodingEnd=1562, cdsStart=1381, cdsEnd=1507, phase=0, exonNumber=6, sequence='null'}, Exon{id='NM_015305.4_7', chromosome='14', start=76803371, end=76803481, strand='-', genomicCodingStart=76803371, genomicCodingEnd=76803481, cdnaCodingStart=1563, cdnaCodingEnd=1673, cdsStart=1508, cdsEnd=1618, phase=2, exonNumber=7, sequence='null'}, Exon{id='NM_015305.4_8', chromosome='14', start=76791297, end=76791366, strand='-', genomicCodingStart=76791297, genomicCodingEnd=76791366, cdnaCodingStart=1674, cdnaCodingEnd=1743, cdsStart=1619, cdsEnd=1688, phase=2, exonNumber=8, sequence='null'}, Exon{id='NM_015305.4_9', chromosome='14', start=76790611, end=76790774, strand='-', genomicCodingStart=76790611, genomicCodingEnd=76790774, cdnaCodingStart=1744, cdnaCodingEnd=1907, cdsStart=1689, cdsEnd=1852, phase=1, exonNumber=9, sequence='null'}, Exon{id='NM_015305.4_10', chromosome='14', start=76786009, end=76789388, strand='-', genomicCodingStart=76789228, genomicCodingEnd=76789388, cdnaCodingStart=1908, cdnaCodingEnd=2068, cdsStart=1853, cdsEnd=2013, phase=2, exonNumber=10, sequence='null'}]";
        assertEquals(expected, transcript.getExons().toString());


        // UTR entire exon ONE [+] :: NM_001142601.2 - SPHK1
        // UTR entire exon ONE [-] :: NM_001160372.4 TRAPPC9
        gene = getGene("SPHK1", parsedGeneList);
        transcript = getTranscript(gene,"NM_001142601.2");
        assertEquals(76385645, transcript.getGenomicCodingStart());
        assertEquals(76387586, transcript.getGenomicCodingEnd());
        assertEquals(393, transcript.getCdnaCodingStart());
        assertEquals(1547, transcript.getCdnaCodingEnd());
        assertEquals(1154, transcript.getCdsLength());

        assertEquals(6, transcript.getExons().size());

        expected = "[Exon{id='NM_001142601.2_1', chromosome='17', start=76384609, end=76384806, strand='+', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=1, sequence='null'}, Exon{id='NM_001142601.2_2', chromosome='17', start=76385451, end=76385654, strand='+', genomicCodingStart=76385645, genomicCodingEnd=76385654, cdnaCodingStart=393, cdnaCodingEnd=402, cdsStart=1, cdsEnd=10, phase=0, exonNumber=2, sequence='null'}, Exon{id='NM_001142601.2_3', chromosome='17', start=76385985, end=76386137, strand='+', genomicCodingStart=76385985, genomicCodingEnd=76386137, cdnaCodingStart=403, cdnaCodingEnd=555, cdsStart=11, cdsEnd=163, phase=2, exonNumber=3, sequence='null'}, Exon{id='NM_001142601.2_4', chromosome='17', start=76386221, end=76386315, strand='+', genomicCodingStart=76386221, genomicCodingEnd=76386315, cdnaCodingStart=556, cdnaCodingEnd=650, cdsStart=164, cdsEnd=258, phase=2, exonNumber=4, sequence='null'}, Exon{id='NM_001142601.2_5', chromosome='17', start=76386393, end=76386508, strand='+', genomicCodingStart=76386393, genomicCodingEnd=76386508, cdnaCodingStart=651, cdnaCodingEnd=766, cdsStart=259, cdsEnd=374, phase=0, exonNumber=5, sequence='null'}, Exon{id='NM_001142601.2_6', chromosome='17', start=76386806, end=76387855, strand='+', genomicCodingStart=76386806, genomicCodingEnd=76387586, cdnaCodingStart=767, cdnaCodingEnd=1547, cdsStart=375, cdsEnd=1155, phase=1, exonNumber=6, sequence='null'}]";
        assertEquals(expected, transcript.getExons().toString());

        gene = getGene("TRAPPC9", parsedGeneList);
        transcript = getTranscript(gene,"NM_001160372.4");
        assertEquals(139731061, transcript.getGenomicCodingStart());
        assertEquals(140451373, transcript.getGenomicCodingEnd());
        assertEquals(117, transcript.getCdnaCodingStart());
        assertEquals(3563, transcript.getCdnaCodingEnd());
        assertEquals(3446, transcript.getCdsLength());

        assertEquals(23, transcript.getExons().size());

        expected = "[Exon{id='NM_001160372.4_1', chromosome='8', start=140457639, end=140457744, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=1, sequence='null'}, Exon{id='NM_001160372.4_2', chromosome='8', start=140450790, end=140451383, strand='-', genomicCodingStart=140450790, genomicCodingEnd=140451373, cdnaCodingStart=117, cdnaCodingEnd=700, cdsStart=1, cdsEnd=584, phase=0, exonNumber=2, sequence='null'}, Exon{id='NM_001160372.4_3', chromosome='8', start=140439052, end=140439197, strand='-', genomicCodingStart=140439052, genomicCodingEnd=140439197, cdnaCodingStart=701, cdnaCodingEnd=846, cdsStart=585, cdsEnd=730, phase=1, exonNumber=3, sequence='null'}, Exon{id='NM_001160372.4_4', chromosome='8', start=140435112, end=140435240, strand='-', genomicCodingStart=140435112, genomicCodingEnd=140435240, cdnaCodingStart=847, cdnaCodingEnd=975, cdsStart=731, cdsEnd=859, phase=2, exonNumber=4, sequence='null'}, Exon{id='NM_001160372.4_5', chromosome='8', start=140426615, end=140426641, strand='-', genomicCodingStart=140426615, genomicCodingEnd=140426641, cdnaCodingStart=976, cdnaCodingEnd=1002, cdsStart=860, cdsEnd=886, phase=2, exonNumber=5, sequence='null'}, Exon{id='NM_001160372.4_6', chromosome='8', start=140405577, end=140405698, strand='-', genomicCodingStart=140405577, genomicCodingEnd=140405698, cdnaCodingStart=1003, cdnaCodingEnd=1124, cdsStart=887, cdsEnd=1008, phase=2, exonNumber=6, sequence='null'}, Exon{id='NM_001160372.4_7', chromosome='8', start=140397620, end=140397745, strand='-', genomicCodingStart=140397620, genomicCodingEnd=140397745, cdnaCodingStart=1125, cdnaCodingEnd=1250, cdsStart=1009, cdsEnd=1134, phase=0, exonNumber=7, sequence='null'}, Exon{id='NM_001160372.4_8', chromosome='8', start=140370964, end=140371180, strand='-', genomicCodingStart=140370964, genomicCodingEnd=140371180, cdnaCodingStart=1251, cdnaCodingEnd=1467, cdsStart=1135, cdsEnd=1351, phase=0, exonNumber=8, sequence='null'}, Exon{id='NM_001160372.4_9', chromosome='8', start=140360050, end=140360193, strand='-', genomicCodingStart=140360050, genomicCodingEnd=140360193, cdnaCodingStart=1468, cdnaCodingEnd=1611, cdsStart=1352, cdsEnd=1495, phase=2, exonNumber=9, sequence='null'}, Exon{id='NM_001160372.4_10', chromosome='8', start=140311248, end=140311374, strand='-', genomicCodingStart=140311248, genomicCodingEnd=140311374, cdnaCodingStart=1612, cdnaCodingEnd=1738, cdsStart=1496, cdsEnd=1622, phase=2, exonNumber=10, sequence='null'}, Exon{id='NM_001160372.4_11', chromosome='8', start=140300469, end=140300614, strand='-', genomicCodingStart=140300469, genomicCodingEnd=140300614, cdnaCodingStart=1739, cdnaCodingEnd=1884, cdsStart=1623, cdsEnd=1768, phase=1, exonNumber=11, sequence='null'}, Exon{id='NM_001160372.4_12', chromosome='8', start=140290993, end=140291078, strand='-', genomicCodingStart=140290993, genomicCodingEnd=140291078, cdnaCodingStart=1885, cdnaCodingEnd=1970, cdsStart=1769, cdsEnd=1854, phase=2, exonNumber=12, sequence='null'}, Exon{id='NM_001160372.4_13', chromosome='8', start=140287608, end=140287734, strand='-', genomicCodingStart=140287608, genomicCodingEnd=140287734, cdnaCodingStart=1971, cdnaCodingEnd=2097, cdsStart=1855, cdsEnd=1981, phase=0, exonNumber=13, sequence='null'}, Exon{id='NM_001160372.4_14', chromosome='8', start=140283889, end=140284021, strand='-', genomicCodingStart=140283889, genomicCodingEnd=140284021, cdnaCodingStart=2098, cdnaCodingEnd=2230, cdsStart=1982, cdsEnd=2114, phase=2, exonNumber=14, sequence='null'}, Exon{id='NM_001160372.4_15', chromosome='8', start=140275658, end=140275821, strand='-', genomicCodingStart=140275658, genomicCodingEnd=140275821, cdnaCodingStart=2231, cdnaCodingEnd=2394, cdsStart=2115, cdsEnd=2278, phase=1, exonNumber=15, sequence='null'}, Exon{id='NM_001160372.4_16', chromosome='8', start=140252777, end=140252929, strand='-', genomicCodingStart=140252777, genomicCodingEnd=140252929, cdnaCodingStart=2395, cdnaCodingEnd=2547, cdsStart=2279, cdsEnd=2431, phase=2, exonNumber=16, sequence='null'}, Exon{id='NM_001160372.4_17', chromosome='8', start=140221459, end=140221583, strand='-', genomicCodingStart=140221459, genomicCodingEnd=140221583, cdnaCodingStart=2548, cdnaCodingEnd=2672, cdsStart=2432, cdsEnd=2556, phase=2, exonNumber=17, sequence='null'}, Exon{id='NM_001160372.4_18', chromosome='8', start=140023937, end=140024079, strand='-', genomicCodingStart=140023937, genomicCodingEnd=140024079, cdnaCodingStart=2673, cdnaCodingEnd=2815, cdsStart=2557, cdsEnd=2699, phase=0, exonNumber=18, sequence='null'}, Exon{id='NM_001160372.4_19', chromosome='8', start=139988726, end=139988836, strand='-', genomicCodingStart=139988726, genomicCodingEnd=139988836, cdnaCodingStart=2816, cdnaCodingEnd=2926, cdsStart=2700, cdsEnd=2810, phase=1, exonNumber=19, sequence='null'}, Exon{id='NM_001160372.4_20', chromosome='8', start=139910147, end=139910300, strand='-', genomicCodingStart=139910147, genomicCodingEnd=139910300, cdnaCodingStart=2927, cdnaCodingEnd=3080, cdsStart=2811, cdsEnd=2964, phase=1, exonNumber=20, sequence='null'}, Exon{id='NM_001160372.4_21', chromosome='8', start=139885879, end=139885969, strand='-', genomicCodingStart=139885879, genomicCodingEnd=139885969, cdnaCodingStart=3081, cdnaCodingEnd=3171, cdsStart=2965, cdsEnd=3055, phase=0, exonNumber=21, sequence='null'}, Exon{id='NM_001160372.4_22', chromosome='8', start=139731979, end=139732202, strand='-', genomicCodingStart=139731979, genomicCodingEnd=139732202, cdnaCodingStart=3172, cdnaCodingEnd=3395, cdsStart=3056, cdsEnd=3279, phase=2, exonNumber=22, sequence='null'}, Exon{id='NM_001160372.4_23', chromosome='8', start=139727725, end=139731228, strand='-', genomicCodingStart=139731061, genomicCodingEnd=139731228, cdnaCodingStart=3396, cdnaCodingEnd=3563, cdsStart=3280, cdsEnd=3447, phase=0, exonNumber=23, sequence='null'}]";
        assertEquals(expected, transcript.getExons().toString());

        // NO CDSs [+] WASH7P
        // NO CDSs [-] DDX11L1
        gene = getGene("WASH7P", parsedGeneList);
        transcript = getTranscript(gene,"NR_024540.1");
        assertEquals(0, transcript.getGenomicCodingStart());
        assertEquals(0, transcript.getGenomicCodingEnd());
        assertEquals(0, transcript.getCdnaCodingStart());
        assertEquals(0, transcript.getCdnaCodingEnd());
        assertEquals(0, transcript.getCdsLength());

        assertEquals(11, transcript.getExons().size());

        expected = "[Exon{id='NR_024540.1_1', chromosome='1', start=29321, end=29370, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=1, sequence='null'}, Exon{id='NR_024540.1_2', chromosome='1', start=24738, end=24891, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=2, sequence='null'}, Exon{id='NR_024540.1_3', chromosome='1', start=18268, end=18366, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=3, sequence='null'}, Exon{id='NR_024540.1_4', chromosome='1', start=17915, end=18061, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=4, sequence='null'}, Exon{id='NR_024540.1_5', chromosome='1', start=17606, end=17742, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=5, sequence='null'}, Exon{id='NR_024540.1_6', chromosome='1', start=17233, end=17368, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=6, sequence='null'}, Exon{id='NR_024540.1_7', chromosome='1', start=16858, end=17055, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=7, sequence='null'}, Exon{id='NR_024540.1_8', chromosome='1', start=16607, end=16765, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=8, sequence='null'}, Exon{id='NR_024540.1_9', chromosome='1', start=15796, end=15947, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=9, sequence='null'}, Exon{id='NR_024540.1_10', chromosome='1', start=14970, end=15038, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=10, sequence='null'}, Exon{id='NR_024540.1_11', chromosome='1', start=14362, end=14829, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=11, sequence='null'}]";
        assertEquals(expected, transcript.getExons().toString());

        gene = getGene("DDX11L1", parsedGeneList);
        transcript = getTranscript(gene,"NR_046018.2");
        assertEquals(0, transcript.getGenomicCodingStart());
        assertEquals(0, transcript.getGenomicCodingEnd());
        assertEquals(0, transcript.getCdnaCodingStart());
        assertEquals(0, transcript.getCdnaCodingEnd());
        assertEquals(0, transcript.getCdsLength());

        assertEquals(3, transcript.getExons().size());

        expected = "[Exon{id='NR_046018.2_1', chromosome='1', start=11874, end=12227, strand='+', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=1, sequence='null'}, Exon{id='NR_046018.2_2', chromosome='1', start=12613, end=12721, strand='+', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=2, sequence='null'}, Exon{id='NR_046018.2_3', chromosome='1', start=13221, end=14409, strand='+', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=3, sequence='null'}]";
        assertEquals(expected, transcript.getExons().toString());

        // double stop codon [+] :: NM_024813.3 - RPAP2
        // double stop codon [-] :: NM_018159.4 - NUDT11
        gene = getGene("RPAP2", parsedGeneList);
        transcript = getTranscript(gene,"NM_024813.3");
        assertEquals(92299074, transcript.getGenomicCodingStart());
        assertEquals(92387011, transcript.getGenomicCodingEnd());
        assertEquals(16, transcript.getCdnaCodingStart());
        assertEquals(1854, transcript.getCdnaCodingEnd());
        assertEquals(1838, transcript.getCdsLength());

        assertEquals(13, transcript.getExons().size());

        expected = "[Exon{id='NM_024813.3_1', chromosome='1', start=92299059, end=92299146, strand='+', genomicCodingStart=92299074, genomicCodingEnd=92299146, cdnaCodingStart=16, cdnaCodingEnd=88, cdsStart=1, cdsEnd=73, phase=0, exonNumber=1, sequence='null'}, Exon{id='NM_024813.3_2', chromosome='1', start=92300194, end=92300239, strand='+', genomicCodingStart=92300194, genomicCodingEnd=92300239, cdnaCodingStart=89, cdnaCodingEnd=134, cdsStart=74, cdsEnd=119, phase=2, exonNumber=2, sequence='null'}, Exon{id='NM_024813.3_3', chromosome='1', start=92301476, end=92301590, strand='+', genomicCodingStart=92301476, genomicCodingEnd=92301590, cdnaCodingStart=135, cdnaCodingEnd=249, cdsStart=120, cdsEnd=234, phase=1, exonNumber=3, sequence='null'}, Exon{id='NM_024813.3_4', chromosome='1', start=92303977, end=92304075, strand='+', genomicCodingStart=92303977, genomicCodingEnd=92304075, cdnaCodingStart=250, cdnaCodingEnd=348, cdsStart=235, cdsEnd=333, phase=0, exonNumber=4, sequence='null'}, Exon{id='NM_024813.3_5', chromosome='1', start=92304284, end=92304349, strand='+', genomicCodingStart=92304284, genomicCodingEnd=92304349, cdnaCodingStart=349, cdnaCodingEnd=414, cdsStart=334, cdsEnd=399, phase=0, exonNumber=5, sequence='null'}, Exon{id='NM_024813.3_6', chromosome='1', start=92307188, end=92307276, strand='+', genomicCodingStart=92307188, genomicCodingEnd=92307276, cdnaCodingStart=415, cdnaCodingEnd=503, cdsStart=400, cdsEnd=488, phase=0, exonNumber=6, sequence='null'}, Exon{id='NM_024813.3_7', chromosome='1', start=92320599, end=92320634, strand='+', genomicCodingStart=92320599, genomicCodingEnd=92320634, cdnaCodingStart=504, cdnaCodingEnd=539, cdsStart=489, cdsEnd=524, phase=1, exonNumber=7, sequence='null'}, Exon{id='NM_024813.3_8', chromosome='1', start=92323445, end=92324375, strand='+', genomicCodingStart=92323445, genomicCodingEnd=92324375, cdnaCodingStart=540, cdnaCodingEnd=1470, cdsStart=525, cdsEnd=1455, phase=1, exonNumber=8, sequence='null'}, Exon{id='NM_024813.3_9', chromosome='1', start=92333391, end=92333473, strand='+', genomicCodingStart=92333391, genomicCodingEnd=92333473, cdnaCodingStart=1471, cdnaCodingEnd=1553, cdsStart=1456, cdsEnd=1538, phase=0, exonNumber=9, sequence='null'}, Exon{id='NM_024813.3_10', chromosome='1', start=92336347, end=92336427, strand='+', genomicCodingStart=92336347, genomicCodingEnd=92336427, cdnaCodingStart=1554, cdnaCodingEnd=1634, cdsStart=1539, cdsEnd=1619, phase=1, exonNumber=10, sequence='null'}, Exon{id='NM_024813.3_11', chromosome='1', start=92345846, end=92345914, strand='+', genomicCodingStart=92345846, genomicCodingEnd=92345914, cdnaCodingStart=1635, cdnaCodingEnd=1703, cdsStart=1620, cdsEnd=1688, phase=1, exonNumber=11, sequence='null'}, Exon{id='NM_024813.3_12', chromosome='1', start=92380724, end=92380873, strand='+', genomicCodingStart=92380724, genomicCodingEnd=92380873, cdnaCodingStart=1704, cdnaCodingEnd=1853, cdsStart=1689, cdsEnd=1838, phase=1, exonNumber=12, sequence='null'}, Exon{id='NM_024813.3_13', chromosome='1', start=92387011, end=92402056, strand='+', genomicCodingStart=92387011, genomicCodingEnd=92387011, cdnaCodingStart=1854, cdnaCodingEnd=1854, cdsStart=1839, cdsEnd=1839, phase=-1, exonNumber=13, sequence='null'}]";
        assertEquals(expected, transcript.getExons().toString());

        gene = getGene("NUDT11", parsedGeneList);
        transcript = getTranscript(gene,"NM_018159.4");
        assertEquals(51491749, transcript.getGenomicCodingStart());
        assertEquals(51496444, transcript.getGenomicCodingEnd());
        assertEquals(149, transcript.getCdnaCodingStart());
        assertEquals(643, transcript.getCdnaCodingEnd());
        assertEquals(494, transcript.getCdsLength());

        assertEquals(2, transcript.getExons().size());

        expected = "[Exon{id='NM_018159.4_1', chromosome='X', start=51495951, end=51496592, strand='-', genomicCodingStart=51495951, genomicCodingEnd=51496444, cdnaCodingStart=149, cdnaCodingEnd=642, cdsStart=1, cdsEnd=494, phase=0, exonNumber=1, sequence='null'}, Exon{id='NM_018159.4_2', chromosome='X', start=51490011, end=51491749, strand='-', genomicCodingStart=51490011, genomicCodingEnd=51490011, cdnaCodingStart=643, cdnaCodingEnd=643, cdsStart=495, cdsEnd=495, phase=-1, exonNumber=2, sequence='null'}]";
        assertEquals(expected, transcript.getExons().toString());

        // stop codon is not in CDS [+] :: NM_001135638.2 - PIP5K1A
        // stop codon is not in CDS [-] :: NM_198721.4 - COL25A1
        gene = getGene("PIP5K1A", parsedGeneList);
        transcript = getTranscript(gene,"NM_001135638.2");
        assertEquals(151198997, transcript.getGenomicCodingStart());
        assertEquals(151247865, transcript.getGenomicCodingEnd());
        assertEquals(446, transcript.getCdnaCodingStart());
        assertEquals(2134, transcript.getCdnaCodingEnd());
        assertEquals(1688, transcript.getCdsLength());

        assertEquals(16, transcript.getExons().size());

        expected = "[Exon{id='NM_001135638.2_1', chromosome='1', start=151198552, end=151199081, strand='+', genomicCodingStart=151198997, genomicCodingEnd=151199081, cdnaCodingStart=446, cdnaCodingEnd=530, cdsStart=1, cdsEnd=85, phase=0, exonNumber=1, sequence='null'}, Exon{id='NM_001135638.2_2', chromosome='1', start=151224245, end=151224279, strand='+', genomicCodingStart=151224245, genomicCodingEnd=151224279, cdnaCodingStart=531, cdnaCodingEnd=565, cdsStart=86, cdsEnd=120, phase=2, exonNumber=2, sequence='null'}, Exon{id='NM_001135638.2_3', chromosome='1', start=151224371, end=151224406, strand='+', genomicCodingStart=151224371, genomicCodingEnd=151224406, cdnaCodingStart=566, cdnaCodingEnd=601, cdsStart=121, cdsEnd=156, phase=0, exonNumber=3, sequence='null'}, Exon{id='NM_001135638.2_4', chromosome='1', start=151227320, end=151227400, strand='+', genomicCodingStart=151227320, genomicCodingEnd=151227400, cdnaCodingStart=602, cdnaCodingEnd=682, cdsStart=157, cdsEnd=237, phase=0, exonNumber=4, sequence='null'}, Exon{id='NM_001135638.2_5', chromosome='1', start=151231671, end=151231801, strand='+', genomicCodingStart=151231671, genomicCodingEnd=151231801, cdnaCodingStart=683, cdnaCodingEnd=813, cdsStart=238, cdsEnd=368, phase=0, exonNumber=5, sequence='null'}, Exon{id='NM_001135638.2_6', chromosome='1', start=151232248, end=151232365, strand='+', genomicCodingStart=151232248, genomicCodingEnd=151232365, cdnaCodingStart=814, cdnaCodingEnd=931, cdsStart=369, cdsEnd=486, phase=1, exonNumber=6, sequence='null'}, Exon{id='NM_001135638.2_7', chromosome='1', start=151232551, end=151232703, strand='+', genomicCodingStart=151232551, genomicCodingEnd=151232703, cdnaCodingStart=932, cdnaCodingEnd=1084, cdsStart=487, cdsEnd=639, phase=0, exonNumber=7, sequence='null'}, Exon{id='NM_001135638.2_8', chromosome='1', start=151234197, end=151234496, strand='+', genomicCodingStart=151234197, genomicCodingEnd=151234496, cdnaCodingStart=1085, cdnaCodingEnd=1384, cdsStart=640, cdsEnd=939, phase=0, exonNumber=8, sequence='null'}, Exon{id='NM_001135638.2_9', chromosome='1', start=151236558, end=151236763, strand='+', genomicCodingStart=151236558, genomicCodingEnd=151236763, cdnaCodingStart=1385, cdnaCodingEnd=1590, cdsStart=940, cdsEnd=1145, phase=0, exonNumber=9, sequence='null'}, Exon{id='NM_001135638.2_10', chromosome='1', start=151238182, end=151238265, strand='+', genomicCodingStart=151238182, genomicCodingEnd=151238265, cdnaCodingStart=1591, cdnaCodingEnd=1674, cdsStart=1146, cdsEnd=1229, phase=1, exonNumber=10, sequence='null'}, Exon{id='NM_001135638.2_11', chromosome='1', start=151239130, end=151239178, strand='+', genomicCodingStart=151239130, genomicCodingEnd=151239178, cdnaCodingStart=1675, cdnaCodingEnd=1723, cdsStart=1230, cdsEnd=1278, phase=1, exonNumber=11, sequence='null'}, Exon{id='NM_001135638.2_12', chromosome='1', start=151239955, end=151240039, strand='+', genomicCodingStart=151239955, genomicCodingEnd=151240039, cdnaCodingStart=1724, cdnaCodingEnd=1808, cdsStart=1279, cdsEnd=1363, phase=0, exonNumber=12, sequence='null'}, Exon{id='NM_001135638.2_13', chromosome='1', start=151242123, end=151242269, strand='+', genomicCodingStart=151242123, genomicCodingEnd=151242269, cdnaCodingStart=1809, cdnaCodingEnd=1955, cdsStart=1364, cdsEnd=1510, phase=2, exonNumber=13, sequence='null'}, Exon{id='NM_001135638.2_14', chromosome='1', start=151242438, end=151242567, strand='+', genomicCodingStart=151242438, genomicCodingEnd=151242567, cdnaCodingStart=1956, cdnaCodingEnd=2085, cdsStart=1511, cdsEnd=1640, phase=2, exonNumber=14, sequence='null'}, Exon{id='NM_001135638.2_15', chromosome='1', start=151246920, end=151246965, strand='+', genomicCodingStart=151246920, genomicCodingEnd=151246965, cdnaCodingStart=2086, cdnaCodingEnd=2131, cdsStart=1641, cdsEnd=1686, phase=1, exonNumber=15, sequence='null'}, Exon{id='NM_001135638.2_16', chromosome='1', start=151247863, end=151249531, strand='+', genomicCodingStart=151247863, genomicCodingEnd=151247865, cdnaCodingStart=2132, cdnaCodingEnd=2134, cdsStart=1687, cdsEnd=1689, phase=-1, exonNumber=16, sequence='null'}]";
        assertEquals(expected, transcript.getExons().toString());

        gene = getGene("COL25A1", parsedGeneList);
        transcript = getTranscript(gene,"NM_198721.4");
        assertEquals(108813927, transcript.getGenomicCodingStart());
        assertEquals(109302019, transcript.getGenomicCodingEnd());
        assertEquals(547, transcript.getCdnaCodingStart());
        assertEquals(2511, transcript.getCdnaCodingEnd());
        assertEquals(1964, transcript.getCdsLength());

        assertEquals(38, transcript.getExons().size());

        expected = "[Exon{id='NM_198721.4_1', chromosome='4', start=109302169, end=109302658, strand='-', genomicCodingStart=0, genomicCodingEnd=0, cdnaCodingStart=0, cdnaCodingEnd=0, cdsStart=0, cdsEnd=0, phase=-1, exonNumber=1, sequence='null'}, Exon{id='NM_198721.4_2', chromosome='4', start=109301723, end=109302075, strand='-', genomicCodingStart=109301723, genomicCodingEnd=109302019, cdnaCodingStart=547, cdnaCodingEnd=843, cdsStart=1, cdsEnd=297, phase=0, exonNumber=2, sequence='null'}, Exon{id='NM_198721.4_3', chromosome='4', start=109300583, end=109300652, strand='-', genomicCodingStart=109300583, genomicCodingEnd=109300652, cdnaCodingStart=844, cdnaCodingEnd=913, cdsStart=298, cdsEnd=367, phase=0, exonNumber=3, sequence='null'}, Exon{id='NM_198721.4_4', chromosome='4', start=109050135, end=109050179, strand='-', genomicCodingStart=109050135, genomicCodingEnd=109050179, cdnaCodingStart=914, cdnaCodingEnd=958, cdsStart=368, cdsEnd=412, phase=2, exonNumber=4, sequence='null'}, Exon{id='NM_198721.4_5', chromosome='4', start=109048168, end=109048175, strand='-', genomicCodingStart=109048168, genomicCodingEnd=109048175, cdnaCodingStart=959, cdnaCodingEnd=966, cdsStart=413, cdsEnd=420, phase=2, exonNumber=5, sequence='null'}, Exon{id='NM_198721.4_6', chromosome='4', start=109010358, end=109010375, strand='-', genomicCodingStart=109010358, genomicCodingEnd=109010375, cdnaCodingStart=967, cdnaCodingEnd=984, cdsStart=421, cdsEnd=438, phase=0, exonNumber=6, sequence='null'}, Exon{id='NM_198721.4_7', chromosome='4', start=108974533, end=108974559, strand='-', genomicCodingStart=108974533, genomicCodingEnd=108974559, cdnaCodingStart=985, cdnaCodingEnd=1011, cdsStart=439, cdsEnd=465, phase=0, exonNumber=7, sequence='null'}, Exon{id='NM_198721.4_8', chromosome='4', start=108974367, end=108974393, strand='-', genomicCodingStart=108974367, genomicCodingEnd=108974393, cdnaCodingStart=1012, cdnaCodingEnd=1038, cdsStart=466, cdsEnd=492, phase=0, exonNumber=8, sequence='null'}, Exon{id='NM_198721.4_9', chromosome='4', start=108941366, end=108941437, strand='-', genomicCodingStart=108941366, genomicCodingEnd=108941437, cdnaCodingStart=1039, cdnaCodingEnd=1110, cdsStart=493, cdsEnd=564, phase=0, exonNumber=9, sequence='null'}, Exon{id='NM_198721.4_10', chromosome='4', start=108940539, end=108940646, strand='-', genomicCodingStart=108940539, genomicCodingEnd=108940646, cdnaCodingStart=1111, cdnaCodingEnd=1218, cdsStart=565, cdsEnd=672, phase=0, exonNumber=10, sequence='null'}, Exon{id='NM_198721.4_11', chromosome='4', start=108937808, end=108937843, strand='-', genomicCodingStart=108937808, genomicCodingEnd=108937843, cdnaCodingStart=1219, cdnaCodingEnd=1254, cdsStart=673, cdsEnd=708, phase=0, exonNumber=11, sequence='null'}, Exon{id='NM_198721.4_12', chromosome='4', start=108920578, end=108920604, strand='-', genomicCodingStart=108920578, genomicCodingEnd=108920604, cdnaCodingStart=1255, cdnaCodingEnd=1281, cdsStart=709, cdsEnd=735, phase=0, exonNumber=12, sequence='null'}, Exon{id='NM_198721.4_13', chromosome='4', start=108918172, end=108918216, strand='-', genomicCodingStart=108918172, genomicCodingEnd=108918216, cdnaCodingStart=1282, cdnaCodingEnd=1326, cdsStart=736, cdsEnd=780, phase=0, exonNumber=13, sequence='null'}, Exon{id='NM_198721.4_14', chromosome='4', start=108901119, end=108901172, strand='-', genomicCodingStart=108901119, genomicCodingEnd=108901172, cdnaCodingStart=1327, cdnaCodingEnd=1380, cdsStart=781, cdsEnd=834, phase=0, exonNumber=14, sequence='null'}, Exon{id='NM_198721.4_15', chromosome='4', start=108899154, end=108899180, strand='-', genomicCodingStart=108899154, genomicCodingEnd=108899180, cdnaCodingStart=1381, cdnaCodingEnd=1407, cdsStart=835, cdsEnd=861, phase=0, exonNumber=15, sequence='null'}, Exon{id='NM_198721.4_16', chromosome='4', start=108896667, end=108896711, strand='-', genomicCodingStart=108896667, genomicCodingEnd=108896711, cdnaCodingStart=1408, cdnaCodingEnd=1452, cdsStart=862, cdsEnd=906, phase=0, exonNumber=16, sequence='null'}, Exon{id='NM_198721.4_17', chromosome='4', start=108889701, end=108889733, strand='-', genomicCodingStart=108889701, genomicCodingEnd=108889733, cdnaCodingStart=1453, cdnaCodingEnd=1485, cdsStart=907, cdsEnd=939, phase=0, exonNumber=17, sequence='null'}, Exon{id='NM_198721.4_18', chromosome='4', start=108889221, end=108889256, strand='-', genomicCodingStart=108889221, genomicCodingEnd=108889256, cdnaCodingStart=1486, cdnaCodingEnd=1521, cdsStart=940, cdsEnd=975, phase=0, exonNumber=18, sequence='null'}, Exon{id='NM_198721.4_19', chromosome='4', start=108884178, end=108884222, strand='-', genomicCodingStart=108884178, genomicCodingEnd=108884222, cdnaCodingStart=1522, cdnaCodingEnd=1566, cdsStart=976, cdsEnd=1020, phase=0, exonNumber=19, sequence='null'}, Exon{id='NM_198721.4_20', chromosome='4', start=108869088, end=108869150, strand='-', genomicCodingStart=108869088, genomicCodingEnd=108869150, cdnaCodingStart=1567, cdnaCodingEnd=1629, cdsStart=1021, cdsEnd=1083, phase=0, exonNumber=20, sequence='null'}, Exon{id='NM_198721.4_21', chromosome='4', start=108863319, end=108863387, strand='-', genomicCodingStart=108863319, genomicCodingEnd=108863387, cdnaCodingStart=1630, cdnaCodingEnd=1698, cdsStart=1084, cdsEnd=1152, phase=0, exonNumber=21, sequence='null'}, Exon{id='NM_198721.4_22', chromosome='4', start=108862501, end=108862545, strand='-', genomicCodingStart=108862501, genomicCodingEnd=108862545, cdnaCodingStart=1699, cdnaCodingEnd=1743, cdsStart=1153, cdsEnd=1197, phase=0, exonNumber=22, sequence='null'}, Exon{id='NM_198721.4_23', chromosome='4', start=108860927, end=108860971, strand='-', genomicCodingStart=108860927, genomicCodingEnd=108860971, cdnaCodingStart=1744, cdnaCodingEnd=1788, cdsStart=1198, cdsEnd=1242, phase=0, exonNumber=23, sequence='null'}, Exon{id='NM_198721.4_24', chromosome='4', start=108859656, end=108859733, strand='-', genomicCodingStart=108859656, genomicCodingEnd=108859733, cdnaCodingStart=1789, cdnaCodingEnd=1866, cdsStart=1243, cdsEnd=1320, phase=0, exonNumber=24, sequence='null'}, Exon{id='NM_198721.4_25', chromosome='4', start=108852902, end=108852925, strand='-', genomicCodingStart=108852902, genomicCodingEnd=108852925, cdnaCodingStart=1867, cdnaCodingEnd=1890, cdsStart=1321, cdsEnd=1344, phase=0, exonNumber=25, sequence='null'}, Exon{id='NM_198721.4_26', chromosome='4', start=108852236, end=108852280, strand='-', genomicCodingStart=108852236, genomicCodingEnd=108852280, cdnaCodingStart=1891, cdnaCodingEnd=1935, cdsStart=1345, cdsEnd=1389, phase=0, exonNumber=26, sequence='null'}, Exon{id='NM_198721.4_27', chromosome='4', start=108848759, end=108848803, strand='-', genomicCodingStart=108848759, genomicCodingEnd=108848803, cdnaCodingStart=1936, cdnaCodingEnd=1980, cdsStart=1390, cdsEnd=1434, phase=0, exonNumber=27, sequence='null'}, Exon{id='NM_198721.4_28', chromosome='4', start=108846139, end=108846219, strand='-', genomicCodingStart=108846139, genomicCodingEnd=108846219, cdnaCodingStart=1981, cdnaCodingEnd=2061, cdsStart=1435, cdsEnd=1515, phase=0, exonNumber=28, sequence='null'}, Exon{id='NM_198721.4_29', chromosome='4', start=108845189, end=108845251, strand='-', genomicCodingStart=108845189, genomicCodingEnd=108845251, cdnaCodingStart=2062, cdnaCodingEnd=2124, cdsStart=1516, cdsEnd=1578, phase=0, exonNumber=29, sequence='null'}, Exon{id='NM_198721.4_30', chromosome='4', start=108844519, end=108844569, strand='-', genomicCodingStart=108844519, genomicCodingEnd=108844569, cdnaCodingStart=2125, cdnaCodingEnd=2175, cdsStart=1579, cdsEnd=1629, phase=0, exonNumber=30, sequence='null'}, Exon{id='NM_198721.4_31', chromosome='4', start=108841695, end=108841721, strand='-', genomicCodingStart=108841695, genomicCodingEnd=108841721, cdnaCodingStart=2176, cdnaCodingEnd=2202, cdsStart=1630, cdsEnd=1656, phase=0, exonNumber=31, sequence='null'}, Exon{id='NM_198721.4_32', chromosome='4', start=108832380, end=108832433, strand='-', genomicCodingStart=108832380, genomicCodingEnd=108832433, cdnaCodingStart=2203, cdnaCodingEnd=2256, cdsStart=1657, cdsEnd=1710, phase=0, exonNumber=32, sequence='null'}, Exon{id='NM_198721.4_33', chromosome='4', start=108827135, end=108827188, strand='-', genomicCodingStart=108827135, genomicCodingEnd=108827188, cdnaCodingStart=2257, cdnaCodingEnd=2310, cdsStart=1711, cdsEnd=1764, phase=0, exonNumber=33, sequence='null'}, Exon{id='NM_198721.4_34', chromosome='4', start=108825196, end=108825222, strand='-', genomicCodingStart=108825196, genomicCodingEnd=108825222, cdnaCodingStart=2311, cdnaCodingEnd=2337, cdsStart=1765, cdsEnd=1791, phase=0, exonNumber=34, sequence='null'}, Exon{id='NM_198721.4_35', chromosome='4', start=108824174, end=108824227, strand='-', genomicCodingStart=108824174, genomicCodingEnd=108824227, cdnaCodingStart=2338, cdnaCodingEnd=2391, cdsStart=1792, cdsEnd=1845, phase=0, exonNumber=35, sequence='null'}, Exon{id='NM_198721.4_36', chromosome='4', start=108819252, end=108819329, strand='-', genomicCodingStart=108819252, genomicCodingEnd=108819329, cdnaCodingStart=2392, cdnaCodingEnd=2469, cdsStart=1846, cdsEnd=1923, phase=0, exonNumber=36, sequence='null'}, Exon{id='NM_198721.4_37', chromosome='4', start=108817397, end=108817435, strand='-', genomicCodingStart=108817397, genomicCodingEnd=108817435, cdnaCodingStart=2470, cdnaCodingEnd=2508, cdsStart=1924, cdsEnd=1962, phase=0, exonNumber=37, sequence='null'}, Exon{id='NM_198721.4_38', chromosome='4', start=108808725, end=108813929, strand='-', genomicCodingStart=108813927, genomicCodingEnd=108813929, cdnaCodingStart=2509, cdnaCodingEnd=2511, cdsStart=1963, cdsEnd=1965, phase=-1, exonNumber=38, sequence='null'}]";
        assertEquals(expected, transcript.getExons().toString());
    }




    private Xref getXref(Transcript transcript, String xrefId) {
        for (Xref xref : transcript.getXrefs()) {
            if (xref.getId().equals(xrefId)) {
                return xref;
            }
        }
        return null;
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
            if (id.equals(gene.getName())) {
                return gene;
            }
        }
        return null;
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
}
