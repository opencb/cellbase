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

import org.junit.jupiter.api.Test;
import org.eclipse.jetty.util.ajax.JSON;
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Created by fjlopez on 10/05/17.
 */
public class RepeatsBuilderTest extends GenericBuilderTest<Repeat> {

    public RepeatsBuilderTest() {

        super(Repeat.class);

    }

    @Test
    public void testParse() throws Exception {
        CellBaseConfiguration configuration = CellBaseConfiguration.load(getClass().getClassLoader().getResourceAsStream("configuration.test.yaml"));
        Path repeatsFilesDir = Paths.get(getClass().getResource("/repeats").getPath());
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "repeats.test");
        (new RepeatsBuilder(repeatsFilesDir, serializer, configuration)).parse();
        serializer.close();
        Set<Repeat> expected = loadRepeatSet(Paths.get(getClass().getClassLoader().getResource("repeats/repeats.test.json.gz").getPath()));
        Set<Repeat> current = loadRepeatSet(Paths.get("/tmp/repeats.test.json.gz"));
        assertEquals(expected.size(), current.size());
        for (Repeat repeat : expected) {
            assertTrue(current.contains(repeat));
        }
        for (Repeat repeat : current) {
            assertTrue(expected.contains(repeat));
        }
    }

    private Set<Repeat> loadRepeatSet(Path path) throws IOException {
        Set<Repeat> repeatSet = new HashSet<>(16);

        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(path)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                repeatSet.add(jsonObjectMapper.convertValue(JSON.parse(line), Repeat.class));
                line = bufferedReader.readLine();
            }
        }

        return repeatSet;
    }


}