package org.opencb.cellbase.build.transform

import net.sf.picard.reference.IndexedFastaSequenceFile
import net.sf.picard.reference.ReferenceSequence
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Created by parce on 6/5/14.
 */
class ClinvarParserTest extends Specification {

    private static ClinvarParser clinvarParser
    private static Path outputFile

    void setupSpec() {
        setup: "load the reference fasta file"
        URL clinvarXmlFileResource = getClass().getClassLoader().getResource("clinvarExample.xml")
        def clinvarPath = Paths.get(clinvarXmlFileResource.toURI())
        URL outputFileResource = getClass().getClassLoader().getResource("clinvar-example-output.txt")
        outputFile = Paths.get(outputFileResource.toURI())
        // TODO: borrar cuando este la secuencia
        URL referenceFastaResource = getClass().getClassLoader().getResource("testReferenceSequence.fasta")
        URL referenceFastaIndexResource = getClass().getClassLoader().getResource("testReferenceSequence.fasta.fai")
        def referenceFastaFile = Paths.get(referenceFastaResource.toURI())
        def referenceFastaIndexFile = Paths.get(referenceFastaIndexResource.toURI())
        clinvarParser = new ClinvarParser(clinvarPath, referenceFastaFile, referenceFastaIndexFile, outputFile)
        // TODO fin del bloque a borrar
//        clinvarParser = new ClinvarParser(clinvarPath, outputFile)
//        IndexedFastaSequenceFile fastaFile = Mock()
//        ReferenceSequence sequence = Mock()
//        ReferenceSequence seq = new ReferenceSequence("Y", 23, "")
//        fastaFile.getSubsequenceAt(_, _, _) >> sequence
//        clinvarParser.setGenomeSequenceFastaFile(fastaFile)
    }

    // TODO: no va a hacer falta, el fichero se crea cada vez y se vacia
//    void cleanupSpec() {
//        cleanup:
//        outputFile.toFile().c()
//    }

    def "parse clinvar"() {
        when:
        clinvarParser.parseClinvar()

        then:
        outputFile.toFile().exists() == true
    }
}
