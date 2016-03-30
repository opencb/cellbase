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

import org.junit.Ignore
import org.opencb.biodata.models.core.Gene
import org.opencb.cellbase.core.CellBaseConfiguration
import org.opencb.cellbase.core.serializer.CellBaseSerializer
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * Created by parce on 5/03/15.
 */
// TODO: fix test
@Ignore
class GeneParserTest extends Specification {


    static List<Gene> serializedGenes

    def setupSpec() {
        def geneTestDir = Paths.get(GeneParserTest.class.getResource("/geneParser").toURI())
        def genomeSequenceFasta = Paths.get(GeneParserTest.class.getResource("/geneParser/Homo_sapiens.GRCh38.fa").toURI())

        // custom test serializer that adds the serialized variants to a list
        def serializer = Mock(CellBaseSerializer)
        serializedGenes = new ArrayList<Gene>()
        serializer.serialize(_) >> { Gene arg -> serializedGenes.add(arg) }

        def species = new CellBaseConfiguration.SpeciesProperties.Species()
        def geneParser = new GeneParser(geneTestDir, genomeSequenceFasta, species, serializer)
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
        transcript.tfbs.size() == tfbsNumber

        where:
        transcriptId     || geneId            | name             | biotype   | chromosome | start | end   | strand | exonsNumber | tfbsNumber
        "ENST00000473358"|| "ENSG00000243485" | "MIR1302-11-001" | "lincRNA" | "1"        | 29554 | 31097 | "+"    | 3           | 3
        "ENST00000469289"|| "ENSG00000243485" | "MIR1302-11-002" | "lincRNA" | "1"        | 30267 | 31109 | "+"    | 2           | 3
        "ENST00000449442"|| "ENSG00000218839" | "FAM138C-001"    | "lincRNA" | "9"        | 34394 | 35860 | "-"    | 3           | 5
        "ENST00000305248"|| "ENSG00000218839" | "FAM138C-002"    | "lincRNA" | "9"        | 34965 | 35871 | "-"    | 2           | 5
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

    @Unroll
    def "transcript #transcriptId tfbs #name parsed"() {
        expect:
        def gene = serializedGenes.find({gene -> gene.id == geneId})
        def transcript = gene.transcripts.find({transcript -> transcript.id == transcriptId})
        def transcriptTfbs = transcript.tfbs.find({tfbs -> tfbs.chromosome == chromosome && tfbs.start == start && tfbs.end == end && tfbs.pwm == pwm})
        transcriptTfbs.tfName == name
        transcriptTfbs.relativeStart == relativeStart
        transcriptTfbs.relativeEnd == relativeEnd
        transcriptTfbs.score == score.floatValue()
        transcriptTfbs.strand == strand

        where:
        geneId            | transcriptId      || name   | pwm        | chromosome | start | end   | relativeStart | relativeEnd | score | strand
        "ENSG00000243485" | "ENST00000473358" || "ZEB1" | "MA0103.2" | "1"        | 27315 | 27323 | -2239         | -2231       | 1     | "+"
        "ENSG00000243485" | "ENST00000473358" || "Egr1" | "MA0162.2" | "1"        | 29343 | 29356 | -211          | -198        | 0.959 | "+"
        "ENSG00000243485" | "ENST00000473358" || "Egr1" | "MA0162.2" | "1"        | 29388 | 29401 | -166          | -153        | 0.958 | "+"
        "ENSG00000243485" | "ENST00000469289" || "Egr1" | "MA0162.2" | "1"        | 29343 | 29356 | -924          | -911        | 0.959 | "+"
        "ENSG00000243485" | "ENST00000469289" || "Egr1" | "MA0162.2" | "1"        | 29388 | 29401 | -879          | -866        | 0.958 | "+"
        "ENSG00000243485" | "ENST00000469289" || "Jund" | "MA0492.1" | "1"        | 30496 | 30510 | 230           | 244         | 0.986 | "+"
        "ENSG00000218839" | "ENST00000449442" || "Egr1" | "MA0366.1" | "9"        | 35390 | 35394 | 467           | 471         | 1     | "+"
        "ENSG00000218839" | "ENST00000449442" || "Egr1" | "MA0341.1" | "9"        | 36390 | 36394 | -534          | -530        | 1     | "+"
        "ENSG00000218839" | "ENST00000449442" || "Egr1" | "MA0366.1" | "9"        | 36565 | 36569 | -709          | -705        | 1     | "+"
        "ENSG00000218839" | "ENST00000449442" || "Egr1" | "MA0341.1" | "9"        | 36565 | 36569 | -709          | -705        | 1     | "+"
        "ENSG00000218839" | "ENST00000449442" || "Jund" | "MA0491.1" | "9"        | 36583 | 36593 | -733          | -723        | 0.993 | "+"
        "ENSG00000218839" | "ENST00000305248" || "Egr1" | "MA0366.1" | "9"        | 35390 | 35394 | 478           | 482         | 1     | "+"
        "ENSG00000218839" | "ENST00000305248" || "Egr1" | "MA0341.1" | "9"        | 36390 | 36394 | -523          | -519        | 1     | "+"
        "ENSG00000218839" | "ENST00000305248" || "Egr1" | "MA0366.1" | "9"        | 36565 | 36569 | -698          | -694        | 1     | "+"
        "ENSG00000218839" | "ENST00000305248" || "Egr1" | "MA0341.1" | "9"        | 36565 | 36569 | -698          | -694        | 1     | "+"
        "ENSG00000218839" | "ENST00000305248" || "Jund" | "MA0491.1" | "9"        | 36583 | 36593 | -722          | -712        | 0.993 | "+"
    }

    def cleanupSpec() {
        // delete reference genome sqlLite
        File referenceGenomeSqlLiteFile = Paths.get(GeneParserTest.class.getResource("/geneParser/reference_genome.db").toURI()).toFile()
        if (referenceGenomeSqlLiteFile.exists()) {
            referenceGenomeSqlLiteFile.delete()
        }
    }
}
