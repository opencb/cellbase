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

package org.opencb.cellbase.client.rest;

import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.CellBaseDataResponse;
import org.opencb.cellbase.core.api.core.ClinicalDBAdaptor;

import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;


import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by fjlopez on 07/07/17.
 */
public class ClinicalVariantClientTest {

    private CellBaseClient cellBaseClient;

    public ClinicalVariantClientTest() {
        try {
            cellBaseClient = new CellBaseClient(ClientConfiguration.load(getClass().getResourceAsStream("/client-configuration-test.yml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void search() throws Exception {
        CellBaseDataResponse<Variant> queryResponse = cellBaseClient
                .getClinicalClient()
                .search(new Query(ClinicalDBAdaptor.QueryParams.SOURCE.key(), "clinvar"),
                        new QueryOptions(QueryOptions.LIMIT, 3));
        assertTrue(queryResponse.getResponses().get(0).getNumTotalResults() > 100000);

        queryResponse = cellBaseClient
                .getClinicalClient()
                .search(new Query(ClinicalDBAdaptor.QueryParams.SOURCE.key(), "cosmic"),
                        new QueryOptions(QueryOptions.LIMIT, 3));
        assertTrue(queryResponse.getResponses().get(0).getNumTotalResults() > 2000000);
    }

    @Test
    public void alleleOriginLabels() throws Exception {
        CellBaseDataResponse<String> queryResponse = cellBaseClient.getClinicalClient().alleleOriginLabels();
        assertTrue(queryResponse.allResults().size() > 0);
    }

    @Test
    public void clinsigLabels() throws Exception {
        CellBaseDataResponse<String> queryResponse = cellBaseClient.getClinicalClient().clinsigLabels();
        assertTrue(queryResponse.allResults().size() > 0);
    }

    @Test
    public void consistencyLabels() throws Exception {
        CellBaseDataResponse<String> queryResponse = cellBaseClient.getClinicalClient().consistencyLabels();
        assertTrue(queryResponse.allResults().size() > 0);
    }

    @Test
    public void modeInheritanceLabels() throws Exception {
        CellBaseDataResponse<String> queryResponse = cellBaseClient.getClinicalClient().modeInheritanceLabels();
        assertTrue(queryResponse.allResults().size() > 0);
    }

    @Test
    public void variantTypes() throws Exception {
        CellBaseDataResponse<String> queryResponse = cellBaseClient.getClinicalClient().variantTypes();
        assertTrue(queryResponse.allResults().size() > 0);
    }

}
