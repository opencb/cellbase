package org.opencb.cellbase.app.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mortbay.util.ajax.JSON;
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.biodata.models.variant.avro.VariantAvro;
import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;



/**
 * Created by fjlopez on 10/05/17.
 */
public class RepeatsParserTest extends GenericParserTest<Repeat> {

    public RepeatsParserTest() {
        super(Repeat.class);
    }

    @Test
    public void testParse() throws Exception {
        Path repeatsFilesDir = Paths.get(getClass().getResource("/repeats").getPath());
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "repeats.test");
        (new RepeatsParser(repeatsFilesDir, serializer)).parse();
        serializer.close();
        assertEquals(loadRepeatSet(Paths.get(getClass().getResource("/repeats/repeats.test.json.gz").getFile())),
                loadRepeatSet(Paths.get("/tmp/repeats.test.json.gz")));
    }

    private Set<Repeat> loadRepeatSet(Path path) throws IOException {
        Set<Repeat> repeatSet = new HashSet<>(16);

        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(path)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                repeatSet.add(jsonObjectMapper.convertValue(JSON.parse(line), Repeat.class));
                line = bufferedReader.readLine();
            }
        }

        return repeatSet;
    }


}