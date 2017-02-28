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
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.QueryOptions;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 14/02/17.
 */
public class HgvsCalculatorTest {
    private HgvsCalculator hgvsCalculator;
    private GeneDBAdaptor geneDBAdaptor;

    @Before
    public void init() {
        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();
        try {
            cellBaseConfiguration = CellBaseConfiguration
                    .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        hgvsCalculator = new HgvsCalculator(dbAdaptorFactory.getGenomeDBAdaptor("hsapiens", "GRCh37"));
        geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor("hsapiens", "GRCh37");

    }

    @Test
    public void run() throws Exception {

        List<String> hgvsList = getVariantHgvs(new Variant("19:45411941:T:C"));
        assertEquals(4, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000434152(ENSG00000130203):c.466T>C",
                "ENST00000425718(ENSG00000130203):c.388T>C", "ENST00000252486(ENSG00000130203):c.388T>C",
                "ENST00000446996(ENSG00000130203):c.388T>C"));
//        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000434152.1:c.466T>C", "ENST00000425718.1:c.388T>C",
//                "ENST00000252486.4:c.388T>C", "ENST00000446996.1:c.388T>C"));

        hgvsList = getVariantHgvs(new Variant("17", 4542753, "G", "A"));
        assertEquals(6, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000293761(ENSG00000161905):c.309C>T",
                "ENST00000574640(ENSG00000161905):c.192C>T", "ENST00000572265(ENSG00000161905):c.-145C>T",
                "ENST00000545513(ENSG00000161905):c.375C>T","ENST00000570836(ENSG00000161905):c.309C>T",
                "ENST00000576394(ENSG00000161905):c.309C>T"));
//        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000293761.3:c.309C>T",
//                "ENST00000574640.1:c.192C>G", "ENST00000572265.1:c.-145C>T",
//                "ENST00000545513.1:c.375C>T","ENST00000570836.1:c.309C>T","ENST00000576394.1:c.309C>T",
//                "ENST00000574640.1:c.192C>T"));

        hgvsList = getVariantHgvs(new Variant("13", 20600928, "-", "A"));
        assertEquals(4, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000382871(ENSG00000121741):c.1735+32dupA",
                "ENST00000382874(ENSG00000121741):c.1735+32dupA", "ENST00000382883(ENSG00000121741):c.181+32dupA",
                "ENST00000382869(ENSG00000121741):c.1735+32dupA"));
//        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000382871.2:c.1735+32dupA",
//                "ENST00000382874.2:c.1735+32dupA", "ENST00000382883.3:c.181+32dupA",
//                "ENST00000382869.3:c.1735+32dupA"));

        hgvsList = getVariantHgvs(new Variant("13", 19752539, "AA", "-"));
        assertEquals(1, hgvsList.size());
        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000400113(ENSG00000198033):c.227-6_227-5delTT"));
//        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000400113.3:c.227-6_227-5delTT"));

        hgvsList = getVariantHgvs(new Variant("13", 28835528, "-", "C"));
        assertEquals(1, hgvsList.size());
//        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000400113(ENSG00000198033):c.227-6_227-5delTT"));
//        assertThat(hgvsList, CoreMatchers.hasItems("ENST00000400113.3:c.227-6_227-5delTT"));



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