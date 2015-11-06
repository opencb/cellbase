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
        CellBaseSerializer cellBaseSerializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "cadd.json");

        Path inputPath = Paths.get(getClass().getResource("/cadd_example.txt.gz").toURI());
        CaddScoreParser caddScoreParser = new CaddScoreParser(inputPath, cellBaseSerializer);
        caddScoreParser.parse();


    }
}