package org.opencb.cellbase.app.transform.clinical.variant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.mongodb.util.JSON;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.EvidenceEntry;
import org.opencb.biodata.models.variant.avro.ModeOfInheritance;
import org.opencb.biodata.models.variant.avro.Property;
import org.opencb.biodata.models.variant.avro.VariantAvro;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

/**
 * Created by fjlopez on 07/10/16.
 */
@Ignore
public class ClinicalVariantParserTest {

    private ObjectMapper jsonObjectMapper;

    public ClinicalVariantParserTest() {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void parse() throws Exception {
        Path clinicalVariantFolder = Paths.get(getClass().getResource("/clinicalVariant").toURI());
        Path genomeSequenceFilePath = Paths.get(getClass()
                .getResource("/clinicalVariant/Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz").toURI());

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "clinical_variant", true);
        (new ClinicalVariantParser(clinicalVariantFolder, genomeSequenceFilePath, "GRCh37",  serializer)).parse();

        List<Variant> parsedVariantList = loadSerializedVariants("/tmp/clinical_variant.json.gz");
        assertEquals(14, parsedVariantList.size());

        // ClinVar record with three variants in an Haplotype
        List<Variant> variantList = getVariantByAccession(parsedVariantList, "RCV000000591");
        assertEquals(3, variantList.size());
        // First variant in the haplotype
        Variant variant = variantList.get(0);
        assertEquals("18", variant.getChromosome());
        assertEquals(Integer.valueOf(55217985), variant.getStart());
        assertEquals("A", variant.getReference());
        assertEquals("C", variant.getAlternate());
        // Two evidenceEntry for the two variation records, another for the RCV
        assertEquals(3, variant.getAnnotation().getTraitAssociation().size());
        // Check proper variation record is there
        // This is the variation record that corresponds to the variant alone: there cannot be GenotypeSet property
        EvidenceEntry evidenceEntry = getEvidenceEntryByAccession(variant, "242756");
        assertNotNull(evidenceEntry);
        Property property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNull(property);
        // This is the variation record that corresponds to the compound record: the mate variant string must be within
        // the GenotypeSet property
        evidenceEntry = getEvidenceEntryByAccession(variant, "561");
        assertNotNull(evidenceEntry);
        // Check it's properly flagged as part of the genotype set
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:55217992:A:T,18:55217991:G:A", property.getValue());
        evidenceEntry = getEvidenceEntryByAccession(variant, "RCV000000591");
        assertNotNull(evidenceEntry);
        // Check affected feature (gene symbol) properly parsed
        assertEquals(1, evidenceEntry.getGenomicFeatures().size());
        assertEquals("FECH", evidenceEntry.getGenomicFeatures().get(0).getXrefs().get("symbol"));
        // Check it's properly flagged as part of the genotype set
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:55217992:A:T,18:55217991:G:A", property.getValue());
        // Second variant in the haplotype
        variant = variantList.get(1);
        assertEquals("18", variant.getChromosome());
        assertEquals(Integer.valueOf(55217991), variant.getStart());
        assertEquals("G", variant.getReference());
        assertEquals("A", variant.getAlternate());
        // Two evidenceEntry for the two variation records, another for the RCV
        assertEquals(3, variant.getAnnotation().getTraitAssociation().size());
        // Check proper variation record is there
        // This is the variation record that corresponds to the variant alone: there cannot be GenotypeSet property
        evidenceEntry = getEvidenceEntryByAccession(variant, "242755");
        assertNotNull(evidenceEntry);
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNull(property);
        // This is the variation record that corresponds to the compound record: the mate variant string must be within
        // the GenotypeSet property
        evidenceEntry = getEvidenceEntryByAccession(variant, "561");
        assertNotNull(evidenceEntry);
        // Check it's properly flagged as part of the genotype set
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:55217992:A:T,18:55217985:A:C", property.getValue());
        evidenceEntry = getEvidenceEntryByAccession(variant, "RCV000000591");
        assertNotNull(evidenceEntry);
        // Check affected feature (gene symbol) properly parsed
        assertEquals(1, evidenceEntry.getGenomicFeatures().size());
        assertEquals("FECH", evidenceEntry.getGenomicFeatures().get(0).getXrefs().get("symbol"));
        // Check it's properly flagged as part of the genotype set
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:55217992:A:T,18:55217985:A:C", property.getValue());
        // Third variant in the haplotype
        variant = variantList.get(2);
        assertEquals("18", variant.getChromosome());
        assertEquals(Integer.valueOf(55217992), variant.getStart());
        assertEquals("A", variant.getReference());
        assertEquals("T", variant.getAlternate());
        // Two evidenceEntry for the two variation records, another for the RCV
        assertEquals(3, variant.getAnnotation().getTraitAssociation().size());
        // Check variation records are there
        // This is the variation record that corresponds to the variant alone: there cannot be GenotypeSet property
        evidenceEntry = getEvidenceEntryByAccession(variant, "242821");
        assertNotNull(evidenceEntry);
        // This is the variation record that corresponds to the compound record: the mate variant string must be within
        // the GenotypeSet property
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNull(property);
        evidenceEntry = getEvidenceEntryByAccession(variant, "561");
        assertNotNull(evidenceEntry);
        // Check it's properly flagged as part of the genotype set
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:55217985:A:C,18:55217991:G:A", property.getValue());
        evidenceEntry = getEvidenceEntryByAccession(variant, "RCV000000591");
        assertNotNull(evidenceEntry);
        // Check affected feature (gene symbol) properly parsed
        assertEquals(1, evidenceEntry.getGenomicFeatures().size());
        assertEquals("FECH", evidenceEntry.getGenomicFeatures().get(0).getXrefs().get("symbol"));
        // Check it's properly flagged as part of the genotype set
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:55217985:A:C,18:55217991:G:A", property.getValue());




        // ClinVar record provides GenotypeSet
        // in this case rather than MeasureSet
        variantList = getVariantByAccession(parsedVariantList, "RCV000169692");
        assertEquals(2, variantList.size());
        // First variant in the genotype set
        variant = variantList.get(0);
        assertEquals("18", variant.getChromosome());
        assertEquals(Integer.valueOf(56390278), variant.getStart());
        assertEquals("A", variant.getReference());
        assertEquals("G", variant.getAlternate());
        // Two evidenceEntry for the two variation records, another for the RCV
        assertEquals(3, variant.getAnnotation().getTraitAssociation().size());
        // Check variation records are there
        // This is the variation record that corresponds to the variant alone: there cannot be GenotypeSet property
        evidenceEntry = getEvidenceEntryByAccession(variant, "242617");
        assertNotNull(evidenceEntry);
        // This is the variation record that corresponds to the compound record: the mate variant string must be within
        // the GenotypeSet property
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNull(property);
        evidenceEntry = getEvidenceEntryByAccession(variant, "424712");
        assertNotNull(evidenceEntry);
        // Check it's properly flagged as part of the genotype set
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:56390321:C:", property.getValue());
        evidenceEntry = getEvidenceEntryByAccession(variant, "RCV000169692");
        assertNotNull(evidenceEntry);
        assertEquals(1, evidenceEntry.getGenomicFeatures().size());
        assertEquals("MALT1", evidenceEntry.getGenomicFeatures().get(0).getXrefs().get("symbol"));
        // Check it's properly flagged as part of the genotype set
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:56390321:C:", property.getValue());
        // Second variant in the genotype set
        variant = variantList.get(1);
        assertEquals("18", variant.getChromosome());
        assertEquals(Integer.valueOf(56390321), variant.getStart());
        assertEquals("C", variant.getReference());
        assertEquals("", variant.getAlternate());
        // Two evidenceEntry for the two variation records, another for the RCV
        assertEquals(3, variant.getAnnotation().getTraitAssociation().size());
        // Check proper variation record is there
        // This is the variation record that corresponds to the variant alone: there cannot be GenotypeSet property
        evidenceEntry = getEvidenceEntryByAccession(variant, "242616");
        assertNotNull(evidenceEntry);
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNull(property);
        // This is the variation record that corresponds to the compound record: the mate variant string must be within
        // the GenotypeSet property
        evidenceEntry = getEvidenceEntryByAccession(variant, "424712");
        assertNotNull(evidenceEntry);
        // Check it's properly flagged as part of the genotype set
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:56390278:A:G", property.getValue());
        evidenceEntry = getEvidenceEntryByAccession(variant, "RCV000169692");
        assertNotNull(evidenceEntry);
        assertEquals(1, evidenceEntry.getGenomicFeatures().size());
        assertEquals("MALT1", evidenceEntry.getGenomicFeatures().get(0).getXrefs().get("symbol"));
        // Check it's properly flagged as part of the genotype set
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:56390278:A:G", property.getValue());


        variantList = getVariantByAccession(parsedVariantList, "COSM1193237");
        assertEquals(1, variantList.size());
        variant = variantList.get(0);
        assertThat(variant.getAnnotation().getTraitAssociation().stream()
                .map(evidenceEntryItem -> evidenceEntryItem.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("RCV000148505"));

        variantList = getVariantByAccession(parsedVariantList, "RCV000148485");
        assertEquals(1, variantList.size());
        variant = variantList.get(0);
        assertNotNull(variant);
        assertThat(variant.getAnnotation().getTraitAssociation().stream()
                        .map(evidenceEntryItem -> evidenceEntryItem.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("COSM5745645"));
        // Check mode of inheritance is properly parsed
        evidenceEntry = getEvidenceEntryByAccession(variant, "RCV000148485");
        assertEquals(1, evidenceEntry.getHeritableTraits().size());
        assertEquals(ModeOfInheritance.monoallelic, evidenceEntry.getHeritableTraits().get(0).getInheritanceMode());
        property = getProperty(evidenceEntry.getAdditionalProperties(), "modeOfInheritance");
        ObjectReader reader = jsonObjectMapper.readerFor(jsonObjectMapper.getTypeFactory().constructParametrizedType(List.class, null, Map.class));
        List<Map<String, String>> traitMapList = reader.readValue(property.getValue());
        assertEquals("autosomal dominant inheritance", traitMapList.get(0).get("modeOfInheritance"));


        variantList = getVariantByAccession(parsedVariantList, "COSM4059225");
        assertEquals(1, variantList.size());

        variantList = getVariantByAccession(parsedVariantList, "3259");
        assertEquals(1, variantList.size());
        variant = variantList.get(0);
        assertEquals(Integer.valueOf(7577545), variant.getStart());
        assertEquals("T", variant.getReference());
        assertEquals("C", variant.getAlternate());
        assertEquals("PMID:0008075648",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

        variantList = getVariantByAccession(parsedVariantList, "5223");
        assertEquals(1, variantList.size());
        variant = variantList.get(0);
        assertEquals(Integer.valueOf(4), variant.getStart());
        assertEquals("CTTCTCACCCT", variant.getReference());
        assertEquals("", variant.getAlternate());
        assertEquals("PMID:0008479743",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

        variantList = getVariantByAccession(parsedVariantList, "1590");
        assertEquals(1, variantList.size());
        variant = variantList.get(0);
        assertEquals(Integer.valueOf(7578502), variant.getStart());
        assertEquals("A", variant.getReference());
        assertEquals("G", variant.getAlternate());
        assertEquals("PMID:0002649981",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

        variantList = getVariantByAccession(parsedVariantList, "2143");
        assertEquals(1, variantList.size());
        variant = variantList.get(0);
        assertEquals(Integer.valueOf(7578406), variant.getStart());
        assertEquals("C", variant.getReference());
        assertEquals("T", variant.getAlternate());
        assertEquals("PMID:0002649981",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

        variantList = getVariantByAccession(parsedVariantList, "1407");
        assertEquals(1, variantList.size());
        variant = variantList.get(0);
        assertEquals(Integer.valueOf(7578536), variant.getStart());
        assertEquals("T", variant.getReference());
        assertEquals("G", variant.getAlternate());
        assertEquals("PMID:0001694291",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

    }

    private Property getProperty(List<Property> propertyList, String propertyName) {
        for (Property property : propertyList) {
            if (propertyName.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

    private EvidenceEntry getEvidenceEntryByAccession(Variant variant, String accession) {
        for (EvidenceEntry evidenceEntry : variant.getAnnotation().getTraitAssociation()) {
            if (evidenceEntry.getId().equals(accession)) {
                return evidenceEntry;
            }
        }
        return null;
    }

    private List<Variant> getVariantByAccession(List<Variant> variantList, String accession) {
        List<Variant> returnVariantList = new ArrayList<>();
        for (Variant variant : variantList) {
            if (variant.getAnnotation().getTraitAssociation() != null) {
                int i = 0;
                while (i < variant.getAnnotation().getTraitAssociation().size()
                        && !variant.getAnnotation().getTraitAssociation().get(i).getId().equals(accession)) {
                    i++;
                }
                if (i < variant.getAnnotation().getTraitAssociation().size()) {
                    returnVariantList.add(variant);
                }
            }
        }
        return returnVariantList;
    }

    private List<Variant> loadSerializedVariants(String fileName) {
        List<Variant> variantList = new ArrayList<>(3);

        try {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(Paths.get(fileName));
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