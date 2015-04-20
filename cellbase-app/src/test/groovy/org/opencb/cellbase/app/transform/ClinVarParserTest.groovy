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

package org.opencb.cellbase.app.transform

import org.opencb.cellbase.core.serializer.CellBaseSerializer
import org.opencb.cellbase.core.common.clinical.ClinvarPublicSet
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * Created by lcruz on 23/10/14.
 */
class ClinVarParserTest extends Specification {

    @Shared
    def clinvarParser
    @Shared
    List<ClinvarPublicSet> serializedVariants

    def setupSpec() {
        // custom test serializer that adds the serialized variants to a list
        def serializer = Mock(CellBaseSerializer)
        serializedVariants = new ArrayList<ClinvarPublicSet>()
        serializer.serialize(_) >> { ClinvarPublicSet arg -> serializedVariants.add(arg) }
        def clinvarXmlFile = Paths.get(VariantEffectParserTest.class.getResource("/clinvar_v19_test.xml").toURI())

        clinvarParser = new ClinVarParser(clinvarXmlFile, ClinVarParser.GRCH37_ASSEMBLY, serializer)
    }

    def "Parse"() {
        when:
        clinvarParser.parse()
        then: "serialize 3 variants"
        serializedVariants.size() == 3
    }

    @Unroll
    def "parsed variant #chr:#start-#end #ref #alt cosmic values"() {
        expect:
        serializedVariants[variantNumber].chromosome.equals(chr)
        serializedVariants[variantNumber].start == start
        serializedVariants[variantNumber].end == end
        serializedVariants[variantNumber].reference.equals(ref)
        serializedVariants[variantNumber].alternate.equals(alt)

        where:
        variantNumber || chr  | start     | end       | ref | alt
        0             || "12" | 2795019   | 2795019   | "C" | "T"
        1             || "14" | 24709794  | 24709794  | "G" | "-"
        2             || "4"  | 187120195 | 187120196 | "A" | "AA"
    }
}