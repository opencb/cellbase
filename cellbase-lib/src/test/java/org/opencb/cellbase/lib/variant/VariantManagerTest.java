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

package org.opencb.cellbase.lib.variant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;
import org.junit.jupiter.api.TestInstance;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.VariantManager;
import org.opencb.cellbase.lib.variant.annotation.VariantAnnotationCalculator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VariantManagerTest extends GenericMongoDBAdaptorTest {
    private ObjectMapper jsonObjectMapper;
    private VariantAnnotationCalculator variantAnnotationCalculator;
    private VariantManager variantManager;

    public VariantManagerTest() throws IOException {
        super();
    }

    @BeforeAll
    public void setUp() throws Exception {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/variant-annotation/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/genome_sequence.test.json.gz").toURI());
        loadRunner.load(path, "genome_sequence");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/regulatory_region.test.json.gz").toURI());
        loadRunner.load(path, "regulatory_region");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/protein.test.json.gz").toURI());
        loadRunner.load(path, "protein");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/prot_func_pred_chr_13.test.json.gz").toURI());
        loadRunner.load(path, "protein_functional_prediction");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/prot_func_pred_chr_18.test.json.gz").toURI());
        loadRunner.load(path, "protein_functional_prediction");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/prot_func_pred_chr_19.test.json.gz").toURI());
        loadRunner.load(path, "protein_functional_prediction");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/prot_func_pred_chr_MT.test.json.gz").toURI());
        loadRunner.load(path, "protein_functional_prediction");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/variation_chr1.full.test.json.gz").toURI());
        loadRunner.load(path, "variation");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/variation_chr2.full.test.json.gz").toURI());
        loadRunner.load(path, "variation");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/variation_chr19.full.test.json.gz").toURI());
        loadRunner.load(path, "variation");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/variation_chrMT.full.test.json.gz").toURI());
        loadRunner.load(path, "variation");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/structuralVariants.json.gz").toURI());
        loadRunner.load(path, "variation");
        path = Paths.get(getClass()
                .getResource("/genome/genome_info.json").toURI());
        loadRunner.load(path, "genome_info");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/repeats.json.gz").toURI());
        loadRunner.load(path, "repeats");
        path = Paths.get(getClass()
                .getResource("/variant-annotation/clinical_variants.test.json.gz").toURI());
        loadRunner.load(path, "clinical_variants");
        variantAnnotationCalculator = new VariantAnnotationCalculator("hsapiens", "GRCh37",
                cellBaseManagerFactory);

        path = Paths.get(getClass()
                .getResource("/hgvs/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene");
        path = Paths.get(getClass()
                .getResource("/hgvs/genome_sequence.test.json.gz").toURI());
        loadRunner.load(path, "genome_sequence");

        variantManager = cellBaseManagerFactory.getVariantManager("hsapiens", "GRCh37");

    }

    @Test
    public void testNormalisation() throws Exception {
        CellBaseDataResult<Variant> results = variantManager.getNormalizationByVariant("22:18512237:-:AGTT");
        assertEquals(1, results.getResults().size());
    }

    @Test
    public void testHgvs() throws Exception {
        List<CellBaseDataResult<String>> results = variantManager.getHgvsByVariant("22:38318124:-:CTTTTG");
        assertEquals(4, results.get(0).getResults().size());
    }
}
