package org.opencb.cellbase.build.transform

import org.opencb.biodata.models.variant.clinical.Cosmic
import org.opencb.cellbase.build.serializers.CellBaseSerializer
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * Created by lcruz on 23/10/14.
 */
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
        def cosmicFile = Paths.get(VariantEffectParserTest.class.getResource("/cosmicTest.csv").toURI())

        cosmicParser = new CosmicParser(serializer, cosmicFile)
    }

    def "Parse"() {
        when:
        cosmicParser.parse()
        then: "serialize 4 variants"
        serializedVariants.size() == 7
    }

    @Unroll
    def "parsed variant #chr:#start-#end #ref #alt cosmic values"() {
        expect:
        serializedVariants[variantNumber].chromosome.equals(chr)
        serializedVariants[variantNumber].start.equals(start)
        serializedVariants[variantNumber].end.equals(end)
        serializedVariants[variantNumber].reference.equals(ref)
        serializedVariants[variantNumber].alternate.equals(alt)

        where:
        variantNumber || chr  | start     | end       | ref      | alt
        0             || "12" | 25398285  | 25398285  | "G"      | "T"
        1             || "1"  | 215793922 | 215793922 | "A"      | "-"
        2             || "4"  | 152069187 | 152069188 | "-"      | "A"
        3             || "17" | 7578478   | 7578478   | "C"      | "T"
        4             || "5"  | 35874605  | 35874607  | "CTC"    | "AAAAAG"
        5             || "19" | 13054627  | 13054628  | "-"      | "TTGTC"
        6             || "9"  | 5070038   | 5070043   | "GAAGAT" | "-"
    }
}