package org.opencb.cellbase.lib.mongodb.db;

import org.junit.Test;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.core.CellbaseConfiguration;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.variation.VariantAnnotationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;

public class VariantAnnotationMongoDBAdaptorTest {


    @Test
    public void testGetAllConsequenceTypesByVariant() {

        CellbaseConfiguration config = new CellbaseConfiguration();

        config.addSpeciesConnection("hsapiens", "GRCh37", "mongodb-hxvm-var-001.ebi.ac.uk", "hsapiens_cb_v3", 27017, "mongo", "biouser",
                "B10p@ss", 10, 10);
        config.addSpeciesAlias("hsapiens","hsapiens");

        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(config);

        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = dbAdaptorFactory.getGenomicVariantAnnotationDBAdaptor("hsapiens", "GRCh37");

        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("13", 32889622, "G", "A"), new QueryOptions());

    }
}