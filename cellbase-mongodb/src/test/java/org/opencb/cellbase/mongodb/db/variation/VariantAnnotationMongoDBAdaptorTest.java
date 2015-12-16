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

package org.opencb.cellbase.mongodb.db.variation;

import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.db.DBAdaptorFactory;
import org.opencb.cellbase.core.db.api.variation.VariantAnnotationDBAdaptor;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by imedina on 09/10/15.
 */
public class VariantAnnotationMongoDBAdaptorTest {

    @Test
    public void testGetAnnotationByVariant() throws Exception {

        Variant variant = new Variant("19:45411941:T:C");

        Path inputPath = Paths.get(getClass().getResource("/configuration.json").toURI());
        CellBaseConfiguration cellBaseConfiguration = CellBaseConfiguration.load(new FileInputStream(inputPath.toFile()));

        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        VariantAnnotationDBAdaptor variantAnnotationMongoDBAdaptor = dbAdaptorFactory.getVariantAnnotationDBAdaptor("hsapiens", "grch37");

//        QueryResult annotationByVariant = variantAnnotationMongoDBAdaptor.getAnnotationByVariant(variant, new QueryOptions());

//        assertNotNull(annotationByVariant);
    }
}