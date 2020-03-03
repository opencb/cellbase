/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.impl.core;

import org.junit.jupiter.api.Test;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Created by fjlopez on 09/05/16.
 */
public class XRefMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {
    public XRefMongoDBAdaptorTest() throws Exception {
        super();
        setUp();
    }

    public void setUp() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/xref/gene.test.json.gz").toURI());
        loadRunner.load(path, "gene");
    }

    @Test
    public void contains() throws Exception {
        XRefMongoDBAdaptor xRefDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor("hsapiens", "GRCh37");
//        CellBaseDataResult xrefs = xRefDBAdaptor.contains("BRCA2", new QueryOptions());
//        Set<String> reference = new HashSet<>(Arrays.asList("ENSG00000185515", "ENSG00000139618", "ENSG00000107949",
//                "ENSG00000083093", "ENSG00000170037"));
//        Set<String> set = (Set) xrefs.getResults().stream()
//                .map(result -> ((String) ((Document) result).get("id"))).collect(Collectors.toSet());
//        assertEquals(reference, set);
    }

}