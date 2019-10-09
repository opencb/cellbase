package org.opencb.cellbase.app.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
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