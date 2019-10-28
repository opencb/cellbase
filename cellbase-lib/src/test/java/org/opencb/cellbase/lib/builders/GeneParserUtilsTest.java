/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.builders;


import org.junit.Test;
import org.opencb.biodata.models.core.Constraint;
import org.opencb.cellbase.lib.builders.GeneParserUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import static org.junit.Assert.assertEquals;


public class GeneParserUtilsTest {
    @Test
    public void testGetConstraints() throws Exception {
        Path gnmoadFile = Paths.get(getClass().getResource("/gnomad.v2.1.1.lof_metrics.by_transcript.txt.gz").getFile());
        Map<String, List<Constraint>> constraints = GeneParserUtils.getConstraints(gnmoadFile);

        Constraint constraint1 = new Constraint("gnomAD", "pLoF", "oe_mis", 1.0187);
        Constraint constraint2 = new Constraint("gnomAD", "pLoF", "oe_syn", 1.0252);
        Constraint constraint3 = new Constraint("gnomAD", "pLoF", "oe_lof", 0.73739);
        List<Constraint> expected = new ArrayList<>();
        expected.add(constraint1);
        expected.add(constraint2);
        expected.add(constraint3);

        List<Constraint> transcriptConstraints = constraints.get("ENST00000600966");
        assertEquals(3, transcriptConstraints.size());
        assertEquals(expected, transcriptConstraints);


        constraint1 = new Constraint("gnomAD", "pLoF", "oe_mis", 1.0141);
        constraint2 = new Constraint("gnomAD", "pLoF", "oe_syn", 1.0299);
        constraint3 = new Constraint("gnomAD", "pLoF", "oe_lof", 0.78457);
        Constraint constraint4 = new Constraint("gnomAD", "pLoF", "exac_pLI", 9.0649E-5);
        Constraint constraint5 = new Constraint("gnomAD", "pLoF", "exac_oe_lof", 0.65033);
        expected = new ArrayList<>();
        expected.add(constraint1);
        expected.add(constraint2);
        expected.add(constraint3);
        expected.add(constraint4);
        expected.add(constraint5);

        transcriptConstraints = constraints.get("ENST00000263100");
        assertEquals(5, transcriptConstraints.size());
        assertEquals(expected, transcriptConstraints);

        List<Constraint> geneConstraints = constraints.get("ENSG00000121410");
        assertEquals(5, geneConstraints.size());
        assertEquals(expected, geneConstraints);
    }
}