package org.opencb.cellbase.lib.variant.annotation.hgvs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.util.ajax.JSON;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.variant.annotation.hgvs.HgvsTranscriptCalculator;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;
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
 * ensembl v90
 * tested with cellbase 4.8
 *
 * (most tests are in hgvscalculatortest but those are for grch37)
 */
public class HgvsTranscriptCalculatorTest extends GenericMongoDBAdaptorTest {


    private ObjectMapper jsonObjectMapper;
    List<Gene> geneList;
    private GenomeDBAdaptor genomeDBAdaptor;
    protected DBAdaptorFactory dbAdaptorFactory;

    public HgvsTranscriptCalculatorTest() throws IOException {

    }

    // TODO add KeyError: '1:244856830:T:-', generated an error in the python script

    @Before
    public void setUp() throws Exception {


        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/hgvs/gene_grch38.test.json.gz").toURI());
        loadRunner.load(path, "gene");
        path = Paths.get(getClass()
                .getResource("/hgvs/genome_sequence_grch38.test.json.gz").toURI());
        loadRunner.load(path, "genome_sequence");

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        geneList = loadGenes(Paths.get(getClass().getResource("/hgvs/gene_grch38.test.json.gz").getFile()));

        CellBaseConfiguration cellBaseConfiguration = CellBaseConfiguration
                .load(GenericMongoDBAdaptorTest.class.getClassLoader().getResourceAsStream("configuration.test.json"));
        dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor("hsapiens", "GRCh37");


    }

    @Test
    public void testArrayOutOfBounds() throws Exception {
//        14:105367307:-:CCCTGTCCAGCCAGCCCATTGACCACGAAGACAGCACCATGCAGGCCGGACAGGGAGGCGATCCAGATCTCGG	14	105367307	-	CCCTGTCCAGCCAGCCCATTGACCACGAAGACAGCACCATGCAGGCCGGACAGGGAGGCGATCCAGATCTCGG	indel	ENSP00000393559	p.Pro190GlnfsTer15	p.Pro190GlnfsTer15
        Gene gene = getGene("ENSG00000179364");
        Transcript transcript = getTranscript(gene, "ENST00000447393");
        Variant variant = new Variant("14",
                105367307,
                "-",
                "CCCTGTCCAGCCAGCCCATTGACCACGAAGACAGCACCATGCAGGCCGGACAGGGAGGCGATCCAGATCTCGG");

        HgvsTranscriptCalculator predictor = new HgvsTranscriptCalculator(genomeDBAdaptor, variant, transcript,"ENSG00000179364");
        Assert.assertEquals("p.Trp845_Val848delinsLeu", predictor.calculate());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////// UTILS ///////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Transcript getTranscript(Gene gene, String id) {
        for (Transcript transcript : gene.getTranscripts()) {
            if (transcript.getId().equals(id)) {
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
