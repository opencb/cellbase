package org.opencb.cellbase.app.transform;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.biodata.models.variant.avro.Xref;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;

/**
 * Created by parce on 03/12/15.
 */
public class VariationParserTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
        // TODO: remove temp files
    }

    @Test
    public void testParse() throws Exception {
        TestSerializer testSerializer = new TestSerializer();
        VariationParser parser = new VariationParser(Paths.get(this.getClass().getResource("/variationParser").getPath()), testSerializer);
        parser.parse();

        Set<String> outputFileNames = checkOutputFileNames(testSerializer.serializedVariants);
        checkVariants(testSerializer.serializedVariants);

    }

    private Set<String> checkOutputFileNames(Map<String, List<Variant>> serializedVariantsMap) {
        Set<String> outputFileNames = serializedVariantsMap.keySet();
        assertTrue(outputFileNames.contains("variation_chr14"));
        assertTrue(outputFileNames.contains("variation_chr1"));
        assertTrue(outputFileNames.contains("variation_chr7"));
        return outputFileNames;
    }

    private void checkVariants(Map<String, List<Variant>> serializedVariantsMap) {

        // chr1 variants
        List<Variant> chr1Variations = serializedVariantsMap.get("variation_chr1");
        assertEquals(2, chr1Variations.size());
        // first alternate
        Variant variant = chr1Variations.stream().filter(v -> v.getAlternate().equals("A")).findFirst().get();
        checkVariant(variant, "G", "A", 112954964, 112954964, "1", "rs1412931", VariantType.SNV);
        VariantAnnotation annotation = variant.getAnnotation();
        List<ConsequenceType> consequenceTypes = annotation.getConsequenceTypes();
        assertEquals(6, consequenceTypes.size());
        // TODO consequence types details
        assertEquals(11, annotation.getPopulationFrequencies().size());
        // TODO frequencies
        // TODO hgvs
        // TODO check xrefs
        variant = chr1Variations.stream().filter(v -> v.getAlternate().equals("C")).findFirst().get();
        checkVariant(variant, "G", "C", 112954964, 112954964, "1", "rs1412931", VariantType.SNV);
        annotation = variant.getAnnotation();
        consequenceTypes = annotation.getConsequenceTypes();
        assertEquals(6, consequenceTypes.size());
        // TODO consequence types details
        assertEquals(6, annotation.getPopulationFrequencies().size());
        // TODO frequencies
        // TODO hgvs
        // TODO check xrefs

        List<Variant> chr7Variations = serializedVariantsMap.get("variation_chr7");
        assertEquals(1, chr7Variations.size());
        variant = chr7Variations.get(0);
        checkVariant(variant, "G", "A", 54421937, 54421937, "1", "rs1404666", VariantType.SNV);
        annotation = variant.getAnnotation();
        consequenceTypes = annotation.getConsequenceTypes();
        assertEquals(2, consequenceTypes.size());
        // TODO consequence types details
        assertEquals(0, annotation.getPopulationFrequencies().size());
        // TODO frequencies
        // TODO hgvs
        // TODO check xrefs

        List<Variant> chr14Variations = serializedVariantsMap.get("variation_chr14");
        assertEquals(2, chr14Variations.size());
        variant = chr14Variations.stream().filter(v -> v.getAlternate().equals("C")).findFirst().get();
        checkVariant(variant, "A", "C", 77697967, 77697967, "1", "rs375566", VariantType.SNV);
        annotation = variant.getAnnotation();
        consequenceTypes = annotation.getConsequenceTypes();
        assertEquals(4, consequenceTypes.size());
        // TODO consequence types details
        assertEquals(0, annotation.getPopulationFrequencies().size());
        // TODO frequencies
        // TODO hgvs
        // TODO check xrefs
        variant = chr14Variations.stream().filter(v -> v.getAlternate().equals("G")).findFirst().get();
        checkVariant(variant, "A", "G", 77697967, 77697967, "1", "rs375566", VariantType.SNV);
        annotation = variant.getAnnotation();
        consequenceTypes = annotation.getConsequenceTypes();
        assertEquals(4, consequenceTypes.size());
        // TODO consequence types details
        assertEquals(22, annotation.getPopulationFrequencies().size());
        // TODO frequencies
        // TODO hgvs
        // TODO check xrefs
    }

    private void checkVariant(Variant variant, String expectedReference, String expectedAlternate, Integer expectedStart,
                              Integer expectedEnd, String expectedStrand, String expectedId, VariantType expectedVariantType) {
        assertEquals(expectedReference, variant.getReference());
        assertEquals(expectedAlternate, variant.getAlternate());
        assertEquals(expectedStart, variant.getStart());
        assertEquals(expectedEnd, variant.getEnd());
        assertEquals(expectedStrand, variant.getStrand());
        assertEquals(1, variant.getIds().size());
        assertEquals(expectedId, variant.getIds().get(0));
        assertEquals(expectedVariantType, variant.getType());
    }

    class TestSerializer implements CellBaseFileSerializer {

        Map<String, List<Variant>> serializedVariants = new HashMap<>();

        @Override
        public void serialize(Object object) {

        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public void serialize(Object object, String fileName) {
            List fileNameList = serializedVariants.getOrDefault(fileName, new ArrayList<>());
            fileNameList.add(object);
            serializedVariants.put(fileName, fileNameList);
        }

    }
}
