package org.opencb.cellbase.core.variant;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class VariantAnnotationUtilsTest {
    @RunWith(Parameterized.class)
    public static class GetVariantAnnotationTest {
        private String variant;
        private VariantType expectedVariantType;
        public GetVariantAnnotationTest(String variant, VariantType expectedVariantType){
            this.variant = variant;
            this.expectedVariantType = expectedVariantType;
        }
        @Test
        public void test() {
            assertEquals(VariantAnnotationUtils.getVariantType(new Variant(variant)), expectedVariantType);
        }
        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {"13:52718051:N:TGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGN", VariantType.INSERTION},
                    {"13:52718051:C:TGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTGTG", VariantType.MNV},
                    {"13:52718051:C:TGTGTG", VariantType.MNV},
                    {"13:52718051:C:G", VariantType.SNV},
                    {"13:52718051:C:", VariantType.DELETION},
                    {"13:52718051::G", VariantType.INSERTION}
            });
        }
    }
}
