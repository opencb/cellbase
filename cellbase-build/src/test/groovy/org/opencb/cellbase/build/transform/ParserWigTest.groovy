package org.opencb.cellbase.build.transform
import org.opencb.biodata.models.feature.ConservedRegionFeature
import org.opencb.cellbase.build.serializers.json.CellBaseJsonSerializer
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths
/**
 * @author lcruz
 * @since 04/11/2014
 */
class ParserWigTest extends Specification {
    @Shared
    def parserWig
    @Shared
    List<ConservedRegionFeature> serializedVariants

    def setupSpec() {
        // custom test serializer that adds the serialized variants to a list
        Path outputFilePath = Paths.get("/home/lcruz/Escritorio/salidaPhylop.txt")
        CellBaseJsonSerializer serializer = new CellBaseJsonSerializer(outputFilePath, true);
        //def serializer = Mock(CellBaseSerializer)
        serializedVariants = new ArrayList<ConservedRegionFeature>()
//        serializer.serialize(_) >> { ConservedRegionFeature arg -> serializedVariants.add(arg) }
//        seralizer.serialize(_) >> {ConservedRegionFeature arg -> arg.toString()}
        //def phylopFile = Paths.get(VariantEffectParserTest.class.getResource("/cosmicTest.csv").toURI())
        def phylopFile = Paths.get("/home/lcruz/Escritorio")

        parserWig = new ParserWig(serializer, phylopFile, 2000, "phylop")
    }

    def "Parse"() {
        when:
        parserWig.parse()
        then: "serialize 4 variants"
        serializedVariants.size() == 7
    }

   /* @Unroll
    def "parsed variant #variantNumber has coordinates #chr:#start-#end, reference #ref  and alternate #alt"() {
        given:
        phylopParser.parse()

        expect:
        serializedVariants[variantNumber].chromosome.equals(chr)
        serializedVariants[variantNumber].start.equals(start)
        serializedVariants[variantNumber].end.equals(end)

        where:
        variantNumber || chr  | start     | end
        0             || "12" | 25398285  | 25398285
    }

    @Unroll
    def "parsed variant #chr:#start-#end #ref #alt has fathcm '#fathmm', pubmedid #pubmed, study id #idStudy and age #age"() {
        expect:
        serializedVariants[variantNumber].chromosome.equals(chr)
        serializedVariants[variantNumber].start.equals(start)
        serializedVariants[variantNumber].end.equals(end)

        where:
        variantNumber || chr  | start     | end
        0             || "12" | 25398285  | 25398285
        1             || "1"  | 215793922 | 215793922
        2             || "4"  | 152069187 | 152069188
        3             || "17" | 7578478   | 7578478
        4             || "5"  | 35874605  | 35874607
        5             || "19" | 13054627  | 13054628
        6             || "9"  | 5070038   | 5070043
    }*/
}
