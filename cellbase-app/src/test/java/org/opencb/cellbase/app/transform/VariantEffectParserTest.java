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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class VariantEffectParserTest {
    
    private static Path file;
//    private static JsonSerializer<VariantEffect> serializer;
    private static CellBaseFileSerializer serializer;

    @BeforeClass
    public static void setUpClass() throws URISyntaxException, IOException {
        URL resource = VariantEffectParserTest.class.getResource("/vep-example-output.txt");
        file = Paths.get(resource.toURI());

        serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp"));
//        serializer = new JsonSerializer<>(Paths.get("/tmp/vep-example-output"));
//        serializer.open();
//        serializer.pre();
    }
    
    @AfterClass
    public static void tearDownClass() {
//        serializer.post();
        try {
            serializer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        VariantEffectParser instance = new VariantEffectParser(file, serializer);
        instance.parse();
//        Assert.assertEquals(3, numEffectsWritten);
    }
    
}
