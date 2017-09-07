package org.opencb.cellbase.app.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mortbay.util.ajax.JSON;
import org.opencb.biodata.models.variant.avro.VariantAvro;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 15/05/17.
 */
public class DgvParserTest {
    @Test
    public void parse() throws Exception {
        Path dgvFile = Paths.get(getClass().getResource("/dgv.txt.gz").getFile());
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "dgv.test");
        (new DgvParser(dgvFile, serializer)).parse();
        serializer.close();
        assertEquals(loadDGVSet(Paths.get(getClass().getResource("/dgv.json.gz").getFile())),
                loadDGVSet(Paths.get("/tmp/dgv.test.json.gz")));
    }

    private Set<VariantAvro> loadDGVSet(Path path) throws IOException {
        Set<VariantAvro> variantAvroSet = new HashSet<>(5);
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(path)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                variantAvroSet.add(jsonObjectMapper.convertValue(JSON.parse(line), VariantAvro.class));
                line = bufferedReader.readLine();
            }
        }

        return variantAvroSet;
    }



}