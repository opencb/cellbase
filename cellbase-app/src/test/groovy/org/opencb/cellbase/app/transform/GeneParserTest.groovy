package org.opencb.cellbase.app.transform

import org.opencb.biodata.models.core.Gene
import org.opencb.cellbase.app.serializers.CellBaseSerializer
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * Created by parce on 5/03/15.
 */
class GeneParserSpockTest extends Specification {


    static List<Gene> serializedGenes

    def setupSpec() {
        def geneTestDir = Paths.get(GeneParserTest.class.getResource("/geneParser").toURI())
        def genomeSequenceFasta = Paths.get(GeneParserTest.class.getResource("/geneParser/Homo_sapiens.GRCh38.fa").toURI())

        // custom test serializer that adds the serialized variants to a list
        def serializer = Mock(CellBaseSerializer)
        serializedGenes = new ArrayList<Gene>()
        serializer.serialize(_) >> { Gene arg -> serializedGenes.add(arg) }

        def geneParser = new GeneParser(geneTestDir, genomeSequenceFasta, serializer)
        geneParser.parse()
    }

    @Unroll
    def "gene #geneId parsed"() {
        expect:
        serializedGenes.findAll({gene -> gene.getId().equals(geneId)}).size() == 1

        where:
        geneId = "ENSG00000243485"
    }
}
