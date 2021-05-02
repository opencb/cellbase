package org.opencb.cellbase.app.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.util.JSON;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;

/**
 * Created by fjlopez on 14/07/17.
 */
public class GenericParserTest<T> {

    protected static final ObjectMapper jsonObjectMapper = new ObjectMapper();
    Class<T> clazz;

    public GenericParserTest(Class<T> clazz) {
        this.clazz = clazz;
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    protected List<T> loadSerializedObjects(String fileName) {
        List<T> objectList = new ArrayList<>();

        try {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(Paths.get(fileName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                objectList.add(parseObject(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
            assertFalse(false);
        }

        return objectList;
    }

    protected T parseObject(String jsonString) {
        return jsonObjectMapper.convertValue(JSON.parse(jsonString), clazz);
    }

}
