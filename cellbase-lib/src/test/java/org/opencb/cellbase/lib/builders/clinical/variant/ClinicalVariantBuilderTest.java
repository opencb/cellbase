/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.lib.builders.clinical.variant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.mongodb.util.JSON;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;


import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by fjlopez on 07/10/16.
 */
public class ClinicalVariantBuilderTest {
    private static final String SYMBOL = "symbol";
    private static final String DOCM = "docm";

    private ObjectMapper jsonObjectMapper;

    public ClinicalVariantBuilderTest() {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void testDecompose() throws Exception {
        // Remove all previous clinical variant temporary test data
        cleanUp();

/**
 * clinvar id 266834
 * 	chr13:32316508:GAC:ATCGATCGAT
 *
 * chr13:32316508:G:ATCGATCG insertion >> chr13:32316508:G:ATCGATCG
 * chr13:32316510:C:T SNV >> chr13:32316517:C:T
 *
 * Position wrong, and ID wrong
 *
 * clinvar 438988
 * 	chr13:32339556:AAAAA:GAAAAG
 * chr13:32339555:G:GG >> chr13:32339556::G
 * chr13:32339560:A:G >> chr13:32339561:A:G
 *Accession:
 *     VCV000438988.2 == RCV000507387
 * Variation ID:
 *     438988
 * correct ID, wrong positions
 */
        Path clinicalVariantFolder = Paths.get(getClass().getResource("/variant/annotation/clinicalVariant/grch38").toURI());
        org.apache.commons.io.FileUtils.copyDirectory(clinicalVariantFolder.toFile(), Paths.get("/tmp/clinicalVariant4").toFile());
        clinicalVariantFolder = Paths.get("/tmp/clinicalVariant4");

        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/Homo_sapiens.GRCh38.90.dna.primary_assembly.chr13.fa.gz").toURI()).toFile(),
                clinicalVariantFolder.resolve("Homo_sapiens.GRCh38.90.dna.primary_assembly.chr13.fa.gz").toFile());
        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/Homo_sapiens.GRCh38.90.dna.primary_assembly.chr13.fa.fai").toURI()).toFile(),
                clinicalVariantFolder.resolve("Homo_sapiens.GRCh38.90.dna.primary_assembly.chr13.fa.gz.fai").toFile());
        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/Homo_sapiens.GRCh38.90.dna.primary_assembly.chr13.fa.gzi").toURI()).toFile(),
                clinicalVariantFolder.resolve("Homo_sapiens.GRCh38.90.dna.primary_assembly.chr13.fa.gz.gzi").toFile());

        Path genomeSequenceFilePath = clinicalVariantFolder.resolve("Homo_sapiens.GRCh38.90.dna.primary_assembly.chr13.fa.gz");

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), EtlCommons.CLINICAL_VARIANTS_DATA, true);
        (new ClinicalVariantParser(clinicalVariantFolder, true, genomeSequenceFilePath, "GRCh38",  serializer)).parse();

        List<Variant> parsedVariantList = loadSerializedVariants("/tmp/" + EtlCommons.CLINICAL_VARIANTS_JSON_FILE);
        assertEquals(2, parsedVariantList.size());


        List<Variant> variantList = getVariantByAccession(parsedVariantList, "RCV000507387");
        assertEquals(2, variantList.size());
        Variant variant = variantList.get(0);
        assertEquals("13", variant.getChromosome());
        assertEquals(Integer.valueOf(32339556), variant.getStart());
        assertEquals("", variant.getReference());
        assertEquals("G", variant.getAlternate());

    }

    @Test
    public void noNormaliseTest() throws Exception {
        // Remove all previous clinical variant temporary test data
        cleanUp();

        // Copy clinical variant test data to tmp
        // NOTE: mvn/idea tests run created lots of problems with running all tests in this ClinicalVariantParserTest
        // in one go; summarising there's some problem with rocks db directories and the way running the tests handles
        // temporary files/directories. Best solution found was to make different copies fo the data to the tmp and run
        // each test over a separate copy of the test data. Note the 3 on /tmp/clinicalVariant3 below
        Path clinicalVariantFolder = Paths.get(getClass().getResource("/variant/annotation/clinicalVariant").toURI());
        org.apache.commons.io.FileUtils.copyDirectory(clinicalVariantFolder.toFile(),
                Paths.get("/tmp/clinicalVariant3").toFile());
        clinicalVariantFolder = Paths.get("/tmp/clinicalVariant3");

        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz").toURI()).toFile(),
                clinicalVariantFolder.resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz").toFile());
        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz.fai").toURI()).toFile(),
                clinicalVariantFolder.resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz.fai").toFile());
        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz.gzi").toURI()).toFile(),
                clinicalVariantFolder.resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz.gzi").toFile());

        Path genomeSequenceFilePath = clinicalVariantFolder.resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz");

        Path clinicalVariantChunksFolder = Paths.get("/tmp/clinicalVariant3/clinvar_chunks");
        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/clinicalVariant/ClinVarFullRelease_2020-02.xml.gz").toURI()).toFile(),
                clinicalVariantChunksFolder.resolve("ClinVarFullRelease_2020-02.xml.gz").toFile());

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), EtlCommons.CLINICAL_VARIANTS_DATA, true);
        (new ClinicalVariantBuilder(clinicalVariantFolder, false, genomeSequenceFilePath, "GRCh37",  serializer)).parse();

        List<Variant> parsedVariantList = loadSerializedVariants("/tmp/" + EtlCommons.CLINICAL_VARIANTS_JSON_FILE);
        assertEquals(23, parsedVariantList.size());

        // ClinVar record for an un-normalised variant. It appears in the variant_summary.txt as 17 53	53	C	CC
        // Genome sequence context for that position is TGTCCCTGCTGAA
        //                                                   ^
        //                                                   53
        // After normalisation should be 17 51 -   C
        List<Variant> variantList = getVariantByAccession(parsedVariantList, "RCV000488336");
        assertEquals(1, variantList.size());
        Variant variant = variantList.get(0);
        assertEquals("17", variant.getChromosome());
        assertEquals(Integer.valueOf(53), variant.getStart());
        assertEquals("C", variant.getReference());
        assertEquals("CC", variant.getAlternate());

    }

    @Test
    public void parseMNVTest() throws Exception {

        // Remove all previous clinical variant temporary test data
        cleanUp();

        // Copy clinical variant test data to tmp
        // NOTE: mvn/idea tests run created lots of problems with running all tests in this ClinicalVariantParserTest
        // in one go; summarising there's some problem with rocks db directories and the way running the tests handles
        // temporary files/directories. Best solution found was to make different copies fo the data to the tmp and run
        // each test over a separate copy of the test data. Note the 1 on /tmp/clinicalVariant1 below
        Path clinicalVariantFolder = Paths.get(getClass().getResource("/variant/annotation/clinicalVariant").toURI());
        org.apache.commons.io.FileUtils.copyDirectory(clinicalVariantFolder.toFile(),
                Paths.get("/tmp/clinicalVariant1").toFile());
        clinicalVariantFolder = Paths.get("/tmp/clinicalVariant1");

        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz").toURI()).toFile(),
                clinicalVariantFolder.resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz").toFile());
        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz.fai").toURI()).toFile(),
                clinicalVariantFolder.resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz.fai").toFile());
        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz.gzi").toURI()).toFile(),
                clinicalVariantFolder.resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz.gzi").toFile());


        Path clinicalVariantChunksFolder = Paths.get("/tmp/clinicalVariant1/clinvar_chunks");
        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/clinicalVariant/ClinVarFullRelease_2020-02.xml.gz").toURI()).toFile(),
                clinicalVariantChunksFolder.resolve("ClinVarFullRelease_2020-02.xml.gz").toFile());

        Path genomeSequenceFilePath = clinicalVariantFolder.resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz");

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), EtlCommons.CLINICAL_VARIANTS_DATA, true);
        (new ClinicalVariantBuilder(clinicalVariantFolder, true, genomeSequenceFilePath, "GRCh37",  serializer)).parse();

        List<Variant> parsedVariantList = loadSerializedVariants("/tmp/" + EtlCommons.CLINICAL_VARIANTS_JSON_FILE);
        assertEquals(29, parsedVariantList.size());

        // DOCM MNV (from docm.json.gz):
        // "chromosome":"1","start":115256528,"stop":115256529,"reference":"TT","variant":"CA"
        Variant variant1 = getVariantByVariant(parsedVariantList,
                new Variant("1", 115256528, "T", "C"));
        assertNotNull(variant1);
        assertEvidenceEntriesHaplotype("1:115256528:T:C,1:115256529:T:A", variant1);

        variant1 = getVariantByVariant(parsedVariantList,
                new Variant("1", 115256529, "T", "A"));
        assertNotNull(variant1);
        assertEvidenceEntriesHaplotype("1:115256528:T:C,1:115256529:T:A", variant1);

        // MNV present, from variant_summary:
        // 9    107594021        107594034        AGAACTTCCTCTCA  GTACAGTGGCGTGACCTCAGCTCACTGCAACCTCTGCCTCCTGAGTTCAAGTGATTCTCGTGCCTCAGCCTCCCAAGTAGCTGGGATTACAGCTCCTGCCACCACGCCCG
        // Six simple variants to be obtained from its decomposition
        List<Variant> variantList = getVariantByAccession(parsedVariantList, "RCV000010100");
        assertEquals(6, variantList.size());

        // Check corresponding EvidenceEntry objects for all 6 variants have been flagged with the proper "haplotype"
        // additional property
        for (Variant variant : variantList) {
            // Each simple variant must contain three EvidenceEntry objects: one for the variation ID, another one for
            // the RCV and one SCV
            assertEquals(3, variant.getAnnotation().getTraitAssociation().size());
            assertEvidenceEntriesHaplotype("9:107594021:-:GTAC,"
                    + "9:107594023:-:TGGCGTGACCTCAGCTCACTGC,"
                    + "9:107594026:-:CTCTGCCTCCTGAG,"
                    + "9:107594029:-:AAGTGATT,"
                    + "9:107594032:-:GTGCC,"
                    + "9:107594035:-:GCCTCCCAAGTAGCTGGGATTACAGCTCCTGCCACCACGCCCG",
                    variant);
        }
    }

    private void assertEvidenceEntriesHaplotype(String expectedHaplotype, Variant variant) {
        for (EvidenceEntry evidenceEntry : variant.getAnnotation().getTraitAssociation()) {
            assertNotNull(evidenceEntry.getAdditionalProperties());
            Property clinicalHaplotypeProperty = getProperty(evidenceEntry.getAdditionalProperties(),
                    ClinicalIndexer.HAPLOTYPE_FIELD_NAME);
            assertNotNull(clinicalHaplotypeProperty);
            assertEquals(expectedHaplotype, clinicalHaplotypeProperty.getValue());
        }
    }


    @Test
    public void parse() throws Exception {

        // Remove all previous clinical variant temporary test data
        cleanUp();

        // Copy clinical variant test data to tmp
        // NOTE: mvn/idea tests run created lots of problems with running all tests in this ClinicalVariantParserTest
        // in one go; summarising there's some problem with rocks db directories and the way running the tests handles
        // temporary files/directories. Best solution found was to make different copies fo the data to the tmp and run
        // each test over a separate copy of the test data. Note the 2 on /tmp/clinicalVariant2 below
        Path clinicalVariantFolder = Paths.get(getClass().getResource("/variant/annotation/clinicalVariant").toURI());
        org.apache.commons.io.FileUtils.copyDirectory(clinicalVariantFolder.toFile(),
                Paths.get("/tmp/clinicalVariant2").toFile());
        clinicalVariantFolder = Paths.get("/tmp/clinicalVariant2");

        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz").toURI()).toFile(),
                clinicalVariantFolder.resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz").toFile());
        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz.fai").toURI()).toFile(),
                clinicalVariantFolder.resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz.fai").toFile());
        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz.gzi").toURI()).toFile(),
                clinicalVariantFolder.resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz.gzi").toFile());

        Path genomeSequenceFilePath = clinicalVariantFolder.resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz");

        Path clinicalVariantChunksFolder = Paths.get("/tmp/clinicalVariant2/clinvar_chunks");
        org.apache.commons.io.FileUtils.copyFile(Paths.get(getClass()
                        .getResource("/variant/annotation/clinicalVariant/ClinVarFullRelease_2020-02.xml.gz").toURI()).toFile(),
                clinicalVariantChunksFolder.resolve("ClinVarFullRelease_2020-02.xml.gz").toFile());

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), EtlCommons.CLINICAL_VARIANTS_DATA, true);
        (new ClinicalVariantBuilder(clinicalVariantFolder, true, genomeSequenceFilePath, "GRCh37",  serializer)).parse();

        List<Variant> parsedVariantList = loadSerializedVariants("/tmp/" + EtlCommons.CLINICAL_VARIANTS_JSON_FILE);
        assertEquals(29, parsedVariantList.size());

        // ClinVar variant with invalid alternate allele string ("TTBS") must NOT be parsed
        List<Variant> variantList = getVariantByAccession(parsedVariantList, "RCV000820150");
        assertEquals(0, variantList.size());

        // COSMIC SNV with more complicated hgvs c.431-1G>A
        variantList = getVariantByAccession(parsedVariantList, "COSM4450061");
        assertEquals(1, variantList.size());
        Variant variant = variantList.get(0);
        assertEquals("1", variant.getChromosome());
        assertEquals(Integer.valueOf(939039), variant.getStart());
        assertEquals("G", variant.getReference());
        assertEquals("A", variant.getAlternate());

        // COSMIC insertion
        variantList = getVariantByAccession(parsedVariantList, "COSM5381470");
        assertEquals(1, variantList.size());
        variant = variantList.get(0);
        assertEquals("1", variant.getChromosome());
        assertEquals(Integer.valueOf(69568), variant.getStart());
        assertEquals(Integer.valueOf(69567), variant.getEnd());
        assertEquals("", variant.getReference());
        assertEquals("T", variant.getAlternate());

        // ClinVar record for an insertion with emtpy reference allele (some other insertions do provide reference nts)
        // It appears in the variant_summary.txt as 3       37090475        37090476        -       TT
        // No normalisation applies
        variantList = getVariantByAccession(parsedVariantList, "RCV000221270");
        assertEquals(1, variantList.size());
        variant = variantList.get(0);
        assertEquals("3", variant.getChromosome());
        assertEquals(Integer.valueOf(37090476), variant.getStart());
        assertEquals("", variant.getReference());
        assertEquals("TT", variant.getAlternate());

        // ClinVar record for an un-normalised variant. It appears in the variant_summary.txt as 17 53	53	C	CC
        // Genome sequence context for that position is TGTCCCTGCTGAA
        //                                                   ^
        //                                                   53
        // After normalisation should be 17 51 -   C
        variantList = getVariantByAccession(parsedVariantList, "RCV000488336");
        assertEquals(1, variantList.size());
        variant = variantList.get(0);
        assertEquals("17", variant.getChromosome());
        assertEquals(Integer.valueOf(51), variant.getStart());
        assertEquals("", variant.getReference());
        assertEquals("C", variant.getAlternate());

        // Arbitrary ClinVar record
        variantList = getVariantByAccession(parsedVariantList, "RCV000000829");
        assertEquals(1, variantList.size());
        variant = variantList.get(0);
        assertEquals("5", variant.getChromosome());
        assertEquals(Integer.valueOf(112136976), variant.getStart());
        assertEquals("AG", variant.getReference());
        assertEquals("", variant.getAlternate());

        variant = getVariantByVariant(parsedVariantList,
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

        variant = getVariantByVariant(parsedVariantList,
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

        variant = getVariantByVariant(parsedVariantList,
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

        // ClinVar record with three variants in an Haplotype
        variantList = getVariantByAccession(parsedVariantList, "RCV000000591");
        assertEquals(3, variantList.size());
        // First variant in the haplotype
        variant = variantList.get(0);
        assertEquals("18", variant.getChromosome());
        assertEquals(Integer.valueOf(55217985), variant.getStart());
        assertEquals("A", variant.getReference());
        assertEquals("C", variant.getAlternate());
        // Two evidenceEntry for the two variation records, another for the RCV
        // and one for SCV
        assertEquals(4, variant.getAnnotation().getTraitAssociation().size());
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

        // SCV
        evidenceEntry = getEvidenceEntryByAccession(variant, "SCV000020740");
        assertNotNull(evidenceEntry);
        assertEquals(1, evidenceEntry.getGenomicFeatures().size());
        assertEquals("FECH", evidenceEntry.getGenomicFeatures().get(0).getXrefs().get("symbol"));
        assertNotNull(evidenceEntry.getVariantClassification());
        assertEquals(ClinicalSignificance.pathogenic,
                evidenceEntry.getVariantClassification().getClinicalSignificance());

        // Second variant in the haplotype
        variant = variantList.get(1);
        assertEquals("18", variant.getChromosome());
        assertEquals(Integer.valueOf(55217991), variant.getStart());
        assertEquals("G", variant.getReference());
        assertEquals("A", variant.getAlternate());
        // Two evidenceEntry for the two variation records, another for the RCV
        assertEquals(4, variant.getAnnotation().getTraitAssociation().size());
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
        assertEquals(4, variant.getAnnotation().getTraitAssociation().size());
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
        assertEquals(4, variant.getAnnotation().getTraitAssociation().size());
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
        assertEquals("18:56390321:C:-", property.getValue());
        evidenceEntry = getEvidenceEntryByAccession(variant, "RCV000169692");
        assertNotNull(evidenceEntry);
        assertEquals(1, evidenceEntry.getGenomicFeatures().size());
        assertEquals("MALT1", evidenceEntry.getGenomicFeatures().get(0).getXrefs().get("symbol"));
        // Check it's properly flagged as part of the genotype set
        property = getProperty(evidenceEntry.getAdditionalProperties(), "GenotypeSet");
        assertNotNull(property.getValue());
        assertEquals("18:56390321:C:-", property.getValue());
        // Second variant in the genotype set
        variant = variantList.get(1);
        assertEquals("18", variant.getChromosome());
        assertEquals(Integer.valueOf(56390321), variant.getStart());
        assertEquals("C", variant.getReference());
        assertEquals("", variant.getAlternate());
        // Two evidenceEntry for the two variation records, another for the RCV
        assertEquals(4, variant.getAnnotation().getTraitAssociation().size());
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

    private void cleanUp() throws URISyntaxException, IOException {
        // Clean up temporary files/directories/indexes
        org.apache.commons.io.FileUtils.deleteDirectory(Paths.get("/tmp/clinicalVariant1/").toFile());
        org.apache.commons.io.FileUtils.deleteDirectory(Paths.get("/tmp/clinicalVariant2/").toFile());
        org.apache.commons.io.FileUtils.deleteDirectory(Paths.get("/tmp/clinicalVariant3/").toFile());
        org.apache.commons.io.FileUtils.deleteDirectory(Paths.get("/tmp/clinicalVariant4/").toFile());
        Paths.get("/tmp/clinical_variants.json.gz").toFile().delete();
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

    private Property getProperty(List<Property> propertyList, String propertyName) {
        for (Property property : propertyList) {
            if (propertyName.equals(property.getName())) {
                return property;
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
                        && (variant.getAnnotation().getTraitAssociation().get(i).getId() == null
                            || !variant.getAnnotation().getTraitAssociation().get(i).getId().equals(accession))) {
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