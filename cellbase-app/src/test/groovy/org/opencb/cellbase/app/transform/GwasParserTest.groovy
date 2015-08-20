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
import org.opencb.cellbase.core.common.clinical.gwas.Gwas
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * Created by parce on 10/16/14.
 */
class GwasParserTest extends Specification {

    static List<Gwas> serializedVariants

    def setupSpec() {
        // gwas file containing 10 lines, and a dbsnp subset containing all the snps of the gwas file
        def gwasTestFile = Paths.get(GwasParserTest.class.getResource("/gwasTest.csv").toURI())
        def dbSnpFile = Paths.get(GwasParserTest.class.getResource("/dbSnpTest.gz").toURI())

        // custom test serializer that adds the serialized variants to a list
        def serializer = Mock(CellBaseSerializer)
        serializedVariants = new ArrayList<Gwas>()
        serializer.serialize(_) >> { Gwas arg -> serializedVariants.add(arg) }

        def gwasParser = new GwasParser(gwasTestFile, dbSnpFile, serializer)
        gwasParser.parse()
    }

    @Unroll
    def "parsed variant #chr:#start #reference #alternate has #studies studies, #traits traits and #tests tests"() {
        expect:
        def gwas = serializedVariants.find{ gwas -> gwas.chromosome == chr && gwas.start == start &&
                                                    gwas.reference == reference && gwas.alternate == alternate }
        gwas.studies.size() == studies
        gwas.studies.first().traits.size() == traits
        gwas.studies.first().traits.first().tests.size() == tests

        where:
        chr  | start     | reference | alternate || studies | traits | tests | source
        "11" | 89011046  | "G"       | "A"       ||  1      | 2      | 1     | "gwas"
        "16" | 89986117  | "C"       | "G"       ||  1      | 4      | 1     | "gwas"
        "16" | 89986117  | "C"       | "T"       ||  1      | 4      | 1     | "gwas"
        "9"  | 16915021  | "T"       | "C"       ||  3      | 1      | 2     | "gwas"
        "X"  | 102302457 | "A"       | "G"       ||  1      | 1      | 1     | "gwas"
    }

    @Unroll
    def "parsed variant #chr:#start #reference #alternate contains '#diseaseOrTrait' trait is #variantContainsTrait"() {
        expect:
        def gwas = serializedVariants.find{ gwas -> gwas.chromosome == chr && gwas.start == start &&
                                                    gwas.reference == reference && gwas.alternate == alternate }
        gwas.studies.first().traits.diseaseTrait.contains(diseaseOrTrait) == variantContainsTrait

        where:
        chr  | start    | reference | alternate | diseaseOrTrait                || variantContainsTrait
        "11" | 89011046 | "G"       | "A"       | "Blue vs. green eyes"         || true
        "11" | 89011046 | "G"       | "A"       | "Skin sensitivity to sun"     || true
        "11" | 89011046 | "G"       | "A"       | "Red vs non-red hair color"   || false
        "11" | 89011046 | "G"       | "A"       | "Blond vs. brown hair color"  || false
        "11" | 89011046 | "G"       | "A"       | "Freckles"                    || false
        "16" | 89986117 | "C"       | "T"       | "Blue vs. green eyes"         || false
        "16" | 89986117 | "C"       | "T"       | "Skin sensitivity to sun"     || true
        "16" | 89986117 | "C"       | "T"       | "Red vs non-red hair color"   || true
        "16" | 89986117 | "C"       | "T"       | "Blond vs. brown hair color"  || true
        "16" | 89986117 | "C"       | "T"       | "Freckles"                    || true
        "16" | 89986117 | "C"       | "T"       | "Blue vs. green eyes"         || false
        "16" | 89986117 | "C"       | "T"       | "Skin sensitivity to sun"     || true
        "16" | 89986117 | "C"       | "T"       | "Red vs non-red hair color"   || true
        "16" | 89986117 | "C"       | "T"       | "Blond vs. brown hair color"  || true
        "16" | 89986117 | "C"       | "T"       | "Freckles"                    || true
    }

    @Unroll
    def "parsed variant #chr:#start #reference #alternate contains '#test' test"() {
        expect:
        def gwas = serializedVariants.find{ gwas -> gwas.chromosome == chr && gwas.start == start &&
                                                    gwas.reference == reference && gwas.alternate == alternate }
        gwas.studies.first().traits.first().tests.pValueText.contains(test)

        where:
        chr  | start    | reference | alternate || test
        "9"  | 16915021 | "T"       | "C"       || "(All invasive)"
        "9"  | 16915021 | "T"       | "C"       || "(Serious invasive)"
    }

}
