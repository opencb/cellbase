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
import org.opencb.cellbase.core.serializer.CellBaseSerializer
import org.opencb.cellbase.core.common.clinical.ClinvarPublicSet
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * Created by lcruz on 23/10/14.
 */
// TODO: fix test
@Ignore
class ClinVarParserTest extends Specification {

    @Shared
    def clinvarParser
    @Shared
    def clinvarParserWithoutEFO
    @Shared
    List<ClinvarPublicSet> serializedVariants
    @Shared
    List<ClinvarPublicSet> noEfoSerializedVariants


    def setupSpec() {
        // custom test serializers that adds the serialized variants to a list
        def serializer = Mock(CellBaseSerializer)
        def noEfoSerializer = Mock(CellBaseSerializer)
        serializedVariants = new ArrayList<ClinvarPublicSet>()
        noEfoSerializedVariants = new ArrayList<ClinvarPublicSet>()
        serializer.serialize(_) >> { ClinvarPublicSet arg -> serializedVariants.add(arg) }
        noEfoSerializer.serialize(_) >> { ClinvarPublicSet arg -> noEfoSerializedVariants.add(arg) }

        def clinvarXmlFile = Paths.get(ClinVarParserTest.class.getResource("/clinvarParser/clinvar_v19_test.xml.gz").toURI())
        def efosFile = Paths.get(ClinVarParserTest.class.getResource("/clinvarParser/ClinVar_Traits_EFO_Names.csv").toURI())

        clinvarParser = new ClinVarParser(clinvarXmlFile, efosFile,ClinVarParser.GRCH37_ASSEMBLY, serializer)
        clinvarParserWithoutEFO = new ClinVarParser(clinvarXmlFile, null, ClinVarParser.GRCH37_ASSEMBLY, noEfoSerializer)
    }

    def "Parse"() {
        when:
        clinvarParser.parse()
        clinvarParserWithoutEFO.parse()
        then: "serialize 3 variants"
        serializedVariants.size() == 3
        noEfoSerializedVariants.size() == 3
    }

    @Unroll
    def "parsed variant #chr:#start-#end #ref #alt has clinvarset ID #clinvarID, reference accession #referenceClinvarAccession"() {
        expect:
        def variant = serializedVariants.find {v -> v.clinvarSet.ID == clinvarID}
        def noEfoVariant = noEfoSerializedVariants.find {v -> v.clinvarSet.ID == clinvarID}
        variant.chromosome.equals(chr)
        variant.start == start
        variant.end == end
        variant.reference.equals(ref)
        variant.alternate.equals(alt)
        variant.source.equals(source)
        variant.clinvarSet.referenceClinVarAssertion.clinVarAccession.acc == referenceClinvarAccession
        noEfoVariant.chromosome.equals(chr)
        noEfoVariant.start == start
        noEfoVariant.end == end
        noEfoVariant.reference.equals(ref)
        noEfoVariant.alternate.equals(alt)
        noEfoVariant.source.equals(source)
        noEfoVariant.clinvarSet.referenceClinVarAssertion.clinVarAccession.acc == referenceClinvarAccession

        where:
        clinvarID || chr  | start     | end       | ref | alt  | source    | referenceClinvarAccession
        3489885   || "12" | 2795019   | 2795019   | "C" | "T"  | "clinvar" | "RCV000079303"
        3193940   || "14" | 24709794  | 24709794  | "G" | "-"  | "clinvar" | "RCV000032179"
        3194294   || "4"  | 187120195 | 187120196 | "A" | "AA" | "clinvar" | "RCV000032546"
    }

    @Unroll
    def "parsed variant #clinvarID has preferred trait '#traitName', EFO term name '#efoName', EFO term ID #efoId and EFO URL #efoURL"() {
        expect:
        def variant = serializedVariants.find {v -> v.clinvarSet.ID == clinvarID}
        def traitElements = variant.clinvarSet.referenceClinVarAssertion.traitSet.trait.first().name.elementValue
        def preferredTraitName = traitElements.find{e -> e.type == "Preferred"}.value
        def traitEfoNameElement = traitElements.find{e -> e.type == ClinVarParser.EFO_NAME}
        def traitEfoName = traitEfoNameElement == null ? null : traitEfoNameElement.value
        def traitEfoIdElement = traitElements.find{e -> e.type == ClinVarParser.EFO_ID}
        def traitEfoId = traitEfoIdElement == null ? null : traitEfoIdElement.value
        def traitEfoURLElement = traitElements.find{e -> e.type == ClinVarParser.EFO_URL}
        def traitEfoURL = traitEfoURLElement == null ? null : traitEfoURLElement.value
        preferredTraitName == traitName
        traitEfoName == efoName
        traitEfoId == efoId
        traitEfoURL == efoURL

        where:
        clinvarID || traitName                                    | efoName                  | efoId           | efoURL
        3489885   || "not specified"                              | null                     | null            | null
        3193940   || "Dyskeratosis congenita autosomal dominant"  | "Dyskeratosis congenita" | "Orphanet:1775" | "http://www.orpha.net/ORDO/Orphanet_1775"
        3194294   || "Bietti crystalline corneoretinal dystrophy" | null                     | null            | null
    }
}