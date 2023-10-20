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

import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResponse;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;


import java.io.IOException;

/**
 * Created by fjlopez on 07/07/17.
 */
public class ClinicalVariantClient extends ParentRestClient<Variant> {

    ClinicalVariantClient(String species, String assembly, String dataRelease, String apiKey, ClientConfiguration configuration) {
        super(species, assembly, dataRelease, apiKey, configuration);

        this.clazz = Variant.class;

        this.category = "clinical";
        this.subcategory = "variant";

    }

    public CellBaseDataResponse<Variant> search(Query query, QueryOptions queryOptions) throws IOException {
        return execute("search", query, queryOptions, clazz);
    }

    public CellBaseDataResponse<String> alleleOriginLabels() throws IOException {
        return execute("alleleOriginLabels", new Query(), new QueryOptions(), String.class);
    }

    public CellBaseDataResponse<String> clinsigLabels() throws IOException {
        return execute("clinsigLabels", new Query(), new QueryOptions(), String.class);
    }

    public CellBaseDataResponse<String> consistencyLabels() throws IOException {
        return execute("consistencyLabels", new Query(), new QueryOptions(), String.class);
    }

    public CellBaseDataResponse<String> modeInheritanceLabels() throws IOException {
        return execute("modeInheritanceLabels", new Query(), new QueryOptions(), String.class);
    }

    public CellBaseDataResponse<String> variantTypes() throws IOException {
        return execute("type", new Query(), new QueryOptions(), String.class);
    }
}
