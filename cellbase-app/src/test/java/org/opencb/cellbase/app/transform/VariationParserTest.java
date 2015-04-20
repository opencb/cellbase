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
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VariationParserTest {

    @Test
    public void testParse() throws Exception {
        URL resource = VariantEffectParserTest.class.getResource("/variationParser");
        Path inputDir = Paths.get(resource.toURI());
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(inputDir);
        VariationParser variationParser = new VariationParser(inputDir, serializer);
        variationParser.parse();
        serializer.close();
    }
}