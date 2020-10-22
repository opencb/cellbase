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

import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.cellbase.lib.impl.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.queries.RepeatsQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.lib.impl.core.RepeatsMongoDBAdaptor;

public class RepeatsManager extends AbstractManager implements AggregationApi<RepeatsQuery, Repeat> {

    private RepeatsMongoDBAdaptor repeatsDBAdaptor;

    public RepeatsManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() {
        repeatsDBAdaptor = dbAdaptorFactory.getRepeatsDBAdaptor(species, assembly);
    }

    @Override
    public CellBaseCoreDBAdaptor<RepeatsQuery, Repeat> getDBAdaptor() {
        return repeatsDBAdaptor;
    }
}
