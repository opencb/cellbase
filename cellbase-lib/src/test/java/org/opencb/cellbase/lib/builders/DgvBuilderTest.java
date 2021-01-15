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
import org.opencb.biodata.models.variant.avro.VariantAvro;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;



/**
 * Created by fjlopez on 15/05/17.
 */
public class DgvBuilderTest {

    public void parse() throws Exception {
        Path dgvFile = Paths.get(getClass().getResource("/dgv.txt.gz").getFile());
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "dgv.test");
        (new DgvBuilder(dgvFile, serializer)).parse();
        serializer.close();
        assertEquals(loadDGVSet(Paths.get(getClass().getResource("/dgv.json.gz").getFile())),
                loadDGVSet(Paths.get("/tmp/dgv.test.json.gz")));
    }

    private Set<VariantAvro> loadDGVSet(Path path) throws IOException {
        Set<VariantAvro> variantAvroSet = new HashSet<>(5);
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(path)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                variantAvroSet.add(jsonObjectMapper.convertValue(JSON.parse(line), VariantAvro.class));
                line = bufferedReader.readLine();
            }
        }

        return variantAvroSet;
    }



}