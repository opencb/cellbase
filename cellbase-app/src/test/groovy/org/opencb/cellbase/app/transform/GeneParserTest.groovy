package org.opencb.cellbase.app.transform

import org.opencb.biodata.models.core.Gene
import org.opencb.cellbase.app.serializers.CellBaseSerializer
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
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
    def "gene #geneId has #transcriptsNumber transcripts"() {
        expect:
        serializedGenes.findAll({gene -> gene.getId().equals(geneId)}).size() == 1
        serializedGenes.findAll({gene -> gene.getId().equals(geneId)}).first().transcripts.size() == transcriptsNumber

        where:
        geneId || transcriptsNumber
        "ENSG00000243485" || 2
        "ENSG00000218839" || 2
    }

    @Unroll
    def "transcript #transcriptId from gene #geneId well parsed"() {
        expect:
        def transcript = serializedGenes.find({gene -> gene.getId() == geneId}).transcripts.find({transcript -> transcript.getId() == transcriptId})
        transcript.getName() == name
        transcript.getBiotype() == biotype
        transcript.getStatus() == status
        transcript.getChromosome() == chromosome
        transcript.getStart() == start
        transcript.getEnd() == end
        transcript.getExons().size() == exonsNumber

        where:
        transcriptId     || geneId            | name             | biotype   | status  | chromosome | start | end   | strand | exonsNumber
        "ENST00000473358"|| "ENSG00000243485" | "MIR1302-11-001" | "lincRNA" | "KNOWN" | "1"        | 29554 | 31097 | "+"    | 3
        "ENST00000469289"|| "ENSG00000243485" | "MIR1302-11-002" | "lincRNA" | "KNOWN" | "1"        | 30267 | 31109 | "+"    | 2
        "ENST00000449442"|| "ENSG00000218839" | "FAM138C-001"    | "lincRNA" | "KNOWN" | "9"        | 34394 | 35860 | "-"    | 3
        "ENST00000305248"|| "ENSG00000218839" | "FAM138C-002"    | "lincRNA" | "KNOWN" | "9"        | 34965 | 35871 | "-"    | 2
    }


    def cleanupSpec() {
        File referenceGenomeSqlLiteFile = Paths.get(GeneParserTest.class.getResource("/geneParser/reference_genome.db").toURI()).toFile()
        if (referenceGenomeSqlLiteFile.exists()) {
            referenceGenomeSqlLiteFile.delete()
        }
    }
}
