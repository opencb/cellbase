package org.opencb.cellbase.app.transform.clinical.variant;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mortbay.util.ajax.JSON;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAvro;
import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 07/10/16.
 */
public class ClinicalVariantParserTest {
    @Test
    public void parse() throws Exception {
        Path clinicalVariantFolder = Paths.get(getClass().getResource("/clinicalVariant").toURI());

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"), "clinical_variant");
        (new ClinicalVariantParser(clinicalVariantFolder, "GRCh37",  serializer)).parse();

        List<Variant> variantList = loadSerializedVariants("/tmp/clinical_variant.json.gz");
        assertEquals(4, variantList.size());

        assertThat(variantList.get(0).getAnnotation().getVariantTraitAssociation().getSomatic().stream()
                .map(somatic -> somatic.getAccession()).collect(Collectors.toList()),
                CoreMatchers.hasItems("COSM1193237"));
        assertThat(variantList.get(0).getAnnotation().getVariantTraitAssociation().getGermline().stream()
                .map(somatic -> somatic.getAccession()).collect(Collectors.toList()),
                CoreMatchers.hasItems("RCV000148505"));

        assertThat(variantList.get(1).getAnnotation().getVariantTraitAssociation().getGermline().stream()
                        .map(somatic -> somatic.getAccession()).collect(Collectors.toList()),
                CoreMatchers.hasItems("RCV000148485"));
        assertThat(variantList.get(1).getAnnotation().getVariantTraitAssociation().getSomatic().stream()
                        .map(somatic -> somatic.getAccession()).collect(Collectors.toList()),
                CoreMatchers.hasItems("COSM5745645"));

        assertThat(variantList.get(2).getAnnotation().getVariantTraitAssociation().getGermline().stream()
                        .map(somatic -> somatic.getAccession()).collect(Collectors.toList()),
                CoreMatchers.hasItems("RCV000148484"));

        assertThat(variantList.get(3).getAnnotation().getVariantTraitAssociation().getGermline().stream()
                        .map(somatic -> somatic.getAccession()).collect(Collectors.toList()),
                CoreMatchers.hasItems("COSM4059225"));


    }

    private List<Variant> loadSerializedVariants(String fileName) {
        List<Variant> variantList = new ArrayList<>(3);

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
                variantList.add(new Variant(jsonObjectMapper.convertValue(JSON.parse(line), VariantAvro.class)));
            }
        } catch (IOException e) {
            e.printStackTrace();
            assertFalse(false);
        }

        return variantList;
    }

}