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

import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by imedina on 26/05/16.
 */
public class GenomicRegionClient extends ParentRestClient<GenomeSequenceFeature> {

    public GenomicRegionClient(ClientConfiguration clientConfiguration) {
        super(clientConfiguration);

        this.clazz = GenomeSequenceFeature.class;

        this.category = "genomic";
        this.subcategory = "region";
    }

    public QueryResponse<Variant> getVariation(List<String> id, Map<String, Object> params) throws IOException {
        return execute(id, "variation", params, Variant.class);
    }
}
