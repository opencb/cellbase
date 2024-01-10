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
import org.opencb.biodata.models.core.Xref;
import org.opencb.cellbase.core.api.XrefQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.XrefManager;

import java.util.Collections;
import java.util.List;

import static org.bson.assertions.Assertions.fail;


/**
 * Created by fjlopez on 09/05/16.
 */
public class XRefMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public XRefMongoDBAdaptorTest() throws Exception {
        super();
    }

    @Test
    public void queryTest() throws Exception {
        XrefManager xrefManager = cellBaseManagerFactory.getXrefManager(SPECIES, ASSEMBLY);
        XrefQuery query = new XrefQuery();
        query.setIds(Collections.singletonList("BRCA1"));
        query.setDataRelease(dataRelease.getRelease());
        List<CellBaseDataResult<Xref>> resultList = xrefManager.search(Collections.singletonList(query));
        CellBaseDataResult<Xref> result = resultList.get(0);
        boolean found = false;
        for (Xref xref : result.getResults()) {
            if (xref.getId().equals("ENSG00000012048")) {
                found = true;
                break;
            }
        }
        if (!found) {
            fail();
        }
    }
}