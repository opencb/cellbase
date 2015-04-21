/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
