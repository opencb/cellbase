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

import org.junit.Ignore
import org.opencb.cellbase.core.serializer.CellBaseSerializer
import org.opencb.cellbase.core.common.clinical.Cosmic
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * Created by lcruz on 23/10/14.
 */
@Ignore
class CosmicParserTest extends Specification {

    @Shared
    def cosmicParser
    @Shared
    List<Cosmic> serializedVariants

    def setupSpec() {
        // custom test serializer that adds the serialized variants to a list
        def serializer = Mock(CellBaseSerializer)
        serializedVariants = new ArrayList<Cosmic>()
        serializer.serialize(_) >> { Cosmic arg -> serializedVariants.add(arg) }
        def cosmicFile = Paths.get(CosmicParserTest.class.getResource("/cosmicTest.csv").toURI())

        cosmicParser = new CosmicParser(cosmicFile, serializer)
    }

    def "Parse"() {
        when:
        cosmicParser.parse()
        then: "serialize 4 variants"
        serializedVariants.size() == 7
    }

    @Unroll
    def "parsed variant #variantNumber has coordinates #chr:#start-#end, reference #ref  and alternate #alt"() {
        expect:
        serializedVariants[variantNumber].chromosome.equals(chr)
        serializedVariants[variantNumber].start.equals(start)
        serializedVariants[variantNumber].end.equals(end)
        serializedVariants[variantNumber].reference.equals(ref)
        serializedVariants[variantNumber].alternate.equals(alt)
        serializedVariants[variantNumber].source.equals(source)

        where:
        variantNumber || chr  | start     | end       | ref      | alt      | source
        0             || "12" | 25398285  | 25398285  | "G"      | "T"      | "cosmic"
        1             || "1"  | 215793922 | 215793922 | "A"      | "-"      | "cosmic"
        2             || "4"  | 152069187 | 152069188 | "-"      | "A"      | "cosmic"
        3             || "17" | 7578478   | 7578478   | "C"      | "T"      | "cosmic"
        4             || "5"  | 35874605  | 35874607  | "CTC"    | "AAAAAG" | "cosmic"
        5             || "19" | 13054627  | 13054628  | "-"      | "TTGTC"  | "cosmic"
        6             || "9"  | 5070038   | 5070043   | "GAAGAT" | "-"      | "cosmic"
    }

    @Unroll
    def "parsed variant #chr:#start-#end #ref #alt has fathcm '#fathmm', pubmedid #pubmed, study id #idStudy and age #age"() {
        expect:
        serializedVariants[variantNumber].chromosome.equals(chr)
        serializedVariants[variantNumber].start.equals(start)
        serializedVariants[variantNumber].end.equals(end)
        serializedVariants[variantNumber].reference.equals(ref)
        serializedVariants[variantNumber].alternate.equals(alt)
        serializedVariants[variantNumber].fathmmPrediction.equals(fathmm)
        serializedVariants[variantNumber].pubmedPMID.equals(pubmed)
        serializedVariants[variantNumber].idStudy.equals(idStudy)
        serializedVariants[variantNumber].age == age

        where:
        variantNumber || chr  | start     | end       | ref      | alt      | fathmm   | pubmed     | idStudy | age
        0             || "12" | 25398285  | 25398285  | "G"      | "T"      | "CANCER" | "23205087" | null    | null
        1             || "1"  | 215793922 | 215793922 | "A"      | "-"      | ""       | "24293293" | 529     | 53.0
        2             || "4"  | 152069187 | 152069188 | "-"      | "A"      | ""       | "22980975" | 431     | 80.0
        3             || "17" | 7578478   | 7578478   | "C"      | "T"      | "CANCER" | "10780666" | null    | 24.0
        4             || "5"  | 35874605  | 35874607  | "CTC"    | "AAAAAG" | ""       | "21536738" | null    | null
        5             || "19" | 13054627  | 13054628  | "-"      | "TTGTC"  | ""       | "24895336" | null    | null
        6             || "9"  | 5070038   | 5070043   | "GAAGAT" | "-"      | ""       | "17984312" | null    | 80.0
    }
}