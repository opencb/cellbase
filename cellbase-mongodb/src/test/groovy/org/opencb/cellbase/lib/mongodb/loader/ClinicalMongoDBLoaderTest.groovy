package org.opencb.cellbase.lib.mongodb.loader

import org.opencb.cellbase.lib.mongodb.model.ClinicalVariation
import org.opencb.cellbase.lib.mongodb.serializer.CellbaseMongoDBSerializer
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * Created by parce on 11/4/14.
 */
class ClinicalMongoDBLoaderTest extends Specification {

    @Shared
    def resourcesDir

    void setupSpec() {
        resourcesDir = Paths.get(getClass().getClassLoader().getResource("clinvar.json.gz").toURI()).getParent()
    }

    void "Loader initializes serializer, serialize 7 variants and closes serializer"() {
        given:
        CellbaseMongoDBSerializer serializer = Mock()
        MongoDBLoader loader = new ClinicalMongoDBLoader(serializer, resourcesDir)

        when:
        loader.load()

        then:
        1 * serializer.init()
        7 * serializer.serialize(_)
        1 * serializer.close()
    }

    @Unroll
    void "loaded variation #chr:#start-#end #ref #alt has #cosmic cosmic objects, #clinvar clinvar objects and #gwas gwas objects"() {
        given:
        // custom test serializer that adds the serialized variants to a list
        List<ClinicalVariation> serializedVariants = new ArrayList<>()
        CellbaseMongoDBSerializer listSerializer = Mock()
        listSerializer.serialize(_) >> { ClinicalVariation arg -> serializedVariants.add(arg) }

        MongoDBLoader loader = new ClinicalMongoDBLoader(listSerializer, resourcesDir)
        loader.load()

        expect:
        def clinicalVariation = serializedVariants.find{ variant -> variant.chromosome == chr && variant.start == start &&
                                                                    variant.reference == ref && variant.alternate == alt }
        def cosmicObjects = clinicalVariation.cosmicList ? clinicalVariation.cosmicList.size() : 0
        def clinvarObjects = clinicalVariation.clinvarList ? clinicalVariation.clinvarList.size() : 0
        def gwasObjects = clinicalVariation.gwasList ? clinicalVariation.gwasList.size() : 0
        cosmic == cosmicObjects
        clinvar == clinvarObjects
        gwas == gwasObjects

        where:
        chr  | start     | end       | ref | alt || cosmic | clinvar | gwas
        "1"  | 109817590 | 109817590 | "G" | "T" || 0      | 1       | 1
        "9"  | 22137685  | 22137685  | "T" | "G" || 0      | 0       | 1
        "10" | 129839177 | 129839177 | "G" | "A" || 1      | 1       | 0
        "11" | 20623023  | 20623023  | "C" | "T" || 3      | 0       | 1
        "19" | 45411941  | 45411941  | "T" | "C" || 1      | 5       | 1
        "X"  | 54950197  | 54950197  | "G" | "A" || 4      | 0       | 0
        "Y"  | 28501735  | 28501735  | "G" | "A" || 0      | 1       | 0
    }

}
