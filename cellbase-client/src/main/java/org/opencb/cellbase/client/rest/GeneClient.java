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

import org.opencb.biodata.formats.protein.uniprot.v202003jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.TranscriptTfbs;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResponse;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.io.IOException;
import java.util.List;

/**
 * Created by imedina on 12/05/16.
 */
public class GeneClient extends FeatureClient<Gene> {

    public GeneClient(String species, String assembly, ClientConfiguration clientConfiguration) {
        super(species, assembly, clientConfiguration);

        this.clazz = Gene.class;

        this.category = "feature";
        this.subcategory = "gene";
    }


    public CellBaseDataResponse<String> getBiotypes(Query query) throws IOException {
        return execute("distinct", query, new QueryOptions("field", "biotype"), String.class);
    }

    @Deprecated
    public CellBaseDataResponse<Gene> list(Query query) throws IOException {
        return execute("list", query, new QueryOptions(), Gene.class);
    }

    public CellBaseDataResponse<Transcript> getTranscript(String id, QueryOptions queryOptions) throws IOException {
        return execute(id, "transcript", queryOptions, Transcript.class);
    }

    public CellBaseDataResponse<TranscriptTfbs> getTfbs(String id, QueryOptions queryOptions) throws IOException {
        return execute(id, "tfbs", queryOptions, TranscriptTfbs.class);
    }

    public CellBaseDataResponse<Variant> getVariation(List<String> id, QueryOptions queryOptions) throws IOException {
        return execute(id, "variant", queryOptions, Variant.class);
    }

    public CellBaseDataResponse<Entry> getProtein(String id, QueryOptions queryOptions) throws IOException {
        return execute(id, "protein", queryOptions, Entry.class);
    }

}
