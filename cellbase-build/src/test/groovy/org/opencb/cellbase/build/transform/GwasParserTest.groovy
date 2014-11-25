package org.opencb.cellbase.build.transform

import org.opencb.cellbase.core.serializer.CellBaseSerializer
import org.opencb.cellbase.core.common.clinical.Gwas
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * Created by parce on 10/16/14.
 */
class GwasParserTest extends Specification {

    static def serializedVariants

    def setupSpec() {
        // gwas file containing 10 lines, and a dbsnp subset containing all the snps of the gwas file
        def gwasTestFile = Paths.get(VariantEffectParserTest.class.getResource("/gwasTest.csv").toURI())
        def dbSnpFile = Paths.get(VariantEffectParserTest.class.getResource("/dbSnpTest.gz").toURI())

        // custom test serializer that adds the serialized variants to a list
        def serializer = Mock(CellBaseSerializer)
        serializedVariants = new ArrayList<Gwas>()
        serializer.serialize(_) >> { Gwas arg -> serializedVariants.add(arg) }

        def gwasParser = new GwasParser(gwasTestFile, dbSnpFile, serializer)
        gwasParser.parse()
    }

    @Unroll
    def "parsed variant #variantNumber #chr:#start #reference #alternate has #studies studies"() {
        expect:
        serializedVariants[variantNumber].chromosome.equals(chr)
        serializedVariants[variantNumber].start.equals(start)
        serializedVariants[variantNumber].reference.equals(reference)
        serializedVariants[variantNumber].alternate.equals(alternate)
        serializedVariants[variantNumber].studies.size().equals(studies)

        where:
        variantNumber || chr  | start    | reference | alternate | studies
        0             || "11" | 89011046 | "G"       | "A"       |  1
        1             || "16" | 89986117 | "C"       | "G"       |  1
        2             || "16" | 89986117 | "C"       | "T"       |  1
        3             || "9"  | 16915021 | "T"       | "C"       |  3
    }

    @Unroll
    def "parsed variant #variantNumber #chr:#start #reference #alternate has #traits traits"() {
        expect:
        // 4 * serializer.serialize(_)
        serializedVariants[variantNumber].chromosome.equals(chr)
        serializedVariants[variantNumber].studies.first().traits.size().equals(traits)


        where:
        variantNumber || chr  | start    | reference | alternate | traits
        0             || "11" | 89011046 | "G"       | "A"       | 2
        1             || "16" | 89986117 | "C"       | "G"       | 4
        2             || "16" | 89986117 | "C"       | "T"       | 4
        3             || "9"  | 16915021 | "T"       | "C"       | 1
    }

    @Unroll
    def "parsed variant #variantNumber #chr:#start #reference #alternate has #tests tests"() {
        expect:
        // 4 * serializer.serialize(_)
        serializedVariants[variantNumber].chromosome.equals(chr)
        serializedVariants[variantNumber].studies.first().traits.first().tests.size().equals(tests)


        where:
        variantNumber || chr  | start    | reference | alternate | tests
        0             || "11" | 89011046 | "G"       | "A"       | 1
        1             || "16" | 89986117 | "C"       | "G"       | 1
        2             || "16" | 89986117 | "C"       | "T"       | 1
        3             || "9"  | 16915021 | "T"       | "C"       | 2
    }

    @Unroll
    def "parsed variant #variantNumber #chr:#start #reference #alternate contains '#trait' trait is #variantContainsTrait"() {
        expect:
        serializedVariants[variantNumber].studies.first().traits.diseaseTrait.contains(diseaseOrTrait) == variantContainsTrait

        where:
        variantNumber || chr  | start    | reference | alternate | diseaseOrTrait                | variantContainsTrait
        0             || "11" | 89011046 | "G"       | "A"       | "Blue vs. green eyes"         | true
        0             || "11" | 89011046 | "G"       | "A"       | "Skin sensitivity to sun"     | true
        0             || "11" | 89011046 | "G"       | "A"       | "Red vs non-red hair color"   | false
        0             || "11" | 89011046 | "G"       | "A"       | "Blond vs. brown hair color"  | false
        0             || "11" | 89011046 | "G"       | "A"       | "Freckles"                    | false
        1             || "16" | 89986117 | "C"       | "T"       | "Blue vs. green eyes"         | false
        1             || "16" | 89986117 | "C"       | "T"       | "Skin sensitivity to sun"     | true
        1             || "16" | 89986117 | "C"       | "T"       | "Red vs non-red hair color"   | true
        1             || "16" | 89986117 | "C"       | "T"       | "Blond vs. brown hair color"  | true
        1             || "16" | 89986117 | "C"       | "T"       | "Freckles"                    | true
        2             || "16" | 89986117 | "C"       | "T"       | "Blue vs. green eyes"         | false
        2             || "16" | 89986117 | "C"       | "T"       | "Skin sensitivity to sun"     | true
        2             || "16" | 89986117 | "C"       | "T"       | "Red vs non-red hair color"   | true
        2             || "16" | 89986117 | "C"       | "T"       | "Blond vs. brown hair color"  | true
        2             || "16" | 89986117 | "C"       | "T"       | "Freckles"                    | true
    }

    @Unroll
    def "parsed variant #variantNumber #chr:#start #reference #alternate contains '#test' test"() {
        expect:
        serializedVariants[variantNumber].studies.first().traits.first().tests.pValueText.contains(test)

        where:
        variantNumber || chr  | start    | reference | alternate | test
        3             || "9"  | 16915021 | "T"       | "C"       | "(All invasive)"
        3             || "9"  | 16915021 | "T"       | "C"       | "(Serious invasive)"
    }

}
