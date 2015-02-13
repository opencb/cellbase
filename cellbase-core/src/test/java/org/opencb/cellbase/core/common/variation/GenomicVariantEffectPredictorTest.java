package org.opencb.cellbase.core.common.variation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.variation.GenomicVariant;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 11/4/13
 * Time: 1:31 PM
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class GenomicVariantEffectPredictorTest {

    @Test
    public void testGetAllEffectsByVariant() throws Exception {
        File file = new File(getClass().getResource("/gene.json").getFile());
        ObjectMapper mapper = new ObjectMapper();
        Gene gene = mapper.readValue(file, Gene.class);
        //System.out.println("gene = " + gene);

        GenomicVariantEffectPredictor genomicVariantEffectPredictor = new GenomicVariantEffectPredictor();
        List<GenomicVariantEffect> effects = genomicVariantEffectPredictor.getAllEffectsByVariant(new GenomicVariant("13", 32889611-1000, "T"), Arrays.asList(gene), null);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(effects));


        effects = genomicVariantEffectPredictor.getAllEffectsByVariant(new GenomicVariant("13", 32890227, "T"), Arrays.asList(gene), null);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(effects));

    }
}
