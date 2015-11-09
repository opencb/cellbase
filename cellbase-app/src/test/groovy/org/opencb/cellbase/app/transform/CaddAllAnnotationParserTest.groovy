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
import org.opencb.cellbase.app.transform.formats.Cadd
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * Created by parce on 10/21/14.
 */
class CaddAllAnnotationParserTest extends Specification {

    def caddParserChr1
    def caddParserChrX
    def serializer
    List<Cadd> serializedVariants

    def setup() {
        // custom test serializer that adds the serialized variants to a list
        serializer = Mock(CellBaseSerializer)
        serializedVariants = new ArrayList<Cadd>()
        serializer.serialize(_) >> { Cadd arg -> serializedVariants.add(arg) }

        def caddFile = Paths.get(CaddAllAnnotationParserTest.class.getResource("/caddTest.tsv.gz").toURI())
        caddParserChr1 = new CaddAllAnnotationParser(caddFile, "1", serializer)
        caddParserChrX  = new CaddAllAnnotationParser(caddFile, "X", serializer)
    }

    def "Parse"() {
        when: "parse chromosome 1"
        caddParserChr1.parse()
        then: "serialize 6 variants"
        6 * serializer.serialize(_)

        when: "parse chromosome X"
        caddParserChrX.parse()
        then: "serialize 4 variants"
        4 * serializer.serialize(_)
    }

    @Unroll
    def "parsed variant #chr:#start-#end #ref #alt has #scores cadd score values"() {
        given:
        caddParserChr1.parse()
        caddParserChrX.parse()

        expect:
        serializedVariants[variantNumber].chromosome.equals(chr)
        serializedVariants[variantNumber].start.equals(start)
        serializedVariants[variantNumber].end.equals(end)
        serializedVariants[variantNumber].reference.equals(ref)
        serializedVariants[variantNumber].alternate.equals(alt)
        serializedVariants[variantNumber].caddValues.size().equals(scores)

        where:
        variantNumber || chr | start  | end     | ref | alt | scores
        0             || "1" | 10001  | 10001   | "T" | "A" |  3
        1             || "1" | 10001  | 10001   | "T" | "C" |  3
        2             || "1" | 10001  | 10001   | "T" | "G" |  3
        3             || "1" | 10002  | 10002   | "A" | "C" |  3
        4             || "1" | 10002  | 10002   | "A" | "G" |  3
        5             || "1" | 10002  | 10002   | "A" | "T" |  3
        6             || "X" | 63148  | 63148   | "A" | "C" |  1
        7             || "X" | 63148  | 63148   | "A" | "G" |  1
        8             || "X" | 63148  | 63148   | "A" | "T" |  1
        9             || "X" | 63149  | 63149   | "G" | "A" |  1
    }

    @Unroll
    def "parsed variant #chr:#start-#end #ref #alt has #score cadd phred score for feature #feature"() {
        given:
        caddParserChr1.parse()
        caddParserChrX.parse()

        expect:
        serializedVariants[variantNumber].chromosome.equals(chr)
        serializedVariants[variantNumber].start.equals(start)
        serializedVariants[variantNumber].end.equals(end)
        serializedVariants[variantNumber].reference.equals(ref)
        serializedVariants[variantNumber].alternate.equals(alt)
        serializedVariants[variantNumber].caddValues.findAll{ v -> v.genomicFeature.equals(feature) }.first().phred == new Float(score)

        where:
        variantNumber || chr | start | end   | ref | alt | score | feature
        0             || "1" | 10001 | 10001 | "T" | "A" | 1.597 | "ENSR00000668495"
        0             || "1" | 10001 | 10001 | "T" | "A" | 1.597 | "ENST00000456328"
        0             || "1" | 10001 | 10001 | "T" | "A" | 1.597 | "ENST00000488147"
        1             || "1" | 10001 | 10001 | "T" | "C" | 1.165 | "ENSR00000668495"
        1             || "1" | 10001 | 10001 | "T" | "C" | 1.165 | "ENST00000456328"
        1             || "1" | 10001 | 10001 | "T" | "C" | 1.165 | "ENST00000488147"
        2             || "1" | 10001 | 10001 | "T" | "G" | 1.186 | "ENST00000456328"
        2             || "1" | 10001 | 10001 | "T" | "G" | 1.186 | "ENST00000488147"
        2             || "1" | 10001 | 10001 | "T" | "G" | 1.186 | "ENSR00000668495"
        6             || "X" | 63148 | 63148 | "A" | "C" | 2.323 | "NA"
        7             || "X" | 63148 | 63148 | "A" | "G" | 2.296 | "NA"
        8             || "X" | 63148 | 63148 | "A" | "T" | 2.809 | "NA"
        9             || "X" | 63149 | 63149 | "G" | "A" | 1.770 | "NA"
    }
}
