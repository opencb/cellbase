package org.opencb.cellbase.lib.builders;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PharmGKBBuilderTest {

    @Test
    public void parseGenericNames() {
        String genericName = "\"\"\"(2R,3R,11bR)-9-methoxy-3-(2-methylpropyl)-2,3,4,6,7,11b-hexahydro-1H-benzo[a]quinolizine-2,10-diol\"\", \"\"10-o-desmethyl-alpha-htbz\"\"\"";
        List<String> names = stringFieldToList(genericName);
        assertTrue(names.size() == 2);
        assertTrue(names.contains("(2R,3R,11bR)-9-methoxy-3-(2-methylpropyl)-2,3,4,6,7,11b-hexahydro-1H-benzo[a]quinolizine-2,10-diol"));
        assertTrue(names.contains("10-o-desmethyl-alpha-htbz"));
    }

    private List<String> stringFieldToList(String field) {
        if (field.startsWith("\"")) {
            return Arrays.stream(field.replace("\"\"\"", "\"").replace("\"\"", "\"").replace("\", \"", "\",\"").split("\",\""))
                    .map(s -> s.replace("\"", "").trim()).collect(Collectors.toList());
        } else {
            if (field.contains(", ")) {
                return Arrays.stream(field.replace(", ", ",").split(",")).map(String::trim).collect(Collectors.toList());
            } else {
                return Collections.singletonList(field);
            }
        }
    }
}