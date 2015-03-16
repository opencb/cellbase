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
        def gene = serializedGenes.findAll({gene -> gene.getId().equals(geneId)}).first()
        gene.name == geneName
        gene.biotype == biotype
        gene.chromosome == chromosome
        gene.start == start
        gene.end == end
        gene.strand == strand
        gene.transcripts.size() == transcriptsNumber


        where:
        geneId            || geneName     | biotype   | chromosome | start | end   | strand | transcriptsNumber
        "ENSG00000243485" || "MIR1302-11" | "lincRNA" | "1"        | 29554 | 31109 | "+"    | 2
        "ENSG00000218839" || "FAM138C"    | "lincRNA" | "9"        | 34394 | 35871 | "-"    | 2
    }

    @Unroll
    def "transcript #transcriptId from gene #geneId parsed"() {
        expect:
        def transcript = serializedGenes.find({gene -> gene.getId() == geneId}).transcripts.find({transcript -> transcript.getId() == transcriptId})
        transcript.name == name
        transcript.biotype == biotype
        transcript.chromosome == chromosome
        transcript.start == start
        transcript.end == end
        transcript.exons.size() == exonsNumber

        where:
        transcriptId     || geneId            | name             | biotype   | chromosome | start | end   | strand | exonsNumber
        "ENST00000473358"|| "ENSG00000243485" | "MIR1302-11-001" | "lincRNA" | "1"        | 29554 | 31097 | "+"    | 3
        "ENST00000469289"|| "ENSG00000243485" | "MIR1302-11-002" | "lincRNA" | "1"        | 30267 | 31109 | "+"    | 2
        "ENST00000449442"|| "ENSG00000218839" | "FAM138C-001"    | "lincRNA" | "9"        | 34394 | 35860 | "-"    | 3
        "ENST00000305248"|| "ENSG00000218839" | "FAM138C-002"    | "lincRNA" | "9"        | 34965 | 35871 | "-"    | 2
    }

    @Unroll
    def "exon #exonId from transcript #transcriptId parsed"() {
        expect:
        def gene = serializedGenes.find({gene -> gene.getId() == geneId})
        def transcript = gene.transcripts.find({transcript -> transcript.getId() == transcriptId})
        def exon = transcript.exons.find({exon -> exon.getId() == exonId})
        exon.chromosome == chromosome
        exon.start == start
        exon.end == end
        exon.strand == strand
        exon.phase == phase
        exon.exonNumber == exonNumber

        where:
        exonId            || geneId            | transcriptId      | chromosome | start | end   | strand | phase | exonNumber
        "ENSE00001947070" || "ENSG00000243485" | "ENST00000473358" | "1"        | 29554 | 30039 | "+"    | -1    | 1
        "ENSE00001922571" || "ENSG00000243485" | "ENST00000473358" | "1"        | 30564 | 30667 | "+"    | -1    | 2
        "ENSE00001827679" || "ENSG00000243485" | "ENST00000473358" | "1"        | 30976 | 31097 | "+"    | -1    | 3
        "ENSE00001841699" || "ENSG00000243485" | "ENST00000469289" | "1"        | 30267 | 30667 | "+"    | -1    | 1
        "ENSE00001890064" || "ENSG00000243485" | "ENST00000469289" | "1"        | 30976 | 31109 | "+"    | -1    | 2
    }

    def "tfbs"() {
        // TODO:
    }

    def cleanupSpec() {
        // delete reference genome sqlLite
        File referenceGenomeSqlLiteFile = Paths.get(GeneParserTest.class.getResource("/geneParser/reference_genome.db").toURI()).toFile()
        if (referenceGenomeSqlLiteFile.exists()) {
            referenceGenomeSqlLiteFile.delete()
        }
    }
}
