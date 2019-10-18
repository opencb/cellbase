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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;



/**
 * Created by fjlopez on 13/07/17.
 */
public class RegulatoryRegionParserTest extends GenericParserTest<RegulatoryFeature> {

    public RegulatoryRegionParserTest() {
        super(RegulatoryFeature.class);
    }
    @Test
    public void parser() throws IOException, URISyntaxException, NoSuchMethodException, SQLException, ClassNotFoundException {
        Path regulationFolder = Paths.get(getClass().getResource("/regulation").toURI());

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "regulatory_region", true);
        (new RegulatoryRegionParser(regulationFolder, serializer)).parse();
        serializer.close();

        List<RegulatoryFeature> regulatoryFeatureList = loadSerializedObjects("/tmp/regulatory_region.json.gz");
        assertEquals(3, regulatoryFeatureList.size());

        RegulatoryFeature regulatoryFeature = getRegulatoryFeature(regulatoryFeatureList, null);
        assertEquals("14", regulatoryFeature.getChromosome());
        assertEquals("TF_binding_site", regulatoryFeature.getFeatureType());
        assertEquals(23034888, regulatoryFeature.getStart());
        assertEquals(23034896, regulatoryFeature.getEnd());
        assertEquals("7.391", regulatoryFeature.getScore());
        assertEquals("-", regulatoryFeature.getStrand());
        assertEquals("THAP1", regulatoryFeature.getName());
        assertEquals("MA0597.1", regulatoryFeature.getMatrix());

        regulatoryFeature = getRegulatoryFeature(regulatoryFeatureList, "ENSR00000105157");
        assertEquals("18", regulatoryFeature.getChromosome());
        assertEquals("Regulatory_Build", regulatoryFeature.getSource());
        assertEquals("Open_chromatin", regulatoryFeature.getFeatureType());
        assertEquals(76429380, regulatoryFeature.getStart());
        assertEquals(76430144, regulatoryFeature.getEnd());

        regulatoryFeature = getRegulatoryFeature(regulatoryFeatureList, "ENSR00000225390");
        assertEquals("8", regulatoryFeature.getChromosome());
        assertEquals("Regulatory_Build", regulatoryFeature.getSource());
        assertEquals("Open_chromatin", regulatoryFeature.getFeatureType());
        assertEquals(66405962, regulatoryFeature.getStart());
        assertEquals(66406502, regulatoryFeature.getEnd());


    }

    private RegulatoryFeature getRegulatoryFeature(List<RegulatoryFeature> regulatoryFeatureList, String id) {
        for (RegulatoryFeature regulatoryFeature : regulatoryFeatureList) {
            if ((regulatoryFeature.getId() == null && id == null)
                    || (regulatoryFeature.getId() != null && regulatoryFeature.getId().equals(id))) {
                return regulatoryFeature;
            }
        }
        return null;
    }

}