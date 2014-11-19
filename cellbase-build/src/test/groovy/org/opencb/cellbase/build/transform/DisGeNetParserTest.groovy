package org.opencb.cellbase.build.transform

import org.opencb.cellbase.core.serializer.CellBaseSerializer
import org.opencb.cellbase.build.transform.formats.DisGeNet
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * Created by parce on 10/24/14.
 */
class DisGeNetParserTest extends Specification {

    @Shared
    def disGeNetParser
    @Shared
    List<DisGeNet> serializedGenes

    void setupSpec() {
        // custom test serializer that adds the serialized variants to a list
        def serializer = Mock(CellBaseSerializer)
        serializedGenes = new ArrayList<DisGeNet>()
        serializer.serialize(_) >> { DisGeNet arg -> serializedGenes.add(arg) }

        // test files
        def disGeNetFile = Paths.get(VariantEffectParserTest.class.getResource("/disGeNetTest.csv").toURI())
        def entrezIdToEnsemlIdFile = Paths.get(VariantEffectParserTest.class.getResource("/entrezIdToEnsemblIdTest.csv").toURI())

        disGeNetParser = new DisGeNetParser(disGeNetFile, entrezIdToEnsemlIdFile, serializer)
    }

    def "Parse"() {
        when: "parse disgenet file"
        disGeNetParser.parse()

        then: "3 genes serialized"
        serializedGenes.size() == 3
    }

    @Unroll
    def "Serialized gene #geneSymbol has name '#geneName',ensembl id(s) #ensemblGeneIds and #diseases disease(s)"() {
        expect:
        def disGeNet = serializedGenes.find{ gene -> gene.geneSymbol.equals(geneSymbol) }
        disGeNet.geneName == geneName
        (disGeNet.geneEnsemblIds as Set).equals(ensemblGeneIds as Set)
        disGeNet.diseases.size() == diseases

        where:
        geneSymbol || geneName                             | ensemblGeneIds                | diseases
        "BLM"      || "Bloom syndrome, RecQ helicase-like" | ["ENSG00000197299", "LRG_20"] | 1
        "DYM"      || "dymeclin"                           | ["ENSG00000141627"]           | 2
        "LAMB3"    || "laminin, beta 3"                    | ["ENSG00000196878"]           | 1
    }

    @Unroll
    def "Serialized gene #geneSymbol has disease #diseaseId with name #diseaseName, score #score, #numPubMeds pubmeds, association types #associationTypes and sources #sources"() {
        expect:
        def disGeNet = serializedGenes.find{ gene -> gene.geneSymbol.equals(geneSymbol) }
        def disease = disGeNet.diseases.find{ disease -> disease.diseaseId.equals(diseaseId) }
        disease.diseaseName.equals(diseaseName)
        disease.score == new Float(score)
        disease.numberOfPubmeds == pubmeds
        (disease.associationTypes as Set).equals(associationTypes as Set)
        (disease.sources as Set).equals(sources as Set)

        where:
        geneSymbol || diseaseId       | diseaseName                                           | score             | pubmeds | associationTypes                                       | sources
        "BLM"      || "umls:C0005859" | "Bloom Syndrome"                                      | 0.744459274113461 | 63      | ["GeneticVariation", "Biomarker", "AlteredExpression"] | ["UNIPROT", "CTD_human", "MGD", "GAD", "LHGDN", "BeFree"]
        "DYM"      || "umls:C1846431" | "Smith-McCort Dysplasia"                              | 0.705971165243041 | 22      | ["GeneticVariation", "Biomarker"]                      |  ["UNIPROT", "CTD_human", "MGD", "BeFree"]
        "DYM"      || "umls:C0265286" | "Dyggve-Melchior-Clausen syndrome"                    | 0.702089907835064 | 8       | ["GeneticVariation", "Biomarker"]                      |  ["UNIPROT", "CTD_human", "MGD", "BeFree"]
        "LAMB3"    || "umls:C0268374" | "Epidermolysis Bullosa, Junctional, Non-Herlitz Type" | 0.7               | 1       | ["GeneticVariation"]                                   |  ["UNIPROT", "CTD_human", "MGD"]
    }


}
