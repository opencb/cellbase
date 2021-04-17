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
import org.opencb.cellbase.core.api.TranscriptQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.lib.impl.core.TranscriptMongoDBAdaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TranscriptManager extends AbstractManager implements AggregationApi<TranscriptQuery, Transcript>  {

    private TranscriptMongoDBAdaptor transcriptDBAdaptor;

    public TranscriptManager(String species, CellBaseConfiguration configuration) throws CellBaseException {
        this(species, null, configuration);
    }

    public TranscriptManager(String species, String assembly, CellBaseConfiguration configuration) throws CellBaseException {
        super(species, assembly, configuration);

        this.init();
    }

    private void init() {
        transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor();
    }

    @Override
    public CellBaseCoreDBAdaptor<TranscriptQuery, Transcript> getDBAdaptor() {
        return transcriptDBAdaptor;
    }

    public List<CellBaseDataResult<Transcript>> info(List<String> ids, ProjectionQueryOptions query, String source) {
        return transcriptDBAdaptor.info(ids, query, source);
    }


    public CellBaseDataResult<String> getCdna(String id) {
        TranscriptQuery query = new TranscriptQuery();
        query.setTranscriptsXrefs(Collections.singletonList(id));
        CellBaseDataResult<Transcript> transcriptCellBaseDataResult = transcriptDBAdaptor.query(query);
        String cdnaSequence = null;
        if (transcriptCellBaseDataResult != null && !transcriptCellBaseDataResult.getResults().isEmpty()) {
            for (Transcript transcript: transcriptCellBaseDataResult.getResults()) {
                // transcript.id will have version. id is from the user, so can include the version or not.
                if (transcript.getId().startsWith(id)) {
                    cdnaSequence = transcript.getCdnaSequence();
                    break;
                }
            }
        }
        return new CellBaseDataResult<>(id, transcriptCellBaseDataResult.getTime(), transcriptCellBaseDataResult.getEvents(),
                transcriptCellBaseDataResult.getNumResults(), Collections.singletonList(cdnaSequence), 1);
    }

    private List<CellBaseDataResult<String>> getCdna(List<String> idList) {
        List<CellBaseDataResult<String>> cellBaseDataResults = new ArrayList<>();
        for (String id : idList) {
            cellBaseDataResults.add(getCdna(id));
        }
        return cellBaseDataResults;
    }

    public List<CellBaseDataResult<String>> getSequence(String id) {
        List<String> transcriptsList = Arrays.asList(id.split(","));
        List<CellBaseDataResult<String>> queryResult = getCdna(transcriptsList);
        for (int i = 0; i < transcriptsList.size(); i++) {
            queryResult.get(i).setId(transcriptsList.get(i));
        }
        return queryResult;
    }


}

