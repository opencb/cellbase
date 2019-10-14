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

import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.biodata.models.core.Xref;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;

/**
 * Created by swaathi on 23/05/16.
 */
public class VariationClient extends FeatureClient<Variant> {

    public VariationClient(String species, String assembly, ClientConfiguration configuration) {
        super(species, assembly, configuration);
        this.clazz = Variant.class;

        this.category = "feature";
        this.subcategory = "variation";
    }

    public QueryResponse<String> getAllConsequenceTypes(Query query) throws IOException {
        return execute("consequence_types", query, new QueryOptions(), String.class);
    }

    public QueryResponse<String> getConsequenceTypeById(String id, QueryOptions options) throws IOException {
        return execute(id, "consequence_type", options, String.class);
    }
//    check data model returned
    public QueryResponse<RegulatoryFeature> getRegulatory(String id, QueryOptions options) throws IOException {
        return execute(id, "regulatory", options, RegulatoryFeature.class);
    }

    public QueryResponse<String> getPhenotype(String id, QueryOptions options) throws IOException {
        return execute(id, "phenotype", options, String.class);
    }

    public QueryResponse<String> getSequence(String id, QueryOptions options) throws IOException {
        return execute(id, "sequence", options, String.class);
    }

    public QueryResponse<String> getPopulationFrequency(String id, QueryOptions options) throws IOException {
        return execute(id, "population_frequency", options, String.class);
    }

    public QueryResponse<Xref> getXref(String id, QueryOptions options) throws IOException {
        return execute(id, "xref", options, Xref.class);
    }

}
