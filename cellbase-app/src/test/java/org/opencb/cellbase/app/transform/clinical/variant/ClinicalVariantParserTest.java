package org.opencb.cellbase.app.transform.clinical.variant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.util.JSON;
import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Created by fjlopez on 07/10/16.
 */
public class ClinicalVariantParserTest {
    private static final String SYMBOL = "symbol";
    private static final String DOCM = "docm";

    @Test
    public void parse() throws Exception {
        Path clinicalVariantFolder = Paths.get(getClass().getResource("/clinicalVariant").toURI());
        Path genomeSequenceFilePath = Paths.get(getClass()
                .getResource("/clinicalVariant/Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz").toURI());

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), EtlCommons.CLINICAL_VARIANTS_DATA, true);
        (new ClinicalVariantParser(clinicalVariantFolder, genomeSequenceFilePath, "GRCh37",  serializer)).parse();

        List<Variant> variantList = loadSerializedVariants("/tmp/" + EtlCommons.CLINICAL_VARIANTS_JSON_FILE);
        assertEquals(12, variantList.size());

        Variant variant = getVariantByVariant(variantList,
                new Variant("1", 11169361, "C", "G"));
        assertNotNull(variant);
        EvidenceEntry checkEvidenceEntry = getEvidenceEntryBySource(variant.getAnnotation().getTraitAssociation(), DOCM);
        assertNotNull(checkEvidenceEntry);
        assertThat(checkEvidenceEntry.getGenomicFeatures().stream()
                        .map(genomicFeature -> genomicFeature.getXrefs() != null ?
                                genomicFeature.getXrefs().get(SYMBOL) : null).collect(Collectors.toList()),
                CoreMatchers.hasItems("MTOR"));
        assertThat(checkEvidenceEntry.getGenomicFeatures().stream()
                        .map(genomicFeature -> genomicFeature.getEnsemblId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("ENST00000361445"));
        assertEquals(1, checkEvidenceEntry.getHeritableTraits().size());
        assertEquals("renal carcinoma", checkEvidenceEntry.getHeritableTraits().get(0).getTrait());
        assertEquals(ClinicalSignificance.likely_pathogenic,
                checkEvidenceEntry.getVariantClassification().getClinicalSignificance());
        assertEquals(1, variant.getAnnotation().getDrugs().size());
        assertEquals(new Drug("rapamycin", "activation", "gain-of-function",
                null, "preclinical", "emerging", Collections.singletonList("PMID:24631838")),
                variant.getAnnotation().getDrugs().get(0));

        variant = getVariantByVariant(variantList,
                new Variant("1", 11169375, "A", "C"));
        assertNotNull(variant);
        assertEquals(4, variant.getAnnotation().getTraitAssociation().size());
        assertThat(getAllGeneSymbols(variant.getAnnotation().getTraitAssociation(), DOCM),
                CoreMatchers.hasItems("MTOR"));
        assertThat(getAllEnsemblIds(variant.getAnnotation().getTraitAssociation(), DOCM),
                CoreMatchers.hasItems("ENST00000361445"));
        assertThat(getAllTraitNames(variant.getAnnotation().getTraitAssociation(), DOCM),
                CoreMatchers.hasItems("breast cancer", "uterine corpus endometrial carcinoma",
                        "gastric adenocarcinoma", "renal clear cell carcinoma"));

        variant = getVariantByVariant(variantList,
                new Variant("1", 11169377, "T", "A"));
        assertNotNull(variant);
        assertEquals(4, variant.getAnnotation().getTraitAssociation().size());
        assertThat(getAllGeneSymbols(variant.getAnnotation().getTraitAssociation(), DOCM),
                CoreMatchers.hasItems("MTOR"));
        assertThat(getAllEnsemblIds(variant.getAnnotation().getTraitAssociation(), DOCM),
                CoreMatchers.hasItems("ENST00000361445"));
        assertThat(getAllTraitNames(variant.getAnnotation().getTraitAssociation(), DOCM),
                CoreMatchers.hasItems("gastric adenocarcinoma", "renal clear cell carcinoma", "breast cancer",
                        "uterine corpus endometrial carcinoma"));

        variant = getVariantByAccession(variantList, "COSM1193237");
        assertNotNull(variant);
        assertThat(variant.getAnnotation().getTraitAssociation().stream()
                .map(evidenceEntry -> evidenceEntry.getId() != null ? evidenceEntry.getId() : null)
                        .collect(Collectors.toList()),
                CoreMatchers.hasItems("RCV000148505"));

        variant = getVariantByAccession(variantList, "RCV000148485");
        assertNotNull(variant);
        assertThat(variant.getAnnotation().getTraitAssociation().stream()
                        .map(evidenceEntry -> evidenceEntry.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("COSM5745645"));

        variant = getVariantByAccession(variantList, "COSM4059225");
        assertNotNull(variant);

        variant = getVariantByAccession(variantList, "3259");
        assertEquals(Integer.valueOf(7577545), variant.getStart());
        assertEquals("T", variant.getReference());
        assertEquals("C", variant.getAlternate());
        assertEquals("PMID:0008075648",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

        variant = getVariantByAccession(variantList, "5223");
        assertEquals(Integer.valueOf(4), variant.getStart());
        assertEquals("CTTCTCACCCT", variant.getReference());
        assertEquals("", variant.getAlternate());
        assertEquals("PMID:0008479743",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

        variant = getVariantByAccession(variantList, "1590");
        assertEquals(Integer.valueOf(7578502), variant.getStart());
        assertEquals("A", variant.getReference());
        assertEquals("G", variant.getAlternate());
        assertEquals("PMID:0002649981",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

        variant = getVariantByAccession(variantList, "2143");
        assertEquals(Integer.valueOf(7578406), variant.getStart());
        assertEquals("C", variant.getReference());
        assertEquals("T", variant.getAlternate());
        assertEquals("PMID:0002649981",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

        variant = getVariantByAccession(variantList, "1407");
        assertEquals(Integer.valueOf(7578536), variant.getStart());
        assertEquals("T", variant.getReference());
        assertEquals("G", variant.getAlternate());
        assertEquals("PMID:0001694291",
                variant.getAnnotation().getTraitAssociation().get(0).getBibliography().get(0));

    }

    private Set<String> getAllTraitNames(List<EvidenceEntry> evidenceEntryList, String source) {
        Set<String> traitNameSet = new HashSet<>(evidenceEntryList.size());
        for (EvidenceEntry evidenceEntry : evidenceEntryList) {
            if (source.equals(evidenceEntry.getSource().getName())) {
                traitNameSet.addAll(evidenceEntry.getHeritableTraits().stream()
                        .map(heritableTrait -> heritableTrait.getTrait()).collect(Collectors.toSet()));
            }
        }
        return traitNameSet;
    }

    private Set<String> getAllEnsemblIds(List<EvidenceEntry> evidenceEntryList, String source) {
        Set<String> ensemblIdSet = new HashSet<>(evidenceEntryList.size());
        for (EvidenceEntry evidenceEntry : evidenceEntryList) {
            if (source.equals(evidenceEntry.getSource().getName())) {
                ensemblIdSet.addAll(evidenceEntry.getGenomicFeatures().stream()
                        .map(genomicFeature -> genomicFeature.getEnsemblId()).collect(Collectors.toSet()));
            }
        }
        return ensemblIdSet;

    }

    private Set<String> getAllGeneSymbols(List<EvidenceEntry> evidenceEntryList, String source) {
        Set<String> geneSymbolSet = new HashSet<>(evidenceEntryList.size());
        for (EvidenceEntry evidenceEntry : evidenceEntryList) {
            if (source.equals(evidenceEntry.getSource().getName())) {
                geneSymbolSet.addAll(evidenceEntry.getGenomicFeatures().stream()
                                .map(genomicFeature -> genomicFeature.getXrefs() != null ?
                                        genomicFeature.getXrefs().get(SYMBOL) : null).collect(Collectors.toSet()));
            }
        }
        return geneSymbolSet;
    }

    private EvidenceEntry getEvidenceEntryBySource(List<EvidenceEntry> evidenceEntryList, String sourceName) {
        for (EvidenceEntry evidenceEntry : evidenceEntryList) {
            if (sourceName.equals(evidenceEntry.getSource().getName())) {
                return evidenceEntry;
            }
        }
        return null;
    }

    private Variant getVariantByVariant(List<Variant> variantList, Variant variant) {
        for (Variant variant1 : variantList) {
            if (variant.getChromosome().equals(variant1.getChromosome())
                    && variant.getStart().equals(variant1.getStart())
                    && variant.getReference().equals(variant1.getReference())
                    && variant.getAlternate().equals(variant1.getAlternate())) {
                return variant1;
            }
        }
        return null;
    }

    private Variant getVariantByAccession(List<Variant> variantList, String accession) {
        for (Variant variant : variantList) {
            if (variant.getAnnotation().getTraitAssociation() != null) {
                for (EvidenceEntry evidenceEntry : variant.getAnnotation().getTraitAssociation()) {
                    // DOCM does not provide IDs
                    if (evidenceEntry.getId() != null
                    && evidenceEntry.getId().equals(accession)) {
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