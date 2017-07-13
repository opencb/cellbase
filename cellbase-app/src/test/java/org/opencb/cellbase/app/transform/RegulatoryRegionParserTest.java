package org.opencb.cellbase.app.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOExceptionWithCause;
import org.junit.Test;
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

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 13/07/17.
 */
public class RegulatoryRegionParserTest {

    @Test
    public void parser() throws IOException, URISyntaxException, NoSuchMethodException, SQLException, ClassNotFoundException {
        Path regulationFolder = Paths.get(getClass().getResource("/regulation").toURI());

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "regulatory_region", true);
        (new RegulatoryRegionParser(regulationFolder, serializer)).parse();

        List<RegulatoryFeature> variantList = loadSerializedRegulatoryRegions("/tmp/regulatory_region.json.gz");
        assertEquals(3, variantList.size());

    }

    private List<RegulatoryFeature> loadSerializedRegulatoryRegions(String fileName) {
        List<RegulatoryFeature> regulatoryFeatureList = new ArrayList<>();

        try {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(Paths.get(fileName));
            ObjectMapper jsonObjectMapper = new ObjectMapper();
            jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
            jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                regulatoryFeatureList.add(jsonObjectMapper.convertValue(JSON.parse(line), RegulatoryFeature.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
            assertFalse(false);
        }

        return regulatoryFeatureList;
    }
}