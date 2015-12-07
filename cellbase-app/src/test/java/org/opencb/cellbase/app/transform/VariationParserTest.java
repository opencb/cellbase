package org.opencb.cellbase.app.transform;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by parce on 03/12/15.
 */
public class VariationParserTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
        // TODO: remove temp files
    }

    @Test
    public void testParse() throws Exception {
        VariationParser parser = new VariationParser(Paths.get(this.getClass().getResource("/variationParser").getPath()),
                new TestSerializer());
        parser.parse();
    }

    class TestSerializer implements CellBaseFileSerializer {

        Map<String, List> serializedVariants = new HashMap<>();

        @Override
        public void serialize(Object object) {

        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public void serialize(Object object, String fileName) {
            List fileNameList = serializedVariants.getOrDefault(fileName, new ArrayList<>());
            fileNameList.add(object);
        }

    }
}
