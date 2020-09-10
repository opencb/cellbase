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
import org.opencb.biodata.formats.feature.gff.Gff2;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class RegulatoryFeatureBuilderTest {
    @Test
    public void testParse() throws Exception {
        Path regulationDirectoryPath = Paths.get(getClass().getResource("/regulation").toURI());
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "regulatory_feature", true);
        RegulatoryFeatureBuilder parser = new RegulatoryFeatureBuilder(regulationDirectoryPath, serializer);
        parser.parse();
        Set<Gff2> features = parser.regulatoryFeatureSet;
        assertEquals(1, features.size());

        Gff2 feature = features.iterator().next();

        assertEquals(103826, feature.getStart());
        assertEquals(104072, feature.getEnd());
        assertEquals("ID=open_chromatin_region:ENSR00000898744;bound_end=104072;bound_start=103826;description=Open chromatin region;feature_type=Open chromatin", feature.getAttribute());
        assertEquals("GL000008.2", feature.getSequenceName());
        assertEquals("open_chromatin_region", feature.getFeature());
    }
}
