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
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResponse;
import org.opencb.commons.datastore.core.QueryOptions;


import java.io.IOException;
import java.util.List;

/**
 * Created by swaathi on 20/05/16.
 */
public class TranscriptClient extends ParentRestClient<Transcript> {

    public TranscriptClient(String species, String assembly, ClientConfiguration configuration) {
        super(species, assembly, configuration);
        this.clazz = Transcript.class;

        this.category = "feature";
        this.subcategory = "transcript";
    }


    public CellBaseDataResponse<Gene> getGene(String id, QueryOptions options) throws IOException {
        return execute(id, "gene", options, Gene.class);
    }

    public CellBaseDataResponse<Variant> getVariation(String id, QueryOptions options) throws IOException {
        return execute(id, "variation", options, Variant.class);
    }

    public CellBaseDataResponse<String> getSequence(String id, QueryOptions options) throws IOException {
        return execute(id, "sequence", options, String.class);
    }

    public CellBaseDataResponse<Entry> getProtein(String id, QueryOptions options) throws IOException {
        return execute(id, "protein", options, Entry.class);
    }

    public CellBaseDataResponse<List> getProteinFunctionPrediction(String id, QueryOptions options) throws IOException {
        return execute(id, "function_prediction", options, List.class);
    }
}
