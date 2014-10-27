package org.opencb.cellbase.build.transform

import org.opencb.biodata.models.variant.clinical.Cosmic
import org.opencb.cellbase.build.serializers.CellBaseSerializer
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths
/**
 * Created by lcruz on 23/10/14.
 */
class CosmicParserTest extends Specification {

    def cosmicParser
    def serializer
    def serializedVariants = new ArrayList<Cosmic>()

    def setup() {
        // custom test serializer that adds the serialized variants to a list
        serializer = Mock(CellBaseSerializer)
        serializedVariants = new ArrayList<Cosmic>()
        serializer.serialize(_) >> { Cosmic arg -> serializedVariants.add(arg) }
        def cosmicFile = Paths.get(VariantEffectParserTest.class.getResource("/cosmicTest.csv").toURI())

        cosmicParser = new CosmicParser(serializer, cosmicFile)
    }

    def "Parse"() {
        when:
        cosmicParser.parse()
        then: "serialize 4 variants"
        4 * serializer.serialize(_)
    }

    @Unroll
    def "parsed variant #chr:#start-#end #ref #alt cosmic values"() {
        given:
        cosmicParser.parse()

        expect:
        serializedVariants[variantNumber].chromosome.equals(chr)
        serializedVariants[variantNumber].start.equals(start)
        serializedVariants[variantNumber].end.equals(end)
        serializedVariants[variantNumber].reference.equals(ref)
        serializedVariants[variantNumber].alternate.equals(alt)

        where:
        variantNumber || chr | start | end | ref | alt
        0 || "1" | 87369101 | 87369101 | "G" | "T"
        1 || "7" | 132719840 | 132719840 | "G" | "A"
        2 || "19" | 58863845 | 58863845 | "C" | "A"
        3 || "1" | 78047725 | 78047725 | "T" | "A"
    }
}