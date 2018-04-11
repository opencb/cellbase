package org.opencb.cellbase.app.transform.clinical.variant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.util.JSON;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.EvidenceEntry;
import org.opencb.biodata.models.variant.avro.Property;
import org.opencb.biodata.models.variant.avro.VariantAvro;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Created by fjlopez on 07/10/16.
 */
@Ignore
public class ClinicalVariantParserTest {
    @Test
    public void parse() throws Exception {
        Path clinicalVariantFolder = Paths.get(getClass().getResource("/clinicalVariant").toURI());
        Path genomeSequenceFilePath = Paths.get(getClass()
                .getResource("/clinicalVariant/Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz").toURI());

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "clinical_variant", true);
        (new ClinicalVariantParser(clinicalVariantFolder, genomeSequenceFilePath, "GRCh37",  serializer)).parse();

        List<Variant> parsedVariantList = loadSerializedVariants("/tmp/clinical_variant.json.gz");
        assertEquals(11, parsedVariantList.size());

        // This RCV does not have any genomic feature associated with it (Gene). ClinVar record provides GenotypeSet
        // in this case rather than MeasureSet
        List<Variant> variantList = getVariantByAccession(parsedVariantList, "RCV000169692");
        assertNotNull(variantList);
        assertEquals(2, variantList.size());
        // First variant in the genotype set
        Variant variant = variantList.get(0);
        assertEquals("18", variant.getChromosome());
        assertEquals(Integer.valueOf(56390278), variant.getStart());
        assertEquals("A", variant.getReference());
        assertEquals("G", variant.getAlternate());
        // One evidenceEntry for the variation record, another for the RCV
        assertEquals(2, variant.getAnnotation().getTraitAssociation().size());
        // Check proper variation record is there
        assertThat(variant.getAnnotation().getTraitAssociation().stream()
                        .map(evidenceEntryItem -> evidenceEntryItem.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("187140"));
        EvidenceEntry evidenceEntry = getEvidenceEntryByAccession(variant, "RCV000169692");
        assertNotNull(evidenceEntry);
        assertTrue(evidenceEntry.getGenomicFeatures().isEmpty());
        // Check it's properly flagged as part of the genotype set
        Property property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:56390321:C:-", property.getValue());
        // Second variant in the genotype set
        variant = variantList.get(0);
        assertEquals("18", variant.getChromosome());
        assertEquals(Integer.valueOf(56390321), variant.getStart());
        assertEquals("C", variant.getReference());
        assertEquals("", variant.getAlternate());
        // One evidenceEntry for the variation record, another for the RCV
        assertEquals(2, variant.getAnnotation().getTraitAssociation().size());
        // Check proper variation record is there
        assertThat(variant.getAnnotation().getTraitAssociation().stream()
                        .map(evidenceEntryItem -> evidenceEntryItem.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("187141"));
        evidenceEntry = getEvidenceEntryByAccession(variant, "RCV000169692");
        assertNotNull(evidenceEntry);
        assertTrue(evidenceEntry.getGenomicFeatures().isEmpty());
        // Check it's properly flagged as part of the genotype set
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:56390278:A:G", property.getValue());


        variant = getVariantByAccession(parsedVariantList, "COSM1193237");
        assertNotNull(variant);
        assertThat(variant.getAnnotation().getTraitAssociation().stream()
                .map(evidenceEntryItem -> evidenceEntryItem.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("RCV000148505"));

        variant = getVariantByAccession(parsedVariantList, "RCV000148485");
        assertNotNull(variant);
        assertThat(variant.getAnnotation().getTraitAssociation().stream()
                        .map(evidenceEntryItem -> evidenceEntryItem.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("COSM5745645"));

        variant = getVariantByAccession(parsedVariantList, "COSM4059225");
        assertNotNull(variant);

        variant = getVariantByAccession(parsedVariantList, "3259");
        assertEquals(Integer.valueOf(7577545), variant.getStart());
        assertEquals("T", variant.getReference());
        assertEquals("C", variant.getAlternate());
        assertEquals("PMID:0008075648",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

        variant = getVariantByAccession(parsedVariantList, "5223");
        assertEquals(Integer.valueOf(4), variant.getStart());
        assertEquals("CTTCTCACCCT", variant.getReference());
        assertEquals("", variant.getAlternate());
        assertEquals("PMID:0008479743",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

        variant = getVariantByAccession(parsedVariantList, "1590");
        assertEquals(Integer.valueOf(7578502), variant.getStart());
        assertEquals("A", variant.getReference());
        assertEquals("G", variant.getAlternate());
        assertEquals("PMID:0002649981",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

        variant = getVariantByAccession(parsedVariantList, "2143");
        assertEquals(Integer.valueOf(7578406), variant.getStart());
        assertEquals("C", variant.getReference());
        assertEquals("T", variant.getAlternate());
        assertEquals("PMID:0002649981",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

        variant = getVariantByAccession(parsedVariantList, "1407");
        assertEquals(Integer.valueOf(7578536), variant.getStart());
        assertEquals("T", variant.getReference());
        assertEquals("G", variant.getAlternate());
        assertEquals("PMID:0001694291",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

    }

    private EvidenceEntry getEvidenceEntryByAccession(Variant variant, String accession) {
        for (EvidenceEntry evidenceEntry : variant.getAnnotation().getTraitAssociation()) {
            if (evidenceEntry.getId().equals(accession)) {
                return evidenceEntry;
            }
        }
        return null;
    }

    private Variant getVariantByAccession(List<Variant> variantList, String accession) {
        for (Variant variant : variantList) {
            if (variant.getAnnotation().getTraitAssociation() != null) {
                for (EvidenceEntry evidenceEntry : variant.getAnnotation().getTraitAssociation()) {
                    if (evidenceEntry.getId().equals(accession)) {
                        return variant;
                    }
                }
            }
        }
        return null;
    }

    private List<Variant> loadSerializedVariants(String fileName) {
        List<Variant> variantList = new ArrayList<>(3);

        try {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(Paths.get(fileName));
            ObjectMapper jsonObjectMapper = new ObjectMapper();
            jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
            jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                variantList.add(new Variant(jsonObjectMapper.convertValue(JSON.parse(line), VariantAvro.class)));
            }
        } catch (IOException e) {
            e.printStackTrace();
            assertFalse(false);
        }

        return variantList;
    }

}