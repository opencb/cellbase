package org.opencb.cellbase.lib.variant.hgvs;

import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.exception.CellBaseException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HgvsCalculatorUnitTest {

    @Test
    void shouldProduceOneWellFormedHgvsEntryForASNVVariant() throws CellBaseException {

        HgvsCalculator hgvsCalculator = new HgvsCalculator(null, 1);
        Gene gene = new Gene();
        gene.setId("ENSG00000000001");
        Transcript transcript = new Transcript();
        transcript.setId("ENST00000000001");
        transcript.setChromosome("1");
        transcript.setStart(1);
        transcript.setEnd(2);
        transcript.setStrand("+");
        transcript.setCdnaCodingEnd(1);
        Exon exon = new Exon();
        exon.setStart(1);
        exon.setEnd(2);
        transcript.setExons(Arrays.asList(exon));
        gene.setTranscripts(Arrays.asList(transcript));
        Variant variant = Variant.newBuilder("1", 1, 2, "A", "T").build();
        variant.setType(VariantType.SNV);
        List<String> result = hgvsCalculator.run(variant, Arrays.asList(gene), false);

        assertEquals(Arrays.asList("ENST00000000001(ENSG00000000001):c.*A>T"), result);
    }

    @Test
    void shouldHaveAPdotValueFollowingACorrespondingCdotValue() throws CellBaseException {

        final String _10_GCC = String.join("", Collections.nCopies(10, "GCC"));
        final String _9_A = String.join("", Collections.nCopies(9, "A"));

        final String trans1 = "ENST00000000001";
        final String trans2 = "ENST00000000002";
        final String trans3 = "ENST00000000003";

        final String gene1 = "ENSG00000000001";

        final String protein1 = "ENSP00000000001";
        final String protein2 = "ENSP00000000002";
        final String protein3 = "ENSP00000000003";

        HgvsCalculator hgvsCalculator = new HgvsCalculator(null, 1);

        // Three transcripts whose coding sequences overlap the variant position
        Transcript transcript1 = new Transcript()
                .setId(trans1)
                .setChromosome("1")
                .setStart(15)
                .setEnd(45)
                .setStrand("+")
                .setCdnaCodingEnd(30)
                .setCdnaSequence(_10_GCC)
                .setProteinSequence(_9_A)
                .setGenomicCodingStart(15)
                .setGenomicCodingEnd(45)
                .setCdsLength(33)
                .setProteinId(protein1)
                .setExons(Arrays.asList(new Exon().setStart(15).setEnd(30)));

        Transcript transcript2 = new Transcript()
                .setId(trans2)
                .setChromosome("1")
                .setStart(18)
                .setEnd(48)
                .setStrand("+")
                .setCdnaCodingEnd(30)
                .setCdnaSequence(_10_GCC)
                .setProteinSequence(_9_A)
                .setGenomicCodingStart(18)
                .setGenomicCodingEnd(48)
                .setCdsLength(33)
                .setProteinId(protein2)
                .setExons(Arrays.asList(new Exon().setStart(15).setEnd(30)));

        Transcript transcript3 = new Transcript()
                .setId(trans3)
                .setChromosome("1")
                .setStart(21)
                .setEnd(51)
                .setStrand("+")
                .setCdnaCodingEnd(30)
                .setCdnaSequence(_10_GCC)
                .setProteinSequence(_9_A)
                .setGenomicCodingStart(21)
                .setGenomicCodingEnd(51)
                .setCdsLength(33)
                .setProteinId(protein3)
                .setExons(Arrays.asList(new Exon().setStart(15).setEnd(30)));


        Gene gene = new Gene()
                .setId(gene1)
                .setTranscripts(Arrays.asList(transcript1, transcript2, transcript3));

        Variant variant = Variant.newBuilder("1", 22, 22, "C", "G").build();
        variant.setType(VariantType.SNV);

        assertEquals(Arrays.asList(
                trans1 + "(" + gene1 + "):c.22C>G",
                protein1 + ":p.Arg8Gly",
                trans2 + "(" + gene1 + "):c.22C>G",
                protein2 + ":p.Arg8Gly",
                trans3 + "(" + gene1 + "):c.22C>G",
                protein3 + ":p.Arg8Gly"
        ), hgvsCalculator.run(variant, Arrays.asList(gene), false));
    }
}
