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

package org.opencb.cellbase.client.rest;

import org.opencb.biodata.models.core.Gene;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by imedina on 12/05/16.
 */
public class GeneClient extends ParentRestClient {


    public GeneClient(ClientConfiguration clientConfiguration) {
        super(clientConfiguration);

        this.category = "feature";
        this.subcategory = "gene";
    }

    public QueryResponse<String> distinct(Query query) throws IOException {
        return execute("biotype", query, null, String.class);
    }

    public QueryResponse<Gene> get(String id, Map<String, Object> params, QueryOptions options) throws IOException {
        return execute(id, "info", params, options, Gene.class);
    }

    public QueryResponse<Gene> search(Query query, QueryOptions options) throws IOException {
        return execute("all", query, options, Gene.class);
    }


}
