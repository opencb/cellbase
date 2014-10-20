package org.opencb.cellbase.build.transform

import org.opencb.cellbase.build.serializers.CellBaseSerializer
import spock.lang.Specification

import java.nio.file.Paths

/**
 * Created by parce on 10/16/14.
 */
class GwasParserTest extends Specification {

    def "parse"() {
        given: "a gwas file containing 18 lines, and a dbsnp subset containing all the snps of the gwas file"
        def serializer = Mock(CellBaseSerializer)
        def gwasTestFile = Paths.get(VariantEffectParserTest.class.getResource("/gwasTest.csv").toURI())
        def dbSnpFile = Paths.get(VariantEffectParserTest.class.getResource("/dbSnpTest.gz").toURI())
        def gwasParser = new GwasParser(serializer, gwasTestFile, dbSnpFile)

        when: "parse the gwas file"
        gwasParser.parse()

        then: "11 variants should be created, because some of the lines of the input file are different studies or tests of the same variant"
        11 * serializer.serialize(_)
    }

}
