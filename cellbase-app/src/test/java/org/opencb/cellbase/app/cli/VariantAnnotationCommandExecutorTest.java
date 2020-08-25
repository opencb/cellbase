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

package org.opencb.cellbase.app.cli;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.util.JSON;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;
import org.opencb.biodata.models.variant.avro.VariantAvro;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.app.cli.admin.executors.LoadCommandExecutor;
import org.opencb.cellbase.app.cli.main.CellBaseCliOptionsParser;
import org.opencb.cellbase.app.cli.main.executors.VariantAnnotationCommandExecutor;
import org.opencb.cellbase.core.variant.AnnotationBasedPhasedQueryManager;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by fjlopez on 07/10/16.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VariantAnnotationCommandExecutorTest {

    private static final String OUTPUT_FILENAME = "/tmp/test.json.gz";
    private static final String GRCH37_DBNAME = "cellbase_hsapiens_grch37_v4";
    private Path resourcesFolder = Paths.get(getClass().getResource("/variant/annotation/").toURI());

    private ObjectMapper jsonObjectMapper;

    public VariantAnnotationCommandExecutorTest() throws URISyntaxException {

    }

    @BeforeAll
    public void setUp() throws Exception {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void proteinChangeMatchTest() throws IOException, URISyntaxException {
        // Remove database content
        cleanUp();
        // Load test data
        AdminCliOptionsParser.LoadCommandOptions loadCommandOptions = new AdminCliOptionsParser().getLoadCommandOptions();
        loadCommandOptions.commonOptions.conf = resourcesFolder.resolve("commandExecutor").toString();
        loadCommandOptions.data = "clinical_variants,gene";
        loadCommandOptions.database = GRCH37_DBNAME;
        loadCommandOptions.input = resourcesFolder.resolve("commandExecutor/proteinChangeMatch").toString();
        LoadCommandExecutor loadCommandExecutor = new LoadCommandExecutor(loadCommandOptions);
        loadCommandExecutor.loadCellBaseConfiguration();
        loadCommandExecutor.execute();
        // Set up annotation CLI options: NOTE checkAminoAcidChange is NOT enabled
        CellBaseCliOptionsParser.VariantAnnotationCommandOptions variantAnnotationCommandOptions
                = new CellBaseCliOptionsParser().getVariantAnnotationCommandOptions();
        variantAnnotationCommandOptions.assembly = "GRCh37";
        variantAnnotationCommandOptions.commonOptions.conf = resourcesFolder.resolve("commandExecutor").toString();
        variantAnnotationCommandOptions.input
                = resourcesFolder.resolve("commandExecutor/proteinChangeMatch/proband.duprem.atomic.left.split.vcf.gz").toString();
        variantAnnotationCommandOptions.output = OUTPUT_FILENAME;
        variantAnnotationCommandOptions.local = true;
        variantAnnotationCommandOptions.species = "hsapiens";
        // Annotate
        VariantAnnotationCommandExecutor variantAnnotationCommandExecutor
                = new VariantAnnotationCommandExecutor(variantAnnotationCommandOptions);
        variantAnnotationCommandExecutor.loadCellBaseConfiguration();
        variantAnnotationCommandExecutor.execute();
        // Load annotated variants
        List<Variant> variantList = loadResult();

        // Check results
        // Only one variant present in input VCF (2:170361068:G:C)
        assertEquals(1, variantList.size());
        // 2:170361068:G:C in the VCF file must NOT match 2:170361068:G:T variant in clinvar since checkAminoAcidChange
        // is disabled in this run
        Variant variant = getByVariant(variantList, new Variant("2:170361068:G:C"));
        // No trait association expected
        assertNotNull(variant);
        assertNotNull(variant.getAnnotation());
        assertNull(variant.getAnnotation().getTraitAssociation());

        // Enable checkAminoAcidChange
        variantAnnotationCommandOptions.checkAminoAcidChange = true;
        // Annotate
        variantAnnotationCommandExecutor
                = new VariantAnnotationCommandExecutor(variantAnnotationCommandOptions);
        variantAnnotationCommandExecutor.loadCellBaseConfiguration();
        variantAnnotationCommandExecutor.execute();
        // Load annotated variants
        variantList = loadResult();
        // Check results
        // Only one variant present in input VCF (2:170361068:G:C)
        assertEquals(1, variantList.size());
        // 2:170361068:G:C in the VCF file must match xxx variant in clinvar at the protein change level
        variant = getByVariant(variantList, new Variant("2:170361068:G:C"));
        // Only one COSMIC trait association expected
        assertNotNull(variant);
        assertNotNull(variant.getAnnotation());
        assertNotNull(variant.getAnnotation().getTraitAssociation());
        assertEquals(1, variant.getAnnotation().getTraitAssociation().size());
        assertEquals("COSM4624460", variant.getAnnotation().getTraitAssociation().get(0).getId());
    }

    @Test
    public void indexedVariantWithoutRequiredAttributeTest() throws IOException, URISyntaxException {
        cleanUp();
        // Custom VCF annotation file includes a malformed variant which should be skipped and the indexing process
        // continued
        VariantAnnotationCommandExecutor variantAnnotationCommandExecutor
                = new VariantAnnotationCommandExecutor(getVariantAnnotationCommandOptions(
                resourcesFolder.resolve("commandExecutor/customAnnotation/proband.duprem.atomic.left.split.vcf.gz").toString(),
                true,
                resourcesFolder.resolve("commandExecutor/customAnnotation/GEL_GL_6628.duprem.sites.annot.subset.atomic.left.split.test.vcf.gz").toString(),
                "GEL.GL.6628",
                "FAKEATTRIBUTE",
                null,
                null,
                -1
        ));
        variantAnnotationCommandExecutor.loadCellBaseConfiguration();
        variantAnnotationCommandExecutor.execute();
        List<Variant> variantList = loadResult();

        assertEquals(4, variantList.size());

        // Provided as a singleton in the input VCF. Is provided as a singleton in the index as well. However, the
        // indexed variant does not contain FAKEATTRIBUTE within its list of attributes. Should leave
        // additionalAttributes to null
        Variant variant = getByVariant(variantList, new Variant("chr1:10001:T:A"));
        assertNotNull(variant);
        assertNotNull(variant.getAnnotation());
        assertNull(variant.getAnnotation().getAdditionalAttributes());
    }

    @Test
    public void twoCustomFilesTest() throws IOException, URISyntaxException {
        cleanUp();
        String customFileString =
                resourcesFolder.resolve("commandExecutor/customAnnotation/twoCustomFiles/GEL_GL_6628.duprem.sites.annot.subset.atomic.left.split.test.vcf.gz").toString()
                + ","
                + resourcesFolder.resolve("commandExecutor/customAnnotation/twoCustomFiles/GEL_GL_another.duprem.sites.annot.subset.atomic.left.split.test.vcf.gz").toString();
        VariantAnnotationCommandExecutor variantAnnotationCommandExecutor
                = new VariantAnnotationCommandExecutor(getVariantAnnotationCommandOptions(
                resourcesFolder.resolve("commandExecutor/customAnnotation/proband.duprem.atomic.left.split.vcf.gz").toString(),
                true,
                customFileString,
                "GEL.GL.6628,GEL.GL.another",
                "GN,AF,AC,AN,MAF,HWE,AN_Cancer,AN_SRv3,AN_RD,AN_SRv4,AC_Cancer,AC_SRv3,AC_RD,AC_SRv4,AF_Cancer,AF_SRv3,AF_RD,AF_SRv4,MAF_Cancer,MAF_SRv3,MAF_RD,MAF_SRv4,HWE_Cancer,HWE_SRv3,HWE_RD,HWE_SRv4:GN,AF,AC,AN,MAF,HWE,AN_Cancer,AN_SRv3,AN_RD,AN_SRv4,AC_Cancer,AC_SRv3,AC_RD,AC_SRv4,AF_Cancer,AF_SRv3,AF_RD,AF_SRv4,MAF_Cancer,MAF_SRv3,MAF_RD,MAF_SRv4,HWE_Cancer,HWE_SRv3,HWE_RD,HWE_SRv4",
                null,
                null,
                100
        ));
        variantAnnotationCommandExecutor.loadCellBaseConfiguration();

        variantAnnotationCommandExecutor.execute();
        List<Variant> variantList = loadResult();

        assertEquals(4, variantList.size());

        // Provided as a singleton in the input VCF. Is provided as a singleton in the index as well. Should get
        // annotated with corresponding custom annotation
        Variant variant = getByVariant(variantList, new Variant("chr1:10001:T:A"));
        assertNotNull(variant);
        assertNotNull(variant.getAnnotation());
        assertNotNull(variant.getAnnotation().getAdditionalAttributes());
        assertEquals(2, variant.getAnnotation().getAdditionalAttributes().size());
        assertTrue(variant.getAnnotation().getAdditionalAttributes().containsKey("GEL.GL.6628"));
        assertNotNull(variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628"));
        assertNotNull(variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628").getAttribute());
        assertEquals(26,
                variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628").getAttribute().size());
        assertContainsCustomFrequencies(variant, "GEL.GL.6628", "0.0145889", "3770", "55");
        assertTrue(variant.getAnnotation().getAdditionalAttributes().containsKey("GEL.GL.another"));
        assertNotNull(variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.another"));
        assertNotNull(variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.another").getAttribute());
        assertEquals(26,
                variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628").getAttribute().size());
        assertContainsCustomFrequencies(variant, "GEL.GL.another", "0.0236074", "3770", "89");

    }

    @Test
    public void alreadyIndexedTest() throws IOException, URISyntaxException {
        cleanUp();
        // Custom VCF annotation file includes a malformed variant which should be skipped and the indexing process
        // continued
        VariantAnnotationCommandExecutor variantAnnotationCommandExecutor
                = new VariantAnnotationCommandExecutor(getVariantAnnotationCommandOptions(
                resourcesFolder.resolve("commandExecutor/customAnnotation/proband.duprem.atomic.left.split.vcf.gz").toString(),
                true,
                resourcesFolder.resolve("commandExecutor/customAnnotation/GEL_GL_6628.duprem.sites.annot.subset.atomic.left.split.test.vcf.gz").toString(),
                "GEL.GL.6628",
                "GN,AF,AC,AN,MAF,HWE,AN_Cancer,AN_SRv3,AN_RD,AN_SRv4,AC_Cancer,AC_SRv3,AC_RD,AC_SRv4,AF_Cancer,AF_SRv3,AF_RD,AF_SRv4,MAF_Cancer,MAF_SRv3,MAF_RD,MAF_SRv4,HWE_Cancer,HWE_SRv3,HWE_RD,HWE_SRv4",
                null,
                null,
                100
        ));
        variantAnnotationCommandExecutor.loadCellBaseConfiguration();

        // Running two times so that index is already there the second time
        variantAnnotationCommandExecutor.execute();
        // Delete output file to ensure the one created after the second execution is properly re-generated
        (new File(OUTPUT_FILENAME)).delete();
        variantAnnotationCommandExecutor.execute();
        List<Variant> variantList = loadResult();

        assertEquals(4, variantList.size());

        // Provided as a singleton in the input VCF. Is provided as a singleton in the index as well. Should get
        // annotated with corresponding custom annotation
        Variant variant = getByVariant(variantList, new Variant("chr1:10001:T:A"));
        assertNotNull(variant);
        assertNotNull(variant.getAnnotation());
        assertNotNull(variant.getAnnotation().getAdditionalAttributes());
        assertEquals(1, variant.getAnnotation().getAdditionalAttributes().size());
        assertTrue(variant.getAnnotation().getAdditionalAttributes().containsKey("GEL.GL.6628"));
        assertNotNull(variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628"));
        assertNotNull(variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628").getAttribute());
        assertEquals(26,
                variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628").getAttribute().size());
        assertContainsCustomFrequencies(variant, "GEL.GL.6628", "0.0145889", "3770", "55");

    }

    @Test
    public void maxFilesCustomAnnotationTest() throws IOException, URISyntaxException {
        cleanUp();
        // Custom VCF annotation file includes a malformed variant which should be skipped and the indexing process
        // continued
        VariantAnnotationCommandExecutor variantAnnotationCommandExecutor
                = new VariantAnnotationCommandExecutor(getVariantAnnotationCommandOptions(
                resourcesFolder.resolve("commandExecutor/customAnnotation/proband.duprem.atomic.left.split.vcf.gz").toString(),
                true,
                resourcesFolder.resolve("commandExecutor/customAnnotation/GEL_GL_6628.duprem.sites.annot.subset.atomic.left.split.test.vcf.gz").toString(),
                "GEL.GL.6628",
                "GN,AF,AC,AN,MAF,HWE,AN_Cancer,AN_SRv3,AN_RD,AN_SRv4,AC_Cancer,AC_SRv3,AC_RD,AC_SRv4,AF_Cancer,AF_SRv3,AF_RD,AF_SRv4,MAF_Cancer,MAF_SRv3,MAF_RD,MAF_SRv4,HWE_Cancer,HWE_SRv3,HWE_RD,HWE_SRv4",
                null,
                null,
                100
                ));
        variantAnnotationCommandExecutor.loadCellBaseConfiguration();
        variantAnnotationCommandExecutor.execute();
        List<Variant> variantList = loadResult();

        assertEquals(4, variantList.size());

        // Provided as a singleton in the input VCF. Is provided as a singleton in the index as well. Should get
        // annotated with corresponding custom annotation
        Variant variant = getByVariant(variantList, new Variant("chr1:10001:T:A"));
        assertNotNull(variant);
        assertNotNull(variant.getAnnotation());
        assertNotNull(variant.getAnnotation().getAdditionalAttributes());
        assertEquals(1, variant.getAnnotation().getAdditionalAttributes().size());
        assertTrue(variant.getAnnotation().getAdditionalAttributes().containsKey("GEL.GL.6628"));
        assertNotNull(variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628"));
        assertNotNull(variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628").getAttribute());
        assertEquals(26,
                variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628").getAttribute().size());
        assertContainsCustomFrequencies(variant, "GEL.GL.6628", "0.0145889", "3770", "55");


    }

    private void assertContainsCustomFrequencies(Variant variant, String fileId, String af, String an, String ac) {
        assertTrue(variant
                .getAnnotation()
                .getAdditionalAttributes()
                .get(fileId)
                .getAttribute()
                .containsKey("AF"));
        assertEquals(af, variant
                .getAnnotation()
                .getAdditionalAttributes()
                .get(fileId)
                .getAttribute()
                .get("AF"));
        assertTrue(variant
                .getAnnotation()
                .getAdditionalAttributes()
                .get(fileId)
                .getAttribute()
                .containsKey("AN"));
        assertEquals(an, variant
                .getAnnotation()
                .getAdditionalAttributes()
                .get(fileId)
                .getAttribute()
                .get("AN"));
        assertTrue(variant
                .getAnnotation()
                .getAdditionalAttributes()
                .get(fileId)
                .getAttribute()
                .containsKey("AC"));
        assertEquals(ac, variant
                .getAnnotation()
                .getAdditionalAttributes()
                .get(fileId)
                .getAttribute()
                .get("AC"));

    }

    @Test
    public void phasedCustomAnnotationTest() throws IOException, URISyntaxException {
        cleanUp();
        // Custom VCF annotation file includes a malformed variant which should be skipped and the indexing process
        // continued
        VariantAnnotationCommandExecutor variantAnnotationCommandExecutor
                = new VariantAnnotationCommandExecutor(getVariantAnnotationCommandOptions(
                resourcesFolder.resolve("commandExecutor/customAnnotation/proband.duprem.atomic.left.split.vcf.gz").toString(),
                true,
                resourcesFolder.resolve("commandExecutor/customAnnotation/GEL_GL_6628.duprem.sites.annot.subset.atomic.left.split.test.vcf.gz").toString(),
                "GEL.GL.6628",
                "GN,AF,AC,AN,MAF,HWE,AN_Cancer,AN_SRv3,AN_RD,AN_SRv4,AC_Cancer,AC_SRv3,AC_RD,AC_SRv4,AF_Cancer,AF_SRv3,AF_RD,AF_SRv4,MAF_Cancer,MAF_SRv3,MAF_RD,MAF_SRv4,HWE_Cancer,HWE_SRv3,HWE_RD,HWE_SRv4",
                null,
                null,
                -1
                ));
        variantAnnotationCommandExecutor.loadCellBaseConfiguration();
        variantAnnotationCommandExecutor.execute();
        List<Variant> variantList = loadResult();

        assertEquals(4, variantList.size());

        // Provided as a singleton in the input VCF. Forms part of an MNV in the index 1:10007:TTG:TAA. Since just
        // this variant of the two in the MNV is provided, it should NOT be annotated with custom annotation
        Variant variant = getByVariant(variantList, new Variant("chr1:10009:G:A"));
        assertNotNull(variant);
        assertNotNull(variant.getAnnotation());
        assertNull(variant.getAnnotation().getAdditionalAttributes());

        // Variant comes in phase with 1:10023:G:C. Both variants form an MNV which is also indexed. Must be annotated
        // with corresponding custom annotation
        variant = getByVariant(variantList, new Variant("chr1:10022:T:C"));
        assertNotNull(variant);
        assertNotNull(variant.getAnnotation());
        assertNotNull(variant.getAnnotation().getAdditionalAttributes());
        assertEquals(1, variant.getAnnotation().getAdditionalAttributes().size());
        assertTrue(variant.getAnnotation().getAdditionalAttributes().containsKey("GEL.GL.6628"));
        assertNotNull(variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628"));
        assertNotNull(variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628").getAttribute());
        assertEquals(26,
                variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628").getAttribute().size());
        assertContainsCustomFrequencies(variant, "GEL.GL.6628", "0.000116932", "8552", "1");

        // Variant comes in phase with 1:10022:T:C. Both variants form an MNV which is also indexed. Must be annotated
        // with corresponding custom annotation
        variant = getByVariant(variantList, new Variant("chr1:10023:G:C"));
        assertNotNull(variant);
        assertNotNull(variant.getAnnotation());
        assertNotNull(variant.getAnnotation().getAdditionalAttributes());
        assertEquals(1, variant.getAnnotation().getAdditionalAttributes().size());
        assertTrue(variant.getAnnotation().getAdditionalAttributes().containsKey("GEL.GL.6628"));
        assertNotNull(variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628"));
        assertNotNull(variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628").getAttribute());
        assertEquals(26,
                variant.getAnnotation().getAdditionalAttributes().get("GEL.GL.6628").getAttribute().size());
        assertContainsCustomFrequencies(variant, "GEL.GL.6628", "0.000116932", "8552", "1");


    }

    @Test
    public void additionalPopulationFrequencyUnphasedAnnotationTest() throws Exception {
        cleanUp();
        VariantAnnotationCommandExecutor variantAnnotationCommandExecutor
                = new VariantAnnotationCommandExecutor(getVariantAnnotationCommandOptions(
                        resourcesFolder.resolve("commandExecutor/additionalPopulationFrequency/Homo_sapiens.1.vcf.gz").toString(),
                false,
                null,
                null,
                null,
                resourcesFolder
                        .resolve("commandExecutor/additionalPopulationFrequency/chr1.2017-12-27_01_12.hgva.freq.cellbase.test.json.gz")
                        .toString(),
                true,
                -1
                ));
        variantAnnotationCommandExecutor.loadCellBaseConfiguration();
        variantAnnotationCommandExecutor.execute();
        List<Variant> variantList = loadResult();

        // One deletion, two MNVs (both from the input VCF file and the pop freqs file) AND one SNV (1:62165740:T:G)
        // from the json.gz population frequencies file. This last SNV is part of input MNV (1:62165739:AT:TG) but
        // since decomposition is off remains "unvisited" in the RocksDB by any input variant; since the
        // complete-input-population option is on, this SNV should be appended at the end of the output file as is in
        // the RocksDB
        assertEquals(4, variantList.size());

        // Single SNV (1:62165740:T:G) which is part of the larger MNV (1:62165739:AT:TG) but since decomposition is off
        // does get appended as an independent variant at the end of the file. Only one pop freq object belongs to this
        // SNV alone
        Variant variant = getByVariant(variantList, new Variant("1:62165740:T:G"));
        assertNotNull(variant);
        String phaseSet = AnnotationBasedPhasedQueryManager.getSampleAttribute(variant, "PS");
        assertNull(phaseSet);
        assertNotNull(variant.getAnnotation());
        assertNotNull(variant.getAnnotation().getPopulationFrequencies());
        assertEquals(1, variant.getAnnotation().getPopulationFrequencies().size());
        List<PopulationFrequency> populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("GNOMAD_GENOMES",
                        "AMR",
                        "T",
                        "G",
                        (float) 0.98062956,
                        (float) 0.01937046,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());


        // Deletion in a multiallelic position. This deletion does not have pop freqs - should be left to null
        variant = getByVariant(variantList, new Variant("1:62165739:AT:-"));
        assertNotNull(variant);
        phaseSet = AnnotationBasedPhasedQueryManager.getSampleAttribute(variant, "PS");
        assertNull(phaseSet);
        assertNotNull(variant.getAnnotation());
        assertNull(variant.getAnnotation().getPopulationFrequencies());

        // MNV AT:TG. The MNV as such ist also in the database: does have pop
        // freqs and three PopulationFrequency objects should be returned.
        variant = getByVariant(variantList, new Variant("1:62165739:AT:TG"));
        assertNotNull(variant);
        phaseSet = AnnotationBasedPhasedQueryManager.getSampleAttribute(variant, "PS");
        assertNull(phaseSet);
        assertNotNull(variant.getAnnotation());
        assertNotNull(variant.getAnnotation().getPopulationFrequencies());
        assertEquals(3, variant.getAnnotation().getPopulationFrequencies().size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AFR",
                        "AT",
                        "TG",
                        (float) 0.9849,
                        (float) 0.0151,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AMR",
                        "AT",
                        "TG",
                        (float) 0.9957,
                        (float) 0.0043,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "EUR",
                        "AT",
                        "TG",
                        (float) 0.999,
                        (float) 0.001,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());


    }

    @Test
    public void additionalPopulationFrequencyPhasedAnnotationTest() throws Exception {
        cleanUp();
        VariantAnnotationCommandExecutor variantAnnotationCommandExecutor
                = new VariantAnnotationCommandExecutor(getVariantAnnotationCommandOptions(
                        resourcesFolder.resolve("commandExecutor/additionalPopulationFrequency/Homo_sapiens.1.vcf.gz").toString(),
                true,
                null,
                null,
                null,
                resourcesFolder
                        .resolve("commandExecutor/additionalPopulationFrequency/chr1.2017-12-27_01_12.hgva.freq.cellbase.test.json.gz")
                        .toString(),
                true,
                -1));
        variantAnnotationCommandExecutor.loadCellBaseConfiguration();
        variantAnnotationCommandExecutor.execute();
        List<Variant> variantList = loadResult();

        // One deletion which is not part of any MNV, two SNVs obtained as the result of decomposing an MNV and another
        // two from a second MNV.
        assertEquals(5, variantList.size());

        // Deletion forms part of MNV AATT:TTGG. The deletion and posterior insertion in this MNV form another MNV in
        // the database: does have pop freqs and three PopulationFrequency objects should be returned.
        Variant variant = getByVariant(variantList, new Variant("2:1:AA:-"));
        assertNotNull(variant);
        String phaseSet = AnnotationBasedPhasedQueryManager.getSampleAttribute(variant, "PS");
        assertEquals("2:1:AA:-,2:5:-:GG", phaseSet);
        assertNotNull(variant.getAnnotation());
        assertNotNull(variant.getAnnotation().getPopulationFrequencies());
        assertEquals(3, variant.getAnnotation().getPopulationFrequencies().size());
        List<PopulationFrequency> populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AFR",
                        "AAnn--",
                        "--nngg",
                        (float) 0.8,
                        (float) 0.1,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AMR",
                        "AAnn--",
                        "--nngg",
                        (float) 0.8,
                        (float) 0.1,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "EUR",
                        "AAnn--",
                        "--nngg",
                        (float) 0.8,
                        (float) 0.1,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());

        // Insertion forms part of MNV AATT:TTGG. The deletion and posterior insertion in this MNV form another MNV in
        // the database: does have pop freqs and three PopulationFrequency objects should be returned.
        variant = getByVariant(variantList, new Variant("2:5:-:GG"));
        assertNotNull(variant);
        phaseSet = AnnotationBasedPhasedQueryManager.getSampleAttribute(variant, "PS");
        assertEquals("2:1:AA:-,2:5:-:GG", phaseSet);
        assertNotNull(variant.getAnnotation());
        assertNotNull(variant.getAnnotation().getPopulationFrequencies());
        assertEquals(3, variant.getAnnotation().getPopulationFrequencies().size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AFR",
                        "aann--",
                        "--nnGG",
                        (float) 0.8,
                        (float) 0.1,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AMR",
                        "aann--",
                        "--nnGG",
                        (float) 0.8,
                        (float) 0.1,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "EUR",
                        "aann--",
                        "--nnGG",
                        (float) 0.8,
                        (float) 0.1,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());

        // Deletion in a multiallelic position. This deletion does not have pop freqs - should be left to null
        variant = getByVariant(variantList, new Variant("1:62165739:AT:-"));
        assertNotNull(variant);
        phaseSet = AnnotationBasedPhasedQueryManager.getSampleAttribute(variant, "PS");
        assertNull(phaseSet);
        assertNotNull(variant.getAnnotation());
        assertNull(variant.getAnnotation().getPopulationFrequencies());

        // SNV forms part of MNV AT:TG. The two SNVs in this MNV form another MNV in the database: does have pop
        // freqs and three PopulationFrequency objects should be returned.
        variant = getByVariant(variantList, new Variant("1:62165739:A:T"));
        assertNotNull(variant);
        phaseSet = AnnotationBasedPhasedQueryManager.getSampleAttribute(variant, "PS");
        assertEquals("1:62165739:A:T,1:62165740:T:G", phaseSet);
        assertNotNull(variant.getAnnotation());
        assertNotNull(variant.getAnnotation().getPopulationFrequencies());
        assertEquals(3, variant.getAnnotation().getPopulationFrequencies().size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AFR",
                        "At",
                        "Tg",
                        (float) 0.9849,
                        (float) 0.0151,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AMR",
                        "At",
                        "Tg",
                        (float) 0.9957,
                        (float) 0.0043,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "EUR",
                        "At",
                        "Tg",
                        (float) 0.999,
                        (float) 0.001,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());

        // SNV forms part of MNV AT:TG. The two SNVs in this MNV form another MNV in the database: does have pop
        // freqs and three PopulationFrequency objects should be returned. Also, this SNV as such also appears in the
        // json.gz population frequencies file (i.e. without being part of the MNV) and includes a GNOMAD
        // PopulationFrequency value; this object must also be appended to the population frequencies list
        variant = getByVariant(variantList, new Variant("1:62165740:T:G"));
        assertNotNull(variant);
        phaseSet = AnnotationBasedPhasedQueryManager.getSampleAttribute(variant, "PS");
        assertEquals("1:62165739:A:T,1:62165740:T:G", phaseSet);
        assertNotNull(variant.getAnnotation());
        assertNotNull(variant.getAnnotation().getPopulationFrequencies());
        assertEquals(4, variant.getAnnotation().getPopulationFrequencies().size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("GNOMAD_GENOMES",
                        "AMR",
                        "T",
                        "G",
                        (float) 0.98062956,
                        (float) 0.01937046,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AFR",
                        "aT",
                        "tG",
                        (float) 0.9849,
                        (float) 0.0151,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AMR",
                        "aT",
                        "tG",
                        (float) 0.9957,
                        (float) 0.0043,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variant.getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "EUR",
                        "aT",
                        "tG",
                        (float) 0.999,
                        (float) 0.001,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());

    }

    private List<PopulationFrequency> getPopulationFrequency(List<PopulationFrequency> populationFrequencyList,
                                                             PopulationFrequency populationFrequency) {
        List<PopulationFrequency> populationFrequencyList1 = new ArrayList<>(1);
        for (PopulationFrequency populationFrequency1 : populationFrequencyList) {
            if (populationFrequency.getStudy().equals(populationFrequency1.getStudy())
                    && populationFrequency.getPopulation().equals(populationFrequency1.getPopulation())
                    && populationFrequency.getRefAllele().equals(populationFrequency1.getRefAllele())
                    && populationFrequency.getAltAllele().equals(populationFrequency1.getAltAllele())
                    && populationFrequency.getAltAlleleFreq().equals(populationFrequency1.getAltAlleleFreq())
                    && populationFrequency.getRefAlleleFreq().equals(populationFrequency1.getRefAlleleFreq())) {
                populationFrequencyList1.add(populationFrequency);
            }
        }

        return populationFrequencyList1;
    }

    private Variant getByVariant(List<Variant> variantList, Variant variant) {
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

    private List<Variant> loadResult() {
        List<Variant> variantList = new ArrayList<>(3);

        try {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(Paths.get(OUTPUT_FILENAME));
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

    private void cleanUp() throws IOException {
        (new File(OUTPUT_FILENAME)).delete();
        org.apache.commons.io.FileUtils.deleteDirectory(resourcesFolder
                .resolve("commandExecutor/additionalPopulationFrequency/chr1.2017-12-27_01_12.hgva.freq.cellbase.test.json.gz.idx").toFile());
        org.apache.commons.io.FileUtils.deleteDirectory(resourcesFolder
                .resolve("commandExecutor/customAnnotation/GEL_GL_6628.duprem.sites.annot.subset.atomic.left.split.test.vcf.gz.idx").toFile());

        try (MongoDataStoreManager mongoManager
                     = new MongoDataStoreManager(Collections.singletonList(new DataStoreServerAddress("localhost",
                27017)))) {
            MongoDBConfiguration.Builder builder = MongoDBConfiguration.builder();
            MongoDBConfiguration  mongoDBConfiguration = builder.build();
            mongoManager.get(GRCH37_DBNAME, mongoDBConfiguration);
            mongoManager.drop(GRCH37_DBNAME);
        }

    }

    private CellBaseCliOptionsParser.VariantAnnotationCommandOptions
    getVariantAnnotationCommandOptions(String inputFilename,
                                       boolean decompose,
                                       String customfiles,
                                       String customfileIds,
                                       String customFileFields,
                                       String populationFrequencyFilename,
                                       Boolean completeInputPopulation,
                                       int maxOpenFiles) {

        CellBaseCliOptionsParser.VariantAnnotationCommandOptions variantAnnotationCommandOptions
                = new CellBaseCliOptionsParser().getVariantAnnotationCommandOptions();

        variantAnnotationCommandOptions.referenceFasta = resourcesFolder
                .resolve("Homo_sapiens.GRCh37.75.dna.primary_assembly.chr17.fa.gz").toString();
        variantAnnotationCommandOptions.benchmark = false;
        variantAnnotationCommandOptions.phased = true;
        variantAnnotationCommandOptions.input = inputFilename;
        variantAnnotationCommandOptions.skipNormalize = false;
        variantAnnotationCommandOptions.skipDecompose = !decompose;
        variantAnnotationCommandOptions.skipLeftAlign = false;
        variantAnnotationCommandOptions.output = Paths.get(OUTPUT_FILENAME).toString();
        variantAnnotationCommandOptions.outputFormat = "json";
        variantAnnotationCommandOptions.include = "cytobands";
        variantAnnotationCommandOptions.exclude = null;
        variantAnnotationCommandOptions.numThreads = 1;
        variantAnnotationCommandOptions.batchSize = 3;
        variantAnnotationCommandOptions.local = true;
        variantAnnotationCommandOptions.species = "hsapiens";
        variantAnnotationCommandOptions.assembly = "GRCh38";
        variantAnnotationCommandOptions.customFiles = customfiles;
        variantAnnotationCommandOptions.customFileIds = customfileIds;
        variantAnnotationCommandOptions.customFileFields = customFileFields;
        variantAnnotationCommandOptions.maxOpenFiles = maxOpenFiles;
        variantAnnotationCommandOptions.noImprecision = true;
        variantAnnotationCommandOptions.buildParams = (new HashMap<>(1));

        if (populationFrequencyFilename != null) {
            variantAnnotationCommandOptions.buildParams.put("population-frequencies", populationFrequencyFilename);
        }

        if (completeInputPopulation != null) {
            variantAnnotationCommandOptions.buildParams.put("complete-input-population",
                    completeInputPopulation.toString());
        }

        variantAnnotationCommandOptions.cellBaseAnnotation = false;
        variantAnnotationCommandOptions.commonOptions.conf
                = resourcesFolder.resolve("commandExecutor/configuration.json").toString();

        return variantAnnotationCommandOptions;
    }

}