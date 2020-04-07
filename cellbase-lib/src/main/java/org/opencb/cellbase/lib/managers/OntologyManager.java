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

package org.opencb.cellbase.lib.managers;

import org.opencb.biodata.models.core.OntologyTerm;
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.queries.OntologyQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.lib.impl.core.OntologyMongoDBAdaptor;

public class OntologyManager extends AbstractManager implements AggregationApi<OntologyQuery, OntologyTerm> {

    private OntologyMongoDBAdaptor ontologyMongoDBAdaptor;

    public OntologyManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);

        this.init();
    }

    private void init() {
        ontologyMongoDBAdaptor = dbAdaptorFactory.getOntologyMongoDBAdaptor(species, assembly);
    }

    @Override
    public CellBaseCoreDBAdaptor<OntologyQuery, OntologyTerm> getDBAdaptor() {
        return ontologyMongoDBAdaptor;
    }
}
