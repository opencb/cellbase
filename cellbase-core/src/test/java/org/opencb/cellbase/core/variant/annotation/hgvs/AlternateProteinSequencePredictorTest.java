package org.opencb.cellbase.core.variant.annotation.hgvs;

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
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by fjlopez on 14/02/17.
 */
public class AlternateProteinSequencePredictorTest {

    private ObjectMapper jsonObjectMapper;
    List<Gene> geneList;

    public AlternateProteinSequencePredictorTest() throws IOException {

    }

    @Before
    public void setUp() throws IOException {

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        geneList = loadGenes(Paths.get(getClass().getResource("/hgvs/gene.test.json.gz").getFile()));
    }

    @Test
    public void testInsert() throws Exception {

        Gene gene = getGene("ENSG00000091536");
        Transcript transcript = getTranscript(gene, "ENST00000418233");
        Variant variant = new Variant("17",
                18173905,
                "-",
                "A");

        AlternateProteinSequencePredictor predictor = new AlternateProteinSequencePredictor(variant, transcript);

        String reference =
                "TCATCCTAGGAGGTGCCTGTGGCCGGGCGCAGTAGCTCATGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGACCACCTGAGGTCAGGAATTTGAGACTAGCCGGCCCAACATGG"
                        + "CGAAACCCCATCTCTACTAAACATACAAAAAATTAGCCAGGCGTCGTGGCGGGCGCCTGTAATCCCAGCTACTCAGGAGGCTGAGGCAGGAGAATCGCTTGAACCCAGGAGG"
                        + "CGGAGCTTGCAGTGGGCCGAGATTGCGCCACTGCACTCTAGCCTGGGGGACAACAGCGAAACTCCGTCTCAAAAATATATATATATATTAATTAAATAAAAAAACGAGGTGC"
                        + "CTTCTCCTGACTCCCTGATCCCCGCGCTCTCCAGCTCTGCCCTCGCGATCGCTGGAGCCCCCTGAGGAACTCACGCAGACGCGGCTGCACCGCCTCATCAATCCCAACTTCT"
                        + "ACGGCTATCAGGACGCCCCCTGGAAGATCTTCCTGCGCAAAGAGGTGTTTTACCCCAAGGACAGCTACAGCCATCCTGTGCAGCTTGACCTCCTGTTCCGGCAGATCCTGCA"
                        + "CGACACGCTCTCCGAGGCCTGCCTTCGCATCTCTGAGGATGAGAGGCTCAGGATGAAGGCCTTGTTTGCCCAGAACCAGCTGGACACACAGAAGCCTCTGGTAACGGAAAGC"
                        + "GTGAAGCGGGCCGTGGTCAGCACTGCACGAGACACCTGGGAGGTCTACTTCTCCCGCATCTTCCCCGCCACGGGCAGCGTGGGCACTGGTGTGCAGCTCCTAGCTGTGTCCC"
                        + "ACGTGGGCATCAAACTCCTGAGGATGGTCAAGGGTGGCCAGGAGGCCGGCGGGCAGCTGCGGGTCCTGCGTGCATACAGCTTTGCAGATATCCTGTTTGTGACCATGCCCTC"
                        + "CCAGAACATGCTGGAGTTCAACCTGGCCAGTGAGAAGGTCATCCTCTTCTCAGCCCGAGCGCACCAGGTCAAGACCCTGGTAGATGACTTCATCTTGGAGCTGAAGAAGGAC"
                        + "TCTGACTACGTGGTCGCTGTGAGGAACTTCCTGCCTGAGGACCCTGCGCTGCTGGCTTTCCACAAGGGTGACATCATACACCTGCAGCCCCTAGAGCCACCTCGAGTGGGCT"
                        + "ACAGTGCTGGCTGCGTGGTTCGCAGGAAGGTGGTGTACCTGGAGGAGCTGCGACGTAGAGGCCCCGACTTTGGCTGGAGGTTCGGGACCATCCACGGGCGCGTGGGCCGCTT"
                        + "CCCTTCGGAGCTGGTGCAGCCCGCTGCTGCCCCCGACTTCCTGCAGCTGCCAACGGAGCCAGGCCGCGGCCGAGCAGCCGCCGTGGCCGCTGCTGTGGCCTCTGCAGCCGCT"
                        + "GCACAGGAGGTGGGCCGCAGGAGAGAGGGTCCCCCAGTCAGGGCCCGCTCTGCTGACCATGGGGAGGACGCCCTGGCGCTCCCACCCTACACAATGCTCGAGTTTGCCCAGA"
                        + "AGTATTTCCGAGACCCTCAGAGGAGACCCCAGGATGGCCTCAGGCTGAAATCCAAGGAGCCTCGGGAGTCCAGAACCTTGGAGGACATGCTTTGCTTCACCAAGACTCCCCT"
                        + "CCAGGAATCCCTCATCGAACTCAGCGACAGCAGCCTCAGCAAGATGGCCACCGACATGTTCCTAGCTGTAATGAGGTTCATGGGGGATGCCCCACTGAAGGGCCAGAGTGAC"
                        + "CTGGACGTGCTTTGTAACCTCCTGAAGCTGTGCGGGGACCATGAGGTCATGCGGGATGAATGTTACTGCCAAGTTGTGAAGCAGATCACAGACAATACCAGCTCCAAGCAGG"
                        + "ACAGCTGCCAGCGAGGCTGGAGGCTGCTGTATATCGTGACCGCCTACCACAGCTGCTCTGAGGTCCTCCACCCACACCTCACTCGCTTCCTCCAAGACGTGAGCCGGACCCC"
                        + "AGGCCTGCCCTTTCAGGGGATCGCCAAGGCCTGCGAGCAGAACCTGCAGAAAACCTTGCGCTTCGGAGGTCGTCTGGAGCTCCCCAGCAGCATAGAGCTTCGGGCCATGTTG"
                        + "GCAGGCCGCAGTTCCAAGAGGCAACTCTTTCTTCTTCCTGGAGGCCTTGAACGCCATCTCAAAATCAAAACATGCACTGTGGCCCTGGACGTGGTGGAAGAGATATGTGCTG"
                        + "AGATGGCTCTGACACGCCCTGAGGCCTTCAATGAATATGTTATCTTCGTTGTCACCAACCGTGGCCAGCATGTGTGCCCACTCAGTCGCCGTGCTTACATCCTGGATGTGGC"
                        + "CTCAGAGATGGAGCAGGTGGACGGCGGCTACATGCTCTGGTTCCGGCGTGTGCTCTGGGATCAGCCACTCAAGTTCGAGAATGAGCTATATGTGACCATGCACTACAACCAG"
                        + "GTCCTGCCTGACTACCTGAAGGGACTCTTCAGCAGTGTGCCGGCCAGCCGGCCCAGCGAGCAGCTGCTGCAGCAGGTGTCCAAGCTGGCTTCACTGCAGCATCGCGCCAAGG"
                        + "ACCACTTCTACCTGCCGAGCGTGCGGGAAGTCCAGGAGTACATCCCAGCCCAGCTCTACCGTACAACGGCAGGCTCGACCTGGCTCAACCTGGTCAGCCAGCACCGGCAGCA"
                        + "GACACAGGCGCTCAGCCCCCACCAGGCCCGTGCCCAGTTTCTGGGCCTCCTCAGCGCCTTACCTATGTTCGGCTCCTCCTTCTTCTTCATCCAGAGCTGCAGCAACATTGCT"
                        + "GTGCCAGCCCCTTGCATCCTTGCCATCAACCACAATGGCCTCAACTTTCTCAGCACAGAGACTCATGAATTGATGGTGAAGTTCCCCCTGAAGGAGATCCAGTCGACGCGGA"
                        + "CCCAGCGGCCCACGGCCAACTCCAGCTACCCCTATGTGGAGATTGCGCTGGGGGACGTGGCGGCCCAGCGCACCTTGCAAGCTGCAGCTGGAGCAGGTAAGAGCTGGGGAAG"
                        + "TGTTGGATGGGCGTGGACTGTCACTGTCACCTGCAGGGACTGGAACTGTGTCGTGTGGTGGCCGTGCACGTGGAGAACCTGCTCAGTGCCCATGAGAAGCGGCTCACATTGC"
                        + "CCCCCAGCGAGATCACCCTGCTCTGACCCAGCCCCCAGCCCTCCAGTACCTTCTGCCAGAAGACTCACTGTGTGGCCTCAGAGAAATCACTGAACCTCTCAGGATCAATGAC"
                        + "CCCTGTAAGGGGCCAGAGCCTTGGAGGACACTAAGAGGAGGCAGGAGGAGCAACTCAAATCCCCAAGAACACAAGAAGACCCATCCTGAACTGGGATGGAATGGCAGCATGC"
                        + "AAACTTGGATCAGATAGCAGGAGGAACTTTCAAAAGTCTGGCCCACTGTGCAGTGGAGCAGAAGGCAGGACCATGAGGCCTCCTGCCATGTACCCATTGCAGACCCTGCCCC"
                        + "TAACTCCTGCCTATGACACAGAAGCCCCACACCAGTTGCCCA";

        String actual = predictor.getAlternateDnaSequence();
        Assert.assertEquals(reference, actual);
    }

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