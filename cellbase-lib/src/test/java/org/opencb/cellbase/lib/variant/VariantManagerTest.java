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
import org.junit.jupiter.api.TestInstance;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.VariantQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.DataReleaseManager;
import org.opencb.cellbase.lib.managers.VariantManager;
import org.opencb.cellbase.lib.variant.annotation.VariantAnnotationCalculator;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VariantManagerTest extends GenericMongoDBAdaptorTest {
    private ObjectMapper jsonObjectMapper;
    private VariantAnnotationCalculator variantAnnotationCalculator;
    private VariantManager variantManager;
    private DataReleaseManager dataReleaseManager;

    public VariantManagerTest() throws IOException {
        super();
    }

    @BeforeAll
    public void setUp() throws Exception {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        clearDB(CELLBASE_DBNAME);
        initDB();

        variantAnnotationCalculator = new VariantAnnotationCalculator(SPECIES, ASSEMBLY, dataRelease, token, cellBaseManagerFactory);
        variantManager = cellBaseManagerFactory.getVariantManager(SPECIES, ASSEMBLY);
    }

    @Test
    public void testNormalisation() throws Exception {
        CellBaseDataResult<Variant> results = variantManager.getNormalizationByVariant("22:18512237:-:AGTT", dataRelease, token);
        assertEquals(1, results.getResults().size());
    }

    @Test
    public void testHgvs() throws Exception {
        List<CellBaseDataResult<String>> results = variantManager.getHgvsByVariant("22:38318124:-:CTTTTG", dataRelease, token);
        assertEquals(5, results.get(0).getResults().size());
    }
}
