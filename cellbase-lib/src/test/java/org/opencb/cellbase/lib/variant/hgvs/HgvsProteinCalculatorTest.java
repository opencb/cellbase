package org.opencb.cellbase.lib.variant.hgvs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.TestInstance;
import org.mortbay.util.ajax.JSON;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * All test cases are:
 *
 * grch38
 * ensembl v99
 * tested with cellbase 5
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HgvsProteinCalculatorTest {

    private ObjectMapper jsonObjectMapper;
    List<Gene> geneList;

    public HgvsProteinCalculatorTest() throws IOException {

    }

    // TODO add KeyError: '1:244856830:T:-', generated an error in the python script

    @BeforeAll
    public void setUp() throws IOException {

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        geneList = loadGenes(Paths.get(getClass().getResource("/hgvs/gene_grch38.test.json.gz").getFile()));
    }

    @Test
    public void testCellBaseEmpty() throws Exception {
        // cellbase not reporting this one:
        // 12:51807429:G:C

        //12:51807429:G:C	ENSP00000347255	p.Ter1940TyrextTer?	-	cb_empty	9409
        //12:51807429:G:C	ENSP00000487583	p.Ter1981TyrextTer?	-	cb_empty	9409
        //12:51807429:G:C	ENSP00000476447	p.Ter1992TyrextTer?	-	cb_empty	9409

        Gene gene = getGene("ENSG00000196876");
        Transcript transcript = getTranscript(gene, "ENST00000355133");
        Variant variant = new Variant("12",
                51807429,
                "G",
                "C");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        Assert.assertEquals("p.Ter1940TyrextTer?", hgvsProtein.getHgvs());

    }

    // investigating why these are failing
    // @Test
    public void testCellBaseEmpty() throws Exception {
        Gene gene = null;
        Transcript transcript = null;
        Variant variant = null;
        HgvsProteinCalculator predictor = null;
        HgvsProtein hgvsProtein = null;

        //12:51807429:G:C	ENSP00000347255	p.Ter1940TyrextTer?	-	cb_empty	9409
        //12:51807429:G:C	ENSP00000487583	p.Ter1981TyrextTer?	-	cb_empty	9409
        //12:51807429:G:C	ENSP00000476447	p.Ter1992TyrextTer?	-	cb_empty	9409

        gene = getGene("ENSG00000196876");
        transcript = getTranscript(gene, "ENST00000355133");
        variant = new Variant("12",
                51807429,
                "G",
                "C");

        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        Assert.assertEquals("p.Ter1940TyrextTer?", hgvsProtein.getHgvs());

        gene = getGene("ENSG00000087088");
        transcript = getTranscript(gene, "ENST00000391871");
        variant = new Variant("19",
                48955714,
                "G",
                "-");

        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        Assert.assertEquals("p.Glu24ArgfsTer19", hgvsProtein.getHgvs());

//        16:68812133:AG:-    ENSP00000481063 p.Ser337PhefsTer12  -   cb_empty


        gene = getGene("ENSG00000039068");
        transcript = getTranscript(gene, "ENST00000611625");
        variant = new Variant("16",
                68812133,
                "AG",
                "-");
        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        Assert.assertEquals("p.Ser337PhefsTer12", hgvsProtein.getHgvs());

//        16:11254950:GCTCCTGCAGCGGCCGCACGCGGCG:T ENSP00000329418 p.Arg169_Leu177delinsMet    -   cb_empty

        gene = getGene("ENSG00000185338");
        transcript = getTranscript(gene, "ENST00000332029");
        variant = new Variant("16",
                11254950,
                "GCTCCTGCAGCGGCCGCACGCGGCG",
                "T");
        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        Assert.assertEquals("p.Arg169_Leu177delinsMet", hgvsProtein.getHgvs());

//        13:48362831:CATTAATGGTTCACCTCGAA:T  ENSP00000267163 p.Ile246HisfsTer12  -   cb_empty

        gene = getGene("ENSG00000139687");
        transcript = getTranscript(gene, "ENST00000267163");
        variant = new Variant("13",
                48362831,
                "CATTAATGGTTCACCTCGAA",
                "T");

        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        Assert.assertEquals("p.Ile246HisfsTer12", hgvsProtein.getHgvs());

//        3:53495166:GAT:-    ENSP00000490183 p.Met7del   -   cb_empty
//        3:53495166:GAT:-    ENSP00000409174 p.Met7del   -   cb_empty

        gene = getGene("ENSG00000157388");
        transcript = getTranscript(gene, "ENST00000636570");
        variant = new Variant("3",
                53495166,
                "GAT",
                "-");

        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        Assert.assertEquals("p.Met7del", hgvsProtein.getHgvs());
    }

    // investigating why these are failing
    //@Test
    public void testMoreMismatches() throws Exception {
        // from Antonio
        Gene gene = null;
        Transcript transcript = null;
        Variant variant = null;
        HgvsProteinCalculator predictor = null;
        HgvsProtein hgvsProtein = null;



//        var_id  entity_id   vep_hgvs    cb_hgvs mismatch_category
//        MT:8247:-:CCC   ENSP00000354876 p.Met221delinsThrLeu    p.Glu220_Met221delinsPro    other

        gene = getGene("ENSG00000198712");
        transcript = getTranscript(gene, "ENST00000361739");
        variant = new Variant("MT",
                8247,
                "-",
                "CCC");

        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        Assert.assertEquals("p.Met221delinsThrLeu", hgvsProtein.getHgvs());

//        11:32396362:-:CCGA  ENSP00000491984 p.Ala382ValfsTer4   p.Met1LeufsTer385   other
//        11:32396362:-:CCGA  ENSP00000491511 p.Ala382ValfsTer4   p.Met1LeufsTer385   other
//        11:32396362:-:CCGA  ENSP00000492269 p.Ala365ValfsTer4   p.Met1LeufsTer368   other

        gene = getGene("ENSG00000184937");
        transcript = getTranscript(gene, "ENST00000640146");
        variant = new Variant("11",
                32396362,
                "-",
                "CCGA");

        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        Assert.assertEquals("p.Ala382ValfsTer4", hgvsProtein.getHgvs());

        //        11:32434945:G:- ENSP00000491984 p.Pro134ArgfsTer24  p.Met1LeufsTer157   other
//        11:32434945:G:- ENSP00000491511 p.Pro134ArgfsTer24  p.Met1LeufsTer157   other
//        11:32434945:G:- ENSP00000492269 p.Pro134ArgfsTer24  p.Met1LeufsTer157   other

        gene = getGene("ENSG00000184937");
        transcript = getTranscript(gene, "ENST00000640146");
        variant = new Variant("11",
                32434945,
                "G",
                "-");

        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        Assert.assertEquals("p.Pro134ArgfsTer24", hgvsProtein.getHgvs());

        //        6:31164655:TTCCCCCTCAGGGAAAGGG:-    ENSP00000484778 p.Glu171LeufsTer?   p.Met1LeufsTer? other

        gene = getGene("ENSG00000204531");
        transcript = getTranscript(gene, "ENST00000620031");
        variant = new Variant("6",
                31164655,
                "TTCCCCCTCAGGGAAAGGG",
                "-");

        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        Assert.assertEquals("p.Glu171LeufsTer?", hgvsProtein.getHgvs());


//        19:48955714:-:G ENSP00000375744 p.Glu24GlyfsTer33   p.Asn21LysfsTer36   other

        gene = getGene("ENSG00000087088");
        transcript = getTranscript(gene, "ENST00000391871");
        variant = new Variant("19",
                48955714,
                "-",
                "G");

        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        Assert.assertEquals("p.Glu24GlyfsTer33", hgvsProtein.getHgvs());

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// INSERTIONS //////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testInsertion() throws Exception {
//9:83970290:-:TGA        9       83970290        -       TGA     indel   ENSP00000365439 p.Lys411_Gln412insSer   p.Lys411_Gln412insSer
        Gene gene = getGene("ENSG00000165119");
        Transcript transcript = getTranscript(gene, "ENST00000376263");
        Variant variant = new Variant("9",
                83970290,
                "-",
                "TGA");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Lys411_Gln412insSer", hgvsProtein.getHgvs());

        gene = getGene("ENSG00000165119");
        transcript = getTranscript(gene, "ENST00000376263");
        variant = new Variant("9",
                83970290,
                "-",
                "CTA");
        predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein1 = predictor.calculate();
        assertEquals("p.Lys411_Gln412insTer", hgvsProtein1.getHgvs());
    }

    @Test
    public void testDup() throws Exception {
//2:51027601:-:CCTCGCCCT  2       51027601        -       CCTCGCCCT       indel   ENSP00000490017 p.Glu75_Glu77dup        p.Gly78Ter      vep_dup_cb_ter
        Gene gene = getGene("ENSG00000179915");
        Transcript transcript = getTranscript(gene, "ENST00000636066");
        Variant variant = new Variant("2",
                51027601,
                "-",
                "CCTCGCCCT");
        // Reverse strand:  AGGGCGAGG
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Glu75_Glu77dup", hgvsProtein.getHgvs());
    }

    @Test
    public void testInsertionPositiveStrandPhase0() throws Exception {
        // 17:18173905:-:A  indel   ENSP00000408800 p.Leu757AlafsTer79      p.Leu757fs      fs_shorthand_same_pos
        // phase 0
        // positive strand
        // confirmed start
        Gene gene = getGene("ENSG00000091536");
        Transcript transcript = getTranscript(gene, "ENST00000418233");
        Variant variant = new Variant("17",
                18173905,
                "-",
                "A");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Leu757AlafsTer79", hgvsProtein.getHgvs());
    }

    @Test
    public void testStopGainedInsertionPositiveStrand() throws Exception {
        // 17:18173905:-:A  indel   ENSP00000408800 p.Leu757AlafsTer79      p.Leu757fs      fs_shorthand_same_pos
        // positive strand, confirmed start
        // Result: ENSP00000408800.3:p.Phe568Ter     why not Phe568delinsTer?
        // http://aug2017.archive.ensembl.org/Homo_sapiens/Variation/Explore?db=core;g=ENSG00000091536;r=17:18153395-18179222;source=dbSNP;t=ENST00000418233;v=rs746666103;vdb=variation;vf=112226266
        // Note: https://rest.ensembl.org/variant_recoder/human/17:18166483::AGCTTCCCCTTTTCCCAGAAG?content-type=application/json
        Gene gene = getGene("ENSG00000091536");
        Transcript transcript = getTranscript(gene, "ENST00000418233");
        Variant variant = new Variant("17",
                18166484,
                "-",
                "AGCTTCCCCTTTTCCCAGAAG");
        //AGCTTCCCCTTTTCCCAGAAGCC
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Phe568Ter", hgvsProtein.getHgvs());
    }

    @Test
    public void testInsertionPositiveStrandPhase0Nonsense() throws Exception {
        // Issue #5 - NonsenseReportedAsFrameShift
        // positive strand
        // phase 0, confirmed start
        //        1:236717940:-:T 1       236717940       -       T       indel   ENSP00000443495 p.Lys71Ter      p.Lys71fs       nonsense_as_fs_same_pos
        Gene gene = getGene("ENSG00000077522");
        Transcript transcript = getTranscript(gene, "ENST00000542672");
        Variant variant = new Variant("1",
                236717940,
                "-",
                "T");
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Lys71Ter", predictor.calculate().getHgvs());

        // This variant is 5' UTR, no HGVS protein returned
        gene = getGene("ENSG00000077522");
        transcript = getTranscript(gene, "ENST00000546208");
        variant = new Variant("1",
                236717940,
                "-",
                "T");
        predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
//        Assert.assertEquals("", hgvsProtein.getHgvs());
        assertNull(hgvsProtein);
    }

    @Test
    public void testInsertionPositiveStrandPhase0FS() throws Exception {
        // Issue #6 - FrameShiftReportedAsDup
        // positive strand
        // phase 0, confirmed start (no flags)
        //4:102582930:-:T 4       102582930       -       T       indel   ENSP00000424790 p.Ser301PhefsTer7       p.Phe300dup     fs_as_dup
        Gene gene = getGene("ENSG00000109320");
        Transcript transcript = getTranscript(gene, "ENST00000505458");
        Variant variant = new Variant("4",
                102582930,
                "-",
                "T");
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        System.out.println("hgvsProtein.getIds() = " + hgvsProtein.getIds());
        assertEquals("p.Ser301PhefsTer7", hgvsProtein.getHgvs());
    }

    @Test
    public void testInsertionPositiveStrandPhase0LargeAlternate() throws Exception {
        // fs_shorthand_same_pos
        // positive strand
        // phase 0
        // confirmed start (no flags)
        // 32102   4:154744350:-:CTTCATGGAAGAACCC  4       154744350       -       CTTCATGGAAGAACCC        indel   ENSP00000426761 p.Val9PhefsTer23        p.Val9fs        fs_shorthand_same_pos
        Gene gene = getGene("ENSG00000121207");
        Transcript transcript = getTranscript(gene, "ENST00000507827");
        Variant variant = new Variant("4",
                154744350,
                "-",
                "CTTCATGGAAGAACCC");
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Val9PhefsTer23", predictor.calculate().getHgvs());
    }

    @Test
    public void testInsertionPositiveStrandPhase2() throws Exception {
        // fs_shorthand_diff_pos
        // positive strand
        // phase 2
        //19:11111569:-:GGGT      19      11111569        -       GGGT    indel   ENSP00000453513 p.Tyr202TrpfsTer7       p.Gly201TrpfsTer41      fs_shorthand_diff_pos
        Gene gene = getGene("ENSG00000130164");
        Transcript transcript = getTranscript(gene, "ENST00000560467");
        Variant variant = new Variant("19",
                11111569,
                "-",
                "GGGT");
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Tyr202TrpfsTer7", predictor.calculate().getHgvs());

        //19:11111569:-:GGGT      19      11111569        -       GGGT    indel   ENSP00000252444 p.Tyr460TrpfsTer7       p.Gly459TrpfsTer41      fs_shorthand_diff_pos
        transcript = getTranscript(gene, "ENST00000252444");
        variant = new Variant("19",
                11111569,
                "-",
                "GGGT");
        predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Tyr460TrpfsTer7", predictor.calculate().getHgvs());
    }

    @Test
    public void testInsReverseStrandPhase2() throws Exception {
        // fs_shorthand_diff_pos
        // reverse strand
        // phase 2
        // unconfirmed start
        //16:2092152:-:GTGT    16    2092152    -    GTGT    indel    ENSP00000461391    p.Cys8HisfsTer?    p.Thr7GlnfsTer176
        Gene gene = getGene("ENSG00000008710");
        Transcript transcript = getTranscript(gene, "ENST00000561668");
        Variant variant = new Variant("16",
                2092152,
                "-",
                "GTGT");    // ACAC
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Cys8HisfsTer?", predictor.calculate().getHgvs());
    }

    @Test
    public void testInsertionInframe() throws Exception {
        // confirmed start, no flags
        // forward strand, rs751051082
        // Results: ENSP00000443495.1:p.Lys499_Arg500insLysLysSer
        Gene gene = getGene("ENSG00000077522");
        Transcript transcript = getTranscript(gene, "ENST00000542672");
        Variant variant = new Variant("1",
                236747759,
                "-",
                "AAAAGAGCC");
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Lys499_Arg500insLysLysSer", hgvsProtein.getHgvs());


        // confirmed start, no flags
        // forward strand
        // Results: ENSP00000355537.4:p.Lys499_Arg500insLysLysGlu and ENSP00000443495.1:p.Lys499_Arg500insLysLysGlu
        // https://rest.ensembl.org/variant_recoder/human/1:236747758::AAAAAGAGC?content-type=application/json
        gene = getGene("ENSG00000077522");
        transcript = getTranscript(gene, "ENST00000542672");
        variant = new Variant("1",
                236747759,
                "-",
                "AAAAAGAGC");
        predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein1 = predictor.calculate();
        assertEquals("p.Lys499_Arg500insLysLysGlu", hgvsProtein1.getHgvs());
        assertThat(hgvsProtein1.getIds(), CoreMatchers.hasItems("ENSP00000443495"));

        // forward strand
        // unconfirmed start
        gene = getGene("ENSG00000130164");
        transcript = getTranscript(gene, "ENST00000560467");
        variant = new Variant("19",
                11105431,
                "-",
                "CTGCGAAGA");
        predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein2 = predictor.calculate();
        assertNotNull(hgvsProtein2);
        assertEquals("p.Cys40_Asp42dup", hgvsProtein2.getHgvs());

        // reverse strand
        // unconfirmed start
        // Result: ENSP00000461391.1:p.Gly31_Thr32dup
        // https://rest.ensembl.org/variant_recoder/human/16:2092075::CGTCCC?content-type=application/json
        gene = getGene("ENSG00000008710");
        transcript = getTranscript(gene, "ENST00000561668");
        variant = new Variant("16",
                2092076,
                "-",
                "CGTCCC");
        predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein3 = predictor.calculate();
        assertNotNull(hgvsProtein3);
        assertEquals("p.Gly31_Thr32dup", hgvsProtein3.getHgvs());
    }

    @Test
    public void testInsertionCdsStartGreaterThanCdsLength() throws Exception {
        // FIXME is 2092113 the right coordinate?
        // 26225   16:2092113:-:TC 16      2092113 -       TC      indel   ENSP00000461391 p.Asp20GlufsTer162      p.Asp20ArgfsTer28       fs_shorthand_same_pos
        // Reverse strand, 5' incomplete
        // Result: ENSP00000461391.1:p.Asp20GlyfsTer162,
        // https://rest.ensembl.org/variant_recoder/human/16:2092113::TC?content-type=application/json
        Gene gene = getGene("ENSG00000008710");
        Transcript transcript = getTranscript(gene, "ENST00000561668");
        Variant variant = new Variant("16",
                2092114,
                "-",
                "TC");
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertNotNull(hgvsProtein);
        assertEquals("p.Asp20GlyfsTer162", hgvsProtein.getHgvs());
    }

    // -------------------- negative strand --------------------------------------

    @Test
    public void testInsertionNegativeStrandPhase0() throws Exception {
        // negative strand, phase 0
        // unconfirmed start
        // 15110   16:2106127:-:T  16      2106127 -       T       indel   ENSP00000457132 p.Asp786GlyfsTer38      p.Gln785fs      fs_shorthand_diff_pos

        // cdna start Position = 1, cds = 2355, cdnaVariantIndex = 2355
        // GTGGTGGTGCAG[t]GACCAGCTGGGAGCCGCTGTG

        Gene gene = getGene("ENSG00000008710");
        Transcript transcript = getTranscript(gene, "ENST00000487932");
        Variant variant = new Variant("16",
                2106127,
                "-",
                "T");
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Asp786GlyfsTer38", predictor.calculate().getHgvs());
    }

    @Test
    public void testInsertionNegativeStrandPhase2UnconfirmedStart() throws Exception {
        //         14086   X:109669061:-:T X       109669061       -       T       indel   ENSP00000423539 p.Tyr19IlefsTer2        p.Gly18fs
        // phase II
        // negative strand
        // unconfirmed start, protein sequence starts with X
        Gene gene = getGene("ENSG00000068366");
        Transcript transcript = getTranscript(gene, "ENST00000514500");
        Variant variant = new Variant("X",
                109669061,
                "-",
                "T");

        String proteinSequence =
                "XTLFKIGYDYKLEQIKKGYDAPLCNLLLFKKVKALLGGNVRMMLSGGAPLSPQTHRFMNVCFCCPIGQGYGLTESCGAGTVTEVTDYTTGRVGAPLICCEIKLKDWQEGGYTINDKPNPRGEIVIGGQNISMGYFKNEEKTAEDYSVDENGQRNLG";

        String reference =
                "AAACTCTGTTCAAGATAGGGTATGATTACAAATTGGAACAGATCAAAAAGGGATATGATGCACCTCTTTGCAATCTGTTACTGTTTAAAAAGGTCAAGGCCCTGCTGGGAGGGAATGTCCGCATGATGCTGTCTGGAGGGGCCCCGCTATCTCCTCAGACACACCGATTCATGAATGTCTGCTTCTGCTGCCCAATTGGCCAGGGTTATGGACTGACAGAATCATGTGGTGCTGGGACAGTTACTGAAGTAACTGACTATACTACTGGCAGAGTTGGAGCACCTCTTATTTGCTGTGAAATTAAGCTAAAAGACTGGCAAGAAGGCGGTTATACAATTAATGACAAGCCAAACCCCAGAGGTGAAATCGTAATTGGTGGACAGAACATCTCCATGGGATATTTTAAAAATGAAGAGAAAACAGCAGAAGATTATTCTGTGGATGAAAATGGACAAAGGAACTTGGGTTGATATCTGCAATAATCCTGCTATGGAAGCTGAAATACTGAAAGAAATTCGAGAAGCTGCAAATGCCATGAAATTGGAGCGATTTGAAATTCCAATCAAGGTTCGATTAAGCCCAGAGCCATGGACCCCTGAAACTGGTTTGGTAACTGATGCTTTCAAACTGAAAAGGAAGGAGCTGAGGAACCATTACCTCAAAGACATTGAACGAATGTATGGGGGCAAATAAAAT";

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Tyr19IlefsTer2", predictor.calculate().getHgvs());
    }

    @Test
    public void testInsertionNegativeStrandPhase2ConfirmedStart() throws Exception {
        // phase II
        // negative strand
        // confirmed start (no annotation flags)
        //        14085   X:109669061:-:T X       109669061       -       T       indel   ENSP00000262835 p.Tyr374IlefsTer2       p.Tyr374fs      fs_shorthand_same_pos

        String proteinSequence =
                "MAKRIKAKPTSDKPGSPYRSVTHFDSLAVIDIPGADTLDKLFDHAVSKFGKKDSLGTREILSEENEMQPNGKVFKKLILGNYKWMNYLEVNRRVNNFGSGLTALGLKPKNTIAIFCETRAEWMIAAQTCFKYNFPLVTLYATLGKEAVVHGLNESEASYLITSVELLESKLKTALLDISCVKHIIYVDNKAINKAEYPEGFEIHSMQSVEELGSNPENLGIPPSRPTPSDMAIVMYTSGSTGRPKGVMMHHSNLIAGMTGQCERIPGLGPKDTYIGYLPLAHVLELTAEISCFTYGCRIGYSSPLTLSDQSSKIKKGSKGDCTVLKPTLMAAVPEIMDRIYKNVMSKVQEMNYIQKTLFKIGYDYKLEQIKKGYDAPLCNLLLFKKVKALLGGNVRMMLSGGAPLSPQTHRFMNVCFCCPIGQGYGLTESCGAGTVTEVTDYTTGRVGAPLICCEIKLKDWQEGGYTINDKPNPRGEIVIGGQNISMGYFKNEEKTAEDYSVDENGQRWFCTGDIGEFHPDGCLQIIDRKKDLVKLQAGEYVSLGKVEAALKNCPLIDNICAFAKSDQSYVISFVVPNQKRLTLLAQQKGVEGTWVDICNNPAMEAEILKEIREAANAMKLERFEIPIKVRLSPEPWTPETGLVTDAFKLKRKELRNHYLKDIERMYGGK";
        Gene gene = getGene("ENSG00000068366");
        Transcript transcript = getTranscript(gene, "ENST00000348502");
        Variant variant = new Variant("X",
                109669061,
                "-",
                "T");
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Tyr374IlefsTer2", predictor.calculate().getHgvs());
    }

    @Test
    public void testDuplicationAsNonsense() throws Exception {
        // Issue #9 Dups reported as nonsense
        // positive strand
        // unconfirmed start
        // 12271   X:71137733:-:CTC        X       71137733        -       CTC     indel   ENSP00000404373 p.Pro167dup     p.Tyr168Ter     vep_dup_cb_ter
        // Result: "ENSP00000404373.1:p.Thr166_Pro167insSer",
        // https://rest.ensembl.org/variant_recoder/human/X:71137733::CTC?content-type=application/json
        Gene gene = getGene("ENSG00000184634");
        Transcript transcript = getTranscript(gene, "ENST00000444034");
        Variant variant = new Variant("X",
                71137734,
                "-",
                "CTC");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Thr166_Pro167insSer", hgvsProtein.getHgvs());

        // forward strand
        // Result: ENSP00000489147.1:p.Ser1646_Gly1647insArgPro
        // https://aug2017.rest.ensembl.org/variant_recoder/human/NC_000022.11:g.50731057_50731058insCGGCCC?content-type=application/json
        // FIXME after Ensembl 102 the protein chanegd and the resultis now
        //  https://rest.ensembl.org/variant_recoder/human/22:50731056::CCGGCC?content-type=application/json
        gene = getGene("ENSG00000251322");
        transcript = getTranscript(gene, "ENST00000262795");
        variant = new Variant("22",
                50731058,
                "-",
                "CGGCCC");

        predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein2 = predictor.calculate();
        assertNotNull(hgvsProtein2);
        assertEquals("p.Ser1523_Gly1524insArgPro", hgvsProtein2.getHgvs());
    }

    /////////////////////////////////////
    ///////////// DELETIONS /////////////
    /////////////////////////////////////

    @Test
    public void testDeletionInframe() throws Exception {
        // Reverse Strand,  CDS 5' Incomplete
        Gene gene = getGene("ENSG00000165119");
        Transcript transcript = getTranscript(gene, "ENST00000481820");
        Variant variant = new Variant("9",
                83970774,
                "CCA",
                "-");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Gly29del", hgvsProtein.getHgvs());
        assertThat(hgvsProtein.getIds(), CoreMatchers.hasItems("ENSP00000473957"));

        // 9:83978229-83978231
        // Reverse Strand (same gene as above), confirmed start
        // ENSP00000317788.4:p.Glu8del
        transcript = getTranscript(gene, "ENST00000351839");
        variant = new Variant("9",
                83978229,
                "TTC",
                "-");

        predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein2 = predictor.calculate();
        assertEquals("p.Glu8del", hgvsProtein2.getHgvs());
        assertThat(hgvsProtein2.getIds(), CoreMatchers.hasItems("ENSP00000317788"));


        // forward strand
        // confirmed start
        gene = getGene("ENSG00000196549");
        transcript = getTranscript(gene, "ENST00000492661");
        variant = new Variant("3",
                155116953,
                "TGA",
                "-");

        predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein3 = predictor.calculate();
        assertEquals("p.Asp209del", hgvsProtein3.getHgvs());
        assertThat(hgvsProtein3.getIds(), CoreMatchers.hasItems("ENSP00000420389"));

        // forward strand
        // unconfirmed start
        gene = getGene("ENSG00000091536");
        transcript = getTranscript(gene, "ENST00000578575");
        variant = new Variant("17",
                18161390,
                "CCA",
                "-");

        predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein4 = predictor.calculate();
        assertEquals("p.Pro9del", hgvsProtein4.getHgvs());
    }

    @Test
    public void testDeletionSynonymousFS() throws Exception {
        // Issue #3
        //2701    6:112061056:G:- 6       112061056       G       -       indel   ENSP00000357653 p.Ser57GlnfsTer27       p.Val56fs       fs_shorthand_diff_pos
        // change at del position 56 is synonymous GTG (val) > GTT (Val)
        // Therefore first affected aa is Ser(57)
        Gene gene = getGene("ENSG00000112761");
        Transcript transcript = getTranscript(gene, "ENST00000368664");
        Variant variant = new Variant("6",
                112061056,
                "G",
                "-");
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Ser57GlnfsTer27", predictor.calculate().getHgvs());
    }

    @Test
    public void testDeletionFS() throws Exception {
        // 2:47822224:T:-  2       47822224        T       -       indel   ENSP00000385398 p.Ile482PhefsTer6       p.Ile482fs
        // negative strand
        Gene gene = getGene("ENSG00000138081");
        Transcript transcript = getTranscript(gene, "ENST00000402508");
        Variant variant = new Variant("2",
                47822224,
                "T",
                "-");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Ile482PhefsTer6", predictor.calculate().getHgvs());
    }

   @Test
    public void testDeletion0() throws Exception {
        // Issue #4
        // 6:121447732:TTC:-  indel   ENSP00000282561 p.Ser297del     p.Ser297_Cys298del      del_cb_aa_1_out
        Gene gene = getGene("ENSG00000152661");
        Transcript transcript = getTranscript(gene, "ENST00000282561");
        Variant variant = new Variant("6",
                121447732,
                "TTC",
                "-");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Ser297del", predictor.calculate().getHgvs());
    }

    @Test
    public void testDeletion2() throws Exception {
        // Issue #4
        // shift
        // 20291   14:104714676:GAGGAC:-   14      104714676       GAGGAC  -       indel   ENSP00000376410 p.Asp1175_Glu1176del    p.Glu1176_Asp1178del    del_cb_aa_more_than_1_out
        Gene gene = getGene("ENSG00000203485");
        Transcript transcript = getTranscript(gene, "ENST00000392634");
        Variant variant = new Variant("14",
                104714676,
                "GAGGAC",
                "-");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Asp1175_Glu1176del", predictor.calculate().getHgvs());
    }

    @Test
    public void testDeletion3() throws Exception {
        // Issue #4
        // off by one
        // 1861    11:75566844:CAAGCG:-    11      75566844        CAAGCG  -       indel   ENSP00000435452 p.Lys166_Arg167del      p.Lys166_Ser168del      del_cb_aa_1_out
        Gene gene = getGene("ENSG00000149257");
        Transcript transcript = getTranscript(gene, "ENST00000525611");
        Variant variant = new Variant("11",
                75566844,
                "CAAGCG",
                "-");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
//        Assert.assertEquals("ENSP00000435452:p.Lys166_Arg167del", predictor.calculate().getHgvs());
        assertEquals("p.Lys166_Arg167del", predictor.calculate().getHgvs());
    }

    @Test
    public void testArrayOutOfBoundsDeletion() throws Exception {

//        21      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000410728 p.Asp325IlefsTer5               cb_empty
//        22      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000491601 p.Asp270IlefsTer5               cb_empty
//        23      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000491340 p.Asp272IlefsTer5               cb_empty
//        24      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000491294 p.Asp272IlefsTer5               cb_empty
//        25      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000363193         p.Arg1266His    vep_empty
//        26      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000491685 p.Asp529IlefsTer5               cb_empty
//        27      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000363215         p.Arg1266His    vep_empty
//        28      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000492010 p.Asp252IlefsTer5               cb_empty
//        29      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000283179 p.Asp467IlefsTer5               cb_empty
//        30      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000491305 p.Asp451IlefsTer5               cb_empty
//        31      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000393151 p.Asp529IlefsTer5               cb_empty
//        32      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000491215 p.Asp548IlefsTer5               cb_empty
//        33      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000491491 p.Asp8IlefsTer5         cb_empty
//        34      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000491903 p.Asp74IlefsTer5                cb_empty
//        35      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000491807 p.Asp272IlefsTer5               cb_empty
//        36      1:244856830:T:- 1       244856830       T       -       indel   ENSP00000492573 p.Asp209IlefsTer5               cb_empty

        Gene gene = getGene("ENSG00000153187");
        Transcript transcript = getTranscript(gene, "ENST00000638475");
        Variant variant = new Variant("1",
                244856830,
                "T",
                "-");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Asp451IlefsTer5", hgvsProtein.getHgvs());
        assertThat(hgvsProtein.getIds(), CoreMatchers.hasItems("ENSP00000491305"));

    }

    /////////////////////////////////////
    ///////////// SNV ///////////////////
    /////////////////////////////////////

    @Test
    public void testArrayOutOfBounds() throws Exception {
        // 11      1:99884391:A:G  1       99884391        A       G       snv     ENSP00000355106 p.Asn829Ser             cb_empty
        Gene gene = getGene("ENSG00000162688");
        Transcript transcript = getTranscript(gene, "ENST00000361915");
        Variant variant = new Variant("1",
                99884391,
                "A",
                "G");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Asn829Ser", hgvsProtein.getHgvs());
        assertThat(hgvsProtein.getIds(), CoreMatchers.hasItems("ENSP00000355106"));
    }

    @Test
    public void testSnvOffByOne() throws Exception {
        Gene gene = getGene("ENSG00000162688");
        Transcript transcript = getTranscript(gene, "ENST00000370165");
        Variant variant = new Variant("1",
                99884391,
                "A",
                "G");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
//        Assert.assertEquals("p.Asn829Ser", hgvsProtein.getHgvs());

        gene = getGene("ENSG00000130164");
        transcript = getTranscript(gene, "ENST00000252444");
        variant = new Variant("19",
                11110721,
                "A",
                "G");

        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        assertEquals("p.Glu422Gly", hgvsProtein.getHgvs());
        assertThat(hgvsProtein.getIds(), CoreMatchers.hasItems("ENSP00000252444"));


//        gene = getGene("ENSG00000144028");
//        transcript = getTranscript(gene, "ENST00000323853");
//        variant = new Variant("2",
//                96291454,
//                "C",
//                "T");
//
//        predictor = new HgvsProteinCalculator(variant, transcript);
//        hgvsProtein = predictor.calculate();
//        Assert.assertEquals("p.Ala787Thr", hgvsProtein.getHgvs());
//        assertThat(hgvsProtein.getIds(), CoreMatchers.hasItems("ENSP00000317123"));
//
//        gene = getGene("ENSG00000160285");
//        transcript = getTranscript(gene, "ENST00000397728");
//        variant = new Variant("22",
//                46228567,
//                "G",
//                "A");
//
//        predictor = new HgvsProteinCalculator(variant, transcript);
//        hgvsProtein = predictor.calculate();
//        Assert.assertEquals("p.Thr16Ile", hgvsProtein.getHgvs());
//        assertThat(hgvsProtein.getIds(), CoreMatchers.hasItems("ENSP00000380837"));
    }

    @Test
    public void testSnv() throws Exception {
        // Issue #2 missing protein example
        // phase 0
        // positive strand
      //  61      3:155143536:G:A 3       155143536       G       A       snv     ENSP00000420389 p.Val428Met             cb_empty
        Gene gene = getGene("ENSG00000196549");
        Transcript transcript = getTranscript(gene, "ENST00000492661");
        Variant variant = new Variant("3",
                155143536,
                "G",
                "A");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Val428Met", hgvsProtein.getHgvs());
        assertThat(hgvsProtein.getIds(), CoreMatchers.hasItems("ENSP00000420389", "P08473"));
    }

    @Test
    public void testSilentSNV() throws Exception {
        // 288     11:77179045:G:A 11      77179045        G       A       snv     ENSP00000386635 p.Arg750=       p.Arg750=
        Gene gene = getGene("ENSG00000137474");
        Transcript transcript = getTranscript(gene, "ENST00000409619");
        Variant variant = new Variant("11",
                77179045,
                "G",
                "A");
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Arg750=", hgvsProtein.getHgvs());

        assertThat(hgvsProtein.getIds(), CoreMatchers.hasItems("ENSP00000386635"));

    }

    @Test
    public void testStopLoss() throws Exception {
        // Issue #7 - stop loss reported as missense

        // phase 0
        // positive strand
        //	3:183650359:G:A	3	183650359	G	A	snv	ENSP00000419120	p.Met1?	p.Met1Ile	start_loss_as_missense
        Gene gene = getGene("ENSG00000114796");
        Transcript transcript = getTranscript(gene, "ENST00000473045");
        Variant variant = new Variant("3",
                183650359,
                "G",
                "A");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Met1?", hgvsProtein.getHgvs());

        assertThat(hgvsProtein.getIds(), CoreMatchers.hasItems("ENSP00000419120"));
        // uniprot trembl name, not swissprot
        //assertThat(hgvsProtein.getIds(), CoreMatchers.hasItems("C9JXR5"));

        // negative strand
        // phase 0
//2731    12:132687314:A:T        12      132687314       A       T       snv     ENSP00000442578 p.Met1? p.Met1Lys       q
        gene = getGene("ENSG00000177084");
        transcript = getTranscript(gene, "ENST00000537064");
        variant = new Variant("12",
                132687314,
                "A",
                "T");

        predictor = new HgvsProteinCalculator(variant, transcript);
        hgvsProtein = predictor.calculate();
        assertEquals("p.Met1?", hgvsProtein.getHgvs());

        assertThat(hgvsProtein.getIds(), CoreMatchers.hasItems("ENSP00000442578"));
    }

    @Test
    public void testSnv1() throws Exception {
        // 288     11:77179045:G:A 11      77179045        G       A       snv     ENSP00000386635 p.Arg750=       p.Arg750=
        Gene gene = getGene("ENSG00000137474");
        Transcript transcript = getTranscript(gene, "ENST00000409619");
        Variant variant = new Variant("11",
                77179045,
                "G",
                "A");
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertEquals("p.Arg750=", hgvsProtein.getHgvs());

        assertThat(hgvsProtein.getIds(), CoreMatchers.hasItems("ENSP00000386635"));
    }

    @Test
    public void testSnvSynonymous() throws Exception {
        Gene gene = getGene("ENSG00000221859");
        Transcript transcript = getTranscript(gene, "ENST00000380095");
        Variant variant = new Variant("21",
                44637453,
                "C",
                "T");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Ala12=", predictor.calculate().getHgvs());
    }

    @Test
    public void testSnvMissense() throws Exception {
        Gene gene = getGene("ENSG00000221859");
        Transcript transcript = getTranscript(gene, "ENST00000380095");
        Variant variant = new Variant("21",
                44637501,
                "G",
                "C");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Glu28Asp", predictor.calculate().getHgvs());
    }

    @Test
    public void testSnvStopGained() throws Exception {
        Gene gene = getGene("ENSG00000221859");
        Transcript transcript = getTranscript(gene, "ENST00000380095");
        Variant variant = new Variant("21",
                44637643,
                "C",
                "T");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Gln76Ter", predictor.calculate().getHgvs());
    }


    // ------------------- from HgvsCalculator --------------------------------
//    @Test
//    public void testSNVBadReferenceSequence() throws Exception {
//
//        // Weird character ("U") in protein sequence (e.g. ENST00000525566/ENSP00000434516, position 648) must not
//        // return any protein HGVS description
//        Gene gene = getGene("ENSG00000198431");
//        Transcript transcript = getTranscript(gene, "ENST00000525566");
//        Variant variant = new Variant("12",
//                104742191,
//                "T",
//                "C");
//        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
//        HgvsProtein hgvsProtein = predictor.calculate();
//        Assert.assertNull(hgvsProtein);
//
//    }

    @Test
    public void testSnvBadAltAA() throws Exception {
        // Invalid alternte nt - no prot hgvs should be returned
        Gene gene = getGene("ENSG00000137474");
        Transcript transcript = getTranscript(gene, "ENST00000409619");
        Variant variant = new Variant("11",
                77179045,
                "G",
                "S");
        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        HgvsProtein hgvsProtein = predictor.calculate();
        assertNull(hgvsProtein);
    }

    @Test
    public void testDelins() throws Exception {
        // Issue #8 delins reported as dels
        // 166     14:91313274:CCTGCTGCC:- 14      91313274        CCTGCTGCC       -       indel   ENSP00000374507 p.Trp845_Val848delinsLeu        p.Trp845_Val848del      delins_as_del
        Gene gene = getGene("ENSG00000015133");
        Transcript transcript = getTranscript(gene, "ENST00000389857");
        Variant variant = new Variant("14",
                91313274,
                "CCTGCTGCC",
                "-");

        HgvsProteinCalculator predictor = new HgvsProteinCalculator(variant, transcript);
        assertEquals("p.Trp845_Val848delinsLeu", predictor.calculate().getHgvs());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////// UTILS ///////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Transcript getTranscript(Gene gene, String id) {
        for (Transcript transcript : gene.getTranscripts()) {
            if (transcript.getId().startsWith(id)) {
                return transcript;
            }
        }
        return null;
    }

    private Gene getGene(String id) {
        for (Gene gene : geneList) {
            if (gene.getId().equals(id)) {
                return gene;
            }
        }
        return null;
    }

    private List<Gene> loadGenes(Path path) throws IOException {
        List<Gene> repeatSet = new ArrayList<>();

        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(path)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                repeatSet.add(jsonObjectMapper.convertValue(JSON.parse(line), Gene.class));
                line = bufferedReader.readLine();
            }
        }

        return repeatSet;
    }
}