/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.app.transform;

import org.junit.Test;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by imedina on 06/11/15.
 */
public class CaddScoreParserTest {

    @Test
    public void testParse() throws Exception {
        CellBaseSerializer cellBaseSerializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "cadd");

        Path inputPath = Paths.get(getClass().getResource("/cadd_example.txt.gz").toURI());
        CaddScoreParser caddScoreParser = new CaddScoreParser(inputPath, cellBaseSerializer);
        caddScoreParser.parse();

        long l1 = 3763369539475633456L;
        long l2 = 2440691865951630640L;
        float DECIMAL_RESOLUTION = 10000f;


        float a = ((short) (l1 >> 48) - 10000) / DECIMAL_RESOLUTION;
        float c = ((short) (l1 >> 32) - 10000) / DECIMAL_RESOLUTION;
        float g = ((short) (l1 >> 16) - 10000) / DECIMAL_RESOLUTION;
        float t = ((short) (l1 >> 0) - 10000) / DECIMAL_RESOLUTION;

        assertEquals("Error getting A score value from CADD", 0.337f, a, DECIMAL_RESOLUTION);
        assertEquals("Error getting C score value from CADD", 0.1432f, c, DECIMAL_RESOLUTION);
        assertEquals("Error getting G score value from CADD", 0.2024f, g, DECIMAL_RESOLUTION);
        assertEquals("Error getting T score value from CADD", 2f, t, DECIMAL_RESOLUTION);


        a = ((short) (l2 >> 48) - 10000) / DECIMAL_RESOLUTION;
        c = ((short) (l2 >> 32) - 10000) / DECIMAL_RESOLUTION;
        g = ((short) (l2 >> 16) - 10000) / DECIMAL_RESOLUTION;
        t = ((short) (l2 >> 0) - 10000) / DECIMAL_RESOLUTION;

        assertEquals("Error getting A score value from CADD", -0.1328f, a, DECIMAL_RESOLUTION);
        assertEquals("Error getting C score value from CADD", -0.4797f, c, DECIMAL_RESOLUTION);
        assertEquals("Error getting G score value from CADD", -0.2772f, g, DECIMAL_RESOLUTION);
        assertEquals("Error getting T score value from CADD", 2f, t, DECIMAL_RESOLUTION);
    }
}