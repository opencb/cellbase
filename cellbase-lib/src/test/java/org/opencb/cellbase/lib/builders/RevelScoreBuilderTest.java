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

package org.opencb.cellbase.lib.builders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mortbay.util.ajax.JSON;
import org.opencb.biodata.models.core.MissenseVariantFunctionalScore;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RevelScoreBuilderTest {

    @Test
    public void testParse() throws Exception {
        CellBaseSerializer cellBaseSerializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"),
                "missense_variation_functional_score");

        Path inputPath = Paths.get(RevelScoreBuilderTest.class.getResource("/revel").toURI());
        RevelScoreBuilder builder = new RevelScoreBuilder(inputPath, cellBaseSerializer);
        builder.parse();

        cellBaseSerializer.close();

        List<MissenseVariantFunctionalScore> actual = loadScores(Paths.get("/tmp/missense_variation_functional_score.json.gz"));
        List<MissenseVariantFunctionalScore> expected = loadScores(Paths.get(RevelScoreBuilderTest.class.getResource(
                "/revel/missense_variation_functional_score.json.gz").getFile()));

        assertEquals(expected, actual);
    }

    private List<MissenseVariantFunctionalScore> loadScores(Path path) throws IOException {
        List<MissenseVariantFunctionalScore> scores = new ArrayList<>(10);
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(path)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                scores.add(jsonObjectMapper.convertValue(JSON.parse(line), MissenseVariantFunctionalScore.class));
                line = bufferedReader.readLine();
            }
        }

        return scores;
    }
}