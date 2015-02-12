package org.opencb.cellbase.core.properties;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by parce on 12/02/15.
 */
public class PropertiesReader {

    public static CellBaseProperties readCellBaseProperties() throws IOException {
        Path cellbasePropertiesJsonFile = Paths.get(PropertiesReader.class.getClassLoader().getResource("cellbaseProperties.json").getFile());
        return readCellBaseProperties(cellbasePropertiesJsonFile);
    }

    public static CellBaseProperties readCellBaseProperties(Path cellbasePropertiesJsonFile) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        CellBaseProperties properties = jsonMapper.readValue(cellbasePropertiesJsonFile.toFile(), CellBaseProperties.class);
        return properties;
    }
}
