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

import org.opencb.biodata.models.core.pgs.VariantPolygenicScore;
import org.opencb.biodata.models.variant.avro.PolygenicScoreAnnotation;
import org.opencb.cellbase.core.api.PolygenicScoreQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.lib.impl.core.PolygenicScoreMongoDBAdaptor;

import java.util.List;

public class PolygenicScoreManager extends AbstractManager implements AggregationApi<PolygenicScoreQuery, VariantPolygenicScore> {

    private PolygenicScoreMongoDBAdaptor pgsDBAdaptor;

    public PolygenicScoreManager(String species, CellBaseConfiguration configuration) throws CellBaseException {
        this(species, null, configuration);
    }

    public PolygenicScoreManager(String species, String assembly, CellBaseConfiguration configuration) throws CellBaseException {
        super(species, assembly, configuration);

        this.init();
    }

    private void init() {
        pgsDBAdaptor = dbAdaptorFactory.getPolygenicScoreMongoDBAdaptor();
    }

    @Override
    public CellBaseCoreDBAdaptor<PolygenicScoreQuery, VariantPolygenicScore> getDBAdaptor() {
        return pgsDBAdaptor;
    }

    public List<CellBaseDataResult<VariantPolygenicScore>> info(List<String> ids, ProjectionQueryOptions query, int dataRelease,
                                               String apiKey) throws CellBaseException {
        return pgsDBAdaptor.info(ids, query, dataRelease, apiKey);
    }

    public CellBaseDataResult<PolygenicScoreAnnotation> getPolygenicScoreAnnotation(String chromosome, Integer start, String reference,
                                                                                    String alternate, int dataRelease)
            throws CellBaseException {
        return pgsDBAdaptor.getPolygenicScoreAnnotation(chromosome, start, reference, alternate, dataRelease);
    }
}
