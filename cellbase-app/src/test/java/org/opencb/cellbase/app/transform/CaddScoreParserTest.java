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

import org.junit.Ignore;
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

    // TODO: to finish - properly reimplement
    @Ignore
    @Test
    public void testParse() throws Exception {
        CellBaseSerializer cellBaseSerializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "cadd");

        Path inputPath = Paths.get(getClass().getResource("/cadd_example.txt.gz").toURI());
        CaddScoreParser caddScoreParser = new CaddScoreParser(inputPath, cellBaseSerializer);
        caddScoreParser.parse();

        // Next values are taken from the generated file at /tmp
        // They correspond to first and last scores in the file
        // Both raw and scaled scores ar tested
        long l1 = 2909650398779952672L;
//        long l1 = 246853554624987437L;
        long l2 = 2777354483929861664L;
        long l3 = 1701815202902769664L;
        long l4 = 421650576018505728L;

        float DECIMAL_RESOLUTION = 100f;


        // raw CADD scores tests
        float a = (((short) (l1 >> 48)) / DECIMAL_RESOLUTION) - 10;
        float c = (((short) (l1 >> 32)) / DECIMAL_RESOLUTION) - 10;
        float g = (((short) (l1 >> 16)) / DECIMAL_RESOLUTION) - 10;
        float t = (((short) (l1 >> 0) ) / DECIMAL_RESOLUTION) - 10;

//        assertEquals("Error getting A score value from raw CADD", 0.337f, a, 1 / DECIMAL_RESOLUTION);
//        assertEquals("Error getting C score value from raw CADD", 0.143f, c, 1 / DECIMAL_RESOLUTION);
//        assertEquals("Error getting G score value from raw CADD", 0.202f, g, 1 / DECIMAL_RESOLUTION);
//        assertEquals("Error getting T score value from raw CADD", 10f, t, 1 / DECIMAL_RESOLUTION);


        a = ((short) (l2 >> 48) - 10000) / DECIMAL_RESOLUTION;
        c = ((short) (l2 >> 32) - 10000) / DECIMAL_RESOLUTION;
        g = ((short) (l2 >> 16) - 10000) / DECIMAL_RESOLUTION;
        t = ((short) (l2 >> 0) - 10000) / DECIMAL_RESOLUTION;

        assertEquals("Error getting A score value from raw CADD", -0.133f, a, 1 / DECIMAL_RESOLUTION);
        assertEquals("Error getting C score value from raw CADD", -0.4797f, c, 1 / DECIMAL_RESOLUTION);
        assertEquals("Error getting G score value from raw CADD", -0.2772f, g, 1 / DECIMAL_RESOLUTION);
        assertEquals("Error getting T score value from raw CADD", 10f, t, 1 / DECIMAL_RESOLUTION);


        // scaled CADD scores tests
        a = ((short) (l3 >> 48)) / DECIMAL_RESOLUTION;
        c = ((short) (l3 >> 32)) / DECIMAL_RESOLUTION;
        g = ((short) (l3 >> 16)) / DECIMAL_RESOLUTION;
        t = ((short) (l3 >> 0)) / DECIMAL_RESOLUTION;

        assertEquals("Error getting A score value from scaled CADD", 6.046f, a, 1 / DECIMAL_RESOLUTION);
        assertEquals("Error getting C score value from scaled CADD", 4.073f, c, 1 / DECIMAL_RESOLUTION);
        assertEquals("Error getting G score value from scaled CADD", 4.705f, g, 1 / DECIMAL_RESOLUTION);
        assertEquals("Error getting T score value from scaled CADD", 0f, t, 1 / DECIMAL_RESOLUTION);

        a = ((short) (l4 >> 48)) / DECIMAL_RESOLUTION;
        c = ((short) (l4 >> 32)) / DECIMAL_RESOLUTION;
        g = ((short) (l4 >> 16)) / DECIMAL_RESOLUTION;
        t = ((short) (l4 >> 0)) / DECIMAL_RESOLUTION;

        assertEquals("Error getting A score value from scaled CADD", 1.498f, a, 1 / DECIMAL_RESOLUTION);
        assertEquals("Error getting C score value from scaled CADD", 0.247f, c, 1 / DECIMAL_RESOLUTION);
        assertEquals("Error getting G score value from scaled CADD", 0.748f, g, 1 / DECIMAL_RESOLUTION);
        assertEquals("Error getting T score value from scaled CADD", 0f, t, 1 / DECIMAL_RESOLUTION);
    }
}