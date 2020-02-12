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

import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.api.core.TranscriptDBAdaptor;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Arrays;
import java.util.List;

public class TranscriptManager extends AbstractManager {

    public TranscriptManager(CellBaseConfiguration configuration) {
        super(configuration);
    }

    public CellBaseDataResult<Transcript> search(Query query, QueryOptions queryOptions, String species, String assembly) {
        logger.debug("blahh...");
        TranscriptDBAdaptor dbAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
        return dbAdaptor.nativeGet(query, queryOptions);
    }

    public CellBaseDataResult<Transcript> groupBy(Query query, QueryOptions queryOptions, String species, String assembly, String fields) {
        logger.debug("blahh...");
        TranscriptDBAdaptor dbAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
        return dbAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
    }

    public CellBaseDataResult<Transcript> aggregationStats(Query query, QueryOptions queryOptions, String species, String assembly, String fields) {
        logger.debug("blahh...");
        TranscriptDBAdaptor dbAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
        queryOptions.put(QueryOptions.COUNT, true);
        return dbAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
    }

    public List<CellBaseDataResult> info(Query query, QueryOptions queryOptions, String species, String assembly, String genes) {
        logger.debug("blahh...");
        TranscriptDBAdaptor dbAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
        List<Query> queries = createQueries(genes, TranscriptDBAdaptor.QueryParams.XREFS.key());
        List<CellBaseDataResult> queryResults = dbAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < queries.size(); i++) {
            queryResults.get(i).setId((String) queries.get(i).get(TranscriptDBAdaptor.QueryParams.XREFS.key()));
        }
        return queryResults;
    }
}
