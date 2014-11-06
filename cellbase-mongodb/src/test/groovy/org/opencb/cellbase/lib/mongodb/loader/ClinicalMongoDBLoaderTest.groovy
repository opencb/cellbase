package org.opencb.cellbase.lib.mongodb.loader

import org.opencb.cellbase.core.serializer.CellBaseSerializer
import org.opencb.cellbase.lib.mongodb.serializer.CellbaseMongoDBSerializer

//import org.opencb.cellbase.lib.mongodb.serializer.CellbaseMongoDBSerializer
import spock.lang.Specification

import java.nio.file.Paths

/**
 * Created by parce on 11/4/14.
 */
class ClinicalMongoDBLoaderTest extends Specification {

    CellbaseMongoDBSerializer serializer = Mock()
    MongoDBLoader loader

    void setup() {
        def resourcesDir = Paths.get(getClass().getClassLoader().getResource("clinvar.json.gz").toURI()).getParent()
        loader = new ClinicalMongoDBLoader(serializer, resourcesDir)
    }

    void "load"() {
        when:
        loader.load()

        then:
        1 * serializer.init()
        7 * serializer.serialize(_)
        1 * serializer.close()
    }

}
