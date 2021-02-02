package org.opencb.cellbase.lib.variant.hgvs;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.TestInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.GeneQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.impl.core.GeneMongoDBAdaptor;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.cellbase.lib.managers.GeneManager;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;


/**
 * Created by fjlopez on 14/02/17.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HgvsCalculatorTest extends GenericMongoDBAdaptorTest {
    private HgvsCalculator hgvsCalculator;
    private GeneMongoDBAdaptor geneDBAdaptor;
    private GeneManager geneManager;

    public HgvsCalculatorTest() throws IOException {
    }

    @BeforeAll
    public void init() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/hgvs/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene");
        path = Paths.get(getClass()
                .getResource("/hgvs/genome_sequence.test.json.gz").toURI());
        loadRunner.load(path, "genome_sequence");
        CellBaseManagerFactory cellBaseManagerFactory = new CellBaseManagerFactory(cellBaseConfiguration);
        geneManager = cellBaseManagerFactory.getGeneManager("hsapiens", "GRCh37");
        hgvsCalculator = new HgvsCalculator(cellBaseManagerFactory.getGenomeManager("hsapiens", "GRCh37"));
        geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor("hsapiens", "GRCh37");
    }

//    @Test
//    public void testProteinHgvsInsertion() throws Exception {
//        // Frameshift on the last aa causes generation of exact same aa followed by stop codon, i.e.
//        // original sequence            ......CTGGCT
//        // original sequence                        GTAATCAC......
//        // codons                             |  |  |  |
//        // original aa sequence               T  T  L  STOP
//        // sequence after TTAA insertion            ttaaGTAA
//        // codons                             |  |  |  |
//        // altered aa sequence                T  T  L  STOP
//        // Variant validator describes it as a simple frameshift and that's how we're handling it
//        List<String> hgvsList = getVariantHgvs(new Variant("3",
//                149238596,
//                "-",
//                "TTAA"));
//        // six protein hgvs expected
//        assertNumberProteinHGVS(3, hgvsList);
//        // do not know which of these correspond to variant validator ones but looks consistent
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000419465:p.Leu400fs",
//                "ENSP00000353847:p.Leu400fs",
//                "ENSP00000419234:p.Leu400fs"));
//
//        // No "unconfirmed start" flag is provided by ENSEMBL for transcript ENST00000618610 but, however, the protein
//        // sequence they provide starts with an "X" and first exon phase indicates a shift. This was causing the code
//        // to break. I took the GRCh38 example as I don't know about a GRCh37 case in this situation, i.e. all data
//        // for this (and only this) example are GRCh38 data as opposed to the rest of test data.
//        hgvsList = getVariantHgvs(new Variant("20",
//                42106815,
//                "-",
//                "C"));
//        // six protein hgvs expected
//        assertNumberProteinHGVS(9, hgvsList);
//        // Can only validate "ENSP00000362283:p.Ala1121fs" and "ENSP00000362294:p.Ala1140fs" with variant validator
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000362283:p.Ala1121fs",
//                "ENSP00000348408:p.Ala1130fs",
//                "ENSP00000362280:p.Ala1131fs",
//                "ENSP00000362286:p.Ala1120fs",
//                "ENSP00000362294:p.Ala1140fs",
//                "ENSP00000362297:p.Ala1111fs",
//                "ENSP00000362289:p.Ala1143fs",
//                "ENSP00000481466:p.Ala739fs",
//                "ENSP00000484524:p.Ala756fs"));
//
//        // Made-up variant derived from a ClinVar insertion that used to break the code. After right-shifting ends up
//        // exhausting the reference protein sequence and therefore being an extension, i.e. the start coordinate in
//        // protein coordinates corresopnds to the aa next to the last aa in the protein sequence
//        hgvsList = getVariantHgvs(new Variant("12",
//                103232958,
//                "-",
//                "T"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(2, hgvsList);
//        // Can only validate ENSP00000448059:p.Ter453Valext*? using Variant Validator. The other one
//        // (ENSP00000303500:p.Ter448Valext*?) does not seem to appear in Variant Validator but I'm fairly confident
//        // that it's due to some difference in transcript/protein annotation between RefSeq and ENSEMBL
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000448059:p.Ter453Valext*?",
//                "ENSP00000303500:p.Ter448Valext*?"));
//
//        // Made-up variant derived from a ClinVar insertion that used to break the code; affects start of an
//        // unconfirmed-start transcript (ENST00000372421) by affecting the start codon that would span out of the
//        // transcript sequence boundaries. Therefore, no protein HGVS must be returned for the corresponding protein.
//        // I have no good means to validate ENSP00000396608:p.Asn2fs. Rest of protein HGVS have been validated using
//        // Variant Validator.
//        hgvsList = getVariantHgvs(new Variant("10",
//                79397320,
//                "-",
//                "TATTG"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(11, hgvsList);
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000396608:p.Asn2fs",
//                "ENSP00000385717:p.Asn28fs",
//                "ENSP00000361517:p.Asn28fs",
//                "ENSP00000361520:p.Asn28fs",
//                "ENSP00000286628:p.Asn28fs",
//                "ENSP00000286627:p.Asn28fs",
//                "ENSP00000385552:p.Asn28fs",
//                "ENSP00000346321:p.Asn28fs",
//                "ENSP00000385806:p.Asn28fs",
//                "ENSP00000475086:p.Asn28fs",
//                "ENSP00000474686:p.Asn28fs"));
//
//        // insertion of more than one AA
//        hgvsList = getVariantHgvs(new Variant("22",
//                38318124,
//                "-",
//                "CTTTTG"));
//        // two protein hgvs expected, checked against variant validator
//        assertNumberProteinHGVS(2, hgvsList);
//        // I have no good means of checking "ENSP00000404543:p.Lys154_Gln155insLeuLeu" as this
//        // genomic variant is not in ENSEMBL Variation and Variant Validator does not generate HGVS for this particular
//        // protein (this protein/transcript is apparently not in RefSeq); I had to make up this particular variant to
//        // test this particular bit of code (insertion of a STOP codon). The other one
//        // (ENSP00000215957:p.Lys238_Gln239insLeuLeu) has been properly validated with variant validator
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000215957:p.Lys238_Gln239insLeuLeu",
//                "ENSP00000404543:p.Lys154_Gln155insLeuLeu"));
//
//        // STOP codon gained while spanning insertion
//        hgvsList = getVariantHgvs(new Variant("22",
//                38318124,
//                "-",
//                "CTTTAA"));
//        // two protein hgvs expected, checked against variant validator
//        assertNumberProteinHGVS(2, hgvsList);
//        // I have no good means of checking "ENSP00000404543:p.Gln155Ter" as this
//        // genomic variant is not in ENSEMBL Variation and Variant Validator does not generate HGVS for this particular
//        // protein (this protein/transcript is apparently not in RefSeq); I had to make up this particular variant to
//        // test this particular bit of code (insertion of a STOP codon). The other one (ENSP00000215957:p.Gln239Ter)
//        // has been properly validated with variant validator
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000215957:p.Gln239Ter", "ENSP00000404543:p.Gln155Ter"));
//
//        // Must fail to predict AA while spanning insertion and therefore must not return any HGVS
//        hgvsList = getVariantHgvs(new Variant("22",
//                38318124,
//                "-",
//                "CTTFGA"));
//        // none protein hgvs expected
//        assertNumberProteinHGVS(0, hgvsList);
//
//        // An example that used to fail taken from ClinVar
//        hgvsList = getVariantHgvs(new Variant("10",
//                104263974,
//                "-",
//                "C"));
//        // three protein hgvs expected, checked against variant validator
//        assertNumberProteinHGVS(3, hgvsList);
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000358918:p.Ala25fs",
//                "ENSP00000358915:p.Ala25fs",
//                "ENSP00000411597:p.Ala25fs"));
//
//        // Unexpected nt character "F" should not return any protein HGVS
//        hgvsList = getVariantHgvs(new Variant("22",
//                38318124,
//                "-",
//                "FGA"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(0, hgvsList);
//
//        // Affects last incomplete codon (i.e. end of the codon goes beyond transcript ENST00000513223 sequence). Just
//        // one protein HGVS (validated with variant validator) must be returned for the other coding transcript
//        // (ENST00000264930/ENSP00000264930).
//        hgvsList = getVariantHgvs(new Variant("22",
//                38318124,
//                "-",
//                "TAA"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(2, hgvsList);
//        // I have no good means of checking "ENSP00000404543:p.Gln155Ter" as this
//        // genomic variant is not in ENSEMBL Variation and Variant Validator does not generate HGVS for this particular
//        // protein (this protein/transcript is apparently not in RefSeq); I had to make up this particular variant to
//        // test this particular bit of code (insertion of a STOP codon). The other one (ENSP00000215957:p.Gln239Ter)
//        // has been properly validated with variant validator
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000215957:p.Gln239Ter", "ENSP00000404543:p.Gln155Ter"));
//
//        // Affects last incomplete codon (i.e. end of the codon goes beyond transcript (ENST00000513223) sequence). Just
//        // one protein HGVS (validated with variant validator) must be returned for the other coding transcript
//        // (ENST00000264930/ENSP00000264930).
//        hgvsList = getVariantHgvs(new Variant("5",
//                1057644,
//                "-",
//                "A"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(1, hgvsList);
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000264930:p.Lys990fs"));
//
//        // Duplication of 1 single aa
//        hgvsList = getVariantHgvs(new Variant("22",
//                38318124,
//                "-",
//                "CAA"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(2, hgvsList);
//        // I have no good means of checking "ENSP00000404543:p.Gln156dup" as this
//        // genomic variant is not in ENSEMBL Variation and Variant Validator does not generate HGVS for this particular
//        // protein (this protein/transcript is apparently not in RefSeq); I had to make up this particular variant to
//        // test this particular bit of code (duplication of 1 single aa)
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000215957:p.Gln240dup",
//                "ENSP00000404543:p.Gln156dup"));
//
//        // In-frame insertion
//        hgvsList = getVariantHgvs(new Variant("22",
//                38318124,
//                "-",
//                "GTT"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(2, hgvsList);
//        // I have no good means of checking "ENSP00000404543:p.Gln154_Gln155insVal" as this
//        // genomic variant is not in ENSEMBL Variation and Variant Validator does not generate HGVS for this particular
//        // protein (this protein/transcript is apparently not in RefSeq); I had to make up this particular variant to
//        // test this particular bit of code (in-frame insertion)
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000215957:p.Lys238_Gln239insVal",
//                "ENSP00000404543:p.Lys154_Gln155insVal"));
//
//        // Duplication
//        hgvsList = getVariantHgvs(new Variant("22",
//                38318124,
//                "-",
//                "CAGCAGCAC"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(2, hgvsList);
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000404543:p.His157_Gln159dup",
//                "ENSP00000215957:p.His241_Gln243dup"));
//
//        // Frameshift insertion
//        hgvsList = getVariantHgvs(new Variant("15",
//                48736763,
//                "-",
//                "GTATCCA"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(2, hgvsList);
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000453958:p.Ser441fs",
//                "ENSP00000325527:p.Ser2005fs"));
//
//        // Arbitrarily selected insertion
//        hgvsList = getVariantHgvs(new Variant("22",
//                38318124,
//                "-",
//                "CAGCAGCAC"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(2, hgvsList);
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000404543:p.His157_Gln159dup",
//                "ENSP00000215957:p.His241_Gln243dup"));
//    }
//
//    @Test
//    public void testProteinHgvsSNV() throws Exception {
//        // Weird character ("U") in protein sequence (e.g. ENST00000525566/ENSP00000434516, position 648) must not
//        // return any protein HGVS description
//        List<String> hgvsList = getVariantHgvs(new Variant("12",
//                104742191,
//                "T",
//                "C"));
//        // six protein hgvs expected
//        assertNumberProteinHGVS(0, hgvsList);
//
//        // Synonymous variant
//        hgvsList = getVariantHgvs(new Variant("10",
//                104865516,
//                "G",
//                "A"));
//        // six protein hgvs expected
//        assertNumberProteinHGVS(6, hgvsList);
//        // Can't know which of these proteins correspond to the variant validator ones but HGVS descriptions seem to
//        // align correctly with Variant Validator ones
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000339479:p.Val112=",
//                "ENSP00000383960:p.Val112=",
//                "ENSP00000392236:p.Val83=",
//                "ENSP00000396468:p.Val112=",
//                "ENSP00000411330:p.Val35=",
//                "ENSP00000447664:p.Val47="));
//
//        // Affects STOP codon - warning message expected and no protein HGVS should be returned
//        hgvsList = getVariantHgvs(new Variant("16",
//                28488951,
//                "T",
//                "A"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(0, hgvsList);
//
//        // Affects STOP codon - warning message expected and no protein HGVS should be returned
//        hgvsList = getVariantHgvs(new Variant("21",
//                46058088,
//                "T",
//                "A"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(0, hgvsList);
//
//        // Invalid alternte nt - no prot hgvs should be returned
//        hgvsList = getVariantHgvs(new Variant("2",
//                183702696,
//                "G",
//                "S"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(0, hgvsList);
//
//        // STOP gain
//        hgvsList = getVariantHgvs(new Variant("2",
//                183702696,
//                "G",
//                "A"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(1, hgvsList);
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000295113:p.Arg281Ter"));
//
//        // Arbitrary non-synonymous SNV
//        hgvsList = getVariantHgvs(new Variant("22",
//                38333177,
//                "A",
//                "G"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(2, hgvsList);
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000215957:p.Asn800Ser","ENSP00000416766:p.Asn114Ser"));
//    }
//
//    @Test
//    public void testProteinHgvsDeletion() throws Exception {
//        // Made-up variant derived from a ClinVar deletion that used to break the code; start falls outside coding
//        // region but after right aligning both fall within the coding region
//        List<String> hgvsList = getVariantHgvs(new Variant("1",
//                156084708,
//                "CCATGGAGA",
//                "-"));
//        // 5 protein hgvs expected
//        assertNumberProteinHGVS(5, hgvsList);
//        // No proper validation could be done for these but he main objective of this test case was to check that a
//        // deletion originally falling outside the coding region, if after normalisation falls within the coding region
//        // then protein hgvs must be returned. Variant Validator returns things like "p.(Met1?)"; i've been searching
//        // for the use of the "?" but couldn't find anything quickly so i'm sticking to this representation
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000357284:p.Met1_Thr3del",
//                "ENSP00000292304:p.Met1_Thr3del",
//                "ENSP00000355292:p.Met1_Thr3del",
//                "ENSP00000357283:p.Met1_Thr3del",
//                "ENSP00000357282:p.Met1_Thr3del"));
//
//        // Made-up variant derived from a ClinVar deletion that used to break the code; affects start of an
//        // unconfirmed-start transcript (ENST00000372421) by affecting the start codon that would span out of the
//        // transcript sequence boundaries. Therefore, no protein HGVS must be returned fot the corresponding protein.
//        // Rest of protein HGVS (except "ENSP00000396608:p.Asn2fs" for which there's no protein in variant validator)
//        // have been validated using Variant Validator.
//        hgvsList = getVariantHgvs(new Variant("10",
//                79397316,
//                "TATTG",
//                "-"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(11, hgvsList);
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000396608:p.Asn2fs",
//                "ENSP00000385717:p.Asn28fs",
//                "ENSP00000361517:p.Asn28fs",
//                "ENSP00000361520:p.Asn28fs",
//                "ENSP00000286628:p.Asn28fs",
//                "ENSP00000286627:p.Asn28fs",
//                "ENSP00000385552:p.Asn28fs",
//                "ENSP00000346321:p.Asn28fs",
//                "ENSP00000385806:p.Asn28fs",
//                "ENSP00000475086:p.Asn28fs",
//                "ENSP00000474686:p.Asn28fs"));
//
//        // Removes last CODING nts positive strand - will require additional query to genome sequence
//        hgvsList = getVariantHgvs(new Variant("5",
//                205988,
//                "ATCC",
//                "-"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(2, hgvsList);
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000296824:p.Gln68fs", "ENSP00000411206:p.Gln68fs"));
//
//
//        // Removes last CODING nts positive strand - will require additional query to genome sequence
//        hgvsList = getVariantHgvs(new Variant("22",
//                38333157,
//                "GCAG",
//                "-"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(3, hgvsList);
//        // I have no good means of checking "ENSP000(00406053:p.Glu369fs" nor "ENSP00000416766:p.Glu107fs" as this
//        // genomic variant is not in ENSEMBL Variation and Variant Validator does not generate HGVS for this particular
//        // protein (this protein/transcript is apparently not in RefSeq); I had to make up this particular variant to
//        // test this particular bit of code (raising additional query to genome sequence)
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000215957:p.Glu793fs",
//                "ENSP00000406053:p.Glu369fs",
//                "ENSP00000416766:p.Glu107fs"));
//
//        // Removes last CODING nts negative strand - will require additional query to genome sequence
//        hgvsList = getVariantHgvs(new Variant("5",
//                1057643,
//                "TTCT",
//                "-"));
//        // two protein hgvs expected
//        assertNumberProteinHGVS(1, hgvsList);
//        // I have no good means of checking "ENSP00000428854:p.Ala346fs" as this genomic variant is not in ENSEMBL
//        // Variation and Variant Validator does not generate HGVS for this particular protein (this protein/transcript
//        // is apparently not in RefSeq); I had to make up this particular variant to test this particular bit of code
//        // (raising additional query to genome sequence)
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000264930:p.Glu989fs"));
//
//        // Removes whole STOP codon and just those 3 nts - out of protein seq boundaries, warning message expected and
//        // no PROTEIN hgvs must be returned
//        hgvsList = getVariantHgvs(new Variant("21",
//                46058088,
//                "TGA",
//                "-"));
//        // two transcript hgvs expected
//        assertEquals(2, hgvsList.size());
//        // no PROTEIN hgvs expected
//        assertNumberProteinHGVS(0, hgvsList);
//
//        // Requires right-aligning
//        //      93 94 95 96
//        // ...QQA  C  C  V  PV...
//        //         |
//        hgvsList = getVariantHgvs(new Variant("21",
//                46057614,
//                "TGC",
//                "-"));
//        assertEquals(3, hgvsList.size());
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000369438:p.Cys95del"));
//
//        hgvsList = getVariantHgvs(new Variant("21", 46057613, "CTGCTGTGTGCCTGT", "-"));
//        assertEquals(3, hgvsList.size());
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000369438:p.Cys105_Cys109del"));
//
//        hgvsList = getVariantHgvs(new Variant("10", 135369109,
//                "CTTCTGCTGCTGTTGTTGGCA", "-"));
//        assertEquals(9, hgvsList.size());
//        // There may be more than these, but these 4 are the ones that I can actually validate
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000341282:p.Cys268_Lys274del",
//                "ENSP00000357503:p.Cys232_Lys238del", "ENSP00000303978:p.Cys268_Lys274del",
//                "ENSP00000411779:p.Cys232_Lys238del"));
//
//        hgvsList = getVariantHgvs(new Variant("21", 46074460, "GGT", "-"));
//        assertEquals(3, hgvsList.size());
//        // There may be more than these, but these 4 are the ones that I can actually validate
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000375476:p.Thr24del"));
//
//        hgvsList = getVariantHgvs(new Variant("21", 46074291, "ACAAA", "-"));
//        assertEquals(3, hgvsList.size());
//        // There may be more than these, but these 4 are the ones that I can actually validate
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000375476:p.Leu80fs"));
//
//        hgvsList = getVariantHgvs(new Variant("21", 46074466, "GGGGACACAGCAC", "-"));
//        assertEquals(3, hgvsList.size());
//        // There may be more than these, but these 4 are the ones that I can actually validate
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000375476:p.Cys19fs"));
//
//        hgvsList = getVariantHgvs(new Variant("21", 46057613, "CTGCTGTGTGCCTGT", "-"));
//        assertEquals(3, hgvsList.size());
//        // There may be more than these, but these 4 are the ones that I can actually validate
//        assertThat(hgvsList, CoreMatchers.hasItems("ENSP00000369438:p.Cys105_Cys109del"));
//    }

    private void assertNumberProteinHGVS(int expectedNumber, List<String> hgvsList) {
        int counter = 0;
        // no PROTEIN hgvs expected
        for (String hgvs : hgvsList) {
            if (hgvs.startsWith("ENSP")) {
                counter += 1;
            }
        }

        assertEquals(expectedNumber, counter);

    }

    private void assertNumberTranscriptHGVS(int expectedNumber, List<String> hgvsList) {
        int counter = 0;
        // no PROTEIN hgvs expected
        for (String hgvs : hgvsList) {
            if (hgvs.startsWith("ENST")) {
                counter += 1;
            }
        }

        assertEquals(expectedNumber, counter);

    }

    /***
     * NB: THESE ARE GRCH37 TRANSCRIPTS!
     */
    @Test
    public void testTranscriptHgvs() throws QueryException, IllegalAccessException {

        // Invalid characters in alternate allele ("TBS") - should not break the code, no transcript hgvs should be
        // returned
        List<String> hgvsList = getVariantHgvs(new Variant("5",
                1057643,
                "-",
                "TBS"));
        // two protein hgvs expected
        assertEquals(0, hgvsList.size());

        // Duplication
        hgvsList = getVariantHgvs(new Variant("22",
                38318124,
                "-",
                "CAGCAGCAC"));
        // two transcript hgvs expected
        assertNumberTranscriptHGVS(2, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000445494(ENSG00000100139):c.471_479dup9",
                "ENST00000215957(ENSG00000100139):c.723_731dup9"));

        // Duplication in positive transcript - must right align and properly calculate the duplicated range
        hgvsList = getVariantHgvs(new Variant("22",
                38318177,
                "-",
                "CC"));
        // I don't have good means for checking "ENST00000445494(ENSG00000100139):c.518_519dupCC";
        // ENST00000215957(ENSG00000100139):c.770_771dupCC is the one properly validated.
        assertNumberTranscriptHGVS(2, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000445494(ENSG00000100139):c.518_519dupCC",
                "ENST00000215957(ENSG00000100139):c.770_771dupCC"));

        hgvsList = getVariantHgvs(new Variant("22", 38308486, "C", "T"));
        assertNumberTranscriptHGVS(3, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000445494(ENSG00000100139):c.72C>T",
                "ENST00000215957(ENSG00000100139):c.324C>T", "ENST00000489812(ENSG00000100139):n.775C>T"));

        hgvsList = getVariantHgvs(new Variant("22", 38379525, "G", "-"));
        // There may be more than these, but these 4 are the ones that I can actually validate
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000396884(ENSG00000100146):c.267delC",
                "ENST00000360880(ENSG00000100146):c.267delC", "ENST00000427770(ENSG00000100146):c.267delC",
                "ENST00000470555(ENSG00000100146):n.70+821delC"));

        hgvsList = getVariantHgvs(new Variant("19", 45411941, "T", "C"));
        assertNumberTranscriptHGVS(4, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000252486(ENSG00000130203):c.388T>C",
                "ENST00000446996(ENSG00000130203):c.388T>C", "ENST00000434152(ENSG00000130203):c.466T>C",
                "ENST00000425718(ENSG00000130203):c.388T>C"));

        hgvsList = getVariantHgvs(new Variant("1", 136024, "C", "T"));
        assertNumberTranscriptHGVS(1, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000423372(ENSG00000237683):c.*910-222G>A"));

        hgvsList = getVariantHgvs(new Variant("13", 25457289, "G", "A"));
        assertNumberTranscriptHGVS(2, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000381884(ENSG00000151849):c.*26C>T",
                "ENST00000545981(ENSG00000151849):c.*697C>T"));

        hgvsList = getVariantHgvs(new Variant("13", 25496789, "C", "G"));
        assertNumberTranscriptHGVS(2, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000381884(ENSG00000151849):c.-67+110G>C",
                "ENST00000545981(ENSG00000151849):c.-67+110G>C"));

        hgvsList = getVariantHgvs(new Variant("13", 25487369, "G", "A"));
        assertNumberTranscriptHGVS(2, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000381884(ENSG00000151849):c.-66-140C>T",
                "ENST00000545981(ENSG00000151849):c.-66-140C>T"));

        hgvsList = getVariantHgvs(new Variant("13", 25459270, "C", "T"));
        assertNumberTranscriptHGVS(3, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000471870(ENSG00000151849):n.367+144G>A",
                "ENST00000381884(ENSG00000151849):c.3477+144G>A",
                "ENST00000545981(ENSG00000151849):c.*131+144G>A"));

        hgvsList = getVariantHgvs(new Variant("13", 26967553, "A", "G"));
        assertNumberTranscriptHGVS(4, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000477290(ENSG00000132964):n.510A>G",
                "ENST00000465820(ENSG00000132964):n.133-2869A>G",
                "ENST00000381527(ENSG00000132964):c.696A>G",
                "ENST00000536792(ENSG00000132964):c.*143A>G"));

        hgvsList = getVariantHgvs(new Variant("13", 26966929, "G", "A"));
        assertNumberTranscriptHGVS(4, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000477290(ENSG00000132964):n.461-575G>A",
                "ENST00000465820(ENSG00000132964):n.133-3493G>A",
                "ENST00000381527(ENSG00000132964):c.647-575G>A",
                "ENST00000536792(ENSG00000132964):c.*94-575G>A"));

        hgvsList = getVariantHgvs(new Variant("13", 26962152, "T", "C"));
        assertNumberTranscriptHGVS(4, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000477290(ENSG00000132964):n.460+2673T>C",
                "ENST00000465820(ENSG00000132964):n.132+2673T>C",
                "ENST00000381527(ENSG00000132964):c.646+2673T>C",
                "ENST00000536792(ENSG00000132964):c.*93+2673T>C"));

        hgvsList = getVariantHgvs(new Variant("11", 62543180, "A", "G"));
        assertNumberTranscriptHGVS(8, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000524976(ENSG00000162227):n.90-63A>G",
                "ENST00000532915(ENSG00000162227):n.89-63A>G",
                "ENST00000527073(ENSG00000168569):n.66-1043T>C",
                "ENST00000294168(ENSG00000162227):c.-13-63A>G",
                "ENST00000526261(ENSG00000162227):c.-76A>G",
                "ENST00000525405(ENSG00000162227):c.-13-63A>G",
                "ENST00000529509(ENSG00000162227):c.-13-63A>G",
                "ENST00000528367(ENSG00000168569):c.315-1043T>C"));

        hgvsList = getVariantHgvs(new Variant("2", 191399259, "-", "CGC"));
        assertNumberTranscriptHGVS(2, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000409150(ENSG00000189362):c.97+24_97+26dupGCG",
                "ENST00000343105(ENSG00000189362):c.97+24_97+26dupGCG"));

        hgvsList = getVariantHgvs(new Variant("19:45411941:T:C"));
        assertNumberTranscriptHGVS(4, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000434152(ENSG00000130203):c.466T>C",
                "ENST00000425718(ENSG00000130203):c.388T>C", "ENST00000252486(ENSG00000130203):c.388T>C",
                "ENST00000446996(ENSG00000130203):c.388T>C"));

        hgvsList = getVariantHgvs(new Variant("17", 4542753, "G", "A"));
        assertNumberTranscriptHGVS(7, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000573740(ENSG00000161905):n.336C>T",
                "ENST00000293761(ENSG00000161905):c.309C>T",
                "ENST00000574640(ENSG00000161905):c.192C>T", "ENST00000572265(ENSG00000161905):c.-145C>T",
                "ENST00000545513(ENSG00000161905):c.375C>T","ENST00000570836(ENSG00000161905):c.309C>T",
                "ENST00000576394(ENSG00000161905):c.309C>T"));

        hgvsList = getVariantHgvs(new Variant("13", 20600928, "-", "A"));
        assertNumberTranscriptHGVS(6, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000468677(ENSG00000121741):n.220+32dupA",
                "ENST00000382870(ENSG00000121741):n.244+32dupA",
                "ENST00000382871(ENSG00000121741):c.1735+32dupA",
                "ENST00000382874(ENSG00000121741):c.1735+32dupA", "ENST00000382883(ENSG00000121741):c.181+32dupA",
                "ENST00000382869(ENSG00000121741):c.1735+32dupA"));

        hgvsList = getVariantHgvs(new Variant("13", 19752539, "AA", "-"));
        assertNumberTranscriptHGVS(1, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000400113(ENSG00000198033):c.227-6_227-5delTT"));

        hgvsList = getVariantHgvs(new Variant("13", 28835528, "-", "C"));
        assertNumberTranscriptHGVS(4, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000503791(ENSG00000152520):n.1506-11_1506-10insC",
                "ENST00000380958(ENSG00000152520):c.1354-11_1354-10insC",
                "ENST00000399613(ENSG00000152520):c.754-11_754-10insC",
                "ENST00000282391(ENSG00000152520):c.418-11_418-10insC"));

        hgvsList = getVariantHgvs(new Variant("22", 17488824, "-", "G"));
        assertNumberTranscriptHGVS(3, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000523144(ENSG00000215568):n.59+7_59+8insC",
                "ENST00000400588(ENSG00000215568):c.174+7_174+8insC",
                "ENST00000465611(ENSG00000215568):c.59+7_59+8insC"));

        hgvsList = getVariantHgvs(new Variant("5", 1093610, "-", "GGGCGGGGACT"));
        assertNumberTranscriptHGVS(1, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000264930(ENSG00000113504):c.342+28_342+38dup11"));

        hgvsList = getVariantHgvs(new Variant("2", 179622239, "TCAAAG", "-"));
        assertNumberTranscriptHGVS(10, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000585451(ENSG00000237298):n.199-3426_199-3421del6",
                "ENST00000590773(ENSG00000237298):n.300+1192_300+1197del6",
                "ENST00000578746(ENSG00000237298):n.121+1192_121+1197del6",
                "ENST00000342992(ENSG00000155657):c.10303+1467_10303+1472del6",
                "ENST00000460472(ENSG00000155657):c.10165+1467_10165+1472del6",
                "ENST00000589042(ENSG00000155657):c.10678+25_10678+30del6",
                "ENST00000591111(ENSG00000155657):c.10303+1467_10303+1472del6",
                "ENST00000342175(ENSG00000155657):c.10166-720_10166-715del6",
                "ENST00000359218(ENSG00000155657):c.10540+25_10540+30del6",
                "ENST00000360870(ENSG00000155657):c.10303+1467_10303+1472del6"));

        hgvsList = getVariantHgvs(new Variant("14", 24607040, "GTCAAACCATT", "-"));
        assertNumberTranscriptHGVS(9, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000561059(ENSG00000092010):n.696+37_696+47del11",
                "ENST00000561142(ENSG00000092010):n.456+37_456+47del11",
                "ENST00000559741(ENSG00000092010):n.178+37_178+47del11",
                "ENST00000560420(ENSG00000092010):n.144+37_144+47del11",
                "ENST00000206451(ENSG00000092010):c.390+37_390+47del11",
                "ENST00000559123(ENSG00000092010):c.-88+37_-88+47del11",
                "ENST00000382708(ENSG00000092010):c.390+37_390+47del11",
                "ENST00000561435(ENSG00000092010):c.390+37_390+47del11",
                "ENST00000558112(ENSG00000092010):c.144+37_144+47del11"));

        hgvsList = getVariantHgvs(new Variant("10", 82122881, "-", "ACACA"));
        assertNumberTranscriptHGVS(6, hgvsList);
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000372199(ENSG00000133665):c.270+51_270+52ins5",
                "ENST00000372198(ENSG00000133665):c.312+51_312+52ins5",
                "ENST00000372197(ENSG00000133665):c.270+51_270+52ins5",
                "ENST00000444807(ENSG00000133665):c.270+51_270+52ins5",
                "ENST00000256039(ENSG00000133665):c.270+51_270+52ins5",
                "ENST00000444807(ENSG00000133665):c.270+51_270+52ins5"));

    }

    private List<String> getVariantHgvs(Variant variant) throws QueryException, IllegalAccessException {
        GeneQuery query = new GeneQuery();
        Region region = new Region(variant.getChromosome(), variant.getStart(), variant.getEnd());
        query.setRegions(Collections.singletonList(region));
        CellBaseDataResult<Gene> results = geneManager.search(query);
        List<Gene> geneList = results.getResults();
        return hgvsCalculator.run(variant, geneList);
    }

//    private List<String> getVariantHgvs(Variant variant) {
//
//        // Do not return proteins, as we just want to test the hgvs transcripts right now. Proteins are tested in another class.
//        List<Gene> geneList = geneDBAdaptor.getByRegion(new Region(variant.getChromosome(), variant.getStart(),
//                        variant.getEnd()), new QueryOptions("include",
//                        "name,id,transcripts.id,"
//                        + "transcripts.annotationFlags,transcripts.xrefs,"
//                        + "transcripts.strand,transcripts.name,transcripts.start,transcripts.end,"
//                        + "transcripts.cDnaSequence,"
//                        + "transcripts.genomicCodingStart,transcripts.genomicCodingEnd,transcripts.cdnaCodingStart,"
//                        + "transcripts.exons.phase,"
//                        + "transcripts.cdnaCodingEnd,transcripts.exons.start,"
//                        + "transcripts.exons.genomicCodingStart,transcripts.exons.genomicCodingEnd,"
//                        + "transcripts.exons.cdsStart,transcripts.exons.cdsEnd,"
//                        + "transcripts.exons.end")).getResult();
//
//        return hgvsCalculator.run(variant, geneList);
//    }

}