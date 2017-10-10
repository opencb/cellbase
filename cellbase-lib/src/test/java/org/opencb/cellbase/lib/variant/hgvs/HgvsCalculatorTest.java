package org.opencb.cellbase.lib.variant.hgvs;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.variant.annotation.hgvs.HgvsCalculator;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.QueryOptions;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 14/02/17.
 */
public class HgvsCalculatorTest extends GenericMongoDBAdaptorTest {
    private HgvsCalculator hgvsCalculator;
    private GeneDBAdaptor geneDBAdaptor;

    public HgvsCalculatorTest() throws IOException {
    }

    @Before
    public void init() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/hgvs/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene");
        path = Paths.get(getClass()
                .getResource("/hgvs/genome_sequence.test.json.gz").toURI());
        loadRunner.load(path, "genome_sequence");
        hgvsCalculator = new HgvsCalculator(dbAdaptorFactory.getGenomeDBAdaptor("hsapiens", "GRCh37"));
        geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor("hsapiens", "GRCh37");
    }

    @Test
    public void run() throws Exception {

        List<String> hgvsList = getVariantHgvs(new Variant("19", 45411941, "T", "C"));
        assertEquals(4, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000252486(ENSG00000130203):c.388T>C",
                "ENST00000446996(ENSG00000130203):c.388T>C", "ENST00000434152(ENSG00000130203):c.466T>C",
                "ENST00000425718(ENSG00000130203):c.388T>C"));

        hgvsList = getVariantHgvs(new Variant("1", 136024, "C", "T"));
        assertEquals(1, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000423372(ENSG00000237683):c.*910-222G>A"));

        hgvsList = getVariantHgvs(new Variant("13", 25457289, "G", "A"));
        assertEquals(2, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000381884(ENSG00000151849):c.*26C>T",
                "ENST00000545981(ENSG00000151849):c.*697C>T"));

        hgvsList = getVariantHgvs(new Variant("13", 25496789, "C", "G"));
        assertEquals(2, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000381884(ENSG00000151849):c.-67+110G>C",
                "ENST00000545981(ENSG00000151849):c.-67+110G>C"));

        hgvsList = getVariantHgvs(new Variant("13", 25487369, "G", "A"));
        assertEquals(2, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000381884(ENSG00000151849):c.-66-140C>T",
                "ENST00000545981(ENSG00000151849):c.-66-140C>T"));

        hgvsList = getVariantHgvs(new Variant("13", 25459270, "C", "T"));
        assertEquals(3, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000471870(ENSG00000151849):n.367+144G>A",
                "ENST00000381884(ENSG00000151849):c.3477+144G>A",
                "ENST00000545981(ENSG00000151849):c.*131+144G>A"));

        hgvsList = getVariantHgvs(new Variant("13", 26967553, "A", "G"));
        assertEquals(4, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000477290(ENSG00000132964):n.510A>G",
                "ENST00000465820(ENSG00000132964):n.133-2869A>G",
                "ENST00000381527(ENSG00000132964):c.696A>G",
                "ENST00000536792(ENSG00000132964):c.*143A>G"));

        hgvsList = getVariantHgvs(new Variant("13", 26966929, "G", "A"));
        assertEquals(4, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000477290(ENSG00000132964):n.461-575G>A",
                "ENST00000465820(ENSG00000132964):n.133-3493G>A",
                "ENST00000381527(ENSG00000132964):c.647-575G>A",
                "ENST00000536792(ENSG00000132964):c.*94-575G>A"));

        hgvsList = getVariantHgvs(new Variant("13", 26962152, "T", "C"));
        assertEquals(4, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000477290(ENSG00000132964):n.460+2673T>C",
                "ENST00000465820(ENSG00000132964):n.132+2673T>C",
                "ENST00000381527(ENSG00000132964):c.646+2673T>C",
                "ENST00000536792(ENSG00000132964):c.*93+2673T>C"));

        hgvsList = getVariantHgvs(new Variant("11", 62543180, "A", "G"));
        assertEquals(8, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000524976(ENSG00000162227):n.90-63A>G",
                "ENST00000532915(ENSG00000162227):n.89-63A>G",
                "ENST00000527073(ENSG00000168569):n.66-1043T>C",
                "ENST00000294168(ENSG00000162227):c.-13-63A>G",
                "ENST00000526261(ENSG00000162227):c.-76A>G",
                "ENST00000525405(ENSG00000162227):c.-13-63A>G",
                "ENST00000529509(ENSG00000162227):c.-13-63A>G",
                "ENST00000528367(ENSG00000168569):c.315-1043T>C"));

        hgvsList = getVariantHgvs(new Variant("2", 191399259, "-", "CGC"));
        assertEquals(2, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000409150(ENSG00000189362):c.97+24_97+26dupGCG",
                "ENST00000343105(ENSG00000189362):c.97+24_97+26dupGCG"));

        hgvsList = getVariantHgvs(new Variant("19:45411941:T:C"));
        assertEquals(4, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000434152(ENSG00000130203):c.466T>C",
                "ENST00000425718(ENSG00000130203):c.388T>C", "ENST00000252486(ENSG00000130203):c.388T>C",
                "ENST00000446996(ENSG00000130203):c.388T>C"));

        hgvsList = getVariantHgvs(new Variant("17", 4542753, "G", "A"));
        assertEquals(7, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000573740(ENSG00000161905):n.336C>T",
                "ENST00000293761(ENSG00000161905):c.309C>T",
                "ENST00000574640(ENSG00000161905):c.192C>T", "ENST00000572265(ENSG00000161905):c.-145C>T",
                "ENST00000545513(ENSG00000161905):c.375C>T","ENST00000570836(ENSG00000161905):c.309C>T",
                "ENST00000576394(ENSG00000161905):c.309C>T"));

        hgvsList = getVariantHgvs(new Variant("13", 20600928, "-", "A"));
        assertEquals(6, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000468677(ENSG00000121741):n.220+32dupA",
                "ENST00000382870(ENSG00000121741):n.244+32dupA",
                "ENST00000382871(ENSG00000121741):c.1735+32dupA",
                "ENST00000382874(ENSG00000121741):c.1735+32dupA", "ENST00000382883(ENSG00000121741):c.181+32dupA",
                "ENST00000382869(ENSG00000121741):c.1735+32dupA"));

        hgvsList = getVariantHgvs(new Variant("13", 19752539, "AA", "-"));
        assertEquals(1, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000400113(ENSG00000198033):c.227-6_227-5delTT"));

        hgvsList = getVariantHgvs(new Variant("13", 28835528, "-", "C"));
        assertEquals(4, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000503791(ENSG00000152520):n.1506-11_1506-10insC",
                "ENST00000380958(ENSG00000152520):c.1354-11_1354-10insC",
                "ENST00000399613(ENSG00000152520):c.754-11_754-10insC",
                "ENST00000282391(ENSG00000152520):c.418-11_418-10insC"));

        hgvsList = getVariantHgvs(new Variant("22", 17488824, "-", "G"));
        assertEquals(3, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000523144(ENSG00000215568):n.59+7_59+8insC",
                "ENST00000400588(ENSG00000215568):c.174+7_174+8insC",
                "ENST00000465611(ENSG00000215568):c.59+7_59+8insC"));

        hgvsList = getVariantHgvs(new Variant("5", 1093610, "-", "GGGCGGGGACT"));
        assertEquals(1, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000264930(ENSG00000113504):c.342+28_342+38dup11"));

        hgvsList = getVariantHgvs(new Variant("2", 179622239, "TCAAAG", "-"));
        assertEquals(10, hgvsList.size());
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
        assertEquals(9, hgvsList.size());
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
        assertEquals(6, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000372199(ENSG00000133665):c.270+51_270+52ins5",
                "ENST00000372198(ENSG00000133665):c.312+51_312+52ins5",
                "ENST00000372197(ENSG00000133665):c.270+51_270+52ins5",
                "ENST00000444807(ENSG00000133665):c.270+51_270+52ins5",
                "ENST00000256039(ENSG00000133665):c.270+51_270+52ins5",
                "ENST00000444807(ENSG00000133665):c.270+51_270+52ins5"));

    }

    private List<String> getVariantHgvs(Variant variant) {

        List<Gene> geneList = geneDBAdaptor
                .getByRegion(new Region(variant.getChromosome(), variant.getStart(),
                        variant.getEnd()), new QueryOptions("include", "name,id,transcripts.id,"
                        + "transcripts.strand,transcripts.name,transcripts.start,transcripts.end,"
                        + "transcripts.genomicCodingStart,transcripts.genomicCodingEnd,transcripts.cdnaCodingStart,"
                        + "transcripts.cdnaCodingEnd,transcripts.exons.start,"
                        + "transcripts.exons.cdsStart,transcripts.exons.cdsEnd,"
                        + "transcripts.exons.end")).getResult();

        return hgvsCalculator.run(variant, geneList);
    }

}